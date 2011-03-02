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


import junit.framework.TestCase;

import com.robonobo.common.util.Version;

public class VersionTest extends TestCase {

	public void testIsGreaterThan() 
	{
		Version v = new Version("1.10.10.12");
		Version v2 = new Version("1.20.3.2231");
		assertTrue(v2.isGreaterThan(v));
	}
	
	public void testIsLessThan() 
	{
		Version v = new Version("1.3.0.2");
		Version v2 = new Version("1.2.0.10");
		assertTrue(v2.isLessThan(v));
	}
	
	public void testIsEqual() 
	{
		Version v = new Version("1.3.123.1232");
		Version v2 = new Version("1.3.123.1232");
		assertTrue(v2.equals(v));
	}
	
	public void testToString() 
	{
		Version v = new Version("1.3.123.1232");
		assertTrue(v.toString().equals("1.3.123.1232"));
	}
}
