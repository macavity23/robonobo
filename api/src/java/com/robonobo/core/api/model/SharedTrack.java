package com.robonobo.core.api.model;

import java.io.File;

public class SharedTrack extends Track {
	public enum ShareStatus {
		Paused, Sharing
	};

	private File file;
	private ShareStatus shareStatus;

	public SharedTrack(Stream stream, File file, ShareStatus status) {
		super(stream);
		this.file = file;
		this.shareStatus = status;
		transferStatus = new SharingTransferStatus();
	}

	public SharedTrack(SharedTrack t) {
		super(t);
		file = t.file;
		shareStatus = t.shareStatus;
	}

	@Override
	public SharedTrack clone() {
		return new SharedTrack(this);
	}

	public File getFile() {
		return file;
	}

	public ShareStatus getShareStatus() {
		return shareStatus;
	}
}
