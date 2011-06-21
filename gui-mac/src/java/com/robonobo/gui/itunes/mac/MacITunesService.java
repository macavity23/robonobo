package com.robonobo.gui.itunes.mac;

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.robonobo.core.api.model.Playlist;
import com.robonobo.core.api.model.SharedTrack;
import com.robonobo.core.api.model.User;
import com.robonobo.core.itunes.ITunesService;

public class MacITunesService extends ITunesService {
	static final String OSASCRIPT = "osascript";
	private String[] scriptNames = { "listTracksInLibrary.scpt", "syncPlaylist.scpt", "createRoboFolder.scpt", "listAllPlaylists.scpt" };
	private File scriptsDir;
	private boolean iTunesReady = false;

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

	private void createRobonoboFolder() throws IOException {
		runScript("createRoboFolder.scpt", null);
	}

	private void copyScriptsIntoDir(File itScriptsDir) throws IOException {
		for (String scriptName : scriptNames) {
			copyScriptFromJar(scriptName, itScriptsDir);
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
		procArgs.add(OSASCRIPT);
		procArgs.add(script);
		if (scriptArgs != null)
			procArgs.addAll(scriptArgs);
		ProcessBuilder pb = new ProcessBuilder(procArgs);
		pb.directory(scriptsDir);
		Process proc = pb.start();
		String line;
		BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		// Haven't been able to output to stdout from applescript - some pages
		// suggested writing to the file /dev/fd/0, but applescript claims that
		// file doesn't exist... instead we return a list of items and newlines.
		// So we need to strip off the first line, which is blank, and strip off
		// the commas from the end of subsequent lines (except the last one)
		boolean first = true;
		while ((line = reader.readLine()) != null) {
			if (first)
				first = false;
			else {
				if (line.endsWith(","))
					line = line.substring(0, line.length() - 1);
				result.add(line);
			}
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
		return clean(result);
	}

	private void copyScriptFromJar(String scriptName, File targetDir) throws IOException {
		String fqName = "com/robonobo/gui/itunes/mac/" + scriptName;
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

	private List<String> clean(List<String> scriptOut) {
		// Because I hate applescript and want to write as little of it as
		// possible, the output lines that we get back have a comma at the
		// end, which we remove
		List<String> result = new ArrayList<String>();
		for(String line : scriptOut) {
			String tLine = line.trim();
			if(tLine.endsWith(","))
				tLine = tLine.substring(0, tLine.length()-1);
			result.add(tLine);
		}
		return result;
	}
	
	@Override
	public List<File> getAllITunesFiles(FileFilter filter) throws IOException {
		checkReady();
		List<File> result = new ArrayList<File>();
		List<String> trackPaths = runScript("listTracksInLibrary.scpt", null);
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
		List<String> scriptOutput = runScript("listAllPlaylists.scpt", null);
		// This returns the playlist title on its own line, then the fq path of each track in the playlist on its own line, then a blank line... and repeat
		String playlistName = null;
		List<File> tracks = null;
		for(String line : scriptOutput) {
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
	public synchronized void syncPlaylist(User u, Playlist p) throws IOException {
		checkReady();
		createRobonoboFolder();
		log.debug("Syncing with iTunes: user " + u.getEmail() + ", playlist '" + p.getTitle() + "'");

		// Create a temp file with a list of the shared tracks in this playlist
		File trackListFile = File.createTempFile("robotracklist", "tmp");
		PrintWriter writer = new PrintWriter(trackListFile);
		for (String streamId : p.getStreamIds()) {
			SharedTrack sh = getRobonobo().getDbService().getShare(streamId);
			if (sh != null) {
				writer.println(sh.getFile().getAbsolutePath());
			}
		}
		writer.close();

		// This is much easier than the windows version, it's all done in
		// applescript
		List<String> scriptArgs = new ArrayList<String>();
		scriptArgs.add(getFolderName(u));
		scriptArgs.add(p.getTitle());
		scriptArgs.add(trackListFile.getAbsolutePath());
		runScript("syncPlaylist.scpt", scriptArgs);

		trackListFile.delete();
	}

	private String getFolderName(User u) {
		return u.getFriendlyName();
	}

	public String getName() {
		return "Macintosh iTunes Service";
	}

}
