package com.shendeng.datamanager;

import io.netty.util.internal.chmv8.ForkJoinPool;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang.StringUtils;
import org.quartz.spi.ThreadPool;

import com.alibaba.fastjson.JSON;
import com.csvreader.CsvReader;
import com.shendeng.utils.Const;
import com.shendeng.utils.DateTool;
import com.shendeng.utils.JedisUtil.Hash;
import com.shendeng.utils.JedisUtil.Keys;
import com.shendeng.utils.JedisUtil.SortSet;
import com.shendeng.utils.RedisUtil;
import com.shendeng.utils.StockUtils;
import com.shendeng.utils.StrTool;
import com.shendeng.utils.SystemConfig;
import com.shendeng.utils.WmcloudUtil;
/**
 * 获取快照数据
 * @see 以线程组的方式，单次执行2秒内
 * @author naxj
 * @date 2016-12-15
 */
public class Level1ManagerV2 {
	
	private static Level1ManagerV2 self = new Level1ManagerV2();

	public static Level1ManagerV2 getInstance() {
		return self;
	}
	
	private Hash hash = RedisUtil.getJedisUtil().HASH;
	private SortSet sorset = RedisUtil.getJedisUtil().SORTSET;
	private Keys keys = RedisUtil.getJedisUtil().KEYS;
	// 内装所有排序的大包
	Map<String, IdentityHashMap<Double, String>> rank = new HashMap<String, IdentityHashMap<Double, String>>();
	// 涨跌幅额排序
	IdentityHashMap<Double, String> stortChange = new IdentityHashMap<Double, String>();
//	Multimap<Double, String> stortChange =   ArrayListMultimap.create();
	// 涨跌幅排序
	IdentityHashMap<Double, String> stortChangePct = new IdentityHashMap<Double, String>();
	// 振幅排序
	IdentityHashMap<Double, String> stortSwing = new IdentityHashMap<Double, String>();
	// 成交量榜
	IdentityHashMap<Double, String> stortVolumnup = new IdentityHashMap<Double, String>();
	// 成交额榜
	IdentityHashMap<Double, String> stortVolumnpriceup = new IdentityHashMap<Double, String>();
	// 换手率榜
	IdentityHashMap<Double, String> stortTurnoverrateup = new IdentityHashMap<Double, String>();
	


	public void refreshData(){
		try {
			long a = System.currentTimeMillis();
			getStockIdx();
			Map<String, IdentityHashMap<Double, String>> rank = getAllTickRT();
			if (rank != null && !rank.isEmpty()) {
				if (rank.get("stortChange") != null && !rank.get("stortChange").isEmpty()) {
					sorset.zrem(Const.RKEY_LEVEL1_SORT_change);
					System.out.println("-----stortChange------"+rank.get("stortChange").size());
					sorset.zadd(Const.RKEY_LEVEL1_SORT_change,
							rank.get("stortChange"));
					stortChange.clear();
				}
				if (rank.get("stortChangePct") != null && !rank.get("stortChangePct").isEmpty()) {
					sorset.zrem(Const.RKEY_LEVEL1_SORT_changePct);
					System.out.println("-----stortChangePct------"+rank.get("stortChangePct").size());
					sorset.zadd(Const.RKEY_LEVEL1_SORT_changePct,
							rank.get("stortChangePct"));
					stortChangePct.clear();
				}
				if (rank.get("stortSwing") != null && !rank.get("stortSwing").isEmpty()) {
					sorset.zrem(Const.RKEY_LEVEL1_SORT_swing);
					System.out.println("-----stortSwing------"+rank.get("stortSwing").size());
					sorset.zadd(Const.RKEY_LEVEL1_SORT_swing,
							rank.get("stortSwing"));
					stortSwing.clear();
				}
				if (rank.get("stortVolumnup") != null && !rank.get("stortVolumnup").isEmpty()) {
					sorset.zrem(Const.RKEY_LEVEL1_SORT_volumnup);
					System.out.println("stortVolumnup----"+rank.get("stortVolumnup").size());
					sorset.zadd(Const.RKEY_LEVEL1_SORT_volumnup,
							rank.get("stortVolumnup"));
					stortVolumnup.clear();
				}
				if (rank.get("stortVolumnpriceup") != null && !rank.get("stortVolumnpriceup").isEmpty()) {
					sorset.zrem(Const.RKEY_LEVEL1_SORT_volumnpriceup);		
					System.out.println("stortVolumnpriceup--------***********--"+rank.get("stortVolumnpriceup").size());
					sorset.zadd(Const.RKEY_LEVEL1_SORT_volumnpriceup,
							rank.get("stortVolumnpriceup"));
					stortVolumnpriceup.clear();
				}
				if (rank.get("stortTurnoverrateup") != null && !rank.get("stortTurnoverrateup").isEmpty()) {
					sorset.zrem(Const.RKEY_LEVEL1_SORT_turnoverrateup);
					sorset.zadd(Const.RKEY_LEVEL1_SORT_turnoverrateup,
							rank.get("stortTurnoverrateup"));
					stortTurnoverrateup.clear();
				}
			}
			System.out.println("股票数据快照执行时间----------"+(System.currentTimeMillis()-a)/1000);
		} catch (Exception e) {
			rank.clear();
			stortChange.clear();
			stortChangePct.clear();
			stortSwing.clear();
			stortVolumnup.clear();
			stortVolumnpriceup.clear();
			stortTurnoverrateup.clear();
			new Exception("下载股票数据快照"
					+ DateTool.DateToStr(new Date(), DateTool.TIME_FORMAT))
					.printStackTrace();
			e.printStackTrace();
		}
	}
	/**
	 * 获取指数快照
	 * 
	 * @param priceMap
	 *            行情快照redis Key
	 * @throws IOException
	 */
	public void getStockIdx() throws IOException {
		String allIdx = SystemConfig.getSysVal("idx.code");
		CsvReader readerIdx = WmcloudUtil
				.url2csv(Const.UW_LEVEL1_CSV + "?securityID=" + allIdx + "&assetClass=IDX&exchangeCD=");
		if (readerIdx != null) {
			readerIdx.readRecord();
			while (readerIdx.readRecord()) {
				String[] items = readerIdx.getValues();
				String simpleCode = StockUtils.getLTCode(items[0]) + items[1];
				items = StockUtils.arrayAddLength(items, 3);
				items[Const.PRICE_assetClass] = "IDX";
				items[Const.PRICE_industryID1] = "";
				items[Const.PRICE_industryName1] = "";
				hash.hset(Const.RKEY_LEVEL1_, simpleCode, JSON.toJSONString(items));
			}
			readerIdx.close();
		}
	}
	/**
	 * 获取所有A股股票快照
	 * @return
	 */
	public Map<String, IdentityHashMap<Double, String>> getAllTickRT() {
		long begin = System.currentTimeMillis();
		//将所有A股股票编码 按每组300进行分组
		List<List<String>> codes = Cache.getStockCodeSplit(100);
		//线程组
//		ThreadGroup threadGroup = new ThreadGroup("SubSnapshotTask");
		ForkJoinPool fjp = new ForkJoinPool(10);
		ExecutorService threadPool = Executors.newFixedThreadPool(10);
		int i=0;
		
		stortChange.clear();
		stortChangePct.clear();
		stortSwing.clear();
		stortVolumnup.clear();
		stortVolumnpriceup.clear();
		stortTurnoverrateup.clear();
		//将所有股票编码分成几组，并使用子线程去处理数据
		for(List<String> subCodes:codes){
//			String threadName = "SubSnapshotTask-"+(++i)+"-"+System.currentTimeMillis();
//			SubSnapshotTask subSnapshotTask = new SubSnapshotTask(subCodes);
//			Thread thred = new Thread(threadGroup, subSnapshotTask,threadName);
//            thred.start();
			fjp.submit(new SubSnapshotTask(subCodes));
//            threadPool.submit(new SubSnapshotTask(subCodes));
		}
		//等待所有子线程完成
//		while(threadGroup.activeCount()>0){
////			System.out.println(threadGroup.activeCount());
//			//如果20秒还没有执行完所有线程
//			if ((System.currentTimeMillis()-begin)>(1000*30)){
//				System.out.println(DateTool.DateToStr(new Date(), "yyyy-MM-dd HH:mm:ss:S----")+"关闭所有执行A股股票快照的线程");
//				//尝试结束所有子线程
//				threadGroup.interrupt();
//			}
//		}
//		threadGroup.interrupt();
//		threadGroup.destroy();
		
//		threadPool.shutdown();
		fjp.shutdown();
		while (true) {
			if(fjp.isTerminated()){
				System.out.println("getAllTickRT用时----"+(System.currentTimeMillis() - begin));
				rank.clear();
				System.out.println("getAllTickRT------stortChange-------------------"+stortChange.size());
				rank.put("stortChange", stortChange);
				rank.put("stortChangePct", stortChangePct);
				rank.put("stortSwing", stortSwing);
				rank.put("stortVolumnup", stortVolumnup);
				rank.put("stortVolumnpriceup", stortVolumnpriceup);
				rank.put("stortTurnoverrateup", stortTurnoverrateup);
				break;
			}
		}
		
		return this.rank;
	}
	/**
	 * 获取指定股票的快照
	 * @param codes
	 * @throws Exception 
	 */
	public void getTickRT(List<String> codes) throws Exception {
		long begin1 = System.currentTimeMillis();
		String codeStr = StringUtils.join(codes, ",");
		//从通联获取数据
		CsvReader readerHs = WmcloudUtil.url2csv(Const.UW_LEVEL1_CSV + "?ticker=" + codeStr);
		if (readerHs != null) {
			try {
				readerHs.readRecord();
				while (readerHs.readRecord()) {
					String[] items = readerHs.getValues();
					//处理单只股票
					processOneStock(items);
				}
			} catch (Exception ex) {
				System.err.println(ex);
				throw ex;
			} finally {
				//关闭流
				readerHs.close();
			}
		}
//		System.out.println("securityID=&assetClass=E&exchangeCD=XSHE:" + (System.currentTimeMillis() - begin1));
	}
	/**
	 * 处理单只股票 csv转map
	 * 
	 * @param items
	 */
	private void processOneStock(String[] items) {
		String simpleCode = StockUtils.getLTCode(items[0]) + items[1];
		items = StockUtils.arrayAddLength(items, 3);
		items[Const.PRICE_assetClass] = "";//XSHE
		items[Const.PRICE_industryID1] = "";
		items[Const.PRICE_industryName1] = "";
		// 保存全部信息
		hash.hset(Const.RKEY_LEVEL1_, simpleCode, JSON.toJSONString(items));
		// 保存涨跌幅额排序
		String[] sortChangeItem = new String[] { simpleCode, items[Const.PRICE_lastPrice],
				items[Const.PRICE_change] };
		String change = StrTool.zero2decimal(items[Const.PRICE_change]);
		stortChange.put(new Double(change), JSON.toJSONString(sortChangeItem));
		// 保存涨跌幅排序
		String[] sortChangePctItem = new String[] { simpleCode, items[Const.PRICE_lastPrice],
				items[Const.PRICE_changePct] };
		String changePct = StrTool.zero2decimal(items[Const.PRICE_changePct]);
		stortChangePct.put(new Double(changePct), JSON.toJSONString(sortChangePctItem));
		// 保存振幅排序
		String[] sortAmplitudeItem = new String[] { simpleCode, items[Const.PRICE_lastPrice],
				items[Const.PRICE_amplitude] };
		String amplitude = StrTool.zero2decimal(items[Const.PRICE_amplitude]);
		stortSwing.put(new Double(amplitude), JSON.toJSONString(sortAmplitudeItem));

		// 成交量榜
		String[] sortVolumeItem = new String[] { simpleCode, items[Const.PRICE_lastPrice],
				items[Const.PRICE_volume] };
		String volume = StrTool.zero2decimal(items[Const.PRICE_volume]);
		stortVolumnup.put(new Double(volume), JSON.toJSONString(sortVolumeItem));

		// 成交额榜
		String[] sortValueItem = new String[] { simpleCode, items[Const.PRICE_lastPrice],
				items[Const.PRICE_value] };
		String value = StrTool.zero2decimal(items[Const.PRICE_value]);
		stortVolumnpriceup.put(new Double(value), JSON.toJSONString(sortValueItem));

		// 换手率榜
		String[] sortTurnoverRateItem = new String[] { simpleCode, items[Const.PRICE_lastPrice],
				items[Const.PRICE_turnoverRate] };
		String turnoverRate = StrTool.zero2decimal(items[Const.PRICE_turnoverRate]);
		stortTurnoverrateup.put(new Double(turnoverRate), JSON.toJSONString(sortTurnoverRateItem));
	}

	public static void main(String[] args) {
		IdentityHashMap<String, String>  test = new IdentityHashMap<String, String>();
		test.put(new String("a111"), "2222");
		test.put(new String("B112"), "121212");
		test.put(new String("C111"), "SDF");
		test.put(new String("B112"), "SADF");
		test.put(new String("a111"), "DFGSDG");
		test.put(new String("C111"), "ASDFASF");
		System.out.println(test);
//		if(Cache.getStockCodes() == null || Cache.getStockCodes().isEmpty()){
//			if(StockInfoManager.getInstance().getEqu()){
//				Level1ManagerV2 lm2 = new Level1ManagerV2();
//				lm2.refreshData();
//			}
//		}
	}
	/**
	 * 获取并处理快照信息的子线程
	 * @author naxj
	 *
	 */
	class SubSnapshotTask implements Runnable {
		//子线程处理的投票
		private List<String> codes;
		public SubSnapshotTask(List<String> codes){
			this.codes = codes;
		}
		@Override
		public void run() {
			try {
//				System.out.println(Thread.currentThread().getName()+":run");
				long begin = System.currentTimeMillis();
				getTickRT(this.codes);
//				System.out.println(Thread.currentThread().getName()+":"+(System.currentTimeMillis()-begin));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}  

	}
}

