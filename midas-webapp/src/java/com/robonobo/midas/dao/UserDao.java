package com.robonobo.midas.dao;

import java.util.List;

import com.robonobo.midas.model.MidasUser;

public interface UserDao {

	public abstract MidasUser getById(long id);

	public abstract MidasUser getByEmail(String email);

	public abstract List<MidasUser> getAll();

	public abstract MidasUser create(MidasUser user);

	public abstract void save(MidasUser user);

	public abstract void delete(MidasUser user);

	public abstract Long getUserCount();

}