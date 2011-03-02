package com.robonobo.core.storage;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.robonobo.common.pageio.buffer.FilePageBuffer;
import com.robonobo.common.pageio.buffer.PageInfoStore;
import com.robonobo.core.api.model.Stream;
import com.robonobo.mina.external.buffer.PageInfo;
import com.robonobo.mina.external.buffer.StreamPosition;
import com.twmacinta.util.MD5;

public class PageInfoMgr implements PageInfoStore {
	private static final String CREATE_PB_PARAMS_SQL = "CREATE CACHED TABLE PB_PARAMS (STREAM_ID VARCHAR(36) NOT NULL PRIMARY KEY, TOTAL_PAGES BIGINT, PAGES_RECVD BIGINT, BYTES_RECVD BIGINT, LAST_CONTIG_PAGE BIGINT, FILE_PATH VARCHAR(1024))";
	private static final String INSERT_PB_PARAMS_SQL = "INSERT INTO PB_PARAMS (STREAM_ID, TOTAL_PAGES, PAGES_RECVD, BYTES_RECVD, LAST_CONTIG_PAGE, FILE_PATH) VALUES (?, ?, ?, ?, ?, ?)";
	private static final String UPDATE_PB_FILE_SQL = "UPDATE PB_PARAMS SET FILE_PATH = ? WHERE STREAM_ID = ?";

	private static final String GET_PB_PARAMS_SQL = "SELECT * FROM PB_PARAMS WHERE STREAM_ID = ?";
	private static final String GET_TOTAL_PAGES_SQL = "SELECT TOTAL_PAGES FROM PB_PARAMS WHERE STREAM_ID = ?";
	private static final String GET_PAGES_RECVD_SQL = "SELECT PAGES_RECVD FROM PB_PARAMS WHERE STREAM_ID = ?";
	private static final String GET_BYTES_RECVD_SQL = "SELECT BYTES_RECVD FROM PB_PARAMS WHERE STREAM_ID = ?";
	private static final String GET_LAST_CONTIG_PAGE_SQL = "SELECT LAST_CONTIG_PAGE FROM PB_PARAMS WHERE STREAM_ID = ?";

	private static final String SET_PB_PARAMS_SQL = "UPDATE PB_PARAMS SET TOTAL_PAGES = ?, PAGES_RECVD = ?, BYTES_RECVD = ?, LAST_CONTIG_PAGE = ? WHERE STREAM_ID = ?";
	private static final String UPDATE_PB_PARAMS_SQL = "UPDATE PB_PARAMS SET PAGES_RECVD = (PAGES_RECVD + 1), BYTES_RECVD = (BYTES_RECVD + ?), LAST_CONTIG_PAGE = ? WHERE STREAM_ID = ?";
	private static final String UPDATE_TOTAL_PAGES_SQL = "UPDATE PB_PARAMS SET TOTAL_PAGES = ? WHERE STREAM_ID = ?";

	private static final String NUKE_PB_PARAMS_SQL = "DELETE FROM PB_PARAMS WHERE STREAM_ID = ?";

	private static final int PAGEMAP_SZ = 32;

	Log log = LogFactory.getLog(getClass());
	private final List<Connection> freeConns = new ArrayList<Connection>();
	private Map<String, String> tableNames = new HashMap<String, String>();
	private final String dbUrl;
	private int numConnsCreated = 0;

	public PageInfoMgr(String dbPathPrefix) {
		try {
			Class.forName("org.hsqldb.jdbcDriver");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		dbUrl = "jdbc:hsqldb:file:" + dbPathPrefix;
		File dbPropsFile = new File(dbPathPrefix + ".properties");
		if (!dbPropsFile.exists()) {
			log.info("Creating pageDB with prefix " + dbPathPrefix);
			try {
				Connection conn = getConnection();
				Statement s = conn.createStatement();
				s.executeUpdate(CREATE_PB_PARAMS_SQL);
				s.close();
				returnConnection(conn);
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		} else
			log.info("Using pageDB with prefix " + dbPathPrefix);
	}

	public FilePageBuffer createPageBuf(Stream s, File f) throws IOException {
		Connection conn = null;
		String sid = s.getStreamId();
		try {
			conn = getConnection();
			
			PreparedStatement ps = conn.prepareStatement(getInitStreamSQL(sid));
			ps.executeUpdate();
			ps.close();

			ps = conn.prepareStatement(INSERT_PB_PARAMS_SQL);
			ps.setString(1, sid);
			ps.setLong(2, -1);
			ps.setLong(3, 0);
			ps.setLong(4, 0);
			ps.setLong(5, -1);
			ps.setString(6, f.getAbsolutePath());
			ps.executeUpdate();
			ps.close();
			
			FilePageBuffer pb = new FilePageBuffer(sid, f, this);
			return pb;
		} catch (SQLException e) {
			log.error("Caught sqlexception creating pagebuf for stream " + sid, e);
			throw new IOException("Error: " + e.getMessage());
		} finally {
			if (conn != null)
				returnConnection(conn);
		}
	}

	public FilePageBuffer updateAndReturnPageBuf(Stream s, File f) throws IOException {
		Connection conn = null;
		String sid = s.getStreamId();
		try {
			conn = getConnection();
			PreparedStatement ps = conn.prepareStatement(UPDATE_PB_FILE_SQL);
			ps.setString(1, f.getAbsolutePath());
			ps.setString(2, sid);
			ps.executeUpdate();
			ps.close();
			FilePageBuffer pb = new FilePageBuffer(sid, f, this);
			return pb;
		} catch (SQLException e) {
			log.error("Caught sqlexception creating pagebuf for stream " + sid, e);
			throw new IOException("Error: " + e.getMessage());
		} finally {
			if (conn != null)
				returnConnection(conn);
		}		
	}
	
	public FilePageBuffer getPageBuffer(String streamId) {
		Connection conn = null;
		try {
			conn = getConnection();
			PreparedStatement ps = conn.prepareStatement(GET_PB_PARAMS_SQL);
			ps.setString(1, streamId);
			ResultSet rs = ps.executeQuery();
			FilePageBuffer pb = null;
			if (rs.next()) {
				File f = new File(rs.getString("FILE_PATH"));
				pb = new FilePageBuffer(streamId, f, this);
			}
			ps.close();
			return pb;
		} catch (SQLException e) {
			log.error("Caught sqlexception creating pagebuf for stream " + streamId, e);
		} finally {
			if (conn != null)
				returnConnection(conn);
		}
		return null;
	}

	public long getBytesReceived(String streamId) {
		Connection conn = null;
		try {
			conn = getConnection();
			PreparedStatement ps = conn.prepareStatement(GET_BYTES_RECVD_SQL);
			ps.setString(1, streamId);
			ResultSet rs = ps.executeQuery();
			long result = 0;
			if (rs.next()) {
				result = rs.getLong("BYTES_RECVD");
			}
			ps.close();
			return result;
		} catch (SQLException e) {
			log.error("Caught sqlexception retrieving data for stream " + streamId, e);
		} finally {
			if (conn != null)
				returnConnection(conn);
		}
		return 0;
	}

	public long getLastContiguousPage(String streamId) {
		Connection conn = null;
		try {
			conn = getConnection();
			PreparedStatement ps = conn.prepareStatement(GET_LAST_CONTIG_PAGE_SQL);
			ps.setString(1, streamId);
			ResultSet rs = ps.executeQuery();
			long result = -1;
			if (rs.next()) {
				result = rs.getLong("LAST_CONTIG_PAGE");
			}
			ps.close();
			return result;
		} catch (SQLException e) {
			log.error("Caught sqlexception retrieving data for stream " + streamId, e);
		} finally {
			if (conn != null)
				returnConnection(conn);
		}
		return -1;
	}

	public StreamPosition getStreamPosition(String streamId) {
		long lastContig = getLastContiguousPage(streamId);
		long firstPageInMap = lastContig + 1;
		long lastPageInMap = lastContig + PAGEMAP_SZ;
		int pageMap = 0;
		Connection conn = null;
		try {
			conn = getConnection();
			PreparedStatement ps = conn.prepareStatement(getPageMapSQL(streamId, firstPageInMap, lastPageInMap));
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				long gotPageNum = rs.getLong(1);
				int toShift = (int) (gotPageNum - firstPageInMap);
				pageMap |= (1 << toShift);
			}
		} catch (SQLException e) {
			log.error("Caught sqlexception getting pagemap for stream " + streamId, e);
		} finally {
			if (conn != null)
				returnConnection(conn);
		}
		return new StreamPosition(lastContig, pageMap);
	}

	public long getPagesReceived(String streamId) {
		Connection conn = null;
		try {
			conn = getConnection();
			PreparedStatement ps = conn.prepareStatement(GET_PAGES_RECVD_SQL);
			ps.setString(1, streamId);
			ResultSet rs = ps.executeQuery();
			long result = 0;
			if (rs.next()) {
				result = rs.getLong("PAGES_RECVD");
			}
			ps.close();
			return result;
		} catch (SQLException e) {
			log.error("Caught sqlexception retrieving data for stream " + streamId, e);
		} finally {
			if (conn != null)
				returnConnection(conn);
		}
		return 0;
	}

	public long getTotalPages(String streamId) {
		Connection conn = null;
		try {
			conn = getConnection();
			PreparedStatement ps = conn.prepareStatement(GET_TOTAL_PAGES_SQL);
			ps.setString(1, streamId);
			ResultSet rs = ps.executeQuery();
			long result = -1;
			if (rs.next()) {
				result = rs.getLong("TOTAL_PAGES");
			}
			ps.close();
			return result;
		} catch (SQLException e) {
			log.error("Caught sqlexception retrieving data for stream " + streamId, e);
		} finally {
			if (conn != null)
				returnConnection(conn);
		}
		return -1;
	}

	public void setTotalPages(String streamId, long totalPages) {
		Connection conn = null;
		try {
			conn = getConnection();
			PreparedStatement ps = conn.prepareStatement(UPDATE_TOTAL_PAGES_SQL);
			ps.setLong(1, totalPages);
			ps.setString(2, streamId);
			ps.executeUpdate();
			ps.close();
		} catch (SQLException e) {
			log.error("Caught sqlexception setting data for stream " + streamId, e);
		} finally {
			if (conn != null)
				returnConnection(conn);
		}
	}

	public void shutdown() throws Exception {
		Connection conn = getConnection();
		Statement s = conn.createStatement();
		s.executeUpdate("SHUTDOWN");
		s.close();
		returnConnection(conn);
	}

	public int getNumPageInfos(String streamId) {
		Connection conn = null;
		try {
			conn = getConnection();
			PreparedStatement ps = conn.prepareStatement(getCountPageInfosSQL(streamId));
			ResultSet rs = ps.executeQuery();
			rs.next();
			int result = rs.getInt(1);
			ps.close();
			return result;
		} catch (SQLException e) {
			log.error("Caught sqlexception retrieving numpageinfos", e);
			return 0;
		} finally {
			if (conn != null)
				returnConnection(conn);
		}
	}

	public boolean haveGotPage(String streamId, long pageNum) {
		Connection conn = null;
		try {
			conn = getConnection();
			PreparedStatement ps = conn.prepareStatement(getGetPageInfoSQL(streamId, pageNum));
			ResultSet rs = ps.executeQuery();
			boolean result = rs.next();
			ps.close();
			return result;
		} catch (SQLException e) {
			log.error("Caught sqlexception retrieving pageinfo", e);
			return false;
		} finally {
			if (conn != null)
				returnConnection(conn);
		}
	}

	public PageInfo getPageInfo(String streamId, long pageNum) {
		Connection conn = null;
		try {
			conn = getConnection();
			PreparedStatement ps = conn.prepareStatement(getGetPageInfoSQL(streamId, pageNum));
			ResultSet rs = ps.executeQuery();
			if (!rs.next()) {
				ps.close();
				return null;
			}
			PageInfo pi = new PageInfo();
			pi.setPageNumber(rs.getLong(1));
			pi.setByteOffset(rs.getLong(2));
			pi.setTimeOffset(rs.getLong(3));
			pi.setLength(rs.getLong(4));
			ps.close();
			return pi;
		} catch (SQLException e) {
			log.error("Caught sqlexception retrieving pageinfo", e);
			return null;
		} finally {
			if (conn != null)
				returnConnection(conn);
		}
	}

	// Synchronized to avoid putting more than one page for a stream in at the
	// same time, which would throw off the pagebuf params
	public synchronized void putPageInfo(String streamId, PageInfo pi) throws IOException {
		if (getPageInfo(streamId, pi.getPageNumber()) != null)
			return;
		Connection conn = null;
		try {
			conn = getConnection();
			// Store our page info and update our pagebuf params as a
			// transaction, so we're consistent if we get kill-9d halfway
			// through
			conn.setAutoCommit(false);
			// Insert page info
			String sql = getInsertPageInfoSQL(streamId, pi.getPageNumber(), pi.getByteOffset(), pi.getTimeOffset(), pi.getLength());
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.executeUpdate();
			ps.close();
			// Figure out which is our last contig page now - we haven't
			// committed this transaction yet so this one won't show
			long lastContigPage = getLastContiguousPage(streamId);
			while ((lastContigPage + 1 == pi.getPageNumber()) || haveGotPage(streamId, lastContigPage + 1)) {
				lastContigPage++;
			}
			ps = conn.prepareStatement(UPDATE_PB_PARAMS_SQL);
			ps.setLong(1, pi.getLength());
			ps.setLong(2, lastContigPage);
			ps.setString(3, streamId);
			ps.executeUpdate();
			ps.close();
			conn.commit();
		} catch (SQLException e) {
			throw new IOException("Caught sqlexception putting pageinfo: " + e.getMessage());
		} finally {
			if (conn != null) {
				try {
					conn.setAutoCommit(true);
				} catch (SQLException ignore) {
				}
				returnConnection(conn);
			}
		}
	}

	public void putAllPageInfo(String streamId, List<PageInfo> pis) throws IOException {
		// This method does not demonstrate the attractiveness of java
		Connection conn = null;
		try {
			conn = getConnection();
			conn.setAutoCommit(false);
			Statement s = conn.createStatement();
			long bytesRecvd = 0;
			long lastPage = -1;
			for (PageInfo pi : pis) {
				s.addBatch(getInsertPageInfoSQL(streamId, pi.getPageNumber(), pi.getByteOffset(), pi.getTimeOffset(), pi.getLength()));
				bytesRecvd += pi.getLength();
				if (pi.getPageNumber() > lastPage)
					lastPage = pi.getPageNumber();
			}
			s.executeBatch();
			s.close();
			// Update pb params
			PreparedStatement ps = conn.prepareStatement(SET_PB_PARAMS_SQL);
			ps.setLong(1, pis.size());
			ps.setLong(2, pis.size());
			ps.setLong(3, bytesRecvd);
			ps.setLong(4, lastPage);
			ps.setString(5, streamId);
			ps.executeUpdate();
			ps.close();
			conn.commit();
		} catch (SQLException e) {
			if (conn != null) {
				try {
					conn.rollback();
				} catch (SQLException ignore) {
				}
			}
			throw new IOException("Caught SQLException: " + e.getMessage());
		} finally {
			if (conn != null) {
				try {
					conn.setAutoCommit(true);
				} catch (SQLException ignore) {
				}
				returnConnection(conn);
			}
		}
	}

	public void nuke(String streamId) {
		Connection conn = null;
		try {
			conn = getConnection();
			PreparedStatement ps = conn.prepareStatement(getNukeStreamSQL(streamId));
			ps.executeUpdate();
			ps.close();
			ps = conn.prepareStatement(NUKE_PB_PARAMS_SQL);
			ps.setString(1, streamId);
			ps.executeUpdate();
			ps.close();
			tableNames.remove(streamId);
		} catch (SQLException e) {
			log.error("Caught sqlexception checking completeness", e);
		} finally {
			if (conn != null)
				returnConnection(conn);
		}
	}

	public synchronized Connection getConnection() throws SQLException {
		if (freeConns.size() > 0)
			return freeConns.remove(0);
		else {
			log.debug("PageDB: now created " + ++numConnsCreated + " db connections");
			return DriverManager.getConnection(dbUrl);
		}
	}

	public synchronized void returnConnection(Connection conn) {
		freeConns.add(conn);
	}

	private String getInitStreamSQL(String streamId) {
		return "CREATE CACHED TABLE " + tableName(streamId)
				+ " (PAGENUM BIGINT NOT NULL PRIMARY KEY, BYTEOFFSET BIGINT, TIMEOFFSET BIGINT, LENGTH BIGINT)";
	}

	private String getGetPageInfoSQL(String streamId, long pageNum) {
		return "SELECT PAGENUM, BYTEOFFSET, TIMEOFFSET, LENGTH FROM " + tableName(streamId) + " WHERE PAGENUM = "
				+ pageNum;
	}

	private String getInsertPageInfoSQL(String streamId, long pageNum, long byteOffset, long timeOffset, long length) {
		StringBuffer sb = new StringBuffer("INSERT INTO ");
		sb.append(tableName(streamId));
		sb.append(" (PAGENUM, BYTEOFFSET, TIMEOFFSET, LENGTH) VALUES (");
		sb.append(pageNum).append(", ");
		sb.append(byteOffset).append(", ");
		sb.append(timeOffset).append(", ");
		sb.append(length).append(")");
		return sb.toString();
	}

	private String getCountPageInfosSQL(String streamId) {
		return "SELECT COUNT(*) FROM " + tableName(streamId);
	}

	private String getPageMapSQL(String streamId, long firstPage, long lastPage) {
		StringBuffer sb = new StringBuffer("SELECT PAGENUM FROM ");
		sb.append(tableName(streamId));
		sb.append(" WHERE PAGENUM >= ").append(firstPage);
		sb.append(" AND PAGENUM <= ").append(lastPage);
		return sb.toString();
	}

	private String getNukeStreamSQL(String streamId) {
		return "DROP TABLE " + tableName(streamId);
	}

	private synchronized String tableName(String streamId) {
		if (tableNames.containsKey(streamId))
			return tableNames.get(streamId);
		MD5 md5 = new MD5();
		md5.Update(streamId);
		String tn = "tbl_" + md5.asHex();
		tableNames.put(streamId, tn);
		return tn;
	}
}
