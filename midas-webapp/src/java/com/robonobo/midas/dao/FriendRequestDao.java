package com.robonobo.midas.dao;

import java.util.List;

import com.robonobo.midas.model.MidasFriendRequest;

public interface FriendRequestDao {

	public abstract MidasFriendRequest retrieveByUsers(long requestorId, long requesteeId);

	public abstract List<MidasFriendRequest> retrieveByRequestee(long requesteeId);

	public abstract MidasFriendRequest retrieveByRequestCode(String requestCode);

	public abstract void save(MidasFriendRequest req);

	public abstract void delete(MidasFriendRequest req);

}