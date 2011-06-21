package com.robonobo.core.metadata;

import com.robonobo.core.api.model.User;

public interface UserCallback {
	public void success(User u);
	public void error(long userId, Exception e);
}
