package com.robonobo.core.toolkit.audio;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.robonobo.common.concurrent.CatchingRunnable;

@Deprecated
public class GenericAudioPlayback extends CatchingRunnable {
	Log log = LogFactory.getLog(getClass());
	InputStream inputStream;
	boolean isRunning = false;
	SourceDataLine line;
	AudioInputStream din = null;

	public GenericAudioPlayback() {
	}

	public GenericAudioPlayback(InputStream in) {
		this();
		this.inputStream = new BufferedInputStream(in);
	}

	public synchronized void setVolume(float v) {
		if(v < 0)
			v = 0f;
		if(v > 1)
			v = 1f;
		float gain = (float) (20d * Math.log(v) / Math.log(10));
		if(getLine() != null) {
			FloatControl volume = (FloatControl) getLine().getControl(FloatControl.Type.MASTER_GAIN);
			if(volume != null)
				volume.setValue(gain);
		}
	}

	public synchronized float getVolume() {
		if(getLine() != null) {
			FloatControl volume = (FloatControl) getLine().getControl(FloatControl.Type.MASTER_GAIN);
			if(volume != null) {
				float gain = volume.getValue();
				return (float) Math.pow(10, gain / 20);
			}
		}
		return 0;
	}

	public SourceDataLine getLine() {
		return line;
	}

	public void doRun() throws IOException, LineUnavailableException, UnsupportedAudioFileException {
		isRunning = true;
		AudioInputStream in = null;
		while(in == null && isRunning) {
			try {
				in = AudioSystem.getAudioInputStream(inputStream);
			} catch(UnsupportedAudioFileException e) {
				log.info("Failed to read from stream, trying again", e);
				try {
					Thread.sleep(1000);
				} catch(InterruptedException ex) {
					Thread.currentThread().interrupt();
					return;
				}
			}
		}
		AudioFormat baseFormat = in.getFormat();
		AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, baseFormat.getSampleRate(), 16, baseFormat.getChannels(), baseFormat.getChannels() * 2, baseFormat.getSampleRate(), false);
		din = AudioSystem.getAudioInputStream(decodedFormat, in);
		byte[] data = new byte[2048];
		line = getLine(decodedFormat);
		if(line != null && isRunning) {
			line.start();
			int nBytesRead = 0, nBytesWritten = 0;
			while(nBytesRead != -1 && isRunning) {
				nBytesRead = din.read(data, 0, data.length);
				if(nBytesRead != -1)
					nBytesWritten = line.write(data, 0, nBytesRead);
			}
			line.drain();
			line.stop();
			line.close();
			din.close();
		}
		in.close();
		isRunning = false;
		synchronized(this) {
			notifyAll();
		}
	}

	public boolean isRunning() {
		return isRunning;
	}

	public void stop() {
		isRunning = false;
		try {
			if(din != null)
				din.close();
			if(line != null)
				line.close();
		} catch(IOException e) {
		}
	}

	private SourceDataLine getLine(AudioFormat audioFormat) throws LineUnavailableException {
		SourceDataLine res = null;
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
		res = (SourceDataLine) AudioSystem.getLine(info);
		res.open(audioFormat);
		return res;
	}
}
