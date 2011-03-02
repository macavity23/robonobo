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


import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class BeanPropertyAccessor {
	Object o;
	Log log = LogFactory.getLog(getClass());
	
	public BeanPropertyAccessor(Object o) {
		this.o = o;
	}
	
	/**
	 * Attempts to copy properties from the object from to object to
	 * @param from
	 * @param to
	 */
	public static void copy(Object from, Object to) throws Exception {
		BeanPropertyAccessor fromAccessor = new BeanPropertyAccessor(from);
		BeanPropertyAccessor toAccessor = new BeanPropertyAccessor(to);
		Iterator i =  fromAccessor.getPropertyNames().iterator();
		while(i.hasNext()) {
			String property = (String)i.next();
			Method m = fromAccessor.getGetterMethod(property);
			if(fromAccessor.getProperty(property) != null) {
				toAccessor.setProperty(property, m.getReturnType(), fromAccessor.getProperty(property));
			}
		}
		
	}
	
	public Collection getPropertyNames() {
		List methodNames = new Vector();
		Method[] methods = o.getClass().getMethods();
		for(int i = 0; i < methods.length; i++) {
			if(methods[i].getName().startsWith("set") && methods[i].getParameterTypes().length == 1) {
				String name = getName(methods[i]);
				methodNames.add(name);
				
				// descend down hierachy
				try {
					Method m = getGetterMethod(name);
					Object obj = m.invoke(this.o, new Object[] {});
					if(obj != null) {
						Iterator j = new BeanPropertyAccessor(obj).getPropertyNames().iterator();
						while(j.hasNext()) {
							String subName = (String)j.next();
							methodNames.add(name + "." + subName);
						}
					}
				}
				catch(NoSuchMethodException e) {
					// do nothing, no such method
				}
				catch(Exception e) {
					log.error("Cannot invoke getter methd",e);
				}
			}
		}
		return methodNames;
	}
	
	public void setProperty(String name, Object value) throws Exception {
		setProperty(name, value.getClass(), value);
	}
		
	public void setProperty(String name, int value) throws Exception {
		setProperty(name, int.class, new Integer(value));
	}
	
	public void setProperty(String name, boolean value) throws Exception {
		setProperty(name, boolean.class, new Boolean(value));
	}
	
	public void setProperty(String name, float value) throws Exception {
		setProperty(name, float.class, new Float(value));
	}
	
	public Object getProperty(String name) throws Exception {
		String[] bits = name.split("\\.", 2);
		Method m = getGetterMethod(bits[0]);
		Object rtn = m.invoke(o, new Object[] {});
		
		if(bits.length == 1) 
			return rtn;
		else
			return new BeanPropertyAccessor(rtn).getProperty(bits[1]);
	}
	
	protected void setProperty(String name, Class cl, Object value) throws Exception {
		String[] bits = name.split("\\.", 2);
		
		if(bits.length == 1) {
			Method m = getSetterMethod(bits[0], cl);
			m.invoke(o, new Object[] {value});
		} else {
			new BeanPropertyAccessor(getProperty(bits[0])).setProperty(bits[1], cl, value);	
		}
	}

	protected String getName(Method m) {
		return m.getName().substring(3,4).toLowerCase() + m.getName().substring(4);
	}

	protected String getSetterMethodName(String name) {
		return "set" + name.substring(0,1).toUpperCase() + name.substring(1);
	}

	protected Method getSetterMethod(String name, Class cl) throws NoSuchMethodException{
		String method = getSetterMethodName(name);
		return o.getClass().getMethod(method, new Class[] {cl});
	}

//	protected String getGetterMethodName(String name) {
//		return "get" + name.substring(0,1).toUpperCase() + name.substring(1);
//	}

	protected Method getGetterMethod(String name) throws NoSuchMethodException {
		String[] nominations = new String[] {
			"get" + name.substring(0,1).toUpperCase() + name.substring(1),
			"is" + name.substring(0,1).toUpperCase() + name.substring(1)
		};
		for(int i = 0; i < nominations.length; i++) {
			try {
				String method = nominations[i];
				return o.getClass().getMethod(method, new Class[] {});
			}
			catch(NoSuchMethodException e) {
				// nope. couldnt find it
			}
		}
		throw new NoSuchMethodException("Tried to find methods " + nominations + " but failed");
	}
}
