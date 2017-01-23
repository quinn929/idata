package com.shendeng.quartz;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.shendeng.datamanager.Level1Manager;
import com.shendeng.utils.Const;

public class RefreshLevel1PrevClosePrice implements Job {

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		try {
			if (Const.isTradingDay) {
				Level1Manager.getInstance().setLastPrice2PrevClosePrice();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}
