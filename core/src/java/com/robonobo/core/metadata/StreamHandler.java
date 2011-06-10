package com.robonobo.core.metadata;

import com.robonobo.core.api.model.Stream;

public interface StreamHandler {
	public void success(Stream s);
	public void error(String streamId, Exception ex);
}
