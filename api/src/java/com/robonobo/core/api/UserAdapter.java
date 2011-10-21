package com.robonobo.core.api;

import com.robonobo.core.api.model.User;
import com.robonobo.core.api.model.UserConfig;

public class UserAdapter implements UserListener {
	@Override
	public void userChanged(User u) {
	}

	@Override
	public void userConfigChanged(UserConfig cfg) {
	}
	
	@Override
	public void allUsersAndPlaylistsLoaded() {
	}
}
