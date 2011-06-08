package com.robonobo.core.api;

public interface AudioPlayerListener {
	public void onCompletion();
	public void onError(String error);
	public void onProgress(long microsecs);
	public void playbackStarted();
}
