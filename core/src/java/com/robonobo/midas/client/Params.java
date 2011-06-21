package com.robonobo.midas.client;

import com.google.protobuf.AbstractMessage;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.AbstractMessage.Builder;

public class Params {
	public enum Operation {
		Get, Put, Delete
	}
	Operation op;
	AbstractMessage.Builder<?> resultBldr;
	String url;
	Object obj;
	GeneratedMessage sendMsg;
	String username;
	String password;

	public Params(Operation op, GeneratedMessage sendMsg, Builder<?> resultBldr, String url, Object obj) {
		this.op = op;
		this.sendMsg = sendMsg;
		this.resultBldr = resultBldr;
		this.url = url;
		this.obj = obj;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("P[");
		sb.append(op).append(",").append(sendMsg).append(",").append(resultBldr).append(",").append(url).append(",").append(obj).append(",").append(username).append(",").append(password).append("]");
		return sb.toString();
	}
}
