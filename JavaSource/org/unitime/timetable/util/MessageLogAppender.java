/*
 * UniTime 3.4 - 3.5 (University Timetabling Application)
 * Copyright (C) 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.unitime.timetable.ApplicationProperties;
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
			iMinLevel = Level.toLevel(ApplicationProperties.getProperty("unitime.message.log.level", Level.WARN.toString()), Level.WARN);
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
			iLogLimit = Integer.parseInt(ApplicationProperties.getProperty("unitime.message.log.limit", String.valueOf(iLogLimit)));
			iCleanupInterval = Integer.parseInt(ApplicationProperties.getProperty("unitime.message.log.cleanup.interval", String.valueOf(iCleanupInterval)));
			iCleanupDays = Integer.parseInt(ApplicationProperties.getProperty("unitime.message.log.cleanup.days", String.valueOf(iCleanupDays)));
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
