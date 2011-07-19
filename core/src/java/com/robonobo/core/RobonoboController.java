package com.robonobo.core;

import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.core.api.*;
import com.robonobo.core.api.config.RobonoboConfig;
import com.robonobo.core.api.model.*;
import com.robonobo.core.metadata.PlaylistCallback;
import com.robonobo.core.metadata.UserConfigCallback;
import com.robonobo.core.wang.WangListener;
import com.robonobo.mina.external.*;

/** Main external-facing Robonobo class
 * 
 * @author macavity */
public class RobonoboController {
	private RobonoboInstance inst;
	private Log log;

	public RobonoboController(String[] args) throws Exception {
		inst = new RobonoboInstance(args);
		log = LogFactory.getLog(getClass());
	}

	public String getVersion() {
		return inst.getVersion();
	}

	public void addTrackListener(TrackListener listener) {
		inst.getEventService().addTrackListener(listener);
	}

	public void removeTrackListener(TrackListener listener) {
		inst.getEventService().removeTrackListener(listener);
	}

	public void addPlaybackListener(PlaybackListener l) {
		inst.getEventService().addPlaybackListener(l);
	}

	public void removePlaybackListener(PlaybackListener l) {
		inst.getEventService().removePlaybackListener(l);
	}

	public void addRobonoboStatusListener(RobonoboStatusListener l) {
		inst.getEventService().addStatusListener(l);
	}

	public void removeRobonoboStatusListener(RobonoboStatusListener l) {
		inst.getEventService().removeStatusListener(l);
	}

	public void addLoginListener(LoginListener l) {
		inst.getEventService().addLoginListener(l);
	}

	public void removeLoginListener(LoginListener l) {
		inst.getEventService().removeLoginListener(l);
	}

	public void addUserListener(UserListener l) {
		inst.getEventService().addUserListener(l);
	}

	public void removeUserListener(UserListener l) {
		inst.getEventService().removeUserListener(l);
	}

	public void addPlaylistListener(PlaylistListener l) {
		inst.getEventService().addPlaylistListener(l);
	}

	public void removePlaylistListener(PlaylistListener l) {
		inst.getEventService().removePlaylistListener(l);
	}

	public void addLibraryListener(LibraryListener l) {
		inst.getEventService().addLibraryListener(l);
	}

	public void removeLibraryListener(LibraryListener l) {
		inst.getEventService().removeLibraryListener(l);
	}

	public void addWangListener(WangListener l) {
		inst.getEventService().addWangListener(l);
	}

	public void removeWangListener(WangListener l) {
		inst.getEventService().removeWangListener(l);
	}

	public void addTransferSpeedListener(TransferSpeedListener l) {
		inst.getEventService().addTransferSpeedListener(l);
	}

	public void removeTransferSpeedListener(TransferSpeedListener l) {
		inst.getEventService().removeTransferSpeedListener(l);
	}

	public void addTaskListener(TaskListener l) {
		inst.getEventService().addTaskListener(l);
	}

	public void removeTaskListener(TaskListener l) {
		inst.getEventService().removeTaskListener(l);
	}

	public void addNodeFilter(NodeFilter nf) {
		inst.getMina().addNodeFilter(nf);
	}

	public void removeNodeFilter(NodeFilter nf) {
		inst.getMina().removeNodeFilter(nf);
	}

	public RobonoboStatus getStatus() {
		return inst.getStatus();
	}

	public List<File> getITunesLibrary(FileFilter filter) throws RobonoboException {
		try {
			return inst.getITunesService().getAllITunesFiles(filter);
		} catch (IOException e) {
			throw new RobonoboException(e);
		}
	}

	public Map<String, List<File>> getITunesPlaylists(FileFilter filter) throws RobonoboException {
		try {
			return inst.getITunesService().getAllITunesPlaylists(filter);
		} catch (IOException e) {
			throw new RobonoboException(e);
		}
	}

	/** Notifies that this streamId is going to be played shortly and should be downloaded/prioritized as necessary
	 * 
	 * @throws RobonoboException */
	public void preFetch(String streamId) throws RobonoboException {
		if (inst.getShareService().getShare(streamId) != null)
			return;
		inst.getDownloadService().preFetch(streamId);
	}

	public void addDownload(String streamId) throws RobonoboException {
		inst.getDownloadService().addDownload(streamId);
	}

	/** Spawns off a thread to download any tracks that are not already being downloaded or shared - returns immediately */
	public void spawnNecessaryDownloads(final Collection<String> streamIds) {
		getExecutor().execute(new CatchingRunnable() {
			public void doRun() throws Exception {
				for (String sid : streamIds) {
					Track t = getTrack(sid);
					if (t instanceof CloudTrack)
						addDownload(sid);
				}
			}
		});
	}

	public Stream addShare(String pathToFile) throws RobonoboException {
		File f = new File(pathToFile);
		Stream s;
		try {
			s = inst.getFormatService().getStreamForFile(f);
		} catch (IOException e) {
			throw new RobonoboException(e);
		}
		inst.getStreamService().putStream(s);
		inst.getShareService().addShare(s, f);
		return s;
	}

	public DownloadingTrack getDownload(String streamId) {
		return inst.getDownloadService().getDownload(streamId);
	}

	public Stream getKnownStream(String streamId) {
		return inst.getStreamService().getKnownStream(streamId);
	}

	public SharedTrack getShare(String streamId) {
		return inst.getDbService().getShare(streamId);
	}

	public SharedTrack getShareByFilePath(File path) {
		return inst.getShareService().getShareByFilePath(path.getAbsolutePath());
	}

	public void deleteDownload(String streamId) throws RobonoboException {
		inst.getDownloadService().deleteDownload(streamId);
	}

	public void deleteDownloads(List<String> sids) throws RobonoboException {
		inst.getDownloadService().deleteDownloads(sids);
	}

	public void deleteShare(String streamId) throws RobonoboException {
		inst.getShareService().deleteShare(streamId);
	}

	public int getNumSharesAndDownloads() {
		return inst.getDbService().numSharesAndDownloads();
	}

	public String getMyNodeId() {
		return inst.getMina().getMyNodeId();
	}

	public List<String> getMyEndPointUrls() {
		return inst.getMina().getMyEndPointUrls();
	}

	public List<ConnectedNode> getConnectedNodes() {
		return inst.getMina().getConnectedNodes();
	}

	public List<String> getConnectedSources(String streamId) {
		return inst.getMina().getConnectedSources(streamId);
	}

	public List<String> getDownloads() {
		return inst.getDbService().getDownloads();
	}

	public String getMimeTypeForFile(File f) {
		return inst.getFormatService().getMimeTypeForFile(f);
	}

	public int getNumShares() {
		return inst.getDbService().getNumShares();
	}

	public Set<String> getShareStreamIds() {
		return inst.getDbService().getShareSids();
	}

	public List<SharedTrack> getSharesByPattern(String searchPattern) {
		ArrayList<SharedTrack> result = new ArrayList<SharedTrack>();
		result.addAll(inst.getShareService().getSharesByPattern(searchPattern));
		sortShares(result);
		return result;
	}

	public boolean isNetworkRunning() {
		if (inst.getMina() == null)
			return false;
		return inst.getMina().isStarted();
	}

	public void pauseDownload(String streamId) {
		inst.getDownloadService().pauseDownload(streamId);
	}

	private void sortShares(ArrayList<SharedTrack> result) {
		// Doesn't really matter how we sort as long as it's consistent
		Collections.sort(result, new Comparator<SharedTrack>() {
			public int compare(SharedTrack s1, SharedTrack s2) {
				return s1.getStream().getTitle().compareTo(s2.getStream().getTitle());
			}
		});
	}

	public void start() throws RobonoboException {
		inst.start();
	}

	public Track getTrack(String streamId) {
		return inst.getTrackService().getTrack(streamId);
	}

	/** Download must already be added */
	public void startDownload(String streamId) throws RobonoboException {
		try {
			inst.getDownloadService().startDownload(streamId);
		} catch (Exception e) {
			throw new RobonoboException(e);
		}
	}

	public void search(String query, int startResult, SearchListener listener) {
		inst.getSearchService().startSearch(query, startResult, listener);
	}

	public int numUsefulSources(String streamId) {
		return inst.getMina().numSources(streamId);
	}

	/** @param streamId
	 * @return */
	public Set<String> getSources(String streamId) {
		return inst.getMina().getSources(streamId);
	}

	public void findSources(String streamId, FoundSourceListener listener) {
		inst.getMina().addFoundSourceListener(streamId, listener);
	}

	public void stopFindingSources(String streamId, FoundSourceListener listener) {
		inst.getMina().removeFoundSourceListener(streamId, listener);
	}

	public void shutdown() {
		inst.getEventService().removeAllListeners();
		inst.shutdown();
	}

	public boolean haveAllSharesStarted() {
		return inst.getTrackService().haveAllSharesStarted();
	}

	public ScheduledThreadPoolExecutor getExecutor() {
		return inst.getExecutor();
	}

	public void play(String streamId) {
		inst.getPlaybackService().play(streamId);
	}

	public void pause() {
		inst.getPlaybackService().pause();
	}

	/** @param ms
	 *            Position in the stream to seek to, as millisecs from stream start */
	public void seek(long ms) {
		inst.getPlaybackService().seek(ms);
	}

	/** If we are playing, pause. If we are paused, play. Otherwise, do nothing */
	public void togglePlayPause() {
		inst.getPlaybackService().togglePlayPause();
	}

	public void stopPlayback() {
		inst.getPlaybackService().stop();
	}

	public Stream currentPlayingStream() {
		String sid = inst.getPlaybackService().getCurrentStreamId();
		if (sid == null)
			return null;
		return inst.getStreamService().getKnownStream(sid);
	}

	public List<File> getWatchDirs() {
		return inst.getDbService().getWatchDirs();
	}

	public void addWatchDir(final File dir) {
		inst.getDbService().putWatchDir(dir);
		// This may take a while, kick it off in another thread
		inst.getExecutor().execute(new CatchingRunnable() {
			public void doRun() throws Exception {
				inst.getShareService().checkWatchDir(dir);
			}
		});
	}

	public void deleteWatchDir(File dir) {
		inst.getDbService().deleteWatchDir(dir);
	}

	/** Attempts to login with the supplied details. This method returns immediately - to know the result, add a
	 * LoginListener before you call this */
	public void login(String email, String password) {
		inst.getUserService().login(email, password);
	}

	public User getMyUser() {
		return inst.getUserService().getMyUser();
	}

	public UserConfig getMyUserConfig() {
		return inst.getUserService().getMyUserConfig();
	}

	public void fetchMyUserConfig(UserConfigCallback handler) {
		inst.getUserService().refreshMyUserConfig(handler);
	}

	public double getBankBalance() throws RobonoboException {
		try {
			return inst.getWangService().getBankBalance();
		} catch (CurrencyException e) {
			throw new RobonoboException(e);
		}
	}

	public double getOnHandBalance() throws RobonoboException {
		try {
			return inst.getWangService().getOnHandBalance();
		} catch (CurrencyException e) {
			throw new RobonoboException(e);
		}
	}

	public User getUser(String email) {
		return inst.getUserService().getUser(email);
	}

	public User getUser(long userId) {
		return inst.getUserService().getUser(userId);
	}

	public void getOrFetchPlaylist(long playlistId, PlaylistCallback handler) {
		inst.getPlaylistService().getOrFetchPlaylist(playlistId, handler);
	}

	public Playlist getKnownPlaylist(long playlistId) {
		return inst.getPlaylistService().getExistingPlaylist(playlistId);
	}

	public Playlist getMyPlaylistByTitle(String title) {
		return inst.getPlaylistService().getMyPlaylistByTitle(title);
	}

	public void postPlaylistServiceUpdate(final String service, final long playlistId, final String msg) {
		getExecutor().execute(new CatchingRunnable() {
			public void doRun() throws Exception {
				inst.getPlaylistService().postPlaylistUpdateToService(service, playlistId, msg);
			}
		});
	}

	public void checkUsersUpdate() {
		inst.getUserService().checkAllUsersUpdate();
	}

	public void updatePlaylist(Playlist p) {
		inst.getPlaylistService().updatePlaylist(p);
		inst.getEventService().firePlaylistChanged(p);
	}

	public void createPlaylist(Playlist p, PlaylistCallback handler) {
		inst.getPlaylistService().createPlaylist(p, handler);
	}

	public void addFriends(Collection<String> emails) {
		inst.getUserService().addFriends(emails);
	}

	public void sharePlaylist(Playlist p, Set<Long> friendIds, Set<String> emails) throws RobonoboException {
		try {
			inst.getPlaylistService().sharePlaylist(p, friendIds, emails);
		} catch (IOException e) {
			throw new RobonoboException(e);
		}
	}

	public void deletePlaylist(Playlist pl) {
		inst.getPlaylistService().deletePlaylist(pl);
	}

	public void checkPlaylistUpdate(long playlistId) throws RobonoboException {
		inst.getPlaylistService().checkPlaylistUpdate(playlistId);
	}

	public PlaylistConfig getPlaylistConfig(long playlistId) {
		return inst.getDbService().getPlaylistConfig(playlistId);
	}

	public void putPlaylistConfig(PlaylistConfig pc) {
		inst.getDbService().putPlaylistConfig(pc);
	}

	public void saveUserConfigItem(String itemName, String itemVal) {
		inst.getUserService().saveUserConfigItem(itemName, itemVal);
	}

	public void runTask(Task t) {
		inst.getTaskService().runTask(t);
	}

	public RobonoboConfig getConfig() {
		return inst.getConfig();
	}

	public Object getConfig(String cfgName) {
		return inst.getConfig(cfgName);
	}

	public void saveConfig() {
		inst.saveConfig();
	}

	public int numUnseenTracks(Playlist p) {
		return inst.getDbService().numUnseenTracks(p);
	}

	public void markAllAsSeen(Playlist p) {
		inst.getDbService().markAllAsSeen(p);
	}

	public void markAllLibraryTracksAsSeen(long userId) {
		inst.getDbService().markAllLibraryTracksAsSeen(userId);
	}

	public void requestTopUp() throws IOException {
		inst.getHttpService().requestTopUp();
	}

	public Library getFriendLibrary(long userId) {
		return inst.getDbService().getLibrary(userId);
	}

	public UpdateInfo getUpdateInfo() throws RobonoboException {
		try {
			return inst.getHttpService().getUpdateInfo();
		} catch (IOException e) {
			throw new RobonoboException(e);
		}
	}

	public void setHandoverHandler(HandoverHandler handler) {
		inst.getMina().setHandoverHandler(handler);
	}

	/** For debugging only */
	public Connection getMetadataDbConnection() throws SQLException {
		return inst.getDbService().getConnection();
	}

	/** For debugging only */
	public void returnMetadataDbConnection(Connection conn) {
		inst.getDbService().returnConnection(conn);
	}

	/** For debugging only */
	public Connection getPageDbConnection() throws SQLException {
		return inst.getStorageService().getPageDbConnection();
	}

	/** For debugging only */
	public void returnPageDbConnection(Connection conn) throws SQLException {
		inst.getStorageService().returnPageDbConnection(conn);
	}
}
