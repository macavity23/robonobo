package com.robonobo.midas.client;

import com.robonobo.common.util.TextUtil;
import com.robonobo.core.api.proto.CoreApi.SearchResponse;
import com.robonobo.core.metadata.SearchHandler;
import com.robonobo.midas.client.Params.Operation;

public class SearchRequest implements Request {
	private MidasClientConfig cfg;
	private String query;
	private int firstResult;
	private SearchHandler handler;

	public SearchRequest(MidasClientConfig cfg, String query, int firstResult, SearchHandler handler) {
		this.cfg = cfg;
		this.query = query;
		this.firstResult = firstResult;
		this.handler = handler;
	}

	@Override
	public int remaining() {
		if (query == null)
			return 0;
		return 1;
	}

	@Override
	public Params getNextParams() {
		Params p = new Params(Operation.Get, null, SearchResponse.newBuilder(), cfg.getSearchQueryUrl(query, firstResult), query);
		query = null;
		return p;
	}

	@Override
	public void success(Object obj) {
		SearchResponse resp = (SearchResponse) obj;
		handler.success(resp);
	}

	@Override
	public void error(Params p, Exception e) {
		String q = (String) p.obj;
		handler.error(q, e);
	}
}
