package com.robonobo.wang.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;

import com.robonobo.common.concurrent.SafetyNet;
import com.robonobo.common.http.PreemptiveHttpClient;
import com.robonobo.common.util.ExceptionEvent;
import com.robonobo.common.util.ExceptionListener;
import com.robonobo.wang.proto.WangProtocol.CoinListMsg;

public class WangTest {
	public static void main(String[] args) throws Exception {
		SafetyNet.addListener(new ExceptionListener() {
			public void onException(ExceptionEvent e) {
				e.getException().printStackTrace();
			}
		});
		PropertyConfigurator.configureAndWatch("../wang-server/src/java/log4j.properties");
		Log log = LogFactory.getLog(WangTest.class);
		WangConfig cfg = new WangConfig();
		cfg.setBankUrl("http://localhost:8080/wang-server");
		cfg.setAccountEmail("macavity@well.com");
		cfg.setAccountPwd("foo");
		WangClient client = new WangClient(cfg, new PreemptiveHttpClient());
		log.info("Starting client");
		client.start();
		log.info("My balance: " + client.getAccurateBankBalance() + " (in bank), " + client.getOnHandBalance() + " (in hand)");
		log.info("Withdrawing w50");
		CoinListMsg coins = client.getCoins(50);
		log.info("My balance: " + client.getAccurateBankBalance() + " (in bank), " + client.getOnHandBalance() + " (in hand)");
		log.info("Depositing coins");
		client.putCoins(coins);
		log.info("My balance: " + client.getAccurateBankBalance() + " (in bank), " + client.getOnHandBalance() + " (in hand)");
		client.stop();
	}
}
