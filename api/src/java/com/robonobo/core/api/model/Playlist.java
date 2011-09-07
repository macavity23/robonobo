package com.robonobo.core.api.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.robonobo.common.exceptions.SeekInnerCalmException;
import com.robonobo.core.api.proto.CoreApi.PlaylistMsg;

public class Playlist implements Comparable<Playlist> {
	public static final String VIS_ALL = "all";
	public static final String VIS_FRIENDS = "friends";
	public static final String VIS_ME = "me";
	
	String title;
	Date updated;
	List<String> streamIds = new ArrayList<String>();
	String description;
	long playlistId;
	String visibility = VIS_FRIENDS;
	Set<Long> ownerIds = new HashSet<Long>();
	
	public Playlist() {
	}
	
	public Playlist(PlaylistMsg msg) {
		playlistId = msg.getId();
		title = msg.getTitle();
		updated = new Date(msg.getUpdatedDate());
		description = msg.getDescription();
		String vis = msg.getVisibility();
		if((!vis.equals(VIS_ALL)) && (!vis.equals(VIS_FRIENDS)) && (!vis.equals(VIS_ME)))
			throw new SeekInnerCalmException("invalid visibility: "+vis);
		visibility = vis;
		streamIds.addAll(msg.getStreamIdList());
		ownerIds.addAll(msg.getOwnerIdList());
	}
	
	public PlaylistMsg toMsg() {
		PlaylistMsg.Builder b = PlaylistMsg.newBuilder();
		b.setId(playlistId);
		b.setTitle(title);
		if(updated != null)
			b.setUpdatedDate(updated.getTime());
		if(description != null)
			b.setDescription(description);
		b.setVisibility(visibility);
		b.addAllStreamId(streamIds);
		b.addAllOwnerId(ownerIds);
		return b.build();
	}
	
	public void copyFrom(Playlist p) {
		title = p.title;
		description = p.description;
		playlistId = p.playlistId;
		updated = p.updated;
		visibility = p.visibility;
		streamIds.clear();
		streamIds.addAll(p.getStreamIds());
	}

	public boolean equals(Object obj) {
		if(obj == null)
			return false;
		if(!(obj instanceof Playlist))
			return false;
		Playlist p = (Playlist) obj;
		return playlistId == p.getPlaylistId();
	}

	@Override
	public int compareTo(Playlist o) {
		return title.toLowerCase().compareTo(o.getTitle().toLowerCase());
	}
	
	public long getPlaylistId() {
		return playlistId;
	}

	public String getDescription() {
		return description;
	}

	public String getTitle() {
		return title;
	}

	public Date getUpdated() {
		return updated;
	}

	public int hashCode() {
		return (int) (getClass().getName().hashCode() ^ getPlaylistId());
	}

	public void setPlaylistId(long playlistId) {
		this.playlistId = playlistId;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setUpdated(Date updated) {
		this.updated = updated;
	}


	public String toString() {
		StringBuffer sb = new StringBuffer("[Playlist:id=").append(playlistId).append(",title=").append(title).append(",streams=(");
		int i = 0;
		for (String s : streamIds) {
			if(i++ > 0)
				sb.append(",");
			sb.append(s);			
		}
		sb.append(")]");
		return sb.toString();
	}

	public List<String> getStreamIds() {
		return streamIds;
	}

	public void setStreamIds(List<String> streamIds) {
		this.streamIds = streamIds;
	}

	public String getVisibility() {
		return visibility;
	}
	
	public void setVisibility(String visibility) {
		this.visibility = visibility;
	}
	
	public Set<Long> getOwnerIds() {
		return ownerIds;
	}

	public void setOwnerIds(Set<Long> ownerIds) {
		this.ownerIds = ownerIds;
	}
}