package com.robonobo.core.api;

import java.io.IOException;

public interface AudioPlayer {
	public enum Status {
		Starting, Playing, Paused, Stopped
	};

	public void play() throws IOException;

	public void pause() throws IOException;

	public void stop();

	public void seek(long ms) throws IOException;
	
	public void addListener(AudioPlayerListener listener);

	public void removeListener(AudioPlayerListener listener);
	
	public Status getStatus();
}
