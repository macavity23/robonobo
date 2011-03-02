package com.robonobo.midas.dao;

import com.robonobo.midas.model.MidasInvite;

public interface InviteDao {

	public abstract MidasInvite retrieveByEmail(String email);

	public abstract MidasInvite retrieveByInviteCode(String inviteCode);

	public abstract void save(MidasInvite invite);

	public abstract void delete(MidasInvite invite);

}