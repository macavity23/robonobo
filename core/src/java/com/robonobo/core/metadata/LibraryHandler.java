package com.robonobo.core.metadata;

import com.robonobo.core.api.model.Library;

public interface LibraryHandler {
	public void success(Library l);

	public void error(long userId, Exception e);
}
