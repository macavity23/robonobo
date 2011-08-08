package com.robonobo.midas.model;

import com.robonobo.core.api.model.Comment;
import com.robonobo.core.api.proto.CoreApi.CommentMsg;

public class MidasComment extends Comment {
	public MidasComment() {
		super();
	}

	public MidasComment(CommentMsg msg) {
		super(msg);
	}
}
