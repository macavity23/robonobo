package com.robonobo.common.util;

import java.util.Comparator;
/*
 * Robonobo Common Utils
 * Copyright (C) 2008 Will Morton (macavity@well.com) & Ray Hilton (ray@wirestorm.net)

 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/


/*
 * This source code file is Copyright 2004 Ray Hilton / Will Morton. All rights reserved.
 * Unauthorised duplication of this file is expressly forbidden without prior
 * written permission.
 */
public class Modulo implements Comparator<Long> {
	long modulus;

	/**
	 * Modulus can be up to and including Integer.MAX_VALUE+1 (2^32), but no
	 * larger
	 */
	public Modulo(long mod) {
		if (mod < 0)
			throw new IllegalArgumentException(
					"mod must be non-negative");
		modulus = mod;
	}

	public Modulo(int mod) {
		if (mod < 0)
			throw new IllegalArgumentException(
					"mod must be non-negative");
		modulus = (long) mod;
	}

	// LessThan/MoreThan is fun with modulo numbers;
	// If we are doing arithmetic modulo M with a number n,
	// then the numbers (n+1)%M to (n+(M/2))%M are greater than n,
	// and (n-(M/2)+1)%M to (n-1)%M are less than n

	/**
	 * Like (num1 &lt; num2), but using modulo arithmetic
	 */
	public boolean lt(long num1, long num2) {
		if (num1 < 0 || num2 < 0)
			throw new IllegalArgumentException(
					"Both arguments must be non-negative");

		if (num2 >= (modulus / 2)) {
			if ((num1 > (num2 - (modulus / 2))) && (num1 < num2))
				return true;
			else
				return false;
		} else {
			if ((num1 <= (num2 + (modulus / 2))) && (num1 >= num2))
				return false;
			else
				return true;
		}
	}

	/**
	 * Like (num1 &lt; num2), but using modulo arithmetic
	 */
	public boolean lt(int num1, int num2) {
		if (num1 < 0 || num2 < 0)
			throw new IllegalArgumentException(
					"Both arguments must be non-negative");

		return lt((long) num1, (long) num2);
	}

	/**
	 * Like (num1 &lt;= num2), but using modulo arithmetic
	 */
	public boolean lte(long num1, long num2) {
		if (num1 < 0 || num2 < 0)
			throw new IllegalArgumentException(
					"Both arguments must be non-negative");

		return (num1 == num2) || lt(num1, num2);
	}

	/**
	 * Like (num1 &lt;= num2), but using modulo arithmetic
	 */
	public boolean lte(int num1, int num2) {
		if (num1 < 0 || num2 < 0)
			throw new IllegalArgumentException(
					"Both arguments must be non-negative");

		return lte((long) num1, (long) num2);
	}

	/**
	 * Like (num1 &gt; num2), but using modulo arithmetic
	 */
	public boolean gt(long num1, long num2) {
		if (num1 < 0 || num2 < 0)
			throw new IllegalArgumentException(
					"Both arguments must be non-negative");

		return lt(num2, num1);
	}

	/**
	 * Like (num1 &gt; num2), but using modulo arithmetic
	 */
	public boolean gt(int num1, int num2) {

		if (num1 < 0 || num2 < 0)
			throw new IllegalArgumentException(
					"Both arguments must be non-negative");

		return gt((long) num1, (long) num2);
	}

	/**
	 * Like (num1 &gt;= num2), but using modulo arithmetic
	 */
	public boolean gte(long num1, long num2) {
		if (num1 < 0 || num2 < 0)
			throw new IllegalArgumentException(
					"Both arguments must be non-negative");

		return (num1 == num2) || lt(num2, num1);
	}

	/**
	 * Like (num1 &gt;= num2), but using modulo arithmetic
	 */
	public boolean gte(int num1, int num2) {
		if (num1 < 0 || num2 < 0)
			throw new IllegalArgumentException(
					"Both arguments must be non-negative");

		return gte((long) num1, (long) num2);
	}

	/**
	 * Like (num1 + num2), but using modulo arithmetic
	 */
	public long add(long num1, long num2) {
		if (num1 < 0 || num2 < 0)
			throw new IllegalArgumentException(
					"Both arguments must be non-negative (" + num1 + ", "
							+ num2 + ")");

		// We have to cast here in case it overflows internally,
		// eg when modulus == Uint32.MaxValue such as in TCP/S-EON
		return (long) ((num1 + num2) % modulus);
	}

	/**
	 * Like (num1 + num2), but using modulo arithmetic
	 */
	public int add(int num1, int num2)  {
		return (int) add((long) num1, (long) num2);
	}

	/**
	 * Like (num - toSubtract), but using modulo arithmetic
	 */
	public long sub(long num, long toSubtract) {
		if (num < 0)
			throw new IllegalArgumentException("Argument must be non-negative");

		if (num >= toSubtract) {
			return (num - toSubtract);
		} else {
			return (long) (modulus - (toSubtract - num));
		}
	}

	/**
	* Like (num - toSubtract), but using modulo arithmetic
	*/
	public int sub(int num, int toSubtract) {
		return (int) sub((long) num, (long) toSubtract);
	}

	/**
	 * Compares the two values using modulo arithmetic
	 */
	public int compare(Long l1, Long l2) {
		if(lt(l1, l2))
			return -1;
		if(gt(l1, l2))
			return 1;
		return 0;
	}
	
	public long getModulus() {
		return modulus;
	}
}