package com.robonobo.core.service;

import static com.robonobo.common.util.FileUtil.*;
import static com.robonobo.common.util.TimeUtil.*;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.robonobo.common.concurrent.Batcher;
import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.common.exceptions.SeekInnerCalmException;
import com.robonobo.common.pageio.buffer.FilePageBuffer;
import com.robonobo.common.util.FileUtil;
import com.robonobo.core.api.RobonoboException;
import com.robonobo.core.api.Task;
import com.robonobo.core.api.model.*;
import com.robonobo.core.api.model.DownloadingTrack.DownloadStatus;
import com.robonobo.core.api.model.SharedTrack.ShareStatus;
import com.robonobo.core.storage.StorageService;
import com.robonobo.mina.external.MinaControl;
import com.robonobo.mina.external.buffer.PageBuffer;
import com.robonobo.spi.FormatSupportProvider;

/** Responsible for translating mina Broadcasts to robonobo Shares
 * 
 * @author macavity */
public class ShareService extends AbstractService {
	private static final long START_SHARE_BATCH_TIME = 1000L;
	/** Seconds */
	// public static final int WATCHDIR_CHECK_FREQ = 60 * 10;
	public static final int WATCHDIR_CHECK_FREQ = 30;
	public static final int WATCHDIR_INITIAL_WAIT = 30;
	Log log = LogFactory.getLog(getClass());
	DbService db;
	EventService event;
	UserService users;
	StorageService storage;
	StreamService streams;
	PlaybackService playback;
	MinaControl mina;
	/** These are shares that have been added but whose files are no longer present - we keep track of these in case they
	 * are on removable drives that might be plugged back in later, but we don't show them to the user */
	Set<String> defunctShareSids = new HashSet<String>();
	private ScheduledFuture<?> watchDirTask;
	private ScheduledFuture<?> fetchMyCommentsTask;
	private boolean watchDirRunning = false;
	private CommentService comments;

	public ShareService() {
		addHardDependency("core.mina");
		addHardDependency("core.streams");
		addHardDependency("core.storage");
		addHardDependency("core.comments");
	}

	@Override
	public void startup() throws Exception {
		db = rbnb.getDbService();
		event = rbnb.getEventService();
		users = rbnb.getUserService();
		storage = rbnb.getStorageService();
		streams = rbnb.getStreamService();
		playback = rbnb.getPlaybackService();
		comments = rbnb.getCommentService();
		mina = rbnb.getMina();
		watchDirTask = getRobonobo().getExecutor().scheduleWithFixedDelay(new WatchDirChecker(), WATCHDIR_INITIAL_WAIT, WATCHDIR_CHECK_FREQ, TimeUnit.SECONDS);
	}

	@Override
	public void shutdown() {
		// Don't specifically stop our shares, the mina shutdown will stop them
		watchDirTask.cancel(true);
		if (fetchMyCommentsTask != null)
			fetchMyCommentsTask.cancel(true);
	}

	/** If there is a defunct share (ie one with a non-existent file behind it), nuke it */
	public void nukeDefunctShare(String sid) {
		boolean isDefunct;
		synchronized (this) {
			isDefunct = defunctShareSids.contains(sid);
			if (isDefunct)
				defunctShareSids.remove(sid);
		}
		if (isDefunct) {
			storage.nukePageBuf(sid);
			db.deleteShare(sid);
		}
	}

	/** NB The stream referenced by stream id must already have been put into streamservice
	 * 
	 * @param streamId
	 * @param dataFile
	 * @throws RobonoboException */
	public void addShare(Stream s, File dataFile) throws RobonoboException {
		String streamId = s.getStreamId();
		log.info("Adding share for id " + streamId + " at " + dataFile.getAbsolutePath());
		// If we have a defunct share for this (ie one with a nonexistent file behind it), replace it with this one
		nukeDefunctShare(streamId);
		SharedTrack sh = db.getShare(streamId);
		if (sh != null) {
			log.info("Not adding share for id " + streamId + " - sharing already");
			return;
		}
		sh = new SharedTrack(s, dataFile, ShareStatus.Sharing);
		try {
			PageBuffer pb = storage.createPageBufForShare(sh.getStream(), sh.getFile(), true);
			FormatSupportProvider fsp = rbnb.getFormatService().getFormatSupportProvider(s.getMimeType());
			if (fsp == null)
				throw new IOException("No FSP available for the mimeType " + s.getMimeType());
			log.debug("Paginating " + dataFile.getAbsolutePath());
			fsp.paginate(dataFile, pb);
		} catch (IOException e) {
			throw new RobonoboException(e);
		}
		sh.setDateAdded(now());
		db.putShare(sh);
		startShare(streamId);
		rbnb.getLibraryService().addToMyLibrary(streamId);
		event.fireTrackUpdated(s.getStreamId());
		event.fireMyLibraryUpdated();
		rbnb.getPlaylistService().checkPlaylistsForNewShare(sh);
	}

	private File fileForFinishedDownload(Stream s) {
		String artist = s.getAttrValue("artist");
		if (artist == null)
			artist = "Unknown Artist";
		String album = s.getAttrValue("album");
		if (album == null)
			album = "Unknown Album";
		String sep = File.separator;
		File targetDir = new File(rbnb.getConfig().getFinishedDownloadsDirectory() + sep + makeFileNameSafe(artist) + sep + makeFileNameSafe(album));
		targetDir.mkdirs();
		String fileExt = rbnb.getFormatService().getFormatSupportProvider(s.getMimeType()).getDefaultFileExtension();
		return new File(targetDir, makeFileNameSafe(s.getTitle()) + "." + fileExt);
	}

	public void addShareFromCompletedDownload(DownloadingTrack d) throws RobonoboException {
		Stream s = d.getStream();
		log.debug("Adding share for completed download " + s.getStreamId());
		if (d.getDownloadStatus() != DownloadStatus.Finished) {
			throw new SeekInnerCalmException();
		}
		File curFile = d.getFile();
		File shareFile = fileForFinishedDownload(s);
		// Copy the file rather than move, as windoze won't allow us to move it while we're
		// accessing it (unix does it just fine *while* allowing us to keep reading from the file descriptor...)
		try {
			FileUtil.copyFile(curFile, shareFile);
		} catch (IOException e) {
			throw new RobonoboException(e);
		}
		curFile.deleteOnExit();
		SharedTrack sh = new SharedTrack(s, shareFile, ShareStatus.Sharing);
		sh.setDateAdded(now());
		db.putShare(sh);
		try {
			rbnb.getStorageService().createPageBufForShare(s, shareFile, false);
		} catch (IOException e) {
			throw new RobonoboException(e);
		}
		startShare(s.getStreamId());
		event.fireTrackUpdated(s.getStreamId());
		rbnb.getPlaylistService().checkPlaylistsForNewShare(sh);
	}

	public void deleteShare(String streamId) {
		log.info("Deleting share for stream " + streamId);
		playback.stopForDeletedStream(streamId);
		SharedTrack share = db.getShare(streamId);
		if (share == null)
			return;
		stopShare(streamId);
		db.deleteShare(streamId);
		storage.nukePageBuf(streamId);
		rbnb.getLibraryService().delFromMyLibrary(streamId);
		event.fireTrackUpdated(streamId);
		event.fireMyLibraryUpdated();
	}

	private void startShare(String streamId) throws RobonoboException {
		mina.startBroadcast(streamId);
	}

	private void stopShare(String streamId) {
		mina.stopBroadcast(streamId);
	}

	public String getName() {
		return "Share service";
	}

	public String getProvides() {
		return "core.shares";
	}

	public SharedTrack getShare(String streamId) {
		synchronized (this) {
			if (defunctShareSids.contains(streamId))
				return null;
		}
		SharedTrack share = db.getShare(streamId);
		return share;
	}

	public Collection<SharedTrack> getSharesByPattern(String searchPattern) {
		Collection<SharedTrack> shares = db.getSharesByPattern(searchPattern);
		return shares;
	}

	public SharedTrack getShareByFilePath(String filePath) {
		return db.getShareByFilePath(filePath);
	}

	public void checkWatchDir(File watchDir) throws RobonoboException {
		synchronized (this) {
			if (watchDirRunning)
				return;
			watchDirRunning = true;
		}
		try {
			log.debug("Checking watch dir " + watchDir.getAbsolutePath());
			for (File itemInWatchDir : watchDir.listFiles()) {
				if (!db.haveCheckedFile(itemInWatchDir)) {
					List<File> mp3z = FileUtil.getFilesWithinPath(itemInWatchDir, "mp3");
					for (File mp3File : mp3z) {
						log.debug("Adding share from file " + mp3File.getAbsolutePath());
						SharedTrack sh = getShareByFilePath(mp3File.getAbsolutePath());
						if (sh == null) {
							Stream s;
							try {
								s = getRobonobo().getFormatService().getStreamForFile(mp3File);
								streams.putStream(s);
								addShare(s, mp3File);
							} catch (Exception e) {
								log.error("Error adding file " + mp3File.getAbsolutePath() + ": " + e.getMessage());
								continue;
							}
						}
					}
					db.notifyFileChecked(itemInWatchDir);
				}
			}
		} finally {
			synchronized (this) {
				watchDirRunning = false;
			}
		}
	}

	void startAllShares() throws IOException, RobonoboException {
		if (db.getNumShares() > 0)
			rbnb.getTaskService().runTask(new StartSharesTask());
		else {
			rbnb.getTrackService().setAllSharesStarted(true);
			event.fireAllTracksLoaded();
			event.fireMyLibraryUpdated();
			startFetchingComments();
		}
	}

	public void startFetchingComments() {
		if (fetchMyCommentsTask != null)
			fetchMyCommentsTask.cancel(false);
		fetchMyCommentsTask = rbnb.getExecutor().scheduleAtFixedRate(new CatchingRunnable() {
			public void doRun() throws Exception {
				User me = users.getMyUser();
				if (me != null)
					comments.fetchCommentsForLibrary(me.getUserId());
			}
		}, 0, rbnb.getConfig().getUserUpdateFrequency(), TimeUnit.SECONDS);
	}

	class StartSharesTask extends Task {
		boolean finished = false;

		public StartSharesTask() {
			title = "Starting my shares";
		}

		@Override
		public void runTask() throws Exception {
			Set<String> shareSids = db.getShareSids();
			final int numToStart = shareSids.size();
			// Iterate over these and check that they're ok, punting them up to the gui in batches
			Batcher<String> puntToGuiBatcher = new Batcher<String>(START_SHARE_BATCH_TIME, rbnb.getExecutor()) {
				int i = 1;

				protected void runBatch(Collection<String> sids) throws Exception {
					rbnb.getMina().startBroadcasts(sids);
					event.fireTracksUpdated(sids);
					i += sids.size();
					if (!finished) {
						completion = ((float) i) / numToStart;
						statusText = "Starting share " + i + " of " + numToStart;
						fireUpdated();
					}
				}
			};
			statusText = "Starting share 1 of " + numToStart;
			fireUpdated();
			for (String sid : shareSids) {
				FilePageBuffer pb = storage.getPageBuf(sid, false);
				if (pb == null) {
					// Errot
					log.error("Found null pagebuf when starting share for " + sid + " - deleting share");
					db.deleteShare(sid);
					continue;
				}
				// If the file for this share doesn't exist, don't start this share but keep the reference around -
				// might be
				// a removable drive that will come back later
				File file = pb.getFile();
				if (!file.exists() || !file.canRead()) {
					log.error("Could not find or read from file " + file.getAbsolutePath() + " for stream " + sid + " - not starting share");
					synchronized (this) {
						defunctShareSids.add(sid);
					}
					continue;
				}
				puntToGuiBatcher.add(sid);
			}
			log.debug("Start Share thread finished: started " + shareSids.size() + " shares");
			finished = true;
			completion = 1f;
			statusText = "Done.";
			fireUpdated();
			rbnb.getTrackService().setAllSharesStarted(true);
			event.fireAllTracksLoaded();
			event.fireMyLibraryUpdated();
		}
	}

	private class WatchDirChecker extends CatchingRunnable {
		public WatchDirChecker() {
		}

		public void doRun() throws Exception {
			List<File> watchDirs = db.getWatchDirs();
			for (File watchDir : watchDirs) {
				checkWatchDir(watchDir);
			}
		}
	}
}
