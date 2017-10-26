/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
*/
package org.unitime.timetable.util.queue;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

import javax.activation.DataSource;
import javax.activation.FileDataSource;

import org.apache.log4j.Logger;

/**
 * 
 * @author Tomas Muller
 *
 */

public class LocalQueueProcessor extends Thread implements QueueProcessor {
    protected static Logger sLog = Logger.getLogger(QueueProcessor.class);
    protected static long sTimeToKeep = 1000 * 60 * 60 * 10; // Keep done items for at least 10 hours.
    protected static long sTimeToSleep = 1000 * 60 * 60; // Sleep no more than an hour.

	private Queue<QueueItem> iQueue = new LinkedList<QueueItem>();
	private List<QueueItem> iFinished = new ArrayList<QueueItem>();
	private QueueItem iItem = null;
	private RunningItem iRunningItem = null;
	
	protected static LocalQueueProcessor sInstance = null;

	private boolean iCanContinue = true;
	
	protected LocalQueueProcessor() {
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
			iRunningItem = new RunningItem(iItem);
			iRunningItem.start();
			try {
				iRunningItem.join();
			} catch (InterruptedException e) {
				sLog.info("Task " + iItem.name() + " was interrupted.");
			}
			
			sLog.info("Task " + iItem.name() + (iItem.hasError() ? " failed (" + iItem.error().getMessage() + ")." : " is done."));

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
			sInstance = new LocalQueueProcessor();
			sInstance.start();
		}
		return sInstance;
	}
	
	public String generateId() {
		while (true) {
			String id = UUID.randomUUID().toString();
			if (get(id) == null) return id;
		}
	}
	
	public QueueItem add(QueueItem item) {
		synchronized(iQueue) {
			if (item.getId() == null)
				item.setId(generateId());
			iQueue.add(item);
			iQueue.notify();
		}
		return item;
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
	
	public QueueItem get(String id) {
		synchronized (iQueue) {
			for (Iterator<QueueItem> i = iQueue.iterator(); i.hasNext();) {
				QueueItem item = i.next();
				if (id.equals(item.getId())) return item;
			}
			for (Iterator<QueueItem> i = iFinished.iterator(); i.hasNext();) {
				QueueItem item = i.next();
				if (id.equals(item.getId())) return item;
			}
		}
		if (iItem != null && id.equals(iItem.getId())) return iItem;
		return null;
	}
	
	public boolean remove(String id) {
		synchronized (iQueue) {
			for (Iterator<QueueItem> i = iQueue.iterator(); i.hasNext();) {
				QueueItem item = i.next();
				if (id.equals(item.getId())) {
					i.remove();
					return true;
				}
			}
			for (Iterator<QueueItem> i = iFinished.iterator(); i.hasNext();) {
				QueueItem item = i.next();
				if (id.equals(item.getId())) {
					i.remove();
					return true;
				}
			}
		}
		if (iItem != null && id.equals(iItem.getId())) {
			iRunningItem.cancel();
		}
		return false;
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
	
	static class RunningItem extends Thread {
		boolean iInterrupted = false;
		QueueItem iRunningItem = null;
		
		private RunningItem(QueueItem item) {
			super("QueueRunner(" + item.name() + ")");
			iRunningItem = item;
			setDaemon(true);
		}
		
		@Override
		public void run() {
			iRunningItem.executeItem();
		}
		
		@SuppressWarnings("deprecation")
		public void cancel() {
			if (iInterrupted) {
				stop();
			} else {
				interrupt();
				iInterrupted = true;
			}
		}
	}

	@Override
	public DataSource getFile(String id) {
		QueueItem item = get(id);
		if (item != null && item.hasOutput()) return new FileDataSource(item.output());
		return null;
	}
}
