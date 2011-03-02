package com.robonobo.core.update;

import static com.robonobo.common.util.FileUtil.*;

import java.io.*;
import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.robonobo.common.exceptions.SeekInnerCalmException;

/**
 * Updates the rbnb home dir to the format expected by a new version
 * 
 * @author macavity
 * 
 */
public class Updater {
	static final int CURRENT_VERSION = 1;
	private File homeDir;
	Log log = LogFactory.getLog(getClass());

	public Updater(File homeDir) {
		this.homeDir = homeDir;
	}

	public void runUpdate() throws IOException {
		int v = getVersion();
		if (v < CURRENT_VERSION) {
			while (v < CURRENT_VERSION) {
				int nextV = v + 1;
				Method m;
				try {
					m = Updater.class.getDeclaredMethod("updateVersion" + v + "ToVersion" + nextV);
					m.invoke(this);
				} catch (Exception e) {
					throw new SeekInnerCalmException(e);
				}
				v = nextV;
			}
			saveVersion();
		}
	}

	private int getVersion() throws IOException {
		File versionFile = new File(homeDir, "version");
		if (!versionFile.exists())
			return 0;
		FileInputStream fis = new FileInputStream(versionFile);
		byte[] buf = new byte[16];
		int numRead = fis.read(buf);
		String versionStr = new String(buf, 0, numRead);
		fis.close();
		return Integer.parseInt(versionStr);
	}

	private void saveVersion() throws IOException {
		File versionFile = new File(homeDir, "version");
		FileOutputStream fos = new FileOutputStream(versionFile);
		String versionStr = String.valueOf(CURRENT_VERSION);
		fos.write(versionStr.getBytes());
		fos.close();
	}

	private void updateVersion0ToVersion1() {
		log.info("Updating robohome dir "+homeDir.getAbsolutePath()+" from version 0 to version 1");
		// Just delete the config and db dirs - a bit lazy, but for version 0 it should be ok
		File configDir = new File(homeDir, "config");
		deleteDirectory(configDir);
		File dbDir = new File(homeDir, "db");
		deleteDirectory(dbDir);
		// log4j props file name changed
		File log4jFile = new File(homeDir, "robonobo-log4j.properties");
		log4jFile.delete();
	}
}
