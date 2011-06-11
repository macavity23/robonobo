package com.robonobo.core.service;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.*;
import org.apache.http.auth.*;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.*;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import com.robonobo.core.api.proto.CoreApi.UpdateMsg;

public class HttpService extends AbstractService {
	private DefaultHttpClient client;

	public HttpService() {
	}
	
	@Override
	public String getName() {
		return "Http Service";
	}

	@Override
	public String getProvides() {
		return "core.http";
	}

	@Override
	public void startup() throws Exception {
		ThreadSafeClientConnManager connMgr = new ThreadSafeClientConnManager();
		connMgr.setMaxTotal(64);
		connMgr.setDefaultMaxPerRoute(rbnb.getConfig().getMidasThreadPoolSize());
		HttpParams params = new BasicHttpParams();
		long timeout = rbnb.getConfig().getHttpTimeout();
		params.setLongParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, timeout);
		params.setLongParameter(CoreConnectionPNames.SO_TIMEOUT, timeout);
		client = new DefaultHttpClient(connMgr);
		client.addRequestInterceptor(new PreemptiveAuthInterceptor());
	}

	@Override
	public void shutdown() throws Exception {
		client.getConnectionManager().shutdown();
	}
	
	public DefaultHttpClient getClient() {
		return client;
	}
	
	
	// Bunged these two methods in here as I can't find anywhere better
	public void requestTopUp() throws IOException {
		HttpGet get = new HttpGet(rbnb.getConfig().getTopUpUrl());
		HttpEntity body = null;
		try {
			HttpResponse resp = client.execute(get);
			body = resp.getEntity();
			int statusCode = resp.getStatusLine().getStatusCode();
			if(statusCode != 200)
				log.error("Error requesting topup, server said: "+statusCode);
		} finally {
			if(body != null)
				EntityUtils.consume(body);
		}
	}

	/**
	 * @return The update message, or an empty string if there is no message
	 */
	public String getUpdateMessage() throws IOException {
		String checkUrl = rbnb.getConfig().getWebsiteUrlBase() + "checkupdate?version="+rbnb.getVersion();
		UpdateMsg.Builder ub = UpdateMsg.newBuilder();
		HttpGet get = new HttpGet(checkUrl);
		HttpEntity body = null;
		try {
			HttpResponse resp = client.execute(get);
			body = resp.getEntity();
			int statusCode = resp.getStatusLine().getStatusCode();
			if(statusCode == 200) {
				InputStream is = body.getContent();
				try {
					ub.mergeFrom(is);
					return ub.build().getUpdateHtml();
				} finally {
					is.close();
				}				
			} else
				throw new IOException("Error getting update msg from " + checkUrl + " - server replied with status " + statusCode);
		} finally {
			if (body != null)
				EntityUtils.consume(body);
		}		
	}

	class PreemptiveAuthInterceptor implements HttpRequestInterceptor {

        public void process(
                final HttpRequest request, 
                final HttpContext context) throws HttpException, IOException {
            
            AuthState authState = (AuthState) context.getAttribute(
                    ClientContext.TARGET_AUTH_STATE);
            
            // If no auth scheme avaialble yet, try to initialize it preemptively
            if (authState.getAuthScheme() == null) {
                AuthScheme authScheme = (AuthScheme) context.getAttribute(
                        "preemptive-auth");
                CredentialsProvider credsProvider = (CredentialsProvider) context.getAttribute(
                        ClientContext.CREDS_PROVIDER);
                HttpHost targetHost = (HttpHost) context.getAttribute(
                        ExecutionContext.HTTP_TARGET_HOST);
                if (authScheme != null) {
                    Credentials creds = credsProvider.getCredentials(
                            new AuthScope(
                                    targetHost.getHostName(), 
                                    targetHost.getPort()));
                    if (creds == null) {
                        throw new HttpException("No credentials for preemptive authentication");
                    }
                    authState.setAuthScheme(authScheme);
                    authState.setCredentials(creds);
                }
            }
            
        }
        
    }
}
