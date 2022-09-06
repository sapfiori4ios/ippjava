package com.neoprojectdemo;


import java.io.Closeable;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

public class SCCHttpClient implements Closeable {

    private static final String HEADER_CONNECTIVITY_CONSUMER_ACCOUNT = "SAP-Connectivity-ConsumerAccount";
    private static final String ENVIRONMENT_VARIABLE_HTTP_PROXY_PORT = "HC_OP_HTTP_PROXY_PORT";
    private static final String ENVIRONMENT_VARIABLE_HTTP_PROXY_HOST = "HC_OP_HTTP_PROXY_HOST";
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private CloseableHttpClient client;
    private String account;

    public SCCHttpClient(HttpClientBuilder builder, String account) {
        client = builder.setProxy(getProxyHost()).build();
        this.account = account;
    }

    public HttpResponse execute(HttpUriRequest request) throws IOException {
    	LOGGER.log(Level.SEVERE,"SUB ACCOUNT: " + account);
        request.setHeader(HEADER_CONNECTIVITY_CONSUMER_ACCOUNT, account);
        return client.execute(request);
    }

    @Override
    public void close() throws IOException {
        this.client.close();
    }

    private HttpHost getProxyHost() {
        String proxyHost = System.getenv(ENVIRONMENT_VARIABLE_HTTP_PROXY_HOST);
        int proxyPort = Integer.parseInt(System.getenv(ENVIRONMENT_VARIABLE_HTTP_PROXY_PORT));
        return new HttpHost(proxyHost, proxyPort);
    }
}