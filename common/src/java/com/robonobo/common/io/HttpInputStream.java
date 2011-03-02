package com.robonobo.common.io;
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


import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;

public class HttpInputStream extends InputStream
{
	InputStream innerStream;
	protected String url;
	public HttpInputStream(String url) throws IOException
	{
		super();
		this.url = url;
		
		//innerStream = createInputStream();
	}
	
	protected InputStream createInputStream() throws IOException {
		HttpClient client = new HttpClient();
		HttpMethod method = createMethod(url);
		
		int status = client.executeMethod(method);
		switch(status) {
		case 200:
		case 206:
			return method.getResponseBodyAsStream();
		default:
			throw new IOException("Unable to connect to url '" + url + "', status code: " + status);
		}
	}
	
	protected HttpMethod createMethod(String location) {
		return new GetMethod(location);
	}

	public InputStream getActualStream() throws IOException {
		if(innerStream == null)
			innerStream = createInputStream();
		
		return innerStream;
	}
	
	public int read() throws IOException
	{
		return getActualStream().read();
	}

	public int available() throws IOException
	{
		return getActualStream().available();
	}
	public synchronized void mark(int readlimit)
	{
		return;
	}
	
	public boolean markSupported()
	{
		return false;
	}
	
	public int read(byte[] b) throws IOException
	{
		return getActualStream().read(b);
	}
	
	public int read(byte[] b, int off, int len) throws IOException
	{
		return getActualStream().read(b, off, len);
	}
	
	public long skip(long n) throws IOException
	{
		return getActualStream().skip(n);
	}
	
	public void close() throws IOException
	{
		getActualStream().close();
	}
	
	public synchronized void reset() throws IOException
	{
		getActualStream().reset();
	}
}
