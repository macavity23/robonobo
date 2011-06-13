package com.robonobo.core.service;

import static com.robonobo.common.util.CodeUtil.*;

import java.util.*;

import com.robonobo.core.api.model.Stream;
import com.robonobo.core.metadata.AbstractMetadataService;
import com.robonobo.core.metadata.StreamHandler;

public class StreamService extends AbstractService {
	AbstractMetadataService metadata;
	DbService db;

	public StreamService() {
		addHardDependency("core.metadata");
		addHardDependency("core.db");
	}

	@Override
	public String getName() {
		return "Stream Service";
	}

	@Override
	public String getProvides() {
		return "core.streams";
	}

	@Override
	public void startup() throws Exception {
		metadata = rbnb.getMetadataService();
		db = rbnb.getDbService();
	}

	@Override
	public void shutdown() throws Exception {
		// Do nothing
	}

	/**
	 * This will only return a stream if it has already been looked up from the metadata service - to do this, call
	 * fetchStreams() in this class - don't call the metadata service directly
	 */
	public Stream getKnownStream(String sid) {
		return db.getStream(sid);
	}

	public void putStream(Stream s) {
		db.putStream(s);
		metadata.putStream(s, null);
	}

	/**
	 * Fetches metadata from our remote metadata service. Returns immediately - if you want to be informed when the
	 * stream has been fetched, pass in a handler
	 */
	public void fetchStreams(Collection<String> sids, StreamHandler handler) {
		List<String> lookupSids = new ArrayList<String>();
		for (String sid : sids) {
			Stream s = getKnownStream(sid);
			if(s == null)
				lookupSids.add(sid);
			else if (handler != null)
				handler.success(s);
		}
		if(lookupSids.size() > 0)
			metadata.fetchStreams(lookupSids, new AddToDbHandler(handler));
	}
	
	class AddToDbHandler implements StreamHandler {
		StreamHandler onwardHandler;
		public AddToDbHandler(StreamHandler onwardHandler) {
			this.onwardHandler = onwardHandler;
		}

		public void success(Stream s) {
			db.putStream(s);
			if(onwardHandler != null)
				onwardHandler.success(s);
		}
		
		public void error(String streamId, Exception ex) {
			log.error("Caught "+shortClassName(ex.getClass())+" fetching stream from metadataservice:"+ex.getMessage());
			if(onwardHandler != null)
				onwardHandler.error(streamId, ex);
		}
	}
}
