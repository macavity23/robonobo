package com.robonobo.wang.client;

import static com.robonobo.common.util.TextUtil.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.*;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.methods.*;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import com.google.protobuf.AbstractMessage;
import com.google.protobuf.GeneratedMessage;
import com.robonobo.common.exceptions.Errot;
import com.robonobo.wang.WangServerException;
import com.robonobo.wang.proto.WangProtocol.BalanceMsg;
import com.robonobo.wang.proto.WangProtocol.BlindedCoinListMsg;
import com.robonobo.wang.proto.WangProtocol.CoinListMsg;
import com.robonobo.wang.proto.WangProtocol.CoinRequestListMsg;
import com.robonobo.wang.proto.WangProtocol.DenominationListMsg;
import com.robonobo.wang.proto.WangProtocol.DepositStatusMsg;

public class BankFacade {
	static final Pattern URL_PATTERN = Pattern.compile("^http://([\\w\\.]+?):?(\\d*)/.*$");
	private String baseUrl;
	private DefaultHttpClient client;
	Log log = LogFactory.getLog(getClass());
	HttpContext context;

	public BankFacade(String baseUrl, String userEmail, String password, DefaultHttpClient client) {
		this.baseUrl = baseUrl;
		this.client = client;
		if (!baseUrl.endsWith("/"))
			baseUrl += "/";
		Matcher m = URL_PATTERN.matcher(baseUrl);
		if (!m.matches())
			throw new Errot("bank url " + baseUrl + "does not match expected pattern");
		String bankHost = m.group(1);
		String portStr = m.group(2);
		int bankPort;
		if (isEmpty(portStr))
			bankPort = 80;
		else
			bankPort = Integer.parseInt(portStr);
		log.debug("Setting up bank http auth scope on " + bankHost + ":" + bankPort);
		AuthScope bankAuthScope = new AuthScope(bankHost, bankPort);
		client.getCredentialsProvider().setCredentials(bankAuthScope, new UsernamePasswordCredentials(userEmail, password));
		AuthCache authCache = new BasicAuthCache();
		BasicScheme basicAuth = new BasicScheme();
		authCache.put(new HttpHost(bankHost, bankPort), basicAuth);
		context = new BasicHttpContext();
		context.setAttribute(ClientContext.AUTH_CACHE, authCache);
	}

	public DenominationListMsg getDenominations() throws WangServerException {
		DenominationListMsg.Builder bldr = DenominationListMsg.newBuilder();
		makeRequest(null, bldr, baseUrl + "getDenominations");
		return bldr.build();
	}

	public double getBalance() throws WangServerException {
		BalanceMsg.Builder bldr = BalanceMsg.newBuilder();
		makeRequest(null, bldr, baseUrl + "getBalance");
		return bldr.getAmount();
	}

	public BlindedCoinListMsg getCoins(CoinRequestListMsg requestList) throws WangServerException {
		BlindedCoinListMsg.Builder bldr = BlindedCoinListMsg.newBuilder();
		makeRequest(requestList, bldr, baseUrl + "getCoins");
		return bldr.build();
	}

	public DepositStatusMsg depositCoins(CoinListMsg coinList) throws WangServerException {
		DepositStatusMsg.Builder bldr = DepositStatusMsg.newBuilder();
		makeRequest(coinList, bldr, baseUrl + "depositCoins");
		return bldr.build();
	}

	@SuppressWarnings("unchecked")
	protected void makeRequest(GeneratedMessage toSend, AbstractMessage.Builder builder, String url) throws WangServerException {
		try {
			HttpRequestBase request;
			if (toSend == null)
				request = new HttpGet(url);
			else {
				HttpPut put = new HttpPut(url);
				put.setEntity(new ByteArrayEntity(toSend.toByteArray()));
				request = put;
			}
			HttpEntity body = null;
			try {
				HttpResponse resp = client.execute(request, context);
				body = resp.getEntity();
				int statusCode = resp.getStatusLine().getStatusCode();
				if (statusCode != 200)
					throw new IOException("Server replied with status: " + statusCode);
				InputStream is = body.getContent();
				try {
					builder.mergeFrom(is);
				} finally {
					is.close();
				}
			} finally {
				if (body != null)
					EntityUtils.consume(body);
			}
		} catch (IOException e) {
			throw new WangServerException(e);
		}
	}
}
