package com.robonobo.common.util;

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

/**
 * A PRNG using the Mersenne Twister algorithm. See
 * http://www.qbrundage.com/michaelb/pubs/essays/random_number_generation
 */
public class MTRandom {
	private int mt_index;
	private int[] mt_buffer = new int[624];

	private MTRandom(java.util.Random r) {
		for(int i = 0; i < 624; i++)
			mt_buffer[i] = r.nextInt();
		mt_index = 0;
	}

	MTRandom() {
		this(new java.util.Random());
	}

	MTRandom(long seed) {
		this(new java.util.Random(seed));
	}

	public int nextInt() {
		if(mt_index == 624) {
			mt_index = 0;
			int i = 0;
			int s;
			for(; i < 624 - 397; i++) {
				s = (mt_buffer[i] & 0x80000000) | (mt_buffer[i + 1] & 0x7FFFFFFF);
				mt_buffer[i] = mt_buffer[i + 397] ^ (s >> 1) ^ ((s & 1) * 0x9908B0DF);
			}

			for(; i < 623; i++) {
				s = (mt_buffer[i] & 0x80000000) | (mt_buffer[i + 1] & 0x7FFFFFFF);
				mt_buffer[i] = mt_buffer[i - (624 - 397)] ^ (s >> 1) ^ ((s & 1) * 0x9908B0DF);
			}

			s = (mt_buffer[623] & 0x80000000) | (mt_buffer[0] & 0x7FFFFFFF);
			mt_buffer[623] = mt_buffer[396] ^ (s >> 1) ^ ((s & 1) * 0x9908B0DF);
		}

		return mt_buffer[mt_index++];
	}

	/**
	 * Returns a random value between 0 (inclusive) and n (exclusive)
	 */
	public int nextInt(int n) {
		return nextInt() % n;
	}
}
