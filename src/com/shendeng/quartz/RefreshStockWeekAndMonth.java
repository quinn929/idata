package com.shendeng.quartz;

import java.util.Date;

import org.quartz.Job;
import org.quartz.JobExecutionContext;

import com.shendeng.datamanager.KlineManager;
import com.shendeng.utils.Const;
import com.shendeng.utils.DateTool;

/**
 * 股票相关的定时任务
 *
 * @author qy
 * @date 2016年5月6日
 */
public class RefreshStockWeekAndMonth implements Job{

	@Override
	public void execute(JobExecutionContext arg0){
		try{
			if(Const.isTradingDay){
				KlineManager.getInstance().synStockWeekAndMonth();
			}
		}catch(Exception e){
			//失败再来一次
			KlineManager.getInstance().synStockWeekAndMonth();
			e.printStackTrace();
			new Exception("定时任务执行失败 RefreshStockWeekAndMonth"+DateTool.DateToStr(new Date(), DateTool.TIME_FORMAT)).printStackTrace();
		}
	}

}
