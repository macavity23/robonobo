package net.robonobo.plugin.mp3.test;

import java.io.FileInputStream;

import org.apache.log4j.BasicConfigurator;

import com.robonobo.core.toolkit.audio.GenericAudioPlayback;


public class TestMp3Player {

	public static void main(String[] args) throws Exception {
		BasicConfigurator.configure();
		System.out.println(args[0]);
		GenericAudioPlayback playback = new GenericAudioPlayback(new FileInputStream(args[0]));
		playback.doRun();
	}
}
