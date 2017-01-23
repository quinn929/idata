package com.shendeng.utils;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

/**
 * 字符串处理
 * 
 * @author naxj
 * 
 */
public class StrTool {
	/**
	 * 替换json关键字
	 * 
	 * @param s
	 * @return
	 */
	public static String string2Json(String s) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			switch (c) {
			case '\"':
				sb.append("\\\"");
				break;
			case '\\':
				sb.append("\\\\");
				break;
			case '/':
				sb.append("\\/");
				break;
			case '\b':
				sb.append("\\b");
				break;
			case '\f':
				sb.append("\\f");
				break;
			case '\n':
				sb.append("\\n");
				break;
			case '\r':
				sb.append("\\r");
				break;
			case '\t':
				sb.append("\\t");
				break;
			default:
				sb.append(c);
			}
		}
		return sb.toString();
	}

	/**
	 * 将对象转换为String类型
	 * 
	 * @param o
	 *            obj对象
	 * @return
	 */
	public static String toString(Object o) {
		if (null == o) {
			return null;
		}else {
			return o.toString();
		}
	}

	/**
	 * 将对象转换为String类型
	 * 
	 * @param o
	 *            obj对象
	 * @param defaultValue
	 *            为空的默认值
	 * @return
	 */
	public static String toString(Object o, String defaultValue) {
		if (null == o) {
			return defaultValue;
		} else {
			return o.toString();
		}
	}

	/**
	 * obj转string(如果长度小于9位且不是数字不为空，直接返回对应的string，否则截取3到6位为*)
	 * 
	 * @param o
	 *            obj对象
	 * @return
	 */
	public static String toName(Object o) {
		if (null == o || !isNumeric(o.toString()) || o.toString().length() < 9) {
			if (o != null) {
				return o.toString();
			}
			return "";
		}
		String str = o.toString();
		str = str.substring(0, 3) + "***" + str.substring(str.length() - 4, str.length() - 1);
		return str;
	}

	/**
	 * 判断是否为数据
	 * 
	 * @param str
	 *            obj对象
	 * @return
	 */
	public static boolean isNumeric(Object str) {
		if (null == str) {
			return false;
		}
		Pattern pattern = Pattern.compile("[0-9]*");
		return pattern.matcher(str.toString()).matches();
	}

	/**
	 * 将对象转换为Long类型
	 * 
	 * @param o
	 *            obj对象
	 * @param defaultValue
	 *            obj为空的默认值
	 * @return
	 */
	public static Long toLong(Object o, Long defaultValue) {
		if (null == o) {
			return defaultValue;
		} else {
			return (Long) o;
		}
	}

	/**
	 * 将对象转换为Long类型
	 * 
	 * @param o
	 *            obj对象
	 * @return
	 */
	public static Long toLong(Object o) {
		if (null == o) {
			return null;
		} else {
			return new Long(o.toString());
		}
	}

	/**
	 * 将对象转换为Integer类型
	 * 
	 * @param o
	 *            obj对象
	 * @return
	 */
	public static Integer toInteger(Object o) {
		if (null == o) {
			return null;
		} else {
			return new Integer(o.toString());
		}
	}

	/**
	 * 将对象转换为Int类型
	 * 
	 * @param o
	 *            obj对象
	 * @return
	 */
	public static int toInt(Object o) {
		if (null == o) {
			return 0;
		} else if (o.toString() == "") {
			return 0;
		} else {
			return new Integer(o.toString());
		}
	}

	/**
	 * 将对象转换为Double类型
	 * 
	 * @param o
	 *            obj对象
	 * @return
	 */
	public static Double toDouble(Object o) {
		if (null == o || o.equals("")) {
			return null;
		} else {
			return new Double(o.toString());
		}
	}

	/**
	 * 将对象转换为Integer类型
	 * 
	 * @param o
	 *            obj对象
	 * @param value
	 *            为空默认值
	 * @return
	 */
	public static Integer toInteger(Object o, int value) {
		if (null == o) {
			return value;
		} else {
			return new Integer(o.toString());
		}
	}

	/**
	 * 将对象转换为Date类型
	 * 
	 * @param o
	 *            obj对象
	 * @return
	 */
	public static Date toTime(Object o) {
		if (null == o) {
			return null;
		} else {
			return (Date) o;
		}
	}

	/**
	 * 将对象转换为String日期格式(MM-dd HH:mm)
	 * 
	 * @param o
	 *            obj对象
	 * @return
	 */
	public static String toTimeStr(Object o) {
		Date d = toTime(o);
		if (null == o) {
			return "";
		} else {
			return DateTool.DateToStr(d, "MM-dd HH:mm");
		}
	}

	/**
	 * 对象是否为空
	 * 
	 * @param o
	 *            obj对象
	 * @return
	 */
	public static boolean isNotBank(Object o) {
		if (null != o && !o.toString().trim().equals("")) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 对象是否为空
	 * 
	 * @param o
	 *            obj对象
	 * @return
	 */
	public static boolean isBank(Object o) {
		return !isNotBank(o);
	}

	/**
	 * 解码
	 * 
	 * @param s
	 *            需要解码字符串
	 * @return 返回解码后的字符串
	 */
	public static String decode(String s) {
		if (null != s) {
			try {
				return URLDecoder.decode(s, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * 获取当前域名
	 * 
	 * @param request
	 *            request对象
	 * @return
	 */
	public static String getDomain(HttpServletRequest request) {
		String url = request.getRequestURL().toString();
		String uri = request.getRequestURI();
		String domain = url.replace(uri, "");
		if (!domain.startsWith("http://")) {
			domain = "http://" + domain;
		}
		if (domain.endsWith("/")) {
			domain = domain.substring(0, domain.length() - 1);
		}
		return domain;
	}

	/**
	 * 如果obj为空或者不是数字则显示 i
	 * 
	 * @param o
	 *            待处理的obj
	 * @param i
	 *            如果为空或不是数字显示成该数字
	 * @return
	 */
	public static Double objToPositive(Object o, Integer i) {
		if (o == null) {
			return StrTool.toDouble(i);
		} else if (o.toString().length() > 0) {
			if (StrTool.toDouble(o) > 0) {
				return StrTool.toDouble(o);
			} else {
				return StrTool.toDouble(i);
			}
		} else {
			return StrTool.toDouble(i);
		}
	}

	/**
	 * 判断是否为整型
	 * 
	 * @param str
	 *            字符串
	 * @return
	 */
	public static boolean isInt(String str) {
		for (int i = str.length(); --i >= 0;) {
			if (!Character.isDigit(str.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 设置Double数值的精度
	 * 
	 * @param price
	 *            价格
	 * @param scale
	 *            保留几位小数
	 * @return
	 */
	public static Double setDoubleScale(Double d, int scale) {
		BigDecimal b = new BigDecimal(d);
		d = b.setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
		return d;
	}

	/**
	 * 字符串前后各截取一位
	 * 
	 * @param str
	 *            待处理字符串
	 * @param identifier
	 *            含有identifier才截取
	 * @return
	 */
	public static String cutBothSidesIdentifier(String str, String identifier) {
		if (str.startsWith(identifier)) {
			str = str.substring(1, str.length());
		}
		if (str.endsWith(identifier)) {
			str = str.substring(0, str.length() - 1);
		}
		return str;
	}

	/**
	 * 字符串去掉重复的内容
	 * 
	 * @param str 待处理字符串
	 * @param identifier
	 *            分隔符
	 * @return
	 */
	public static String removalRepeat(String str) {
		if (null == str) {
			return null;
		}
		str = cutBothSidesIdentifier(str, ",");
		String[] strArray = str.split(",");
		String retureStr = "";
		for (int i = 0; i < strArray.length; i++) {
			if (retureStr.indexOf(strArray[i]) < 0) {
				retureStr = retureStr + strArray[i];
			}
		}
		retureStr = cutBothSidesIdentifier(retureStr, ",");
		return retureStr;
	}
	
	   /**
     * 随机生成八位数
     *
     * @return
     */
    public static String createRandom(){
    	int random = (int)(Math.random()*90000000+100000000);
    	return StrTool.toString(random);
    }
    
    /**
     * 如果数据等于0，则在0后加上八位随机数
     *
     * @param obj 检测的值
     * @return
     */
    public static String zero2decimal(Object obj){
    	try {
    		if(obj == null || "".equals(obj)){
        		return "0.0000"+createRandom();
        	}else if(toDouble(obj) == 0){
    			return "0.0000"+createRandom();
    		}else{
    			return toString(obj)+createRandom();
    		}
		} catch (Exception e) {
			System.out.println(obj == null);
			System.out.println("".equals(obj));
			e.printStackTrace();
		}
    	return "0.0000"+createRandom();
    }
    
    /**
     * 保留两位小数
     *
     * @return
     */
    public static String keep2Point(String str){
    	return String.format("%.2f",new BigDecimal(str).doubleValue());
    }
}
