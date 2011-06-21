package com.robonobo.common.http;

import java.io.IOException;

import org.apache.http.*;
import org.apache.http.auth.*;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.*;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.*;

/**
 * Extends the commons httpclient to do preemptive basic auth by default. To do preemptive auth:
 * <ol>
 * <li>Call getCredentialsProvider().setCredentials() with your host and credentials</li>
 * <li>In your calls to execute(), make sure you pass an httpcontext that was created with newContext()</li>
 * </ol>
 * 
 * @author macavity
 * 
 */
public class PreemptiveHttpClient extends DefaultHttpClient {
	public static final String PREEMPTIVE_AUTH = "preemptive-auth";

	public PreemptiveHttpClient() {
		super();
		addInterceptor();
	}

	public PreemptiveHttpClient(ClientConnectionManager conman, HttpParams params) {
		super(conman, params);
		addInterceptor();
	}

	public PreemptiveHttpClient(ClientConnectionManager conman) {
		super(conman);
		addInterceptor();
	}

	public PreemptiveHttpClient(HttpParams params) {
		super(params);
		addInterceptor();
	}

	private void addInterceptor() {
		addRequestInterceptor(new PreemptiveAuthInterceptor());
	}

	public HttpContext newPreemptiveContext(HttpHost host) {
		AuthCache authCache = new BasicAuthCache();
		BasicScheme basicAuth = new BasicScheme();
		authCache.put(host, basicAuth);
		BasicHttpContext newContext = new BasicHttpContext();
		newContext.setAttribute(ClientContext.AUTH_CACHE, authCache);
		newContext.setAttribute(PREEMPTIVE_AUTH, basicAuth);
		return newContext;
	}

	class PreemptiveAuthInterceptor implements HttpRequestInterceptor {
		public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException {
			AuthState authState = (AuthState) context.getAttribute(ClientContext.TARGET_AUTH_STATE);
			// If no auth scheme avaialble yet, try to initialize it preemptively
			if (authState.getAuthScheme() == null) {
				AuthScheme authScheme = (AuthScheme) context.getAttribute(PREEMPTIVE_AUTH);
				CredentialsProvider credsProvider = (CredentialsProvider) context.getAttribute(ClientContext.CREDS_PROVIDER);
				HttpHost targetHost = (HttpHost) context.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
				if (authScheme != null) {
					Credentials creds = credsProvider.getCredentials(new AuthScope(targetHost.getHostName(), targetHost.getPort()));
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
