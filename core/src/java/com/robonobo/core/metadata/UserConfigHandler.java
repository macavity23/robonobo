package com.robonobo.core.metadata;

import com.robonobo.core.api.model.UserConfig;

public interface UserConfigHandler {
	public void success(UserConfig uc);
	public void error(long userId, Exception e);
}
