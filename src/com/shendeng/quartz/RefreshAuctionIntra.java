package com.shendeng.quartz;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.quartz.Job;
import org.quartz.JobExecutionContext;

import com.shendeng.datamanager.IndustryManager;
import com.shendeng.datamanager.KlineManager;
import com.shendeng.datamanager.Level1Manager;
import com.shendeng.utils.Const;
import com.shendeng.utils.DateTool;
import com.shendeng.utils.RedisUtil;

/**
 * 集合竞价相关定时任务
 * 
 * @author qy
 */
public class RefreshAuctionIntra implements Job {

	public void execute(JobExecutionContext arg0) {
		try {
			if (Const.isTradingDay) {
//				if (true) {
				if ((new Date().compareTo(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(DateTool.DateToStr(new Date(), "yyyy-MM-dd") + " 09:15:00")) >= 0) && (new Date().compareTo(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(DateTool.DateToStr(new Date(), "yyyy-MM-dd") + " 09:25:00")) <= 0)) {
					//清空股票分时图
					RedisUtil.getJedisUtil().KEYS.delPattern(Const.RKEY_MINS_+"*");
					RedisUtil.getJedisUtil().KEYS.del(Const.RKEY_LEVEL1_DETAIL_);
					// 跟新集合竞价数据
					Level1Manager.getInstance().getAuctionIntra();
//					threadPool.submit(new getAuctionIntra());
				}
			}
		} catch (Exception e) {
			//失败再来一次
			try {
				if ((new Date().compareTo(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(DateTool.DateToStr(new Date(), "yyyy-MM-dd") + " 09:15:00")) >= 0) && (new Date().compareTo(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(DateTool.DateToStr(new Date(), "yyyy-MM-dd") + " 09:25:00")) <= 0)) {
					//清空股票分时图
					RedisUtil.getJedisUtil().KEYS.delPattern(Const.RKEY_MINS_+"*");
					RedisUtil.getJedisUtil().KEYS.del(Const.RKEY_LEVEL1_DETAIL_);
					// 跟新集合竞价数据
					Level1Manager.getInstance().getAuctionIntra();
//					threadPool.submit(new getAuctionIntra());
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
			new Exception("定时任务执行失败 集合竞价RefreshAuctionIntra" + DateTool.DateToStr(new Date(), DateTool.TIME_FORMAT)).printStackTrace();
		}
	}

}

class getAuctionIntra implements Runnable {
	@Override
	public void run() {
		try {
			Level1Manager.getInstance().getAuctionIntra();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}