package com.robonobo.remote.service;

import java.util.ArrayList;
import java.util.List;

/**
 * The client end of a remote wang service - server end is RemoteWangService in the wang-server project
 * @author macavity
 *
 */
public class RemoteWangFacade extends JbossRemotingFacade implements WangService {
	public RemoteWangFacade(String url, String secret) throws Exception {
		super(url, "wang", secret);
	}
	
	public double getBalance(String email, String passwd) {
		List<String> extraArgs = new ArrayList<String>(1);
		extraArgs.add(passwd);
		Object retVal = invoke("getBalance", email, extraArgs);
		if(retVal == null)
			return 0;
		return (Double) retVal;
	}

	public void changePassword(String email, String oldPasswd, String newPasswd) {
		List<String> extraArgs = new ArrayList<String>(2);
		extraArgs.add(oldPasswd);
		extraArgs.add(newPasswd);
		invoke("changePassword", email, extraArgs);
	}
	
	public void createUser(String email, String friendlyName, String passwd) {
		List<String> extraArgs = new ArrayList<String>(2);
		extraArgs.add(friendlyName);
		extraArgs.add(passwd);
		invoke("createUser", email, extraArgs);
	}
	
	@Override
	public void deleteUser(String email) {
		invoke("deleteUser", email, null);
	}
	
	public void topUpBalance(String email, double amount) {
		List<String> extraArgs = new ArrayList<String>(1);
		extraArgs.add(Double.toString(amount));
		invoke("topUpBalance", email, extraArgs);
	}
	
	public Long countUsers() {
		return (Long) invoke("countUsers", null, null);
	}
}
