package com.robonobo.core.wang;

import com.robonobo.wang.client.WangConfig;

public class RobonoboWangConfig extends WangConfig {
	private String currencyUrl = "http://wang.mu";

	/** These are all as powers of 2 */
	private int bidIncrement = -11;
	private int minBid = -11;
	private int openingBalance = -6;
	private int lowestCostMaxBid = -9;
	private int maxRateMaxBid = -6;

	/** Bytes per sec */
	private int minTopRate = 1024 * 5;

	// The below all extend wangconfig
	private String accountEmail;
	private String accountPwd;
	private String bankUrl = "http://wang.robonobo.com";
	/**
	 * The number of coins to keep on hand: <coin
	 * exponent>:<number>[,<exponent>:<num>...]
	 */
	private String floatLevel = "-6:4,-9:4,-11:8";
	/** If not set, this will be set to the dir 'coins' in the robo home dir */
	private String coinStoreDir = "";
	/**
	 * The time to cache the bank balance (secs) - leave this at a large value
	 * unless you're using multiple nodes with the same account and you
	 * desperately need up-to-date figures
	 */
	private int bankBalanceCacheTime = 60 * 60 * 24;

	public String getCurrencyUrl() {
		return currencyUrl;
	}

	public void setCurrencyUrl(String currencyUrl) {
		this.currencyUrl = currencyUrl;
	}

	public int getBidIncrement() {
		return bidIncrement;
	}

	public void setBidIncrement(int bidIncrement) {
		this.bidIncrement = bidIncrement;
	}

	public int getMinBid() {
		return minBid;
	}

	public void setMinBid(int minBid) {
		this.minBid = minBid;
	}

	public int getOpeningBalance() {
		return openingBalance;
	}

	public void setOpeningBalance(int openingBalance) {
		this.openingBalance = openingBalance;
	}

	public int getLowestCostMaxBid() {
		return lowestCostMaxBid;
	}

	public void setLowestCostMaxBid(int lowestCostMaxBid) {
		this.lowestCostMaxBid = lowestCostMaxBid;
	}

	public int getMaxRateMaxBid() {
		return maxRateMaxBid;
	}

	public void setMaxRateMaxBid(int maxRateMaxBid) {
		this.maxRateMaxBid = maxRateMaxBid;
	}

	@Override
	public String getAccountEmail() {
		return accountEmail;
	}

	@Override
	public void setAccountEmail(String accountEmail) {
		this.accountEmail = accountEmail;
	}

	@Override
	public String getAccountPwd() {
		return accountPwd;
	}

	@Override
	public void setAccountPwd(String accountPwd) {
		this.accountPwd = accountPwd;
	}

	@Override
	public String getBankUrl() {
		return bankUrl;
	}

	@Override
	public void setBankUrl(String bankUrl) {
		this.bankUrl = bankUrl;
	}

	@Override
	public String getFloatLevel() {
		return floatLevel;
	}

	@Override
	public void setFloatLevel(String floatLevel) {
		this.floatLevel = floatLevel;
	}

	@Override
	public String getCoinStoreDir() {
		return coinStoreDir;
	}

	@Override
	public void setCoinStoreDir(String coinStoreDir) {
		this.coinStoreDir = coinStoreDir;
	}

	public int getMinTopRate() {
		return minTopRate;
	}

	public void setMinTopRate(int minTopRate) {
		this.minTopRate = minTopRate;
	}

	public int getBankBalanceCacheTime() {
		return bankBalanceCacheTime;
	}

	public void setBankBalanceCacheTime(int bankBalanceCacheTime) {
		this.bankBalanceCacheTime = bankBalanceCacheTime;
	}
}
