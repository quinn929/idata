package com.shendeng.quartz;

import java.io.IOException;
import java.util.Date;

import org.quartz.Job;
import org.quartz.JobExecutionContext;

import com.shendeng.datamanager.KlineManager;
import com.shendeng.datamanager.StockInfoManager;
import com.shendeng.utils.Const;
import com.shendeng.utils.DateTool;

/**
 * 股票相关的定时任务
 * 
 * @author qy
 * @date 2016年5月6日
 */
public class RefreshStockOther implements Job {

	@Override
	public void execute(JobExecutionContext arg0) {
		try {
			if (Const.isTradingDay) {
				StockInfoManager.getInstance().getFdmtislately();
				KlineManager.getInstance().synStockDays();
			}
		} catch (IOException e) {
			try {//失败再来一次
				StockInfoManager.getInstance().getFdmtislately();
				KlineManager.getInstance().synStockDays();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
			new Exception("每日下午跟新，日k和合并利润表(最近)异常" + DateTool.DateToStr(new Date(), DateTool.TIME_FORMAT)).printStackTrace();
		}
	}

}
