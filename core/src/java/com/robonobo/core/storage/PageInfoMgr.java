package com.robonobo.core.storage;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.concurrent.locks.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.robonobo.common.exceptions.SeekInnerCalmException;
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

	private static final String CREATE_PAGE_INFO_SQL = "CREATE CACHED TABLE page_info (stream_id VARCHAR(36) NOT NULL, page_num BIGINT NOT NULL, byte_offset BIGINT NOT NULL, time_offset BIGINT NOT NULL, length BIGINT NOT NULL)";
	private static final String CREATE_PAGE_INFO_PI_IDX_SQL = "CREATE UNIQUE INDEX page_info_pi_idx ON page_info(stream_id, page_num)";
	private static final String CREATE_PAGE_INFO_SID_IDX_SQL = "CREATE INDEX page_info_sid_idx ON page_info(stream_id)";
	
	private static final String GET_PAGE_INFO_SQL = "SELECT page_num, byte_offset, time_offset, length FROM page_info WHERE stream_id = ? AND page_num = ?";
	private static final String PUT_PAGE_INFO_SQL = "INSERT INTO page_info (stream_id, page_num, byte_offset, time_offset, length) VALUES(?, ?, ?, ?, ?)";
	private static final String COUNT_PAGE_INFO_SQL = "SELECT COUNT(*) FROM page_info WHERE stream_id = ?";
	private static final String GET_PAGE_MAP_SQL = "SELECT page_num FROM page_info WHERE stream_id = ? AND page_num >= ? AND page_num <= ?";
	
	private static final String DELETE_PAGE_INFO_SQL = "DELETE FROM page_info WHERE stream_id = ?";
	
	private static final int PAGEMAP_SZ = 32;

	Log log = LogFactory.getLog(getClass());
	private final List<Connection> freeConns = new ArrayList<Connection>();
	private Map<String, String> tableNames = new HashMap<String, String>();
	private final String dbUrl;
	private int numConnsCreated = 0;
	
	Lock putPageInfoLock = new ReentrantLock();
	boolean connInUse = false;
	Connection conn;

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
				s.executeUpdate(CREATE_PAGE_INFO_SQL);
				s.executeUpdate(CREATE_PAGE_INFO_PI_IDX_SQL);
				s.executeUpdate(CREATE_PAGE_INFO_SID_IDX_SQL);
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
			PreparedStatement ps = conn.prepareStatement(INSERT_PB_PARAMS_SQL);
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
			return getLastContiguousPage(streamId, conn);
		} catch (SQLException e) {
			log.error("Caught sqlexception retrieving data for stream " + streamId, e);
		} finally {
			if (conn != null)
				returnConnection(conn);
		}
		return -1;	
	}
	
	private long getLastContiguousPage(String streamId, Connection conn) {
		try {
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
		}
		return -1;
	}

	public StreamPosition getStreamPosition(String streamId) {
		int pageMap = 0;
		long lastContig = -1;
		Connection conn = null;
		try {
			conn = getConnection();
			lastContig = getLastContiguousPage(streamId, conn);
			long firstPageInMap = lastContig + 1;
			long lastPageInMap = lastContig + PAGEMAP_SZ;
			PreparedStatement ps = conn.prepareStatement(GET_PAGE_MAP_SQL);
			ps.setString(1, streamId);
			ps.setLong(2, firstPageInMap);
			ps.setLong(3, lastPageInMap);
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
			PreparedStatement ps = conn.prepareStatement(COUNT_PAGE_INFO_SQL);
			ps.setString(1, streamId);
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
			return haveGotPage(streamId, pageNum, conn);
		} catch (SQLException e) {
			log.error("Caught sqlexception retrieving pageinfo", e);
			return false;
		} finally {
			if (conn != null)
				returnConnection(conn);
		}
	}
	
	private boolean haveGotPage(String streamId, long pageNum, Connection conn) throws SQLException {
		PreparedStatement ps = conn.prepareStatement(GET_PAGE_INFO_SQL);
		ps.setString(1, streamId);
		ps.setLong(2, pageNum);
		ResultSet rs = ps.executeQuery();
		boolean result = rs.next();
		ps.close();
		return result;
	}

	public PageInfo getPageInfo(String streamId, long pageNum) {
		Connection conn = null;
		try {
			conn = getConnection();
			PreparedStatement ps = conn.prepareStatement(GET_PAGE_INFO_SQL);
			ps.setString(1, streamId);
			ps.setLong(2, pageNum);
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

	public void putPageInfo(String streamId, PageInfo pi) throws IOException {
		if (getPageInfo(streamId, pi.getPageNumber()) != null)
			return;
		Connection conn = null;
		// Use a lock to make sure we only put one pageinfo at a time to ensure our last contig page value is correct
		putPageInfoLock.lock();
		try {
			conn = getConnection();
			// Store our page info and update our pagebuf params as a
			// transaction, so we're consistent if we get kill-9d halfway
			// through
			conn.setAutoCommit(false);
			// Insert page info
			PreparedStatement ps = conn.prepareStatement(PUT_PAGE_INFO_SQL);
			ps.setString(1, streamId);
			ps.setLong(2, pi.getPageNumber());
			ps.setLong(3, pi.getByteOffset());
			ps.setLong(4, pi.getTimeOffset());
			ps.setLong(5, pi.getLength());
			ps.executeUpdate();
			ps.close();
			// Figure out which is our last contig page now - we haven't
			// committed this transaction yet so this one won't show
			long lastContigPage = getLastContiguousPage(streamId, conn);
			while ((lastContigPage + 1 == pi.getPageNumber()) || haveGotPage(streamId, lastContigPage + 1, conn)) {
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
			putPageInfoLock.unlock();
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
			long bytesRecvd = 0;
			long lastPage = -1;
			PreparedStatement ps = conn.prepareStatement(PUT_PAGE_INFO_SQL);
			for (PageInfo pi : pis) {
				ps.setString(1, streamId);
				ps.setLong(2, pi.getPageNumber());
				ps.setLong(3, pi.getByteOffset());
				ps.setLong(4, pi.getTimeOffset());
				ps.setLong(5, pi.getLength());
				ps.addBatch();
				bytesRecvd += pi.getLength();
				if (pi.getPageNumber() > lastPage)
					lastPage = pi.getPageNumber();
			}
			ps.executeBatch();
			ps.close();
			// Update pb params
			ps = conn.prepareStatement(SET_PB_PARAMS_SQL);
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
			PreparedStatement ps = conn.prepareStatement(DELETE_PAGE_INFO_SQL);
			ps.setString(1, streamId);
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
		try {
			if(conn == null)
				conn = DriverManager.getConnection(dbUrl);
			while(connInUse)
				wait();
			connInUse = true;
			return conn;
		} catch (InterruptedException e) {
			log.debug("Caught interruptedexception waiting for pim db lock");
			return null;
		}
		
//		if (freeConns.size() > 0)
//			return freeConns.remove(0);
//		else {
//			log.debug("PageDB: now created " + ++numConnsCreated + " db connections");
//			Connection conn = DriverManager.getConnection(dbUrl);
//			Statement st = conn.createStatement();
//			
//			// DEBUG
//			st.execute("SET FILES LOG SIZE 0");
//			
//			
//			return conn;
//		}
	}

	public synchronized void returnConnection(Connection conn) {
		connInUse = false;
		notify();
//		freeConns.add(conn);
	}
}
