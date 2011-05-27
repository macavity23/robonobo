package com.robonobo.gui.tasks;

import static com.robonobo.common.util.FileUtil.*;

import java.io.File;
import java.io.FileFilter;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;

import org.doomdark.uuid.UUIDGenerator;

import com.robonobo.core.RobonoboController;
import com.robonobo.core.api.RobonoboException;
import com.robonobo.core.api.Task;
import com.robonobo.core.api.model.*;

public class ImportITunesTask extends Task {
	private RobonoboController control;

	public ImportITunesTask(RobonoboController control) {
		this.control = control;
		title = "Importing from iTunes";
	}

	@Override
	public void runTask() throws Exception {
		log.info("Running import iTunes task");
		statusText = "Reading list of files from iTunes";
		completion = 0;
		fireUpdated();
		FileFilter mp3Filter = new FileFilter() {
			public boolean accept(File f) {
				return "mp3".equalsIgnoreCase(getFileExtension(f));
			}
		};
		List<File> files = control.getITunesLibrary(mp3Filter);
		Map<String, List<File>> itPls = control.getITunesPlaylists(mp3Filter);
		int i=0;
		int totalSz = files.size() + itPls.size();
		for (File file : files) {
			if(cancelRequested) {
				cancelConfirmed();
				return;
			}
			completion = (float) i / totalSz;
			i++;
			statusText = "Importing file "+i+" of " +files.size();
			fireUpdated();
			
			String filePath = file.getAbsolutePath();
			try {
				control.addShare(filePath);
			} catch (RobonoboException e) {
				log.error("Error adding share from file " + filePath, e);
				continue;
			}
		}
		
		i=0;
		for (String pName : itPls.keySet()) {
			if(cancelRequested) {
				cancelConfirmed();
				return;
			}
			completion = (float) (files.size() + i) / totalSz;
			i++;
			statusText = "Importing playlist "+i+" of "+itPls.size();
			fireUpdated();
			
			Playlist p = control.getMyPlaylistByTitle(pName);
			if (p == null) {
				p = new Playlist();
				p.setTitle(pName);
				p.getOwnerIds().add(control.getMyUser().getUserId());
				List<File> tracks = itPls.get(pName);
				for (File track : tracks) {
					SharedTrack sh = control.getShareByFilePath(track);
					if (sh == null)
						log.error("ITunes playlist '" + pName + "' has track '" + track
								+ "', but I am not sharing it");
					else
						p.getStreamIds().add(sh.getStream().getStreamId());
				}
				control.addOrUpdatePlaylist(p);
			} else {
				// Update existing playlist - add each track if it's not already in there
				List<File> tracks = itPls.get(pName);
				for (File track : tracks) {
					SharedTrack sh = control.getShareByFilePath(track);
					if (sh == null)
						log.error("ITunes playlist '" + pName + "' has track '" + track
								+ "', but I am not sharing it");
					else if (!p.getStreamIds().contains(sh.getStream().getStreamId()))
						p.getStreamIds().add(sh.getStream().getStreamId());
				}
				control.addOrUpdatePlaylist(p);
			}
		}
		completion = 1f;
		statusText = "Done.";
		fireUpdated();
	}
}
