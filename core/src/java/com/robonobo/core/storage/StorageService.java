package com.robonobo.core.storage;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.robonobo.core.api.model.Stream;
import com.robonobo.core.service.AbstractService;
import com.robonobo.mina.external.buffer.PageBuffer;
import com.robonobo.mina.external.buffer.PageInfo;

@SuppressWarnings("unchecked")
public class StorageService extends AbstractService {
	protected Map<String, PageBuffer> bufferCache = new HashMap<String, PageBuffer>();
	Log log = LogFactory.getLog(getClass());
	PageInfoMgr pim;

	public StorageService() {
		addHardDependency("core.format");
	}

	@Override
	public void startup() throws Exception {
		String s = File.separator;
		File dbDir = new File(getRobonobo().getHomeDir(), "db");
		if(!dbDir.exists())
			dbDir.mkdirs();
		pim = new PageInfoMgr(dbDir.getAbsolutePath() + s + "pages");
	}

	public String getName() {
		return "Storage service";
	}

	public String getProvides() {
		return "core.storage";
	}

	public void shutdown() {
		try {
			pim.shutdown();
		} catch (Exception e) {
			log.error("Error shutting down PIM", e);
		}
	}

	public PageBuffer createPageBufForDownload(Stream s, File dataFile) throws IOException {
		PageBuffer pb = pim.createPageBuf(s, dataFile);
		bufferCache.put(s.getStreamId(), pb);
		return pb;
	}

	/**
	 * @param initPageInfo true if we should create params in the db for this pagebuf, false if they're already there
	 * TODO should figure this out ourselves maybe?
	 */
	public PageBuffer createPageBufForShare(Stream s, File dataFile, boolean initPageInfo) throws IOException {
		PageBuffer pb;
		if (initPageInfo)
			pb = pim.createPageBuf(s, dataFile);
		else
			pb = pim.updateAndReturnPageBuf(s, dataFile);
		bufferCache.put(s.getStreamId(), pb);
		return pb;
	}

	public PageBuffer loadPageBuf(String streamId) throws IOException {
		if (bufferCache.containsKey(streamId))
			return bufferCache.get(streamId);
		PageBuffer pb = pim.getPageBuffer(streamId);
		bufferCache.put(streamId, pb);
		return pb;
	}

	public PageInfo getPageInfo(String streamId, long pageNum) {
		return pim.getPageInfo(streamId, pageNum);
	}

	public void nukePageBuf(String streamId) {
		log.debug("Nuking pagebuf for stream " + streamId);
		pim.nuke(streamId);
		bufferCache.remove(streamId);
	}
	
	public Connection getPageDbConnection() throws SQLException {
		return pim.getConnection();
	}
	
	public void returnPageDbConnection(Connection conn) {
		pim.returnConnection(conn);
	}
}
