package com.robonobo.core.metadata;

import com.robonobo.core.api.proto.CoreApi.SearchResponse;

public interface SearchCallback {
	public void success(SearchResponse response);

	public void error(String query, Exception e);
}
