package com.shendeng.utils;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.codec.EncoderException;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

public class HttpUtil {
	/**
	 * 创建http client
	 */
	private static CloseableHttpClient httpClient = createHttpsClient();
	/**
	 * 通联TOKEN
	 */
	private static final String ACCESS_TOKEN = "c4c7c2025b642ba3970ac9208a7882dd448614277165fb16c3ae6d55f7dc3245";

	/**
	 * 创建http client
	 * 
	 * @return
	 */
	public static CloseableHttpClient createHttpsClient() {
		X509TrustManager x509mgr = new X509TrustManager() {
			@Override
			public void checkClientTrusted(X509Certificate[] xcs, String string) {
			}

			@Override
			public void checkServerTrusted(X509Certificate[] xcs, String string) {
			}

			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}
		};
		// 因为java客户端要进行安全证书的认证，这里我们设置ALLOW_ALL_HOSTNAME_VERIFIER来跳过认证，否则将报错
		SSLConnectionSocketFactory sslsf = null;
		try {
			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(null, new TrustManager[] { x509mgr }, null);
			sslsf = new SSLConnectionSocketFactory(sslContext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		} catch (KeyManagementException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(50000).setConnectionRequestTimeout(10000).setSocketTimeout(50000).build();
//		Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create().register("http", PlainConnectionSocketFactory.INSTANCE).register("https", socketFactory).build();
//		PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
//		CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(connectionManager).setDefaultRequestConfig(requestConfig).build();
		return HttpClients.custom().setSSLSocketFactory(sslsf).setDefaultRequestConfig(requestConfig).build();
	}

	/**
	 * 查询通联数据
	 * 
	 * @param url
	 *            接口url
	 * @return
	 */
	public static String excute(String url) {
		String body = "";
		CloseableHttpResponse response = null;
		HttpGet httpGet = new HttpGet(url);
		try {
			// 在header里加入 Bearer {token}，添加认证的token，并执行get请求获取json数据
			httpGet.addHeader("Authorization", "Bearer " + ACCESS_TOKEN);
			response = httpClient.execute(httpGet);
			HttpEntity entity = response.getEntity();
			body = EntityUtils.toString(entity);
			httpGet.abort();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				response.close();
				httpGet.abort();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return body;
	}

	public static void main(String[] args) throws IOException, EncoderException {
		// 根据api store页面上实际的api url来发送get请求，获取数据
		String url = "https://api.wmcloud.com:443/data/v1/api/market/getTickRTSnapshot.csv?securityID=&assetClass=E";
		String body = excute(url);
		System.out.println(body);
	}
}