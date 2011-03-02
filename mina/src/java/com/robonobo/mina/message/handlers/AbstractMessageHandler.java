package com.robonobo.mina.message.handlers;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;

import com.google.protobuf.GeneratedMessage;
import com.robonobo.mina.instance.MinaInstance;
import com.robonobo.mina.message.MessageHolder;

public abstract class AbstractMessageHandler implements MessageHandler {
	protected MinaInstance mina;
	protected Log log;
	
	public AbstractMessageHandler() {
	}
	
	public abstract void handleMessage(MessageHolder mh);

	public abstract GeneratedMessage parse(String cmdName, InputStream is) throws IOException;

	public void setMina(MinaInstance mina) {
		this.mina = mina;
		log = mina.getLogger(getClass());
	}

}
