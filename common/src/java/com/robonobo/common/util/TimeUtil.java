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

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtil {
	private static final int MS_IN_SEC = 1000;
	private static final int MS_IN_MIN = 60000;
	private static final int MS_IN_HOUR = 3600000;
	private static DateFormat timeFormat;
	private static DateFormat dateFormat;
	private static DateFormat dateTimeFormat;
	
	static {
		timeFormat = new SimpleDateFormat("HH:mm:ss:SSS");
		dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
	}
	
	public static Date now() {
		return new Date();
	}
	
	public static Date timeInFuture(long msInFuture) {
		return new Date(currentTimeMillis()+msInFuture);
	}
	
	public static Date timeInPast(long msInPast) {
		return new Date(currentTimeMillis()-msInPast);
	}

	public static long msElapsedSince(Date pastDate) {
		return currentTimeMillis() - pastDate.getTime();
	}
	
	public static long msUntil(Date futureDate) {
		return futureDate.getTime() - currentTimeMillis();
	}
	
	public static String hoursMinsSecsFromMs(long ms) {
		NumberFormat nf = NumberFormat.getIntegerInstance();
		nf.setMinimumIntegerDigits(2);
		long hours = ms / MS_IN_HOUR;
		long hourRem = ms - (hours * MS_IN_HOUR);
		long mins = hourRem / MS_IN_MIN;
		long minsRem = hourRem - (mins * MS_IN_MIN);
		long secs = minsRem / MS_IN_SEC;
		return hours+":"+nf.format(mins)+":"+nf.format(secs);
	}
	
	public static String minsSecsFromMs(long ms) {
		NumberFormat nf = NumberFormat.getIntegerInstance();
		nf.setMinimumIntegerDigits(2);
		long mins = ms / MS_IN_MIN;
		long minsRem = ms - (mins * MS_IN_MIN);
		long secs = minsRem / MS_IN_SEC;
		return mins+":"+nf.format(secs);
	}

	public static DateFormat getTimeFormat() {
		return timeFormat;
	}

	public static DateFormat getDateFormat() {
		return dateFormat;
	}

	public static DateFormat getDateTimeFormat() {
		return dateTimeFormat;
	}
}
