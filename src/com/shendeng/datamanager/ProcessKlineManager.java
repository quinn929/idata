package com.shendeng.datamanager;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.alibaba.fastjson.JSON;
import com.shendeng.utils.Const;
import com.shendeng.utils.DateTool;
import com.shendeng.utils.JedisUtil.Hash;
import com.shendeng.utils.JedisUtil.SortSet;
import com.shendeng.utils.RedisUtil;
import com.shendeng.utils.StrTool;

/**
 * 查询K线数据
 *
 * @author qy
 * @date 2016年6月10日
 */
public class ProcessKlineManager {

	private static ProcessKlineManager pklm = new ProcessKlineManager();

	public static ProcessKlineManager getInstance() {
		return pklm;
	}
	
	Hash hash = RedisUtil.getJedisUtil().HASH;
	
	SortSet sortSet = RedisUtil.getJedisUtil().SORTSET;
	
	/**
	 * 获取分时数据
	 * 
	 * @param simpleCode
	 * @return
	 * @throws IOException
	 */
	public JSONObject getStockMinute(String simpleCode) throws IOException {
		com.alibaba.fastjson.JSONArray stockInfo = JSON.parseArray(hash.hget(Const.RKEY_LEVEL1_, simpleCode));
		Set<String> minuteSet = sortSet.zrange(Const.RKEY_MINS_ + simpleCode, 0, -1);
		Iterator<String> minuteIter = minuteSet.iterator();
		JSONObject dataItem = null;
		JSONArray data = new JSONArray();
		while (minuteIter.hasNext()) {
			String minuteStr = minuteIter.next();
			com.alibaba.fastjson.JSONArray minuteAry = JSON.parseArray(minuteStr);
			dataItem = new JSONObject();
			Double closePrice = StrTool.toDouble(minuteAry.get(7));
			Double openPrice = StrTool.toDouble(minuteAry.get(8));
			dataItem.put("datetime", minuteAry.get(6));
			dataItem.put("price", closePrice);
			dataItem.put("prevClosePrice", stockInfo.get(Const.PRICE_prevClosePrice));
			dataItem.put("volumnPrice", StrTool.keep2Point(minuteAry.getString(12)));
			dataItem.put("volumn", StrTool.toDouble(minuteAry.get(11)) / 100);
			dataItem.put("color", minuteAry.get(6));
			int r = openPrice.compareTo(closePrice);
			// 返回颜色 红绿
			if(r == 0){
				dataItem.put("color", 2);
			}else{
				dataItem.put("color", r > 0 ? 0 : 1);
			}
			data.add(dataItem);
		}
		JSONObject stockMap = new JSONObject();
		stockMap.put("error", 0);
		stockMap.put("msg", "");
		stockMap.put("success", true);
		stockMap.put("data", data.toString());
		return stockMap;
	}
	
	/**
	 * 获取日K数据
	 * 
	 * @param simpleCode
	 *            股票代码
	 */
	public JSONObject getStockDays(String simpleCode) {
		Set<String> daySet = sortSet.zrevrange(Const.RKEY_K_D_ + simpleCode, 0, -1);
		Iterator dayIter = daySet.iterator();
		String shsz = simpleCode.substring(0, 2);
		JSONObject dataItem = null;
		JSONArray data = new JSONArray();
		while (dayIter.hasNext()) {
			com.alibaba.fastjson.JSONArray dateList = JSON.parseArray(StrTool.toString(dayIter.next()));
			if (StrTool.toDouble(dateList.get(5)) == 0) {
				continue;
			}
			dataItem = ProcessKlineManager.processKline(dateList, shsz);
			data.add(dataItem);
		}
		JSONObject stockMap = new JSONObject();
		stockMap.put("error", 0);
		stockMap.put("msg", "");
		stockMap.put("success", true);
		stockMap.put("data", data);
		return stockMap;
	}

	/**
	 * 获取周K
	 * 
	 * @param simpleCode
	 *            股票代码
	 */
	public JSONObject getStockWeeks(String simpleCode) {
		Set<String> weekSet = sortSet.zrevrange(Const.RKEY_K_W_ + simpleCode, 0, -1);

		Iterator dayIter = weekSet.iterator();
		String shsz = simpleCode.substring(0, 2);
		JSONObject dataItem = null;
		JSONArray data = new JSONArray();

		while (dayIter.hasNext()) {
			com.alibaba.fastjson.JSONArray dateList = JSON.parseArray(StrTool.toString(dayIter.next()));
			// 成交量为0 跳过
			if (StrTool.toDouble(dateList.get(5)) == 0) {
				continue;
			}
			dataItem = ProcessKlineManager.processKline(dateList, shsz);
			data.add(dataItem);
		}

		JSONObject stockMap = new JSONObject();
		stockMap.put("error", 0);
		stockMap.put("msg", "");
		stockMap.put("success", true);
		stockMap.put("data", data);
		return stockMap;
	}
	
	/**
	 * 获取月K
	 * 
	 * @param simpleCode
	 *            股票代码
	 */
	public JSONObject getStockMonths(String simpleCode) {
		Set<String> monthSet = sortSet.zrevrange(Const.RKEY_K_M_ + simpleCode, 0, -1);

		Iterator monthIter = monthSet.iterator();

		String shsz = simpleCode.substring(0, 2);
		JSONObject dataItem = null;
		JSONArray data = new JSONArray();
		while (monthIter.hasNext()) {
			com.alibaba.fastjson.JSONArray dateList = JSON.parseArray(StrTool.toString(monthIter.next()));
			// 成交量为0 跳过
			if (StrTool.toDouble(dateList.get(5)) == 0) {
				continue;
			}
			dataItem = ProcessKlineManager.processKline(dateList, shsz);
			data.add(dataItem);
		}

		JSONObject stockMap = new JSONObject();
		stockMap.put("error", 0);
		stockMap.put("msg", "");
		stockMap.put("success", true);
		stockMap.put("data", data);
		return stockMap;
	}
	
	/**
	 * 获取指数日K
	 * 
	 * @param simpleCode
	 *            股票代码
	 * @return
	 */
	public JSONObject getIndexDays(String simpleCode) {
		Set<String> indexDaySet = sortSet.zrevrange(Const.RKEY_K_D_ + simpleCode, 0, -1);
		Iterator indexDayIter = indexDaySet.iterator();

		JSONObject dataItem = null;
		JSONArray data = new JSONArray();

		int num = 0;
		while (indexDayIter.hasNext()) {
			// 默认取300条
			if (num == 300) {
				break;
			}
			com.alibaba.fastjson.JSONArray dateList = JSON.parseArray(StrTool.toString(indexDayIter.next()));
			// 成交量为0 跳过
			if (StrTool.toDouble(dateList.get(Const.IndexKline_turnoverVol)) == 0) {
				continue;
			}
			dataItem = new JSONObject();
			dataItem.put("code", simpleCode);
			dataItem.put("closePrice", StrTool.keep2Point(dateList.getString(Const.IndexKline_closeIndex)));
			dataItem.put("openPrice", StrTool.keep2Point(dateList.getString(Const.IndexKline_openIndex)));
			dataItem.put("heightPrice", StrTool.keep2Point(dateList.getString(Const.IndexKline_highestIndex)));
			dataItem.put("lowPrice", StrTool.keep2Point(dateList.getString(Const.IndexKline_lowestIndex)));
			dataItem.put("volumn", String.format("%.2f",new BigDecimal(dateList.getString(Const.IndexKline_turnoverVol)).movePointLeft(2).doubleValue()));
			dataItem.put("volPrice", StrTool.keep2Point(dateList.getString(Const.IndexKline_turnoverValue)));
			dataItem.put("datetime", StrTool.toString(dateList.get(Const.IndexKline_tradeDate)).replace("-", ""));
			dataItem.put("yestodayClosePrice", StrTool.toDouble(dateList.get(Const.IndexKline_preCloseIndex)));
			data.add(dataItem);
			num++;
		}
		JSONObject stockMap = new JSONObject();
		stockMap.put("error", 0);
		stockMap.put("msg", "");
		stockMap.put("success", true);
		stockMap.put("data", data);
		return stockMap;
	}

	/**
	 * 获取指数周K
	 * 
	 * @param simpleCode
	 *            股票代码
	 * @param beginDate
	 *            开始时间(yyyyMMdd)
	 * @param endDate
	 *            结束时间(yyyyMMdd)
	 * @return
	 * @throws Exception
	 */
	public JSONObject getIndexWeeks(String simpleCode, String beginDate, String endDate) {
		Date date = new Date();
		beginDate = beginDate == null ? DateTool.DateToStr(DateTool.yearChange(date, -2), "yyyyMMdd") : beginDate;
		endDate = endDate == null ? DateTool.DateToStr(date, "yyyyMMdd") : endDate;
		Set<String> indexWeekSet = sortSet.zrevrange(Const.RKEY_K_D_ + simpleCode, 0, -1);
		Iterator indexWeekIter = indexWeekSet.iterator();

		JSONObject dataItem = null;
		List indexDayList = null;
		JSONArray data = new JSONArray();
		Map<String, List> map = new HashMap<String, List>();
		while (indexWeekIter.hasNext()) {
			com.alibaba.fastjson.JSONArray dateList = JSON.parseArray(StrTool.toString(indexWeekIter.next()));
			map.put(StrTool.toString(dateList.get(Const.IndexKline_tradeDate)), dateList);
		}
		try {
			// 获取周数组
			Map<String, String[]> weeks = DateTool.getWeeks(DateTool.convertDate(beginDate, "yyyyMMdd"), DateTool.convertDate(endDate, "yyyyMMdd"));
			Iterator<String> it = weeks.keySet().iterator();
			String key = null;
			while (it.hasNext()) {
				// 以天为key算出周k
				dataItem = new JSONObject();
				key = it.next();
				String[] ss = weeks.get(key);
				double closeIndex = 0;
				double openIndex = 0;
				double highestIndex = 0;
				double lowestIndex = 0;
				double turnoverVol = 0;
				double turnoverValue = 0;
				double preCloseIndex = 0;
				for (int i = 0; i < ss.length; i++) {
					indexDayList = map.get(ss[i]);
					// 当天数据
					if (DateTool.DateToStr(new Date(), "yyyy-MM-dd").equals(ss[i]) && i == 0) {
						com.alibaba.fastjson.JSONArray item =  JSON.parseArray(hash.hget(Const.RKEY_LEVEL1_,simpleCode));
						closeIndex = Double.parseDouble(StrTool.toString(item.get(Const.PRICE_lastPrice)));
						// 如果是星期日
						if (DateTool.getWeekOfNow() == 0) {
							openIndex = Double.parseDouble(StrTool.toString(item.get(Const.PRICE_openPrice)));
							preCloseIndex = Double.parseDouble(StrTool.toString(item.get(Const.PRICE_prevClosePrice)));
							lowestIndex = Double.parseDouble(StrTool.toString(item.get(Const.PRICE_lowPrice)));
						}
						highestIndex = Double.parseDouble(StrTool.toString(item.get(Const.PRICE_highPrice)));
						turnoverVol = Double.parseDouble(StrTool.toString(item.get(Const.PRICE_volume)));
						if(simpleCode.equals("sh000001")){
							turnoverValue = new BigDecimal(item.getString(Const.PRICE_value)).movePointRight(2).doubleValue();
						}else{
							turnoverValue = Double.parseDouble(StrTool.toString(item.get(Const.PRICE_value)));
						}
						
					}

					if (indexDayList == null) {
						continue;
					}
					if (openIndex == 0) {// 开盘价是周一的开盘价
						openIndex = StrTool.toDouble(indexDayList.get(Const.IndexKline_openIndex));
						// 周一的昨日收盘也是当周的上周收盘
						preCloseIndex = StrTool.toDouble(indexDayList.get(Const.IndexKline_preCloseIndex));
						lowestIndex = StrTool.toDouble(indexDayList.get(Const.IndexKline_lowestIndex));
					}
					// 最高价 当周最高价
					if (highestIndex < StrTool.toDouble(indexDayList.get(Const.IndexKline_highestIndex))) {
						highestIndex = StrTool.toDouble(indexDayList.get(Const.IndexKline_highestIndex));
					}
					// 周低价 当周最低价
					if (lowestIndex > StrTool.toDouble(indexDayList.get(Const.IndexKline_lowestIndex))) {
						lowestIndex = StrTool.toDouble(indexDayList.get(Const.IndexKline_lowestIndex));
					}
					// 收盘价是周五的收盘价
					closeIndex = StrTool.toDouble(indexDayList.get(Const.IndexKline_closeIndex));
					// 成交额
					turnoverVol += StrTool.toDouble(indexDayList.get(Const.IndexKline_turnoverVol));
					// 成交量
					turnoverValue += StrTool.toDouble(indexDayList.get(Const.IndexKline_turnoverValue));
				}
				dataItem.put("code", simpleCode);
				dataItem.put("closePrice", StrTool.keep2Point(StrTool.toString(closeIndex)));
				dataItem.put("openPrice", StrTool.keep2Point(StrTool.toString(openIndex)));
				dataItem.put("heightPrice", StrTool.keep2Point(StrTool.toString(highestIndex)));
				dataItem.put("lowPrice", StrTool.keep2Point(StrTool.toString(lowestIndex)));
				dataItem.put("volumn", new DecimalFormat("0").format(turnoverVol / 100));
				dataItem.put("volPrice", StrTool.keep2Point(StrTool.toString(turnoverValue)));
				dataItem.put("datetime", key);
				dataItem.put("yestodayClosePrice", preCloseIndex);
				// 开盘价和收盘价都不等于0 的才计算
				if (preCloseIndex != 0 && openIndex != 0) {
					data.add(dataItem);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		JSONObject stockMap = new JSONObject();
		stockMap.put("error", 0);
		stockMap.put("msg", "");
		stockMap.put("success", true);
		stockMap.put("data", data);
		return stockMap;
	}

	/**
	 * 获取指数月K
	 * 
	 * @param simpleCode
	 *            股票代码
	 * @param beginDate
	 *            开始日期
	 * @param endDate
	 *            结束日期
	 * @return
	 */
	public JSONObject getIndexMonths(String simpleCode, String beginDate, String endDate) {
		Date date = new Date();
		beginDate = beginDate == null ? DateTool.DateToStr(DateTool.yearChange(date, -10), "yyyyMMdd") : beginDate;
		endDate = endDate == null ? DateTool.DateToStr(date, "yyyyMMdd") : endDate;
		Set<String> indexMonthSet = sortSet.zrevrange(Const.RKEY_K_D_ + simpleCode, 0, -1);
		Iterator indexMonthIter = indexMonthSet.iterator();

		JSONObject dataItem = null;
		List indexDayList = null;
		JSONArray data = new JSONArray();

		Map<String, List> map = new HashMap<String, List>();
		while (indexMonthIter.hasNext()) {
			com.alibaba.fastjson.JSONArray dateList = JSON.parseArray(StrTool.toString(indexMonthIter.next()));
			map.put(StrTool.toString(dateList.get(Const.IndexKline_tradeDate)), dateList);
		}
		// 获取月数组
		Map<String, String[]> months = DateTool.getMonths(DateTool.convertDate(beginDate, "yyyyMMdd"), DateTool.convertDate(endDate, "yyyyMMdd"));
		Iterator<String> it = months.keySet().iterator();
		String key = null;
		while (it.hasNext()) {
			// 以天为key拼接月K
			dataItem = new JSONObject();
			key = it.next();
			String[] ss = months.get(key);
			double closeIndex = 0;
			double openIndex = 0;
			double highestIndex = 0;
			double lowestIndex = 0;
			double turnoverVol = 0;
			double turnoverValue = 0;
			double preCloseIndex = 0;
			String first = ss[0];
			String last = ss[1];
			while (true) {
				indexDayList = map.get(first);
				// 当天数据处理
				if (DateTool.DateToStr(new Date(), "yyyy-MM-dd").equals(first)) {
					com.alibaba.fastjson.JSONArray item = JSON.parseArray(hash.hget(Const.RKEY_LEVEL1_,simpleCode));
//					List item = (List) level1Map.get(simpleCode);
					closeIndex = Double.parseDouble(StrTool.toString(item.get(Const.PRICE_lastPrice)));
					if (DateTool.getWeekOfNow() == 0) {
						openIndex = Double.parseDouble(StrTool.toString(item.get(Const.PRICE_openPrice)));
						preCloseIndex = Double.parseDouble(StrTool.toString(item.get(Const.PRICE_prevClosePrice)));
						lowestIndex = Double.parseDouble(StrTool.toString(item.get(Const.PRICE_lowPrice)));
					}
					highestIndex = Double.parseDouble(StrTool.toString(item.get(Const.PRICE_highPrice)));
					turnoverVol = Double.parseDouble(StrTool.toString(item.get(Const.PRICE_volume)));
					if(simpleCode.equals("sh000001")){
						turnoverValue = new BigDecimal(item.getString(Const.PRICE_value)).movePointRight(2).doubleValue();
					}else{
						turnoverValue = Double.parseDouble(StrTool.toString(item.get(Const.PRICE_value)));
					}
				}
				if (indexDayList != null && indexDayList.get(Const.IndexKline_closeIndex) != null && !"".equals(StrTool.toString(indexDayList.get(Const.IndexKline_closeIndex)))) {
					// 收盘价是当月周后一次的收盘价
					closeIndex = StrTool.toDouble(indexDayList.get(Const.IndexKline_closeIndex));
					if (openIndex == 0) {// 开盘价是周一的开盘价
						String klineOpenIndex = StrTool.toString(indexDayList.get(Const.IndexKline_openIndex));
						if(klineOpenIndex == null || "".equals(klineOpenIndex)){
							klineOpenIndex = "0";
						}
						openIndex = StrTool.toDouble(klineOpenIndex);
						// 周一的昨日收盘也是当周的上周收盘
						if (StrTool.toString(indexDayList.get(Const.IndexKline_preCloseIndex)) != null && StrTool.toString(indexDayList.get(Const.IndexKline_preCloseIndex)).length() > 0) {
							preCloseIndex = StrTool.toDouble(StrTool.toString(indexDayList.get(Const.IndexKline_preCloseIndex)));
						} else {
							preCloseIndex = 0;
						}

						String klineLowestIndex = StrTool.toString(indexDayList.get(Const.IndexKline_lowestIndex));
						if(klineLowestIndex == null || "".equals(klineLowestIndex)){
							klineLowestIndex = "0";
						}
						lowestIndex = StrTool.toDouble(klineLowestIndex);
					}
					// 最高价 当周最高价
					
					String klineHighestIndex = StrTool.toString(indexDayList.get(Const.IndexKline_highestIndex));
					if(klineHighestIndex == null || "".equals(klineHighestIndex)){
						klineHighestIndex = "0";
					}
					
					if (highestIndex < StrTool.toDouble(klineHighestIndex)) {
						highestIndex = StrTool.toDouble(klineHighestIndex);
					}
					// 周低价 当周最低价
					String klineLowestIndex = StrTool.toString(indexDayList.get(Const.IndexKline_lowestIndex));
					if(klineLowestIndex == null || "".equals(klineLowestIndex)){
						klineLowestIndex = "0";
					}
					if (lowestIndex > StrTool.toDouble(klineLowestIndex)) {
						lowestIndex = StrTool.toDouble(klineLowestIndex);
					}
					// 成交额
					String klineTurnoverVol = StrTool.toString(indexDayList.get(Const.IndexKline_turnoverVol));
					if(klineTurnoverVol == null || "".equals(klineTurnoverVol)){
						klineTurnoverVol = "0";
					}
					turnoverVol += StrTool.toDouble(klineTurnoverVol);
					// 成交量
					String klineTurnoverValue = StrTool.toString(indexDayList.get(Const.IndexKline_turnoverValue));
					if(klineTurnoverValue == null || "".equals(klineTurnoverValue)){
						klineTurnoverValue = "0";
					}
					turnoverValue += StrTool.toDouble(klineTurnoverValue);
				}
				// 开始日期和结束日期是同一天不用计算直接跳出
				if (first.equals(last)) {
					break;
				} else {
					first = DateTool.DateToStr(DateTool.dayChange(DateTool.convertDate(first, "yyyy-MM-dd"), 1), "yyyy-MM-dd");
				}
			}
			if (closeIndex == 0 && openIndex == 0 && highestIndex == 0 && lowestIndex == 0 && turnoverVol == 0 && turnoverValue == 0 && preCloseIndex == 0) {
				// 收盘价、开盘价、最高价、最低价、成交量、成交金额、昨日收盘价同时为0的就不需要了
				continue;
			}

			dataItem.put("code", simpleCode);
			dataItem.put("closePrice", StrTool.keep2Point(StrTool.toString(closeIndex)));
			dataItem.put("openPrice", StrTool.keep2Point(StrTool.toString(openIndex)));
			dataItem.put("heightPrice", StrTool.keep2Point(StrTool.toString(highestIndex)));
			dataItem.put("lowPrice", StrTool.keep2Point(StrTool.toString(lowestIndex)));
			dataItem.put("volumn", new DecimalFormat("0").format(turnoverVol / 100));
			dataItem.put("volPrice", StrTool.keep2Point(StrTool.toString(turnoverValue)));
			dataItem.put("datetime", DateTool.strDateToIntegerDate(key, "yyyy-MM-dd", "yyyyMMdd") + "");
			dataItem.put("yestodayClosePrice", preCloseIndex);
			data.add(dataItem);
		}
		JSONObject stockMap = new JSONObject();
		stockMap.put("error", 0);
		stockMap.put("msg", "");
		stockMap.put("success", true);
		stockMap.put("data", data);
		return stockMap;
	}
	
	/**
	 * 取K线数据(K线数据返回字段一样，通用)
	 * 
	 * @param list
	 *            K线数据list
	 * @param shsz
	 *            交易市场简称
	 * @return
	 */
	public static JSONObject processKline(List list, String shsz) {
		JSONObject dataItem = new JSONObject();
		dataItem.put("code", shsz + list.get(Const.Kline_ticker));
		dataItem.put("closePrice", StrTool.keep2Point(StrTool.toString(list.get(Const.Kline_closePrice))));
		dataItem.put("openPrice", StrTool.keep2Point(StrTool.toString(list.get(Const.Kline_openPrice))));
		dataItem.put("heightPrice", StrTool.keep2Point(StrTool.toString(list.get(Const.Kline_highestPrice))));
		dataItem.put("lowPrice", StrTool.keep2Point(StrTool.toString(list.get(Const.Kline_lowestPrice))));
		dataItem.put("volumn", StrTool.toDouble(list.get(Const.Kline_turnoverVol)) / 100);
		dataItem.put("volPrice", StrTool.keep2Point(StrTool.toString(list.get(Const.Kline_turnoverValue))));
		dataItem.put("datetime", StrTool.toString(list.get(Const.Kline_tradeDate)).replace("-", ""));
		dataItem.put("yestodayClosePrice", StrTool.toDouble(list.get(Const.Kline_preClosePrice)));
		if(list.get(Const.Kline_chgPct) != null && !"".equals(list.get(Const.Kline_chgPct))){
			dataItem.put("chgPct", String.format("%.2f",new BigDecimal(StrTool.toString(list.get(Const.Kline_chgPct))).movePointRight(2).doubleValue()));	
		}
		return dataItem;
	}

}
