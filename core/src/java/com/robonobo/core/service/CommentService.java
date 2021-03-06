package com.robonobo.core.service;

import static com.robonobo.common.util.TimeUtil.*;

import java.util.*;
import java.util.Map.Entry;

import com.robonobo.core.api.model.Comment;
import com.robonobo.core.api.model.User;
import com.robonobo.core.metadata.*;

public class CommentService extends AbstractService {
	Map<String, Date> lastFetched = new HashMap<String, Date>();
	Map<String, Map<Comment, Boolean>> cmtsByResId = new HashMap<String, Map<Comment, Boolean>>();
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

	private synchronized void storeComments(String resourceId, Map<Comment, Boolean> cMap) {
		if (cmtsByResId.containsKey(resourceId))
			cmtsByResId.get(resourceId).putAll(cMap);
		else
			cmtsByResId.put(resourceId, cMap);
	}

	public void newCommentForPlaylist(final long playlistId, long parentId, String text, final CommentCallback cb) {
		Comment c = new Comment();
		final String resId = "playlist:" + playlistId;
		c.setResourceId(resId);
		c.setParentId(parentId);
		c.setUserId(rbnb.getUserService().getMyUser().getUserId());
		c.setText(text);
		metadata.newComment(c, new CommentCallback() {
			public void success(Comment c) {
				// Fire the callback first, then our event
				cb.success(c);
				Map<Comment, Boolean> flarp = new HashMap<Comment, Boolean>();
				flarp.put(c, false);
				storeComments(resId, flarp);
				events.fireGotPlaylistComments(playlistId, false, flarp);
				db.markCommentsAsSeen(flarp.keySet());
			}

			public void error(long commentId, Exception ex) {
				cb.error(commentId, ex);
			}
		});
	}

	public void newCommentForLibrary(final long userId, long parentId, String text, final CommentCallback cb) {
		final User me = rbnb.getUserService().getMyUser();
		Comment c = new Comment();
		final String resId = "library:" + userId;
		c.setResourceId(resId);
		c.setParentId(parentId);
		c.setUserId(me.getUserId());
		c.setText(text);
		metadata.newComment(c, new CommentCallback() {
			public void success(Comment c) {
				cb.success(c);
				Map<Comment, Boolean> flarp = new HashMap<Comment, Boolean>();
				flarp.put(c, false);
				storeComments(resId, flarp);
				events.fireGotLibraryComments(userId, false, flarp);
				db.markCommentsAsSeen(flarp.keySet());
			}

			public void error(long commentId, Exception ex) {
				cb.error(commentId, ex);
			}
		});
	}

	public Map<Comment, Boolean> getExistingComments(String resourceId) {
		Map<Comment, Boolean> result = new HashMap<Comment, Boolean>();
		synchronized (this) {
			if (cmtsByResId.containsKey(resourceId))
				result.putAll(cmtsByResId.get(resourceId));
		}
		return result;
	}

	public void markAllCommentsAsSeen(String resourceId) {
		List<Comment> cl = new ArrayList<Comment>();
		synchronized (this) {
			if (cmtsByResId.containsKey(resourceId)) {
				for (Entry<Comment, Boolean> ent : cmtsByResId.get(resourceId).entrySet()) {
					ent.setValue(false);
					cl.add(ent.getKey());
				}
			}
		}
		db.markCommentsAsSeen(cl);
	}

	public void deleteComment(Comment c, final CommentCallback cb) {
		synchronized (this) {
			if (cmtsByResId.containsKey(c.getResourceId()))
				cmtsByResId.get(c.getResourceId()).remove(c);
		}
		metadata.deleteComment(c.getCommentId(), cb);
	}

	public void fetchCommentsForPlaylist(final long playlistId) {
		final String resId = "playlist:" + playlistId;
		final Date fetchTime = now();
		metadata.getAllComments("playlist", playlistId, lastFetched.get(resId), new AllCommentsCallback() {
			public void success(List<Comment> pl) {
				if (pl.size() > 0) {
					Map<Comment, Boolean> cNewMap = new HashMap<Comment, Boolean>();
					boolean anyUnseen = false;
					for (Comment c : pl) {
						boolean unseen = !db.haveSeenComment(c.getCommentId());
						if (unseen)
							anyUnseen = true;
						cNewMap.put(c, unseen);
					}
					storeComments(resId, cNewMap);
					log.debug("Fetched " + pl.size() + " comments for playlist " + playlistId);
					events.fireGotPlaylistComments(playlistId, anyUnseen, cNewMap);
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
					boolean anyUnread = false;
					for (Comment c : pl) {
						boolean unread = !db.haveSeenComment(c.getCommentId());
						if(unread)
							anyUnread = true;
						cNewMap.put(c, unread);
					}
					storeComments(resId, cNewMap);
					log.debug("Fetched " + pl.size() + " comments for library " + userId);
					events.fireGotLibraryComments(userId, anyUnread, cNewMap);
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
