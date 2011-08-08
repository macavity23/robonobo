package com.robonobo.core.metadata;

import com.robonobo.core.api.model.Comment;

public interface CommentCallback {
	public void success(Comment c);
	public void error(long commentId, Exception ex);
}
