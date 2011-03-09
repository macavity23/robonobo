package com.robonobo.mina.message;

import com.robonobo.core.api.proto.CoreApi.EndPoint;

/** Utility methods - here rather than in the messages themselves as they are generated classes */
public class MessageUtil {
	private MessageUtil() {
	}
	
	public static String protocolName(EndPoint ep) {
		String url = ep.getUrl();
		return url.substring(url.indexOf(':'));
	}
}
