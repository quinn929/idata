package com.shendeng.utils;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Date;

import javax.net.ssl.HttpsURLConnection;

import org.apache.http.impl.client.CloseableHttpClient;

import com.csvreader.CsvReader;

/**
 * 通联接口操作工具类
 * 
 * @author naxj
 * 
 */
public class WmcloudUtil {
	private static CloseableHttpClient httpClient = HttpUtil.createHttpsClient();
	/**
	 * 通联cvs接口数据读取
	 * 
	 * @param urlStr
	 *            url接口地址
	 * @return cvs数据流
	 * @throws FileNotFoundException 
	 */
	public static CsvReader url2csvWithGzip(String urlStr) throws FileNotFoundException {
		CsvReader reader = null;
		String body = HttpsUtil.doGetWithGzip(urlStr);
		if (body!=null){
			reader = new CsvReader(new ByteArrayInputStream(body.getBytes()),Charset.forName("GBK"));
		}
		
		return reader;
	}
	/**
	 * 通联cvs接口数据读取
	 * 
	 * @param urlStr
	 *            url接口地址
	 * @return cvs数据流
	 */
	public static CsvReader url2csv(String urlStr) {
		CsvReader reader = null;
		URL url = null;
		HttpsURLConnection conn = null;
		InputStream inputStream = null;
		try {
//			 // 创建SSLContext对象，并使用我们指定的信任管理器初始化 
//            TrustManager[] tm = { new MyX509TrustManager() }; 
//            SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE"); 
//            sslContext.init(null, tm, new java.security.SecureRandom()); 
//            // 从上述SSLContext对象中得到SSLSocketFactory对象 
//            SSLSocketFactory ssf = sslContext.getSocketFactory(); 
			url = new URL(urlStr);
			conn = (HttpsURLConnection) url.openConnection();
//			conn.setSSLSocketFactory(ssf);
			conn.setConnectTimeout(30 * 1000);
			conn.addRequestProperty("Authorization", "Bearer " + SystemConfig.getInstance().WMCLOUD_CONFIG.getToken());
//			conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
			// 得到输入流
			if (conn !=null && (conn.getResponseCode() == HttpURLConnection.HTTP_OK || conn.getResponseCode() == HttpURLConnection.HTTP_CREATED || conn.getResponseCode() == HttpURLConnection.HTTP_ACCEPTED)) {
				inputStream = conn.getInputStream();
				reader = new CsvReader(inputStream, Charset.forName("GBK"));
			}
//			conn.disconnect();
//			inputStream.close();
		} catch (Exception e) {
			try {
				url = new URL(urlStr);
				conn = (HttpsURLConnection) url.openConnection();
				conn.setConnectTimeout(30 * 1000);
				conn.addRequestProperty("Authorization", "Bearer " + SystemConfig.getInstance().WMCLOUD_CONFIG.getToken());
				conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
				// 得到输入流
				if (conn !=null && (conn.getResponseCode() == HttpURLConnection.HTTP_OK || conn.getResponseCode() == HttpURLConnection.HTTP_CREATED || conn.getResponseCode() == HttpURLConnection.HTTP_ACCEPTED)) {
					inputStream = conn.getInputStream();
					reader = new CsvReader(inputStream, Charset.forName("GBK"));
				}
//				inputStream.close();
			} catch (Exception e1) {
				System.out.println("CsvReader第二次读取数据异常"+ DateTool.DateToStr(new Date(), DateTool.TIME_FORMAT));
				e.printStackTrace();
			}
			new Exception("CsvReader读取数据异常" + DateTool.DateToStr(new Date(), DateTool.TIME_FORMAT)).printStackTrace();
			e.printStackTrace();
		}
		return reader;
	}

	/**
	 * 关闭数据连接
	 * 
	 * @param reader
	 */
	public static void closeCsv(CsvReader reader) {
		reader.close();
	}

	/**
	 * 通联json接口数据读取
	 * 
	 * @param urlStr
	 *            url接口地址
	 * @return json数据流
	 */
	public static CsvReader url2json(String urlStr) {
		CsvReader reader = null;
		URL url = null;
		try {
			url = new URL(urlStr);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(20 * 1000);
			conn.addRequestProperty("Authorization", "Bearer " + SystemConfig.getInstance().WMCLOUD_CONFIG.getToken());
			conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
			// 得到输入流
			InputStream inputStream = conn.getInputStream();
			reader = new CsvReader(inputStream, Charset.forName("GBK"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return reader;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
