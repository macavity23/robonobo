package com.robonobo.core.api.model;

import com.robonobo.core.api.proto.CoreApi.StreamAttributeMsg;

/**
 * This is in a separate class as Hibernate Search/Lucene doesn't appear to be
 * able to index maps as properties?
 * 
 * @author macavity
 * 
 */
public class StreamAttribute {
	/** For orm only */
	private long id;
	private Stream stream;
	private String name;
	private String value;

	public StreamAttribute(Stream stream, String name, String value) {
		setStream(stream);
		setName(name);
		setValue(value);
	}

	public StreamAttribute(StreamAttributeMsg msg) {
		setName(msg.getName());
		setValue(msg.getValue());
	}

	public StreamAttribute() {
	}

	public StreamAttributeMsg toMsg() {
		return StreamAttributeMsg.newBuilder().setName(name).setValue(value).build();
	}

	public String toString() {
		return "[attr:" + name + "=" + value + "]";
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Stream getStream() {
		return stream;
	}

	public void setStream(Stream stream) {
		this.stream = stream;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = truncate(name, 256);
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = truncate(value, 256);
	}

	private String truncate(String str, int maxLen) {
		if (str.length() <= maxLen)
			return str;
		return str.substring(0, maxLen);
	}
}
