package com.robonobo.core.api;

import com.robonobo.core.api.model.User;

public interface LoginListener {
	public void loginSucceeded(User me);
	public void loginFailed(String reason);
}
