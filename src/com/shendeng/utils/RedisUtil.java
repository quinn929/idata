package com.shendeng.utils;

import org.apache.commons.lang.StringUtils;
import org.redisson.Config;
import org.redisson.Redisson;
import org.redisson.SingleServerConfig;
/**
 * redis工具类
 * @author naxj
 *
 */
public class RedisUtil {
	private static final RedisUtil redissonUtil = new RedisUtil();
	private static Redisson redisson = null;
	private static JedisUtil jedisUtil = null;
	public RedisUtil(){
		
	}
	/**
	 * 初始化
	 */
	public static void init() {
		if (jedisUtil==null){
			JedisUtil.init(SystemConfig.getInstance().REDIS_CONFIG.getIp(), SystemConfig.getInstance().REDIS_CONFIG.getPart(),SystemConfig.getInstance().REDIS_CONFIG.getPassword(),SystemConfig.getInstance().REDIS_CONFIG.getTimeout(),
					(int)(SystemConfig.getInstance().REDIS_CONFIG.getPoolsize()));
			jedisUtil = JedisUtil.getInstance();
		}
	}
	/**
	 * Redisson实现的redis操作对象
	 * @return
	 */
	public static Redisson getRedisson(){
		return getInstance().redisson;
	}
	/**
	 * jedis实现的redis操作对象 jedis工具类
	 * @return
	 */
	public static JedisUtil getJedisUtil(){
		return getInstance().jedisUtil;
	}
	public static RedisUtil getInstance() {
    	init();
		return redissonUtil;
	}
	
	public static void main(String[] args) {

	}

}
