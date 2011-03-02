package com.robonobo.wang.server.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.BasicConfigurator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.robonobo.wang.client.LucreFacade;
import com.robonobo.wang.server.dao.DenominationDao;

public class CreateDenoms {
	private static final int KEY_LENGTH = 512;

	private static void usage() {
		System.err.println("Usage: CreateDenoms [list of denoms]");
		System.exit(1);
	}

	public static void main(String[] args) throws Exception {
		if (args.length == 0)
			usage();
		// Parse args into denominations
		final int[] denoms = new int[args.length];
		try {
			for (int i = 0; i < args.length; i++) {
				denoms[i] = Integer.parseInt(args[i]);
			}
		} catch (NumberFormatException e) {
			usage();
		}
		// Bring up log4j so we can see what we're doing
		BasicConfigurator.configure();
		final Log log = LogFactory.getLog(CreateDenoms.class);
		// Initialize spring and get our beans
		ApplicationContext appContext = new FileSystemXmlApplicationContext("appContext.xml");
		PlatformTransactionManager ptm = (PlatformTransactionManager) appContext.getBean("transactionManager");
		TransactionTemplate transTemplate = new TransactionTemplate(ptm);
		final DenominationDao denomDao = (DenominationDao) appContext.getBean("denominationDao");
		final LucreFacade lucre = new LucreFacade();
		// Do everything inside a transaction
		transTemplate.execute(new TransactionCallbackWithoutResult() {
			protected void doInTransactionWithoutResult(TransactionStatus ts) {
				try {
					log.info("Deleting all denoms");
					denomDao.deleteAllDenoms();
					for (int i = 0; i < denoms.length; i++) {
						log.info("Creating denom " + denoms[i]);
						denomDao.putDenom(lucre.createDenomination(denoms[i], KEY_LENGTH));
					}
					log.info("Done.");
				} catch (Exception e) {
					// By default, the transactiontemplate only rolls back for RuntimeExceptions, and I can't figure out
					// how to change this...
					throw new RuntimeException(e);
				}
			}
		});

	}
}
