package com.robonobo.sonar.beans;

import com.robonobo.core.api.proto.CoreApi.EndPoint;

/** Hibernate wrapper class around CoreApi.EndPoint */
public class SonarEndPoint {
	private long id;
	private String url;
	private SonarNode node;

	public SonarEndPoint() {
	}

	public SonarEndPoint(EndPoint epMsg) {
		url = epMsg.getUrl();
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public EndPoint toMsg() {
		return EndPoint.newBuilder().setUrl(url).build();
	}

	public SonarNode getNode() {
		return node;
	}

	public void setNode(SonarNode node) {
		this.node = node;
	}
}
