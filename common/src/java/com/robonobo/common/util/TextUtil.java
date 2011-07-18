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

import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.robonobo.common.exceptions.Errot;

/**
 * Assorted text utils
 * 
 * @author macavity
 */
public class TextUtil {
	public static String[] UNITS = { "", "K", "M", "G", "T", "P", "E" };
	private static double[] UNIT_THRESH = { 1d, 1024d, 1048576d, 1073741824d, 1099511627776d, 1125899906842624d, 1152921504606846976d };

	// Never instantiate this class
	private TextUtil() {
	}

	public static String formatDurationHMS(long ms) {
		long hrs = ms / 1000 / 60 / 60;
		long min = ms / 1000 / 60 % 60;
		long sec = ms / 1000 % 60;

		StringBuffer sb = new StringBuffer();
		if (hrs > 0)
			sb.append(leftPad(String.valueOf(hrs), 2, '0')).append(":");
		sb.append(leftPad(String.valueOf(min), 2, '0')).append(":");
		sb.append(leftPad(String.valueOf(sec), 2, '0'));
		return sb.toString();
	}

	public static String formatDurationHMSms(long ms) {
		long hrs = ms / 1000 / 60 / 60;
		long min = ms / 1000 / 60 % 60;
		long sec = ms / 1000 % 60;
		long rms = ms % 1000;

		return leftPad(String.valueOf(hrs), 2, '0') + ":" + leftPad(String.valueOf(min), 2, '0') + ":" + leftPad(String.valueOf(sec), 2, '0') + ":"
				+ leftPad(String.valueOf(rms), 3, '0');
	}

	public static String formatSizeInBytes(int size) {
		if (size < 1)
			return size + "B";
		for (int i = 0; i < (UNITS.length - 1); i++) {
			if (size < UNIT_THRESH[i + 1])
				return (int) (size / UNIT_THRESH[i]) + UNITS[i] + "B";
		}
		return size + "B";
	}

	public static String leftPad(String str, int size) {
		return leftPad(str, size, ' ');
	}

	public static String leftPad(String str, int size, char padChar) {
		if (str.length() >= size)
			return str;
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < size - str.length(); i++) {
			sb.append(padChar);
		}
		sb.append(str);
		return sb.toString();
	}

	public static String rightPad(String str, int size) {
		if (str.length() >= size)
			return str;
		StringBuffer sb = new StringBuffer();
		sb.append(str);
		for (int i = 0; i < size - str.length(); i++) {
			sb.append(' ');
		}
		return sb.toString();
	}

	public static String bytesToHexString(byte[] input) {
		return bytesToHexString(input, input.length);
	}

	/**
	 * 
	 * @param input
	 * @param maxLength
	 *            max number of bytes to print
	 * @return
	 */
	public static String bytesToHexString(byte[] input, int maxLength) {
		if (input == null)
			return null;
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < maxLength; i++) {
			int digit = input[i] & 0xff;
			sb.append(Integer.toHexString(digit));
		}
		return sb.toString();
	}

	public static String byteToBinaryString(byte input) {
		StringBuffer sb = new StringBuffer();
		for (int i = 128; i >= 1; i /= 2) {
			if ((input & i) > 0)
				sb.append("1");
			else
				sb.append("0");
		}
		return sb.toString();
	}

	public static String padToMinWidth(double num, int minWidth) {
		NumberFormat nf = NumberFormat.getNumberInstance();
		int intDigits = numDigits((int) num);
		if (intDigits >= minWidth)
			nf.setMaximumFractionDigits(0);
		else {
			boolean hasFracComponent = (int) num != num;
			if (hasFracComponent) {
				nf.setMaximumFractionDigits(minWidth - intDigits);
				nf.setMinimumFractionDigits(minWidth - intDigits);
			}
		}
		return nf.format(num);
	}

	public static int numDigits(long val) {
		// This seems a bit ugly, there must be a more elegant way
		String s = String.valueOf(val);
		return s.length();
	}

	public static String rightPadOrTruncate(String str, int resultSz) {
		if(resultSz < 5)
			throw new IllegalArgumentException("Can't truncate to <5 chars, won't fit ellipsis in");
		if (str == null)
			str = "";
		if (resultSz >= str.length())
			return rightPad(str, resultSz);
		String el = "[...]";
		int availSz = resultSz - el.length();
		return el + str.substring(str.length() - availSz);
	}

	public static String[] getQuotedArgs(String cmdLine) {
		List args = new ArrayList();
		Pattern p = Pattern.compile("\"(.*?)\"");
		Matcher m = p.matcher(cmdLine);
		int lastQuotEnd = 0;
		while (m.find(lastQuotEnd)) {
			String stuffBefore = cmdLine.substring(lastQuotEnd, m.start());
			String[] argsBefore = stuffBefore.trim().split("\\s+");
			if (argsBefore.length > 1 || argsBefore[0].length() != 0)
				Collections.addAll(args, argsBefore);
			args.add(m.group(1));
			lastQuotEnd = m.end();
		}
		String stuffLeft = cmdLine.substring(lastQuotEnd);
		String[] argsLeft = stuffLeft.trim().split("\\s+");
		// If we have a trailing empty string, don't add it
		if (argsLeft.length > 1 || argsLeft[0].length() != 0)
			Collections.addAll(args, argsLeft);
		String[] result = new String[args.size()];
		args.toArray(result);
		return result;
	}

	public static String repeat(String pattern, int times) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < times; i++) {
			sb.append(pattern);
		}
		return sb.toString();
	}

	/**
	 * URL-encodes the string as UTF-8 (avoids the need to catch
	 * unsupportedencoding exception)
	 */
	public static String urlEncode(String input) {
		if (input == null)
			return null;
		try {
			return URLEncoder.encode(input, "utf-8");
		} catch (UnsupportedEncodingException e) {
			throw new Errot();
		}
	}

	public static String truncate(String str, int length) {
		if (str.length() <= length)
			return str;
		return str.substring(0, length);
	}

	/**
	 * URL-decodes the string as UTF-8 (avoids the need to catch
	 * unsupportedencoding exception)
	 */
	public static String urlDecode(String input) {
		if (input == null)
			return null;
		try {
			return URLDecoder.decode(input, "utf-8");
		} catch (UnsupportedEncodingException e) {
			throw new Errot();
		}
	}

/**
	 * Escapes '<' and '>' characters, replacing them with '&lt;' and '&gt;'
	 */
	public static String escapeHtml(String input) {
		if(input == null)
			return null;
		return input.replace("<", "&lt;").replace(">", "&gt;");
	}

	public static String readInputStreamToString(InputStream stream) throws IOException {
		StringBuffer sb = new StringBuffer(stream.available());
		byte[] buf = new byte[stream.available()];
		for (int n; (n = stream.read(buf)) >= 0;) {
			sb.append(new String(buf, 0, n));
		}
		return sb.toString();
	}

	/**
	 * Returns (eg) '5 apples', or '1 apple'
	 */
	public static String numItems(Collection<?> items, String name) {
		return numItems(items.size(), name);
	}

	public static String numItems(int num, String name) {		
		StringBuffer sb = new StringBuffer();
		sb.append(num).append(" ").append(name);
		if (num != 1)
			sb.append("s");
		return sb.toString();
	}
	public static CharSequence commaSepList(Collection<?> coll) {
		StringBuffer sb = new StringBuffer();
		boolean first = true;
		for (Object obj : coll) {
			if (first)
				first = false;
			else
				sb.append(", ");
			sb.append(obj);
		}
		return sb;
	}

	/**
	 * f should be in the range 0 - 1
	 */
	public static String percentage(float f) {
		if (f < 0f || f > 1f)
			throw new IllegalArgumentException();
		return padToMinWidth(f * 100, 3) + "%";
	}

	/**
	 * If the string is longer than maxWidth, limit it to fit within maxWidth
	 * with an ellipsis (...). Will always return at least the string "..."
	 */
	public static String limitWithEllipsis(String str, Font font, int maxWidth, Component c) {
		FontMetrics metrics = c.getFontMetrics(font);
		int strWidth = metrics.stringWidth(str);
		if (strWidth <= maxWidth)
			return str;
		for (int len = str.length() - 1; len > 0; len--) {
			String subStr = str.substring(0, len) + "...";
			if (metrics.stringWidth(subStr) <= maxWidth)
				return subStr;
		}
		return "...";
	}

	public static String capitalizeFirst(String str) {
		return str.substring(0, 1).toUpperCase() + str.substring(1);
	}

	/**
	 * Returns a hopefully reasonably-decent 64 bit hash. Adapted from
	 * String.hashcode()
	 */
	public static long longHash(String str) {
		long h = 1125899906842597L; // Prime
		int len = str.length();
		for (int i = 0; i < len; i++) {
			h = 31 * h + str.charAt(i);
		}
		return h;
	}
	
	/**
	 * Returns false if the string is null or 0-length, true otherwise
	 */
	public static boolean isNonEmpty(String str) {
		return (str != null && str.length() > 0);
	}
	
	public static boolean isEmpty(String str) {
		return (str == null) || (str.length() == 0);
	}
	
	public static boolean arrContains(String[] arr, String str) {
		for(int i=0;i<arr.length;i++) {
			if(arr[i].equals(str))
				return true;
		}
		return false;
	}
}
