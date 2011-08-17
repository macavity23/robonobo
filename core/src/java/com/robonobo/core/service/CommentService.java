package com.robonobo.core.service;

import static com.robonobo.common.util.TimeUtil.*;

import java.util.*;

import com.robonobo.core.api.model.Comment;
import com.robonobo.core.api.model.User;
import com.robonobo.core.metadata.*;

public class CommentService extends AbstractService {
	Map<String, Date> lastFetched = new HashMap<String, Date>();
	private AbstractMetadataService metadata;
	private DbService db;
	private EventService events;

	public CommentService() {
		addHardDependency("core.metadata");
		addHardDependency("core.db");
		addHardDependency("core.events");
	}

	@Override
	public String getName() {
		return "Comment service";
	}

	@Override
	public String getProvides() {
		return "core.comments";
	}

	@Override
	public void startup() throws Exception {
		metadata = rbnb.getMetadataService();
		db = rbnb.getDbService();
		events = rbnb.getEventService();
	}

	public void newCommentForPlaylist(final long playlistId, long parentId, String text, final CommentCallback cb) {
		Comment c = new Comment();
		c.setResourceId("playlist:" + playlistId);
		c.setParentId(parentId);
		c.setUserId(rbnb.getUserService().getMyUser().getUserId());
		c.setText(text);
		metadata.newComment(c, new CommentCallback() {
			public void success(Comment c) {
				// Fire the callback first, then our event
				cb.success(c);
				Map<Comment, Boolean> flarp = new HashMap<Comment, Boolean>();
				flarp.put(c, false);
				events.fireGotPlaylistComments(playlistId, flarp);
			}

			public void error(long commentId, Exception ex) {
				cb.error(commentId, ex);
			}
		});
	}

	public void newCommentForLibrary(long userId, long parentId, String text, final CommentCallback cb) {
		final User me = rbnb.getUserService().getMyUser();
		Comment c = new Comment();
		c.setResourceId("library:" + userId);
		c.setParentId(parentId);
		c.setUserId(me.getUserId());
		c.setText(text);
		metadata.newComment(c, new CommentCallback() {
			public void success(Comment c) {
				cb.success(c);
				Map<Comment, Boolean> flarp = new HashMap<Comment, Boolean>();
				flarp.put(c, false);
				events.fireGotLibraryComments(me.getUserId(), flarp);
			}

			public void error(long commentId, Exception ex) {
				cb.error(commentId, ex);
			}
		});
	}

	public void deleteComment(long commentId, final CommentCallback cb) {
		metadata.deleteComment(commentId, cb);
	}
	
	public void fetchCommentsForPlaylist(final long playlistId) {
		final String resId = "playlist:" + playlistId;
		final Date fetchTime = now();
		metadata.getAllComments("playlist", playlistId, lastFetched.get(resId), new AllCommentsCallback() {
			public void success(List<Comment> pl) {
				if (pl.size() > 0) {
					Map<Comment, Boolean> cNewMap = new HashMap<Comment, Boolean>();
					for (Comment c : pl) {
						cNewMap.put(c, db.haveSeenComment(c.getCommentId()));
					}
					events.fireGotPlaylistComments(playlistId, cNewMap);
				}
				lastFetched.put(resId, fetchTime);
			}

			public void error(long itemId, Exception ex) {
				log.error("Error fetching comments for playlist " + itemId, ex);
			}
		});
	}

	public void fetchCommentsForLibrary(final long userId) {
		final String resId = "library:" + userId;
		final Date fetchTime = now();
		metadata.getAllComments("library", userId, lastFetched.get(resId), new AllCommentsCallback() {
			public void success(List<Comment> pl) {
				if (pl.size() > 0) {
					Map<Comment, Boolean> cNewMap = new HashMap<Comment, Boolean>();
					for (Comment c : pl) {
						cNewMap.put(c, db.haveSeenComment(c.getCommentId()));
					}
					events.fireGotLibraryComments(userId, cNewMap);
				}
				lastFetched.put(resId, fetchTime);
			}

			public void error(long itemId, Exception ex) {
				log.error("Error fetching comments for library " + itemId, ex);
			}
		});
	}

	@Override
	public void shutdown() throws Exception {
	}
}
