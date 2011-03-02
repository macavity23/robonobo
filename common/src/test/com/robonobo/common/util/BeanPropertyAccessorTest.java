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

import com.robonobo.common.util.BeanPropertyAccessor;

public class BeanPropertyAccessorTest extends TestCase {

	public void testGetStringProperty() throws Exception {
		Object o = new BeanPropertyAccessor(new TestObject()).getProperty("text");
		assertTrue(o instanceof String);
		assertEquals("hello", o);
	}
	
	public void testGetIntProperty() throws Exception {
		Object o = new BeanPropertyAccessor(new TestObject()).getProperty("number");
		assertTrue(o instanceof Integer);
		assertEquals(new Integer(1), o);
	}
	
	public void testGetCompositeProperty() throws Exception {
		Object o = new BeanPropertyAccessor(new TestObject()).getProperty("foo.bar");
		assertTrue(o instanceof String);
		assertEquals("woop", o);
	}
	
	public void testSetStringProperty() throws Exception {
		TestObject o = new TestObject();
		new BeanPropertyAccessor(o).setProperty("text", "plop");
		assertTrue(o.getText() instanceof String);
		assertEquals("plop", o.getText());
	}
	
	public void testSetIntProperty() throws Exception {
		TestObject o = new TestObject();
		new BeanPropertyAccessor(o).setProperty("number", 123);;
		assertEquals(123, o.getNumber());
	}
	
	public void testSetCompositeProperty() throws Exception {
		TestObject o = new TestObject();
		new BeanPropertyAccessor(o).setProperty("foo.bar", "hoopy");
		assertTrue(o.getFoo().getBar() instanceof String);
		assertEquals("hoopy", o.getFoo().getBar());
	}
	
	public class TestObject {
		String text = "hello";
		int number = 1;
		AnotherObject foo = new AnotherObject();
		
		public AnotherObject getFoo() {
			return foo;
		}
		public void setFoo(AnotherObject foo) {
			this.foo = foo;
		}
		public int getNumber() {
			return number;
		}
		public void setNumber(int number) {
			this.number = number;
		}
		public String getText() {
			return text;
		}
		public void setText(String text) {
			this.text = text;
		}
	}
	
	public class AnotherObject {
		String bar = "woop";
		public String getBar() {
			return bar;
		}
		public void setBar(String bar) {
			this.bar = bar;
		}
	}
}
