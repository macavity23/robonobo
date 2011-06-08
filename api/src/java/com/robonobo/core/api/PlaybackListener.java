package com.robonobo.core.api;


/**
 * Notified when audio playback is started/stopped/paused
 */
public interface PlaybackListener {
	public void playbackStarting();
	public void playbackRunning();
	public void playbackPaused();
	public void playbackStopped();
	public void playbackCompleted();
	public void playbackProgress(long microsecs);
	public void playbackError(String error);
	/**
	 * After seekStarted is called, playbackProgress calls may be inaccurate until seekFinished is called
	 */
	public void seekStarted();
	public void seekFinished();
}
