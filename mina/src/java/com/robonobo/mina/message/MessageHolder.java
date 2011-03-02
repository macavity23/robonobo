package com.robonobo.mina.message;

import java.util.Date;

import com.google.protobuf.GeneratedMessage;
import com.robonobo.mina.network.ControlConnection;

public class MessageHolder {
	private String msgName;
	private GeneratedMessage msg;
	private ControlConnection fromCC;
	private Date receivedAt;

	public MessageHolder(String msgName, GeneratedMessage msg, ControlConnection fromCC, Date receivedAt) {
		this.msgName = msgName;
		this.msg = msg;
		this.fromCC = fromCC;
		this.receivedAt = receivedAt;
	}

	public String getMsgName() {
		return msgName;
	}

	public GeneratedMessage getMessage() {
		return msg;
	}

	public ControlConnection getFromCC() {
		return fromCC;
	}

	public Date getReceivedAt() {
		return receivedAt;
	}
}
