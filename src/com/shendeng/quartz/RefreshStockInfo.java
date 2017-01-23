package com.shendeng.quartz;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.quartz.Job;
import org.quartz.JobExecutionContext;

import com.shendeng.datamanager.IndustryManager;
import com.shendeng.datamanager.StockInfoManager;
import com.shendeng.utils.Const;
import com.shendeng.utils.DateTool;
import com.shendeng.utils.JedisUtil;
import com.shendeng.utils.JedisUtil.Keys;
import com.shendeng.utils.JedisUtil.SortSet;
import com.shendeng.utils.RedisUtil;
import com.shendeng.web.QuotationAct;

/**
 * 股票相关的定时任务
 *
 * @author qy
 * @date 2016年5月6日
 */
public class RefreshStockInfo implements Job{

	Keys keys = RedisUtil.getJedisUtil().KEYS;
	
	@Override
	public void execute(JobExecutionContext arg0){
		ExecutorService threadPool = Executors.newFixedThreadPool(5);
		try{
			if(Const.isTradingDay){
				//清空股票分时图
//				keys.delPattern(Const.RKEY_MINS_+"*");
				// 跟新股票基本信息
				if(StockInfoManager.getInstance().getEqu()){
					threadPool.submit(new initStockInfo());
					threadPool.submit(new initIndustry());
//					QuotationAct.setSearch();
					System.out.println("股票相关的定时任务执行完成"+DateTool.DateToStr(new Date(), DateTool.TIME_FORMAT));
				}
			}
		}catch(Exception e){
			//失败再来一次
			//清空股票分时图
//			keys.delPattern(Const.RKEY_MINS_+"*");
			// 跟新股票基本信息
			if(StockInfoManager.getInstance().getEqu()){
				threadPool.submit(new initStockInfo());
				threadPool.submit(new initIndustry());
				QuotationAct.setSearch();
				System.out.println("股票相关的定时任务执行完成"+DateTool.DateToStr(new Date(), DateTool.TIME_FORMAT));
			}
			e.printStackTrace();
			new Exception("股票相关的定时任务执行失败"+DateTool.DateToStr(new Date(), DateTool.TIME_FORMAT)).printStackTrace();
		}finally{
			threadPool.shutdown();
		}
	}
}
/**
 * 初始化行业相关数据
 * 
 * @author qy
 * @date 2016年5月26日
 */
class initIndustry implements Runnable {
	@Override
	public void run() {
		try {
			Thread.sleep(5000);
			// 初始化股票与行业关系
			IndustryManager.getInstance().initIndustry();
			Thread.sleep(7000);
			// 证券板块数据
			IndustryManager.getInstance().initSectypeRel();
			Thread.sleep(9000);
			// 处理行业排名
			IndustryManager.getInstance().processIndustrySort();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

/**
 * 初始化股票基本信息
 * 
 * @author qy
 * @date 2016年5月26日
 */
class initStockInfo implements Runnable {
	@Override
	public void run() {
		try {
			// 跟新股票停复盘信息
			StockInfoManager.getInstance().getSecTips();
			// 合并利润表(最近)
			StockInfoManager.getInstance().getFdmtislately();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}