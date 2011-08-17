package com.robonobo.midas.client;

import com.robonobo.core.api.model.Comment;
import com.robonobo.core.api.proto.CoreApi.CommentMsg;
import com.robonobo.core.metadata.CommentCallback;
import com.robonobo.midas.client.Params.Operation;

public class NewCommentRequest implements Request {
	MidasClientConfig cfg;
	Comment comment;
	CommentCallback callback;

	public NewCommentRequest(MidasClientConfig cfg, Comment comment, CommentCallback callback) {
		this.cfg = cfg;
		this.comment = comment;
		this.callback = callback;
	}

	@Override
	public int remaining() {
		if (comment == null)
			return 0;
		return 1;
	}

	@Override
	public Params getNextParams() {
		Params result = new Params(Operation.Put, comment.toMsg(), CommentMsg.newBuilder(), cfg.getCommentByTypeUrl(comment.getResourceId()), comment);
		comment = null;
		return result;
	}

	@Override
	public void success(Object obj) {
		if(callback != null) {
			CommentMsg msg = (CommentMsg) obj;
			callback.success(new Comment(msg));
		}
	}

	@Override
	public void error(Params p, Exception e) {
		if(callback != null) {
			callback.error(-1, e);
		}
	}
}
