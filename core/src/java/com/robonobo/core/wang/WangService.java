package com.robonobo.core.wang;

import static com.robonobo.common.util.TimeUtil.*;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.doomdark.uuid.UUID;
import org.doomdark.uuid.UUIDGenerator;
import org.hsqldb.lib.tar.RB;

import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.common.exceptions.Errot;
import com.robonobo.core.api.*;
import com.robonobo.core.api.model.*;
import com.robonobo.core.api.proto.CoreApi.Node;
import com.robonobo.core.service.*;
import com.robonobo.wang.WangException;
import com.robonobo.wang.beans.CoinList;
import com.robonobo.wang.client.WangClient;
import com.robonobo.wang.proto.WangProtocol.CoinListMsg;

public class WangService extends AbstractService implements CurrencyClient {
	RobonoboWangConfig config;
	WangClient client;
	Log log = LogFactory.getLog(getClass());
	double cachedBankBalance = 0;
	Date nextCheckBalanceTime = new Date(0);
	boolean clientStarted = false;
	TaskService tasks;
	UserService users;

	public WangService() {
		addHardDependency("core.users");
		addHardDependency("core.tasks");
		addHardDependency("core.http");
	}

	public String getName() {
		return "Wang banking service";
	}

	public String getProvides() {
		return "core.wang";
	}

	@Override
	public void startup() throws Exception {
		tasks = rbnb.getTaskService();
		users = rbnb.getUserService();
		config = (RobonoboWangConfig) rbnb.getConfig("wang");
		File coinStoreDir = new File(rbnb.getHomeDir(), "coins");
		coinStoreDir.mkdirs();
		config.setCoinStoreDir(coinStoreDir.getAbsolutePath());
	}

	private class StartClientTask extends Task {
		public StartClientTask() {
			title = "Starting wang currency client";
		}
		
		@Override
		public void runTask() throws Exception {
			if (client != null)
				client.stop();

			User me = users.getMyUser();
			config.setAccountEmail(me.getEmail());
			config.setAccountPwd(me.getPassword());
			statusText = "Initializing client";
			fireUpdated();
			client = new WangClient(config, rbnb.getHttpService().getClient());
			client.start();
			clientStarted = true;
			completion = 0.5f;
			statusText = "Updating account balance";
			fireUpdated();
			updateBalanceIfNecessary(true);
			completion = 1f;
			statusText = "Done.";
			fireUpdated();
			fireBalanceUpdated();
		}
	}
	
	@Override
	public void shutdown() throws Exception {
		clientStarted = false;
		if (client != null)
			client.stop();
	}

	public boolean isReady() {
		return clientStarted;
	}
	
	public String getAcceptPaymentMethods() {
		// TODO Add in escrow nodes ("escrow:<nodeid>,escrow:<nodeid>") here
		return "upfront";
	}
	
	public Node[] getTrustedEscrowNodes() {
		return new Node[0];
	}
	
	public String currencyUrl() {
		return config.getCurrencyUrl();
	}

	public double getBidIncrement() {
		return Math.pow(2,config.getBidIncrement());
	}

	public double getMinBid() {
		return Math.pow(2,config.getMinBid());
	}

	public int getMinTopRate() {
		return config.getMinTopRate();
	}

	public double getOpeningBalance() {
		return Math.pow(2, config.getOpeningBalance());
	}

	public double getMaxBid(StreamVelocity sv) {
		switch (sv) {
		case LowestCost:
			return Math.pow(2, config.getLowestCostMaxBid());
		case MaxRate:
			return Math.pow(2, config.getMaxRateMaxBid());
		default:
			throw new Errot();
		}
	}

	public double getBankBalance() throws CurrencyException {
		return getBankBalance(false);
	}

	public double getBankBalance(boolean forceUpdate) throws CurrencyException {
		updateBalanceIfNecessary(forceUpdate);
		return cachedBankBalance;
	}

	public double getOnHandBalance() throws CurrencyException {
		return client.getOnHandBalance();
	}
	
	public double depositToken(byte[] token, String narration) throws CurrencyException {
		try {
			CoinListMsg coins = CoinListMsg.parseFrom(token);
			client.putCoins(coins);
			double result = CoinList.totalValue(coins);
			synchronized (this) {
				cachedBankBalance += result;
			}
			fireAccountActivity(result, narration);
			return result;
		} catch (WangException e) {
			throw new CurrencyException(e);
		} catch (IOException e) {
			throw new CurrencyException(e);
		}
	}

	public byte[] withdrawToken(double value, String narration) throws CurrencyException {
		try {
			CoinListMsg coins = client.getCoins(value);
			synchronized (this) {
				cachedBankBalance -= CoinList.totalValue(coins);
			}
			fireAccountActivity(0 - value, narration);
			return coins.toByteArray();
		} catch (WangException e) {
			throw new CurrencyException(e);
		}
	}

	public void loggedIn() {
		rbnb.getTaskService().runTask(new StartClientTask());
	}
	
	private void fireBalanceUpdated() {
		rbnb.getEventService().fireWangBalanceChanged(cachedBankBalance+client.getOnHandBalance());
	}
	
	private void fireAccountActivity(final double creditValue, final String narration) {
		rbnb.getExecutor().execute(new CatchingRunnable() {
			public void doRun() throws Exception {
				updateBalanceIfNecessary(false);
				rbnb.getEventService().fireWangAccountActivity(creditValue, narration);
				fireBalanceUpdated();
			}
		});		
	}
	
	private void updateBalanceIfNecessary(boolean forceUpdate) {
		synchronized (this) {
			if (forceUpdate || now().after(nextCheckBalanceTime)) {
				try {
					cachedBankBalance = client.getAccurateBankBalance();
				} catch (WangException e) {
					log.error("Error updating bank balance", e);
				}
				nextCheckBalanceTime = timeInFuture(config.getBankBalanceCacheTime() * 1000);
			}
		}
	}
}
