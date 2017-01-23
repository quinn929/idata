package com.shendeng.datamanager;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

import com.alibaba.fastjson.JSON;
import com.csvreader.CsvReader;
import com.shendeng.cluster.ClusterUtil;
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
 * K线 分时 数据逻辑处理
 * 
 * @author naxj
 * 
 */
public class KlineManager {

	private static KlineManager klm = new KlineManager();

	public static KlineManager getInstance() {
		return klm;
	}

	private SortSet sorset = RedisUtil.getJedisUtil().SORTSET;

	Hash hash = RedisUtil.getJedisUtil().HASH;

	/**
	 * K线相关的初始化信息
	 * 
	 * @throws Exception
	 * 
	 */
	public void KlineInit() throws Exception {
		try {
			System.out.println("执行k线相关任务" + DateTool.DateToStr(new Date(), DateTool.TIME_FORMAT));
			// 获取全部的股票代码
//			ExecutorService threadPool = Executors.newFixedThreadPool(10);
			ForkJoinPool threadPool = new ForkJoinPool(10);
			for (Object obj : hash.hgetAll(Const.RKEY_STOCK_INFO_).keySet()) {
				String code = StrTool.toString(obj);
				// 初始化分时
				// klm.initStockMinute(code, sorset);
				threadPool.submit(new initStockMinute(code, sorset, true));
				// 初始化日K
				// klm.initStockDays(code, null, null, sorset);
				threadPool.submit(new initStockDays(code, sorset, null, null, true));
				// // 初始化周K
				// klm.initStockWeeks(code, null, null, sorset);
				threadPool.submit(new initStockWeeks(code, sorset, null, null, true));
				// // 初始化月K
				// klm.initStockMonths(code, null, null, sorset);
				threadPool.submit(new initStockMonths(code, sorset, null, null, true));
			}
			// 初始化指数日K
			klm.initIndexDays(null, null, sorset, true);
			// 初始化指数分时
			klm.initIdxStockMinute(sorset, true);
			threadPool.shutdown();
		} catch (Exception e) {
			new Exception("执行k线相关任务异常" + DateTool.DateToStr(new Date(), DateTool.TIME_FORMAT)).printStackTrace();
			e.printStackTrace();
		}
	}

	/**
	 * 收盘更新分时
	 */
	public void synStockMins(){
		ExecutorService threadPool = Executors.newFixedThreadPool(10);
		try {
			for (Object obj : hash.hgetAll(Const.RKEY_STOCK_INFO_).keySet()) {
				String code = StrTool.toString(obj);
				threadPool.submit(new initStockMinute(code, sorset, true));
			}
			String allIdx = SystemConfig.getSysVal("marketIdx.code");
			String[] idxAry = allIdx.split(",");
			for (String idxCode : idxAry) {
				String simpleCode = StockUtils.getStockCode(idxCode);
				initStockMinute(simpleCode, sorset, true);
			}
		}catch (Exception e) {
			for (Object obj : hash.hgetAll(Const.RKEY_STOCK_INFO_).keySet()) {
				String code = StrTool.toString(obj);
				threadPool.submit(new initStockMinute(code, sorset, true));
			}
			String allIdx = SystemConfig.getSysVal("marketIdx.code");
			String[] idxAry = allIdx.split(",");
			for (String idxCode : idxAry) {
				String simpleCode = StockUtils.getStockCode(idxCode);
				initStockMinute(simpleCode, sorset, true);
			}
		}
		threadPool.shutdown();
	}
	
	/**
	 * 每天下午4点半跟新当天日k
	 * 
	 */
	public void synStockDays() {
		Date date = new Date();
		ExecutorService threadPool = Executors.newFixedThreadPool(10);
		try {
			// 跟新所有指数日K
			klm.initIndexDays(null, null, sorset, true);
			klm.initIdxStockMinute(sorset, true);
			for (Object obj : hash.hgetAll(Const.RKEY_STOCK_INFO_).keySet()) {
				// 跟新日K
				threadPool.submit(new initStockDays(StrTool.toString(obj), sorset, DateTool.DateToStr(date, "yyyyMMdd"), DateTool.DateToStr(date, "yyyyMMdd"), false));
				// 保证数据准确性，也重新同步一下分时
				threadPool.submit(new initStockMinute(StrTool.toString(obj), sorset, true));
			}
		} catch (Exception e) {
			// 如果异常再执行一次
			for (Object obj : hash.hgetAll(Const.RKEY_STOCK_INFO_).keySet()) {
				// 跟新当天日K
				threadPool.submit(new initStockDays(StrTool.toString(obj), sorset, DateTool.DateToStr(date, "yyyyMMdd"), DateTool.DateToStr(date, "yyyyMMdd"), false));
				// 保证数据准确性，也重新同步一下分时
				threadPool.submit(new initStockMinute(StrTool.toString(obj), sorset, true));
			}
			e.printStackTrace();
		} finally {
			threadPool.shutdown();
		}
	}

	/**
	 * 每天下午6点跟新当天周k和月k
	 * 
	 */
	public void synStockWeekAndMonth() {
		Date date = new Date();
		ExecutorService threadPool = Executors.newFixedThreadPool(10);
		try {
			for (Object obj : hash.hgetAll(Const.RKEY_STOCK_INFO_).keySet()) {
				// 跟新当天周k
				threadPool.submit(new initStockWeeks(StrTool.toString(obj), sorset, DateTool.DateToStr(date, "yyyyMMdd"), DateTool.DateToStr(date, "yyyyMMdd"), false));
				// 跟新当天月k
				threadPool.submit(new initStockMonths(StrTool.toString(obj), sorset, DateTool.DateToStr(date, "yyyyMMdd"), DateTool.DateToStr(date, "yyyyMMdd"), false));
			}
		} catch (Exception e) {
			// 如果异常再执行一次
			for (Object obj : hash.hgetAll(Const.RKEY_STOCK_INFO_).keySet()) {
				// 跟新当天周k
				threadPool.submit(new initStockWeeks(StrTool.toString(obj), sorset, DateTool.DateToStr(date, "yyyyMMdd"), DateTool.DateToStr(date, "yyyyMMdd"), false));
				// 跟新当天月k
				threadPool.submit(new initStockMonths(StrTool.toString(obj), sorset, DateTool.DateToStr(date, "yyyyMMdd"), DateTool.DateToStr(date, "yyyyMMdd"), false));
			}
			e.printStackTrace();
		} finally {
			threadPool.shutdown();
		}
	}

	/**
	 * 增量添加当前时间的分时
	 * 
	 * @param isNow 是否当前时间
	 * 
	 */
	public void addStockNowMinute(boolean isNow){
//		ExecutorService threadPool = Executors.newFixedThreadPool(10);
		ForkJoinPool threadPool = new ForkJoinPool();
		long a= System.currentTimeMillis();
		try {
			String min = DateTool.DateToStr(new Date(), "HH:mm");
			if(!isNow){
				min = DateTool.DateToStr(DateTool.minuteChange(new Date(), -1), "HH:mm");
			}
			Integer minint = DateTool.strDateToIntegerDate(DateTool.DateToStr(new Date(), "HH:mm"), "HH:mm", "HHmm") ;
			if(min.equals("15:01")){
				min = "15:00";
			}else if(min.equals("15:02")){
				min = "13:00";
			}else if(min.equals("11:31")){
				min = "11:30";
			}else if(minint >= 1503){
				min = "15:00";
			}
			threadPool.submit(new addIDXStockNowMinute(min));
			threadPool.submit(new addXSHEStockNowMinute(min));
			threadPool.submit(new addXSHGStockNowMinute(min));
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			threadPool.shutdown();
			while (true) {
				if(threadPool.isTerminated()){
					if(isNow){
						System.out.println(DateTool.DateToStr(new Date(), DateTool.TIME_FORMAT)+"每分钟分时---"+(System.currentTimeMillis() - a));
					}else{
						System.out.println(DateTool.DateToStr(new Date(), DateTool.TIME_FORMAT)+"添加上一分钟分时---"+(System.currentTimeMillis() - a));
					}
					break;
				}
			}
		}
	}

	/**
	 * 添加沪市股票当前时间的的分时图
	 *
	 * @param min
	 * void
	 * @throws Exception 
	 */
	public void addXSHGStockNowMinute(String min) throws Exception{
		long a = System.currentTimeMillis();
		try {
			// 增量添加沪市股票的分时图
			String url = Const.ONEMINUTE + "?assetClass=E&exchangeCD=XSHG&unit=1&time=" + min;
			CsvReader reader = WmcloudUtil.url2csv(url);
			if (reader != null) {
				reader.readRecord();
				while (reader.readRecord()) {
					String[] items = reader.getValues();
					String code = StockUtils.getLTCode(items[2]) + items[1];
					String barTime = items[6];
					String orderTime = DateTool.changeDateFormat(barTime, "HH:mm", "HHmm");
					sorset.zremrangeByScore(Const.RKEY_MINS_ + code, StrTool.toDouble(orderTime), StrTool.toDouble(orderTime));
					sorset.zadd(Const.RKEY_MINS_ + code, Double.parseDouble(orderTime), JSON.toJSONString(items));
					countDayByMin(code, items);
				}
				reader.close();
			}
		} catch (Exception e) {
			// 增量添加沪市股票的分时图（异常再来一次）
			String url = Const.ONEMINUTE + "?assetClass=E&exchangeCD=XSHG&unit=1&time=" + min;
			CsvReader reader = WmcloudUtil.url2csv(url);
			if (reader != null) {
				reader.readRecord();
				while (reader.readRecord()) {
					String[] items = reader.getValues();
					String code = StockUtils.getLTCode(items[2]) + items[1];
					String barTime = items[6];
					String orderTime = DateTool.changeDateFormat(barTime, "HH:mm", "HHmm");
					sorset.zremrangeByScore(Const.RKEY_MINS_ + code, StrTool.toDouble(orderTime), StrTool.toDouble(orderTime));
					sorset.zadd(Const.RKEY_MINS_ + code, Double.parseDouble(orderTime), JSON.toJSONString(items));
					countDayByMin(code, items);
				}
				reader.close();
			}
			System.err.println("增量添加沪市股票的分时图抛出异常" + DateTool.DateToStr(new Date(), DateTool.TIME_FORMAT));
			e.printStackTrace();
		}
	}
	
	/**
	 * 添加深市股票当前时间的分时图
	 *
	 * @param min
	 * @throws Exception
	 * void
	 */
	public void addXSHEStockNowMinute(String min) throws Exception{
		long a = System.currentTimeMillis();
			try {
				// 增量添加深市股票的分时图
				String url = Const.ONEMINUTE + "?assetClass=E&exchangeCD=XSHE&unit=1&time=" + min;
				CsvReader reader = WmcloudUtil.url2csv(url);
				if (reader != null) {
					reader.readRecord();
					while (reader.readRecord()) {
						String[] items = reader.getValues();
						String code = StockUtils.getLTCode(items[2]) + items[1];
						String barTime = items[6];
						String orderTime = DateTool.changeDateFormat(barTime, "HH:mm", "HHmm");
						sorset.zremrangeByScore(Const.RKEY_MINS_ + code, StrTool.toDouble(orderTime), StrTool.toDouble(orderTime));
						sorset.zadd(Const.RKEY_MINS_ + code, Double.parseDouble(orderTime), JSON.toJSONString(items));
						countDayByMin(code, items);
					}
					reader.close();
				}
			} catch (Exception e) {
				// 增量添加深市股票的分时图（异常再来一次）
				String url = Const.ONEMINUTE + "?assetClass=E&exchangeCD=XSHE&unit=1&time=" + min;
				CsvReader reader = WmcloudUtil.url2csv(url);
				if (reader != null) {
					reader.readRecord();
					while (reader.readRecord()) {
						String[] items = reader.getValues();
						String code = StockUtils.getLTCode(items[2]) + items[1];
						String barTime = items[6];
						String orderTime = DateTool.changeDateFormat(barTime, "HH:mm", "HHmm");
						sorset.zremrangeByScore(Const.RKEY_MINS_ + code, StrTool.toDouble(orderTime), StrTool.toDouble(orderTime));
						sorset.zadd(Const.RKEY_MINS_ + code, Double.parseDouble(orderTime), JSON.toJSONString(items));
						countDayByMin(code, items);
					}
					reader.close();
				}
				System.err.println("增量添加深市股票的分时图抛出异常" + DateTool.DateToStr(new Date(), DateTool.TIME_FORMAT));
				e.printStackTrace();
			}
	}
	
	/**
	 * 添加指数当前时间的分时图
	 *
	 * @param min
	 * @throws Exception
	 * void
	 */
	public void addIDXStockNowMinute(String min) throws Exception{
		long a = System.currentTimeMillis();
		try {
			// 增量添加指數的分时图
			String urlIdx = Const.ONEMINUTE + "?assetClass=IDX&exchangeCD=XSHE,XSHG&unit=1&time=" + min;
			CsvReader readerIdx = WmcloudUtil.url2csv(urlIdx);
			if (readerIdx != null) {
				readerIdx.readRecord();
				while (readerIdx.readRecord()) {
					String[] items = readerIdx.getValues();
					String code = StockUtils.getLTCode(items[2]) + items[1];
					if (StockUtils.getAllIdxSimpleCode().contains(code)) {
						String barTime = items[6];
						String orderTime = DateTool.changeDateFormat(barTime, "HH:mm", "HHmm");
						sorset.zremrangeByScore(Const.RKEY_MINS_ + code, StrTool.toDouble(orderTime), StrTool.toDouble(orderTime));
						sorset.zadd(Const.RKEY_MINS_ + code, Double.parseDouble(orderTime), JSON.toJSONString(items));
						countDayByMin(code, items);
					}
				}
				readerIdx.close();
			}
		} catch (Exception e) {
			// 增量添加指數的分时图(异常再来一次)
			String urlIdx = Const.ONEMINUTE + "?assetClass=IDX&exchangeCD=XSHG,XSHE&unit=1&time=" + min;
			CsvReader readerIdx = WmcloudUtil.url2csv(urlIdx);
			if (readerIdx != null) {
				readerIdx.readRecord();
				while (readerIdx.readRecord()) {
					String[] items = readerIdx.getValues();
					String code = StockUtils.getLTCode(items[2]) + items[1];
					if (StockUtils.getAllIdxSimpleCode().contains(code)) {
						String barTime = items[6];
						String orderTime = DateTool.changeDateFormat(barTime, "HH:mm", "HHmm");
						sorset.zremrangeByScore(Const.RKEY_MINS_ + code, StrTool.toDouble(orderTime), StrTool.toDouble(orderTime));
						sorset.zadd(Const.RKEY_MINS_ + code, Double.parseDouble(orderTime), JSON.toJSONString(items));
						countDayByMin(code, items);
					}
				}
				readerIdx.close();
			}
			System.err.println("增量添加指數的分时图抛出异常" + DateTool.DateToStr(new Date(), DateTool.TIME_FORMAT));
			e.printStackTrace();
		}
	}
	/**
	 * 通过分时计算日K
	 * 
	 * @param code
	 * @param items
	 */
	public void countDayByMin(String code, String[] items) {
		try {
			com.alibaba.fastjson.JSONArray priceAry = JSON.parseArray(hash.hget(Const.RKEY_LEVEL1_, code));
			if (priceAry != null && priceAry.size() > 0) {
				String[] dayItem = new String[10];
				dayItem[0] = items[1];// ticker
				dayItem[1] = items[7];// closePrice
				dayItem[2] = items[8];// openPrice
				dayItem[3] = items[9];// highestPrice
				dayItem[4] = items[10];// lowestPrice

				// 通过分时计算日k成交量和成交额
				Set<String> minSet = sorset.zrange(Const.RKEY_MINS_ + code, 0, 1);
				Iterator<String> minIter = minSet.iterator();
				double totalVolume = 0, totalValue = 0;
				while (minIter.hasNext()) {
					com.alibaba.fastjson.JSONArray minItem = JSON.parseArray(StrTool.toString(minIter.next()));
					totalVolume = +StrTool.toDouble(minItem.get(11));
					totalValue = +StrTool.toDouble(minItem.get(12));
				}

				dayItem[5] = StrTool.toString(totalVolume);// turnoverVol
				dayItem[6] = StrTool.toString(totalValue);// turnoverValue
				dayItem[7] = DateTool.DateToStr(new Date(), DateTool.DATE_FORMAT);// tradeDate

				// 从level1中获取昨日收盘价
				String preClosePrice = StrTool.toString(priceAry.get(Const.PRICE_prevClosePrice));
				dayItem[8] = preClosePrice;// preClosePrice
				dayItem[9] = "0";
//				dayItem[9] = StrTool.toDouble(items[7])-StrTool.toDouble(preClosePrice)/preClosePrice
				String orderTime = "0";
				try {
					orderTime = DateTool.changeDateFormat(dayItem[7], "yyyy-MM-dd", "yyyyMMdd");
				} catch (ParseException e) {
					e.printStackTrace();
				}
				sorset.zremrangeByScore(Const.RKEY_K_D_ + code, StrTool.toDouble(orderTime), StrTool.toDouble(orderTime));
				sorset.zadd(Const.RKEY_K_D_ + code, StrTool.toDouble(orderTime), JSON.toJSONString(dayItem));
			}
		} catch (Exception e) {
			System.err.println(code + "分时计算K线发生异常" + DateTool.DateToStr(new Date(), DateTool.TIME_FORMAT));
			e.printStackTrace();
		}
	}

	/**
	 * 初始化分时
	 * 
	 * @param sorset
	 * 
	 * @throws IOException
	 */
	public void initStockMinute(String simpleCode, SortSet sorset, boolean isDel) {
		long a = System.currentTimeMillis();
		try {
			if (simpleCode != null && simpleCode.length() > 2) {
				String code = simpleCode.substring(2);
				String shsz = StockUtils.getTLCode(simpleCode.substring(0, 2));
				String url = Const.BARRTINTRADAY + "?securityID=" + code + "." + shsz + "&startTime=&endTime=&unit=1";
				CsvReader reader = WmcloudUtil.url2csv(url);
				if (reader != null) {
					Map<Double, String> map = new HashMap<Double, String>();
					reader.readHeaders();
					while (reader.readRecord()) {
						String[] items = reader.getValues();
						String barTime = items[6];
						String orderTime = DateTool.changeDateFormat(barTime, "HH:mm", "HHmm");
						map.put(Double.parseDouble(orderTime), JSON.toJSONString(items));
					}
					long b = System.currentTimeMillis();
					if (map != null && !map.isEmpty()) {
						if (isDel) {
							sorset.zrem(Const.RKEY_MINS_ + simpleCode);
						}
						sorset.zadd(Const.RKEY_MINS_ + simpleCode, map);
						reader.close();
					}
				}
			}
		} catch (Exception e) {
			try {
				if (simpleCode != null && simpleCode.length() > 2) {
					String code = simpleCode.substring(2);
					String shsz = StockUtils.getTLCode(simpleCode.substring(0, 2));
					String url = Const.BARRTINTRADAY + "?securityID=" + code + "." + shsz + "&startTime=&endTime=&unit=1";
					CsvReader reader = WmcloudUtil.url2csv(url);
					if (reader != null) {
						Map<Double, String> map = new HashMap<Double, String>();
						reader.readHeaders();
						while (reader.readRecord()) {
							String[] items = reader.getValues();
							String barTime = items[6];
							String orderTime = DateTool.changeDateFormat(barTime, "HH:mm", "HHmm");
							map.put(Double.parseDouble(orderTime), JSON.toJSONString(items));
						}
						long b = System.currentTimeMillis();
						if (map != null && !map.isEmpty()) {
							if (isDel) {
								sorset.zrem(Const.RKEY_MINS_ + simpleCode);
							}
							sorset.zadd(Const.RKEY_MINS_ + simpleCode, map);
							reader.close();
						}
					}
				}
			} catch (Exception e1) {
				System.out.println("初始化分时异常2");
				e1.printStackTrace();
			}
			new Exception(simpleCode + "初始化分时异常" + (System.currentTimeMillis() - a)).printStackTrace();
			e.printStackTrace();
		}
	}

	/**
	 * 获取指数分时
	 * 
	 * @param sorset
	 * @throws Exception
	 */
	public void initIdxStockMinute(SortSet sorset, boolean isDel) throws Exception {
		String allIdx = SystemConfig.getSysVal("idx.code");
		String[] idxAry = allIdx.split(",");
		for (String idxCode : idxAry) {
			String simpleCode = StockUtils.getStockCode(idxCode);
			String url = Const.BARRTINTRADAY + "?securityID=" + idxCode + "&startTime=&endTime=&unit=1";
			CsvReader reader = WmcloudUtil.url2csv(url);
			Map<Double, String> map = new HashMap<Double, String>();
			if (reader != null) {
				reader.readRecord();
				while (reader.readRecord()) {
					String[] items = reader.getValues();
					String barTime = items[6];
					String orderTime = DateTool.changeDateFormat(barTime, "HH:mm", "HHmm");
					map.put(Double.parseDouble(orderTime), JSON.toJSONString(items));
				}
				if (map != null && !map.isEmpty()) {
					if (isDel) {
						sorset.zrem(Const.RKEY_MINS_ + simpleCode);
					}
					sorset.zadd(Const.RKEY_MINS_ + simpleCode, map);
					reader.close();
				}
			}
		}
	}

	/**
	 * 初始化日K数据
	 * 
	 * @param simpleCode
	 *            股票代码
	 * @param beginDate
	 *            开始时间(yyyyMMdd)
	 * @param endDate
	 *            结束时间(yyyyMMdd)
	 * @param sorset
	 * @throws IOException
	 */
	public void initStockDays(String simpleCode, String beginDate, String endDate, SortSet sorset, boolean isDel) throws Exception {
		Date date = new Date();
		beginDate = beginDate == null ? DateTool.DateToStr(DateTool.yearChange(date, -1), "yyyyMMdd") : beginDate;
		endDate = endDate == null ? DateTool.DateToStr(date, "yyyyMMdd") : endDate;
		String code = simpleCode.substring(2);
		String url = Const.MKTEQUD + "?ticker=" + code + "&beginDate=" + beginDate + "&endDate=" + endDate + "&field=ticker,closePrice,openPrice,highestPrice,lowestPrice,turnoverVol,turnoverValue,tradeDate,preClosePrice,chgPct";
		CsvReader reader = WmcloudUtil.url2csv(url);
		if (reader != null) {
			Map<Double, String> map = new HashMap<Double, String>();
			reader.readRecord();
			while (reader.readRecord()) {
				String[] items = reader.getValues();
				String day = items[7];
				String orderTime = DateTool.changeDateFormat(day, "yyyy-MM-dd", "yyyyMMdd");
				map.put(Double.parseDouble(orderTime), JSON.toJSONString(items));
			}
			if (map != null && !map.isEmpty()) {
				if (isDel) {
					sorset.zrem(Const.RKEY_K_D_ + simpleCode);
				}
				sorset.zadd(Const.RKEY_K_D_ + simpleCode, map);
				reader.close();
			}
		}
	}

	/**
	 * 初始化周K
	 * 
	 * @param simpleCode
	 *            股票代码
	 * @param beginDate
	 *            开始时间(yyyyMMdd)
	 * @param endDate
	 *            结束时间(yyyyMMdd)
	 * @param sorset
	 * @throws IOException
	 */
	public void initStockWeeks(String simpleCode, String beginDate, String endDate, SortSet sorset, boolean isDel) {
		try {
			Date date = new Date();
			beginDate = beginDate == null ? DateTool.DateToStr(DateTool.yearChange(date, -5), "yyyyMMdd") : beginDate;
			endDate = endDate == null ? DateTool.DateToStr(date, "yyyyMMdd") : endDate;
			String code = simpleCode.substring(2);
			String url = Const.MKTEQUWADJ + "?ticker=" + code + "&beginDate=" + beginDate + "&endDate=" + endDate + "&field=ticker,closePrice,openPrice,highestPrice,lowestPrice,turnoverVol,turnoverValue,endDate,preClosePrice,chgPct";
			CsvReader reader = WmcloudUtil.url2csv(url);
			if (reader != null) {
				Map<Double, String> map = new HashMap<Double, String>();
				reader.readRecord();
				while (reader.readRecord()) {
					String[] items = reader.getValues();
					String day = items[7];
					String orderTime = DateTool.changeDateFormat(day, "yyyy-MM-dd", "yyyyMMdd");
					map.put(Double.parseDouble(orderTime), JSON.toJSONString(items));
				}
				if (map != null && !map.isEmpty()) {
					if (isDel) {
						sorset.zrem(Const.RKEY_K_W_ + simpleCode);
					}
					sorset.zadd(Const.RKEY_K_W_ + simpleCode, map);
					reader.close();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(simpleCode);
		}
	}

	/**
	 * 初始化月K
	 * 
	 * @param simpleCode
	 *            股票代码
	 * @param beginDate
	 *            开始时间(yyyyMMdd)
	 * @param endDate
	 *            结束时间(yyyyMMdd)
	 * @param sorset
	 * @throws IOException
	 */
	public void initStockMonths(String simpleCode, String beginDate, String endDate, SortSet sorset, boolean isDel) throws Exception {
		Date date = new Date();
		beginDate = beginDate == null ? DateTool.DateToStr(DateTool.yearChange(date, -20), "yyyyMMdd") : beginDate;
		endDate = endDate == null ? DateTool.DateToStr(date, "yyyyMMdd") : endDate;
		String code = simpleCode.substring(2);
		String shsz = StockUtils.getTLCode(simpleCode.substring(0, 2));
		String url = Const.MKTEQUM + "?ticker=" + code + "&beginDate=" + beginDate + "&endDate=" + endDate + "&field=ticker,closePrice,openPrice,highestPrice,lowestPrice,turnoverVol,turnoverValue,endDate,preClosePrice,chgPct";
		CsvReader reader = WmcloudUtil.url2csv(url);
		if (reader != null) {
			Map<Double, String> map = new HashMap<Double, String>();
			reader.readRecord();
			while (reader.readRecord()) {
				String[] items = reader.getValues();
				String day = items[7];
				String orderTime = DateTool.changeDateFormat(day, "yyyy-MM-dd", "yyyyMMdd");
				map.put(Double.parseDouble(orderTime), JSON.toJSONString(items));
			}
			if (map != null && !map.isEmpty()) {
				if (isDel) {
					sorset.zrem(Const.RKEY_K_M_ + simpleCode);
				}
				sorset.zadd(Const.RKEY_K_M_ + simpleCode, map);
				reader.close();
			}
		}
	}

	/**
	 * 指数日K
	 * 
	 * @param beginDate
	 *            开始时间(yyyyMMdd)
	 * @param endDate
	 *            结束时间(yyyyMMdd)
	 * @param sorset
	 * @throws Exception
	 */
	public void initIndexDays(String beginDate, String endDate, SortSet sorset, boolean isDel) throws Exception {
		String allIdx = SystemConfig.getSysVal("idx.code");
		String[] idxAry = allIdx.split(",");
		Date date = new Date();
		beginDate = beginDate == null ? DateTool.DateToStr(DateTool.yearChange(date, -20), "yyyyMMdd") : beginDate;
		endDate = endDate == null ? DateTool.DateToStr(date, "yyyyMMdd") : endDate;
		for (String idxCode : idxAry) {
			String simpleCode = StockUtils.getStockCode(idxCode);
			String code = simpleCode.substring(2);
			String url = Const.MKTIDXD + "?beginDate=" + beginDate + "&endDate=" + endDate + "&ticker=" + code + "&field=ticker,openIndex,closeIndex,highestIndex,lowestIndex,turnoverVol,turnoverValue,tradeDate,preCloseIndex,CHG,CHGPct";
			CsvReader reader = WmcloudUtil.url2csv(url);
			if (reader != null) {
				Map<Double, String> map = new HashMap<Double, String>();
				reader.readRecord();
				while (reader.readRecord()) {
					String[] items = reader.getValues();
					String day = items[7];
					String orderTime = DateTool.changeDateFormat(day, "yyyy-MM-dd", "yyyyMMdd");
					map.put(Double.parseDouble(orderTime), JSON.toJSONString(items));
				}
				if (map != null && !map.isEmpty()) {
					if (isDel) {
						sorset.zrem(Const.RKEY_K_D_ + simpleCode);
					}
					sorset.zadd(Const.RKEY_K_D_ + simpleCode, map);
					reader.close();
				}
			}
		}
	}

	public static void main(String[] args) {
		long a = System.currentTimeMillis();
		try {
//			klm.addStockNowMinute();
			
//			Integer min = DateTool.strDateToIntegerDate(DateTool.DateToStr(new Date(), "HH:mm"), "HH:mm", "HHmm") ;
//			System.out.println(min);
			// klm.getStockMinute("sz300240");
			// initStockMinute
			klm.addStockNowMinute(false);
			// klm.initStockMinute("sz000990");
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("count time:" + (System.currentTimeMillis() - a));
	}
}

/**
 * 初始化分时图线程
 * 
 * @author qy
 * @date 2016年6月2日
 */
class initStockMinute implements Runnable {
	private String code;
	private KlineManager klm;
	private SortSet sorset;
	private boolean isDel;

	public initStockMinute(String code, SortSet sorset, boolean isDel) {
		this.klm = KlineManager.getInstance();
		this.code = code;
		this.sorset = sorset;
		this.isDel = isDel;
	}

	@Override
	public void run() {
		try {
			klm.initStockMinute(code, sorset, isDel);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

/**
 * 初始化日k线程
 * 
 * @author qy
 * @date 2016年6月2日
 */
class initStockDays implements Runnable {
	private String code;
	private KlineManager klm;
	private SortSet sorset;
	private String beginDate;
	private String endDate;
	private boolean isDel;

	public initStockDays(String code, SortSet sorset, String beginDate, String endDate, boolean isDel) {
		this.klm = KlineManager.getInstance();
		this.code = code;
		this.sorset = sorset;
		this.beginDate = beginDate;
		this.endDate = endDate;
		this.isDel = isDel;
	}

	@Override
	public void run() {
		try {
			klm.initStockDays(code, beginDate, endDate, sorset, isDel);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

/**
 * 初始化周k线程
 * 
 * @author qy
 * @date 2016年6月2日
 */
class initStockWeeks implements Runnable {
	private String code;
	private KlineManager klm;
	private SortSet sorset;
	private String beginDate;
	private String endDate;
	private boolean isDel;

	public initStockWeeks(String code, SortSet sorset, String beginDate, String endDate, boolean isDel) {
		this.klm = KlineManager.getInstance();
		this.code = code;
		this.sorset = sorset;
		this.beginDate = beginDate;
		this.endDate = endDate;
		this.isDel = isDel;
	}

	@Override
	public void run() {
		try {
			klm.initStockWeeks(code, beginDate, endDate, sorset, isDel);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}

/**
 * 初始化月k线程
 * 
 * @author qy
 * @date 2016年6月2日
 */
class initStockMonths implements Runnable {
	private String code;
	private KlineManager klm;
	private SortSet sorset;
	private String beginDate;
	private String endDate;
	private boolean isDel;

	public initStockMonths(String code, SortSet sorset, String beginDate, String endDate, boolean isDel) {
		this.klm = KlineManager.getInstance();
		this.code = code;
		this.sorset = sorset;
		this.beginDate = beginDate;
		this.endDate = endDate;
		this.isDel = isDel;
	}

	@Override
	public void run() {
		try {
			klm.initStockMonths(code, beginDate, endDate, sorset, isDel);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

/**
 * 添加股票指数每分钟分时
 *
 * @author qy
 * @date 2016年12月30日
 */
class addIDXStockNowMinute implements Runnable{
	private String min;
	public addIDXStockNowMinute(String min){
		this.min = min;
	}
	@Override
	public void run() {
		try {
			KlineManager.getInstance().addIDXStockNowMinute(min);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

/**
 * 添加股票指数每分钟分时
 *
 * @author qy
 * @date 2016年12月30日
 */
class addXSHGStockNowMinute implements Runnable{
	private String min;
	public addXSHGStockNowMinute(String min){
		this.min = min;
	}
	@Override
	public void run() {
		try {
			KlineManager.getInstance().addXSHGStockNowMinute(min);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

/**
 * 添加深市股票当前时间的分时图
 *
 * @author qy
 * @date 2016年12月30日
 */
class addXSHEStockNowMinute implements Runnable{
	private String min;
	public addXSHEStockNowMinute(String min){
		this.min = min;
	}
	@Override
	public void run() {
		try {
			KlineManager.getInstance().addXSHEStockNowMinute(min);;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}