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
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;

public class DoubleCoinRequest extends PublicCoinRequest {
	private BigInteger m_biBlindingFactorY;
	private BigInteger m_biBlindingFactorG;
	private UnsignedCoin m_coin = new UnsignedCoin();

	public DoubleCoinRequest() {
	}

	public DoubleCoinRequest(BigInteger coinId, BigInteger blindingY, BigInteger blindingG, BigInteger coinRequest) {
		super(coinRequest);
		m_coin = new UnsignedCoin(coinId);
		m_biBlindingFactorY = blindingY;
		m_biBlindingFactorG = blindingG;
	}

	public DoubleCoinRequest(PublicBank bank) throws NoSuchAlgorithmException {
		BigInteger y;

		m_coin.random(bank);
		y = m_coin.generateCoinNumber(bank);

		// choose b_y
		BigInteger p1 = bank.getPrime().subtract(Util.ONE);
		for (;;) {
			m_biBlindingFactorY = Util.random(1, bank.getPrime().subtract(Util.ONE));
			Util.dumpNumber("by=       ", m_biBlindingFactorY);

			// y has to be an invertible exponent, so ensure it has an inverse
			// mod p-1.
			if (m_biBlindingFactorY.gcd(p1).equals(Util.ONE))
				break;
		}

		// choose b_g
		m_biBlindingFactorG = Util.random(1, bank.getPrime().subtract(Util.ONE));
		Util.dumpNumber("bg=       ", m_biBlindingFactorG);

		// calculate A->B: y^b_y g^b_g
		m_biCoinRequest = bank.getGenerator().modPow(m_biBlindingFactorG, bank.getPrime());
		y = y.modPow(m_biBlindingFactorY, bank.getPrime());
		m_biCoinRequest = m_biCoinRequest.multiply(y).mod(bank.getPrime());
		Util.dumpNumber("A->B=     ", m_biCoinRequest);
	}

	public DoubleCoinRequest(BufferedReader rdr) throws IOException {
		read(rdr);
	}

	public DoubleCoinRequest(String szFile) throws IOException {
		this(Util.newBufferedFileReader(szFile));
	}

	public void write(PrintStream str) {
		super.writePublic(str);
		m_coin.write(str);
		Util.dumpNumber(str, "blindingY=", m_biBlindingFactorY);
		Util.dumpNumber(str, "blindingG=", m_biBlindingFactorG);
	}

	public void read(BufferedReader rdr) throws IOException {
		super.read(rdr);
		m_coin.read(rdr);
		m_biBlindingFactorY = Util.readNumber(rdr, "blindingY=");
		m_biBlindingFactorG = Util.readNumber(rdr, "blindingG=");
	}

	BigInteger unblind(BigInteger biSignedCoin, PublicBank bank) {
		BigInteger z = bank.getPublicKey().modPow(m_biBlindingFactorG, bank.getPrime());
		z = z.modInverse(bank.getPrime());
		z = z.multiply(biSignedCoin);
		z = z.mod(bank.getPrime());

		BigInteger p1 = bank.getPrime().subtract(Util.ONE);
		BigInteger byinv = m_biBlindingFactorY.modInverse(p1);
		z = z.modPow(byinv, bank.getPrime());

		return z;
	}

	public Coin processResponse(PublicBank bank, BigInteger biSignedCoinRequest) {
		BigInteger biCoinSignature = unblind(biSignedCoinRequest, bank);
		Util.dumpNumber("z=        ", biCoinSignature);

		return new Coin(m_coin, biCoinSignature);
	}

	public BigInteger getBlindingFactorY() {
		return m_biBlindingFactorY;
	}

	public void setBlindingFactorY(BigInteger blindingFactorY) {
		m_biBlindingFactorY = blindingFactorY;
	}

	public BigInteger getBlindingFactorG() {
		return m_biBlindingFactorG;
	}

	public void setBlindingFactorG(BigInteger blindingFactorG) {
		m_biBlindingFactorG = blindingFactorG;
	}

	public BigInteger getCoinId() {
		return m_coin.getCoinId();
	}

	public void setCoinId(BigInteger coinId) {
		m_coin = new UnsignedCoin(coinId);
	}
}
