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


public class RemoteException extends Exception
{
	String className;

	public RemoteException()
	{
		super();
		// TODO Auto-generated constructor stub
	}

	public RemoteException(String arg0)
	{
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public RemoteException(String arg0, Throwable arg1)
	{
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}

	public RemoteException(Throwable arg0)
	{
		super(arg0);
		// TODO Auto-generated constructor stub
	}
	
	public void setRealClassName(String name) {
		className = name;
	}
	
	public String getRealClassName() {
		return className;
	}

}
