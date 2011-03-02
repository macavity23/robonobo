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

import static java.lang.System.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * For executing a runnable after a specified amount of time, when that time will be reset frequently, eg a network
 * response timeout; avoids repeated calls to the task executor.
 * 
 * @author macavity
 */
public class Timeout implements Runnable {
	protected static Log log = LogFactory.getLog(Timeout.class);
	protected static DateFormat df = new SimpleDateFormat("HH:mm:ss:SSS");
	protected ScheduledThreadPoolExecutor executor;
	protected Runnable task;
	protected long taskScheduledTime = -1;
	protected long eventFireTime = -1;
	protected NameGetter nameGetter;
	protected ScheduledFuture<?> future;
	protected final boolean debugLogging;

	/**
	 * Use this NameGetter strangeness to allow the name of the timeout to change during its execution
	 */
	public Timeout(ScheduledThreadPoolExecutor executor, Runnable task, NameGetter nameGetter) {
		this.executor = executor;
		this.task = task;
		if (nameGetter != null)
			this.nameGetter = nameGetter;
		else {
			this.nameGetter = new NameGetter() {
				public String getName() {
					return "Unknown Timeout";
				}
			};
		}
		debugLogging = log.isDebugEnabled();
	}

	public Timeout(ScheduledThreadPoolExecutor executor, Runnable task) {
		this(executor, task, null);
	}

	/**
	 * Starts or restarts the timer
	 */
	public synchronized void set(long timeoutMs) {
		long newEventFireTime = currentTimeMillis() + timeoutMs;
		if (newEventFireTime < taskScheduledTime) {
			// If our new fire time is before the task is due to be run, we need to reschedule the task
			if (debugLogging)
				log.debug("Timeout '" + nameGetter.getName() + "' cancelling currently-scheduled task");
			future.cancel(false);
			taskScheduledTime = -1;
		}
		eventFireTime = newEventFireTime;
		if (debugLogging)
			log.debug("Timeout '" + nameGetter.getName() + "' setting fire time to " + df.format(eventFireTime));
		if (taskScheduledTime < 0)
			startTimer();
	}

	public synchronized void cancel() {
		if (debugLogging)
			log.debug("Timeout '" + nameGetter.getName() + "' cancelling ");
		clear();
		if (future != null)
			future.cancel(false);
		taskScheduledTime = -1;
	}

	/**
	 * Stops the timer
	 */
	public synchronized void clear() {
		eventFireTime = -1;
		if (debugLogging)
			log.debug("Timeout '" + nameGetter.getName() + "' clearing timeout");
	}

	public synchronized boolean isTaskIsScheduled() {
		return eventFireTime > currentTimeMillis();
	}

	/**
	 * Must only be called inside a sync block
	 */
	protected void startTimer() {
		if (debugLogging)
			log.debug("Timeout '" + nameGetter.getName() + "' scheduling task to run at " + df.format(eventFireTime));
		future = executor.schedule(this, eventFireTime - currentTimeMillis(), TimeUnit.MILLISECONDS);
		taskScheduledTime = eventFireTime;
	}

	public void run() {
		if (debugLogging)
			log.debug("Timeout '" + nameGetter.getName() + "' task running");
		synchronized (this) {
			taskScheduledTime = -1;
			// If we're not yet scheduled to run, wait until we are
			if (currentTimeMillis() < eventFireTime) {
				if (debugLogging)
					log.debug("Timeout '" + nameGetter.getName() + "' - timeout moved, rescheduling");
				startTimer();
				return;
			}
			// If we've been cleared, just return
			if (eventFireTime < 0) {
				if (debugLogging)
					log.debug("Timeout '" + nameGetter.getName() + "' - timeout cleared, task returning");
				return;
			}
		}
		// Let's rock
		if (debugLogging)
			log.debug("Timeout '" + nameGetter.getName() + "' - FIRING");
		task.run();
	}

	public interface NameGetter {
		public String getName();
	}
}
