package com.robonobo.core.service;

import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.Map.Entry;

import com.robonobo.core.api.model.*;
import com.robonobo.core.api.model.DownloadingTrack.DownloadStatus;
import com.robonobo.core.api.model.SharedTrack.ShareStatus;

/**
 * Encapsulates local db access. Now uses hsqldb rather than derby - much faster!
 */
public class DbService extends AbstractService {
	private final List<Connection> freeConns = new ArrayList<Connection>();
	private int numConnsCreated = 0;

	public static final String CREATE_STREAMS_TBL = "CREATE CACHED TABLE STREAMS (STREAM_ID VARCHAR(36) NOT NULL PRIMARY KEY, TITLE VARCHAR(256) NOT NULL, DESCRIPTION VARCHAR(512), FMT VARCHAR(128) NOT NULL, SIZE BIGINT NOT NULL, DURATION BIGINT NOT NULL)";
	public static final String CREATE_ATTRIBUTES_TBL = "CREATE CACHED TABLE STREAM_ATTRIBUTES (SM_ID INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, STREAM_ID VARCHAR(36) NOT NULL, M_KEY VARCHAR(256) NOT NULL, M_VAL VARCHAR(256) NOT NULL, CONSTRAINT SM_ATT_FK FOREIGN KEY (STREAM_ID) REFERENCES STREAMS (STREAM_ID) ON DELETE CASCADE)";
	public static final String CREATE_SHARES_TBL = "CREATE CACHED TABLE SHARES (SHARE_ID INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, STREAM_ID VARCHAR(36) NOT NULL, FILE_PATH VARCHAR(256) NOT NULL, STATUS VARCHAR(32) NOT NULL, DATE_STARTED BIGINT NOT NULL, CONSTRAINT SHARE_FK FOREIGN KEY (STREAM_ID) REFERENCES STREAMS (STREAM_ID) ON DELETE CASCADE)";
	public static final String CREATE_DOWNLOADS_TBL = "CREATE CACHED TABLE DOWNLOADS (DOWNLOAD_ID INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, STREAM_ID VARCHAR(36) NOT NULL, FILE_PATH VARCHAR(256) NOT NULL, STATUS VARCHAR(32) NOT NULL, DATE_STARTED BIGINT NOT NULL, CONSTRAINT DOWNLOAD_FK FOREIGN KEY (STREAM_ID) REFERENCES STREAMS (STREAM_ID) ON DELETE CASCADE)";
	public static final String CREATE_WATCHDIRS_TBL = "CREATE CACHED TABLE WATCHDIRS (WATCHDIR_ID INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, DIRPATH VARCHAR(1024) NOT NULL)";
	public static final String CREATE_CHECKED_FILES_TBL = "CREATE CACHED TABLE CHECKED_FILES (CHECKEDFILE_ID INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, FILEPATH VARCHAR(1024) NOT NULL)";
	public static final String CREATE_PLAYLIST_CFG_TBL = "CREATE CACHED TABLE PLAYLIST_CFG (PLAYLIST_ID BIGINT NOT NULL, ITEM_NAME VARCHAR(256) NOT NULL, ITEM_VAL VARCHAR(256) NOT NULL)";
	public static final String CREATE_PLAYLIST_SEEN_SIDS_TBL = "CREATE CACHED TABLE playlist_seen_sids (playlist_id BIGINT NOT NULL, stream_id VARCHAR(36) NOT NULL)";
	public static final String CREATE_LIBRARY_TRACKS_TBL = "CREATE CACHED TABLE library_tracks(user_id BIGINT NOT NULL, stream_id VARCHAR(36) NOT NULL, added_date BIGINT NOT NULL)";
	public static final String CREATE_LIBRARY_SEEN_SIDS_TBL = "CREATE CACHED TABLE library_seen_sids (user_id BIGINT NOT NULL, stream_id VARCHAR(36) NOT NULL)";
	public static final String CREATE_LIBRARY_LAST_CHECKED_TBL = "CREATE CACHED TABLE library_last_checked (user_id BIGINT NOT NULL, check_date BIGINT NOT NULL)";

	private static final String READ_STREAM = "SELECT * FROM STREAMS WHERE STREAM_ID = ?";
	private static final String READ_STREAM_ATTRIBUTES = "SELECT * FROM STREAM_ATTRIBUTES WHERE STREAM_ID = ?";
	private static final String READ_SHARE = "SELECT * FROM shares WHERE stream_id = ?";
	private static final String READ_SHARE_BY_PATH = "SELECT * FROM SHARES WHERE FILE_PATH = ?";
	private static final String READ_DOWNLOAD = "SELECT * FROM downloads WHERE stream_id = ?";
	private static final String READ_NUM_SHARES_AND_DOWNLOADS = "SELECT COUNT(*) FROM (SELECT stream_id FROM shares UNION SELECT stream_id FROM downloads)";
	private static final String READ_CHECKED_FILE = "SELECT * FROM CHECKED_FILES WHERE FILEPATH = ?";
	private static final String READ_PLAYLIST_CFG = "SELECT * FROM PLAYLIST_CFG WHERE PLAYLIST_ID = ?";
	private static final String READ_PLAYLIST_SEEN_SIDS = "SELECT stream_id FROM playlist_seen_sids WHERE playlist_id = ?";
	private static final String READ_LIBRARY_TRACKS = "SELECT stream_id, added_date FROM library_tracks WHERE user_id = ?";
	private static final String READ_LIBRARY_LAST_CHECKED = "SELECT check_date FROM library_last_checked WHERE user_id = ?";
	private static final String READ_LIBRARY_UNKNOWN_STREAMS = "SELECT lt.stream_id, lt.added_date FROM library_tracks as lt WHERE lt.user_id = ? AND NOT EXISTS (SELECT * FROM streams AS s WHERE s.stream_id = lt.stream_id)";
	// We do libraries differently from playlists as libs might contain tracks that we don't yet have the streams for
	// (this is never true of playlists) and we don't include those
	private static final String READ_LIBRARY_NUM_UNSEEN_SIDS = "SELECT COUNT(*) FROM ((SELECT lt.stream_id FROM library_tracks as lt WHERE lt.user_id = ? INTERSECT SELECT s.stream_id FROM streams AS s) MINUS SELECT lss.stream_id FROM library_seen_sids AS lss WHERE lss.user_id = ?)";

	private static final String MATCH_SHARES = "SELECT DISTINCT sh.* FROM SHARES AS sh, STREAMS AS st WHERE sh.STREAM_ID = st.STREAM_ID AND (lower(st.TITLE) LIKE lower(?) OR lower(st.DESCRIPTION) LIKE lower(?))"
			+ " UNION SELECT DISTINCT sh.* FROM SHARES AS sh, STREAM_ATTRIBUTES AS sa WHERE sh.STREAM_ID = sa.STREAM_ID AND lower(sa.M_VAL) LIKE lower(?)";

	private static final String CREATE_STREAM = "INSERT INTO STREAMS (STREAM_ID, TITLE, DESCRIPTION, FMT, SIZE, DURATION) VALUES (?, ?, ?, ?, ?, ?)";
	private static final String CREATE_STREAM_ATTRIBUTES = "INSERT INTO STREAM_ATTRIBUTES (STREAM_ID, M_KEY, M_VAL) VALUES (?, ?, ?)";
	private static final String CREATE_SHARE = "INSERT INTO SHARES (STREAM_ID, FILE_PATH, STATUS, DATE_STARTED) VALUES (?, ?, ?, ?)";
	private static final String CREATE_DOWNLOAD = "INSERT INTO DOWNLOADS (STREAM_ID, FILE_PATH, STATUS, DATE_STARTED) VALUES (?, ?, ?, ?)";
	private static final String CREATE_WATCHDIR = "INSERT INTO WATCHDIRS (DIRPATH) VALUES (?)";
	private static final String CREATE_CHECKED_FILE = "INSERT INTO CHECKED_FILES (FILEPATH) VALUES (?)";
	private static final String CREATE_PLAYLIST_CFG = "INSERT INTO PLAYLIST_CFG (PLAYLIST_ID, ITEM_NAME, ITEM_VAL) VALUES (?, ?, ?)";
	private static final String CREATE_PLAYLIST_SEEN_SID = "INSERT INTO playlist_seen_sids (playlist_id, stream_id) VALUES(?, ?)";
	private static final String CREATE_LIBRARY_TRACK = "INSERT INTO library_tracks (user_id, stream_id, added_date) VALUES (?, ?, ?)";
	private static final String CREATE_LIBRARY_LAST_CHECKED = "MERGE INTO library_last_checked AS llc USING (VALUES(CAST(? AS BIGINT))) AS vals(x) ON llc.user_id = vals.x WHEN MATCHED THEN UPDATE SET llc.check_date = CAST(? AS BIGINT) WHEN NOT MATCHED THEN INSERT VALUES(vals.x, CAST(? AS BIGINT))";

	private static final String UPDATE_SHARE = "UPDATE SHARES SET FILE_PATH = ?, STATUS = ?, DATE_STARTED = ? WHERE STREAM_ID = ?";
	private static final String UPDATE_DOWNLOAD = "UPDATE DOWNLOADS SET FILE_PATH = ?, STATUS = ?, DATE_STARTED = ? WHERE STREAM_ID = ?";
	private static final String UPDATE_LIBRARY_SEEN_SIDS = "INSERT INTO library_seen_sids ((SELECT lt.user_id, lt.stream_id FROM library_tracks as lt WHERE lt.user_id = ? INTERSECT SELECT ?, s.stream_id FROM streams AS s) MINUS SELECT lss.user_id, lss.stream_id FROM library_seen_sids AS lss WHERE lss.user_id = ?)";

	private static final String DELETE_STREAM = "DELETE FROM STREAMS WHERE STREAM_ID = ?";
	private static final String DELETE_STREAM_ATTRIBUTES = "DELETE FROM STREAM_ATTRIBUTES WHERE STREAM_ID = ?";
	private static final String DELETE_SHARE = "DELETE FROM SHARES WHERE STREAM_ID = ?";
	private static final String DELETE_DOWNLOAD = "DELETE FROM DOWNLOADS WHERE STREAM_ID = ?";
	private static final String DELETE_WATCHDIR = "DELETE FROM WATCHDIRS WHERE DIRPATH = ?";
	private static final String DELETE_PLAYLIST_CFG = "DELETE FROM PLAYLIST_CFG WHERE PLAYLIST_ID = ?";
	private static final String DELETE_PLAYLIST_SEEN_SIDS = "DELETE FROM playlist_seen_sids WHERE playlist_id = ?";

	private static final String GET_ALL_SHARE_STREAM_IDS = "SELECT STREAM_ID FROM SHARES";
	private static final String GET_ALL_DOWNLOAD_STREAM_IDS = "SELECT STREAM_ID FROM DOWNLOADS ORDER BY DATE_STARTED ASC";
	private static final String GET_NUM_RUNNING_DOWNLOADS = "SELECT COUNT(*) FROM DOWNLOADS WHERE STATUS = 'Downloading'";
	private static final String GET_ALL_WATCHDIRS = "SELECT * FROM WATCHDIRS";

	private String dbUrl;
	private boolean running = false;

	public DbService() {
		addHardDependency("core.event");
	}

	public String getName() {
		return "Local database service";
	}

	public String getProvides() {
		return "core.db";
	}

	@Override
	public void startup() throws Exception {
		String s = File.separator;
		String dbPrefix = getRobonobo().getHomeDir().getAbsolutePath() + s + "db" + s + "metadata";
		try {
			Class.forName("org.hsqldb.jdbcDriver");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		dbUrl = "jdbc:hsqldb:file:" + dbPrefix;
		File dbPropsFile = new File(dbPrefix + ".properties");
		if (!dbPropsFile.exists()) {
			log.info("Creating metadata db with prefix " + dbPrefix);
			String[] creatStats = { CREATE_STREAMS_TBL, CREATE_ATTRIBUTES_TBL, CREATE_SHARES_TBL, CREATE_DOWNLOADS_TBL,
					CREATE_WATCHDIRS_TBL, CREATE_CHECKED_FILES_TBL, CREATE_PLAYLIST_CFG_TBL,
					CREATE_PLAYLIST_SEEN_SIDS_TBL, CREATE_LIBRARY_TRACKS_TBL, CREATE_LIBRARY_SEEN_SIDS_TBL,
					CREATE_LIBRARY_LAST_CHECKED_TBL };
			Connection conn = getConnection();
			for (String stat : creatStats) {
				try {
					Statement st = conn.createStatement();
					st.executeUpdate(stat);
					st.close();
				} catch (SQLException e) {
					throw new RuntimeException(e);
				}
			}
			returnConnection(conn);
		} else
			log.info("Using metadata db with prefix " + dbPrefix);
		running = true;
	}

	@Override
	public void shutdown() throws Exception {
		running = false;
		Connection conn = getConnection();
		Statement s = conn.createStatement();
		s.executeUpdate("SHUTDOWN COMPACT");
		s.close();
		returnConnection(conn);
	}

	/**
	 * This is public for debugging only - do not use this outside this class except for debugging
	 */
	public synchronized Connection getConnection() throws SQLException {
		if (freeConns.size() > 0)
			return freeConns.remove(0);
		else {
			log.debug("MetadataDB: now created " + ++numConnsCreated + " db connections");
			return DriverManager.getConnection(dbUrl, "sa", "");
		}
	}

	/**
	 * This is public for debugging only - do not use this outside this class except for debugging
	 */
	public synchronized void returnConnection(Connection conn) {
		freeConns.add(conn);
	}

	public Stream getStream(String streamId) {
		if (!running)
			return null;
		Connection conn = null;
		try {
			conn = getConnection();
			PreparedStatement ps = conn.prepareStatement(READ_STREAM);
			ps.setString(1, streamId);
			ResultSet rs = ps.executeQuery();
			if (!rs.next())
				return null;
			Stream s = new Stream();
			s.setStreamId(streamId);
			s.setTitle(rs.getString("TITLE"));
			s.setDescription(rs.getString("DESCRIPTION"));
			s.setMimeType(rs.getString("FMT"));
			s.setSize(rs.getLong("SIZE"));
			s.setDuration(rs.getLong("DURATION"));

			ps = conn.prepareStatement(READ_STREAM_ATTRIBUTES);
			ps.setString(1, streamId);
			rs = ps.executeQuery();
			while (rs.next()) {
				s.setAttrValue(rs.getString("M_KEY"), rs.getString("M_VAL"));
			}
			return s;
		} catch (SQLException e) {
			log.error("Error retrieving stream from db", e);
			return null;
		} finally {
			if (conn != null)
				returnConnection(conn);
		}
	}

	public List<File> getWatchDirs() {
		if (!running)
			return null;
		Connection conn = null;
		try {
			conn = getConnection();
			PreparedStatement ps = conn.prepareStatement(GET_ALL_WATCHDIRS);
			ResultSet rs = ps.executeQuery();
			ArrayList<File> result = new ArrayList<File>();
			while (rs.next()) {
				File f = new File(rs.getString("DIRPATH"));
				result.add(f);
			}
			return result;
		} catch (SQLException e) {
			log.error("Error retrieving watchdirs from db", e);
			return null;
		} finally {
			if (conn != null)
				returnConnection(conn);
		}
	}

	public Set<String> getShares() {
		if (!running)
			return null;
		Connection conn = null;
		try {
			conn = getConnection();
			PreparedStatement ps = conn.prepareStatement(GET_ALL_SHARE_STREAM_IDS);
			ResultSet rs = ps.executeQuery();
			Set<String> result = new HashSet<String>();
			while (rs.next()) {
				result.add(rs.getString(1));
			}
			return result;
		} catch (SQLException e) {
			log.error("Error retrieving shares from db", e);
			return null;
		} finally {
			if (conn != null)
				returnConnection(conn);
		}
	}

	public Collection<SharedTrack> getSharesByPattern(String searchPattern) {
		if (!running)
			return null;
		Connection conn = null;
		String sqlPattern = "%" + searchPattern + "%";
		try {
			conn = getConnection();
			PreparedStatement ps = conn.prepareStatement(MATCH_SHARES);
			ps.setString(1, sqlPattern);
			ps.setString(2, sqlPattern);
			ps.setString(3, sqlPattern);
			ResultSet rs = ps.executeQuery();
			List<SharedTrack> shares = new ArrayList<SharedTrack>();
			while (rs.next()) {
				shares.add(shareFromRs(rs));
			}
			return shares;
		} catch (SQLException e) {
			log.error("Error retrieving shares from db", e);
			return null;
		} finally {
			if (conn != null)
				returnConnection(conn);
		}
	}

	public SharedTrack getShare(String streamId) {
		if (!running)
			return null;
		Connection conn = null;
		try {
			conn = getConnection();
			PreparedStatement ps = conn.prepareStatement(READ_SHARE);
			ps.setString(1, streamId);
			ResultSet rs = ps.executeQuery();
			if (!rs.next())
				return null;
			SharedTrack share = shareFromRs(rs);
			return share;
		} catch (SQLException e) {
			log.error("Error retrieving share from db", e);
			return null;
		} finally {
			if (conn != null)
				returnConnection(conn);
		}
	}

	public SharedTrack getShareByFilePath(String filePath) {
		if (!running)
			return null;
		Connection conn = null;
		try {
			conn = getConnection();
			PreparedStatement ps = conn.prepareStatement(READ_SHARE_BY_PATH);
			ps.setString(1, filePath);
			ResultSet rs = ps.executeQuery();
			if (!rs.next())
				return null;
			SharedTrack share = shareFromRs(rs);
			return share;
		} catch (SQLException e) {
			log.error("Error retrieving share from db", e);
			return null;
		} finally {
			if (conn != null)
				returnConnection(conn);
		}
	}

	private SharedTrack shareFromRs(ResultSet rs) throws SQLException {
		Stream s = getStream(rs.getString("STREAM_ID"));
		File f = new File(rs.getString("FILE_PATH"));
		ShareStatus stat = ShareStatus.valueOf(rs.getString("STATUS"));
		SharedTrack share = new SharedTrack(s, f, stat);
		share.setDateAdded(new Date(rs.getLong("DATE_STARTED")));
		return share;
	}

	public void putShare(SharedTrack share) {
		if (!running)
			return;
		Connection conn = null;
		try {
			// TODO replace this with a merge now that we are using hsql2 - others in this class too
			PreparedStatement ps;
			if (getShare(share.getStream().getStreamId()) != null) {
				conn = getConnection();
				ps = conn.prepareStatement(UPDATE_SHARE);
				ps.setString(1, share.getFile().getAbsolutePath());
				ps.setString(2, share.getShareStatus().toString());
				ps.setString(3, share.getStream().getStreamId());
				ps.setLong(4, share.getDateAdded().getTime());
			} else {
				conn = getConnection();
				ps = conn.prepareStatement(CREATE_SHARE);
				ps.setString(1, share.getStream().getStreamId());
				ps.setString(2, share.getFile().getAbsolutePath());
				ps.setString(3, share.getShareStatus().toString());
				ps.setLong(4, share.getDateAdded().getTime());
			}
			ps.executeUpdate();
		} catch (SQLException e) {
			log.error("Error storing share in db", e);
		} finally {
			if (conn != null)
				returnConnection(conn);
		}
	}

	public void deleteShare(String streamId) {
		if (!running)
			return;
		Connection conn = null;
		try {
			conn = getConnection();
			PreparedStatement ps = conn.prepareStatement(DELETE_SHARE);
			ps.setString(1, streamId);
			ps.executeUpdate();
		} catch (SQLException e) {
			log.error("Error deleting share from db", e);
		} finally {
			if (conn != null)
				returnConnection(conn);
		}

	}

	/**
	 * @return All downloads, sorted first-started-first
	 */
	public List<String> getDownloads() {
		if (!running)
			return null;
		Connection conn = null;
		try {
			conn = getConnection();
			PreparedStatement ps = conn.prepareStatement(GET_ALL_DOWNLOAD_STREAM_IDS);
			ResultSet rs = ps.executeQuery();
			List<String> result = new ArrayList<String>();
			while (rs.next()) {
				result.add(rs.getString(1));
			}
			return result;
		} catch (SQLException e) {
			log.error("Error retrieving downloads from db", e);
			return null;
		} finally {
			if (conn != null)
				returnConnection(conn);
		}
	}

	public DownloadingTrack getDownload(String streamId) {
		if (!running)
			return null;
		Connection conn = null;
		try {
			conn = getConnection();
			PreparedStatement ps = conn.prepareStatement(READ_DOWNLOAD);
			ps.setString(1, streamId);
			ResultSet rs = ps.executeQuery();
			if (!rs.next())
				return null;
			DownloadingTrack d = downloadFromRs(rs);
			return d;
		} catch (SQLException e) {
			log.error("Error retrieving download from db", e);
			return null;
		} finally {
			if (conn != null)
				returnConnection(conn);
		}
	}

	public int numRunningDownloads() {
		if (!running)
			return 0;
		Connection conn = null;
		try {
			conn = getConnection();
			PreparedStatement ps = conn.prepareStatement(GET_NUM_RUNNING_DOWNLOADS);
			ResultSet rs = ps.executeQuery();
			if (!rs.next())
				return 0;
			return rs.getInt(1);
		} catch (SQLException e) {
			log.error("Error retrieving download from db", e);
			return 0;
		} finally {
			if (conn != null)
				returnConnection(conn);
		}
	}

	private DownloadingTrack downloadFromRs(ResultSet rs) throws SQLException {
		Stream s = getStream(rs.getString("STREAM_ID"));
		File f = new File(rs.getString("FILE_PATH"));
		DownloadStatus stat = DownloadStatus.valueOf(rs.getString("STATUS"));
		DownloadingTrack d = new DownloadingTrack(s, f, stat);
		d.setDateAdded(new Date(rs.getLong("DATE_STARTED")));
		return d;
	}

	public void putDownload(DownloadingTrack d) {
		if (!running)
			return;
		Connection conn = null;
		try {
			PreparedStatement ps;
			if (getDownload(d.getStream().getStreamId()) != null) {
				conn = getConnection();
				ps = conn.prepareStatement(UPDATE_DOWNLOAD);
				ps.setString(1, d.getFile().getAbsolutePath());
				ps.setString(2, d.getDownloadStatus().toString());
				ps.setLong(3, d.getDateAdded().getTime());
				ps.setString(4, d.getStream().getStreamId());
			} else {
				conn = getConnection();
				ps = conn.prepareStatement(CREATE_DOWNLOAD);
				ps.setString(1, d.getStream().getStreamId());
				ps.setString(2, d.getFile().getAbsolutePath());
				ps.setString(3, d.getDownloadStatus().toString());
				ps.setLong(4, d.getDateAdded().getTime());
			}
			ps.executeUpdate();
		} catch (SQLException e) {
			log.error("Error putting download", e);
		} finally {
			if (conn != null)
				returnConnection(conn);
		}

	}

	public void deleteDownload(String streamId) {
		if (!running)
			return;
		Connection conn = null;
		try {
			conn = getConnection();
			PreparedStatement ps = conn.prepareStatement(DELETE_DOWNLOAD);
			ps.setString(1, streamId);
			ps.executeUpdate();
		} catch (SQLException e) {
			log.error("Error deleting download", e);
		} finally {
			if (conn != null)
				returnConnection(conn);
		}

	}

	public int numSharesAndDownloads() {
		if (!running)
			return 0;
		Connection conn = null;
		try {
			conn = getConnection();
			PreparedStatement ps = conn.prepareStatement(READ_NUM_SHARES_AND_DOWNLOADS);
			ResultSet rs = ps.executeQuery();
			if (!rs.next())
				return 0;
			return rs.getInt(1);
		} catch (SQLException e) {
			log.error("Error retrieving num shares & downloads from db", e);
			return 0;
		} finally {
			if (conn != null)
				returnConnection(conn);
		}
	}

	public void putStream(Stream s) {
		if (!running)
			return;
		if (getStream(s.getStreamId()) != null)
			return;
		Connection conn = null;
		try {
			conn = getConnection();
			PreparedStatement ps;
			ps = conn.prepareStatement(CREATE_STREAM);
			ps.setString(1, s.getStreamId());
			ps.setString(2, s.getTitle());
			ps.setString(3, s.getDescription());
			ps.setString(4, s.getMimeType());
			ps.setLong(5, s.getSize());
			ps.setLong(6, s.getDuration());
			ps.executeUpdate();
			for (StreamAttribute attr : s.getAttributes()) {
				ps = conn.prepareStatement(CREATE_STREAM_ATTRIBUTES);
				ps.setString(1, s.getStreamId());
				ps.setString(2, attr.getName());
				ps.setString(3, attr.getValue());
				ps.executeUpdate();
			}
		} catch (SQLException e) {
			log.error("Error storing stream", e);
		} finally {
			if (conn != null)
				returnConnection(conn);
		}
	}

	public void putWatchDir(File dir) {
		if (!running)
			return;
		Connection conn = null;
		try {
			conn = getConnection();
			PreparedStatement ps;
			ps = conn.prepareStatement(CREATE_WATCHDIR);
			ps.setString(1, dir.getAbsolutePath());
			ps.executeUpdate();
		} catch (SQLException e) {
			log.error("Error putting watchdir", e);
		} finally {
			if (conn != null)
				returnConnection(conn);
		}
	}

	public void deleteStream(String streamId) {
		if (!running)
			return;
		Connection conn = null;
		try {
			conn = getConnection();
			PreparedStatement ps = conn.prepareStatement(DELETE_STREAM_ATTRIBUTES);
			ps.setString(1, streamId);
			ps.executeUpdate();
			ps = conn.prepareStatement(DELETE_STREAM);
			ps.setString(1, streamId);
			ps.executeUpdate();
		} catch (SQLException e) {
			log.error("Error deleting stream", e);
		} finally {
			if (conn != null)
				returnConnection(conn);
		}
	}

	public void deleteWatchDir(File dir) {
		if (!running)
			return;
		Connection conn = null;
		try {
			conn = getConnection();
			PreparedStatement ps = conn.prepareStatement(DELETE_WATCHDIR);
			ps.setString(1, dir.getAbsolutePath());
			ps.executeUpdate();
		} catch (SQLException e) {
			log.error("Error deleting watchdir", e);
		} finally {
			if (conn != null)
				returnConnection(conn);
		}
	}

	public boolean haveCheckedFile(File file) {
		if (!running)
			return false;
		Connection conn = null;
		try {
			conn = getConnection();
			PreparedStatement ps = conn.prepareStatement(READ_CHECKED_FILE);
			ps.setString(1, file.getAbsolutePath());
			ResultSet rs = ps.executeQuery();
			return rs.next();
		} catch (SQLException e) {
			log.error("Error checking checkedfile", e);
		} finally {
			if (conn != null)
				returnConnection(conn);
		}
		return false;
	}

	public void notifyFileChecked(File file) {
		if (!running)
			return;
		Connection conn = null;
		try {
			conn = getConnection();
			PreparedStatement ps = conn.prepareStatement(CREATE_CHECKED_FILE);
			ps.setString(1, file.getAbsolutePath());
			ps.executeUpdate();
		} catch (SQLException e) {
			log.error("Error notifying filechecked", e);
		} finally {
			if (conn != null)
				returnConnection(conn);
		}
	}

	public PlaylistConfig getPlaylistConfig(long playlistId) {
		if (!running)
			return null;
		PlaylistConfig pc = new PlaylistConfig();
		pc.setPlaylistId(playlistId);
		Connection conn = null;
		try {
			conn = getConnection();
			PreparedStatement ps = conn.prepareStatement(READ_PLAYLIST_CFG);
			ps.setLong(1, pc.getPlaylistId());
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				String itemName = rs.getString("ITEM_NAME");
				String itemVal = rs.getString("ITEM_VAL");
				pc.getItems().put(itemName, itemVal);
			}
		} catch (SQLException e) {
			log.error("Error getting playlistconfig", e);
		} finally {
			if (conn != null)
				returnConnection(conn);
		}
		return pc;
	}

	public void putPlaylistConfig(PlaylistConfig pc) {
		if (!running)
			return;
		Connection conn = null;
		try {
			conn = getConnection();
			PreparedStatement ps = conn.prepareStatement(DELETE_PLAYLIST_CFG);
			ps.setLong(1, pc.getPlaylistId());
			ps.executeUpdate();
			for (String itemName : pc.getItems().keySet()) {
				ps = conn.prepareStatement(CREATE_PLAYLIST_CFG);
				ps.setLong(1, pc.getPlaylistId());
				ps.setString(2, itemName);
				ps.setString(3, pc.getItems().get(itemName));
				ps.executeUpdate();
			}
		} catch (SQLException e) {
			log.error("Error putting playlistconfig", e);
		} finally {
			if (conn != null)
				returnConnection(conn);
		}
	}

	public int numUnseenTracks(Playlist p) {
		if (!running)
			return 0;
		Set<String> sids = new HashSet<String>();
		sids.addAll(p.getStreamIds());
		Connection conn = null;
		try {
			conn = getConnection();
			PreparedStatement ps = conn.prepareStatement(READ_PLAYLIST_SEEN_SIDS);
			ps.setLong(1, p.getPlaylistId());
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				sids.remove(rs.getString("STREAM_ID"));
			}
			return sids.size();
		} catch (SQLException e) {
			log.error("Error determining unseen tracks for playlist id " + p.getPlaylistId(), e);
			return 0;
		} finally {
			if (conn != null)
				returnConnection(conn);
		}
	}

	public int numUnseenTracks(Library lib) {
		if (!running)
			return 0;
		Connection conn = null;
		try {
			conn = getConnection();
			PreparedStatement ps = conn.prepareStatement(READ_LIBRARY_NUM_UNSEEN_SIDS);
			ps.setLong(1, lib.getUserId());
			ps.setLong(2, lib.getUserId());
			ResultSet rs = ps.executeQuery();
			if (!rs.next())
				return 0;
			return rs.getInt(1);
		} catch (SQLException e) {
			log.error("Error determining unseen tracks of library for user id " + lib.getUserId(), e);
			return 0;
		} finally {
			if (conn != null)
				returnConnection(conn);
		}
	}

	public Library getLibrary(long userId) {
		if (!running)
			return null;
		Connection conn = null;
		try {
			conn = getConnection();
			PreparedStatement ps = conn.prepareStatement(READ_LIBRARY_TRACKS);
			ps.setLong(1, userId);
			ResultSet rs = ps.executeQuery();
			Map<String, Date> tracks = new HashMap<String, Date>();
			while (rs.next()) {
				tracks.put(rs.getString(1), new Date(rs.getLong(2)));
			}
			if (tracks.size() == 0)
				return null;
			Library lib = new Library();
			lib.setUserId(userId);
			lib.setTracks(tracks);
			ps = conn.prepareStatement(READ_LIBRARY_LAST_CHECKED);
			ps.setLong(1, userId);
			rs = ps.executeQuery();
			if (rs.next())
				lib.setLastUpdated(new Date(rs.getLong(1)));
			return lib;
		} catch (SQLException e) {
			log.error("Error looking up library for user" + userId, e);
			return null;
		} finally {
			if (conn != null)
				returnConnection(conn);
		}
	}

	/**
	 * Returns the sids for the streams in this user's library we haven't yet looked up
	 */
	public Map<String, Date> getUnknownStreamsInLibrary(long userId) {
		if (!running)
			return null;
		Connection conn = null;
		try {
			conn = getConnection();
			PreparedStatement ps = conn.prepareStatement(READ_LIBRARY_UNKNOWN_STREAMS);
			ps.setLong(1, userId);
			ResultSet rs = ps.executeQuery();
			Map<String, Date> result = new HashMap<String, Date>();
			while (rs.next()) {
				result.put(rs.getString(1), new Date(rs.getLong(2)));
			}
			return result;
		} catch (SQLException e) {
			log.error("Error getting unknown streams from library for user" + userId, e);
			return null;
		} finally {
			if (conn != null)
				returnConnection(conn);
		}
	}

	public void addTracksToLibrary(long userId, Map<String, Date> newTracks) {
		if (!running)
			return;
		Connection conn = null;
		try {
			conn = getConnection();
			PreparedStatement ps = conn.prepareStatement(CREATE_LIBRARY_TRACK);
			for (Entry<String, Date> tEntry : newTracks.entrySet()) {
				String sid = tEntry.getKey();
				Date addedDate = tEntry.getValue();
				ps.setLong(1, userId);
				ps.setString(2, sid);
				ps.setLong(3, addedDate.getTime());
				ps.addBatch();
			}
			ps.executeBatch();
			ps = conn.prepareStatement(CREATE_LIBRARY_LAST_CHECKED);
			ps.setLong(1, userId);
			long now = System.currentTimeMillis();
			ps.setLong(2, now);
			ps.setLong(3, now);
			ps.executeUpdate();
		} catch (SQLException e) {
			log.error("Error adding tracks to library for user " + userId, e);
		} finally {
			if (conn != null)
				returnConnection(conn);
		}
	}

	public void markAllAsSeen(Playlist p) {
		if (!running)
			return;
		Connection conn = null;
		try {
			conn = getConnection();
			PreparedStatement ps = conn.prepareStatement(DELETE_PLAYLIST_SEEN_SIDS);
			ps.setLong(1, p.getPlaylistId());
			ps.executeUpdate();
			ps = conn.prepareStatement(CREATE_PLAYLIST_SEEN_SID);
			for (String sid : p.getStreamIds()) {
				ps.setLong(1, p.getPlaylistId());
				ps.setString(2, sid);
				ps.addBatch();
			}
			ps.executeBatch();
		} catch (SQLException e) {
			log.error("Error marking tracks as seen for playlist id " + p.getPlaylistId(), e);
		} finally {
			if (conn != null)
				returnConnection(conn);
		}
	}

	public void markAllAsSeen(Library lib) {
		if (!running)
			return;
		Set<String> sids = new HashSet<String>();
		sids.addAll(lib.getTracks().keySet());
		long start = System.currentTimeMillis();
		Connection conn = null;
		long userId = lib.getUserId();
		try {
			conn = getConnection();
			PreparedStatement ps = conn.prepareStatement(UPDATE_LIBRARY_SEEN_SIDS);
			ps.setLong(1, userId);
			ps.setLong(2, userId);
			ps.setLong(3, userId);
			ps.executeUpdate();
		} catch (SQLException e) {
			log.error("Error marking tracks as seen for library for user " + userId, e);
		} finally {
			if (conn != null)
				returnConnection(conn);
		}

		long end = System.currentTimeMillis();
		log.debug("Marked lib tracks as seen: " + (end - start) + " ms");
	}
}
