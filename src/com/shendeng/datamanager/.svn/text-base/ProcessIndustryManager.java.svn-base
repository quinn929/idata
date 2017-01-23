package com.shendeng.datamanager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONObject;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.shendeng.utils.Const;
import com.shendeng.utils.JedisUtil.Hash;
import com.shendeng.utils.RedisUtil;
import com.shendeng.utils.StockUtils;
import com.shendeng.utils.StrTool;

/**
 * 查询行业数据
 *
 * @author qy
 * @date 2016年6月10日
 */
public class ProcessIndustryManager {
	private static ProcessIndustryManager pim = new ProcessIndustryManager();

	public static ProcessIndustryManager getInstance() {
		return pim;
	}

	Hash hash = RedisUtil.getJedisUtil().HASH;
	
	/**
	 * 行业(板块)排行
	 * 
	 * @param typeCode
	 *            排序方式 default:up up、down、orderup、orderdown
	 */
	public JSONObject tradeUpDownList(String typeCode,int count) {
		JSONObject result = new JSONObject();
		typeCode = typeCode == null ? "up" : typeCode.toLowerCase();
		Set<String> tradeSort = null;
		if (typeCode.equals("up")) {
			tradeSort = RedisUtil.getJedisUtil().SORTSET.zrevrange(Const.RKEY_INDUSTRY_SORT_tradeRate, 0, count);
		} else if (typeCode.equals("down")) {
			tradeSort = RedisUtil.getJedisUtil().SORTSET.zrange(Const.RKEY_INDUSTRY_SORT_tradeRate, 0, count);
		} else if (typeCode.equals("orderup")) {
			tradeSort = RedisUtil.getJedisUtil().SORTSET.zrevrange(Const.RKEY_INDUSTRY_SORT_sumOrder, 0, count);
		} else if (typeCode.equals("orderdown")) {
			tradeSort = RedisUtil.getJedisUtil().SORTSET.zrange(Const.RKEY_INDUSTRY_SORT_sumOrder, 0, count);
		}
		result.put("data", tradeSort);
		result.put("error", 0);
		result.put("msg", "");
		result.put("success", true);
		return result;
	}
	
	/**
	 * 行业成分股排行
	 * 
	 * @param starti
	 *            页码
	 * @param counti
	 *            页数
	 * @param typeId
	 *            行业id
	 * @param typeCode
	 *            排序方式 default:up up、down、orderup、orderdown
	 * @return
	 */
	public JSONObject tradelist(int starti, int counti, String typeId, String typeCode) {
		JSONObject result = new JSONObject();
		typeCode = typeCode == null ? "up" : typeCode.toLowerCase();
		int start = starti == 0 ? 0 : starti;
		int count = counti == 0 ? 10 : counti;
		if (start != 0) {
			// 开始数
			start = (start - 1) * count;
		}

		List<Map<String, String>> returnList = new ArrayList<Map<String, String>>();
		
		// 获取行业股票关系
		JSONArray industryAry = JSON.parseArray(hash.hget(Const.RKEY_INDUSTRY_+"info",typeId));
		List<Map<String, String>> tradelist = new ArrayList<Map<String, String>>();
		// 获取行业所有股票快照数据
		if(industryAry != null){
			for (Object code : industryAry) {
				JSONArray priceAry = JSON.parseArray(hash.hget(Const.RKEY_LEVEL1_,StrTool.toString(code)));
				Map<String, String> priceMap= StockUtils.stockPriceArray2Map(priceAry, null);
				if(priceMap != null && !priceMap.isEmpty()){
					tradelist.add(priceMap);
				}
			}
			if (typeCode.equals("up")) {
				StockUtils.mapListSort(tradelist, "changeRate", Const.SORT_DESC);
				StockUtils.sortKeyZero2End(tradelist, "changeRate");
			} else if (typeCode.equals("down")) {
				StockUtils.mapListSort(tradelist, "changeRate", Const.SORT_ASC);
				StockUtils.sortKeyZero2End(tradelist, "changeRate");
			} else if (typeCode.equals("orderup")) {
				StockUtils.mapListSort(tradelist, "sumorder", Const.SORT_DESC);
				StockUtils.sortKeyZero2End(tradelist, "sumorder");
			} else if (typeCode.equals("orderdown")) {
				StockUtils.mapListSort(tradelist, "sumorder", Const.SORT_ASC);
				StockUtils.sortKeyZero2End(tradelist, "sumorder");
			}
			
			// 将数据存放在要返回的list中
			for (int i = start; i < (start + count); i++) {
				if (i >= tradelist.size()) {
					break;
				}
				returnList.add(tradelist.get(i));
			}
		}
		
		result.put("data", returnList);
		result.put("total", tradelist.size());
		result.put("error", 0);
		result.put("msg", "");
		result.put("success", true);
		return result;
	}
}
