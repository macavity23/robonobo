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
import java.io.PrintStream;
import java.io.IOException;
import java.math.BigInteger;

public class Coin extends UnsignedCoin {
    private BigInteger m_biSignature;

    public Coin() {
	}
    
    public Coin(BigInteger coinId, BigInteger signature) {
    	super(coinId);
    	m_biSignature = signature;
    }
    
    public Coin(BufferedReader reader)
      throws IOException {
	read(reader);
    }
    public Coin(String szFile)
      throws IOException {
	this(Util.newBufferedFileReader(szFile));
    }
    public Coin(UnsignedCoin coin,BigInteger biCoinSignature) {
	set(coin,biCoinSignature);
    }
    
    
    public void read(BufferedReader reader)
      throws IOException {
	super.read(reader);
	m_biSignature=Util.readNumber(reader,"signature=");
    }
    public void write(PrintStream str) {
	super.write(str);
	Util.dumpNumber(str,"signature=",m_biSignature);
    }
    public void set(BigInteger biCoinID,BigInteger biCoinSignature) {
	m_biSignature=biCoinSignature;
	set(biCoinID);
    }
    public void set(UnsignedCoin coin,BigInteger biCoinSignature) {
	set(coin.id(),biCoinSignature);
    }
    public BigInteger getSignature() {
	return m_biSignature;
    }
    public void setSignature(BigInteger signature) {
		m_biSignature = signature;
	}
}

