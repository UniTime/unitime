/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.util.queue;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.apache.log4j.Logger;

/**
 * 
 * @author Tomas Muller
 *
 */

public class QueueProcessor extends Thread {
    protected static Logger sLog = Logger.getLogger(QueueProcessor.class);
    protected static long sTimeToKeep = 1000 * 60 * 60 * 10; // Keep done items for at least 10 hours.
    protected static long sTimeToSleep = 1000 * 60 * 60; // Sleep no more than an hour.

	private Queue<QueueItem> iQueue = new LinkedList<QueueItem>();
	private List<QueueItem> iFinished = new ArrayList<QueueItem>();
	private QueueItem iItem = null;
	
	private static QueueProcessor sInstance = null;

	private boolean iCanContinue = true;
	
	private long iLastId = 0;
	
	private QueueProcessor() {
		super("QueueProcessor");
		setDaemon(true);
	}
	
	public void run() {
		sLog.info("Queue processor is up and running.");
		
		// While can continue
		while (iCanContinue) {
			synchronized (iQueue) {
				// Cleanup finished items (delete those that are too old)
				long now = new Date().getTime();
				for (Iterator<QueueItem> i = iFinished.iterator(); i.hasNext(); ) {
					QueueItem item = i.next();
					if (item.finished() == null || now - item.finished().getTime() > sTimeToKeep) i.remove();
				}
				
				// Check for a new item from the queue
				if (iQueue.isEmpty()) {
					// Sleep for it, if there is nothing to do
					sLog.info("Waiting for tasks to run...");
					try {
						iQueue.wait(sTimeToSleep);
					} catch (InterruptedException e) {
						sLog.info("Interrupted.");
					}
				}
				
				// Take new item from the top of the queue
				iItem = iQueue.poll();
				
				// Null if queue is empty
				if (iItem == null) continue;
			}
			
			// Execute the item
			sLog.info("Executing " + iItem.name());
			
			iItem.executeItem();
			
			sLog.info("Task " + iItem.name() + (iItem.error() == null ? " is done." : " failed (" + iItem.error().getMessage() + ")."));

			// Put the item into the list of finished items
			synchronized (iQueue) {
				iFinished.add(iItem);
				
				iItem = null;
			}				
		}
		
		sLog.info("Queue processor is down.");
		sInstance = null;
	}
	
	public static synchronized QueueProcessor getInstance() {
		if (sInstance == null) {
			sInstance = new QueueProcessor();
			sInstance.start();
		}
		return sInstance;
	}
	
	public void add(QueueItem item) {
		synchronized(iQueue) {
			item.setId(iLastId ++);
			iQueue.add(item);
			iQueue.notify();
		}
	}
	
	public List<QueueItem> getItems(String ownerId, Long sessionId, String type) {
		synchronized (iQueue) {
			List<QueueItem> ret = new ArrayList<QueueItem>();
			
			for (QueueItem item: iFinished) {
				if (ownerId != null && !ownerId.equals(item.getOwnerId())) continue;
				if (sessionId != null && !sessionId.equals(item.getSessionId())) continue;
				if (type!=null && !type.equals(item.type())) continue;
				ret.add(item);
			}
			
			if (iItem != null) {
				boolean add = true;
				if (ownerId != null && !ownerId.equals(iItem.getOwnerId())) add = false;
				if (sessionId != null && !sessionId.equals(iItem.getSessionId())) add = false;
				if (type!=null && !type.equals(iItem.type())) add = false;
				if (add) ret.add(iItem);
			}
			
			for (QueueItem item: iQueue) {
				if (ownerId != null && !ownerId.equals(item.getOwnerId())) continue;
				if (sessionId != null && !sessionId.equals(item.getSessionId())) continue;
				if (type!=null && !type.equals(item.type())) continue;
				ret.add(item);
			}
			
			return ret;
		}
	}
	
	public void remove(Long id) {
		synchronized (iQueue) {
			for (Iterator<QueueItem> i = iQueue.iterator(); i.hasNext();) {
				QueueItem item = i.next();
				if (id.equals(item.getId())) i.remove();
			}
			for (Iterator<QueueItem> i = iFinished.iterator(); i.hasNext();) {
				QueueItem item = i.next();
				if (id.equals(item.getId())) i.remove();
			}
		}
	}

	public static void stopProcessor() {
		if (sInstance == null) return;
		sLog.info("Stopping queue processor...");
		try {
			sInstance.iCanContinue = false;
			sInstance.interrupt();
			sInstance.join();
		} catch (InterruptedException e) {
		} catch (NullPointerException e) {
		}
	}

}
