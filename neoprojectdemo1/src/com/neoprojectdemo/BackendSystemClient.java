package com.neoprojectdemo;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

import com.sap.core.connectivity.api.configuration.ConnectivityConfiguration;
import com.sap.core.connectivity.api.configuration.DestinationConfiguration;
import com.sun.jndi.toolkit.url.Uri;

import java.util.logging.Level;
import java.util.logging.Logger;

public class BackendSystemClient implements Closeable {

	private String destinationName;
	private SCCHttpClient client;
	private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

	public BackendSystemClient(SCCHttpClient client, String destinationName,URI uri) {
		this.client = client;
		this.destinationName = destinationName;
		this.uri = uri;
	}

	public void post(String content) throws IOException {
		HttpResponse response = client.execute(buildPostRequest(getDestinationUrl(), content));
		int statusCode = response.getStatusLine().getStatusCode();
		LOGGER.log(Level.SEVERE, "POST CONTENT: " + content);
		LOGGER.log(Level.SEVERE, "POST URL STATUS CODE : " + statusCode);
		if (statusCode != HttpStatus.SC_OK) {	
			throw new IOException("Backend system returned unexpected status code: " + statusCode);
		}
	}

	private URI uri;
	private DataOutputStream dataOutputStream;
	private DataInputStream dataInputStream;

	private HttpPost buildPostRequest(String url, String body) throws IOException {
		LOGGER.log(Level.SEVERE, "POST URL : " + url);
		HttpPost httpPost = new HttpPost(url);
		httpPost.setHeader("Content-Type", "application/ipp");
		String httpScheme = uri.getScheme().replace("ipp", "http");
		URI httpUri = URI.create(String.format("%s:%s", httpScheme, uri.getSchemeSpecificPart()));
		// HttpURLConnection httpUrlConnection = (HttpURLConnection)
		// httpUri.toURL().openConnection();
		// httpUrlConnection.setConnectTimeout(5000);
		// httpUrlConnection.setDoOutput(true);
		// httpUrlConnection.setRequestProperty("Content-Type",
		// "application/ipp");
		// rfc 8010 syntax of encoding:
		// https://tools.ietf.org/html/rfc8010#page-15
		OutputStream out = new OutputStream() {
			
			@Override
			public void write(int b) throws IOException {
				// TODO Auto-generated method stub
				
			}
		};
		dataOutputStream = new DataOutputStream(out);
		dataOutputStream.writeShort(0x0101); // ipp version 1.1
		// operation -> https://tools.ietf.org/html/rfc8011#section-5.4.15
		dataOutputStream.writeShort(0x0002); // operation Print-Job
		dataOutputStream.writeInt(0x0001); // request id
		dataOutputStream.writeByte(0x01); // operation group tag
		writeAttribute(0x47, "attributes-charset", "us-ascii"); // charset tag
		writeAttribute(0x48, "attributes-natural-language", "en"); // natural-language
																	// tag
		writeAttribute(0x45, "printer-uri", uri.toString()); // uri tag
		dataOutputStream.writeByte(0x03); // end tag
		ByteArrayOutputStream out1 = new ByteArrayOutputStream();
		Writer writer = new OutputStreamWriter(out1, "UTF-8");
		//writeXml(writer);
		httpPost.setEntity(new ByteArrayEntity(out1.toByteArray()));
//		httpPost.setEntity(new StringEntity(dataOutputStream.toString()));
		// httpPost.setHeader(HttpHeaders.CONTENT_TYPE,
		// ContentType.APPLICATION_JSON.toString());
		return httpPost;
	}
	private void writeAttribute(final Integer tag, final String name, final String value) throws IOException {
	    dataOutputStream.writeByte(tag);
	    dataOutputStream.writeShort(name.length());
	    dataOutputStream.write(name.getBytes());
	    dataOutputStream.writeShort(value.length());
	    dataOutputStream.write(value.getBytes());
	}

	private String getDestinationUrl() throws IOException {
		try {
			Context ctx = new InitialContext();
			ConnectivityConfiguration configuration = (ConnectivityConfiguration) ctx
					.lookup("java:comp/env/connectivityConfiguration");
			DestinationConfiguration destinationConfiguration = configuration.getConfiguration(destinationName);
			if (destinationConfiguration == null) {
				throw new IOException("Count not find destination with name: " + destinationName);
			}

			return destinationConfiguration.getProperty("URL");
		} catch (NamingException e) {
			throw new IOException("Naming exception occured while constructing context object", e);
		}
	}

	@Override
	public void close() throws IOException {
		client.close();
	}
}
