package com.robonobo.core.toolkit.audio;

import java.io.InputStream;

import com.robonobo.common.util.Queue;

@Deprecated
public class AudioPlayback {
	Thread playerThread = null;
	GenericAudioPlayback player;
	boolean muted = false;
	float volume = 0f;
	Queue playlist = new Queue();
	
	public AudioPlayback() {
	}
	
	public synchronized void play(InputStream in) {
		if(player != null)
			stop();
		
		player = new GenericAudioPlayback(in);
		player.setVolume(volume);
		
		playerThread = new Thread(player);
		playerThread.setName("AudioPlayback");
		playerThread.start();
	}
	
	public synchronized void stop() {
		if(player != null) {
			player.setVolume(0);
			player.stop();
			player = null;
		}
		
		playerThread = null;
	}
	
	public void setVolume(float vol) {
		volume = vol;
		if(player != null)
			player.setVolume(vol);
	}
	
	public float getVolume() {
		if(player != null)
			return player.getVolume();
		else
			return 0;
	}
	
	public boolean isMuted() {
		return muted;
	}
	
	public void mute() {
		if(player != null && !muted) {
			muted = true;
			volume = player.getVolume();
			player.setVolume(0);
		}
	}
	
	public void unmute() {
		if(player != null && muted) {
			muted = false;
			player.setVolume(this.volume);
		}
	}
}
