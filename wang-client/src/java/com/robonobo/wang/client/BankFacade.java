package com.robonobo.wang.client;

import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PutMethod;

import com.google.protobuf.AbstractMessage;
import com.google.protobuf.GeneratedMessage;
import com.robonobo.wang.WangServerException;
import com.robonobo.wang.proto.WangProtocol.BalanceMsg;
import com.robonobo.wang.proto.WangProtocol.BlindedCoinListMsg;
import com.robonobo.wang.proto.WangProtocol.CoinListMsg;
import com.robonobo.wang.proto.WangProtocol.CoinRequestListMsg;
import com.robonobo.wang.proto.WangProtocol.DenominationListMsg;
import com.robonobo.wang.proto.WangProtocol.DepositStatusMsg;

public class BankFacade {
	private static final int HTTP_TIMEOUT_MS = 30000;
	private String baseUrl;
	private HttpClient client;

	public BankFacade(String baseUrl, String userEmail, String password) {
		this.baseUrl = baseUrl;
		if(!baseUrl.endsWith("/"))
			baseUrl += "/";
		client = new HttpClient(new MultiThreadedHttpConnectionManager());
		client.getParams().setSoTimeout(HTTP_TIMEOUT_MS);
		client.getState().setAuthenticationPreemptive(true);
		client.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(userEmail, password));
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
		HttpMethod method;
		if (toSend == null)
			method = new GetMethod(url);
		else {
			method = new PutMethod(url);
			((PutMethod)method).setRequestEntity(new ByteArrayRequestEntity(toSend.toByteArray()));
		}
		int statusCode;
		try {
			statusCode = client.executeMethod(method);
			if(statusCode != HttpStatus.SC_OK)
				throw new WangServerException("Server replied with status: "+statusCode);
			builder.mergeFrom(method.getResponseBody());
		} catch (HttpException e) {
			throw new WangServerException(e);
		} catch (IOException e) {
			throw new WangServerException(e);
		} finally {
			if(method != null)
				method.releaseConnection();
		}
	}
}
