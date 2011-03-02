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


import java.util.List;

import junit.framework.TestCase;

import com.robonobo.common.media.PlaylistItem;

public class PlaylistItemTest extends TestCase {

	public void testCreatePlaylist() throws Exception {
		List playlist = PlaylistItem.getPlaylist("http://di.fm/mp3/breaks.pls");
		assertTrue(playlist.size()==1);
	}
	
	public void testPlaylistItem() throws Exception {
		List playlist = PlaylistItem.getPlaylist("http://di.fm/mp3/breaks.pls");
		assertTrue(playlist.get(0) instanceof PlaylistItem);
		assertTrue(((PlaylistItem)playlist.get(0)).getFile().length()>0);
		assertTrue(((PlaylistItem)playlist.get(0)).getTitle().length()>0);
		assertTrue(((PlaylistItem)playlist.get(0)).getLength() == -1);
	}
}
