package com.robonobo.core.service;

import static com.robonobo.common.util.TimeUtil.*;

import java.util.*;

import com.robonobo.common.concurrent.Batcher;
import com.robonobo.core.api.Task;
import com.robonobo.core.api.model.*;
import com.robonobo.core.metadata.*;

public class LibraryService extends AbstractService {
	/** secs */
	static final int SEND_UPDATE_TO_SERVER_DELAY = 30;
	/** secs */
	static final int FIRE_UI_EVENT_DELAY = 10;
	private AddBatcher addB;
	private DelBatcher delB;
	Set<Long> stillFetchingLibs = new HashSet<Long>();
	Set<Long> loadedLibs = new HashSet<Long>();
	UserService users;
	AbstractMetadataService metadata;
	TaskService tasks;
	EventService events;
	StreamService streams;
	DbService db;

	public LibraryService() {
		addHardDependency("core.metadata");
		addHardDependency("core.users");
		addHardDependency("core.tasks");
		addHardDependency("core.event");
		addHardDependency("core.streams");
	}

	@Override
	public String getName() {
		return "Library service";
	}

	@Override
	public String getProvides() {
		return "core.libraries";
	}

	@Override
	public void startup() throws Exception {
		metadata = rbnb.getMetadataService();
		users = rbnb.getUserService();
		tasks = rbnb.getTaskService();
		events = rbnb.getEventService();
		streams = rbnb.getStreamService();
		db = rbnb.getDbService();
		addB = new AddBatcher();
		delB = new DelBatcher();
	}

	@Override
	public void shutdown() throws Exception {
		// We might have some pending library updates - do them now
		addB.run();
		delB.run();
	}

	public void addToMyLibrary(String streamId) {
		addB.add(new LibraryTrack(streamId, now()));
	}

	public void delFromMyLibrary(String streamId) {
		delB.add(streamId);
	}

	void updateFriendLibraries() {
		if (stillFetchingLibs.size() == 0)
			tasks.runTask(new FriendLibrariesUpdateTask());
	}

	class FriendLibrariesUpdateTask extends Task {
		public FriendLibrariesUpdateTask() {
			title = "Loading libraries";
		}

		@Override
		public void runTask() throws Exception {
			statusText = "Loading friends' library details";
			fireUpdated();
			Set<Long> friendIds = new HashSet<Long>(rbnb.getUserService().getMyUser().getFriendIds());
			int done = 0;
			for (long friendId : friendIds) {
				User friend = users.getUser(friendId);
				statusText = "Loading details for " + friend.getEmail();
				completion = ((float) done) / friendIds.size();
				fireUpdated();
				LibraryInfo libInfo = db.getLibInfo(friendId);
				Date lastUpdated = (libInfo == null) ? null : libInfo.getLastChecked();
				tasks.runTask(new LibraryUpdateTask(friend, lastUpdated));
			}
			statusText = "Done.";
			completion = 1f;
			fireUpdated();
		}
	}

	class LibraryUpdateTask extends Task implements LibraryCallback {
		private Date lastUpdate;
		private User friend;
		private Set<String> waitingForStreams;
		private int streamsToFetch;
		private long checkTime;

		public LibraryUpdateTask(User friend, Date lastUpdate) {
			title = "Fetching library for " + friend.getEmail();
			this.lastUpdate = lastUpdate;
			this.friend = friend;
			stillFetchingLibs.add(friend.getUserId());
		}

		public void runTask() throws Exception {
			statusText = "Retrieving library details";
			fireUpdated();
			metadata.fetchLibrary(friend.getUserId(), lastUpdate, this);
			checkTime = System.currentTimeMillis();
		}

		private void done() {
			statusText = "Done.";
			completion = 1f;
			stillFetchingLibs.remove(friend.getUserId());
			fireUpdated();
		}
		
		public void success(Library nLib) {
			final long friendId = friend.getUserId();
			Map<String, Date> newTrax = nLib.getTracks();
			log.debug("Received updated library for " + friend.getEmail() + ": " + newTrax.size() + " new tracks");
			if(newTrax.size() > 0)
				db.addUnknownTracksToLibrary(friendId, newTrax, checkTime);
			else
				db.markLibraryAsChecked(friendId, checkTime);
			int numTracks = db.numTracksInLibrary(friendId);
			if(numTracks == 0) {
				done();
				return;
			}
			// Fetch from the db afresh as there might well have been some tracks we didn't get a chance to look up last
			// time
			final Map<String, Date> unknownTracks = db.getUnknownTracksInLibrary(friendId);
			if(!loadedLibs.contains(friendId)) {
				LibraryInfo libInfo = db.getLibInfo(friendId);
				events.fireFriendLibraryReady(friendId, libInfo.getNumUnseen());
				loadedLibs.add(friendId);
			}
			if (unknownTracks.size() == 0) {
				done();
				return;
			}
			// Get the rest of our streams
			waitingForStreams = new HashSet<String>();
			waitingForStreams.addAll(unknownTracks.keySet());
			streamsToFetch = waitingForStreams.size();
			statusText = "Fetching track 1 of " + streamsToFetch;
			fireUpdated();
			// As stream metadata comes in, fire them up to the ui in batches
			final Batcher<String> trackBatcher = new Batcher<String>(FIRE_UI_EVENT_DELAY * 1000, rbnb.getExecutor()) {
				protected void runBatch(Collection<String> sids) throws Exception {
					Map<String, Date> newTracks = new HashMap<String, Date>(sids.size());
					for (String sid : sids) {
						newTracks.put(sid, unknownTracks.get(sid));
					}
					LibraryInfo libInfo = db.getLibInfo(friendId);
					events.fireFriendLibraryUpdated(friendId, libInfo.getNumUnseen(), newTracks);
				}
			};
			streams.fetchStreams(unknownTracks.keySet(), new StreamCallback() {
				public void success(Stream s) {
					String sid = s.getStreamId();
					db.markTrackAsKnown(friendId, sid);
					trackBatcher.add(sid);
					boolean finished = streamUpdated(sid);
					if (finished) {
						try {
							trackBatcher.runNow();
						} catch (Exception ignore) {
						}
					}
				}

				public void error(String sid, Exception ex) {
					log.error("Error fetching stream " + sid, ex);
					boolean finished = streamUpdated(sid);
					if (finished) {
						try {
							trackBatcher.runNow();
						} catch (Exception ignore) {
						}
					}
				}
			});
		}

		public void error(long userId, Exception e) {
			log.error("Failed to retrieve library for user " + userId, e);
			completion = 1f;
			statusText = "An error occurred";
			stillFetchingLibs.remove(userId);
			fireUpdated();
		}

		boolean streamUpdated(String sid) {
			boolean finished;
			waitingForStreams.remove(sid);
			int streamsDone = streamsToFetch - waitingForStreams.size();
			if (streamsDone == streamsToFetch) {
				completion = 1f;
				stillFetchingLibs.remove(friend.getUserId());
				statusText = "Done.";
				finished = true;
			} else {
				completion = ((float) streamsDone) / streamsToFetch;
				statusText = "Fetching track " + (streamsDone + 1) + " of " + streamsToFetch;
				finished = false;
			}
			fireUpdated();
			return finished;
		}
	}

	class LibraryTrack {
		String streamId;
		Date dateAdded;

		public LibraryTrack(String streamId, Date dateAdded) {
			this.streamId = streamId;
			this.dateAdded = dateAdded;
		}
	}

	class AddBatcher extends Batcher<LibraryTrack> {
		public AddBatcher() {
			super(SEND_UPDATE_TO_SERVER_DELAY * 1000, rbnb.getExecutor());
		}

		@Override
		protected void runBatch(Collection<LibraryTrack> tracks) throws Exception {
			if (tracks.size() == 0)
				return;
			Library lib = new Library();
			for (LibraryTrack t : tracks) {
				lib.getTracks().put(t.streamId, t.dateAdded);
			}
			User me = rbnb.getUserService().getMyUser();
			metadata.addToLibrary(me.getUserId(), lib, new LibraryCallback() {
				public void success(Library l) {
					log.debug("Successfully added tracks to my library");
				}

				public void error(long userId, Exception e) {
					log.error("Error adding to my library", e);
				}
			});
		}
	}

	class DelBatcher extends Batcher<String> {
		public DelBatcher() {
			super(SEND_UPDATE_TO_SERVER_DELAY * 1000, rbnb.getExecutor());
		}

		@Override
		protected void runBatch(Collection<String> streamIds) throws Exception {
			if (streamIds.size() == 0)
				return;
			Library lib = new Library();
			for (String sid : streamIds) {
				lib.getTracks().put(sid, null);
			}
			User me = rbnb.getUserService().getMyUser();
			metadata.deleteFromLibrary(me.getUserId(), lib, new LibraryCallback() {
				public void success(Library l) {
					log.debug("Successfully removed tracks from my library");
				}

				public void error(long userId, Exception e) {
					log.error("Error deleting from my library", e);
				}
			});
		}
	}
}
