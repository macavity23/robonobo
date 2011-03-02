/* ====================================================================
 * Copyright (c) 1999, 2000 Ben Laurie.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer. 
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. All advertising materials mentioning features or use of this
 *    software must display the following acknowledgment:
 *    "This product includes software developed by Ben Laurie
 *    for use in the Lucre project."
 *
 * 4. The name "Lucre" must not be used to
 *    endorse or promote products derived from this software without
 *    prior written permission.
 *
 * 5. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by Ben Laurie
 *    for use in the Lucre project."
 *
 * THIS SOFTWARE IS PROVIDED BY BEN LAURIE ``AS IS'' AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL BEN LAURIE OR
 * HIS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 *
 * For more information on Lucre see http://anoncvs.aldigital.co.uk/lucre/.
 *
 */

package uk.co.aldigital.ben.lucre;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class UnsignedCoin {
	private BigInteger m_biCoinID;

	public UnsignedCoin() {
	}

	public UnsignedCoin(BigInteger coinId) {
		m_biCoinID = coinId;
	}

	public void random(PublicBank bank) throws NoSuchAlgorithmException {
		for (;;) {
			m_biCoinID = new BigInteger(bank.getCoinLength() * 8, Util.randomGenerator());
			BigInteger y = generateCoinNumber(bank);
			if (y.compareTo(bank.getPrime()) < 0 && bank.checkGroupMembership())
				break;
		}
	}

	public void set(BigInteger biCoinID) {
		m_biCoinID = biCoinID;
	}

	public BigInteger id() {
		return m_biCoinID;
	}

	BigInteger generateCoinNumber(PublicBank bank) throws NoSuchAlgorithmException {
		int nCoinLength = (m_biCoinID.bitLength() + 7) / 8;
		int nDigestIterations = (bank.getPrimeLength() - nCoinLength) / PublicBank.DIGEST_LENGTH;
		int n;

		// 1 in 256 will be 1 byte shorter...
		if (nCoinLength > bank.getCoinLength())
			return null;

		byte xplusd[] = new byte[bank.getPrimeLength()];
		for (n = 0; n < bank.getCoinLength() - nCoinLength; ++n)
			xplusd[n] = 0;

		byte coin[] = m_biCoinID.toByteArray();
		Util.byteCopy(xplusd, n, coin, 0, nCoinLength);
		nCoinLength += n;

		// Util.addCrypto();
		MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
		byte nb[] = new byte[2];
		Util.hexDump("coin=", xplusd, nCoinLength);
		for (n = 0; n < nDigestIterations; ++n) {
			sha1.update(xplusd, 0, nCoinLength);
			nb[0] = (byte) (n + 1);
			nb[1] = (byte) ((n + 1) / 256);
			sha1.update(nb, 0, 2);
			Util.byteCopy(xplusd, nCoinLength + PublicBank.DIGEST_LENGTH * n, sha1.digest(), 0, PublicBank.DIGEST_LENGTH);
		}

		// HexDump("x|hash(x)=",xplusd,
		// nCoinLength+nDigestIterations*PublicBank.DIGEST_LENGTH);

		BigInteger bi = new BigInteger(xplusd);
//		Util.dumpNumber(System.out, "y=        ", bi);

		return bi;
	}

	public void read(BufferedReader reader) throws IOException {
		m_biCoinID = Util.readNumber(reader, "id=");
	}

	public void write(PrintStream str) {
		Util.dumpNumber(str, "id=", m_biCoinID);
	}

	public static void main(String args[]) {
		try {
			UnsignedCoin coin = new UnsignedCoin();

			PublicBank bank = new PublicBank(new BufferedReader(new FileReader(args[0])));
			coin.random(bank);
			coin.generateCoinNumber(bank);
		} catch (Exception e) {
			System.err.println("Failed: " + e.toString());
		}
	}

	public BigInteger getCoinId() {
		return m_biCoinID;
	}

	public void setCoinId(BigInteger coinID) {
		m_biCoinID = coinID;
	}

}
