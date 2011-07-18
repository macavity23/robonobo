package com.robonobo.midas.client;

import java.util.Collection;

import com.robonobo.core.api.model.Playlist;
import com.robonobo.core.api.proto.CoreApi.PlaylistMsg;
import com.robonobo.core.metadata.PlaylistCallback;
import com.robonobo.midas.client.Params.Operation;

public class SharePlaylistRequest implements Request {

	private MidasClientConfig cfg;
	private Long plId;
	private Collection<Long> friendIds;
	private Collection<String> emails;
	private PlaylistCallback handler;

	public SharePlaylistRequest(MidasClientConfig cfg, long playlistId, Collection<Long> shareFriendIds, Collection<String> friendEmails, PlaylistCallback handler) {
		this.cfg = cfg;
		this.plId = playlistId;
		this.friendIds = shareFriendIds;
		this.emails = friendEmails;
		this.handler = handler;
	}

	@Override
	public int remaining() {
		if(plId == null)
			return 0;
		return 1;
	}

	@Override
	public Params getNextParams() {
		Params p = new Params(Operation.Get, null, PlaylistMsg.newBuilder(), cfg.getSharePlaylistUrl(plId, friendIds, emails), plId);
		plId = null;
		return p;
	}

	@Override
	public void success(Object obj) {
		if(handler != null)
			handler.success(new Playlist((PlaylistMsg) obj));
	}

	@Override
	public void error(Params p, Exception e) {
		if(handler != null) {
			Long plId = (Long) p.obj;
			handler.error(plId, e);
		}
	}

}
