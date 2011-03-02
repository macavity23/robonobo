package com.robonobo.common.media;
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


import java.io.*;
import java.util.List;
import java.util.Vector;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PlaylistItem implements Serializable {
	private static final long serialVersionUID = 1L;
	String file;
	String title;
	int length;

	public PlaylistItem() {
	}

	public static List getPlaylist(String uri) throws IOException, PlaylistFormatException {
		Log log = LogFactory.getLog(PlaylistItem.class);
		Vector list = new Vector();
		HttpClient client = new HttpClient();
		GetMethod get = new GetMethod(uri);
		int status = client.executeMethod(get);
		switch(status) {
		case 200:
			BufferedReader reader = new BufferedReader(new InputStreamReader(get.getResponseBodyAsStream()));
			String line = reader.readLine();
			if(!line.equals("[playlist]"))
				throw new PlaylistFormatException("The provided URL does not describe a playlist");
			String[] kvp;
			int currentEntryNumber = 1;
			int version = 2;
			int supposedNumberOfEntries = 1;
			PlaylistItem currentEntry = new PlaylistItem();
			while((line = reader.readLine()) != null) {
				kvp = line.split("=");
				if(kvp[0].equals("NumberOfEntries")) {
					supposedNumberOfEntries = Integer.parseInt(kvp[1]);
				} else if(kvp[0].equals("Version")) {
					version = Integer.parseInt(kvp[1]);
					if(version != 2)
						throw new PlaylistFormatException("This parser currently only supports version 2 .pls files");
				} else {
					if(!kvp[0].endsWith(String.valueOf(currentEntryNumber))) {
						list.add(currentEntry);
						currentEntryNumber++;
						currentEntry = new PlaylistItem();
					}
					if(kvp[0].startsWith("File")) {
						currentEntry.setFile(kvp[1]);
					} else if(kvp[0].startsWith("Title")) {
						currentEntry.setTitle(kvp[1]);
					} else if(kvp[0].startsWith("Length")) {
						currentEntry.setLength(Integer.parseInt(kvp[1]));
					}
				}
			}
			if(currentEntry != null)
				list.add(currentEntry);
			if(supposedNumberOfEntries != list.size())
				throw new PlaylistFormatException("The server said there were " + supposedNumberOfEntries
						+ " but we actually got " + list.size());
			return list;
		default:
			throw new IOException("The remote server responded with a status " + status + " and not 200 as expected");
		}
	}

	public String getFile() {
		return file;
	}

	public int getLength() {
		return length;
	}

	public String getTitle() {
		return title;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String toString() {
		return "ShoutcastServer[file=" + file + ",title=" + title + ",length=" + length + "]";
	}
}
