package com.shendeng.utils;

import java.io.IOException;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import javax.swing.text.StyledEditorKit.BoldAction;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.CompareToBuilder;

import com.alibaba.fastjson.JSON;
import com.csvreader.CsvReader;
import com.shendeng.utils.JedisUtil.Hash;

/**
 * 股票相关的工具类
 * 
 * @author qy
 * @date 2016年4月27日
 */
public class StockUtils {
	//A股 股票编码正则
	//600xxx和601xxx是沪市A股，000XXX是深市A股，002xxx是深市中小版，300XXX是深市创业板。
	public static Pattern STOCK_PATTERN = Pattern.compile("(((002|000|300|600)[\\d]{3})|60[\\d]{4})");
	private static Hash hash = RedisUtil.getJedisUtil().HASH;
	/**
	 * 是否为沪深A股
	 * @param code
	 * @return
	 * @author naxj
	 */
	public static boolean isAstock(String code){
		return STOCK_PATTERN.matcher(code).find();
	}
	/**
	 * 转换成通联识别的交易所代码
	 * 
	 * @param s
	 *            交易所代码
	 * @return
	 */
	public static String getTLCode(String s) {
		if (s.equals("sh")) {
			return "XSHG";
		} else if (s.equals("sz")) {
			return "XSHE";
		} else {
			return "";
		}
	}

	/**
	 * 将通联识别的交易所代码转成普通交易所代码
	 * 
	 * @param XSHG
	 *            通联识交易所代码
	 * @return
	 */
	public static String getLTCode(String XSHG) {
		if (XSHG.equals("XSHG")) {
			return "sh";
		} else if (XSHG.equals("XSHE")) {
			return "sz";
		} else {
			return "";
		}
	}

	/**
	 * 直接将通联返回的股票代码转换成shXXXXX or szXXXXX
	 * 
	 * @param code
	 *            通联使用的股票代码
	 * @return
	 */
	public static String getStockCode(String code) {
		String[] codes = code.split("\\.");
		return getLTCode(codes[1]) + codes[0];
	}

	/**
	 * 普通交易代码转通联交易代码
	 * 
	 * @param code
	 *            普通交易
	 * @return
	 */
	public static String getTLStockCode(String code) {
		if(code.length() > 2){
			return code.substring(2)+"."+getTLCode(code.substring(0, 2));
		}else{
			return code;
		}
	}

	/**
	 * 处理通联的股票代码返回纯数字的股票代码
	 * 
	 * @param tlCode
	 *            通联使用的股票代码
	 * @return
	 */
	public static String getCode(String tlCode) {
		String[] codes = tlCode.split("\\.");
		return codes[0];
	}

	/**
	 * 将数组多添加几个下标
	 * 
	 * @param oldArray
	 *            原数组
	 * @param addLength
	 *            添加下标数
	 * @return 添加过下标的数组
	 */
	public static String[] arrayAddLength(String[] oldArray, int addLength) {
		int length = Array.getLength(oldArray);
		int newLength = length + addLength;
		String[] newArray = new String[newLength];
		System.arraycopy(oldArray, 0, newArray, 0, length);
		return newArray;
	}

	/**
	 * 判断数据是从当天取还是昨天取 0 当天 ，其他都取前一天的数据
	 * 
	 * @return
	 */
	public static int getHisDay() {
		Calendar now = Calendar.getInstance();
		int week = now.get(Calendar.DAY_OF_WEEK) - 1;
		int hour = now.get(Calendar.HOUR_OF_DAY);
		int min = now.get(Calendar.MINUTE);
		if (week == 6) {// 周六
			return -1;
		} else if (week == 0) {// 周日
			return -2;
		} else if (week == 1) {// 周一
			return -3;
		} else if (hour < 9 || (hour == 9 && min < 30)) {
			return -1;
		} else {
			return 0;
		}
	}

	/**
	 * 计算市盈率
	 * 
	 * @param code
	 *            股票代码
	 * @param price
	 *            当前价格
	 * @param closePrice
	 *            收盘价格
	 * @return 季度收益
	 * @throws Exception
	 */
	public static double calculatePEratio(String code, double price,
			double closePrice) {
		// 如果当前价格为0，收盘价也为0，直接返回0，否则当前价等于收盘价
		if (price == 0) {
			if (closePrice == 0) {
				return 0;
			} else {
				price = closePrice;
			}
		}
		com.alibaba.fastjson.JSONArray ary = JSON.parseArray(hash.hgetAll(
				Const.PKEY_FDMTISLATELY).get(code));
		if (ary == null || ary.size() == 0) {
			return 0;
		}
		if (ary.get(Const.FdmtISLately_reportType) == null) {
			return 0;
		}
		String reportType = StrTool.toString(ary
				.get(Const.FdmtISLately_reportType));
		if (StrTool.toString(ary.get(Const.FdmtISLately_dilutedEPS)) == null
				&& StrTool.toString(ary.get(Const.FdmtISLately_dilutedEPS))
						.length() > 0) {
			return 0;
		}
		Double dilutedEPS = StrTool.toDouble(StrTool.toString(ary
				.get(Const.FdmtISLately_dilutedEPS)));

		// 动态市盈率算法
		if (dilutedEPS != null && dilutedEPS >= 0) {
			// 第一季报 收益 *1/4
			if (reportType.equals("Q1")) {
				return price / dilutedEPS * (1.0 / 4.0);
			} else if (reportType.equals("S1")) {
				// 第二季报 收益* 2/4
				return price / dilutedEPS * (1.0 / 2.0);
			} else if (reportType.equals("CQ3")) {
				// 第三季报 收益* 3/4
				return (price / dilutedEPS) * (3.0 / 4.0);
			} else if (reportType.equals("A")) {
				// 年报 收益 * 4/4
				return price / dilutedEPS;
			}
			return price / dilutedEPS;
		} else {
			return 0;
		}
	}

	/**
	 * 对List<Map<String,String>格式的list进行排序 desc
	 * 
	 * @param mapList
	 *            需要排序的list
	 * @param key
	 *            排序用的字段
	 * @param orderType
	 *            排序方式
	 */
	public static void mapListSort(List<Map<String, String>> mapList,
			final String key, final int orderType) {
		Collections.sort(mapList, new Comparator<Map<String, String>>() {
			public int compare(Map<String, String> entry1,
					Map<String, String> entry2) {
				String value1 = entry1.get(key), value2 = entry2.get(key);
				// 对数据进行处理
				if (value1 != null) {
					value1 = value1.replaceAll("%", "");
				}
				if (value2 != null) {
					value2 = value2.replaceAll("%", "");
				}
				double retval = Double.parseDouble(value1)
						- Double.parseDouble(value2);
				// 排序方式
				if (Const.SORT_ASC == orderType) {
					if (retval < 0) {
						return -1;
					} else if (retval == 0) {
						return 0;
					} else {
						return 1;
					}
				} else {
					if (retval < 0) {
						return 1;
					} else if (retval == 0) {
						return 0;
					} else {
						return -1;
					}
				}
			}
		});
	}

	/**
	 * 将 List Map 里面的某个key的值为0 的放到list最后面
	 *
	 * @param listMap
	 * @param key
	 * @return void
	 */
	public static void sortKeyZero2End(List<Map<String, String>> listMap,String key){
		List<Map<String, String>> not0ListMap = new ArrayList<Map<String, String>>(); 
		List<Map<String, String>> zeroListMap = new ArrayList<Map<String, String>>(); 
		
		for(int i = 0;i < listMap.size();i++){
			Map<String, String> map = listMap.get(i);
			double sortKey = StrTool.toDouble(map.get(key)).doubleValue();
			if(sortKey == 0){
				zeroListMap.add(map);
			}else{
				not0ListMap.add(map);
			}
		}
		listMap.removeAll(listMap);
		listMap.addAll(not0ListMap);
		listMap.addAll(zeroListMap);
	}
	
	/**
	 * 股票行情快照排序（快照的存储格式为数组）
	 * 
	 * @param oriMap
	 *            需要排序的map
	 * @param field
	 *            排序的依据字段 字段必须是可以转换为double类型的值
	 * @param sortType
	 *            排序的方法
	 * @return
	 */
	public static List<String> stockPriceSortByField(
			LinkedHashMap<String, com.alibaba.fastjson.JSONArray> oriMap,
			final int field, final int sortType) {
		List<String> result = new ArrayList<String>();
		if (oriMap != null && !oriMap.isEmpty()) {
			List<Map.Entry<String, com.alibaba.fastjson.JSONArray>> entryList = new ArrayList<Map.Entry<String, com.alibaba.fastjson.JSONArray>>(
					oriMap.entrySet());
			// 排序
			Collections
					.sort(entryList,
							new Comparator<Map.Entry<String, com.alibaba.fastjson.JSONArray>>() {
								public int compare(
										Entry<String, com.alibaba.fastjson.JSONArray> entry1,
										Entry<String, com.alibaba.fastjson.JSONArray> entry2) {
									com.alibaba.fastjson.JSONArray value1 = entry1
											.getValue();
									com.alibaba.fastjson.JSONArray value2 = entry2
											.getValue();
									// 数据处理
									String val1 = StringUtils.isBlank(value1
											.getString(field)) ? "-9999"
											: value1.getString(field);
									String val2 = StringUtils.isBlank(value2
											.getString(field)) ? "-9999"
											: value1.getString(field);
									double retval = Double.parseDouble(val1)
											- Double.parseDouble(val2);
									// 排序方式
									if (Const.SORT_ASC == sortType) {
										if (retval < 0) {
											return -1;
										} else if (retval == 0) {
											return 0;
										} else {
											return 1;
										}
									} else {
										if (retval < 0) {
											return 1;
										} else if (retval == 0) {
											return 0;
										} else {
											return -1;
										}
									}
								}
							});

			Iterator<Map.Entry<String, com.alibaba.fastjson.JSONArray>> iter = entryList
					.iterator();
			Map.Entry<String, com.alibaba.fastjson.JSONArray> tmpEntry = null;
			while (iter.hasNext()) {
				tmpEntry = iter.next();
				// 只返回股票信息，去除指数
				if (!"IDX".equals(tmpEntry.getValue().get(
						Const.PRICE_assetClass))
						&& !StringUtils.isBlank(tmpEntry.getValue().getString(
								Const.PRICE_shortNM))) {
					result.add(tmpEntry.getKey());
				}
			}
		}
		return result;
	}

	/**
	 * 行情快照数组转 map
	 * 
	 * @param items
	 *            行情快照数组
	 * @param jsonArray
	 *            股票信息数组
	 * @return
	 */
	public static Map<String, String> stockPriceArray2Map(
			com.alibaba.fastjson.JSONArray items,
			com.alibaba.fastjson.JSONArray jsonArray) {
		Map<String, String> result = new HashMap<String, String>();
		if(items != null){
			result.put("id", items.getString(Const.PRICE_ticker));
			result.put("price", items.getString(Const.PRICE_lastPrice));
			// 将通联返回的交易所代码 转换成 sh or sz
			result.put("code",
					StockUtils.getLTCode(items.getString(Const.PRICE_exchangeCD))
							+ items.getString(Const.PRICE_ticker));
			result.put("turnoverRate", items.getString(Const.PRICE_turnoverRate));
			result.put("peRate", items.getString(Const.PRICE_staticPE));
			result.put("swing", String.format("%.2f",
					new BigDecimal(items.getString(Const.PRICE_amplitude))
							.movePointRight(2).doubleValue()));
			result.put("circulationValue",
					items.getString(Const.PRICE_negMarketValue));
			String totalShares = "0";
			// 取股票信息的总股本
			if (jsonArray == null) {
				result.put("totalValue", "0");
			} else {
				try {
					totalShares = jsonArray.getString(Const.INFO_totalShares);
				} catch (Exception e) {

				} finally {
					totalShares = totalShares.equals("") ? "0" : totalShares;
				}
				result.put("totalValue",
						jsonArray == null ? "0" : StrTool.keep2Point(totalShares));
			}
			result.put("cityNetRate", "0");
			result.put("highPrice", items.getString(Const.PRICE_highPrice));
			result.put("lowPrice", items.getString(Const.PRICE_lowPrice));
			result.put("volumn", items.getString(Const.PRICE_volume));
			result.put("volumnPrice", items.getString(Const.PRICE_value));
			result.put("closePrice", items.getString(Const.PRICE_prevClosePrice));
			result.put("openPrice", items.getString(Const.PRICE_openPrice));
			result.put("dateTime", items.getString(Const.PRICE_timestamp)
					.substring(0, 10));
			result.put("name", items.getString(Const.PRICE_shortNM));
			result.put("type", "0");
			result.put("change",
					StrTool.keep2Point(items.getString(Const.PRICE_change)));
			// Double 乘以100 和乘以10*10 精度不一样，乘以100 的精度太长不符合数据要求
			result.put("changeRate", String.format("%.2f",
					new BigDecimal(items.getString(Const.PRICE_changePct))
							.movePointRight(2).doubleValue()));
			result.put("sumorder", StrTool.keep2Point(items
					.getString(Const.PRICE_totalOrderValue)));
		}else{
			
		}
		return result;
	}

	/**
	 * 查询通联接口，是否交易日
	 * 
	 * @return
	 * @throws IOException
	 */
	public static boolean isTradingDay() throws IOException {
		// 按接口交易日判断
		String yest = DateTool.DateToStr(new Date(), "yyyyMMdd");
		String url = Const.TRADECAL + "&beginDate=" + yest + "&endDate=" + yest;
		CsvReader body = WmcloudUtil.url2csv(url);
		if (body == null) {
			return false;
		}
		body.readRecord();
		while (body.readRecord()) {
			String[] a = body.getValues();
			if (a[1] != null && "1".equals(a[1])) {
				return true;
			} else {
				return false;
			}
		}
		body.close();
		return false;
	}

	public static boolean isTradingTime() {
		// 按常规时间判断
		Calendar now = Calendar.getInstance();
		int week = now.get(Calendar.DAY_OF_WEEK) - 1;
		try {
			// 九点半之前 周六周日 不是交易日
			if ((new Date().compareTo(new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss").parse(DateTool.DateToStr(new Date(),
					"yyyy-MM-dd") + " 09:30:00")) > 0)
					&& (new Date().compareTo(new SimpleDateFormat(
							"yyyy-MM-dd HH:mm:ss").parse(DateTool.DateToStr(
							new Date(), "yyyy-MM-dd") + " 15:10:00")) < 0)
					&& week < 6 && week > 0) {
				return true;
			} else {
				return false;
			}
		} catch (ParseException e) {
			return false;
		}
	}

	/**
	 * 将指数代码转成指数名称
	 * 
	 * @param code
	 * @return
	 */
	public static String idxCode2Name(String code) {
		if ("sh000001".equals(code)) {
			return "上证指数";
		} else if ("sz399106".equals(code)) {
			return "深证综指";
		} else if ("sz399102".equals(code)) {
			return "创业板综指";
		} else if("sh000300".equals(code)){
			return "沪深300";
		}else if("sh000016".equals(code)){
			return "上证50";
		}else if("sh000905".equals(code)){
			return "中证500";
		}else if("sz399005".equals(code)){
			return "中小板指";
		}else if("sh000010".equals(code)){
			return "上证180";
		}else if("sh000009".equals(code)){
			return "上证380";
		}else{
			return "";
		}

	}

	public static String getAllIdxSimpleCode() {
		String allIdx = SystemConfig.getSysVal("idx.code");
		String[] idxAry = allIdx.split(",");
		String allIdxSimpleCode = "";
		for (String idxCode : idxAry) {
			allIdxSimpleCode += getStockCode(idxCode) + ",";
		}
		return allIdxSimpleCode;
	}

//	public static void listSort(List list){
//		Collections.reverse(list); .sort(new CompareToBuilder() {
//		});
//	}
	
	public static void main(String[] args) throws IOException {
		System.out.println(isAstock("300001"));
		System.out.println(isAstock("600001"));
		System.out.println(isAstock("601001"));
		System.out.println(isAstock("603001"));
		System.out.println(isAstock("000100"));
		System.out.println(isAstock("002001"));
	}

}
