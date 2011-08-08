package com.robonobo.core.metadata;

import java.util.List;

import com.robonobo.core.api.model.Comment;

public interface AllCommentsCallback {
	public void success(List<Comment> pl);
	public void error(long itemId, Exception ex);
}
