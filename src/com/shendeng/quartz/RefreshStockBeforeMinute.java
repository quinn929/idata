package com.shendeng.quartz;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.quartz.Job;
import org.quartz.JobExecutionContext;

import com.shendeng.cluster.ClusterUtil;
import com.shendeng.datamanager.IndustryManager;
import com.shendeng.datamanager.KlineManager;
import com.shendeng.datamanager.StockInfoManager;
import com.shendeng.utils.Const;
import com.shendeng.utils.DateTool;
import com.shendeng.utils.RedisUtil;

/**
 * 增量添加分时定时任务
 * 
 * @author qy
 * @date 2016年5月6日
 */
public class RefreshStockBeforeMinute implements Job {

	@Override
	public void execute(JobExecutionContext arg0) {
		try {
			if (Const.isTradingDay && Const.isTradingTime) {
				// 增量添加当前时间的分时
				if ((new Date().compareTo(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(DateTool.DateToStr(new Date(), "yyyy-MM-dd") + " 09:30:00")) > 0) && (new Date().compareTo(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(DateTool.DateToStr(new Date(), "yyyy-MM-dd") + " 15:10:00")) < 0)) {
					KlineManager.getInstance().addStockNowMinute(false);
				}
			}
		} catch (Exception e) {
			// 失败再来
			KlineManager.getInstance().addStockNowMinute(false	);
			System.err.println("增量添加分时定时任务执行失败" + DateTool.DateToStr(new Date(), DateTool.TIME_FORMAT));
			e.printStackTrace();
		}
	}
}
