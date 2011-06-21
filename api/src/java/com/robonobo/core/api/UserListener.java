package com.robonobo.core.api;

import com.robonobo.core.api.model.User;
import com.robonobo.core.api.model.UserConfig;

public interface UserListener {
	public void userChanged(User u);

	public void userConfigChanged(UserConfig cfg);
}
