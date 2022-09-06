package com.neoprojectdemo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;

public class IppPrintApp {

	@SuppressWarnings("deprecation")
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		URL url = null;
		try {
			url = new URL("http://mfes300705.eu.merckgroup.com");
		} catch (MalformedURLException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		URI uri = null;
		try {
			uri = url.toURI();
		} catch (URISyntaxException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		HttpPost httpPost = new HttpPost(url.toString());
//		httpPost.addHeader("Content-Length", "512");
		httpPost.setHeader("Content-Type", "application/ipp");
//		
//		httpPost.setHeader("Content-Length", "0");
		String httpScheme = uri.getScheme().replace("ipp", "http");
		URI httpUri = URI.create(String.format("%s:%s", httpScheme, uri.getSchemeSpecificPart()));
		ByteArrayOutputStream out1 = new ByteArrayOutputStream();
		try {
			Writer writer = new OutputStreamWriter(out1, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		// writeXml(writer);
		String proxyHost = "127.0.0.1";
		int proxyPort = 1089;
		httpPost.setEntity(new ByteArrayEntity(out1.toByteArray()));
		HttpHost proxy = new HttpHost(proxyHost, proxyPort);
		// DefaultHttpClient httpClient = new DefaultHttpClient();
//		CloseableHttpClient httpClient = HttpClients.createDefault();
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		try {
			HttpResponse response = httpClient.execute(httpPost);
//			response.setHeader("Content-Length", "0");
			int statusCode = response.getStatusLine().getStatusCode();
			
			//
			if (statusCode != HttpStatus.SC_OK) {
				throw new IOException("Backend system returned unexpected status code: " + statusCode);
			}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
