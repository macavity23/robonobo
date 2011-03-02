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


public class Version implements Comparable {
	int[] segments;
	
	public Version(String version) 
	{
		String[] bits = version.split("\\.");
		segments = new int[bits.length];
		for(int i = 0; i < bits.length; i++) 
		{
			segments[i] = Integer.parseInt(bits[i]);
		}
	}
	
	public int getMajor() {
		return getSegment(0);
	}
	
	public int getMinor() {
		return getSegment(1);
	}

	public int getRevision() 
	{
		return getSegment(2);
	}
	
	public void setMajor(int value) {
		setSegment(0, value);
	}
	
	public void setMinor(int value) {
		setSegment(1, value);
	}

	public void setRevision(int value) 
	{
		setSegment(2, value);
	}
	
	public int getSegment(int index) 
	{
		if(segments.length>index)
			return segments[index];
		else
			return 0;
	}
	
	public void setSegment(int index, int value) 
	{
		if(segments.length<=index)
		{
			int[] segs = new int[index+1];
			System.arraycopy(segments, 0, segs, 0, segments.length);
			segments = segs;
		}
		segments[index] = value;
	}
	
	
	public boolean equals(Object arg0) {
		if(arg0 instanceof Version) 
		{
			Version v2 = (Version)arg0;
			for(int i = 0; i < v2.segments.length; i++) {
				if(segments.length<=i || v2.segments[i] != segments[i])
					return false;
				
			}
			return true;
		}	
		else
			return false;
	}
	
	public boolean isGreaterThan(Version v)
	{
		for(int i = 0; i < segments.length; i++)
		{
			if(segments[i]>v.segments[i])
				return true;
		}
		return false;
	}
	
	public boolean isLessThan(Version v)
	{
		for(int i = 0; i < segments.length; i++)
		{
			if(segments[i]<v.segments[i])
				return true;
		}
		return false;
	}
	
	public int compareTo(Object arg0) {
		if(arg0 instanceof Version) {
			if(isGreaterThan((Version)arg0)) return 1;
			if(isLessThan((Version)arg0)) return -1;
			return 0;
		} else {
			throw new ClassCastException();
		}
	}
	
	public String toString() 
	{
		String str = "";
		for(int i = 0; i < segments.length; i++)
		{
			str+=segments[i];
			if(i < segments.length-1)
				str+=".";
		}
		return str;
	}
}
