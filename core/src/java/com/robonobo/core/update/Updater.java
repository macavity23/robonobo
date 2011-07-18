package com.robonobo.core.update;

import static com.robonobo.common.util.FileUtil.*;

import java.io.*;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.robonobo.common.exceptions.Errot;
import com.robonobo.common.serialization.ConfigBeanSerializer;
import com.robonobo.common.util.FileUtil;
import com.robonobo.core.service.DbService;
import com.robonobo.mina.external.MinaConfig;

/** Updates the rbnb home dir to the format expected by a new version
 * 
 * @author macavity */
@SuppressWarnings("unused")
public class Updater {
	static final int CURRENT_VERSION = 3;
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
					throw new Errot(e);
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
		log.info("Updating robohome dir " + homeDir.getAbsolutePath() + " from version 0 to version 1");
		// Just delete the config and db dirs - a bit lazy, but for version 0 it should be ok
		File configDir = new File(homeDir, "config");
		deleteDirectory(configDir);
		File dbDir = new File(homeDir, "db");
		deleteDirectory(dbDir);
		// log4j props file name changed
		File log4jFile = new File(homeDir, "robonobo-log4j.properties");
		log4jFile.delete();
	}

	private void updateVersion1ToVersion2() throws IOException {
		log.info("Updating robohome dir " + homeDir.getAbsolutePath() + " from version 1 to version 2");
		// Update config settings
		ConfigBeanSerializer cbs = new ConfigBeanSerializer();
		File configDir = new File(homeDir, "config");
		File minaCfgFile = new File(configDir, "mina.cfg");
		if (minaCfgFile.exists()) {
			MinaConfig oldCfg = cbs.deserializeConfig(MinaConfig.class, minaCfgFile);
			MinaConfig newCfg = new MinaConfig();
			oldCfg.setBidStrategyClass(newCfg.getBidStrategyClass());
			oldCfg.setSourceRequestBatchTime(newCfg.getSourceRequestBatchTime());
			cbs.serializeConfig(oldCfg, minaCfgFile);
		} else
			log.error("Not updating mina config file - it doesn't exist!");
		// Nuke dbs - not very optimal, but otherwise we get hsqldb errors as we upgraded hsqldb in this version...
		// better to do it now while we have a few users!
		File dbDir = new File(homeDir, "db");
		if (dbDir.exists()) {
			log.info("Nuking db directory...");
			FileUtil.deleteDirectory(dbDir);
		}
	}

	private void updateVersion2ToVersion3() throws IOException {
		log.info("Updating robohome dir " + homeDir.getAbsolutePath() + " from version 2 to version 3");
		String[] stArr = { DbService.CREATE_LIBRARY_KNOWN_TRACKS_TBL, DbService.CREATE_LIBRARY_UNKNOWN_TRACKS_TBL, DbService.CREATE_LIBRARY_INFO_TBL };
		try {
			updateMetadataDb(stArr);
		} catch (SQLException e) {
			throw new IOException("Caught SQLException: "+e.getMessage());
		}
	}
	
	private void compactPagesDb() throws SQLException {
		String sep = File.separator;
		String dbPrefix = homeDir.getAbsolutePath() + sep + "db" + sep + "metadata";
		try {
			Class.forName("org.hsqldb.jdbcDriver");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		String dbUrl = "jdbc:hsqldb:file:" + dbPrefix;
		File dbPropsFile = new File(dbPrefix + ".properties");
		if (dbPropsFile.exists()) {
			log.info("Compacting pages db");
			Connection conn = DriverManager.getConnection(dbUrl, "sa", "");
			Statement st = conn.createStatement();
			st.executeUpdate("SHUTDOWN COMPACT");
			st.close();
		} else
			log.info("pages db props does not exist - not updating pages db");
	}

	private void updateMetadataDb(String[] sqlStatements) throws SQLException {
		String sep = File.separator;
		String dbPrefix = homeDir.getAbsolutePath() + sep + "db" + sep + "metadata";
		try {
			Class.forName("org.hsqldb.jdbcDriver");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		String dbUrl = "jdbc:hsqldb:file:" + dbPrefix;
		File dbPropsFile = new File(dbPrefix + ".properties");
		if (dbPropsFile.exists()) {
			log.info("Updating metadata db with " + sqlStatements.length + " statements");
			Connection conn = DriverManager.getConnection(dbUrl, "sa", "");
			for (String sql : sqlStatements) {
				log.debug("Running: " + sql);
				try {
					Statement st = conn.createStatement();
					st.executeUpdate(sql);
					st.close();
				} catch (SQLException e) {
					throw new RuntimeException(e);
				}
			}
			Statement st = conn.createStatement();
			st.executeUpdate("SHUTDOWN COMPACT");
			st.close();
		} else
			log.info("metadata db props does not exist - not updating metadata db");
	}
}
