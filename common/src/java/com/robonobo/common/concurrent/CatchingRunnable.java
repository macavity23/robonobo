package com.robonobo.common.concurrent;

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

/**
 * Catches exceptions caught by doRun() and reports them to the specified catcher - we need this as otherwise
 * exceptions thrown in the tasks run by the ThreadPoolExecutor go to /dev/null
 * @author macavity
 *
 */
public abstract class CatchingRunnable implements Runnable {
	public CatchingRunnable() {
		super();
	}

	public final void run() {
		try {
			doRun();
		} catch(Throwable t) {
			SafetyNet.notifyException(t, this);
		}
	}

	public abstract void doRun() throws Exception;
}
