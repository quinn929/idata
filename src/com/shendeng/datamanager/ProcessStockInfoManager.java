package com.shendeng.datamanager;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSON;
import com.csvreader.CsvReader;
import com.shendeng.utils.Const;
import com.shendeng.utils.JedisUtil.Hash;
import com.shendeng.utils.RedisUtil;
import com.shendeng.utils.StockUtils;
import com.shendeng.utils.StrTool;
import com.shendeng.utils.SystemConfig;
import com.shendeng.utils.WmcloudUtil;

/**
 * 查询股票基本信息数据
 *
 * @author qy
 * @date 2016年6月12日
 */
public class ProcessStockInfoManager {

	public static ProcessStockInfoManager psim = new ProcessStockInfoManager();
	
	public static ProcessStockInfoManager getInstance() {
		return psim;
	}
	
	private Hash hash = RedisUtil.getJedisUtil().HASH;
	
	/**
	 * 是否指数
	 * 
	 * @param assetClass
	 *            证券类型
	 * @return
	 */
	public static boolean isStockIdx(String assetClass) {
		if (assetClass != null && "IDX".equals(assetClass)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 股票详情接口(门户)
	 * 
	 * @param simpleCode
	 *            股票代码
	 * @return
	 */
	public static JSONObject stockInfo(String simpleCode) {
		JSONObject stockMap = new JSONObject();
		Map<String,String> level1 = RedisUtil.getJedisUtil().HASH.hgetAll(Const.RKEY_LEVEL1_);
		com.alibaba.fastjson.JSONArray stockFullInfo = JSON.parseArray(level1.get(simpleCode));
		Map<String, String> itemMap = new HashMap<String, String>();
		if(stockFullInfo != null && stockFullInfo.size() > 0){
			itemMap.put("lastDate", stockFullInfo.getString(Const.PRICE_dataDate));
			itemMap.put("lastTime", stockFullInfo.getString(Const.PRICE_dataTime));
			itemMap.put("lowPrice", stockFullInfo.getString(Const.PRICE_lowPrice));
			itemMap.put("price", stockFullInfo.getString(Const.PRICE_lastPrice));
			itemMap.put("name", stockFullInfo.getString(Const.PRICE_shortNM));
			itemMap.put("code", simpleCode);
			itemMap.put("type", "0");
			itemMap.put("openPrice", stockFullInfo.getString(Const.PRICE_openPrice));
			itemMap.put("closePrice", stockFullInfo.getString(Const.PRICE_prevClosePrice));
			itemMap.put("highPrice", stockFullInfo.getString(Const.PRICE_highPrice));
			itemMap.put("volumn", stockFullInfo.getString(Const.PRICE_volume));
			itemMap.put("volumnPrice", stockFullInfo.getString(Const.PRICE_value));
			itemMap.put("swing", new DecimalFormat("####.##").format(Double.parseDouble(stockFullInfo.getString(Const.PRICE_changePct))));
		}
		stockMap.put("error", 0);
		stockMap.put("msg", "");
		stockMap.put("success", true);
		stockMap.put("data", itemMap);
		return stockMap;
	}

	/**
	 * 首页指数
	 * 
	 * @param request
	 * @return
	 */
	public JSONObject stockindexlist() {
		JSONObject result = new JSONObject();
		String allIdx = SystemConfig.getSysVal("marketIdx.code");
		String[] idxAry = allIdx.split(",");
		List<Map<String, String>> datalist = new ArrayList<Map<String, String>>();
		for (String idxCode : idxAry) {
			String simpleCode = StockUtils.getStockCode(idxCode);
			com.alibaba.fastjson.JSONArray idxPrice = JSON.parseArray(hash.hget(Const.RKEY_LEVEL1_,simpleCode));
			Map<String, String> idxMap = new HashMap<String, String>();
			if(idxPrice != null){
				idxMap.put("price", idxPrice.getString(Const.PRICE_lastPrice));
				idxMap.put("code", simpleCode);
				idxMap.put("changeRate", String.format("%.2f",new BigDecimal(idxPrice.getString(Const.PRICE_changePct)).movePointRight(2).doubleValue())+"%");
				idxMap.put("change", String.format("%.2f",idxPrice.getDoubleValue(Const.PRICE_change)));
				idxMap.put("type", "1");
				idxMap.put("name", StockUtils.idxCode2Name(simpleCode));
			}
			datalist.add(idxMap);
		}
		
		result.put("data", datalist);
		result.put("error", 0);
		result.put("msg", "");
		result.put("success", true);
		return result;
	}
	
	/**
	 * 股票数据库
	 *
	 * @return
	 */
	public String fmStocks() {
		StringBuffer sb = new StringBuffer();
		//所有用到的指数代码
		String idxCodes = "";
		Map<String, String> allStock = hash.hgetAll(Const.RKEY_STOCK_INFO_);
		
		Map<String, String> allLevel1Stock = hash.hgetAll(Const.RKEY_LEVEL1_);
		
		Map<String, String> level1Stock = new HashMap<String, String>();
		
		for (Object code : allLevel1Stock.keySet()) {
			com.alibaba.fastjson.JSONArray level1InfoAry = JSON.parseArray(allLevel1Stock.get(StrTool.toString(code)));
			level1Stock.put(StrTool.toString(code), level1InfoAry.getString(Const.PRICE_shortNM));
		}
		
		for (Object code : allStock.keySet()) {
			level1Stock.remove(StrTool.toString(code));
			//是否指数
			int is_idx = 0;
			if(idxCodes.equals("")){
				String allIdx = SystemConfig.getSysVal("idx.code");
				String[] idxAry = allIdx.split(",");
				for (String idxCode : idxAry) {
					String simpleCode = StockUtils.getStockCode(idxCode);
					level1Stock.remove(simpleCode);
					idxCodes += simpleCode;
					sb.append(StockUtils.idxCode2Name(simpleCode).trim()).append(" ").append(simpleCode.trim()).append(" ").append(1).append(" ").append(0).append("\n");
					
				}
			}
			if(idxCodes.contains(StrTool.toString(code))){
				is_idx = 1;
			}
			com.alibaba.fastjson.JSONArray stock = JSON.parseArray(allStock.get(StrTool.toString(code)));
			//股票名称、股票代码、是否指数(1是,0否)、是否停牌
			sb.append(stock.getString(Const.INFO_secShortName).trim()).append(" ").append(StrTool.toString(code).trim()).append(" ").append(is_idx).append(" ").append(stock.get(Const.INFO_isSuspend)).append("\n");
		}
		for (Object code : level1Stock.keySet()) {
			sb.append(level1Stock.get(code).trim()).append(" ").append(StrTool.toString(code).trim()).append(" ").append(0).append(" ").append(0).append("\n");
		}
		return sb.toString();
	}
	
	/**
	 * 股票数据库版本
	 * 
	 * @return
	 */
	public JSONObject search() {
		JSONObject result = new JSONObject();
		result.put("error", 0);
		result.put("msg", "");
		result.put("success", 1);
		String version = RedisUtil.getJedisUtil().STRINGS.get("stockVersion");
		result.put("data", "1.1." + version);
		return result;
	}
	
	/**
	 * 获取通联地域分类
	 * 
	 * @param leave
	 *            地区级别
	 * @return
	 */
	public JSONObject stockRegions(String leave) {
		JSONObject result = new JSONObject();
		try {
			// 默认为5,5是市级别
			leave = leave == null ? "5" : leave;
			List data = new ArrayList();
			for (Object key : hash.hgetAll(Const.RKEY_STOCK_SECTYPEREGION_).keySet()) {
				com.alibaba.fastjson.JSONArray ary = JSON.parseArray(hash.hget(Const.RKEY_STOCK_SECTYPEREGION_,StrTool.toString(key)));
				if (ary.get(3).equals(leave)) {
					data.add(ary);
				}
			}
			result.put("error", 0);
			result.put("msg", "");
			result.put("success", true);
			result.put("data", data);
		} catch (Exception ex) {
			ex.printStackTrace();
			result.put("error", 1);
			result.put("msg", ex.getMessage());
			result.put("success", false);
		}
		return result;
	}
	
	/**
	 * 根据编码获取合并利润
	 * 
	 * @param code股票代码
	 * @return
	 */
	public JSONObject getFdmtISLately(String code) {
		com.alibaba.fastjson.JSONArray fdmtislately = JSON.parseArray(hash.hget(Const.PKEY_FDMTISLATELY,code));
		JSONObject result = new JSONObject();
		try {
			JSONObject jo = new JSONObject();
			if(fdmtislately != null){
			jo.put("secID", fdmtislately.get(Const.FdmtISLately_secID));
			jo.put("endDate", fdmtislately.get(Const.FdmtISLately_endDate));
			jo.put("publishDate", fdmtislately.get(Const.FdmtISLately_publishDate));
			jo.put("endDateRep", fdmtislately.get(Const.FdmtISLately_endDateRep));
			jo.put("partyID", fdmtislately.get(Const.FdmtISLately_partyID));
			jo.put("ticker", fdmtislately.get(Const.FdmtISLately_ticker));
			jo.put("secShortName", fdmtislately.get(Const.FdmtISLately_secShortName));
			jo.put("exchangeCD", fdmtislately.get(Const.FdmtISLately_exchangeCD));
			jo.put("actPubtime", fdmtislately.get(Const.FdmtISLately_actPubtime));
			jo.put("mergedFlag", fdmtislately.get(Const.FdmtISLately_mergedFlag));
			jo.put("reportType", fdmtislately.get(Const.FdmtISLately_reportType));
			jo.put("fiscalPeriod", fdmtislately.get(Const.FdmtISLately_fiscalPeriod));
			jo.put("accoutingStandards", fdmtislately.get(Const.FdmtISLately_accoutingStandards));
			jo.put("currencyCD", fdmtislately.get(Const.FdmtISLately_currencyCD));
			jo.put("tRevenue", fdmtislately.get(Const.FdmtISLately_tRevenue));
			jo.put("revenue", fdmtislately.get(Const.FdmtISLately_revenue));
			jo.put("intIncome", fdmtislately.get(Const.FdmtISLately_intIncome));
			jo.put("intExp", fdmtislately.get(Const.FdmtISLately_intExp));
			jo.put("premEarned", fdmtislately.get(Const.FdmtISLately_premEarned));
			jo.put("commisIncome", fdmtislately.get(Const.FdmtISLately_commisIncome));
			jo.put("commisExp", fdmtislately.get(Const.FdmtISLately_commisExp));
			jo.put("TCogs", fdmtislately.get(Const.FdmtISLately_TCogs));
			jo.put("COGS", fdmtislately.get(Const.FdmtISLately_COGS));
			jo.put("premRefund", fdmtislately.get(Const.FdmtISLately_premRefund));
			jo.put("NCompensPayout", fdmtislately.get(Const.FdmtISLately_NCompensPayout));
			jo.put("reserInsurContr", fdmtislately.get(Const.FdmtISLately_reserInsurContr));
			jo.put("policyDivPayt", fdmtislately.get(Const.FdmtISLately_policyDivPayt));
			jo.put("reinsurExp", fdmtislately.get(Const.FdmtISLately_reinsurExp));
			jo.put("bizTaxSurchg", fdmtislately.get(Const.FdmtISLately_bizTaxSurchg));
			jo.put("sellExp	double", fdmtislately.get(Const.FdmtISLately_sellExp));
			jo.put("adminExp", fdmtislately.get(Const.FdmtISLately_adminExp));
			jo.put("finanExp", fdmtislately.get(Const.FdmtISLately_finanExp));
			jo.put("assetsImpairLoss", fdmtislately.get(Const.FdmtISLately_assetsImpairLoss));
			jo.put("fValueChgGain", fdmtislately.get(Const.FdmtISLately_fValueChgGain));
			jo.put("investIncome", fdmtislately.get(Const.FdmtISLately_investIncome));
			jo.put("AJInvestIncome", fdmtislately.get(Const.FdmtISLately_AJInvestIncome));
			jo.put("forexGain", fdmtislately.get(Const.FdmtISLately_forexGain));
			jo.put("operateProfit", fdmtislately.get(Const.FdmtISLately_operateProfit));
			jo.put("NoperateIncome", fdmtislately.get(Const.FdmtISLately_NoperateIncome));
			jo.put("NoperateExp", fdmtislately.get(Const.FdmtISLately_NoperateExp));
			jo.put("NCADisploss", fdmtislately.get(Const.FdmtISLately_NCADisploss));
			jo.put("TProfit", fdmtislately.get(Const.FdmtISLately_TProfit));
			jo.put("incomeTax", fdmtislately.get(Const.FdmtISLately_incomeTax));
			jo.put("NIncome", fdmtislately.get(Const.FdmtISLately_NIncome));
			jo.put("NIncomeAttrP", fdmtislately.get(Const.FdmtISLately_NIncomeAttrP));
			jo.put("minorityGain", fdmtislately.get(Const.FdmtISLately_minorityGain));
			jo.put("basicEPS", fdmtislately.get(Const.FdmtISLately_basicEPS));
			jo.put("dilutedEPS", fdmtislately.get(Const.FdmtISLately_dilutedEPS));
			jo.put("othComprIncome", fdmtislately.get(Const.FdmtISLately_othComprIncome));
			jo.put("TComprIncome", fdmtislately.get(Const.FdmtISLately_TComprIncome));
			jo.put("comprIncAttrP", fdmtislately.get(Const.FdmtISLately_comprIncAttrP));
			jo.put("comprIncAttrMS", fdmtislately.get(Const.FdmtISLately_comprIncAttrMS));
			}
			result.put("data", jo.toString());
			result.put("error", 0);
			result.put("msg", "");
			result.put("success", true);
		} catch (Exception ex) {
			ex.printStackTrace();
			result.put("error", 1);
			result.put("msg", ex.getMessage());
			result.put("success", false);
		}
		return result;
	}

	/**
	 * 键盘精灵
	 *
	 * @param ticker 股票代码
	 * @param pagesize 页数
	 * @param pagenum 页码
	 * @return
	 */
	public JSONObject getEquinfo(String ticker,String pagesize,String pagenum) {
		JSONObject result = new JSONObject();
		try {
			ticker = ticker == null ? "" : ticker;
			pagesize = pagesize == null ? "10" : pagesize;
			pagenum = pagenum == null ? "1" : pagenum;
			// 读取键盘精灵 接口
			String url = Const.EQUINFO + "?ticker=" + ticker + "&pagesize=" + pagesize + "&pagenum=" + pagenum;
			CsvReader reader = WmcloudUtil.url2csv(url);
			reader.readRecord();
			JSONArray data = new JSONArray();
			while (reader.readRecord()) {
				String[] items = reader.getValues();
				JSONObject jo = new JSONObject();
				jo.put("ticker", items[0]);
				jo.put("exchangeCD", StockUtils.getLTCode(items[1]));
				jo.put("shortNM", items[2]);
				data.add(jo);
			}
			reader.close();
			result.put("error", 0);
			result.put("msg", "");
			result.put("success", true);
			result.put("data", data);
		} catch (Exception ex) {
			ex.printStackTrace();
			result.put("error", 1);
			result.put("msg", ex.getMessage());
			result.put("success", false);
		}

		return result;
	}
	
	
	/**
	 * 根据板块获取相关股票(门户)
	 *
	 * @param request
	 * @return
	 */
	public JSONObject getStocks(String typeid,String start_s,String count_s,String typeCode) {
		JSONObject result = new JSONObject();
		int count = count_s == null ? 10 : Integer.parseInt(count_s);
		int start = start_s == null ? 0 : Integer.parseInt(start_s);
		if (start!=0){
			start = (start-1)*count;
		}
		try {
			//获取所有该板块下的股票
			LinkedHashMap<String, com.alibaba.fastjson.JSONArray> stocks = new LinkedHashMap<String, com.alibaba.fastjson.JSONArray>();
			
			String codes = hash.hget(Const.RKEY_INDUSTRY_ + "stock", typeid);
			String[] codeAry = codes.split(",");
			com.alibaba.fastjson.JSONArray items = null;
			
			for (String code : codeAry) {
				items =  JSON.parseArray(hash.hget(Const.RKEY_LEVEL1_,code));
				if (items != null) {
					stocks.put(code, items);
				}
			}
			
			
//			String code = null;
//			com.alibaba.fastjson.JSONArray items = null;
//			for (Object key : hash.hgetAll(Const.RKEY_INDUSTRY_ + "sectyperel").keySet()) {
//				com.alibaba.fastjson.JSONArray ary = JSON.parseArray(hash.hget(Const.RKEY_INDUSTRY_ + "sectyperel",StrTool.toString(key)));
//				if (typeid != null && ary != null && ary.getString(0).startsWith(typeid)) {
//					code = StockUtils.getLTCode(ary.getString(4)) + ary.getString(3);
//					if (!stocks.containsKey(code)) {
//						items =  JSON.parseArray(hash.hget(Const.RKEY_LEVEL1_,code));
//						if (items != null) {
//							stocks.put(code, items);
//						}
//					}
//				}
//			}
			
			if (!StringUtils.isEmpty(typeCode)) {
				List<String> dataList = null;
				List<Map<String, String>> resultDataList = new ArrayList<Map<String, String>>();
				if (typeCode.equals("up")) {
					// name = "涨幅榜";
					// 默认排序
					dataList = StockUtils.stockPriceSortByField(stocks, Const.PRICE_changePct, Const.SORT_DESC);
				} else if (typeCode.equals("down")) {
					// name = "跌幅榜";
					dataList = StockUtils.stockPriceSortByField(stocks, Const.PRICE_changePct, Const.SORT_ASC);
				} else if (typeCode.equals("swingup")) {
					// name = "振幅榜";
					dataList = StockUtils.stockPriceSortByField(stocks, Const.PRICE_amplitude, Const.SORT_DESC);
				} else if (typeCode.equals("volumnup")) {
					// name = "成交量";
					dataList = StockUtils.stockPriceSortByField(stocks, Const.PRICE_volume, Const.SORT_DESC);
				} else if (typeCode.equals("volumnpriceup")) {
					// name = "成交额";
					dataList = StockUtils.stockPriceSortByField(stocks, Const.PRICE_value, Const.SORT_DESC);
				} else if (typeCode.equals("turnoverrateup")) {
					// name = "换手率";
					dataList = StockUtils.stockPriceSortByField(stocks, Const.PRICE_turnoverRate, Const.SORT_DESC);
				} else if (typeCode.equals("orderup")) {
					// name = "资产流入涨";
					dataList = StockUtils.stockPriceSortByField(stocks, Const.PRICE_totalOrderValue, Const.SORT_DESC);
				} else if (typeCode.equals("orderdown")) {
					// name = "资产流入跌";
					dataList = StockUtils.stockPriceSortByField(stocks, Const.PRICE_totalOrderValue, Const.SORT_ASC);
				}

				for (int i = start; i < (start + count); i++) {
					if (i >= dataList.size()) {
						break;
					}
					// 行情数据
					com.alibaba.fastjson.JSONArray wmcloudArray = stocks.get(dataList.get(i));
					
					// 行情数组转map
					Map<String, String> item = StockUtils.stockPriceArray2Map(wmcloudArray, JSON.parseArray(hash.hget(Const.RKEY_STOCK_INFO_,dataList.get(i))));
					if (typeCode.equals("swingup")) {
						// 振幅
						item.put("changeRate", item.get("swing"));
					}
					resultDataList.add(item);
				}
				
				result.put("data",resultDataList);
				result.put("total", dataList.size());
			}
			
			result.put("error", 0);
			result.put("msg", "");
			result.put("success", true);
		} catch (Exception ex) {
			ex.printStackTrace();
			result.put("error", 1);
			result.put("msg", ex.getMessage());
			result.put("success", false);
		}
		return result;
	}
	
	public static void main(String[] args){
		psim.stockRegions("6");
	}
	
}
