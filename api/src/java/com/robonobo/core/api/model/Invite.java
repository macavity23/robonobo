package com.robonobo.core.api.model;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.robonobo.core.api.proto.CoreApi.InviteMsg;

public class Invite {
	long inviteId;
	String email;
	String inviteCode;
	Date updated;
	Set<Long> playlistIds = new HashSet<Long>();
	Set<Long> friendIds = new HashSet<Long>();

	public Invite() {
	}

	public Invite(InviteMsg msg) {
		inviteId = msg.getInviteId();
		email = msg.getEmail();
		inviteCode = msg.getInviteCode();
		if(msg.hasUpdatedDate())
			updated = new Date(msg.getUpdatedDate());
		playlistIds.addAll(msg.getPlaylistIdList());
		friendIds.addAll(msg.getFriendIdList());
	}
	
	public InviteMsg toMsg() {
		InviteMsg.Builder b = InviteMsg.newBuilder();
		b.setInviteId(inviteId);
		b.setEmail(email);
		b.setInviteCode(inviteCode);
		if(updated != null)
			b.setUpdatedDate(updated.getTime());
		b.addAllPlaylistId(playlistIds);
		b.addAllFriendId(friendIds);
		return b.build();
	}
	
	public long getInviteId() {
		return inviteId;
	}

	public void setInviteId(long inviteId) {
		this.inviteId = inviteId;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getInviteCode() {
		return inviteCode;
	}

	public void setInviteCode(String inviteCode) {
		this.inviteCode = inviteCode;
	}

	public Date getUpdated() {
		return updated;
	}

	public void setUpdated(Date updated) {
		this.updated = updated;
	}

	public Set<Long> getPlaylistIds() {
		return playlistIds;
	}

	public void setPlaylistIds(Set<Long> playlistIds) {
		this.playlistIds = playlistIds;
	}

	public Set<Long> getFriendIds() {
		return friendIds;
	}

	public void setFriendIds(Set<Long> friendIds) {
		this.friendIds = friendIds;
	}
	
}
