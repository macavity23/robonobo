
package com.robonobo.core.service;

import static com.robonobo.common.util.TimeUtil.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.robonobo.common.concurrent.Batcher;
import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.core.api.Task;
import com.robonobo.core.api.UserPlaylistListener;
import com.robonobo.core.api.config.MetadataServerConfig;
import com.robonobo.core.api.model.*;
import com.robonobo.core.api.proto.CoreApi.LibraryMsg;

public class LibraryService extends AbstractService implements UserPlaylistListener {
	static final int LIBRARY_UPDATE_DELAY = 30; // Secs
	private AddBatcher addB;
	private DelBatcher delB;
	private Map<Long, Library> libs = new HashMap<Long, Library>();
	Date lastUpdated = null;
	boolean libsLoaded = false;

	public LibraryService() {
		addHardDependency("core.metadata");
		addHardDependency("core.users");
	}

	@Override
	public String getName() {
		return "Library service";
	}

	@Override
	public String getProvides() {
		return "core.library";
	}

	@Override
	public void startup() throws Exception {
		addB = new AddBatcher();
		delB = new DelBatcher();
		rbnb.getEventService().addUserPlaylistListener(this);
	}

	@Override
	public void shutdown() throws Exception {
		// We might have some pending library updates - do them now
		addB.run();
		delB.run();		
	}

	public synchronized Library getLibrary(long userId) {
		return libs.get(userId);
	}

	public void addToLibrary(String streamId) {
		addB.add(new LibraryTrack(streamId, now()));
	}

	public void delFromLibrary(String streamId) {
		delB.add(streamId);
	}

	@Override
	public void loggedIn() {
		// We will need to load the libraries again
		libsLoaded = false;
	}

	@Override
	public void allUsersAndPlaylistsUpdated() {
		rbnb.getTaskService().runTask(new LibrariesUpdateTask());
	}
	
	@Override
	public void userChanged(User u) {
		// Do nothing
	}

	@Override
	public void playlistChanged(Playlist p) {
		// Do nothing
	}

	@Override
	public void libraryChanged(Library lib) {
		// Do nothing
	}

	@Override
	public void userConfigChanged(UserConfig cfg) {
		// Do nothing
	}
	
	class LibrariesUpdateTask extends Task {
		public LibrariesUpdateTask() {
			title = "Updating friend libraries";
		}
		
		@Override
		public void doRun() throws Exception {
			Set<Long> friendIds = new HashSet<Long>(rbnb.getUsersService().getMyUser().getFriendIds());
			// Clean out any ex-friends - your tunes stank anyway! :-P
			synchronized (LibraryService.this) {
				Iterator<Long> it = libs.keySet().iterator();
				while(it.hasNext()) {
					long libUid = it.next();
					if(!friendIds.contains(libUid))
						it.remove();
				}
			}
			int friendsDone = 0;
			for (long friendId : friendIds) {
				completion = (float) friendsDone / friendIds.size();
				User u = rbnb.getUsersService().getUser(friendId);
				statusText = "Fetching library for " + u.getEmail();
				fireUpdated();

				LibraryMsg.Builder b = LibraryMsg.newBuilder();
				MetadataServerConfig msc = rbnb.getUsersService().getMsc();
				try {
					rbnb.getSerializationManager().getObjectFromUrl(b, msc.getLibraryUrl(friendId, lastUpdated));
				} catch (Exception e) {
					log.error("Error getting library", e);
				}
				Library nLib = new Library(b.build());
				log.debug("Received updated library for "+u.getEmail()+": "+nLib.getTracks().size()+" new tracks");
				if (nLib.getTracks().size() > 0) {
					Library cLib;
					synchronized (LibraryService.this) {
						cLib = libs.get(friendId);
						if(cLib == null) {
							libs.put(friendId, nLib);
							cLib = nLib;
						} else
							cLib.getTracks().putAll(nLib.getTracks());
					}
					// Make sure we have the streams
					for (String sid : nLib.getTracks().keySet()) {
						rbnb.getMetadataService().getStream(sid);
					}
					rbnb.getEventService().fireLibraryUpdated(cLib);
				}
			}
			statusText = "Done.";
			completion = 1f;
			fireUpdated();
			lastUpdated = now();
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
			super(LIBRARY_UPDATE_DELAY * 1000, rbnb.getExecutor());
		}

		@Override
		protected void runBatch(Collection<LibraryTrack> tracks) throws Exception {
			if(tracks.size() == 0)
				return;
			Library lib = new Library();
			for (LibraryTrack t : tracks) {
				lib.getTracks().put(t.streamId, t.dateAdded);
			}
			User me = rbnb.getUsersService().getMyUser();
			MetadataServerConfig msc = rbnb.getUsersService().getMsc();
			rbnb.getSerializationManager().putObjectToUrl(lib.toMsg(), msc.getLibraryAddUrl(me.getUserId()));
		}
	}

	class DelBatcher extends Batcher<String> {
		public DelBatcher() {
			super(LIBRARY_UPDATE_DELAY * 1000, rbnb.getExecutor());
		}

		@Override
		protected void runBatch(Collection<String> streamIds) throws Exception {
			if(streamIds.size() == 0)
				return;
			Library lib = new Library();
			for (String sid : streamIds) {
				lib.getTracks().put(sid, null);
			}
			User me = rbnb.getUsersService().getMyUser();
			MetadataServerConfig msc = rbnb.getUsersService().getMsc();
			rbnb.getSerializationManager().putObjectToUrl(lib.toMsg(), msc.getLibraryDelUrl(me.getUserId()));
		}
	}
}
