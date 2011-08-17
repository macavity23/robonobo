package com.robonobo.core.api.model;

import static com.robonobo.common.util.TimeUtil.*;

import java.util.Date;
import java.util.regex.Pattern;

import com.robonobo.core.api.proto.CoreApi.CommentMsg;

public class Comment implements Comparable<Comment> {
	/** Id of this comment */
	long commentId;
	/** type and id of whatever this comment is attached to, eg playlist:3456 */ 
	String resourceId;
	long userId;
	/** id of the parent comment, or -1 if none */
	long parentId = -1;
	Date date = now();
	String text;
	public static final Pattern RESOURCE_ID_PAT = Pattern.compile("^(\\w+):(\\d+)$");
	
	public Comment() {
	}
	
	public Comment(CommentMsg msg) {
		commentId = msg.getCommentId();
		resourceId = msg.getResourceId();
		userId = msg.getUserId();
		if(msg.hasParentId())
			parentId = msg.getParentId();
		date = new Date(msg.getDate());
		text = msg.getText();
	}
	
	public CommentMsg toMsg() {
		CommentMsg.Builder b = CommentMsg.newBuilder();
		b.setCommentId(commentId);
		b.setResourceId(resourceId);
		b.setUserId(userId);
		if(parentId > 0)
			b.setParentId(parentId);
		b.setDate(date.getTime());
		b.setText(text);
		return b.build();
	}

	@Override
	public int compareTo(Comment o) {
		return date.compareTo(o.date);
	}
	
	public long getCommentId() {
		return commentId;
	}

	public void setCommentId(long commentId) {
		this.commentId = commentId;
	}

	public String getResourceId() {
		return resourceId;
	}

	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public long getParentId() {
		return parentId;
	}

	public void setParentId(long parentId) {
		this.parentId = parentId;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
}
