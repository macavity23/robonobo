package com.robonobo.core.service;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.robonobo.core.api.*;
import com.robonobo.core.api.model.*;
import com.robonobo.core.wang.WangListener;
import com.robonobo.mina.external.*;

public class EventService extends AbstractService implements MinaListener {
	private List<TrackListener> trList = new ArrayList<TrackListener>();
	private List<PlaybackListener> plList = new ArrayList<PlaybackListener>();
	private List<UserPlaylistListener> upList = new ArrayList<UserPlaylistListener>();
	private List<RobonoboStatusListener> stList = new ArrayList<RobonoboStatusListener>();
	private List<WangListener> wList = new ArrayList<WangListener>();
	private List<TransferSpeedListener> tsList = new ArrayList<TransferSpeedListener>();
	private List<TaskListener> tlList = new ArrayList<TaskListener>();
	private List<LibraryListener> llList = new ArrayList<LibraryListener>();
	private int minaSupernodes = 0;
	private Log log = LogFactory.getLog(getClass());

	public EventService() {
	}

	public synchronized void addTrackListener(TrackListener listener) {
		trList.add(listener);
	}

	public synchronized void removeTrackListener(TrackListener listener) {
		trList.remove(listener);
	}

	public synchronized void addPlaybackListener(PlaybackListener l) {
		plList.add(l);
	}

	public synchronized void removePlaybackListener(PlaybackListener l) {
		plList.remove(l);
	}

	public synchronized void addStatusListener(RobonoboStatusListener l) {
		stList.add(l);
	}

	public synchronized void removeStatusListener(RobonoboStatusListener l) {
		stList.remove(l);
	}

	public synchronized void addUserPlaylistListener(UserPlaylistListener l) {
		upList.add(l);
	}
	
	public synchronized void removeUserPlaylistListener(UserPlaylistListener l) {
		upList.remove(l);
	}
	
	public synchronized void addLibraryListener(LibraryListener l) {
		llList.add(l);
	}
	
	public synchronized void removeLibraryListener(LibraryListener l) {
		llList.remove(l);
	}
	
	public synchronized void addWangListener(WangListener l) {
		wList.add(l);
	}
	
	public synchronized void removeWangListener(WangListener l) {
		wList.remove(l);
	}
	
	public synchronized void addTransferSpeedListener(TransferSpeedListener l) {
		tsList.add(l);
	}
	
	public synchronized void removeTransferSpeedListener(TransferSpeedListener l) {
		tsList.remove(l);
	}
	
	public synchronized void addTaskListener(TaskListener l) {
		tlList.add(l);
	}
	
	public synchronized void removeTaskListener(TaskListener l) {
		tlList.remove(l);
	}
	
	public void fireTrackUpdated(String streamId) {
		TrackListener[] arr;
		synchronized (this) {
			arr = getTrArr();
		}
		for (TrackListener listener : arr) {
			listener.trackUpdated(streamId);
		}
	}

	public void fireTracksUpdated(Collection<String> streamIds) {
		TrackListener[] arr;
		synchronized (this) {
			arr = getTrArr();
		}
		for (TrackListener listener : arr) {
			listener.tracksUpdated(streamIds);
		}
	}

	public void fireAllTracksLoaded() {
		TrackListener[] arr;
		synchronized (this) {
			arr = getTrArr();
		}
		for (TrackListener listener : arr) {
			listener.allTracksLoaded();
		}
	}
	
	public void firePlaybackStarted() {
		PlaybackListener[] arr;
		synchronized (this) {
			arr = getPlArr();
		}
		for (PlaybackListener listener : arr) {
			listener.playbackRunning();
		}
	}

	public void firePlaybackStarting() {
		PlaybackListener[] arr;
		synchronized (this) {
			arr = getPlArr();
		}
		for (PlaybackListener listener : arr) {
			listener.playbackStarting();
		}
	}

	public void firePlaybackPaused() {
		PlaybackListener[] arr;
		synchronized (this) {
			arr = getPlArr();
		}
		for (PlaybackListener listener : arr) {
			listener.playbackPaused();
		}
	}

	public void firePlaybackStopped() {
		PlaybackListener[] arr;
		synchronized (this) {
			arr = getPlArr();
		}
		for (PlaybackListener listener : arr) {
			listener.playbackStopped();
		}
	}

	public void firePlaybackCompleted() {
		PlaybackListener[] arr;
		synchronized (this) {
			arr = getPlArr();
		}
		for (PlaybackListener listener : arr) {
			listener.playbackCompleted();
		}
	}

	public void firePlaybackProgress(long microsecs) {
		PlaybackListener[] arr;
		synchronized (this) {
			arr = getPlArr();
		}
		for (PlaybackListener listener : arr) {
			listener.playbackProgress(microsecs);
		}
	}

	public void firePlaybackError(String error) {
		PlaybackListener[] arr;
		synchronized (this) {
			arr = getPlArr();
		}
		for (PlaybackListener listener : arr) {
			listener.playbackError(error);
		}
	}

	public void fireSeekStarted() {
		PlaybackListener[] arr;
		synchronized (this) {
			arr = getPlArr();
		}
		for (PlaybackListener listener : arr) {
			listener.seekStarted();
		}
	}

	public void fireSeekFinished() {
		PlaybackListener[] arr;
		synchronized (this) {
			arr = getPlArr();
		}
		for (PlaybackListener listener : arr) {
			listener.seekFinished();
		}
	}

	public void fireUserChanged(User u) {
		UserPlaylistListener[] arr;
		synchronized (this) {
			arr = getUpArr();
		}
		for (UserPlaylistListener listener : arr) {
			listener.userChanged(u);
		}
	}

	public void fireAllUsersAndPlaylistsUpdated() {
		UserPlaylistListener[] arr;
		synchronized (this) {
			arr = getUpArr();
		}
		for (UserPlaylistListener listener : arr) {
			listener.allUsersAndPlaylistsUpdated();
		}
	}

	public void firePlaylistChanged(Playlist p) {
		UserPlaylistListener[] arr;
		synchronized (this) {
			arr = getUpArr();
		}
		for (UserPlaylistListener listener : arr) {
			listener.playlistChanged(p);
		}
	}

	public void fireLoggedIn() {
		UserPlaylistListener[] arr;
		synchronized (this) {
			arr = getUpArr();
		}
		for (UserPlaylistListener listener : arr) {
			listener.loggedIn();
		}
	}

	public void fireLibraryChanged(Library lib, Set<String> newTracks) {
		LibraryListener[] arr;
		synchronized (this) {
			arr = getLlArr();
		}
		for (LibraryListener listener : arr) {
			listener.libraryChanged(lib, newTracks);
		}
	}

	public void fireMyLibraryUpdated() {
		LibraryListener[] arr;
		synchronized (this) {
			arr = getLlArr();
		}
		for (LibraryListener listener : arr) {
			listener.myLibraryUpdated();
		}
	}

	public void fireUserConfigChanged(UserConfig cfg) {
		UserPlaylistListener[] arr;
		synchronized (this) {
			arr = getUpArr();
		}
		for (UserPlaylistListener listener : arr) {
			listener.userConfigChanged(cfg);
		}
	}

	public void fireStatusChanged() {
		RobonoboStatusListener[] arr;
		synchronized (this) {
			arr = getStArr();
		}
		for (RobonoboStatusListener listener : arr) {
			listener.roboStatusChanged();
		}
	}

	public void fireConnectionAdded(ConnectedNode node) {
		RobonoboStatusListener[] arr;
		synchronized (this) {
			arr = getStArr();
		}
		for (RobonoboStatusListener listener : arr) {
			listener.connectionAdded(node);
		}
	}

	public void fireConnectionLost(ConnectedNode node) {
		RobonoboStatusListener[] arr;
		synchronized (this) {
			arr = getStArr();
		}
		for (RobonoboStatusListener listener : arr) {
			listener.connectionLost(node);
		}
	}

	public void removeAllListeners() {
		trList.clear();
		plList.clear();
		upList.clear();
		stList.clear();
	}

	public void nodeConnected(ConnectedNode node) {
		if(node.isSupernode()) {
			minaSupernodes++;
			if(minaSupernodes > 0 && getRobonobo().getUsersService().isLoggedIn()) {
				getRobonobo().setStatus(RobonoboStatus.Connected);
				fireStatusChanged();
			}
		}
		fireConnectionAdded(node);
	}

	public void nodeDisconnected(ConnectedNode node) {
		if (node.isSupernode()) {
			if (minaSupernodes == 1) {
				rbnb.setStatus(RobonoboStatus.NotConnected);
				fireStatusChanged();
			}
			if(minaSupernodes > 0)
				minaSupernodes--;
		}
		fireConnectionLost(node);
	}

	public void fireWangBalanceChanged(double newBalance) {
		WangListener[] arr;
		synchronized (this) {
			arr = getWArr();
		}
		for (WangListener listener : arr) {
			listener.balanceChanged(newBalance);
		}
	}
	
	public void fireWangAccountActivity(double creditValue, String narration) {
		WangListener[] arr;
		synchronized (this) {
			arr = getWArr();
		}
		for (WangListener listener : arr) {
			listener.accountActivity(creditValue, narration);
		}	
	}
	
	public void fireNewTransferSpeeds(Map<String, TransferSpeed> speedsByStream, Map<String, TransferSpeed> speedsByNode) {
		TransferSpeedListener[] arr;
		synchronized (this) {
			arr = getTSArr();
		}
		for (TransferSpeedListener listener : arr) {
			listener.newTransferSpeeds(speedsByStream, speedsByNode);
		}
	}

	public void fireTaskUpdated(Task t) {
		TaskListener[] arr;
		synchronized (this) {
			arr = getTLArr();
		}
		for (TaskListener listener : arr) {
			listener.taskUpdated(t);
		}
	}

	/** Copy the list of listeners, to remove deadlock possibilities */
	private TrackListener[] getTrArr() {
		TrackListener[] result = new TrackListener[trList.size()];
		trList.toArray(result);
		return result;
	}

	/** Copy the list of listeners, to remove deadlock possibilities */
	private PlaybackListener[] getPlArr() {
		PlaybackListener[] result = new PlaybackListener[plList.size()];
		plList.toArray(result);
		return result;
	}

	/** Copy the list of listeners, to remove deadlock possibilities */
	private UserPlaylistListener[] getUpArr() {
		UserPlaylistListener[] result = new UserPlaylistListener[upList.size()];
		upList.toArray(result);
		return result;
	}

	private LibraryListener[] getLlArr() {
		LibraryListener[] result = new LibraryListener[llList.size()];
		llList.toArray(result);
		return result;
	}

	private RobonoboStatusListener[] getStArr() {
		RobonoboStatusListener[] result = new RobonoboStatusListener[stList.size()];
		stList.toArray(result);
		return result;
	}

	private WangListener[] getWArr() {
		WangListener[] result = new WangListener[wList.size()];
		wList.toArray(result);
		return result;
	}

	private TransferSpeedListener[] getTSArr() {
		TransferSpeedListener[] result = new TransferSpeedListener[tsList.size()];
		tsList.toArray(result);
		return result;
	}

	private TaskListener[] getTLArr() {
		TaskListener[] result = new TaskListener[tlList.size()];
		tlList.toArray(result);
		return result;
	}

	public void receptionCompleted(String streamId) {
	}

	public void receptionConnsChanged(String streamId) {
		DownloadingTrack d = rbnb.getDownloadService().getDownload(streamId);
		if(d != null)
			fireTrackUpdated(streamId);
	}
	
	@Override
	public void shutdown() throws Exception {
	}

	@Override
	public void startup() throws Exception {
		// This will fire our status as 'starting'
		fireStatusChanged();
	}

	public String getName() {
		return "Event Service";
	}

	public String getProvides() {
		return "core.event";
	}
}
