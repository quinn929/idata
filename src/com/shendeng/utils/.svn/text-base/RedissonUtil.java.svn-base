package com.shendeng.utils;


import org.apache.commons.lang.StringUtils;
import org.redisson.Config;
import org.redisson.Redisson;
import org.redisson.SingleServerConfig;

public class RedissonUtil {
	private static final RedissonUtil redissonUtil = new RedissonUtil();
	private static Redisson redisson = null;
	public RedissonUtil(){
		
	}
	public static void init() {
		if (redisson==null){
			String password = SystemConfig.getInstance().REDIS_CONFIG.getPassword();
			Config redisConfig = new Config();
			//redisConfig.useClusterServers()
			SingleServerConfig singleServerConfig = redisConfig.useSingleServer();
			singleServerConfig.setAddress(SystemConfig.getInstance().REDIS_CONFIG.getIp()+":"+SystemConfig.getInstance().REDIS_CONFIG.getPart());
			singleServerConfig.setTimeout(10000);
			singleServerConfig.setConnectionPoolSize(40);
			//singleServerConfig.setAddress("127.0.0.1:6379");
	        if (!StringUtils.isBlank(password)) {
	            singleServerConfig.setPassword(password);
	        }
			redisson = (Redisson) Redisson.create(redisConfig);
		}
	}
	public static Redisson getRedisson(){
		return getInstance().redisson;
	}
	public static RedissonUtil getInstance() {
    	init();
		return redissonUtil;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
