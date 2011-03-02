package com.robonobo.midas.model;

import com.robonobo.core.api.model.Invite;
import com.robonobo.core.api.proto.CoreApi.InviteMsg;

public class MidasInvite extends Invite {
	public MidasInvite() {
	}

	public MidasInvite(InviteMsg msg) {
		super(msg);
	}
	
	
}
