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

import com.robonobo.common.util.Modulo;

public class ModuloTest extends TestCase
{

	public void testModuloWrapsAroundMax() {
		Modulo m = new Modulo((long)Math.pow(2,32));
		assertEquals(0l, m.add((long)Math.pow(2,32)-1, 1l));
		assertEquals(0l, m.add((long)Math.pow(2,32), 0l));
	}
	
	public void testModuloWrapsAroundMin() {
		Modulo m = new Modulo((long)Math.pow(2,32));
		assertEquals((long)Math.pow(2,32)-1, m.sub(0l, 1l));
		assertEquals((long)Math.pow(2,32)-1, m.sub(1l, 2l));
	}
	
	
}
