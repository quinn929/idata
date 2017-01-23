package com.shendeng.utils;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class MyX509TrustManager implements X509TrustManager {
	private X509Certificate[] certificates;

	@Override
	public void checkClientTrusted(X509Certificate certificates[], String authType) throws CertificateException {
		if (this.certificates == null) {
			this.certificates = certificates;
		}
	}

	@Override
	public void checkServerTrusted(X509Certificate[] ax509certificate, String s) throws CertificateException {
		if (this.certificates == null) {
			this.certificates = ax509certificate;
		}

	}

	@Override
	public X509Certificate[] getAcceptedIssuers() {
		return null;
	}
}
