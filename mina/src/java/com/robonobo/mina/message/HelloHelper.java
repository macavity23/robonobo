package com.robonobo.mina.message;

import com.robonobo.common.async.PushDataChannel;
import com.robonobo.common.io.ByteBufferInputStream;
import com.robonobo.core.api.proto.CoreApi.EndPoint;
import com.robonobo.mina.message.proto.MinaProtocol.Hello;

/**
 * A little wrapper class that contains a hello msg and some additional info
 */
public class HelloHelper {
	private Hello hello;
	private PushDataChannel dataChannel;
	private EndPoint myEp;
	private EndPoint theirEp;
	private ByteBufferInputStream incoming;

	public HelloHelper(Hello hello, PushDataChannel dataChannel, EndPoint myEp, EndPoint theirEp, ByteBufferInputStream incoming) {
		this.hello = hello;
		this.dataChannel = dataChannel;
		this.myEp = myEp;
		this.theirEp = theirEp;
		this.incoming = incoming;
	}

	public Hello getHello() {
		return hello;
	}

	public EndPoint getMyEp() {
		return myEp;
	}

	public EndPoint getTheirEp() {
		return theirEp;
	}

	public PushDataChannel getDataChannel() {
		return dataChannel;
	}
	
	public ByteBufferInputStream getIncoming() {
		return incoming;
	}
}
