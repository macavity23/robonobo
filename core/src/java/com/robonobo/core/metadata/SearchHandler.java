package com.robonobo.core.metadata;

import com.robonobo.core.api.proto.CoreApi.SearchResponse;

public interface SearchHandler {
	public void success(SearchResponse response);

	public void error(String query, Exception e);
}
