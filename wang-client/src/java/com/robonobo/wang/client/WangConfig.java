package com.robonobo.wang.client;

public class WangConfig {
	private String accountEmail;
	private String accountPwd;
	private String bankUrl = "http://robonobo.com/wang/";
	/** The number of coins to keep on hand: <coin exponent>:<number>[,<exponent>:<num>...] */
	private String floatLevel = "0:8";
	private String coinStoreDir = "c:/tmp/coinstore";

	public WangConfig() {
	}

	public String getAccountEmail() {
		return accountEmail;
	}

	public void setAccountEmail(String accountEmail) {
		this.accountEmail = accountEmail;
	}

	public String getAccountPwd() {
		return accountPwd;
	}

	public void setAccountPwd(String accountPwd) {
		this.accountPwd = accountPwd;
	}

	public String getBankUrl() {
		return bankUrl;
	}

	public void setBankUrl(String serverUrl) {
		this.bankUrl = serverUrl;
	}

	public String getFloatLevel() {
		return floatLevel;
	}

	public void setFloatLevel(String floatLevel) {
		this.floatLevel = floatLevel;
	}

	public String getCoinStoreDir() {
		return coinStoreDir;
	}

	public void setCoinStoreDir(String coinStoreDir) {
		this.coinStoreDir = coinStoreDir;
	}


}
