package com.shendeng.utils;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class JsoupUtils {

	/**
	 * 通联TOKEN
	 */
	private static final String ACCESS_TOKEN = "c4c7c2025b642ba3970ac9208a7882dd448614277165fb16c3ae6d55f7dc3245";

	/**
	 * 获取url页面的数据
	 * 
	 * @param url
	 *            访问的url
	 * @return
	 * @throws IOException
	 */
	public static String getResourceText(String url) throws IOException {
		Connection conn = Jsoup.connect(url);
		conn.header("Authorization", "Bearer " + ACCESS_TOKEN);
		conn.maxBodySize(5242880);
		Document doc = conn.ignoreContentType(true).timeout(1000 * 5).get();
		return doc.text();
	}

	/**
	 * http下载
	 * 
	 * @param httpUrl
	 *            访问的url
	 * @param saveFile
	 *            存在的文件
	 */
	public static void httpDownload(String httpUrl, String saveFile) {
		// 下载网络文件
		int bytesum = 0;
		int byteread = 0;
		InputStream inStream = null;
		FileOutputStream fs = null;
		try {
			URL url = new URL(httpUrl);
			URLConnection conn = url.openConnection();
			conn.addRequestProperty("Authorization", "Bearer " + ACCESS_TOKEN);
			inStream = conn.getInputStream();
			fs = new FileOutputStream(saveFile);
			byte[] buffer = new byte[1204];
			while ((byteread = inStream.read(buffer)) != -1) {
				bytesum += byteread;
				fs.write(buffer, 0, byteread);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (inStream != null) {
					inStream.close();
				}
				if (fs != null) {
					fs.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	/**
	 * 访问url页面
	 * 
	 * @param httpUrl
	 *            访问的url
	 * @return 返回数据流
	 */
	public static InputStream httpDownload2(String httpUrl) {
		InputStream inStream = null;
		try {
			URL url = new URL(httpUrl);
			URLConnection conn = url.openConnection();
			conn.addRequestProperty("Authorization", "Bearer " + ACCESS_TOKEN);
			inStream = conn.getInputStream();
		} catch (IOException e) {
			if (inStream != null) {
				try {
					inStream.close();
				} catch (IOException e1) {
				}
			}
			e.printStackTrace();
		}
		return inStream;
	}

	public static void main(String[] args) throws Exception {
		long a = System.currentTimeMillis();
		InputStream is = httpDownload2("http://stockapi2.gp58.com:81/stockapi/stockupdownlist.htm?typeCode=up");
		System.out.println(System.currentTimeMillis() - a);
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println(sb);
	}
}
