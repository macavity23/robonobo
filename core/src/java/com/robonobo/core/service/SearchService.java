package com.robonobo.core.service;

import com.robonobo.core.api.SearchListener;
import com.robonobo.core.api.model.Stream;
import com.robonobo.core.api.proto.CoreApi.SearchResponse;
import com.robonobo.core.metadata.*;

/**
 * Handles searches, manages resultant http threads
 * 
 * @author macavity
 * 
 */
@SuppressWarnings("unchecked")
public class SearchService extends AbstractService {
	AbstractMetadataService metadata;
	StreamService streams;

	public SearchService() {
		addHardDependency("core.metadata");
		addHardDependency("core.streams");
	}

	@Override
	public void startup() throws Exception {
		metadata = rbnb.getMetadataService();
		streams = rbnb.getStreamService();
	}

	@Override
	public void shutdown() throws Exception {
		// Do nothing
	}

	public String getName() {
		return "Search service";
	}

	public String getProvides() {
		return "core.search";
	}

	public void startSearch(String query, int startResult, SearchListener listener) {
		log.debug("Launching search query for "+query);
		metadata.search(query, startResult, new Handler(listener));
	}

	class Handler implements SearchCallback, StreamCallback {
		private SearchListener listener;

		public Handler(SearchListener listener) {
			this.listener = listener;
		}

		public void success(SearchResponse sr) {
			listener.gotNumberOfResults(sr.getObjectIdCount());
			streams.fetchStreams(sr.getObjectIdList(), this);
		}

		public void success(Stream s) {
			listener.foundResult(s);
		}

		// TODO the param might be a search query or a sid... could separate this into another class, but, on the other
		// hand, as long as we're logging the exception, fuckit
		public void error(String hmmm, Exception e) {
			log.error("Error searching", e);
		}
	}
}
