package com.robonobo.core.api;

import com.robonobo.core.api.proto.CoreApi.Node;


public interface CurrencyClient {
	/** This defines the currency */
	public String currencyUrl();

	public double getBankBalance() throws CurrencyException;

	public double getOnHandBalance() throws CurrencyException;
	
	public byte[] withdrawToken(double value, String narration) throws CurrencyException;

	/** Returns the value of the token */
	public double depositToken(byte[] token, String narration) throws CurrencyException;

	/**
	 * For a stream with the given velocity, what's the maximum we're willing to
	 * pay (per megabyte)
	 */
	public double getMaxBid(StreamVelocity sv);

	/**
	 * The minimum bid we'll accept
	 */
	public double getMinBid();

	/**
	 * The minimum that an overbid must raise by
	 */
	public double getBidIncrement();

	/** How much we send them to start with */
	public double getOpeningBalance();

	/**
	 * The minimum rate (bytes/sec) that the top bidder will be charged for, to
	 * prevent them DoSing everyone else
	 */
	public int getMinTopRate();

	/**
	 * Returns true if we are ready to go. You must check that this is true
	 * before calling getToken() or depositToken() for the first time.
	 */
	public boolean isReady();
	
	/**
	 * Comma-sep list of payment methods we accept
	 */
	public String getAcceptPaymentMethods();
	
	public Node[] getTrustedEscrowNodes();
}
