package com.robonobo.core.service;

import static com.robonobo.common.util.TimeUtil.*;

import java.util.*;

import com.robonobo.core.api.model.Comment;
import com.robonobo.core.metadata.AbstractMetadataService;
import com.robonobo.core.metadata.AllCommentsCallback;

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
				log.error("Error fetching comments for playlist " + itemId, ex);
			}
		});
	}

	@Override
	public void shutdown() throws Exception {
	}
}
