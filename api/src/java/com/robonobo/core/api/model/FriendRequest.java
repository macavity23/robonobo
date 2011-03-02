package com.robonobo.core.api.model;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.robonobo.core.api.proto.CoreApi.FriendRequestMsg;

public class FriendRequest {
	long friendRequestId;
	long requestorId;
	long requesteeId;
	String requestCode;
	Set<Long> playlistIds = new HashSet<Long>();
	Date updated;

	public FriendRequest() {
	}

	public FriendRequest(FriendRequestMsg msg) {
		friendRequestId = msg.getFriendRequestId();
		requestorId = msg.getRequestorId();
		requesteeId = msg.getRequesteeId();
		requestCode = msg.getRequestCode();
		playlistIds.addAll(msg.getPlaylistIdList());
		if(msg.hasUpdatedDate())
			updated = new Date(msg.getUpdatedDate());
	}
	
	public FriendRequestMsg toMsg() {
		FriendRequestMsg.Builder b = FriendRequestMsg.newBuilder();
		b.setFriendRequestId(friendRequestId);
		b.setRequestorId(requestorId);
		b.setRequesteeId(requesteeId);
		b.setRequestCode(requestCode);
		b.addAllPlaylistId(playlistIds);
		if(updated != null)
			b.setUpdatedDate(updated.getTime());
		return b.build();
	}
	
	public long getFriendRequestId() {
		return friendRequestId;
	}

	public void setFriendRequestId(long friendRequestId) {
		this.friendRequestId = friendRequestId;
	}

	public long getRequestorId() {
		return requestorId;
	}

	public void setRequestorId(long requestorId) {
		this.requestorId = requestorId;
	}

	public long getRequesteeId() {
		return requesteeId;
	}

	public void setRequesteeId(long requesteeId) {
		this.requesteeId = requesteeId;
	}

	public String getRequestCode() {
		return requestCode;
	}

	public void setRequestCode(String requestCode) {
		this.requestCode = requestCode;
	}

	public Set<Long> getPlaylistIds() {
		return playlistIds;
	}

	public void setPlaylistIds(Set<Long> playlistIds) {
		this.playlistIds = playlistIds;
	}

	public Date getUpdated() {
		return updated;
	}

	public void setUpdated(Date updated) {
		this.updated = updated;
	}

}
