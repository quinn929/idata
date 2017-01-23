package com.shendeng.datamanager;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
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
import com.shendeng.utils.DateTool;
import com.shendeng.utils.RedisUtil;
import com.shendeng.utils.StockUtils;
import com.shendeng.utils.StrTool;
import com.shendeng.utils.WmcloudUtil;

/**
 * 查询股票快照数据
 * 
 * @author qy
 * @date 2016年5月15日
 */
public class ProcessLevel1Manager {
	private static ProcessLevel1Manager plm = new ProcessLevel1Manager();

	public static ProcessLevel1Manager getInstance() {
		return plm;
	}

	private Hash hash = RedisUtil.getJedisUtil().HASH;

	/**
	 * 获取股票信息
	 * 
	 * @param simpleCode
	 *            股票代码
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public JSONObject getStockInfoApp(String codes) {
		JSONObject stockMap = new JSONObject();
		stockMap.put("error", 0);
		stockMap.put("msg", "");
		stockMap.put("success", true);
		stockMap.put("data", stockInfoApp(codes));
		return stockMap;
	}

	/**
	 * 个股五档数据
	 * 
	 * @param codes
	 *            股票代码
	 * @return
	 */
	public JSONObject stockNewData(String codes) {
		if (codes != null && !"".equals(codes)) {
			String[] codeinArray = codes.split(",");
			ArrayList<Map<String, String>> dataList = new ArrayList<Map<String, String>>();
			for (String tempCode : codeinArray) {
				Map<String, String> itemMap = new HashMap<String, String>();
				// String shsz = tempCode.substring(0, 2);
				// String code = tempCode.substring(2);

				com.alibaba.fastjson.JSONArray priceAry = JSON.parseArray(hash.hget(Const.RKEY_LEVEL1_, tempCode));
				// 数据处理
				if (priceAry != null) {
					itemMap.put("00", "00");
					// 类型转换
					itemMap.put("type", priceAry.getString(Const.PRICE_assetClass).equals("E") ? "0" : "1");
					itemMap.put("signal", "-1");
					itemMap.put("lastDate", priceAry.getString(Const.PRICE_dataDate));
					itemMap.put("lastTime", priceAry.getString(Const.PRICE_dataTime));
					itemMap.put("lowPrice", priceAry.getString(Const.PRICE_lowPrice));
					itemMap.put("price", priceAry.getString(Const.PRICE_lastPrice));
					itemMap.put("name", priceAry.getString(Const.PRICE_shortNM));
					itemMap.put("code", tempCode);
					itemMap.put("openPrice", priceAry.getString(Const.PRICE_openPrice));
					itemMap.put("closePrice", priceAry.getString(Const.PRICE_prevClosePrice));
					itemMap.put("price", priceAry.getString(Const.PRICE_lastPrice));
					itemMap.put("highPrice", priceAry.getString(Const.PRICE_highPrice));
					itemMap.put("volumn", priceAry.getString(Const.PRICE_volume));
					itemMap.put("volumnPrice", priceAry.getString(Const.PRICE_value));
					// itemMap.put("changeRate",StrTool.keep2Point(StrTool.toString(priceAry.getDouble(Const.PRICE_changePct)*100.0)));
					Double changePct = new BigDecimal(StrTool.toString((priceAry.get(Const.PRICE_changePct)))).movePointRight(2).doubleValue();
					itemMap.put("changeRate", String.format("%.2f", changePct));
					itemMap.put("buy_1", priceAry.getString(Const.PRICE_bidBook_price1));
					itemMap.put("buy_2", priceAry.getString(Const.PRICE_bidBook_price2));
					itemMap.put("buy_3", priceAry.getString(Const.PRICE_bidBook_price3));
					itemMap.put("buy_4", priceAry.getString(Const.PRICE_bidBook_price4));
					itemMap.put("buy_5", priceAry.getString(Const.PRICE_bidBook_price5));
					itemMap.put("buy_1_s", priceAry.getString(Const.PRICE_bidBook_volume1));
					itemMap.put("buy_2_s", priceAry.getString(Const.PRICE_bidBook_volume2));
					itemMap.put("buy_3_s", priceAry.getString(Const.PRICE_bidBook_volume3));
					itemMap.put("buy_4_s", priceAry.getString(Const.PRICE_bidBook_volume4));
					itemMap.put("buy_5_s", priceAry.getString(Const.PRICE_bidBook_volume5));
					itemMap.put("sell_1", priceAry.getString(Const.PRICE_askBook_price1));
					itemMap.put("sell_2", priceAry.getString(Const.PRICE_askBook_price2));
					itemMap.put("sell_3", priceAry.getString(Const.PRICE_askBook_price3));
					itemMap.put("sell_4", priceAry.getString(Const.PRICE_askBook_price4));
					itemMap.put("sell_5", priceAry.getString(Const.PRICE_askBook_price5));
					itemMap.put("sell_1_s", priceAry.getString(Const.PRICE_askBook_volume1));
					itemMap.put("sell_2_s", priceAry.getString(Const.PRICE_askBook_volume2));
					itemMap.put("sell_3_s", priceAry.getString(Const.PRICE_askBook_volume3));
					itemMap.put("sell_4_s", priceAry.getString(Const.PRICE_askBook_volume4));
					itemMap.put("sell_5_s", priceAry.getString(Const.PRICE_askBook_volume5));
				} else {
					itemMap.put("code", tempCode);
				}
				dataList.add(itemMap);
			}
			JSONObject result = new JSONObject();
			result.put("error", 0);
			result.put("msg", "");
			result.put("success", true);
			result.put("data", dataList);
			return result;
		} else {
			JSONObject result = new JSONObject();
			result.put("error", 1);
			result.put("msg", "参数不能为空");
			result.put("success", false);
			result.put("data", "");
			return result;
		}
	}

	/**
	 * 个股五档数据(门户)
	 * 
	 * @param codes
	 *            股票代码
	 * @return
	 */
	public JSONObject stockNewData2(String codes) {
		String[] codeinArray = codes.split(",");
		ArrayList<Map<String, String>> dataList = new ArrayList<Map<String, String>>();
		for (String tempCode : codeinArray) {
			Map<String, String> itemMap = new HashMap<String, String>();
			String shsz = tempCode.substring(0, 2);
			String code = tempCode.substring(2);

			com.alibaba.fastjson.JSONArray priceAry = JSON.parseArray(hash.hget(Const.RKEY_LEVEL1_, tempCode));
			// 数据处理
			if (priceAry != null) {
				itemMap.put("00", "00");
				// 类型转换
				itemMap.put("type", priceAry.getString(Const.PRICE_assetClass).equals("E") ? "0" : "1");
				itemMap.put("signal", "-1");
				itemMap.put("lastDate", priceAry.getString(Const.PRICE_dataDate));
				itemMap.put("lastTime", priceAry.getString(Const.PRICE_dataTime));
				itemMap.put("lowPrice", priceAry.getString(Const.PRICE_lowPrice));
				itemMap.put("price", priceAry.getString(Const.PRICE_lastPrice));
				itemMap.put("name", priceAry.getString(Const.PRICE_shortNM));
				itemMap.put("code", tempCode);
				itemMap.put("openPrice", priceAry.getString(Const.PRICE_openPrice));
				itemMap.put("closePrice", priceAry.getString(Const.PRICE_prevClosePrice));
				itemMap.put("price", priceAry.getString(Const.PRICE_lastPrice));
				itemMap.put("highPrice", priceAry.getString(Const.PRICE_highPrice));
				itemMap.put("volumn", priceAry.getString(Const.PRICE_volume));
				itemMap.put("volumnPrice", priceAry.getString(Const.PRICE_value));
				itemMap.put("changeRate", priceAry.getString(Const.PRICE_changePct));
				itemMap.put("buy_1", priceAry.getString(Const.PRICE_bidBook_price1));
				itemMap.put("buy_2", priceAry.getString(Const.PRICE_bidBook_price2));
				itemMap.put("buy_3", priceAry.getString(Const.PRICE_bidBook_price3));
				itemMap.put("buy_4", priceAry.getString(Const.PRICE_bidBook_price4));
				itemMap.put("buy_5", priceAry.getString(Const.PRICE_bidBook_price5));
				itemMap.put("buy_1_s", priceAry.getString(Const.PRICE_bidBook_volume1));
				itemMap.put("buy_2_s", priceAry.getString(Const.PRICE_bidBook_volume2));
				itemMap.put("buy_3_s", priceAry.getString(Const.PRICE_bidBook_volume3));
				itemMap.put("buy_4_s", priceAry.getString(Const.PRICE_bidBook_volume4));
				itemMap.put("buy_5_s", priceAry.getString(Const.PRICE_bidBook_volume5));
				itemMap.put("sell_1", priceAry.getString(Const.PRICE_askBook_price1));
				itemMap.put("sell_2", priceAry.getString(Const.PRICE_askBook_price2));
				itemMap.put("sell_3", priceAry.getString(Const.PRICE_askBook_price3));
				itemMap.put("sell_4", priceAry.getString(Const.PRICE_askBook_price4));
				itemMap.put("sell_5", priceAry.getString(Const.PRICE_askBook_price5));
				itemMap.put("sell_1_s", priceAry.getString(Const.PRICE_askBook_volume1));
				itemMap.put("sell_2_s", priceAry.getString(Const.PRICE_askBook_volume2));
				itemMap.put("sell_3_s", priceAry.getString(Const.PRICE_askBook_volume3));
				itemMap.put("sell_4_s", priceAry.getString(Const.PRICE_askBook_volume4));
				itemMap.put("sell_5_s", priceAry.getString(Const.PRICE_askBook_volume5));
			} else {
				itemMap.put("code", tempCode);
			}
			dataList.add(itemMap);
		}
		JSONObject result = new JSONObject();
		result.put("error", 0);
		result.put("msg", "");
		result.put("success", true);
		result.put("data", dataList);
		return result;
	}

	/**
	 * 处理股票信息数据
	 * 
	 * @param simpleCode
	 *            股票代码
	 * @param stockFullInfo
	 *            股票信息
	 * @return 股票信息json
	 */
	public ArrayList<Map<String, Object>> stockInfoApp(String codes) {
		String[] codeinArray = codes.split(",");
		ArrayList<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
		for (String tempCode : codeinArray) {
			com.alibaba.fastjson.JSONArray stockFullInfo = JSON.parseArray(hash.hget(Const.RKEY_LEVEL1_, tempCode));
			if (stockFullInfo != null && stockFullInfo.size() > 0) {
				Map<String, Object> itemMap = new HashMap<String, Object>();
				// DecimalFormat df = new DecimalFormat("0.00");
				String code = tempCode.substring(2);
				String idx = StrTool.toString(stockFullInfo.get(Const.PRICE_assetClass));
				itemMap.put("id", code);
				itemMap.put("code", tempCode);
				itemMap.put("name", stockFullInfo.get(Const.PRICE_shortNM));
				itemMap.put("turnoverRate", new DecimalFormat("0.0000").format(Double.parseDouble(StringUtils.isEmpty(StrTool.toString(stockFullInfo.get(Const.PRICE_turnoverRate))) ? "0" : StrTool.toString(stockFullInfo.get(Const.PRICE_turnoverRate)))));
				// 指数是没有市盈率
				if (!"IDX".equals(idx)) {
					itemMap.put("type", "0");
					// itemMap.put("peRatio",
					// df.format(StockUtils.calculatePEratio(tempCode,
					// Double.parseDouble(StrTool.toString(stockFullInfo.get(Const.PRICE_lastPrice))),
					// Double.parseDouble(StrTool.toString(stockFullInfo.get(Const.PRICE_prevClosePrice))))));
					itemMap.put("peRatio", "0");
				} else {
					itemMap.put("type", "1");
					itemMap.put("peRatio", "0");
				}
				// 流通市值
				itemMap.put("circulationValue", StrTool.keep2Point(StringUtils.isEmpty(StrTool.toString(stockFullInfo.get(Const.PRICE_negMarketValue))) ? "0" : StrTool.toString(stockFullInfo.get(Const.PRICE_negMarketValue))));

				// 取股票信息的总股本
				String totalShares = "0";
				com.alibaba.fastjson.JSONArray stockInfoAry = JSON.parseArray(hash.hget(Const.RKEY_STOCK_INFO_, tempCode));
				try {
					totalShares = StrTool.toString(stockInfoAry.get(Const.INFO_totalShares));
				} catch (Exception e) {

				} finally {
					totalShares = totalShares.equals("") ? "0" : totalShares;
				}
				itemMap.put("totalValue", stockInfoAry == null ? "0" : StrTool.keep2Point(totalShares));

				itemMap.put("cityNetRate", "0");
				itemMap.put("highPrice", stockFullInfo.get(Const.PRICE_highPrice));
				itemMap.put("lowPrice", stockFullInfo.get(Const.PRICE_lowPrice));
				// 上证指数的成交量是股，需要乘以100 换算成手
				if (tempCode.equals("sh000001")) {
					itemMap.put("volumn", new DecimalFormat("0").format(Double.parseDouble(StrTool.toString(stockFullInfo.get(Const.PRICE_volume))) * 10 * 10));
				} else {
					itemMap.put("volumn", new DecimalFormat("0").format(Double.parseDouble(StrTool.toString(stockFullInfo.get(Const.PRICE_volume)))));
				}
				itemMap.put("volumnPrice", StrTool.keep2Point(stockFullInfo.getString(Const.PRICE_value)));
				itemMap.put("closePrice", stockFullInfo.get(Const.PRICE_prevClosePrice));
				itemMap.put("openPrice", stockFullInfo.get(Const.PRICE_openPrice));
				itemMap.put("dateTime", StrTool.toString(stockFullInfo.get(Const.PRICE_dataDate)).replaceAll("-", "") + StrTool.toString(stockFullInfo.get(Const.PRICE_dataTime)).replaceAll(":", ""));
				itemMap.put("price", stockFullInfo.get(Const.PRICE_lastPrice));
				itemMap.put("change", StrTool.keep2Point(stockFullInfo.getString(Const.PRICE_change)));
				Double changePct = new BigDecimal(StrTool.toString((stockFullInfo.get(Const.PRICE_changePct)))).movePointRight(2).doubleValue();
				itemMap.put("changePct", String.format("%.2f", changePct) + "%");
				// 如果是指数 则自己计算振幅 （（今天的最高价—今天的开盘价）的绝对值 / 今天的开盘价*100 +
				// （今天的最低价-今天开盘价））的绝对值 / 今天的开盘价*100 ）
				if (stockFullInfo.getString(Const.PRICE_assetClass).equals("IDX")) {
					double highPrice = Double.parseDouble(StrTool.toString(itemMap.get("highPrice")));
					double openPrice = Double.parseDouble(StrTool.toString(itemMap.get("openPrice")));
					double lowPrice = Double.parseDouble(StrTool.toString(itemMap.get("lowPrice")));
					double closePrice = Double.parseDouble(StrTool.toString(itemMap.get("closePrice")));
					double swing = (highPrice - lowPrice) / closePrice;
					itemMap.put("swing", new BigDecimal(swing).setScale(4, BigDecimal.ROUND_HALF_UP).movePointRight(2));
					// new BigDecimal(new
					// BigDecimal(Double.toString(highPrice)).subtract(new
					// BigDecimal(Double.toString(lowPrice))).doubleValue()).divide(stockFullInfo.getBigDecimal(Const.PRICE_prevClosePrice),
					// 4, BigDecimal.ROUND_HALF_UP).setScale(4,
					// BigDecimal.ROUND_HALF_UP));
					// StrTool.keep2Point(StrTool
					// .toString(StrTool.objToPositive(
					// new BigDecimal(Math.abs(highPrice
					// - lowPrice)
					// / openPrice).movePointRight(2)
					// .doubleValue()
					// + new BigDecimal(Math.abs(lowPrice
					// - openPrice)
					// / openPrice)
					// .movePointRight(2)
					// .doubleValue(), 0))));
				} else {
					itemMap.put("swing", StrTool.keep2Point(StrTool.toString(StrTool.objToPositive(stockFullInfo.getDouble(Const.PRICE_amplitude) == null ? 0 : new BigDecimal(stockFullInfo.getString(Const.PRICE_amplitude)).movePointRight(2).doubleValue(), 0))));
				}
				dataList.add(itemMap);
			}
		}
		return dataList;
	}

	/**
	 * 个股(涨、跌、振幅)排行榜
	 * 
	 * @param starti
	 *            页码
	 * @param counti
	 *            页数
	 * @param typeCode
	 *            排序类型
	 * @return
	 */
	public String stockUpDownList(int starti, int counti, String typeCode) {
		int count = counti == 0 ? 5000 : counti;
		int start = starti == 0 ? 0 : starti;
		if (start != 0) {
			// 计算开始页数
			start = (start - 1) * count;
		}
		Set<String> dataSet = null;
		typeCode = typeCode == null ? "up" : typeCode;
		if (typeCode.equals("up")) {
			// name = "涨幅榜";
			dataSet = RedisUtil.getJedisUtil().SORTSET.zrevrange(Const.RKEY_LEVEL1_SORT_changePct, 0, -1);
		} else if (typeCode.equals("down")) {
			// name = "跌幅榜";
			dataSet = RedisUtil.getJedisUtil().SORTSET.zrange(Const.RKEY_LEVEL1_SORT_changePct, 0, -1);
		} else if (typeCode.equals("swingup")) {
			// name = "振幅榜";
			dataSet = RedisUtil.getJedisUtil().SORTSET.zrevrange(Const.RKEY_LEVEL1_SORT_swing, 0, -1);
		} else if (typeCode.equals("volumnup")) {
			// name = "成交量榜";
			dataSet = RedisUtil.getJedisUtil().SORTSET.zrevrange(Const.RKEY_LEVEL1_SORT_volumnup, 0, -1);
		} else if (typeCode.equals("volumnpriceup")) {
			// name = "成交额榜";
			dataSet = RedisUtil.getJedisUtil().SORTSET.zrevrange(Const.RKEY_LEVEL1_SORT_volumnpriceup, 0, -1);
		} else if (typeCode.equals("turnoverrateup")) {
			// name = "换手率榜";
			dataSet = RedisUtil.getJedisUtil().SORTSET.zrevrange(Const.RKEY_LEVEL1_SORT_turnoverrateup, 0, -1);
		}
		Iterator<String> dataIter = dataSet.iterator();
		int i = 0;
		StringBuffer sb = new StringBuffer();
		StringBuffer sb0 = new StringBuffer();
		while (dataIter.hasNext()) {
			if (i >= start) {
				if (i == start + count) {
					break;
				}
				String detail = dataIter.next();
				com.alibaba.fastjson.JSONArray item = JSON.parseArray(detail);
				double price = new BigDecimal(StrTool.toString(item.get(2))).movePointRight(2).doubleValue();
				if (price == 0) {
					sb0.append(StrTool.toString(item.get(0)).trim()).append(" ").append(StrTool.keep2Point(StrTool.toString(item.get(1))).trim()).append(" ").append(StrTool.keep2Point(StrTool.toString(price)).trim()).append("\n");
				} else {
					sb.append(StrTool.toString(item.get(0)).trim()).append(" ").append(StrTool.keep2Point(StrTool.toString(item.get(1))).trim()).append(" ").append(StrTool.keep2Point(StrTool.toString(price)).trim()).append("\n");
				}
			}
			i++;
		}
		return sb.append(sb0).toString();
	}

	/**
	 * 个股(涨、跌、振幅)详细
	 * 
	 * @param starti
	 *            页码
	 * @param counti
	 *            页数
	 * @param typeCode
	 *            排序类型 default:up
	 *            up、down、swingup、volumnup、volumnpriceup、turnoverrateup
	 * @return
	 */
	public JSONObject upDownList(int starti, int counti, String typeCode) {
		JSONObject result = new JSONObject();
		int count = counti == 0 ? 10 : counti;
		int start = starti == 0 ? 0 : starti;
		if (start != 0) {
			// 算出开始值
			start = (start - 1) * count;
		}
		try {
			JSONArray ja1 = new JSONArray();

			if (!StringUtils.isEmpty(typeCode)) {
				JSONObject jo = getUpDown(start, count, typeCode);
				result.put("data", jo.get("data"));
			} else {
				ja1.add(getUpDown(start, count, "up"));
				ja1.add(getUpDown(start, count, "down"));
				ja1.add(getUpDown(start, count, "swingup"));
				result.put("data", ja1);
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

	public JSONObject upDownListIndex(int starti, int counti, String typeCode) {
		JSONObject result = new JSONObject();
		int count = counti == 0 ? 10 : counti;
		int start = starti == 0 ? 0 : starti;
		if (start != 0) {
			// 算出开始值
			start = (start - 1) * count;
		}
		try {
			JSONArray ja1 = new JSONArray();

			if (!StringUtils.isEmpty(typeCode)) {
				JSONObject jo = getUpDownIndex(start, count, typeCode);
				result.put("data", jo.get("data"));
			} else {
				ja1.add(getUpDownIndex(start, count, "up"));
				ja1.add(getUpDownIndex(start, count, "down"));
				ja1.add(getUpDownIndex(start, count, "swingup"));
				result.put("data", ja1);
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

	/**
	 * 个股(涨、跌、振幅)详细(门户使用)
	 * 
	 * @param starti
	 *            页码
	 * @param counti
	 *            页数
	 * @param typeCode
	 *            排序类型 default:up
	 *            up、down、swingup、volumnup、volumnpriceup、turnoverrateup
	 * @return
	 */
	public JSONObject upDownList2(int starti, int counti, String typeCode) {
		JSONObject result = new JSONObject();
		int count = counti == 0 ? 10 : counti;
		int start = starti == 0 ? 0 : starti;
		if (start != 0) {
			// 算出开始值
			start = (start - 1) * count;
		}
		try {
			JSONArray ja1 = new JSONArray();

			if (!StringUtils.isEmpty(typeCode)) {
				JSONObject jo = getUpDown(start, count, typeCode);
				result.put("data", jo.get("data"));
			} else {
				ja1.add(getUpDown(start, count, "up"));
				ja1.add(getUpDown(start, count, "down"));
				ja1.add(getUpDown(start, count, "swingup"));
				result.put("data", ja1);
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

	/**
	 * 获取指数涨跌幅
	 * 
	 * @return
	 */
	public JSONObject indexUpDownList() {
		JSONObject result = new JSONObject();
		List<Map<String, String>> resultDataList = new ArrayList<Map<String, String>>();
		Set<String> dataSet = RedisUtil.getJedisUtil().SORTSET.zrevrange(Const.RKEY_LEVEL1_SORT_changePct, 0, 2);
		Set<String> dataSet1 = RedisUtil.getJedisUtil().SORTSET.zrange(Const.RKEY_LEVEL1_SORT_changePct, 0, 2);
		dataSet.addAll(dataSet1);
		Iterator<String> dataIter = dataSet.iterator();
		while (dataIter.hasNext()) {
			String data = dataIter.next();
			com.alibaba.fastjson.JSONArray item = JSON.parseArray(data);
			com.alibaba.fastjson.JSONArray codePriceAry = JSON.parseArray(hash.hget(Const.RKEY_LEVEL1_, StrTool.toString(item.get(0))));
			if (codePriceAry != null) {
				Map<String, String> codePriceMap = StockUtils.stockPriceArray2Map(codePriceAry, null);
				resultDataList.add(codePriceMap);
			}
		}
		result.put("data", resultDataList);
		result.put("error", 0);
		result.put("msg", "");
		result.put("success", true);
		return result;
	}

	/**
	 * 获取指数板块排行
	 * 
	 * @return
	 */
	public JSONObject indexTradeUpDownList() {
		JSONObject result = new JSONObject();
		Set<String> tradeSortUp = RedisUtil.getJedisUtil().SORTSET.zrevrange(Const.RKEY_INDUSTRY_SORT_tradeRate, 0, 2);
		Set<String> tradeSortDown = RedisUtil.getJedisUtil().SORTSET.zrange(Const.RKEY_INDUSTRY_SORT_tradeRate, 0, 2);
		tradeSortUp.addAll(tradeSortDown);
		result.put("data", tradeSortUp);
		result.put("error", 0);
		result.put("msg", "");
		result.put("success", true);
		return result;
	}

	/**
	 * 通用行业排行
	 * 
	 * @param start
	 *            开始条数
	 * @param count
	 *            结束条数
	 * @param typeCode
	 *            类型
	 * @return
	 * @throws Exception
	 */
	private JSONObject getUpDown(Integer start, Integer count, String typeCode) throws Exception {

		Set<String> dataSet = null;
		List<Map<String, String>> resultDataList = new ArrayList<Map<String, String>>();
		String name = "";
		typeCode = typeCode == null ? "up" : typeCode;
		if (typeCode.equals("up")) {
			name = "涨幅榜";
			dataSet = RedisUtil.getJedisUtil().SORTSET.zrevrange(Const.RKEY_LEVEL1_SORT_changePct, 0, 9);
		} else if (typeCode.equals("down")) {
			name = "跌幅榜";
			dataSet = RedisUtil.getJedisUtil().SORTSET.zrange(Const.RKEY_LEVEL1_SORT_changePct, 0, 9);
		} else if (typeCode.equals("swingup")) {
			name = "振幅榜";
			dataSet = RedisUtil.getJedisUtil().SORTSET.zrevrange(Const.RKEY_LEVEL1_SORT_swing, 0, 9);
		} else if (typeCode.equals("volumnup")) {
			name = "成交量榜";
			dataSet = RedisUtil.getJedisUtil().SORTSET.zrevrange(Const.RKEY_LEVEL1_SORT_volumnup, 0, 9);
		} else if (typeCode.equals("volumnpriceup")) {
			name = "成交额榜";
			dataSet = RedisUtil.getJedisUtil().SORTSET.zrevrange(Const.RKEY_LEVEL1_SORT_volumnpriceup, 0, 9);
		} else if (typeCode.equals("turnoverrateup")) {
			name = "换手率榜";
			dataSet = RedisUtil.getJedisUtil().SORTSET.zrevrange(Const.RKEY_LEVEL1_SORT_turnoverrateup, 0, 9);
		}

		Iterator<String> dataIter = dataSet.iterator();
		int i = 0;
		while (dataIter.hasNext()) {
			if (i >= start) {
				if (i == start + count) {
					break;
				}
				String detail = dataIter.next();
				com.alibaba.fastjson.JSONArray item = JSON.parseArray(detail);
				com.alibaba.fastjson.JSONArray codePriceAry = JSON.parseArray(hash.hget(Const.RKEY_LEVEL1_, StrTool.toString(item.get(0))));
				if (codePriceAry != null) {
					Map<String, String> codePriceMap = StockUtils.stockPriceArray2Map(codePriceAry, null);
					// 获取振幅数据
					if (typeCode.equals("swingup")) {
						codePriceMap.put("changeRate", codePriceMap.get("swing"));
					}
					resultDataList.add(codePriceMap);
				}
			}
			i++;
		}

		JSONObject jo1 = new JSONObject();
		jo1.put("name", name);
		jo1.put("typeCode", typeCode);
		jo1.put("data", resultDataList);
		return jo1;
	}

	private JSONObject getUpDownIndex(Integer start, Integer count, String typeCode) throws Exception {

		Set<String> dataSet = null;
		List<String> resultDataList = new ArrayList<String>();
		String name = "";
		typeCode = typeCode == null ? "up" : typeCode;
		if (typeCode.equals("up")) {
			name = "涨幅榜";
			dataSet = RedisUtil.getJedisUtil().SORTSET.zrevrange(Const.RKEY_LEVEL1_SORT_changePct, 0, 9);
		} else if (typeCode.equals("down")) {
			name = "跌幅榜";
			dataSet = RedisUtil.getJedisUtil().SORTSET.zrange(Const.RKEY_LEVEL1_SORT_changePct, 0, 9);
		} else if (typeCode.equals("swingup")) {
			name = "振幅榜";
			dataSet = RedisUtil.getJedisUtil().SORTSET.zrevrange(Const.RKEY_LEVEL1_SORT_swing, 0, 9);
		} else if (typeCode.equals("volumnup")) {
			name = "成交量榜";
			dataSet = RedisUtil.getJedisUtil().SORTSET.zrevrange(Const.RKEY_LEVEL1_SORT_volumnup, 0, 9);
		} else if (typeCode.equals("volumnpriceup")) {
			name = "成交额榜";
			dataSet = RedisUtil.getJedisUtil().SORTSET.zrevrange(Const.RKEY_LEVEL1_SORT_volumnpriceup, 0, 9);
		} else if (typeCode.equals("turnoverrateup")) {
			name = "换手率榜";
			dataSet = RedisUtil.getJedisUtil().SORTSET.zrevrange(Const.RKEY_LEVEL1_SORT_turnoverrateup, 0, 9);
		}

		Iterator<String> dataIter = dataSet.iterator();
		while (dataIter.hasNext()) {
			String detail = dataIter.next();
			com.alibaba.fastjson.JSONArray item = JSON.parseArray(detail);
			item.set(2, StrTool.keep2Point(StrTool.toString(item.getDouble(2) * 100.0)));
			com.alibaba.fastjson.JSONArray codePriceAry = JSON.parseArray(hash.hget(Const.RKEY_LEVEL1_, StrTool.toString(item.get(0))));
			item.set(3, codePriceAry.getString(Const.PRICE_shortNM));
			resultDataList.add(JSON.toJSONString(item));
		}

		JSONObject jo1 = new JSONObject();
		jo1.put("name", name);
		jo1.put("typeCode", typeCode);
		jo1.put("data", resultDataList);
		return jo1;
	}

	/**
	 * 获取五档明细数据
	 * 
	 * @param start
	 *            开始页数
	 * @param count
	 * @param simpleCode
	 * @return
	 */
	public JSONObject getLevel1Detail(Integer start, Integer count, String simpleCode) {
		Integer startnum = (start * count - count) < 0 ? 0 : start * count - count;
		Integer endnum = start * count;
		JSONObject result = new JSONObject();
		if (simpleCode != null && !"".equals(simpleCode)) {
			try {
				String startTime = DateTool.DateToStr(new Date(), "HH:mm");
				if (Double.parseDouble(DateTool.changeDateFormat(startTime, "HH:mm", "HHmm")) > 1505) {
					startTime = "14:50";
				} else if (Double.parseDouble(DateTool.changeDateFormat(startTime, "HH:mm", "HHmm")) < 915) {
					startTime = "09:15";
				}
				List data = getTLLevel1Data(startnum, endnum, simpleCode, startTime);
				result.put("data", data);
				result.put("count", data.size());
				result.put("error", 0);
				result.put("msg", "");
				result.put("success", true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			result.put("error", 1);
			result.put("msg", "股票代码不能为空");
			result.put("success", false);
		}
		return result;
	}

	public JSONObject getLevel1DetailData(Integer start, Integer count, String simpleCode) {
		Integer startnum = (start * count - count) < 0 ? 0 : start * count - count;
		Integer endnum = start * count;
		JSONObject result = new JSONObject();
		if (simpleCode != null && !"".equals(simpleCode)) {
			com.alibaba.fastjson.JSONArray detailAry = JSON.parseArray(hash.hget(Const.RKEY_LEVEL1_DETAIL_, simpleCode));
			List data = new ArrayList();
			if (detailAry != null) {
				if (detailAry.size() > endnum) {
					data = detailAry.subList(startnum, endnum);
				} else {
					if (startnum < detailAry.size()) {
						data = detailAry.subList(startnum, detailAry.size());
					}
				}
			}
			result.put("data", data);
			result.put("count", data.size());
			result.put("error", 0);
			result.put("msg", "");
			result.put("success", true);
		} else {
			result.put("error", 1);
			result.put("msg", "股票代码不能为空");
			result.put("success", false);
		}
		return result;
	}

	/**
	 * 重试次数
	 */
	private int retryNum = 0;

	/**
	 * 读取通联的接口数据并处理
	 * 
	 * @param startnum
	 *            开始条数
	 * @param endnum
	 *            结束条数
	 * @param simpleCode
	 *            股票代码
	 * @param startTime
	 *            开始时间
	 * @return
	 */
	public synchronized List getTLLevel1Data(Integer startnum, Integer endnum, String simpleCode, String startTime) {
		List data = new ArrayList();
		try {
			String endTime = DateTool.DateToStr(DateTool.minuteChange(new Date(), 1), "HH:mm");

			if (Double.parseDouble(DateTool.changeDateFormat(endTime, "HH:mm", "HHmm")) > 1505 && Double.parseDouble(DateTool.changeDateFormat(endTime, "HH:mm", "HHmm")) < 1530) {
				endTime = "15:05";
			} else if (Double.parseDouble(DateTool.changeDateFormat(endTime, "HH:mm", "HHmm")) >= 1530) {
				com.alibaba.fastjson.JSONArray level1Ary = JSON.parseArray(hash.hget(Const.RKEY_LEVEL1_DETAIL_, simpleCode));
				if (level1Ary.size() > endnum) {
					return level1Ary.subList(startnum, endnum);
				} else {
					return level1Ary.subList(startnum, level1Ary.size());
				}
			}

			String tlCode = StockUtils.getTLStockCode(simpleCode);
			CsvReader readerLevel1 = WmcloudUtil.url2csv(Const.LEVEL1DETAIL + "?field=dataTime,volume,lastPrice&securityID=" + tlCode + "&startTime=" + startTime + "&endTime=" + endTime);
			if (readerLevel1 != null) {
				readerLevel1.readHeaders();
				String lastVolume = "";
				while (readerLevel1.readRecord()) {
					String[] items = readerLevel1.getValues();
					String time = items[1];
					String price = items[5];
					String volume = items[4];
					if (lastVolume == "") {
						lastVolume = volume;
					} else {
						if (!lastVolume.equals(volume)) {
							List<String> list = new ArrayList<String>();
							list.add(time);
							list.add(price);

							Integer volumeDif = StrTool.toInt(volume) - StrTool.toInt(lastVolume);
							if (volumeDif > 0) {
								list.add(StrTool.toString(volumeDif / 100));
							} else {
								list.add(StrTool.toString(volumeDif / 100).substring(1));
							}
							lastVolume = volume;
							data.add(list);
						} else {
							continue;
						}
					}
				}
				// 数据倒叙
				Collections.reverse(data);
				if (data.size() > endnum) {
					data = data.subList(startnum, endnum);
					return data;
				} else {
					retryNum++;
					if (retryNum > 20) {
						retryNum = 0;
						Collections.reverse(data);
						if (!data.isEmpty()) {
							if (data.size() > 10) {
								data = data.subList(0, 10);
							} else {
								data = data.subList(0, data.size());
							}
						}
						return data;
					} else {
						int min = retryNum * retryNum;
						String newStartTime = DateTool.DateToStr(DateTool.minuteChange(new Date(), -min), "HH:mm");
						if (Double.parseDouble(DateTool.changeDateFormat(newStartTime, "HH:mm", "HHmm")) > 1505) {
							newStartTime = "14:50";
						} else if (Double.parseDouble(DateTool.changeDateFormat(newStartTime, "HH:mm", "HHmm")) < 915) {
							newStartTime = "09:15";
						}
						return getTLLevel1Data(startnum, endnum, simpleCode, newStartTime);
					}
				}
			} else {
				return data;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		retryNum = 0;
		return data;
	}

	public static void main(String[] args) {
		// plm.stockUpDownList(2, 30, null);
		// plm.getLevel1Detail(4, 10, "sh600520");
	}
}
