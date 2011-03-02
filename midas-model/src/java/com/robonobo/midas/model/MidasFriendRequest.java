package com.robonobo.midas.model;

import com.robonobo.core.api.model.FriendRequest;
import com.robonobo.core.api.proto.CoreApi.FriendRequestMsg;

public class MidasFriendRequest extends FriendRequest {

	public MidasFriendRequest() {
		super();
	}

	public MidasFriendRequest(FriendRequestMsg msg) {
		super(msg);
	}
	
}
