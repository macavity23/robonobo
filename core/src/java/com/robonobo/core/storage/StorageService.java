package com.robonobo.core.storage;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.robonobo.common.pageio.buffer.FilePageBuffer;
import com.robonobo.core.api.model.Stream;
import com.robonobo.core.service.AbstractService;
import com.robonobo.mina.external.buffer.*;

@SuppressWarnings("unchecked")
public class StorageService extends AbstractService implements PageBufferProvider {
	protected Map<String, FilePageBuffer> bufferCache = new HashMap<String, FilePageBuffer>();
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
		synchronized (this) {
			for (PageBuffer pb : bufferCache.values()) {
				try {
					pb.sleep();
				} catch (IOException ignore) {
				}
			}			
		}
	}

	public FilePageBuffer createPageBufForDownload(Stream s, File dataFile) throws IOException {
		FilePageBuffer pb = pim.createPageBuf(s, dataFile);
		synchronized (this) {
			bufferCache.put(s.getStreamId(), pb);			
		}
		return pb;
	}

	/**
	 * @param initPageInfo true if we should create params in the db for this pagebuf, false if they're already there
	 * TODO should figure this out ourselves maybe?
	 */
	public FilePageBuffer createPageBufForShare(Stream s, File dataFile, boolean initPageInfo) throws IOException {
		FilePageBuffer pb;
		if (initPageInfo)
			pb = pim.createPageBuf(s, dataFile);
		else
			pb = pim.updateAndReturnPageBuf(s, dataFile);
		synchronized (this) {
			bufferCache.put(s.getStreamId(), pb);			
		}
		return pb;
	}

	@Override
	public FilePageBuffer getPageBuf(String sid) {
		return getPageBuf(sid, true);
	}

	public FilePageBuffer getPageBuf(String sid, boolean cacheResult) {
		synchronized (this) {
			if (bufferCache.containsKey(sid))
				return bufferCache.get(sid);			
		}
		FilePageBuffer pb = pim.getPageBuffer(sid);
		if(pb != null && cacheResult) {
			synchronized (this) {
				bufferCache.put(sid, pb);				
			}
		}
		return pb;
		
	}
	
	public PageInfo getPageInfo(String streamId, long pageNum) {
		return pim.getPageInfo(streamId, pageNum);
	}

	public void nukePageBuf(String streamId) {
		log.debug("Nuking pagebuf for stream " + streamId);
		pim.nuke(streamId);
		synchronized (this) {
			bufferCache.remove(streamId);
		}
	}
	
	public Connection getPageDbConnection() throws SQLException {
		return pim.getConnection();
	}
	
	public void returnPageDbConnection(Connection conn) {
		pim.returnConnection(conn);
	}
}
