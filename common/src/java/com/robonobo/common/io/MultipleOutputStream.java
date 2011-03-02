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
import java.io.OutputStream;
import java.util.Iterator;

import com.robonobo.common.util.IteratorSafeSet;


public class MultipleOutputStream extends OutputStream
{
	IteratorSafeSet streams = new IteratorSafeSet();
	
	public MultipleOutputStream()
	{
		super();
	}

	public synchronized void write(int b) throws IOException
	{
		Iterator i = streams.iterator();
		streams.setIterating(true);
		while(i.hasNext()) {
			OutputStream stream = ((OutputStream)i.next());
			try {
				stream.write(b);
			}
			catch(IOException e) {
				// woops, something happend, get rid of this stream
				streams.remove(stream);
			}
		}
		streams.setIterating(false);
	}
	
	public synchronized void write(byte[] b, int off, int len) throws IOException
	{
		Iterator i = streams.iterator();
		streams.setIterating(true);
		while(i.hasNext()) {
			OutputStream stream = ((OutputStream)i.next());
			try {
				stream.write(b, off, len);
			}
			catch(IOException e) {
				// woops, something happend, get rid of this stream
				streams.remove(stream);
			}
		}
		streams.setIterating(false);
	}
	
	public synchronized void write(byte[] b) throws IOException
	{
		Iterator i = streams.iterator();
		streams.setIterating(true);
		while(i.hasNext()) {
			OutputStream stream = ((OutputStream)i.next());
			try {
				stream.write(b);
			}
			catch(IOException e) {
				// woops, something happend, get rid of this stream
				streams.remove(stream);
			}
		}
		streams.setIterating(false);
	}
	
	public synchronized void close() throws IOException
	{
		Iterator i = streams.iterator();
		while(i.hasNext()) {
			((OutputStream)i.next()).close();
		}
		
		super.close();
		streams.clear();
	}
	
	public synchronized void flush() throws IOException
	{

		Iterator i = streams.iterator();
		streams.setIterating(true);
		while(i.hasNext()) {
			OutputStream stream = ((OutputStream)i.next());
			try {
				stream.flush();
			}
			catch(IOException e) {
				// woops, something happend, get rid of this stream
				streams.remove(stream);
			}
		}
		streams.setIterating(false);
		
		super.flush();
	}

	public synchronized void addOutputStream(OutputStream out) {
		if(!streams.contains(out)) {
			streams.add(out);
		}
	}
	
	public synchronized void removeOutputStream(OutputStream out) {
		if(streams.contains(out)) {
			streams.remove(out);
		}
	}
	
	public synchronized boolean contains(OutputStream out) {
		return streams.contains(out);
	}
}
