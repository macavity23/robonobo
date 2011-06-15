package com.robonobo.core.api.model;

import java.util.Date;

public abstract class Track {
	public enum PlaybackStatus {
		None, Queued, Downloading, Starting, Playing, Paused
	};

	public Stream stream;
	protected PlaybackStatus playbackStatus;
	protected TransferStatus transferStatus;
	private int dlRate, ulRate;
	private Date dateAdded;

	public Track(Stream stream) {
		this.stream = stream;
	}

	public Stream getStream() {
		return stream;
	}

	public PlaybackStatus getPlaybackStatus() {
		return playbackStatus;
	}

	public void setPlaybackStatus(PlaybackStatus playbackStatus) {
		 this.playbackStatus = playbackStatus;
	}

	/**
	 * This is the status of the search result/download/share, i.e. '112
	 * sources', 'Downloading', 'Ready'
	 */
	public TransferStatus getTransferStatus() {
		return transferStatus;
	}

	public void setRates(int dl, int ul) {
		dlRate = dl;
		ulRate = ul;
	}

	/** bytes/sec */
	public int getDownloadRate() {
		return dlRate;
	}

	/** bytes/sec */
	public int getUploadRate() {
		return ulRate;
	}

	public Date getDateAdded() {
		return dateAdded;
	}

	public void setDateAdded(Date dateAdded) {
		this.dateAdded = dateAdded;
	}
	
	/** Tracks will equal() each other if they have the same stream id, even if they are different classes */
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof Track))
			return false;
		Track t = (Track) o;
		return stream.streamId.equals(t.stream.streamId);
	}
	
	@Override
	public int hashCode() {
		return stream.streamId.hashCode();
	}
}
