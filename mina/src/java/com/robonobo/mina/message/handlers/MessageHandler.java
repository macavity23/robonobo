package com.robonobo.mina.message.handlers;

import java.io.IOException;
import java.io.InputStream;

import com.google.protobuf.GeneratedMessage;
import com.robonobo.mina.instance.MinaInstance;
import com.robonobo.mina.message.MessageHolder;

public interface MessageHandler {
	public GeneratedMessage parse(String cmdName, InputStream is) throws IOException;
	public void handleMessage(MessageHolder msgHolder);
	public void setMina(MinaInstance mina);
}
