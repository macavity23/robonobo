package com.robonobo.midas.client;

import com.robonobo.core.metadata.CommentCallback;
import com.robonobo.midas.client.Params.Operation;

public class DeleteCommentRequest implements Request {
	private MidasClientConfig cfg;
	private Long commentId;
	private CommentCallback callback;

	public DeleteCommentRequest(MidasClientConfig cfg, long commentId, CommentCallback callback) {
		this.cfg = cfg;
		this.commentId = commentId;
		this.callback = callback;
	}
	@Override
	public int remaining() {
		if(commentId == null)
			return 0;
		return 1;
	}

	@Override
	public Params getNextParams() {
		Params p = new Params(Operation.Delete, null, null, cfg.getCommentByIdUrl(commentId), commentId);
		commentId = null;
		return p;
	}

	@Override
	public void success(Object obj) {
		if(callback != null)
			callback.success(null);
	}

	@Override
	public void error(Params p, Exception e) {
		if(callback != null) {
			Long commentId = (Long) p.obj;
			callback.error(commentId, e);
		}
	}
}
