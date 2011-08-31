package com.robonobo.midas;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.robonobo.core.api.model.Playlist;
import com.robonobo.midas.dao.EventDao;
import com.robonobo.midas.model.*;

@Service("event")
public class EventServiceImpl implements EventService {
	@Autowired
	EventDao eventDao;
	
	@Override
	public void userLoggedIn(MidasUser u) {
		saveEvent(u, "login", null);
	}
	
	@Override
	public void userRemainsOnline(MidasUser u) {
		saveEvent(u, "online", null);
	}
	
	@Override
	public void playlistCreated(MidasUser u, Playlist p) {
		saveEvent(u, "playlistCreated", "id="+p.getPlaylistId());
	}
	
	@Override
	public void playlistUpdated(MidasUser u, Playlist p) {
		saveEvent(u, "playlistUpdated", "id="+p.getPlaylistId());
	}
	
	@Override
	public void playlistDeleted(MidasUser u, Playlist p) {
		saveEvent(u, "playlistDeleted", "id="+p.getPlaylistId());
	}
	
	@Override
	public void playlistShared(MidasUser sharer, Playlist p, MidasUser sharee) {
		saveEvent(sharer, "playlistShared", "uid="+sharee.getUserId()+",plId="+p.getPlaylistId());
	}
	
	@Override
	public void playlistPosted(MidasUser u, Playlist p, String postService) {
		saveEvent(u, "playlistCreated", "id="+p.getPlaylistId()+",svc="+postService);
	}

	@Override
	public void specialPlaylistPosted(MidasUser u, long uid, String plName) {
		saveEvent(u, "specPlaylistPosted", "uid="+uid+",name="+plName);
	}

	@Override
	public void addedToLibrary(MidasUser u, int numTracks) {
		saveEvent(u, "addToLibrary", "num="+numTracks);
	}
	
	@Override
	public void removedFromLibrary(MidasUser u, int numTracks) {
		saveEvent(u, "rmFromLibrary", "num="+numTracks);
	}
	
	@Override
	public void inviteSent(MidasUser u, String email, MidasInvite i) {
		saveEvent(u, "inviteSent", "email="+email+",code="+i.getInviteCode());
	}
	
	@Override
	public void inviteAccepted(MidasUser u, MidasInvite i) {
		saveEvent(u, "inviteAccepted", "code="+i.getInviteCode());
	}
	
	@Override
	public void newUser(MidasUser u) {
		saveEvent(u, "newUser", null);
	}
	
	@Override
	public void friendRequestSent(MidasUser requestor, MidasUser requestee) {
		saveEvent(requestor, "friendReqSent", "requestee="+requestee.getUserId());
	}
	
	@Override
	public void friendRequestAccepted(MidasUser requestor, MidasUser requestee) {
		saveEvent(requestor, "friendReqAccepted", "requestee="+requestee.getUserId());
	}
	
	private void saveEvent(MidasUser user, String eventType, String eventInfo) {
		eventDao.saveEvent(new MidasEvent(user.getUserId(), eventType, eventInfo));
	}
}
