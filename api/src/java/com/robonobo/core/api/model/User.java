package com.robonobo.core.api.model;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.robonobo.core.api.proto.CoreApi.UserMsg;

@SuppressWarnings("serial")
public class User implements Serializable {
	long userId;
	String email;
	String password;
	String friendlyName;
	String description;
	String imgUrl;
	Date updated;
	int invitesLeft;
	Set<Long> friendIds = new HashSet<Long>();
	Set<Long> playlistIds = new HashSet<Long>();

	public User() {
	}

	public User(String email, String password) {
		this.email = email;
		this.password = password;
	}

	public User(User copyUser) {
		this(copyUser.getEmail(), copyUser.getPassword());
		setUserId(copyUser.getUserId());
		copyFrom(copyUser);
	}

	public User(UserMsg msg) {
		this(msg.getEmail(), msg.getPassword());
		setUserId(msg.getId());
		setFriendlyName(msg.getFriendlyName());
		setDescription(msg.getDescription());
		setImgUrl(msg.getImageUrl());
		setUpdated(new Date(msg.getUpdatedDate()));
		setInvitesLeft(msg.getInvitesLeft());
		friendIds.addAll(msg.getFriendIdList());
		playlistIds.addAll(msg.getPlaylistIdList());
	}

	public UserMsg toMsg(boolean incPassword) {
		UserMsg.Builder b = UserMsg.newBuilder();
		b.setId(userId);
		b.setEmail(email);
		if(incPassword)
			b.setPassword(password);
		b.setFriendlyName(friendlyName);
		if(description != null)
			b.setDescription(description);
		if(imgUrl != null)
			b.setImageUrl(imgUrl);
		if(updated != null)
			b.setUpdatedDate(updated.getTime());
		b.setInvitesLeft(invitesLeft);
		b.addAllFriendId(friendIds);
		b.addAllPlaylistId(playlistIds);
		return b.build();
	}
	
	/**
	 * Doesn't copy id and email, to do this use the User(User) ctor
	 */
	public void copyFrom(User copyUser) {
		setFriendlyName(copyUser.getFriendlyName());
		setDescription(copyUser.getDescription());
		setImgUrl(copyUser.getImgUrl());
		setUpdated(copyUser.getUpdated());
		setInvitesLeft(copyUser.getInvitesLeft());
		friendIds.clear();
		friendIds.addAll(copyUser.getFriendIds());
		playlistIds.clear();
		playlistIds.addAll(copyUser.getPlaylistIds());		
	}
	
	@Override
	public int hashCode() {
		return getClass().getName().hashCode() ^ (int)userId;
	}
	
	public boolean equals(Object obj) {
		return obj.getClass().equals(getClass()) && (obj.hashCode() == hashCode());
	}

	public String getEmail() {
		return email;
	}

	public String getPassword() {
		return password;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getFriendlyName() {
		return friendlyName;
	}

	public void setFriendlyName(String friendlyName) {
		this.friendlyName = friendlyName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getUpdated() {
		return updated;
	}

	public void setUpdated(Date updated) {
		this.updated = updated;
	}

	public String getImgUrl() {
		return imgUrl;
	}

	public void setImgUrl(String imgUrl) {
		this.imgUrl = imgUrl;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public Set<Long> getFriendIds() {
		return friendIds;
	}

	public void setFriendIds(Set<Long> friendIds) {
		this.friendIds = friendIds;
	}

	public Set<Long> getPlaylistIds() {
		return playlistIds;
	}

	public void setPlaylistIds(Set<Long> playlistIds) {
		this.playlistIds = playlistIds;
	}
	
	@Override
	public String toString() {
		return "User[id="+userId+",email="+email+"]";
	}

	public int getInvitesLeft() {
		return invitesLeft;
	}

	public void setInvitesLeft(int invitesLeft) {
		this.invitesLeft = invitesLeft;
	}
}
