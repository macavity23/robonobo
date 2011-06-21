package com.robonobo.wang.client;

import java.io.File;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.impl.client.DefaultHttpClient;

import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.common.exceptions.Errot;
import com.robonobo.common.http.PreemptiveHttpClient;
import com.robonobo.common.util.TextUtil;
import com.robonobo.wang.*;
import com.robonobo.wang.beans.*;
import com.robonobo.wang.proto.WangProtocol.BlindedCoinListMsg;
import com.robonobo.wang.proto.WangProtocol.BlindedCoinListMsg.Status;
import com.robonobo.wang.proto.WangProtocol.CoinListMsg;
import com.robonobo.wang.proto.WangProtocol.CoinMsg;
import com.robonobo.wang.proto.WangProtocol.CoinRequestListMsg;
import com.robonobo.wang.proto.WangProtocol.DenominationListMsg;
import com.robonobo.wang.proto.WangProtocol.DenominationMsg;
import com.robonobo.wang.proto.WangProtocol.DepositStatusMsg;

public class WangClient {
	private Log log = LogFactory.getLog(getClass());
	private WangConfig config;
	private BankFacade bank;
	private LucreFacade lucre;
	/**
	 * Take note of the smallest denomination, as we round up any withdrawal requests
	 */
	private int smallestDenom;
	/** When we do below this value of local coins, request more */
	private double thresholdFloatLevel;
	/** What coins we should request <denom, num to req> */
	private Map<Integer, Integer> floatCoinsToReq;
	/**
	 * Withdraws from the bank as necessary to maintain our float of coinage
	 */
	private Thread floatUpdaterThread;
	// This map is in descending key order
	private SortedMap<Integer, DenominationPublic> denoms = new TreeMap<Integer, DenominationPublic>(
			new DescendingIntComp());
	private CoinStore coinStore;

	public WangClient(WangConfig config, PreemptiveHttpClient client) {
		this.config = config;
		bank = new BankFacade(config.getBankUrl(), config.getAccountEmail(), config.getAccountPwd(), client);
		lucre = new LucreFacade();
	}

	public void start() throws WangException {
		File coinStoreDir = new File(config.getCoinStoreDir());
		log.info("Wang client starting with bank url "+config.getBankUrl()+" and coin store dir "+coinStoreDir.getAbsolutePath());
		coinStore = new CoinStore(coinStoreDir, config.getAccountPwd());
		// Fetch our set of denominations
		DenominationListMsg list = bank.getDenominations();
		smallestDenom = Integer.MAX_VALUE;
		for (DenominationMsg denomMsg : list.getDenominationList()) {
			denoms.put(denomMsg.getDenom(), new DenominationPublic(denomMsg));
			if (denomMsg.getDenom() < smallestDenom)
				smallestDenom = denomMsg.getDenom();
		}
		log.info("Got coin denominations: " + TextUtil.commaSepList(denoms.keySet()));
		// Withdraw initial float
		// TODO What if our balance is less than this float value? Check
		double totalFloatVal = 0;
		floatCoinsToReq = new HashMap<Integer, Integer>();
		String floatStr = config.getFloatLevel();
		String[] outerToks = floatStr.split(",");
		for (String outerTok : outerToks) {
			String[] innerToks = outerTok.split(":");
			if (innerToks.length != 2)
				throw new WangConfigException("Invalid float string: " + floatStr);
			int denom = Integer.parseInt(innerToks[0]);
			int numToGet = Integer.parseInt(innerToks[1]);
			floatCoinsToReq.put(denom, numToGet);
			totalFloatVal += numToGet * getDenomValue(denom);
		}
		// Withdraw more when we get to half this much
		thresholdFloatLevel = totalFloatVal / 2d;
		withdrawCoins(floatCoinsToReq);
	}

	public void stop() throws WangException {
		log.info("Wang client stopping");
		waitForFloatUpdater();
		// Return our coins to the bank
		CoinListMsg.Builder clBldr = CoinListMsg.newBuilder();
		for (int denom : denoms.keySet()) {
			int numCoins = coinStore.numCoins(denom);
			for (int i = 0; i < numCoins; i++) {
				clBldr.addCoin(coinStore.getCoin(denom));
			}
		}
		CoinListMsg cl = clBldr.build();
		try {
			putCoins(cl);
		} catch (WangException e) {
			log.error("Error returning on-hand coins to the bank - persisting in local store");
			for (CoinMsg coin : cl.getCoinList()) {
				coinStore.putCoin(coin);
			}
		}
		log.info("Wang client stopped");
	}

	/**
	 * Returns a list of coins that are at least as much as the total value, and as close as possible to it (might be
	 * slightly over due to the requested amount not being representable in our denoms). Once these coins are withdrawn,
	 * the withdrawer has responsibility for them.
	 */
	public CoinListMsg getCoins(double totalValue) throws WangException {
		double valSoFar = 0;
		CoinListMsg.Builder clBldr = CoinListMsg.newBuilder();
		// First, get any coins we have in our store
		denomLoop: for (Integer denom : denoms.keySet()) {
			while (valSoFar < totalValue && coinStore.numCoins(denom) > 0) {
				double coinVal = getDenomValue(denom);
				if (totalValue - valSoFar >= coinVal) {
					clBldr.addCoin(coinStore.getCoin(denom));
					valSoFar += coinVal;
				} else {
					continue denomLoop;
				}
			}
		}
		// If we have a shortfall, check to see if adding a smallest-denom coin
		// will make it up
		if (valSoFar < totalValue && valSoFar + getDenomValue(smallestDenom) >= totalValue
				&& coinStore.numCoins(smallestDenom) > 0) {
			clBldr.addCoin(coinStore.getCoin(smallestDenom));
			valSoFar += getDenomValue(smallestDenom);
		}
		try {
			if (valSoFar >= totalValue)
				return clBldr.build();
			else {
				// We don't have enough locally - withdraw more
				double amtToWithdraw = totalValue - valSoFar;
				// Decide how many of each denom we want to withdraw
				Map<Integer, Integer> toWithdraw = new HashMap<Integer, Integer>();
				// Biggest denomination first
				for (int denom : denoms.keySet()) {
					int numThisDenom = (int) (amtToWithdraw / getDenomValue(denom));
					if (numThisDenom > 0) {
						toWithdraw.put(denom, numThisDenom);
						amtToWithdraw -= getDenomValue(denom) * numThisDenom;
					}
				}
				// We might need to add a smallest-denom coin to make up
				// rounding
				if (amtToWithdraw > 0) {
					if (amtToWithdraw > getDenomValue(smallestDenom))
						throw new Errot();
					int numSmallest = toWithdraw.containsKey(smallestDenom) ? toWithdraw.get(smallestDenom) : 0;
					toWithdraw.put(smallestDenom, numSmallest + 1);
					amtToWithdraw -= getDenomValue(smallestDenom);
				}
				try {
					withdrawCoins(toWithdraw);
				} catch (WangException e) {
					// Oopsie... return our coins to our local store before
					// re-throwing exception
					for (CoinMsg coin : clBldr.getCoinList()) {
						coinStore.putCoin(coin);
					}
					throw e;
				}
				clBldr.addAllCoin(getCoins(totalValue - valSoFar).getCoinList());
				return clBldr.build();
			}
		} finally {
			updateFloat();
		}
	}

	public void putCoins(CoinListMsg coins) throws WangException {
		DepositStatusMsg status = bank.depositCoins(coins);
		if (status.getStatus() != DepositStatusMsg.Status.OK)
			throw new BadCoinException();
		if (log.isInfoEnabled()) {
			Map<Integer, Integer> cMap = new HashMap<Integer, Integer>();
			for (CoinMsg coin : coins.getCoinList()) {
				int d = coin.getDenom();
				if (cMap.containsKey(d))
					cMap.put(d, cMap.get(d) + 1);
				else
					cMap.put(d, 1);
			}
			StringBuffer sb = new StringBuffer("Deposited coins: ");
			boolean first = true;
			for (Integer d : cMap.keySet()) {
				if (first)
					first = false;
				else
					sb.append(", ");
				sb.append(cMap.get(d)).append("x").append(d);
			}
			log.info(sb);
		}
	}

	/**
	 * Gets the total value of the coins we have on hand
	 */
	public double getOnHandBalance() {
		double balance = 0;
		for (Integer denom : denoms.keySet()) {
			int numCoins = coinStore.numCoins(denom);
			double denomValue = getDenomValue(denom);
			balance += (denomValue * numCoins);
		}
		return balance;
	}

	/**
	 * Gets the balance of this account stored in the bank
	 */
	public double getBankBalance() throws WangException {
		return bank.getBalance();
	}

	/**
	 * Gets the balance from the bank - waits for any pending withdrawal requests to complete before returning
	 */
	public double getAccurateBankBalance() throws WangException {
		waitForFloatUpdater();
		return bank.getBalance();
	}

	private double getDenomValue(Integer denom) {
		return Math.pow(2, denom);
	}

	/**
	 * 
	 * @param numCoins
	 *            map<denomination, num to withdraw>
	 */
	private void withdrawCoins(Map<Integer, Integer> numCoins) throws WangException {
		List<CoinRequestPrivate> privReqs = new ArrayList<CoinRequestPrivate>();
		CoinRequestListMsg.Builder crlBldr = CoinRequestListMsg.newBuilder();
		// Create coin requests
		for (Integer denomExp : numCoins.keySet()) {
			DenominationPublic denom = denoms.get(denomExp);
			int numToGet = numCoins.get(denomExp);
			for (int i = 0; i < numToGet; i++) {
				CoinRequestPrivate privReq = lucre.createCoinRequest(denom);
				privReqs.add(privReq);
				crlBldr.addCoinRequest(new CoinRequestPublic(privReq).toMsg());
			}
		}
		// Send coin requests to bank, get back coin signatures
		CoinRequestListMsg crl = crlBldr.build();
		BlindedCoinListMsg blCoins = bank.getCoins(crl);
		if (blCoins.getStatus() == Status.InsufficientWang)
			throw new InsufficientWangException();
		if (blCoins.getCoinCount() != crl.getCoinRequestCount())
			throw new WangServerException("Bank returned incorrect number of coins!");
		// Unblind signatures to get coins
		List<Coin> coins = new ArrayList<Coin>();
		for (int i = 0; i < crl.getCoinRequestCount(); i++) {
			CoinRequestPrivate privReq = privReqs.get(i);
			BlindedCoin blCoin = new BlindedCoin(blCoins.getCoin(i));
			DenominationPublic denom = denoms.get(privReq.getDenom());
			Coin coin = lucre.unblindCoin(denom, blCoin, privReq);
			coins.add(coin);
		}
		// Store coins locally
		for (Coin coin : coins) {
			coinStore.putCoin(coin.toMsg());
		}
		if (log.isInfoEnabled()) {
			Map<Integer, Integer> cMap = new HashMap<Integer, Integer>();
			for (Coin coin : coins) {
				int d = coin.getDenom();
				if (cMap.containsKey(d))
					cMap.put(d, cMap.get(d) + 1);
				else
					cMap.put(d, 1);
			}
			StringBuffer sb = new StringBuffer("Withdrew coins: ");
			boolean first = true;
			for (Integer d : cMap.keySet()) {
				if (first)
					first = false;
				else
					sb.append(", ");
				sb.append(cMap.get(d)).append("x").append(d);
			}
			log.info(sb);
		}
	}

	private synchronized void updateFloat() {
		if (getOnHandBalance() > thresholdFloatLevel)
			return;
		if (floatUpdaterThread != null)
			return;
		floatUpdaterThread = new Thread(new CatchingRunnable() {
			public void doRun() throws Exception {
				withdrawCoins(floatCoinsToReq);
				floatUpdaterThread = null;
			}
		});
		floatUpdaterThread.setName("Wang Float Updater");
		floatUpdaterThread.start();
	}

	private void waitForFloatUpdater() {
		Thread ft = floatUpdaterThread;
		if (ft != null) {
			try {
				ft.join(10000);
			} catch (InterruptedException ignore) {
			}
		}
	}

	/**
	 * Reverses the sort order (to become descending)
	 */
	private class DescendingIntComp implements Comparator<Integer> {
		public int compare(Integer i1, Integer i2) {
			return 0 - i1.compareTo(i2);
		}
	}
}
