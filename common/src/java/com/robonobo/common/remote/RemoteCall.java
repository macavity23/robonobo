package com.robonobo.common.remote;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings("serial")
public class RemoteCall implements Serializable {
	public String secret;
	public String methodName;
	public Object arg;
	public List<? extends Object> extraArgs;

	public RemoteCall(String secret, String methodName, Object arg) {
		this.secret = secret;
		this.methodName = methodName;
		this.arg = arg;
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public Object getArg() {
		return arg;
	}

	public void setArg(Object arg) {
		this.arg = arg;
	}

	public List<? extends Object> getExtraArgs() {
		return extraArgs;
	}

	public void setExtraArgs(List<? extends Object> extraArgs) {
		this.extraArgs = extraArgs;
	}
}
