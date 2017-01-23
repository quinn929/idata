package com.shendeng.quartz;

import java.io.IOException;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.shendeng.utils.Const;
import com.shendeng.utils.StockUtils;

/**
 * 股票状态调度(是否交易日)
 *
 * @author qy
 * @date 2016年5月23日
 */
public class RefreshStockState implements Job{

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		try {
			//是否交易日
			Const.isTradingDay = StockUtils.isTradingDay();
			Const.isTradingTime = StockUtils.isTradingTime();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
