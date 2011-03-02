package com.robonobo.core.search;

import java.util.*;

import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.common.serialization.SerializationManager;
import com.robonobo.common.util.TextUtil;
import com.robonobo.core.api.ActiveSearch;
import com.robonobo.core.api.SearchListener;
import com.robonobo.core.api.model.Stream;
import com.robonobo.core.api.proto.CoreApi.SearchResponse;
import com.robonobo.core.service.AbstractService;

/**
 * Handles searches, manages resultant http threads
 * 
 * @author macavity
 * 
 */
@SuppressWarnings("unchecked")
public class SearchService extends AbstractService {
	
	public SearchService() {
	}

	@Override
	public void shutdown() throws Exception {
		// Do nothing
	}

	@Override
	public void startup() throws Exception {
		// Do nothing
	}

	public String getName() {
		return "Search service";
	}

	public String getProvides() {
		return "core.search";
	}

	public void startSearch(final String query, final int startResult, final SearchListener listener) {
		getRobonobo().getExecutor().execute(new CatchingRunnable() {
			public void doRun() throws Exception {
				String searchUrl = getRobonobo().getConfig().getMetadataServerUrl() + "search?type=stream&q="
						+ TextUtil.urlEncode(query);
				if (startResult > 0)
					searchUrl += "&start=" + String.valueOf(startResult);
				SearchResponse.Builder srb = SearchResponse.newBuilder();
				getRobonobo().getSerializationManager().getObjectFromUrl(srb, searchUrl);
				SearchResponse sr = srb.build();
				listener.gotNumberOfResults(sr.getObjectIdCount());
				// Split up our responses into one list per http thread
				int numLookupThreads = SerializationManager.MAX_HTTP_CONNECTIONS_PER_HOST;
				List[] streamIdLists = new List[numLookupThreads];
				for (int i = 0; i < streamIdLists.length; i++) {
					streamIdLists[i] = new ArrayList<String>();
				}
				int j = 0;
				for (String streamId : sr.getObjectIdList()) {
					streamIdLists[j].add(streamId);
					if (++j == numLookupThreads)
						j = 0;
				}
				// Fire off threads to do the lookups
				for (int i = 0; i < streamIdLists.length; i++) {
					SearchResultLookerUpper srlu = new SearchResultLookerUpper(streamIdLists[i], listener);
					getRobonobo().getExecutor().execute(srlu);
				}
			}
		});
	}

	private class SearchResultLookerUpper extends CatchingRunnable {
		private List<String> streamIds;
		private SearchListener listener;

		public SearchResultLookerUpper(List<String> streamIds, SearchListener listener) {
			this.streamIds = streamIds;
			this.listener = listener;
		}

		public void doRun() throws Exception {
			for (String sId : streamIds) {
				Stream s = getRobonobo().getMetadataService().getStream(sId);
				if (listener != null)
					listener.foundResult(s);
			}
		}
	}
}
