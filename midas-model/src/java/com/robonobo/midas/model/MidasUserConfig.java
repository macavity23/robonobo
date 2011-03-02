package com.robonobo.midas.model;

import com.robonobo.core.api.model.UserConfig;
import com.robonobo.core.api.proto.CoreApi.UserConfigMsg;

public class MidasUserConfig extends UserConfig {

	public MidasUserConfig() {
		super();
	}

	public MidasUserConfig(UserConfigMsg msg) {
		super(msg);
	}

}
