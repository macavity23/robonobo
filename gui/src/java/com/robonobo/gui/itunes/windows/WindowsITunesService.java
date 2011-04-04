package com.robonobo.gui.itunes.windows;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.core.api.model.Playlist;
import com.robonobo.core.api.model.SharedTrack;
import com.robonobo.core.api.model.User;
import com.robonobo.core.itunes.ITunesService;

public class WindowsITunesService extends ITunesService {
	static final String CSCRIPT = "cscript.exe";
	// All scripts have both a .js and a .wsf file, plus the commonFuncs.js file
	private String[] scriptNames = { "addTrackToUserPlaylist", "createRoboFolder", "createUserFolder",
			"createUserPlaylist", "deleteUserFolder", "deleteUserPlaylist", "listTracksInUserPlaylist",
			"listUserFolders", "listUserPlaylists", "syncUserPlaylist", "listTracksInLibrary", "listAllPlaylists" };
	private File scriptsDir;
	private boolean iTunesReady = false;

	public WindowsITunesService() {
	}

	public String getName() {
		return "Windows iTunes Service";
	}

	@Override
	public void startup() throws Exception {
		// Don't init iTunes here, do it on-demand (improves startup time)
	}

	private synchronized void checkReady() throws IOException {
		if (!iTunesReady) {
			createScriptsDir();
			copyScriptsIntoDir(scriptsDir);
			// TODO iTunes will pop open if it's not already done, so make sure
			// we get the focus back afterwards
			iTunesReady = true;
		}
	}

	/**
	 * Makes the 'Robonobo' folder within iTunes if it doesn't exist
	 */
	private void createRobonoboFolder() throws IOException {
		runScript("createRoboFolder.wsf", null);
	}

	private void copyScriptsIntoDir(File itScriptsDir) throws IOException {
		copyScriptFromJar("commonFuncs.js", itScriptsDir);
		for (String scriptName : scriptNames) {
			copyScriptFromJar(scriptName + ".js", itScriptsDir);
			copyScriptFromJar(scriptName + ".wsf", itScriptsDir);
		}
	}

	private void createScriptsDir() {
		scriptsDir = new File(new File(getRobonobo().getHomeDir(), "scripts"), "iTunes");
		if (!scriptsDir.exists())
			scriptsDir.mkdirs();
	}

	private List<String> runScript(String script, List<String> scriptArgs) throws IOException {
		List<String> result = new ArrayList<String>();
		List<String> procArgs = new ArrayList<String>();
		procArgs.add(CSCRIPT);
		procArgs.add(script);
		if (scriptArgs != null)
			procArgs.addAll(scriptArgs);
		ProcessBuilder pb = new ProcessBuilder(procArgs);
		pb.directory(scriptsDir);
		Process proc = pb.start();
		String line;
		BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		while ((line = reader.readLine()) != null) {
			result.add(line);
		}
		// Thread t = new Thread(new LogReader(proc.getInputStream()));
		// t.start();
		// Wait for it to finish, then check the return status is ok
		int retCode;
		try {
			retCode = proc.waitFor();
		} catch (InterruptedException e) {
			log.error("Process was interrupted: " + script);
			return result;
		}
		if (retCode != 0) {
			BufferedReader errReader = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
			String errLine;
			List<String> errLines = new ArrayList<String>();
			while ((errLine = errReader.readLine()) != null) {
				errLines.add(errLine);
			}
			log.error("Error executing script '" + script + "': return code was " + retCode);
			return result;
		}
		return result;
	}

	private void copyScriptFromJar(String scriptName, File targetDir) throws IOException {
		String fqName = "com/robonobo/gui/itunes/windows/" + scriptName;
		InputStream is = getClass().getClassLoader().getResourceAsStream(fqName);
		OutputStream os = new FileOutputStream(new File(targetDir, scriptName));
		byte[] buf = new byte[1024];
		int bytesRead;
		while ((bytesRead = is.read(buf)) >= 0) {
			os.write(buf, 0, bytesRead);
		}
		is.close();
		os.close();
	}

	@Override
	public void shutdown() throws Exception {
		// Do nothing
	}

	@Override
	public List<File> getAllITunesFiles(FileFilter filter) throws IOException {
		checkReady();
		List<File> result = new ArrayList<File>();
		List<String> trackPaths = runScript("listTracksInLibrary.wsf", null);
		for (String trackPath : trackPaths) {
			File f = new File(trackPath);
			if (filter == null || filter.accept(f))
				result.add(f);
		}
		return result;
	}

	@Override
	public Map<String, List<File>> getAllITunesPlaylists(FileFilter filter) throws IOException {
		checkReady();
		Map<String, List<File>> result = new HashMap<String, List<File>>();
		List<String> scOutput = runScript("listAllPlaylists.wsf", null);
		// This returns the playlist title on its own line, then the fq path of each track in the playlist on its own line, then a blank line... and repeat
		String playlistName = null;
		List<File> tracks = null;
		for (String line : scOutput) {
			if(playlistName == null) {
				playlistName = line;
				tracks = new ArrayList<File>();
				continue;
			}
			if(line.trim().length() == 0) {
				if(tracks.size() > 0)
					result.put(playlistName, tracks);
				playlistName = null;
				continue;
			}
			File track = new File(line);
			if(track.exists() && ((filter == null) || filter.accept(track)))
				tracks.add(track);
		}
		return result;
	}
	
	@Override
	public void syncPlaylist(User u, Playlist p) throws IOException {
		checkReady();
		createRobonoboFolder();
		log.debug("Syncing with iTunes: user " + u.getEmail() + ", playlist '" + p.getTitle() + "'");
		// First, check to see if we have a folder for the user
		String folderName = getFolderName(u);
		List<String> userFolders = runScript("listUserFolders.wsf", null);
		boolean gotFolder = false;
		for (String folder : userFolders) {
			if (folder.equals(folderName)) {
				gotFolder = true;
				break;
			}
		}
		// If not, create it
		if (!gotFolder) {
			log.info("Adding to iTunes: user " + u.getEmail() + ", playlist '" + p.getTitle() + "'");
			List<String> args = new ArrayList<String>();
			args.add(folderName);
			runScript("createUserFolder.wsf", args);
		}
		// Now check to see if the playlist exists
		List<String> args = new ArrayList<String>();
		args.add(folderName);
		List<String> playlists = runScript("listUserPlaylists.wsf", args);
		boolean gotPlaylist = false;
		for (String playlistTitle : playlists) {
			if (playlistTitle.equals(p.getTitle())) {
				gotPlaylist = true;
				break;
			}
		}
		// Create the playlist if necessary
		if (!gotPlaylist) {
			List<String> createPlArgs = new ArrayList<String>();
			createPlArgs.add(folderName);
			createPlArgs.add(p.getTitle());
			runScript("createUserPlaylist.wsf", createPlArgs);
		}
		// Create a temp file with a list of the shared tracks in this playlist
		File trackListFile = File.createTempFile("robotracklist", "tmp");
		trackListFile.deleteOnExit();
		PrintWriter writer = new PrintWriter(trackListFile);
		for (String streamId : p.getStreamIds()) {
			SharedTrack sh = getRobonobo().getDbService().getShare(streamId);
			if (sh != null) {
				writer.println(sh.getFile().getAbsolutePath());
			}
		}
		writer.close();
		// Let's sync this sucker
		List<String> syncArgs = new ArrayList<String>();
		syncArgs.add(folderName);
		syncArgs.add(p.getTitle());
		syncArgs.add(trackListFile.getAbsolutePath());
		runScript("syncUserPlaylist.wsf", syncArgs);
	}

	private String getFolderName(User u) {
		return u.getFriendlyName();
	}

	class LogReader extends CatchingRunnable {
		InputStream is;

		public LogReader(InputStream is) {
			this.is = is;
		}

		@Override
		public void doRun() throws Exception {
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String line;
			while ((line = br.readLine()) != null) {
				log.debug("DEBUG: script output: " + line);
			}
		}

	}
}
