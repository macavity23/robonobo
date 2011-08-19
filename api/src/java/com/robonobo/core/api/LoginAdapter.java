package com.robonobo.core.api;

import com.robonobo.core.api.model.User;

public class LoginAdapter implements LoginListener {
	@Override
	public void loginSucceeded(User me) {
	}

	@Override
	public void loginFailed(String reason) {
	}
}
