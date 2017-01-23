package com.shendeng.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * 时间工具类
 * 
 * @author 佘崔
 * 
 */
public class DateTool {
	public static Calendar cal = Calendar.getInstance();
	/**
	 * 日期格式 yyyy-MM-dd
	 */
	public static final String DATE_FORMAT = "yyyy-MM-dd";
	/**
	 * 时间格式 yyyy-MM-dd HH:mm:ss
	 */
	public static final String TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
	/**
	 * 短时间格式 MM-dd HH:mm:ss
	 */
	public static final String SHORT_TIME_FORMAT = "MM-dd HH:mm:ss";

	/**
	 * 返回指定格式的字符串日期
	 * 
	 * @param date
	 *            日期 允许NULL,为NULL时返回空字符
	 * @param format
	 *            返回的字符串日期格式
	 * @return
	 */
	public static String DateToStr(Date date, String format) {
		String dateStr = null;
		if (date != null) {
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
			dateStr = simpleDateFormat.format(date);
		}
		return dateStr;
	}

	/**
	 * 日期转换为时间
	 * 
	 * @param date
	 *            日期
	 * @return
	 */
	public static Long toLong(Date date) {
		String dateStr = DateToStr(date, "yyyyMMdd");
		return new Long(dateStr);
	}

	/**
	 * int类型日期转string类型日期
	 * 
	 * @param dateInteger
	 *            int日期
	 * @param oldFormat
	 *            原来的数据类型
	 * @param newFormat
	 *            新的数据类型
	 * @return
	 */
	public static String IntegerToStr(Integer dateInteger, String oldFormat, String newFormat) {
		String dateStr = null;
		Date date = convertDate(dateInteger.toString(), oldFormat);
		dateStr = DateToStr(date, newFormat);
		return dateStr;
	}

	/**
	 * String时间转int时间
	 * 
	 * @param start
	 *            string时间
	 * @param oldFormat
	 *            原来的数据格式
	 * @param newFormat
	 *            新的数据格式
	 * @return
	 */
	public static Integer strDateToIntegerDate(String start, String oldFormat, String newFormat) {
		String dateStr = null;
		Date date = convertDate(start, oldFormat);
		dateStr = DateToStr(date, newFormat);
		return new Integer(dateStr);
	}

	/**
	 * 将英文格式的时间字符串转换为中文格式
	 * 
	 * @param enDateStr
	 *            原数据
	 * @param format
	 *            需要转换的格式
	 * @return
	 */
	public static String enDateStrToZhDateStr(String enDateStr, String format) {
		SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM d HH:mm:ss Z SSS yyyy", Locale.US);
		Date date = null;
		try {
			date = sdf.parse(enDateStr);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		String dateStr = DateTool.DateToStr(date, format);
		return dateStr;
	}

	/**
	 * 返回英文格式的时间类型
	 * 
	 * @param enDateStr
	 *            String日期
	 * @return
	 */
	public static Date enDateStrToDate(String enDateStr) {
		SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM d HH:mm:ss Z SSS yyyy", Locale.US);
		Date date = null;
		try {
			date = sdf.parse(enDateStr);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
	}

	/**
	 * 根据字符串返回指定格式的日期
	 * 
	 * @param dateStr
	 *            日期(字符串)
	 * @param format
	 *            日期格式
	 * @return 日期(Date)
	 * @throws ParseException
	 */
	public static Date convertDate(String dateStr, String format) {
		java.util.Date date = null;
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
		try {
			date = simpleDateFormat.parse(dateStr);
		} catch (ParseException e) {
			System.out.println("警告：" + e.getMessage());
		}
		return date;
	}

	/**
	 * 日期格式化
	 * 
	 * @param dateStr
	 *            日期字符串
	 * @param format
	 *            格式化
	 * @return string日期
	 * @throws ParseException
	 */
	public static String format(String dateStr, String format) throws ParseException {
		Date date = convertDate(dateStr, format);
		return DateToStr(date, format);
	}

	/**
	 * 小时的变动
	 * 
	 * @param date
	 *            时间
	 * @param minute
	 *            变动多少
	 * @return
	 */
	public static Date minuteChange(Date date, Integer minute) {
		java.util.Calendar calendar = java.util.Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(java.util.Calendar.MINUTE, minute);
		return calendar.getTime();
	}

	/**
	 * 小时的变动
	 * 
	 * @param date
	 *            时间
	 * @param hour
	 *            小时变动数
	 * @return
	 */
	public static Date hourChange(Date date, Integer hour) {
		java.util.Calendar calendar = java.util.Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(java.util.Calendar.HOUR_OF_DAY, hour);
		return calendar.getTime();
	}

	/**
	 * 天的变动
	 * 
	 * @param date
	 *            时间
	 * @param day
	 *            天的变动
	 * @return
	 */
	public static Date dayChange(Date date, Integer day) {
		java.util.Calendar calendar = java.util.Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(java.util.Calendar.DAY_OF_WEEK, day);
		return calendar.getTime();
	}

	/**
	 * 天的变动(周末提前到周五,周六到周四)
	 * 
	 * @param date
	 *            时间
	 * @param day
	 *            天的变动
	 * @return
	 */
	public static Date dayChangeFilter(Date date, Integer day) {
		java.util.Calendar calendar = java.util.Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(java.util.Calendar.DAY_OF_WEEK, day);
		int week = calendar.get(Calendar.DAY_OF_WEEK);
		if (week == 1) {
			return dayChangeFilter(calendar.getTime(), -2);
		} else if (week == 7) {
			return dayChangeFilter(calendar.getTime(), -1);
		}
		return calendar.getTime();
	}

	/**
	 * 月的变动
	 * 
	 * @param date
	 *            时间
	 * @param month
	 *            月变动
	 * @return
	 */
	public static Date monthChange(Date date, Integer month) {
		java.util.Calendar calendar = java.util.Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(java.util.Calendar.MONTH, month);
		return calendar.getTime();
	}

	/**
	 * 年的变动
	 * 
	 * @param date
	 *            时间
	 * @param year
	 *            年变动
	 * @return
	 */
	public static Date yearChange(Date date, Integer year) {
		java.util.Calendar calendar = java.util.Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(java.util.Calendar.YEAR, year);
		return calendar.getTime();
	}

	/**
	 * 获取年
	 * 
	 * @param date
	 *            时间
	 * @return
	 */
	public static Integer getYear(Date date) {
		if (null == date) {
			return null;
		}
		java.util.Calendar calendar = java.util.Calendar.getInstance();
		calendar.setTime(date);
		return calendar.get(Calendar.YEAR);
	}

	/**
	 * 获取月
	 * 
	 * @param date
	 *            时间
	 * @return
	 */
	public static Integer getMonth(Date date) {
		if (null == date) {
			return null;
		}
		java.util.Calendar calendar = java.util.Calendar.getInstance();
		calendar.setTime(date);
		return calendar.get(Calendar.MONTH) + 1;
	}

	/**
	 * 获取日期
	 * 
	 * @param date
	 *            时间
	 * @return
	 */
	public static Integer getDay(Date date) {
		if (null == date) {
			return null;
		}
		java.util.Calendar calendar = java.util.Calendar.getInstance();
		calendar.setTime(date);
		return calendar.get(Calendar.DATE);
	}

	/**
	 * 是否是同一天
	 * 
	 * @param d1
	 *            时间1
	 * @param d2
	 *            时间2
	 * @return
	 */
	public static boolean isTheSameDay(Date d1, Date d2) {
		Calendar c1 = Calendar.getInstance();
		Calendar c2 = Calendar.getInstance();
		c1.setTime(d1);
		c2.setTime(d2);
		return (c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)) && (c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH)) && (c1.get(Calendar.DAY_OF_MONTH) == c2.get(Calendar.DAY_OF_MONTH));
	}

	/**
	 * 倒计时
	 * 
	 * @param map
	 *            返回的map(包含日期时分秒信息)
	 * @param startTime
	 *            开始时间
	 * @param endTime
	 *            结束时间
	 * @return
	 */
	public static Map dateDiff(Map map, long startTime, long endTime) {
		long nd = 1000 * 24 * 60 * 60;// 一天的毫秒数
		long nh = 1000 * 60 * 60;// 一小时的毫秒数
		long nm = 1000 * 60;// 一分钟的毫秒数
		long ns = 1000;// 一秒钟的毫秒数long diff;try {
		// 获得两个时间的毫秒时间差异
		long diff = endTime - startTime;
		long day = diff / nd;// 计算差多少天
		long hour = diff % nd / nh;// 计算差多少小时
		long min = diff % nd % nh / nm;// 计算差多少分钟
		long sec = diff % nd % nh % nm / ns;// 计算差多少秒//输出结果
		map.put("day", day);
		map.put("hour", hour);
		map.put("min", min);
		map.put("sec", sec);
		return map;
	}

	/**
	 * 将时间字符串从format1形式转成format2形式
	 * 
	 * @param data
	 *            String时间
	 * @param sourceFormat
	 *            原格式
	 * @param targetFormat
	 *            新格式
	 * @return
	 * @throws ParseException
	 */
	public static String changeDateFormat(String dateStr, String sourceFormat, String targetFormat) throws ParseException {
		SimpleDateFormat sdf1 = new SimpleDateFormat(sourceFormat);
		SimpleDateFormat sdf2 = new SimpleDateFormat(targetFormat);
		Date date = sdf1.parse(dateStr);
		return sdf2.format(date);
	}

	/**
	 * 日期是否在之间
	 * 
	 * @param date
	 *            时间
	 * @param strDateBegin
	 *            对比的开始时间
	 * @param strDateEnd
	 *            对比的结束时间
	 * @return
	 */
	public static boolean isInDate(Date date, String strDateBegin, String strDateEnd) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String strDate = sdf.format(date);
		// 截取当前时间时分秒
		int strDateH = Integer.parseInt(strDate.substring(11, 13));
		int strDateM = Integer.parseInt(strDate.substring(14, 16));
		int strDateS = Integer.parseInt(strDate.substring(17, 19));
		// 截取开始时间时分秒
		int strDateBeginH = Integer.parseInt(strDateBegin.substring(0, 2));
		int strDateBeginM = Integer.parseInt(strDateBegin.substring(3, 5));
		int strDateBeginS = Integer.parseInt(strDateBegin.substring(6, 8));
		// 截取结束时间时分秒
		int strDateEndH = Integer.parseInt(strDateEnd.substring(0, 2));
		int strDateEndM = Integer.parseInt(strDateEnd.substring(3, 5));
		int strDateEndS = Integer.parseInt(strDateEnd.substring(6, 8));
		if ((strDateH >= strDateBeginH && strDateH <= strDateEndH)) {
			// 当前时间小时数在开始时间和结束时间小时数之间
			if (strDateH > strDateBeginH && strDateH < strDateEndH) {
				return true;
				// 当前时间小时数等于开始时间小时数，分钟数在开始和结束之间
			} else if (strDateH == strDateBeginH && strDateM >= strDateBeginM) {
				return true;
				// 当前时间小时数等于开始时间小时数，分钟数等于开始时间分钟数，秒数在开始和结束之间
			} else if (strDateH == strDateBeginH && strDateM == strDateBeginM && strDateS >= strDateBeginS) {
				return true;
			}
			// 当前时间小时数大等于开始时间小时数，等于结束时间小时数，分钟数小等于结束时间分钟数
			else if (strDateH >= strDateBeginH && strDateH == strDateEndH && strDateM <= strDateEndM) {
				return true;
				// 当前时间小时数大等于开始时间小时数，等于结束时间小时数，分钟数等于结束时间分钟数，秒数小等于结束时间秒数
			} else if (strDateH >= strDateBeginH && strDateH == strDateEndH && strDateM == strDateEndM && strDateS <= strDateEndS) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	/**
	 * 计算两个日期之间相差的天数
	 * 
	 * @param smdate
	 *            较小的时间
	 * @param bdate
	 *            较大的时间
	 * @return 相差天数
	 * @throws ParseException
	 */
	public static int daysBetween(Date smdate, Date bdate) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		smdate = sdf.parse(sdf.format(smdate));
		bdate = sdf.parse(sdf.format(bdate));
		Calendar cal = Calendar.getInstance();
		cal.setTime(smdate);
		long time1 = cal.getTimeInMillis();
		cal.setTime(bdate);
		long time2 = cal.getTimeInMillis();
		long between_days = (time2 - time1) / (1000 * 3600 * 24);

		return Integer.parseInt(String.valueOf(between_days));
	}

	/**
	 * 是否是同一月
	 * 
	 * @param d1
	 *            时间1
	 * @param d2
	 *            时间2
	 * @return
	 */
	public static boolean isTheSameMonth(Date d1, Date d2) {
		Calendar c1 = Calendar.getInstance();
		Calendar c2 = Calendar.getInstance();
		c1.setTime(d1);
		c2.setTime(d2);
		return (c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)) && (c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH));
	}

	/**
	 * 把当前时间段按周分组
	 * 
	 * @param startDate
	 *            开始时间
	 * @param endDate
	 *            结束时间
	 * @return 分组后的map
	 * @throws ParseException
	 */
	public static Map<String, String[]> getWeeks(Date startDate, Date endDate) throws ParseException {
		Map<String, String[]> weeks = new TreeMap<String, String[]>(new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				// 如果有空值，直接返回0
				if (o1 == null || o2 == null) {
					return 0;
				}
				return Long.valueOf(o2).compareTo(Long.valueOf(o1));
			}
		});
		String[] groupWeek = new String[5];
		// 1. 判断当前日期时候是否是周一
		Calendar now = Calendar.getInstance();
		// now.setTime(DateTool.convertDate(startDate, "yyyy-MM-dd"));
		now.setTime(startDate);
		int week = now.get(Calendar.DAY_OF_WEEK);
		if (week == 1) {
			// 周日特殊处理
			startDate = dayChange(startDate, 1);
		} else if (week != 2) {
			// 是周一 则指定为基准开始日期
			startDate = dayChange(startDate, 7 - week + 2);
		}
		groupWeek[0] = DateToStr(startDate, "yyyy-MM-dd");
		groupWeek[1] = getNextWeekDay(startDate, 1);
		groupWeek[2] = getNextWeekDay(startDate, 2);
		groupWeek[3] = getNextWeekDay(startDate, 3);
		groupWeek[4] = getNextWeekDay(startDate, 4);
		weeks.put(groupWeek[4].replaceAll("-", ""), groupWeek);
		// 2. 开始分组
		while (true) {
			// 如果下周一的日期比endDate大 则分组完成
			startDate = DateTool.convertDate(groupWeek[0], "yyyy-MM-dd");
			groupWeek = new String[5];
			groupWeek[0] = getNextWeekDay(startDate, 7);
			groupWeek[1] = getNextWeekDay(startDate, 8);
			groupWeek[2] = getNextWeekDay(startDate, 9);
			groupWeek[3] = getNextWeekDay(startDate, 10);
			groupWeek[4] = getNextWeekDay(startDate, 11);
			weeks.put(groupWeek[4].replaceAll("-", ""), groupWeek);
			if (daysBetween(DateTool.convertDate(groupWeek[4], "yyyy-MM-dd"), endDate) < 7) {
				break;
			}
		}
		return weeks;
	}

	/**
	 * 把当前时间段按月分组
	 * 
	 * @param startDate
	 *            开始时间
	 * @param endDate
	 *            结束时间
	 * @return 分组后的map
	 */
	public static Map<String, String[]> getMonths(Date startDate, Date endDate) {
		Map<String, String[]> Months = new TreeMap<String, String[]>(new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				// 如果有空值，直接返回0
				if (o1 == null || o2 == null) {
					return 0;
				}
				return Long.valueOf(o2.replaceAll("-", "")).compareTo(Long.valueOf(o1.replaceAll("-", "")));
			}
		});
		String[] groupMonth = new String[2];// 保存一个月开始日 和 结束日
		// 获取开始日期的最后一天和最开始一天
		groupMonth[0] = DateToStr(startDate, "yyyy-MM-dd");
		groupMonth[1] = getLastDay(startDate);
		Months.put(groupMonth[1], groupMonth);
		while (true) {
			// 下个月
			startDate = dayChange(startDate, 30);
			groupMonth = new String[2];
			// 最后一天与结束时间在同一年同一月则跳出循环
			if (isTheSameMonth(startDate, endDate)) {
				groupMonth[0] = getFirstDay(startDate);
				groupMonth[1] = DateToStr(dayChangeFilter(endDate, 0), "yyyy-MM-dd");
				Months.put(groupMonth[1], groupMonth);
				break;
			}
			groupMonth[0] = getFirstDay(startDate);
			groupMonth[1] = getLastDay(startDate);
			Months.put(groupMonth[1], groupMonth);
		}
		return Months;
	}

	/**
	 * 获得下周星期一的日期
	 * 
	 * @param date
	 *            时间
	 * @param count
	 *            添加天数
	 * @return 返回日期yyyy-MM-dd
	 */
	public static String getNextWeekDay(Date date, int count) {
		Calendar strDate = Calendar.getInstance();
		strDate.setTime(date);
		strDate.add(strDate.DATE, count);
		GregorianCalendar currentDate = new GregorianCalendar();
		currentDate.set(strDate.get(Calendar.YEAR), strDate.get(Calendar.MONTH), strDate.get(Calendar.DATE));
		Date monday = currentDate.getTime();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		String preMonday = df.format(monday);
		return preMonday;
	}

	/**
	 * 当月第一天
	 * 
	 * @param theDate
	 *            时间
	 * @return
	 */
	private static String getFirstDay(Date theDate) {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		GregorianCalendar gcLast = (GregorianCalendar) Calendar.getInstance();
		gcLast.setTime(theDate);
		gcLast.set(Calendar.DAY_OF_MONTH, 1);
		return df.format(gcLast.getTime());

	}

	/**
	 * 当月最后一天
	 * 
	 * @param theDate
	 *            时间
	 * @return
	 */
	private static String getLastDay(Date theDate) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(theDate);
		calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
		int week = calendar.get(Calendar.DAY_OF_WEEK);
		if (week == 1) {
			calendar.add(java.util.Calendar.DAY_OF_WEEK, -2);
		} else if (week == 7) {
			calendar.add(java.util.Calendar.DAY_OF_WEEK, -1);
		}
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		return df.format(calendar.getTime());

	}

	/**
	 * 返回星期几
	 * 
	 * @return
	 */
	public static int getWeekOfNow() {
		Calendar calendar = Calendar.getInstance();
		return calendar.get(Calendar.DAY_OF_WEEK);
	}

}
