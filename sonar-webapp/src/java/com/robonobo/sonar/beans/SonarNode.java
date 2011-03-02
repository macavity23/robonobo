package com.robonobo.sonar.beans;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.robonobo.core.api.proto.CoreApi.EndPoint;
import com.robonobo.core.api.proto.CoreApi.Node;

/** Hibernate wrapper class around CoreApi.Node */
public class SonarNode {
	private String id;
	private String applicationUri;
	private Date lastSeen;
	private int protocolVersion;
	private boolean supernode;
	private Set<SonarEndPoint> endPoints = new HashSet<SonarEndPoint>();
	
	public SonarNode() {
	}
	
	public SonarNode(Node nodeMsg) {
		id = nodeMsg.getId();
		applicationUri = nodeMsg.getApplicationUri();
		protocolVersion = nodeMsg.getProtocolVersion();
		supernode = nodeMsg.getSupernode();
		for (EndPoint epMsg : nodeMsg.getEndPointList()) {
			SonarEndPoint ep = new SonarEndPoint(epMsg);
			ep.setNode(this);
			endPoints.add(ep);
		}
	}
	
	public Node toMsg() {
		Node.Builder bldr = Node.newBuilder();
		bldr.setId(id);
		bldr.setApplicationUri(applicationUri);
		bldr.setProtocolVersion(protocolVersion);
		bldr.setSupernode(supernode);
		for (SonarEndPoint ep : endPoints) {
			bldr.addEndPoint(ep.toMsg());
		}
		return bldr.build();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getApplicationUri() {
		return applicationUri;
	}

	public void setApplicationUri(String applicationUri) {
		this.applicationUri = applicationUri;
	}

	public Date getLastSeen() {
		return lastSeen;
	}

	public void setLastSeen(Date lastSeen) {
		this.lastSeen = lastSeen;
	}

	public int getProtocolVersion() {
		return protocolVersion;
	}

	public void setProtocolVersion(int protocolVersion) {
		this.protocolVersion = protocolVersion;
	}

	public boolean isSupernode() {
		return supernode;
	}

	public void setSupernode(boolean supernode) {
		this.supernode = supernode;
	}

	public Set<SonarEndPoint> getEndPoints() {
		return endPoints;
	}

	public void setEndPoints(Set<SonarEndPoint> sonarEndPoints) {
		this.endPoints = sonarEndPoints;
	}
}
