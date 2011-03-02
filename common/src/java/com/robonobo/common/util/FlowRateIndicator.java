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


import static java.lang.System.*;

import java.util.Date;

public class FlowRateIndicator {
	/** We keep a rolling average over this many secs */
	private static final int SECS_HISTORY = 5;
	private int[] hist = new int[SECS_HISTORY];
	private long lastCheckpoint;
	private int bytesSinceCheckpoint;
	private int flowRate;
	
	public FlowRateIndicator() {
		bytesSinceCheckpoint = 0;
		lastCheckpoint = currentTimeMillis();
	}
	
	public synchronized void notifyData(int numBytes) {
		bytesSinceCheckpoint += numBytes;
		checkAndRecalc();
	}
	
	public synchronized int getFlowRate() {
		checkAndRecalc();
		return flowRate;
	}

	private void checkAndRecalc() {
		long now = currentTimeMillis();
		long elapsedMs = now - lastCheckpoint;
		if(elapsedMs > 1000) {
			// Recalculate the rate
			for(int i=hist.length-1;i>0;i--) {
				hist[i] = hist[i-1];
			}
			hist[0] = (int) (bytesSinceCheckpoint * (1000f / elapsedMs));
			int newRate = 0;
			for(int i=0;i<hist.length;i++) {
				newRate += hist[i] / SECS_HISTORY;
			}
			flowRate = newRate;
			bytesSinceCheckpoint = 0;
			lastCheckpoint = now;
		}
	}
}
