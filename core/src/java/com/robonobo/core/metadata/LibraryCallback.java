package com.robonobo.core.metadata;

import com.robonobo.core.api.model.Library;

public interface LibraryCallback {
	public void success(Library l);

	public void error(long userId, Exception e);
}
