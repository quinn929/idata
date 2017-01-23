package com.shendeng.quartz;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.http.HttpServletRequest;

import com.shendeng.datamanager.IndustryManager;
import com.shendeng.datamanager.KlineManager;
import com.shendeng.datamanager.Level1Manager;
import com.shendeng.datamanager.StockInfoManager;
import com.shendeng.utils.HttpUtil;
import com.shendeng.utils.MD5Util;
import com.shendeng.utils.SystemConfig;
import com.shendeng.web.QuotationAct;

/**
 * job任务管理
 * 
 * @author qy
 * @date 2016年12月30日
 */
public class JobManagement {

	/**
	 * 检测job
	 * 
	 * void
	 */
	private static Timer timer = new Timer();
	public static void checkJobs() {
		// 0秒开始，每30秒执行一次
		timer.schedule(new checkJobs(), 60000, 30000);
	}
	
	public static void stopTimer() {
		// 0秒开始，每30秒执行一次
		System.out.println("停止Timer");
		timer.cancel();
	}

	public static void jobStart() {
		try {
			// 系统启动初始化
			QuotationAct.setSearch();
			initRedisStock();
			try {
				// 每20秒刷新分时
				QuartzManager.addJob("JobLevel1", new RefreshLevel1(), "0/20 * 9-15 ? * MON-FRI", "group1", "trigger1");
				// 每30秒增量添加分时图
				QuartzManager.addJob("JobStockMinute", new RefreshStockMinute(), "10,40 * 9-15 ? * MON-FRI", "group2", "trigger2");
				QuartzManager.addJob("JobStockBeforeMinute", new RefreshStockBeforeMinute(), "0 * 9-15 ? * MON-FRI", "group21", "trigger21");
				// 股市状态，是否交易日 50秒执行
				QuartzManager.addJob("JobStockState", new RefreshStockState(), "0/50 * 9-15 ? * MON-FRI", "group3", "trigger3");
				// 跟新股票基本信息 明天9点30执行
				QuartzManager.addJob("JobStockInfo", new RefreshStockInfo(), "0 30 9 ? * MON-FRI", "group4", "trigger4");
				// 跟新股票基本信息 明天9点30执行
				QuartzManager.addJob("JobStockInfo1", new RefreshStockInfo(), "0 35 9 ? * MON-FRI", "group41", "trigger41");
				QuartzManager.addJob("JobStockInfo2", new RefreshStockInfo(), "0 0 10 ? * MON-FRI", "group42", "trigger42");
				// 收盘更新分时图，防止有错误的
				QuartzManager.addJob("JobStockMin", new RefreshStockMin(), "0 31 11 ? * MON-FRI ", "group51", "trigger51");
				QuartzManager.addJob("JobStockMin2", new RefreshStockMin(), "0 1 15 ? * MON-FRI ", "group52", "trigger52");
				// 每日下午跟新，日k和合并利润表(最近) 下午4点30
				QuartzManager.addJob("JobStockOther", new RefreshStockOther(), "0 30 16 ? * MON-FRI ", "group5", "trigger5");
				// 每日下午6点跟新周k月k
				QuartzManager.addJob("JobStockWeekAndMonth", new RefreshStockWeekAndMonth(), "0 * 18 ? * MON-FRI", "group6", "trigger6");
				// 更新集合竞价数据 每天9点15开始执行
				QuartzManager.addJob("JobAuctionIntra", new RefreshAuctionIntra(), "0/20 * 9-10 ? * MON-FRI", "group7", "trigger7");

				QuartzManager.addJob("JobLevel1PrevClosePrice", new RefreshLevel1PrevClosePrice(), "0 13 9 ? * MON-FRI ", "group9", "trigger9");
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String stopAllJob(HttpServletRequest request) {
		String secret = request.getParameter("secret");
		if(secret !=null && MD5Util.MD5("dakastockwengu").equals(secret)){
			try {
				QuartzManager.removeJob("JobLevel1", "group1", "trigger1", "group1");
				QuartzManager.removeJob("JobStockMinute", "group2", "trigger2", "group2");
				QuartzManager.removeJob("JobStockBeforeMinute", "group21", "trigger21", "group21");
				QuartzManager.removeJob("JobStockState", "group3", "trigger3", "group3");
				QuartzManager.removeJob("JobStockInfo", "group4", "trigger4", "group4");
				QuartzManager.removeJob("JobStockInfo1", "group41", "trigger41", "group41");
				QuartzManager.removeJob("JobStockInfo2", "group42", "trigger42", "group42");
				QuartzManager.removeJob("JobStockOther", "group5", "trigger5", "group5");
				QuartzManager.removeJob("JobStockMin", "group51", "trigger51", "group51");
				QuartzManager.removeJob("JobStockMin2", "group52", "trigger52", "group52");
				QuartzManager.removeJob("JobStockWeekAndMonth", "group6", "trigger6", "group6");
				QuartzManager.removeJob("JobAuctionIntra", "group7", "trigger7", "group7");
				QuartzManager.removeJob("JobLevel1PrevClosePrice", "group9", "trigger9", "group9");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return "停止所有调度";
	}

	public static void main(String[] args) {
		new checkJobs().run();
	}

	/**
	 * 启动初始化
	 * 
	 */
	public static void initRedisStock() {
		String isInit = SystemConfig.getSysVal("is_init");
		ExecutorService threadPool = Executors.newFixedThreadPool(5);
		try {
			// 跟新股票基本信息
			if (StockInfoManager.getInstance().getEqu()) {
				// 是否初始化
				if ("true".trim().toLowerCase().equals(isInit.trim().toLowerCase())) {
					threadPool.submit(new initLevel1());
					threadPool.submit(new initStockInfo());
					threadPool.submit(new initIndustry());
					KlineManager.getInstance().KlineInit();
					Level1Manager.getInstance().getLevel1Detail(true);
				}
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		} finally {
			threadPool.shutdown();
		}
	}
}

class checkJobs extends TimerTask {

	private List<String> otherServiceUrl = new ArrayList<String>();
	private String myHostName = "tc-stockdata-mng_1";// ClusterUtil.getHastName();
	private String serGroup = SystemConfig.getSysVal("serviceGroup");
	// 上次检查的时间
	long lastTime = System.currentTimeMillis();
	/**
	 * 允许挂起的时间
	 */
	private int hangTime = Integer.valueOf(SystemConfig.getSysVal("hangTime"));

	boolean isStopTimer = false;
	
	@Override
	public void run() {
		// 首次检测出其他服务器地址
		if (otherServiceUrl == null || otherServiceUrl.size() == 0) {
			String[] serverAry = serGroup.split(",");
			for (String server : serverAry) {
				String hostName = server.split("\\|")[0];
				if (!hostName.equals(myHostName)) {
					otherServiceUrl.add(server.split("\\|")[1]);
				}
			}
		}

		for (String otherUrl : otherServiceUrl) {
			String status = "";
			try {
				status = HttpUtil.excute(otherUrl + "/stockapi/job/checkjob.htm?secret="+MD5Util.MD5("dakastockwengu"));
			} catch (Exception e) {
				status = "1";
			}

			// 判断另一个服务是否可用，并正在运行
			if ("0".equals(status)) {// 返回0代表成功
				lastTime = System.currentTimeMillis();
				// 只要其中一个是好的就代表木有问题
				break;
			} else {// 否则就是都失败了,失败了就启动自己,并调用对方的停止
				int difTime = (int) ((System.currentTimeMillis() - lastTime) / 1000 / 60);
				System.out.println(otherUrl+"---检测失败----" + difTime);
				if (difTime > hangTime) {// 如果死了超过3分钟
					// 停止对方的调度
					try {
						HttpUtil.excute(otherUrl + "/stockapi/job/stopalljob.htm?secret="+MD5Util.MD5("dakastockwengu"));
					} catch (Exception e) {
						
					}
					isStopTimer = true;
				}
			}
		}
		if(isStopTimer){
			// 启动自己的所有调度
			JobManagement.stopTimer();
			JobManagement.jobStart();
		}
	}
}

/**
 * 初始化level数据
 * 
 * @author qy
 * @date 2016年5月26日
 */
class initLevel1 implements Runnable {
	@Override
	public void run() {
		try {
			// 初始化K线相关的信息
			Level1Manager.getInstance().downloadData();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
