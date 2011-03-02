package com.robonobo.midas.model;

import com.robonobo.core.api.model.User;
import com.robonobo.core.api.proto.CoreApi.UserMsg;

public class MidasUser extends User {
	boolean verified = false;

	public MidasUser() {
	}

	public MidasUser(User u) {
		super(u);
		if (u instanceof MidasUser)
			setVerified(((MidasUser) u).isVerified());
	}

	public MidasUser(UserMsg msg) {
		super(msg);
	}

	@Override
	public void copyFrom(User copyUser) {
		super.copyFrom(copyUser);
		if (copyUser instanceof MidasUser)
			setVerified(((MidasUser) copyUser).isVerified());
	}

	public boolean isVerified() {
		return verified;
	}

	public void setVerified(boolean verified) {
		this.verified = verified;
	}
}
