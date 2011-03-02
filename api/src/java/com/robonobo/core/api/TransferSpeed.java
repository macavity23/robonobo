/**
 * 
 */
package com.robonobo.core.api;

public class TransferSpeed {
	public String id;
	public int download;
	public int upload;
	
	public TransferSpeed(String id, int download, int upload) {
		this.id = id;
		this.download = download;
		this.upload = upload;
	}

	public String getId() {
		return id;
	}

	public int getDownload() {
		return download;
	}

	public int getUpload() {
		return upload;
	}
	
	@Override
	public String toString() {
		return "[id="+id+",dl="+download+",ul="+upload+"]";
	}
}