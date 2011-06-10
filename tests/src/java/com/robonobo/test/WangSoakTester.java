package com.robonobo.test;

import java.io.File;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.PropertyConfigurator;

import com.robonobo.wang.client.WangClient;
import com.robonobo.wang.client.WangConfig;
import com.robonobo.wang.proto.WangProtocol.CoinListMsg;
import com.robonobo.wang.proto.WangProtocol.CoinMsg;

/**
 * Hits the wang server with many repeated withdrawal and deposit requests
 * 
 * @author macavity
 * 
 */
public class WangSoakTester {
	static final double MIN_TRANS_AMT = 0.0002d;
	static final double MAX_TRANS_AMT = 0.5d;
	static final int TRANS_PER_CLIENT = 4;

	int numThreads;
	String bankUrl;
	String username;
	String password;
	String floatLvl = "-6:2,-9:2,-11:2";
	Log log;
	Random rand;

	private static void printUsage() {
		System.err.println("Usage: WangSoakTester <bank url> <email> <pwd> <num threads>");
	}

	public static void main(String[] args) throws Exception {
		if (args.length != 4) {
			printUsage();
			return;
		}
		String bankUrl = args[0];
		String username = args[1];
		String pwd = args[2];
		int numThreads = Integer.parseInt(args[3]);
		WangSoakTester tester = new WangSoakTester(numThreads, bankUrl, username, pwd);
		tester.run();
	}

	public WangSoakTester(int numThreads, String bankUrl, String username, String password) throws Exception {
		this.numThreads = numThreads;
		PropertyConfigurator.configureAndWatch("log4j.properties");
		log = LogFactory.getLog(getClass());
		this.bankUrl = bankUrl;
		this.username = username;
		this.password = password;
		rand = new Random();
	}

	private void run() {
		log.info("Soak test starting");
		for (int i = 0; i < numThreads; i++) {
			Thread t = new SoakTestThread(i);
			t.start();
		}
	}

	private double getTransAmt() {
		double range = MAX_TRANS_AMT - MIN_TRANS_AMT;
		return MIN_TRANS_AMT + ((rand.nextInt(101) / 100d) * range);
	}

	class SoakTestThread extends Thread {
		WangConfig cfg;
		int num;

		public SoakTestThread(int num) {
			this.num = num;
			setName("SoakTestThread-" + num);
			cfg = new WangConfig();
			cfg.setBankUrl(bankUrl);
			cfg.setAccountEmail(username);
			cfg.setAccountPwd(password);
			cfg.setFloatLevel(floatLvl);
			File coinStoreDir = new File("/tmp/wangsoak/coins-" + num);
			coinStoreDir.mkdirs();
			cfg.setCoinStoreDir(coinStoreDir.getAbsolutePath());
		}

		@Override
		public void run() {
			try {
				while (true) {
					log.info("Starting new client");
					WangClient client = new WangClient(cfg, new DefaultHttpClient());
					client.start();
					for (int i = 0; i < TRANS_PER_CLIENT; i++) {
						double amt = getTransAmt();
						log.info("Withdrawing " + amt);
						CoinListMsg coins = client.getCoins(amt);
						log.info("Depositing " + amt);
						client.putCoins(coins);
					}
					log.info("Finishing client");
					client.stop();
				}
			} catch (Exception e) {
				log.error("Uncaught exception", e);
			}

		}
	}
}
