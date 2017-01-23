package com.shendeng.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.shendeng.cluster.ClusterUtil;
import com.shendeng.datamanager.IndustryManager;
import com.shendeng.datamanager.KlineManager;
import com.shendeng.datamanager.Level1Manager;
import com.shendeng.datamanager.StockInfoManager;
import com.shendeng.quartz.CheckRunStatus;
import com.shendeng.quartz.JobManagement;
import com.shendeng.quartz.QuartzManager;
import com.shendeng.quartz.RefreshAuctionIntra;
import com.shendeng.quartz.RefreshLevel1;
import com.shendeng.quartz.RefreshLevel1Detail;
import com.shendeng.quartz.RefreshLevel1PrevClosePrice;
import com.shendeng.quartz.RefreshStockBeforeMinute;
import com.shendeng.quartz.RefreshStockInfo;
import com.shendeng.quartz.RefreshStockMin;
import com.shendeng.quartz.RefreshStockMinute;
import com.shendeng.quartz.RefreshStockOther;
import com.shendeng.quartz.RefreshStockState;
import com.shendeng.quartz.RefreshStockWeekAndMonth;
import com.shendeng.utils.Const;
import com.shendeng.utils.DateTool;
import com.shendeng.utils.RedisUtil;
import com.shendeng.utils.SystemConfig;

/**
 * 行情中心http接口
 * 
 * @author panghong
 * @date 2016-02-01
 * 
 */
public class QuotationAPI extends HttpServlet {

	private QuotationAct quotationAct = new QuotationAct();

	public void destroy() {
		super.destroy();
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		long a = System.currentTimeMillis();
		response.setHeader("Access-Control-Allow-Origin", "*");
		boolean forWeb = false;
		String resultStr = "";
		response.setContentType("text/html");
		String uri = request.getRequestURI();
		uri = uri.toLowerCase();
		if (uri.startsWith("/web/")){
			uri = uri.replace("/web/", "/");
			forWeb = true;
		}
		response.setContentType("application/json;charset=utf-8");
		// 分时图接口
		// eg: /stockapi/stockminute.htm?code=sz002570
		if (uri.startsWith("/stockapi/stockminute.htm")) {
			resultStr = quotationAct.getStockMinute(request);
		}
		// 日K
		// eg: /stockapi/stockDays.htm?code=sz002570
		else if (uri.startsWith("/stockapi/stockdays.htm")) {
			resultStr = quotationAct.getStockDays(request);
		}
		// 周K
		// eg: /stockapi/stockweeks.htm?code=002570
		else if (uri.startsWith("/stockapi/stockweeks.htm")) {
			resultStr = quotationAct.stockWeeks(request);
		}
		// 月K
		// eg: stockapi/stockMonths.htm?code=002570
		else if (uri.startsWith("/stockapi/stockmonths.htm")) {
			resultStr = quotationAct.stockMonths(request);
		}
		// 个股五档数据
		// eg: stockapi/stocknewdata.htm?code=002570
		else if (uri.startsWith("/stockapi/stocknewdata.htm")) {
			resultStr = quotationAct.stocknewdata(request);
		}
		// 个股五档数据(门户)
		// eg: stockapi/stocknewdata2.htm?code=002570
		else if (uri.startsWith("/stockapi/stocknewdata2.htm")) {
			resultStr = quotationAct.stocknewdata2(request);
		}
		// 行业(板块)排行
		// eg: stockapi/tradeUpDownList.htm
		else if (uri.startsWith("/stockapi/tradeupdownlist.htm")) {
			// typeCode 排序方式 default:up up、down、orderup、orderdown
			resultStr = quotationAct.tradeupdownlist(request);
		}
		// 个股(涨、跌、振幅)排行榜
		// eg: stockapi/stockupdownlist.htm?start=1&count=10&typeCode=up
		else if (uri.startsWith("/stockapi/stockupdownlist.htm")) {
			response.setContentType("text/plain;charset=utf-8");
			resultStr = quotationAct.stockupdownlist(request);
		}
		// 个股(涨、跌、振幅)详细
		// eg: stockapi/updownlist.htm?start=1&count=10&typeCode=up
		else if (uri.startsWith("/stockapi/updownlist.htm")) {
			resultStr = quotationAct.updownlist(request);
		}
		else if (uri.startsWith("/stockapi/updownlistindex.htm")) {
			resultStr = quotationAct.updownlistindex(request);
		}
		// 个股(涨、跌、振幅)详细(门户)
		else if (uri.startsWith("/stockapi/updownlist2.htm")) {
			resultStr = quotationAct.updownlist2(request);
		}
		// 股票详情接口(门户)
		else if (uri.startsWith("/stockapi/stockinfo.htm")) {
			resultStr = quotationAct.stockinfo(request);
		}
		// 行业相关个股
		// eg:
		// stockapi/tradeList.htm?start=1&count=20&typeCode=up&typeId=&t=1461636749
		else if (uri.startsWith("/stockapi/tradelist.htm")) {
			resultStr = quotationAct.tradelist(request);
		}
		// 行情首页指数(沪深)
		// eg: stockapi/stockIndexList.htm
		else if (uri.startsWith("/stockapi/stockindexlist.htm")) {
			resultStr = quotationAct.stockindexlist();
		}
		// 股票详情接口
		// eg: /stockapi/stockinfo.htm?code=sh000001
		else if (uri.startsWith("/stockapi/stockinfoapp.htm")) {
			resultStr = quotationAct.stockinfoApp(request);
		}
		// 股票数据库
		// eg: stockapi/fmstocks.htm
		else if (uri.startsWith("/stockapi/fmstocks.htm")) {
			resultStr = quotationAct.fmstocks();
		}
		// 搜索数据库版本判断
		// eg: stockapi/search.htm
		else if (uri.startsWith("/stockapi/search.htm")) {
			resultStr = quotationAct.search();
		}
		// html5分时图(门户)
		// eg: stockapi/stockminute2.htm
		else if (uri.startsWith("/stockapi/stockminute2.htm")) {
			resultStr = quotationAct.stockminute2(request);
		}
		// 地域分类 大于等于5的是市、地区信息(暂未发现使用)
		// eg: /stockapi/stockregions.htm?leave=5
		else if (uri.startsWith("/stockapi/stockregions.htm")) {
			resultStr = quotationAct.stockRegions(request);
		}
		// 合并利润 (暂未发现使用)
		// eg: /stockapi/getfdmtislately.htm?code=sh000001
		else if (uri.startsWith("/stockapi/getfdmtislately.htm")) {
			resultStr = quotationAct.getFdmtISLately(request);
		}
		// 键盘精灵 (暂未发现使用)
		// eg: /stockapi/getequinfo.htm?ticker=002570&pagesize=10&pagenum=1
		else if (uri.startsWith("/stockapi/getequinfo.htm")) {
			resultStr = quotationAct.getEquinfo(request);
		}
		// 根据板块获取相关股票(门户)
		else if (uri.startsWith("/stockapi/getstocks.htm")) {
			resultStr = quotationAct.getStocks(request);
		}
		//指数的涨跌幅排行，涨3条跌3条
		else if(uri.startsWith("/stockapi/indexupdownlist.htm")){
			resultStr = quotationAct.indexUpDownList(request);
		}
		//指数板块排行，涨3条，跌三条
		else if(uri.startsWith("/stockapi/indextradeupdownlist.htm")){
			resultStr = quotationAct.indexTradeUpDownList(request);
		}
		//五档明细数据
		else if (uri.startsWith("/stockapi/stocklevel1detail.htm")) {
			resultStr = quotationAct.stocklevel1detail(request);
		}else if (uri.startsWith("/stockapi/update/stockequ.htm")) {
			resultStr = quotationAct.updateEqu(request);
		}else if (uri.startsWith("/stockapi/job/checkjob.htm")) {
			resultStr = quotationAct.checkServer(request);
		}else if (uri.startsWith("/stockapi/job/stopalljob.htm")) {
			resultStr = new JobManagement().stopAllJob(request);
		}else {
			resultStr = "错误";
		}
		// System.out.println(uri+"处理请求时间"+(System.currentTimeMillis()-a));
		if (forWeb){
			resultStr = "var result = "+resultStr+";";
		}
		PrintWriter out = response.getWriter();
		out.println(resultStr);
		out.flush();
		out.close();
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.doGet(request, response);
	}

	public void init() throws ServletException {
		JobManagement.checkJobs();
		// 是否主服务器的主服务，正式环境使用ClusterUtil.isMainNode4AllServers()
//		if (true) {
//			try {
//				// 系统启动初始化
//				quotationAct.setSearch();
////				initRedisStock();
//				try {
//					// 每20秒刷新分时
//					QuartzManager.addJob("JobLevel1", new RefreshLevel1(), "0/20 * 9-15 ? * MON-FRI","group1","trigger1");
//					// 每30秒增量添加分时图
//					QuartzManager.addJob("JobStockMinute", new RefreshStockMinute(), "10,40 * 9-15 ? * MON-FRI","group2","trigger2");
//					QuartzManager.addJob("JobStockMinute", new RefreshStockBeforeMinute(), "0 * 9-15 ? * MON-FRI","group21","trigger21");
//					// 股市状态，是否交易日 50秒执行
//					QuartzManager.addJob("JobStockState", new RefreshStockState(), "0/50 * 9-15 ? * MON-FRI","group3","trigger3");
//					// 跟新股票基本信息 明天9点30执行
//					QuartzManager.addJob("JobStockInfo", new RefreshStockInfo(), "0 30 9 ? * MON-FRI","group4","trigger4");
//					// 跟新股票基本信息 明天9点30执行
//					QuartzManager.addJob("JobStockInfo", new RefreshStockInfo(), "0 35 9 ? * MON-FRI","group41","trigger41");
//					QuartzManager.addJob("JobStockInfo", new RefreshStockInfo(), "0 0 10 ? * MON-FRI","group42","trigger42");
//					//收盘更新分时图，防止有错误的
//					QuartzManager.addJob("JobStockOther", new RefreshStockMin(), "0 31 11 ? * MON-FRI ","group51","trigger51");
//					QuartzManager.addJob("JobStockOther", new RefreshStockMin(), "0 1 15 ? * MON-FRI ","group52","trigger52");
//					// 每日下午跟新，日k和合并利润表(最近) 下午4点30
//					QuartzManager.addJob("JobStockOther", new RefreshStockOther(), "0 30 16 ? * MON-FRI ","group5","trigger5");
//					// 每日下午6点跟新周k月k
//					QuartzManager.addJob("JobStockWeekAndMonth", new RefreshStockWeekAndMonth(), "0 * 18 ? * MON-FRI","group6","trigger6");
//					// 更新集合竞价数据 每天9点15开始执行
//					QuartzManager.addJob("JobAuctionIntra", new RefreshAuctionIntra(), "0/20 * 9-10 ? * MON-FRI","group7","trigger7");
//					
////					QuartzManager.addJob("JobAuctionIntratest", new RefreshAuctionIntra(), "0/20 * 9-20 ? * MON-FRI","group9","trigger9");
//					
////					QuartzManager.addJob("JobLevel1Detail", new RefreshLevel1Detail(), "0 00 15 ? * MON-FRI ","group8","trigger8");
//					
//					QuartzManager.addJob("JobLevel1PrevClosePrice", new RefreshLevel1PrevClosePrice(), "0 13 9 ? * MON-FRI ","group9","trigger9");
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
	}

}