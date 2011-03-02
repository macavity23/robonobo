package com.robonobo.wang.client;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.robonobo.wang.proto.WangProtocol.CoinMsg;

/**
 * Stores coins securely on the filesystem. If there are no public methods
 * currently being executed, the file store is guaranteed to be in a recoverable
 * state.
 */
public class CoinStore {
	private Log log = LogFactory.getLog(getClass());
	private File storageDir;
	private Cipher encryptCipher;
	private Cipher decryptCipher;
	private Map<Integer, List<String>> coinIds = new HashMap<Integer, List<String>>();

	public CoinStore(File storageDir, String password) {
		// Create our encryption ciphers
		try {
			byte[] keyArr = generateKey(password, 16);
			SecretKeySpec sekritKey = new SecretKeySpec(keyArr, "AES");
			encryptCipher = Cipher.getInstance("AES");
			encryptCipher.init(Cipher.ENCRYPT_MODE, sekritKey);
			decryptCipher = Cipher.getInstance("AES");
			decryptCipher.init(Cipher.DECRYPT_MODE, sekritKey);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		// Load coins from dir
		this.storageDir = storageDir;
		if(!storageDir.exists())
			storageDir.mkdirs();
		log.info("CoinStore loading persisted coins from "+storageDir.getAbsolutePath());
		double coinVal = 0;
		int numCoins = 0;
		for (File coinFile : storageDir.listFiles()) {
			try {
				CoinMsg coin = loadCoinFromFile(coinFile.getName());
				if(!coinIds.containsKey(coin.getDenom()))
					coinIds.put(coin.getDenom(), new LinkedList<String>());
				coinIds.get(coin.getDenom()).add(coin.getCoinId().toString());
				coinVal += getDenomValue(coin.getDenom());
				numCoins++;
			} catch (Exception e) {
				log.error("CoinStore encountered error ("+e.getClass().getName()+") loading coin. Skipping.");
			}
		}
		log.info("CoinStore loaded "+numCoins+" coins, value="+coinVal);
	}

	public synchronized int numCoins(int denom) {
		return (coinIds.containsKey(denom)) ? coinIds.get(denom).size() : 0;
	}

	public synchronized CoinMsg getCoin(int denom) throws CoinStoreException {
		if(!coinIds.containsKey(denom) || coinIds.get(denom).size() == 0)
			return null;
		String coinId = coinIds.get(denom).remove(0);
		CoinMsg coin;
		try {
			coin = loadCoinFromFile(coinId);
		} catch (Exception e) {
			throw new CoinStoreException(e);
		}
		new File(storageDir, coinId).delete();
		return coin;
	}
	
	public synchronized void putCoin(CoinMsg coin) throws CoinStoreException {
		if(!coinIds.containsKey(coin.getDenom()))
			coinIds.put(coin.getDenom(), new LinkedList<String>());
		coinIds.get(coin.getDenom()).add(coin.getCoinId().toString());
		try {
			saveCoinToFile(coin);
		} catch (Exception e) {
			throw new CoinStoreException(e);
		}
	}

	private void saveCoinToFile(CoinMsg coin) throws IOException, GeneralSecurityException {
		byte[] encArr = encryptCipher.doFinal(coin.toByteArray());
		File file = new File(storageDir, coin.getCoinId().toString());
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(encArr);
		fos.close();
	}


	private CoinMsg loadCoinFromFile(String coinId) throws IOException, GeneralSecurityException {
		File file = new File(storageDir, coinId);
		FileInputStream fis = new FileInputStream(file);
		byte[] readArr = new byte[1024];
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int numRead;
		while((numRead = fis.read(readArr)) > 0) {
			baos.write(readArr, 0, numRead);
		}
		fis.close();
		byte[] plainArr = decryptCipher.doFinal(baos.toByteArray());
		return CoinMsg.parseFrom(plainArr);
	}

	private byte[] generateKey(String password, int keyLength) {
		MessageDigest m;
		try {
			m = MessageDigest.getInstance("sha-256");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException();
		}
		m.update(password.getBytes());
		byte[] key = new byte[keyLength];
		System.arraycopy(m.digest(), 0, key, 0, keyLength);
		return key;
	}

	private double getDenomValue(Integer denom) {
		return Math.pow(2, denom);
	}
}
