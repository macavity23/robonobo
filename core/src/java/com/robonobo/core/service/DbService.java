package com.robonobo.core.service;

import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.Date;

import com.robonobo.core.api.model.*;
import com.robonobo.core.api.model.DownloadingTrack.DownloadStatus;
import com.robonobo.core.api.model.SharedTrack.ShareStatus;

/**
 * Encapsulates local db access. Now uses hsqldb rather than derby - much
 * faster!
 */
public class DbService extends AbstractService {
	private final List freeConns = new ArrayList();
	private int numConnsCreated = 0;

	private static final String CREATE_STREAMS_TBL_SQL = "CREATE CACHED TABLE STREAMS (STREAM_ID VARCHAR(36) NOT NULL PRIMARY KEY, TITLE VARCHAR(256) NOT NULL, DESCRIPTION VARCHAR(512), FMT VARCHAR(128) NOT NULL, SIZE BIGINT NOT NULL, DURATION BIGINT NOT NULL)";
	private static final String CREATE_ATTRIBUTES_TBL_SQL = "CREATE CACHED TABLE STREAM_ATTRIBUTES (SM_ID INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, STREAM_ID VARCHAR(36) NOT NULL, M_KEY VARCHAR(256) NOT NULL, M_VAL VARCHAR(256) NOT NULL, CONSTRAINT SM_ATT_FK FOREIGN KEY (STREAM_ID) REFERENCES STREAMS (STREAM_ID) ON DELETE CASCADE)";
	private static final String CREATE_SHARES_TBL_SQL = "CREATE CACHED TABLE SHARES (SHARE_ID INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, STREAM_ID VARCHAR(36) NOT NULL, FILE_PATH VARCHAR(256) NOT NULL, STATUS VARCHAR(32) NOT NULL, DATE_STARTED BIGINT NOT NULL, CONSTRAINT SHARE_FK FOREIGN KEY (STREAM_ID) REFERENCES STREAMS (STREAM_ID) ON DELETE CASCADE)";
	private static final String CREATE_DOWNLOADS_TBL_SQL = "CREATE CACHED TABLE DOWNLOADS (DOWNLOAD_ID INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, STREAM_ID VARCHAR(36) NOT NULL, FILE_PATH VARCHAR(256) NOT NULL, STATUS VARCHAR(32) NOT NULL, DATE_STARTED BIGINT NOT NULL, CONSTRAINT DOWNLOAD_FK FOREIGN KEY (STREAM_ID) REFERENCES STREAMS (STREAM_ID) ON DELETE CASCADE)";
	private static final String CREATE_WATCHDIRS_TBL_SQL = "CREATE CACHED TABLE WATCHDIRS (WATCHDIR_ID INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, DIRPATH VARCHAR(1024) NOT NULL)";
	private static final String CREATE_CHECKED_FILES_TBL_SQL = "CREATE CACHED TABLE CHECKED_FILES (CHECKEDFILE_ID INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, FILEPATH VARCHAR(1024) NOT NULL)";
	private static final String CREATE_PLAYLIST_CFG_TBL_SQL = "CREATE CACHED TABLE PLAYLIST_CFG (PLAYLIST_ID BIGINT NOT NULL, ITEM_NAME VARCHAR(256) NOT NULL, ITEM_VAL VARCHAR(256) NOT NULL)";
	private static final String CREATE_PLAYLIST_SEEN_SIDS_TBL_SQL = "CREATE CACHED TABLE PLAYLIST_SEEN_SIDS (PLAYLIST_ID VARCHAR(64) NOT NULL, STREAM_ID VARCHAR(36) NOT NULL)";

	private static final String READ_STREAM_SQL = "SELECT * FROM STREAMS WHERE STREAM_ID = ?";
	private static final String READ_STREAM_ATTRIBUTES_SQL = "SELECT * FROM STREAM_ATTRIBUTES WHERE STREAM_ID = ?";
	private static final String READ_SHARE_SQL = "SELECT * FROM SHARES WHERE STREAM_ID = ?";
	private static final String READ_SHARE_BY_PATH_SQL = "SELECT * FROM SHARES WHERE FILE_PATH = ?";
	private static final String READ_DOWNLOAD_SQL = "SELECT * FROM DOWNLOADS WHERE STREAM_ID = ?";
	private static final String READ_CHECKED_FILE_SQL = "SELECT * FROM CHECKED_FILES WHERE FILEPATH = ?";
	private static final String READ_PLAYLIST_CFG_SQL = "SELECT * FROM PLAYLIST_CFG WHERE PLAYLIST_ID = ?";
	private static final String READ_PLAYLIST_SEEN_SIDS = "SELECT STREAM_ID FROM PLAYLIST_SEEN_SIDS WHERE PLAYLIST_ID = ?";

	private static final String MATCH_SHARES_SQL = "SELECT DISTINCT sh.* FROM SHARES AS sh, STREAMS AS st WHERE sh.STREAM_ID = st.STREAM_ID AND (lower(st.TITLE) LIKE lower(?) OR lower(st.DESCRIPTION) LIKE lower(?))"
			+ " UNION SELECT DISTINCT sh.* FROM SHARES AS sh, STREAM_ATTRIBUTES AS sa WHERE sh.STREAM_ID = sa.STREAM_ID AND lower(sa.M_VAL) LIKE lower(?)";

	private static final String CREATE_STREAM_SQL = "INSERT INTO STREAMS (STREAM_ID, TITLE, DESCRIPTION, FMT, SIZE, DURATION) VALUES (?, ?, ?, ?, ?, ?)";
	private static final String CREATE_STREAM_ATTRIBUTES_SQL = "INSERT INTO STREAM_ATTRIBUTES (STREAM_ID, M_KEY, M_VAL) VALUES (?, ?, ?)";
	private static final String CREATE_SHARE_SQL = "INSERT INTO SHARES (STREAM_ID, FILE_PATH, STATUS, DATE_STARTED) VALUES (?, ?, ?, ?)";
	private static final String CREATE_DOWNLOAD_SQL = "INSERT INTO DOWNLOADS (STREAM_ID, FILE_PATH, STATUS, DATE_STARTED) VALUES (?, ?, ?, ?)";
	private static final String CREATE_WATCHDIR_SQL = "INSERT INTO WATCHDIRS (DIRPATH) VALUES (?)";
	private static final String CREATE_CHECKED_FILE_SQL = "INSERT INTO CHECKED_FILES (FILEPATH) VALUES (?)";
	private static final String CREATE_PLAYLIST_CFG_SQL = "INSERT INTO PLAYLIST_CFG (PLAYLIST_ID, ITEM_NAME, ITEM_VAL) VALUES (?, ?, ?)";
	private static final String CREATE_PLAYLIST_SEEN_SID = "INSERT INTO PLAYLIST_SEEN_SIDS (PLAYLIST_ID, STREAM_ID) VALUES (?, ?)";
	
	private static final String UPDATE_SHARE_SQL = "UPDATE SHARES SET FILE_PATH = ?, STATUS = ?, DATE_STARTED = ? WHERE STREAM_ID = ?";
	private static final String UPDATE_DOWNLOAD_SQL = "UPDATE DOWNLOADS SET FILE_PATH = ?, STATUS = ?, DATE_STARTED = ? WHERE STREAM_ID = ?";

	private static final String DELETE_STREAM_SQL = "DELETE FROM STREAMS WHERE STREAM_ID = ?";
	private static final String DELETE_STREAM_ATTRIBUTES_SQL = "DELETE FROM STREAM_ATTRIBUTES WHERE STREAM_ID = ?";
	private static final String DELETE_SHARE_SQL = "DELETE FROM SHARES WHERE STREAM_ID = ?";
	private static final String DELETE_DOWNLOAD_SQL = "DELETE FROM DOWNLOADS WHERE STREAM_ID = ?";
	private static final String DELETE_WATCHDIR_SQL = "DELETE FROM WATCHDIRS WHERE DIRPATH = ?";
	private static final String DELETE_PLAYLIST_CFG_SQL = "DELETE FROM PLAYLIST_CFG WHERE PLAYLIST_ID = ?";
	private static final String DELETE_PLAYLIST_SEEN_SIDS = "DELETE FROM PLAYLIST_SEEN_SIDS WHERE PLAYLIST_ID = ?";

	private static final String GET_ALL_SHARE_STREAM_IDS_SQL = "SELECT STREAM_ID FROM SHARES";
	private static final String GET_ALL_DOWNLOAD_STREAM_IDS_SQL = "SELECT STREAM_ID FROM DOWNLOADS ORDER BY DATE_STARTED ASC";
	private static final String GET_NUM_RUNNING_DOWNLOADS_SQL = "SELECT COUNT(*) FROM DOWNLOADS WHERE STATUS = 'Downloading'";
	private static final String GET_ALL_WATCHDIRS_SQL = "SELECT * FROM WATCHDIRS";

	private String dbUrl;

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
			String[] creatStats = { CREATE_STREAMS_TBL_SQL, CREATE_ATTRIBUTES_TBL_SQL, CREATE_SHARES_TBL_SQL,
					CREATE_DOWNLOADS_TBL_SQL, CREATE_WATCHDIRS_TBL_SQL, CREATE_CHECKED_FILES_TBL_SQL, CREATE_PLAYLIST_CFG_TBL_SQL, CREATE_PLAYLIST_SEEN_SIDS_TBL_SQL };
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
	}

	@Override
	public void shutdown() throws Exception {
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
			return (Connection) freeConns.remove(0);
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
		Connection conn = null;
		try {
			conn = getConnection();
			PreparedStatement ps = conn.prepareStatement(READ_STREAM_SQL);
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

			ps = conn.prepareStatement(READ_STREAM_ATTRIBUTES_SQL);
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
		Connection conn = null;
		try {
			conn = getConnection();
			PreparedStatement ps = conn.prepareStatement(GET_ALL_WATCHDIRS_SQL);
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
		Connection conn = null;
		try {
			conn = getConnection();
			PreparedStatement ps = conn.prepareStatement(GET_ALL_SHARE_STREAM_IDS_SQL);
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
		Connection conn = null;
		String sqlPattern = "%" + searchPattern + "%";
		try {
			conn = getConnection();
			PreparedStatement ps = conn.prepareStatement(MATCH_SHARES_SQL);
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
		Connection conn = null;
		try {
			conn = getConnection();
			PreparedStatement ps = conn.prepareStatement(READ_SHARE_SQL);
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
		Connection conn = null;
		try {
			conn = getConnection();
			PreparedStatement ps = conn.prepareStatement(READ_SHARE_BY_PATH_SQL);
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
		Connection conn = null;
		try {
			PreparedStatement ps;
			if (getShare(share.getStream().getStreamId()) != null) {
				conn = getConnection();
				ps = conn.prepareStatement(UPDATE_SHARE_SQL);
				ps.setString(1, share.getFile().getAbsolutePath());
				ps.setString(2, share.getShareStatus().toString());
				ps.setString(3, share.getStream().getStreamId());
				ps.setLong(4, share.getDateAdded().getTime());
			} else {
				conn = getConnection();
				ps = conn.prepareStatement(CREATE_SHARE_SQL);
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
		Connection conn = null;
		try {
			conn = getConnection();
			PreparedStatement ps = conn.prepareStatement(DELETE_SHARE_SQL);
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
		Connection conn = null;
		try {
			conn = getConnection();
			PreparedStatement ps = conn.prepareStatement(GET_ALL_DOWNLOAD_STREAM_IDS_SQL);
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
		Connection conn = null;
		try {
			conn = getConnection();
			PreparedStatement ps = conn.prepareStatement(READ_DOWNLOAD_SQL);
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
		Connection conn = null;
		try {
			conn = getConnection();
			PreparedStatement ps = conn.prepareStatement(GET_NUM_RUNNING_DOWNLOADS_SQL);
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
		Connection conn = null;
		try {
			PreparedStatement ps;
			if (getDownload(d.getStream().getStreamId()) != null) {
				conn = getConnection();
				ps = conn.prepareStatement(UPDATE_DOWNLOAD_SQL);
				ps.setString(1, d.getFile().getAbsolutePath());
				ps.setString(2, d.getDownloadStatus().toString());
				ps.setLong(3, d.getDateAdded().getTime());
				ps.setString(4, d.getStream().getStreamId());
			} else {
				conn = getConnection();
				ps = conn.prepareStatement(CREATE_DOWNLOAD_SQL);
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
		Connection conn = null;
		try {
			conn = getConnection();
			PreparedStatement ps = conn.prepareStatement(DELETE_DOWNLOAD_SQL);
			ps.setString(1, streamId);
			ps.executeUpdate();
		} catch (SQLException e) {
			log.error("Error deleting download", e);
		} finally {
			if (conn != null)
				returnConnection(conn);
		}

	}

	public void putStream(Stream s) {
		if (getStream(s.getStreamId()) != null)
			return;
		Connection conn = null;
		try {
			conn = getConnection();
			PreparedStatement ps;
			ps = conn.prepareStatement(CREATE_STREAM_SQL);
			ps.setString(1, s.getStreamId());
			ps.setString(2, s.getTitle());
			ps.setString(3, s.getDescription());
			ps.setString(4, s.getMimeType());
			ps.setLong(5, s.getSize());
			ps.setLong(6, s.getDuration());
			ps.executeUpdate();
			for (StreamAttribute attr : s.getAttributes()) {
				ps = conn.prepareStatement(CREATE_STREAM_ATTRIBUTES_SQL);
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
		Connection conn = null;
		try {
			conn = getConnection();
			PreparedStatement ps;
			ps = conn.prepareStatement(CREATE_WATCHDIR_SQL);
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
		Connection conn = null;
		try {
			conn = getConnection();
			PreparedStatement ps = conn.prepareStatement(DELETE_STREAM_ATTRIBUTES_SQL);
			ps.setString(1, streamId);
			ps.executeUpdate();
			ps = conn.prepareStatement(DELETE_STREAM_SQL);
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
		Connection conn = null;
		try {
			conn = getConnection();
			PreparedStatement ps = conn.prepareStatement(DELETE_WATCHDIR_SQL);
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
		Connection conn = null;
		try {
			conn = getConnection();
			PreparedStatement ps = conn.prepareStatement(READ_CHECKED_FILE_SQL);
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
		Connection conn = null;
		try {
			conn = getConnection();
			PreparedStatement ps = conn.prepareStatement(CREATE_CHECKED_FILE_SQL);
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
		PlaylistConfig pc = new PlaylistConfig();
		pc.setPlaylistId(playlistId);
		Connection conn = null;
		try {
			conn = getConnection();
			PreparedStatement ps = conn.prepareStatement(READ_PLAYLIST_CFG_SQL);
			ps.setLong(1, pc.getPlaylistId());
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
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
		Connection conn = null;
		try {
			conn = getConnection();
			PreparedStatement ps = conn.prepareStatement(DELETE_PLAYLIST_CFG_SQL);
			ps.setLong(1, pc.getPlaylistId());
			ps.executeUpdate();
			for (String itemName : pc.getItems().keySet()) {
				ps = conn.prepareStatement(CREATE_PLAYLIST_CFG_SQL);
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
		Set<String> sids = new HashSet<String>();
		sids.addAll(p.getStreamIds());
		return numUnseenTracks("playlist:"+p.getPlaylistId(), sids);
	}
	
	public int numUnseenTracks(Library lib) {
		Set<String> sids = new HashSet<String>();
		sids.addAll(lib.getTracks().keySet());
		return numUnseenTracks("library:"+lib.getUserId(), sids);
	}
	
	private int numUnseenTracks(String collectionId, Set<String> sids) {
		Connection conn = null;
		try {
			conn = getConnection();
			PreparedStatement ps = conn.prepareStatement(READ_PLAYLIST_SEEN_SIDS);
			ps.setString(1, collectionId);
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				sids.remove(rs.getString("STREAM_ID"));
			}
			return sids.size();
		} catch (SQLException e) {
			log.error("Error determining unseen tracks for collection "+collectionId, e);
			return 0;
		} finally {
			if (conn != null)
				returnConnection(conn);
		}		
	}
	
	public void markAllAsSeen(Playlist p) {
		Set<String> sids = new HashSet<String>();
		sids.addAll(p.getStreamIds());
		markAllAsSeen("playlist:"+p.getPlaylistId(), sids);
	}
	
	public void markAllAsSeen(Library lib) {
		Set<String> sids = new HashSet<String>();
		sids.addAll(lib.getTracks().keySet());
		markAllAsSeen("library:"+lib.getUserId(), sids);
	}
	
	private void markAllAsSeen(String collectionId, Set<String> sids) {
		Connection conn = null;
		try {
			conn = getConnection();
			PreparedStatement ps = conn.prepareStatement(DELETE_PLAYLIST_SEEN_SIDS);
			ps.setString(1, collectionId);
			ps.executeUpdate();
			for (String sid : sids) {
				ps = conn.prepareStatement(CREATE_PLAYLIST_SEEN_SID);
				ps.setString(1, collectionId);
				ps.setString(2, sid);
				ps.executeUpdate();
			}
		} catch (SQLException e) {
			log.error("Error determining unseen tracks for playlist "+collectionId, e);
		} finally {
			if (conn != null)
				returnConnection(conn);
		}		
	}
}