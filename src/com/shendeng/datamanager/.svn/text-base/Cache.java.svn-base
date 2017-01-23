package com.shendeng.datamanager;

import java.util.ArrayList;
import java.util.List;

import com.shendeng.utils.Const;
import com.shendeng.utils.StockUtils;

/**
 * 使用内存的缓存
 * 
 * @author naxj
 * @date 2016-12-15
 */
public class Cache {
	/**
	 * 股票编码列表
	 */
	private static List<String> STOCK_CODES = new ArrayList<String>();

	/**
	 * 
	 * @param items
	 *            通联stockinfo接口返回cvs中的一行
	 */
	public static void cacheStockListAdd(String[] items) {
		String code = items[Const.INFO_ticker];
		// 只加入沪深A股
		if (StockUtils.isAstock(code)) {
			STOCK_CODES.add(code);
		}
	}

	public static void cacheStockListRemoveAll() {
		STOCK_CODES.clear();
	}

	public static List<String> getStockCodes() {
		return STOCK_CODES;
	}

	public static List<List<String>> getStockCodeSplit(int splitSize) {
		return spliceArrays(STOCK_CODES,splitSize);
	}

	/**
	 * 拆分集合
	 * @param datas
	 * @param splitSize
	 * @param <T>
	 * @return
	 */
	private static <T> List<List<T>> spliceArrays(List<T> datas, int splitSize) {
	    if (datas == null || splitSize < 1) {
	        return  null;
	    }
	    int totalSize = datas.size();
	    int count = (totalSize % splitSize == 0) ?
	            (totalSize / splitSize) : (totalSize/splitSize+1);
	 
	    List<List<T>> rows = new ArrayList<>();
	 
	    for (int i = 0; i < count;i++) {
	 
	        List<T> cols = datas.subList(i * splitSize,
	                (i == count - 1) ? totalSize : splitSize * (i + 1));
	        rows.add(cols);
//	        System.out.println(cols);
	    }
	    return rows;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
