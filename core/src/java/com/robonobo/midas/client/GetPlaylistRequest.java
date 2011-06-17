package com.robonobo.midas.client;

import java.util.Collection;
import java.util.Stack;

import com.robonobo.core.api.model.Playlist;
import com.robonobo.core.api.proto.CoreApi.PlaylistMsg;
import com.robonobo.core.metadata.PlaylistCallback;
import com.robonobo.midas.client.Params.Operation;

public class GetPlaylistRequest implements Request {
	MidasClientConfig cfg;
	Stack<Long> plIds = new Stack<Long>();
	PlaylistCallback handler;
	
	public GetPlaylistRequest(MidasClientConfig cfg, Collection<Long> plIds, PlaylistCallback handler) {
		this.cfg = cfg;
		this.plIds.addAll(plIds);
		this.handler = handler;
	}

	public GetPlaylistRequest(MidasClientConfig cfg, long plId, PlaylistCallback handler) {
		this.cfg = cfg;
		this.plIds.add(plId);
		this.handler = handler;
	}

	@Override
	public int remaining() {
		return plIds.size();
	}

	@Override
	public Params getNextParams() {
		Long plId = plIds.pop();
		return new Params(Operation.Get, null, PlaylistMsg.newBuilder(), cfg.getPlaylistUrl(plId), plId);
	}

	@Override
	public void success(Object obj) {
		PlaylistMsg msg = (PlaylistMsg) obj;
		handler.success(new Playlist(msg));
	}

	@Override
	public void error(Params p, Exception e) {
		Long plId = (Long) p.obj;
		handler.error(plId, e);
	}

}
