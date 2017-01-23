package com.shendeng.datamanager;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONObject;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.csvreader.CsvReader;
import com.shendeng.utils.Const;
import com.shendeng.utils.JedisUtil.Hash;
import com.shendeng.utils.DateTool;
import com.shendeng.utils.RedisUtil;
import com.shendeng.utils.StockUtils;
import com.shendeng.utils.StrTool;
import com.shendeng.utils.WmcloudUtil;
import com.shendeng.web.QuotationAct;

/**
 * 股票相关数据的处理
 * 
 * @author qy
 * @date 2016年5月6日
 */
public class StockInfoManager {

	private static StockInfoManager sim = new StockInfoManager();

	public static StockInfoManager getInstance() {
		return sim;
	}

	Hash hash = RedisUtil.getJedisUtil().HASH;

	/**
	 * 跟新股票基本信息
	 * 
	 * @return
	 * @throws IOException
	 * 
	 */
	public boolean getEqu() {
		try {
			long a = System.currentTimeMillis();
			//清除内存中的股票代码
			Cache.cacheStockListRemoveAll();
			CsvReader reader = WmcloudUtil.url2csv(Const.EQU);
			if (reader != null) {
				reader.readRecord();
				while (reader.readRecord()) {
					// 更新缓存
					String[] items = reader.getValues();
					// 在原有数据基础上添加三个数组下标
					items = StockUtils.arrayAddLength(items, 4);
					String simpleCode = StockUtils.getLTCode(reader.get(2)) + reader.get(1);
					if("L".equals(items[Const.INFO_listStatusCD])){
						//添加到内存中
						Cache.cacheStockListAdd(items);
						
						items[Const.INFO_industryID1] = "";
						items[Const.INFO_industryName1] = "";
						items[Const.INFO_isSuspend] = "0";// 是否停牌 0 未停牌 1 停牌
						items[Const.INFO_assetClass] = "";
						hash.hset(Const.RKEY_STOCK_INFO_, simpleCode, JSON.toJSONString(items));
					}else{
						hash.hdel(Const.RKEY_STOCK_INFO_, simpleCode);
					}
				}
				reader.close();
			}
			System.out.println("股票基本信息同步用时" + (System.currentTimeMillis() - a));
			return true;
		} catch (Exception e) {
			new Exception("跟新股票基本信息异常" + DateTool.DateToStr(new Date(), DateTool.TIME_FORMAT)).printStackTrace();
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 合并利润表(最近)
	 * 
	 * @throws IOException
	 * 
	 */
	public void getFdmtislately() throws IOException {
		try {
			CsvReader reader = WmcloudUtil.url2csv(Const.FDMTISLATELY);
			if(reader != null){
				reader.readRecord();
				while (reader.readRecord()) {
					String[] items = reader.getValues();
					if(items[Const.FdmtISLately_secID] != null && items[Const.FdmtISLately_secID].length() > 5){
						String code = StockUtils.getStockCode(items[Const.FdmtISLately_secID]);
						hash.hset(Const.PKEY_FDMTISLATELY, code, JSON.toJSONString(items));
					}
				}
				reader.close();
			}
		} catch (Exception e) {
			new Exception("合并利润表(最近)" + DateTool.DateToStr(new Date(), DateTool.TIME_FORMAT)).printStackTrace();
			e.printStackTrace();
		}
	}

	/**
	 * 跟新股票停复盘信息
	 * 
	 * @throws IOException
	 * 
	 */
	public void getSecTips() throws IOException {
		Map<String,String> allStock = hash.hgetAll(Const.RKEY_STOCK_INFO_);
		for (Object key : allStock.keySet()) {
			String simpleCode = StrTool.toString(key);
			JSONArray ary = JSON.parseArray(allStock.get(simpleCode));
			if (ary != null) {
				ary.set(Const.INFO_isSuspend, "0");
				hash.hset(Const.RKEY_STOCK_INFO_, simpleCode, JSON.toJSONString(ary));
			}
		}
		
		try {
			CsvReader reader = WmcloudUtil.url2csv(Const.SECTIPS);
			if (reader != null) {
				reader.readRecord();
				while (reader.readRecord()) {
					String[] items = reader.getValues();
					String code = StockUtils.getStockCode(items[0]);
					JSONArray ary = JSON.parseArray(hash.hget(Const.RKEY_STOCK_INFO_, code));
					// 如果信息不为空，数据就没有停牌
					if (ary != null) {
						ary.set(Const.INFO_isSuspend, "1");
						hash.hset(Const.RKEY_STOCK_INFO_, code, JSON.toJSONString(ary));
					}
				}
				reader.close();
				System.out.println("股票停复牌跟新完毕"+DateTool.DateToStr(new Date(), DateTool.TIME_FORMAT));
			}
			QuotationAct.setSearch();
		} catch (Exception e) {
			new Exception("跟新股票停复盘信息" + DateTool.DateToStr(new Date(), DateTool.TIME_FORMAT)).printStackTrace();
			e.printStackTrace();
		}
	}

	/**
	 * 获取通联地域分类
	 * 
	 * @param leave
	 *            地区级别
	 * @return
	 * @throws IOException
	 */
	public void initStockRegions() throws IOException {
		JSONObject result = new JSONObject();
		// 读取地域分类信息
		CsvReader reader = WmcloudUtil.url2csv(Const.SECTYPEREGION);
		reader.readRecord();
		while (reader.readRecord()) {
			String[] items = reader.getValues();
			String code = items[0];
			hash.hset(Const.RKEY_STOCK_SECTYPEREGION_, code, JSON.toJSONString(items));
		}
		reader.close();
	}

	public static void main(String[] args) {
		try {
//			 sim.getEqu();
			// sim.getSecTips();
			// sim.getFdmtislately();
			sim.getSecTips();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
