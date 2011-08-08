package com.robonobo.midas.client;

import java.util.ArrayList;
import java.util.Date;

import com.robonobo.core.api.model.Comment;
import com.robonobo.core.api.proto.CoreApi.CommentMsg;
import com.robonobo.core.api.proto.CoreApi.CommentMsgList;
import com.robonobo.core.metadata.AllCommentsCallback;
import com.robonobo.midas.client.Params.Operation;

public class GetAllCommentsRequest implements Request {
	private MidasClientConfig cfg;
	private String itemType;
	private AllCommentsCallback callback;
	private Long itemId;
	private Date since;
	public GetAllCommentsRequest(MidasClientConfig cfg, String itemType, long itemId, Date since, AllCommentsCallback callback) {
		this.cfg = cfg;
		this.itemType = itemType;
		this.itemId = itemId;
		this.since = since;
		this.callback = callback;
	}
	
	@Override
	public int remaining() {
		if(itemId == null)
			return 0;
		return 1;
	}

	@Override
	public Params getNextParams() {
		Params p = new Params(Operation.Get, null, CommentMsgList.newBuilder(), cfg.getAllCommentsUrl(itemType, itemId, since), itemId);
		itemId = null;
		return p;
	}

	@Override
	public void success(Object obj) {
		CommentMsgList cml = (CommentMsgList) obj;
		ArrayList<Comment> result = new ArrayList<Comment>();
		for (CommentMsg msg : cml.getCommentList()) {
			result.add(new Comment(msg));
		}
		callback.success(result);
	}

	@Override
	public void error(Params p, Exception e) {
		if(callback != null) {
			long itemId = (Long) p.obj;
			callback.error(itemId, e);
		}
	}
}
