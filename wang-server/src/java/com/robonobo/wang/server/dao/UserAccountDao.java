package com.robonobo.wang.server.dao;

import com.robonobo.wang.server.UserAccount;

public interface UserAccountDao {

	public abstract void createUserAccount(String friendlyName, String email, String password) throws DAOException;

	public abstract UserAccount getUserAccount(String email) throws DAOException;

	public abstract UserAccount getAndLockUserAccount(String email) throws DAOException;

	public abstract void putUserAccount(UserAccount ua) throws DAOException;

	public abstract Long countUsers() throws DAOException;

	public abstract void deleteUserAccount(String email) throws DAOException;

}