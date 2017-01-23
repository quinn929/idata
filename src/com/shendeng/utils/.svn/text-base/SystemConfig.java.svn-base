package com.shendeng.utils;

import java.util.ResourceBundle;
/**
 * 系统配置操作类
 * @author naxj
 *
 */
public class SystemConfig {

	//websocket配置
	public WSConfig WS_CONFIG= new WSConfig();
	//redis配置
	public RedisConfig REDIS_CONFIG = new RedisConfig();
	//通联接口配置
	public WmcloudConfig WMCLOUD_CONFIG = new WmcloudConfig();
	
	public String JOB_CLEAR_MESSAGE_EXP=null;
	
	public String JOB_broadcastOnLineUsersJob_EXP=null;
	
	private static SystemConfig sysConf= null;
	private static ResourceBundle bundle = null;
	
	public static void init(){
		bundle = ResourceBundle.getBundle("sysconfig");
		if (bundle == null) {
			throw new IllegalArgumentException(
					"[config.properties] is not found!");
		}
		sysConf = new SystemConfig();
		sysConf.REDIS_CONFIG.setIp(bundle.getString("redis.ip").trim());
		sysConf.REDIS_CONFIG.setPart(bundle.getString("redis.port").trim());
		sysConf.REDIS_CONFIG.setPassword(bundle.getString("redis.password").trim());
		sysConf.REDIS_CONFIG.setTimeout(StrTool.toInt(bundle.getString("redis.timeout").trim()));
		sysConf.REDIS_CONFIG.setPoolsize(StrTool.toInt(bundle.getString("redis.poolsize").trim()));
		
//		sysConf.WS_CONFIG.setIp(bundle.getString("ws.ip").trim());
//		sysConf.WS_CONFIG.setPart(Integer.parseInt(bundle.getString("ws.part").trim()));
//		sysConf.WS_CONFIG.setOrigins(bundle.getString("ws.origins").trim());
		
		sysConf.WMCLOUD_CONFIG.setToken(bundle.getString("wmcloud.token").trim());
		
//		sysConf.JOB_CLEAR_MESSAGE_EXP=bundle.getString("quartz.clearMessage").trim();
//		sysConf.JOB_broadcastOnLineUsersJob_EXP=bundle.getString("quartz.broadcastOnLineUsers").trim();
	}
	
	public static SystemConfig getInstance(){
		if (sysConf==null){
			init();
		}
		return sysConf;
	}
	
	public class WSConfig {
		private String ip;
		private int part;
		private String origins;
		public String getIp() {
			return ip;
		}
		public void setIp(String ip) {
			this.ip = ip;
		}
		public int getPart() {
			return part;
		}
		public void setPart(int part) {
			this.part = part;
		}
		public String getOrigins() {
			return origins;
		}
		public void setOrigins(String origins) {
			this.origins = origins;
		}
	}
	/**
	 * 
	 *
	 * @author qy
	 * @date 2016年5月19日
	 */
	public class RedisConfig {
		private String ip;
		private String part;
		private String password;
		private int timeout;
		private int poolsize;

		public String getIp() {
			return ip;
		}
		public void setIp(String ip) {
			this.ip = ip;
		}
		public String getPart() {
			return part;
		}
		public void setPart(String part) {
			this.part = part;
		}
		public String getPassword() {
			return password;
		}
		public void setPassword(String password) {
			this.password = password;
		}
		public int getTimeout() {
			return timeout;
		}
		public void setTimeout(int timeout) {
			this.timeout = timeout;
		}
		public int getPoolsize() {
			return poolsize;
		}
		public void setPoolsize(int poolsize) {
			this.poolsize = poolsize;
		}
		
	}
	public class WmcloudConfig {
		private String token;

		public String getToken() {
			return token;
		}

		public void setToken(String token) {
			this.token = token;
		}
		
	}
	
	/**
	 * 查询在sysconfig里面key对应的值
	 *
	 * @param key sysconfig配置的key
	 * @return 返回sysconfig key 对应的值
	 */
	public static String getSysVal(String key){
		if (bundle == null) {
			bundle = ResourceBundle.getBundle("sysconfig");
		}
		return bundle.getString(key).trim();
	}
	
	public static void main(String[] args) {
		SystemConfig.getSysVal("is_init");

	}

}
