package com.robonobo.core.api;

import com.robonobo.core.api.model.Stream;
import com.robonobo.mina.external.FoundSourceListener;

public interface SearchListener extends FoundSourceListener {
	public void gotNumberOfResults(int numResults);
	public void foundResult(Stream s);
}
