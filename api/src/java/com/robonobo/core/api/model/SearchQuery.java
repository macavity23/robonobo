package com.robonobo.core.api.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Contains search terms, which are in name/value pairs (both name and value are
 * strings)
 * 
 * @author macavity
 * 
 */
public class SearchQuery {
	/** Map<String, String> */
	private final Map terms = new HashMap();

	public SearchQuery() {
	}

	public Map getTerms() {
		return terms;
	}
}
