package com.shendeng.datamanager;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.csvreader.CsvReader;
import com.shendeng.utils.Const;
import com.shendeng.utils.DateTool;
import com.shendeng.utils.JedisUtil.Hash;
import com.shendeng.utils.JedisUtil.SortSet;
import com.shendeng.utils.RedisUtil;
import com.shendeng.utils.StockUtils;
import com.shendeng.utils.StrTool;
import com.shendeng.utils.SystemConfig;
import com.shendeng.utils.WmcloudUtil;

/**
 * 行业数据逻辑处理
 * 
 * @author naxj
 * 
 */
public class IndustryManager {

	private static IndustryManager im = new IndustryManager();

	public static IndustryManager getInstance() {
		return im;
	}

	private SortSet sortSet = RedisUtil.getJedisUtil().SORTSET;

	private Hash hash = RedisUtil.getJedisUtil().HASH;

	/**
	 * 初始化股票与行业关系
	 * 
	 */
	public void initIndustry() throws IOException {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			// 读取股票行业信息数据流
			CsvReader reader = WmcloudUtil.url2csv(Const.EQUINDUSTRY + sdf.format(new Date()));
			// 读取返回的cvs数据流
			if(reader != null){
				reader.readRecord();
				while (reader.readRecord()) {
					String[] items = reader.getValues();
					String code = items[1];
					// 交易市场代码
					String exchangeCD = items[2];
					// 一级行业编码
					String industryID1 = items[13];
					// 一级行业名称
					String industryName1 = items[14];
					String simpleCode = StockUtils.getLTCode(exchangeCD) + code;
					// 查询股票信息
					JSONArray stock = JSON.parseArray(hash.hget(Const.RKEY_STOCK_INFO_, simpleCode));

					if (stock != null && stock.size() >= 0) {
						// 存到股票基本信息里面面
						stock.set(Const.INFO_industryID1, industryID1);
						stock.set(Const.INFO_industryName1, industryName1);
						hash.hset(Const.RKEY_STOCK_INFO_, simpleCode, JSON.toJSONString(stock));

						// 存到股票数据快照里面
						JSONArray price = JSON.parseArray(hash.hget(Const.RKEY_LEVEL1_, simpleCode));
						if (price != null) {
							price.set(Const.PRICE_industryID1, industryID1);
							price.set(Const.PRICE_industryName1, industryName1);
							hash.hset(Const.RKEY_LEVEL1_, simpleCode, JSON.toJSONString(price));
						}
					}
				}
				reader.close();
				// 处理缓存中的股票与行业关系数据
				processStockAndIndustryID();
			}
		} catch (Exception ex) {
			new Exception("初始化股票与行业关系"+DateTool.DateToStr(new Date(), DateTool.TIME_FORMAT)).printStackTrace();
			ex.printStackTrace();
		}
	}

	/**
	 * 处理缓存中的股票与行业关系数据
	 */
	public void processStockAndIndustryID() {
		try{
		Map<String, String> hybmMap = new HashMap<String, String>();
		Map<String, Set<String>> hyStockCode = new HashMap<String, Set<String>>();
		Map<String, String> stockInfoMap = hash.hgetAll(Const.RKEY_STOCK_INFO_);
		for (Object key : stockInfoMap.keySet()) {
			String simpleCode = StrTool.toString(key);
			JSONArray stockList = JSON.parseArray(stockInfoMap.get(key));
			String hybm = StrTool.toString(stockList.get(Const.INFO_industryID1));
			if (hybm != null && hybm.length() > 0) {
				hybmMap.put(hybm, hybm);
				Set<String> temp = hyStockCode.get(hybm);
				if (temp == null) {
					temp = hyStockCode.put(hybm, new HashSet<String>());
				}
				hyStockCode.get(hybm).add(simpleCode);
			}
		}
		hash.hdel(Const.RKEY_INDUSTRY_ + "info");
		for (String key : hybmMap.keySet()) {
			hash.hset(Const.RKEY_INDUSTRY_ + "info", key, JSON.toJSONString(hyStockCode.get(key)));
		}
		}catch(Exception e){
			new Exception("处理缓存中的股票与行业关系数据" + DateTool.DateToStr(new Date(), DateTool.TIME_FORMAT)).printStackTrace();
			e.printStackTrace();
		}
	}

	/**
	 * 证券板块数据
	 * 
	 * @throws IOException
	 * 
	 */
	public void initSectypeRel() {
		try {
			String allRelTypeid = SystemConfig.getSysVal("rel.typeid");
			String[] relAry = allRelTypeid.split(",");
			hash.hdel(Const.RKEY_INDUSTRY_ + "sectyperel");
			hash.hdel(Const.RKEY_INDUSTRY_ + "stock");
			for (String typeId : relAry) {
				CsvReader reader = WmcloudUtil.url2csv(Const.SECTYPEREL + "?typeID=" + typeId);
				String industry = null;
				if (reader != null) {
					reader.readRecord();
					try {
						while (reader.readRecord()) {
							String[] items = reader.getValues();
							String tlcode = items[2];
							String code = items[3];
							String exchangeCD = items[4];
							if (tlcode != null && (exchangeCD.equals("XSHE") || exchangeCD.equals("XSHG")) && code != null && code.length() == 6) {
								String simpleCode = StockUtils.getStockCode(tlcode);
								if(industry != null){
									industry = industry+","+simpleCode;
								}else{
									industry = simpleCode;
								}
								hash.hset(Const.RKEY_INDUSTRY_ + "sectyperel", simpleCode, JSON.toJSONString(items));
							}
						}
						hash.hset(Const.RKEY_INDUSTRY_ + "stock",typeId,industry);
						reader.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		} catch (Exception e) {
			new Exception("证券板块数据" + DateTool.DateToStr(new Date(), DateTool.TIME_FORMAT)).printStackTrace();
			e.printStackTrace();
		}
	}

	/**
	 * 处理行业排名
	 */
	public void processIndustrySort() {
		try {
			long a = System.currentTimeMillis();
			IdentityHashMap<Double, String> tradeRateMap = new IdentityHashMap<Double, String>();
			IdentityHashMap<Double, String> sumOrderMap = new IdentityHashMap<Double, String>();

			Map<String, String> industryMap = hash.hgetAll(Const.RKEY_INDUSTRY_ + "info");
			// 行业基本信息不为空才继续
			if (hash.hlen(Const.RKEY_INDUSTRY_ + "info") <= 0) {
				return;
			}
			// 行情数据不为空才继续
			if (hash.hlen(Const.RKEY_LEVEL1_) <= 0) {
				return;
			}
			for (Object key : industryMap.keySet()) {
				JSONArray stockCodeAry = JSON.parseArray(industryMap.get(key));

				double avgChangePct = 0;
				double sumChangePct = 0;
				double avgLastPrice = 0;
				String hymc = "";
				int i = 1;
				double sumOrder = 0;// 净资产流入
				double sumValue = 0;// 总成交额
				long sumVolume = 0;
				// 领涨股、领跌股
				String maxCode = "", minCode = "";
				Double maxCodePrice = 0.0, minCodePrice = 0.0;
				for (Object obj : stockCodeAry) {
					String code = StrTool.toString(obj);
					long b = System.currentTimeMillis();
					JSONArray priceAry = JSON.parseArray(hash.hget(Const.RKEY_LEVEL1_, code));
					JSONArray stockInfoAry = JSON.parseArray(hash.hget(Const.RKEY_STOCK_INFO_, code));
					
					if(stockInfoAry == null || "1".equals(stockInfoAry.getString(Const.INFO_isSuspend))){
						continue;
					}else if(priceAry == null || priceAry.getString(Const.PRICE_changePct) == null ){
						continue;
					}
					
					if (maxCodePrice < StrTool.toDouble(priceAry.getString(Const.PRICE_changePct))) {
						maxCodePrice = StrTool.toDouble(priceAry.getString(Const.PRICE_changePct));
						maxCode = code;
					}
				
					if (minCodePrice > StrTool.toDouble(priceAry.getString(Const.PRICE_changePct))) {
						minCodePrice = StrTool.toDouble(priceAry.getString(Const.PRICE_changePct));
						minCode = code;
					} 

					String changetPct = priceAry.getString(Const.PRICE_changePct);
					String lastPrice = priceAry.getString(Const.PRICE_lastPrice);
					// 获取行业名称
					hymc = stockInfoAry.getString(Const.INFO_industryName1);
					changetPct = StringUtils.isBlank(changetPct) ? "0" : changetPct;
					// 最新价格叠加
					avgLastPrice += Double.parseDouble(lastPrice);
					// 变动率叠加
					sumChangePct += Double.parseDouble(changetPct);
					// 资产净流入 = 本轮成交总金额
					String totalOrderValue = priceAry.getString(Const.PRICE_totalOrderValue);
					if(totalOrderValue.isEmpty()){
						sumOrder += 0;
					}else{
						sumOrder += Double.parseDouble(totalOrderValue);
					}
					sumValue += Double.parseDouble(priceAry.getString(Const.PRICE_value));
					sumVolume += Long.parseLong(priceAry.getString(Const.PRICE_volume));
					i++;
				}

				if ("".equals(maxCode)) {
					continue;
				}
				
				if ("".equals(minCode)) {
					continue;
				}
				// 领涨股领跌股价格
				long c = System.currentTimeMillis();
				JSONArray maxPriceAry = JSON.parseArray(hash.hget(Const.RKEY_LEVEL1_, maxCode));
				JSONArray minPriceAry = JSON.parseArray(hash.hget(Const.RKEY_LEVEL1_, minCode));
				// 计算平均值
				avgChangePct = sumChangePct / i;
				avgLastPrice = avgLastPrice / i;
				if (StringUtils.isBlank(hymc)) {
					return;
				}
				Map<String, String> trade = new HashMap<String, String>();
				trade.put("trade", hymc);
				trade.put("stockSize", i + "");
				String tradeRate = String.format("%.2f", new BigDecimal(avgChangePct).movePointRight(2).doubleValue());
				if(StrTool.toDouble(tradeRate) == 0){
					tradeRate = "0.00";
				}
				trade.put("tradeRate", tradeRate + "%");
				trade.put("tradeValue", StrTool.keep2Point(StrTool.toString(avgLastPrice)));
				trade.put("sumOrder", StrTool.keep2Point(StrTool.toString(sumOrder)));// 资产净流入
				trade.put("sumValue", StrTool.keep2Point(StrTool.toString(sumValue)));// 总成交额
				trade.put("sumVolume", StrTool.keep2Point(StrTool.toString(sumVolume)));// 总成交额
				trade.put("tradeTypeId", StrTool.toString(key));
				trade.put("name", maxPriceAry.getString(Const.PRICE_shortNM) == null ? "" : maxPriceAry.getString(Const.PRICE_shortNM));
				trade.put("code", maxCode);
				trade.put("changeRate", String.format("%.2f", maxPriceAry.getString(Const.PRICE_changePct) == null ? 0 : new BigDecimal(maxPriceAry.getString(Const.PRICE_changePct)).movePointRight(2).doubleValue()) + "%");
				trade.put("change", StrTool.keep2Point(maxPriceAry.getString(Const.PRICE_change) == null ? "" : maxPriceAry.getString(Const.PRICE_change)));
				trade.put("lastPrice", StrTool.keep2Point(maxPriceAry.getString(Const.PRICE_lastPrice) == null ? "" : maxPriceAry.getString(Const.PRICE_lastPrice)));
				trade.put("mincode", minCode);
				trade.put("minname", minPriceAry.getString(Const.PRICE_shortNM) == null ? "" : minPriceAry.getString(Const.PRICE_shortNM));
				trade.put("minchangeRate", String.format("%.2f", minPriceAry.getString(Const.PRICE_changePct) == null ? 0 : new BigDecimal(minPriceAry.getString(Const.PRICE_changePct)).movePointRight(2).doubleValue()) + "%");
				trade.put("minchange", StrTool.keep2Point(minPriceAry.getString(Const.PRICE_change) == null ? "" : minPriceAry.getString(Const.PRICE_change)));
				trade.put("minlastPrice", StrTool.keep2Point(minPriceAry.getString(Const.PRICE_lastPrice) == null ? "" : minPriceAry.getString(Const.PRICE_lastPrice)));

				// 行业平均价
				tradeRateMap.put(avgChangePct, JSON.toJSONString(trade));
				// 资产净流入排行
				sumOrderMap.put(sumOrder, JSON.toJSONString(trade));
			}

			if (tradeRateMap != null && !tradeRateMap.isEmpty()) {
				sortSet.zrem(Const.RKEY_INDUSTRY_SORT_tradeRate);
				sortSet.zadd(Const.RKEY_INDUSTRY_SORT_tradeRate, tradeRateMap);
			}

			if (sumOrderMap != null && !sumOrderMap.isEmpty()) {
				sortSet.zrem(Const.RKEY_INDUSTRY_SORT_sumOrder);
				sortSet.zadd(Const.RKEY_INDUSTRY_SORT_sumOrder, sumOrderMap);
			}
		} catch (Exception e) {
			new Exception("处理行业排名" + DateTool.DateToStr(new Date(), DateTool.TIME_FORMAT)).printStackTrace();
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		try {
			im.processIndustrySort();
			// im.processStockAndIndustryID1();
//			 im.initSectypeRel();
			// im.tradeUpDownList("up");
			// im.processStockAndIndustryID();
			// RedisUtil.getJedisUtil().KEYS.delPattern(Const.RKEY_K_D_+"*");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
