package com.robonobo.remote.service;

public interface WangService {

	public double getBalance(String email, String passwd);

	public void changePassword(String email, String oldPasswd, String newPasswd);
	
	public void topUpBalance(String email, double amount);
	/**
	 * For monitoring - ensures db is ok
	 */
	public Long countUsers();

	public void createUser(String email, String friendlyName, String passwd);
}