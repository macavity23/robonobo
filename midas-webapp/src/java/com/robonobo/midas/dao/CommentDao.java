package com.robonobo.midas.dao;

import java.util.Date;
import java.util.List;

import com.robonobo.midas.model.MidasComment;

public interface CommentDao {

	public abstract List<MidasComment> getCommentsSince(String resourceId, Date since);

	public abstract List<MidasComment> getAllComments(String resourceId);

	public abstract void saveComment(MidasComment comment);

	public abstract void deleteAllComments(String resourceId);

	public abstract void deleteComment(MidasComment comment);

	public abstract long getHighestCommentId();

	public abstract MidasComment getComment(long commentId);
}