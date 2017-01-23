package com.shendeng.quartz;

import java.io.IOException;
import java.util.Date;

import org.quartz.Job;
import org.quartz.JobExecutionContext;

import com.shendeng.datamanager.KlineManager;
import com.shendeng.datamanager.Level1Manager;
import com.shendeng.datamanager.StockInfoManager;
import com.shendeng.utils.Const;
import com.shendeng.utils.DateTool;

/**
 * 股票相关的定时任务
 * 
 * @author qy
 * @date 2016年5月6日
 */
public class RefreshLevel1Detail implements Job {

	@Override
	public void execute(JobExecutionContext arg0) {
		try {
			if (Const.isTradingDay) {
				Level1Manager.getInstance().getLevel1Detail(false);
			}
		} catch (Exception e) {
			try {//失败再来一次
				Level1Manager.getInstance().getLevel1Detail(false);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
			new Exception("每日下午跟新，日五档明细数据异常" + DateTool.DateToStr(new Date(), DateTool.TIME_FORMAT)).printStackTrace();
		}
	}

}
