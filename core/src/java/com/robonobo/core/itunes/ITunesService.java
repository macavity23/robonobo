package com.robonobo.core.itunes;

import static com.robonobo.common.util.FileUtil.getFileExtension;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.robonobo.core.api.model.Playlist;
import com.robonobo.core.api.model.User;
import com.robonobo.core.service.AbstractService;

public abstract class ITunesService extends AbstractService {
	protected Log log = LogFactory.getLog(getClass());
	
	public ITunesService() {
		addHardDependency("core.shares");
	}

	public String getProvides() {
		return "core.itunes";
	}

	public void runITunesImport() {
	
	}
	
	/** Pass a null filter to get all files */
	public abstract List<File> getAllITunesFiles(FileFilter filter) throws IOException;
	
	/**
	 * Returns Map<PlaylistTitle, List<TrackLocation>> for all non-robonobo user-created iTunes playlists
	 */
	public abstract Map<String, List<File>> getAllITunesPlaylists(FileFilter filter) throws IOException;

	public abstract void syncPlaylist(User u, Playlist p) throws IOException;
}
