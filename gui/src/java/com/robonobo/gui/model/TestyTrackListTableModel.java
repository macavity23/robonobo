package com.robonobo.gui.model;

import java.io.File;
import java.security.SecureRandom;
import java.util.*;

import org.doomdark.uuid.UUIDGenerator;

import com.robonobo.common.exceptions.SeekInnerCalmException;
import com.robonobo.core.RobonoboController;
import com.robonobo.core.api.model.*;
import com.robonobo.core.api.model.DownloadingTrack.DownloadStatus;
import com.robonobo.core.api.model.SharedTrack.ShareStatus;

@SuppressWarnings("serial")
public class TestyTrackListTableModel extends TrackListTableModel {
	Map<String, Track> trax = new HashMap<String, Track>();
	List<String> traxOrder = new ArrayList<String>();
	
	// numSources = -1 means Sharing, -2 means queued,
	private void add(String title, String artist, String trackNum, String year, String album, long duration, long size, int numSources, int bytesDownloaded) {
		Stream s = new Stream();
		s.setStreamId(UUIDGenerator.getInstance().generateRandomBasedUUID(new SecureRandom()).toString());
		s.setDuration(duration);
		s.setSize(size);
		s.setTitle(title);
		s.setAttrValue("artist", artist);
		s.setAttrValue("album", album);
		s.setAttrValue("track", trackNum);
		s.setAttrValue("year", year);
		Track t;
		if(bytesDownloaded > 0)
			t = new TestyDownloadingTrack(s, DownloadStatus.Downloading, numSources, bytesDownloaded);
		else if(numSources == -1)
			t = new SharedTrack(s, new File("/foo/bar"), ShareStatus.Sharing);
		else if(numSources == -2)
			t = new TestyDownloadingTrack(s, DownloadStatus.Paused, 0, 0);
		else
			t = new CloudTrack(s, numSources);
		trax.put(s.getStreamId(), t);
		traxOrder.add(s.getStreamId());
	}
	
	public TestyTrackListTableModel(RobonoboController control) {
		add("Mi Tierra", "Rodrigo y Gabriela", "01 of 18", "2005",
				"Guitarra de Pasion", 275000, 3700234, -1, 0);
		add("EI Sueno", "Rodrigo y Gabriela", "02 of 18", "2005",
				"Guitarra de Pasion", 303456, 4345987, -1, 0);
		add("EI Bambuquero", "Rodrigo y Gabriela", "03 of 18", "2005",
				"Guitarra de Pasion", 150846, 3109876, -1, 0);
		add("EI Ultimo Baile", "Rodrigo y Gabriela", "04 of 18", "2005",
				"Guitarra de Pasion", 250987, 3876787, 0, 0);
		add("Los Primos", "Rodrigo y Gabriela", "05 of 18", "2005",
				"Guitarra de Pasion", 280987, 2908765, 1, 0);
		add("Juntos", "Rodrigo y Gabriela", "06 of 18", "2005",
				"Guitarra de Pasion", 280987, 3109872, 2, 0);
		add("Alma Libre", "Rodrigo y Gabriela", "07 of 18", "2005",
				"Guitarra de Pasion", 209876, 1987267, 3, 0);
		add("Anoche", "Rodrigo y Gabriela", "09 of 18", "2005",
				"Guitarra de Pasion", 250987, 3879827, -2, 0);
		add("Cafe Colombia", "Rodrigo y Gabriela", "08 of 18", "2005",
				"Guitarra de Pasion", 4123574, 3908278, 1, 1098278);
		add("La Cumbia y la Luna", "Rodrigo y Gabriela", "10 of 18", "2005",
				"Guitarra de Pasion", 260987, 2876787, 2, 2109892);
	}
	
	@Override
	public Track getTrack(int index) {
		String streamId = traxOrder.get(index);
		return trax.get(streamId);
	}
	
	@Override
	public String getStreamId(int index) {
		return traxOrder.get(index);
	}
	
	@Override
	public int getTrackIndex(String streamId) {
		return traxOrder.indexOf(streamId);
	}
	
	@Override
	public int numTracks() {
		return traxOrder.size();
	}

	@Override
	public boolean allowDelete() {
		return false;
	}
	
	@Override
	public void deleteTracks(List<String> streamIds) {
		throw new SeekInnerCalmException();
	}
	
	class TestyDownloadingTrack extends DownloadingTrack {
		int bytesDownloaded;
		DownloadingTransferStatus transStat;
		
		public TestyDownloadingTrack(Stream stream, DownloadStatus status, int numSources, int bytesDownloaded) {
			super(stream, new File("/foo/bar"), status);
			this.bytesDownloaded = bytesDownloaded;
			transStat = new DownloadingTransferStatus(numSources);
			setNumSources(numSources);
		}
		
		@Override
		public long getBytesDownloaded() {
			return bytesDownloaded;
		}
		
		@Override
		public TransferStatus getTransferStatus() {
			return transStat;
		}
	}

}