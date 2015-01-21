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
package org.unitime.timetable.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.hibernate.CacheMode;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.MessageLog;
import org.unitime.timetable.model.dao.MessageLogDAO;

/**
 * @author Tomas Muller
 */
public class MessageLogAppender extends AppenderSkeleton {
	private Saver iSaver = null;
	private Level iMinLevel = null;
	
	@Override
	public void close() {
		if (iSaver != null)
			iSaver.interrupt();
	}

	@Override
	public boolean requiresLayout() {
		return false;
	}
	
	private Saver getSaver() {
		if (iSaver == null) {
			iSaver = new Saver();
			iSaver.start();
		}
		return iSaver;
	}
	
	public Level getMinLevel() {
		if (iMinLevel == null) {
			iMinLevel = Level.toLevel(ApplicationProperty.MessageLogLevel.value());
		}
		return iMinLevel;
	}
	
	@Override
	protected void append(LoggingEvent event) {
		if (!event.getLevel().isGreaterOrEqual(getMinLevel())) return;
		if (event.getLogger().equals(MessageLogAppender.class.getName())) return;
		
		MessageLog m = new MessageLog();
		m.setLevel(event.getLevel().toInt());

		String logger = event.getLoggerName();
		if (logger.indexOf('.') >= 0) logger = logger.substring(logger.lastIndexOf('.') + 1);
		m.setLogger(logger.length() > 255 ? logger.substring(0, 255) : logger);
		
		m.setMessage(event.getMessage() == null ? null : event.getMessage().toString());
		
		m.setTimeStamp(new Date(event.getTimeStamp()));
		
		m.setNdc(event.getNDC());
		
		String thread = event.getThreadName();
		m.setThread(thread == null ? null : thread.length() > 100 ? thread.substring(0, 100) : thread);
		
		Throwable t = (event.getThrowableInformation() != null ? event.getThrowableInformation().getThrowable() : null);
		if (t != null) {
			String ex = "";
			while (t != null) {
				String clazz = t.getClass().getName();
				if (clazz.indexOf('.') >= 0) clazz = clazz.substring(1 + clazz.lastIndexOf('.'));
				if (!ex.isEmpty()) ex += "\n";
				ex += clazz + ": " + t.getMessage();
				if (t.getStackTrace() != null && t.getStackTrace().length > 0)
					ex += " (at " + t.getStackTrace()[0].getFileName() + ":" + t.getStackTrace()[0].getLineNumber() + ")";
				t = t.getCause();
			}
			if (!ex.isEmpty())
				m.setException(ex);
		}
		
		getSaver().add(m);
	}
	
	public static class Saver extends Thread {
		private List<MessageLog> iMessages = new Vector<MessageLog>();
		private boolean iActive = true;
		private int iLogLimit = 5000;
		private int iCleanupInterval = 180;
		private int iCleanupDays = 14;
		private long iCounter = 0;
		
		public Saver() {
			super("MessageLogSaver");
			iLogLimit = ApplicationProperty.MessageLogLimit.intValue();
			iCleanupInterval = ApplicationProperty.MessageLogCleanupInterval.intValue();
			iCleanupDays = ApplicationProperty.LogCleanupMessageLog.intValue();
			setDaemon(true);
		}
		
		@Override
		public void interrupt() {
			iActive = false;
			super.interrupt();
			try { join(); } catch (InterruptedException e) {}
		}
		
		public void add(MessageLog m) {
			if (!iActive) return;
			synchronized (iMessages) {
				if (iLogLimit <= 0 || iMessages.size() < iLogLimit)
					iMessages.add(m);
			}
		}
		
		public void run() {
			while (true) {
				try {
					try {
						sleep(60000);
					} catch (InterruptedException e) {
					}
					iCounter++;
					if ((iCounter % iCleanupInterval) == 0) LogCleaner.cleanupMessageLog(iCleanupDays);
					List<MessageLog> messagesToSave = null;
					synchronized (iMessages) {
						if (!iMessages.isEmpty()) {
							messagesToSave = new ArrayList<MessageLog>(iMessages);
							iMessages.clear();
						}
					}
					if (messagesToSave != null) {
						Session hibSession = MessageLogDAO.getInstance().createNewSession();
						hibSession.setCacheMode(CacheMode.IGNORE);
						Transaction tx = hibSession.beginTransaction();
						try {
							for (MessageLog m: messagesToSave)
								hibSession.save(m);
							hibSession.flush();
							tx.commit();
						} catch (Exception e) {
							tx.rollback();
							System.err.println("Failed to persist " + messagesToSave.size() + " log entries:" + e.getMessage());
						} finally {
							hibSession.close();
						}
					}
					if (!iActive) break;
				} catch (Exception e) {
					System.err.println("Failed to persist log entries:" + e.getMessage());
				}
			}
		}
		
	}
}
