package com.shendeng.quartz;

import java.util.Date;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.shendeng.utils.Const;
import com.shendeng.utils.DateTool;
import com.shendeng.utils.JedisUtil.Hash;
import com.shendeng.utils.JedisUtil.Keys;
import com.shendeng.utils.RedisUtil;

/**
 * 股票状态调度(是否交易日)
 *
 * @author qy
 * @date 2016年5月23日
 */
public class CheckRunStatus implements Job{

	Keys keys = RedisUtil.getJedisUtil().KEYS;
	
	Hash hash = RedisUtil.getJedisUtil().HASH;
	
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		try {
				long stockSize = hash.hlen(Const.RKEY_STOCK_INFO_);
				long minKeySize = keys.keys(Const.RKEY_MINS_+"*").size();
				long dayKeySize = keys.keys(Const.RKEY_K_D_+"*").size();
				if(minKeySize >= stockSize && dayKeySize >= stockSize){
				QuartzManager.removeJob("JobRunStatus");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
