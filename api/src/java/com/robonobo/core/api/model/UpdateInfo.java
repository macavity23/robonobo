package com.robonobo.core.api.model;

import com.robonobo.core.api.proto.CoreApi.UpdateMsg;

public class UpdateInfo {
	String updateTitle;
	String updateHtml;

	public UpdateInfo(String updateTitle, String updateHtml) {
		this.updateTitle = updateTitle;
		this.updateHtml = updateHtml;
	}

	public UpdateInfo(UpdateMsg msg) {
		updateHtml = msg.getUpdateHtml();
		if (msg.hasUpdateTitle())
			updateTitle = msg.getUpdateTitle();
		else
			updateTitle = "A new version is available";
	}

	public UpdateMsg toMsg() {
		UpdateMsg.Builder bldr = UpdateMsg.newBuilder();
		bldr.setUpdateHtml(updateHtml);
		if (updateTitle != null)
			bldr.setUpdateTitle(updateTitle);
		return bldr.build();
	}

	public String getUpdateTitle() {
		return updateTitle;
	}

	public void setUpdateTitle(String updateTitle) {
		this.updateTitle = updateTitle;
	}

	public String getUpdateHtml() {
		return updateHtml;
	}

	public void setUpdateHtml(String updateHtml) {
		this.updateHtml = updateHtml;
	}
}
