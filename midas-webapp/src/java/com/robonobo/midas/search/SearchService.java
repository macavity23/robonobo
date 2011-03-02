package com.robonobo.midas.search;

import java.io.IOException;

import com.robonobo.core.api.proto.CoreApi.SearchResponse;

public interface SearchService {

	public abstract SearchResponse search(String searchType, String queryStr, int firstResult) throws IOException;

}