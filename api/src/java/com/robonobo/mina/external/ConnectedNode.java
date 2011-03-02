package com.robonobo.mina.external;


public class ConnectedNode {
	public String nodeId;
	public String endPointUrl;
	public boolean supernode;
	public String appUri;
	public int uploadRate;
	public int downloadRate;
	public float myGamma;
	public float theirGamma;
	public double myBid;
	public double theirBid;

	public ConnectedNode() {
	}

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	public String getEndPointUrl() {
		return endPointUrl;
	}

	public void setEndPointUrl(String endPointUrl) {
		this.endPointUrl = endPointUrl;
	}

	public boolean isSupernode() {
		return supernode;
	}

	public void setSupernode(boolean supernode) {
		this.supernode = supernode;
	}

	public String getAppUri() {
		return appUri;
	}

	public void setAppUri(String appUri) {
		this.appUri = appUri;
	}

	public int getUploadRate() {
		return uploadRate;
	}

	public void setUploadRate(int uploadRate) {
		this.uploadRate = uploadRate;
	}

	public int getDownloadRate() {
		return downloadRate;
	}

	public void setDownloadRate(int downloadRate) {
		this.downloadRate = downloadRate;
	}

	public float getMyGamma() {
		return myGamma;
	}

	public void setMyGamma(float myGamma) {
		this.myGamma = myGamma;
	}

	public float getTheirGamma() {
		return theirGamma;
	}

	public void setTheirGamma(float theirGamma) {
		this.theirGamma = theirGamma;
	}

	public double getMyBid() {
		return myBid;
	}

	public void setMyBid(double myBid) {
		this.myBid = myBid;
	}

	public double getTheirBid() {
		return theirBid;
	}

	public void setTheirBid(double theirBid) {
		this.theirBid = theirBid;
	}
}
