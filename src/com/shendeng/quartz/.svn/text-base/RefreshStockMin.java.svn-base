package com.shendeng.quartz;


import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.shendeng.datamanager.KlineManager;
import com.shendeng.utils.Const;

public class RefreshStockMin implements Job {

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		try {
			if (Const.isTradingDay) {
				KlineManager.getInstance().synStockMins();
			}
		} catch (Exception e) {
			KlineManager.getInstance().synStockMins();
		}
	}

}
