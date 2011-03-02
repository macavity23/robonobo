package com.robonobo.common.networksimulation;
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


import java.util.Random;

public class PublicInternetConditions implements Conditions {
	private Random random;

	private float pktLossProb;

	private int pktLatency;

	private int maxUploadSpeed;

	public PublicInternetConditions(int seed) {
		random = new Random(seed);

		int n = random.nextInt(100) + 1; // 1-100
		// Packet Loss Prob = (1.2^n) / 800000000
		pktLossProb = (float) (Math.pow(1.2, n) / 800000000.0);

		// Latency = (1.04^n)*20
		n = random.nextInt(100) + 1;
		pktLatency = (int) (Math.pow(1.04, n) * 20);
		
		// Upload speed is randomly between 32Kbps and 1600Kbps
		n = random.nextInt(100) + 1;
		maxUploadSpeed = (int) (Math.pow(1.04, n) * 32000);
		
	}

	public float getPktLossProb() {
		return pktLossProb;
	}

	public int getPacketLatency() {
		return pktLatency;
	}

	public boolean shouldDropThisPacket() {
		return (random.nextFloat() < pktLossProb);
	}

	public int getMaxUploadSpeed() {
		return maxUploadSpeed;
	}
}
