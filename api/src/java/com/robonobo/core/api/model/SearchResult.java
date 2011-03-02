package com.robonobo.core.api.model;

import java.util.ArrayList;
import java.util.List;

/**
 * The results of a search, which is a list of stream and channel ids (as
 * Strings)
 * 
 * @author macavity
 */
public class SearchResult {
	private List matchingStreamIds = new ArrayList();
	private List matchingChannelIds = new ArrayList();

	public SearchResult() {
	}

	public List getMatchingStreamIds() {
		return matchingStreamIds;
	}

	public void setMatchingStreamIds(List matchingStreamIds) {
		this.matchingStreamIds = matchingStreamIds;
	}

	public List getMatchingChannelIds() {
		return matchingChannelIds;
	}

	public void setMatchingChannelIds(List matchingChannelIds) {
		this.matchingChannelIds = matchingChannelIds;
	}
}
