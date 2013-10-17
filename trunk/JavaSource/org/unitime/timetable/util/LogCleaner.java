/*
 * UniTime 3.3 - 3.5 (University Timetabling Application)
 * Copyright (C) 2012 - 2013, UniTime LLC, and individual contributors
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Transaction;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.dao._RootDAO;

/**
 * @author Tomas Muller
 */
public class LogCleaner {
	private static  Log sLog = LogFactory.getLog(LogCleaner.class);
	
	public static void cleanupQueryLog(int days) {
		if (days < 0) return;
		org.hibernate.Session hibSession = new _RootDAO().createNewSession();
		Transaction tx = null;
		try {
			tx = hibSession.beginTransaction();
			int rows = hibSession.createQuery(
					"delete from QueryLog where timeStamp < " + HibernateUtil.addDate("current_date()", ":days")
					).setInteger("days", - days).executeUpdate();
			if (rows > 0)
				sLog.info("All records older than " + days + " days deleted from the query log (" + rows + " records).");
			tx.commit();
		} catch (Throwable t) {
			sLog.warn("Failed to cleanup query log: " + t.getMessage(), t);
			if (tx != null) tx.rollback();
		} finally {
			hibSession.close();
		}
	}
	
	public static void cleanupChangeLog(int days) {
		if (days < 0) return;
		org.hibernate.Session hibSession = new _RootDAO().createNewSession();
		Transaction tx = null;
		try {
			tx = hibSession.beginTransaction();
			int rows = hibSession.createQuery(
					"delete from ChangeLog where timeStamp < " + HibernateUtil.addDate("current_date()", ":days")
					).setInteger("days", - days).executeUpdate();
			if (rows > 0)
				sLog.info("All records older than " + days + " days deleted from the change log (" + rows + " records).");
			tx.commit();
		} catch (Throwable t) {
			sLog.warn("Failed to cleanup query log: " + t.getMessage(), t);
			if (tx != null) tx.rollback();
		} finally {
			hibSession.close();
		}
	}
	
	public static void cleanupOnlineSectioningLog(int days) {
		if (days < 0) return;
		org.hibernate.Session hibSession = new _RootDAO().createNewSession();
		Transaction tx = null;
		try {
			tx = hibSession.beginTransaction();
			int rows = hibSession.createQuery(
					"delete from OnlineSectioningLog where timeStamp < " + HibernateUtil.addDate("current_date()", ":days")
					).setInteger("days", - days).executeUpdate();
			if (rows > 0)
				sLog.info("All records older than " + days + " days deleted from the online sectioning log (" + rows + " records).");
			tx.commit();
		} catch (Throwable t) {
			sLog.warn("Failed to cleanup query log: " + t.getMessage(), t);
			if (tx != null) tx.rollback();
		} finally {
			hibSession.close();
		}
	}
	
	public static void cleanupMessageLog(int days) {
		if (days < 0) return;
		org.hibernate.Session hibSession = new _RootDAO().createNewSession();
		Transaction tx = null;
		try {
			tx = hibSession.beginTransaction();
			int rows = hibSession.createQuery(
					"delete from MessageLog where timeStamp < " + HibernateUtil.addDate("current_date()", ":days")
					).setInteger("days", - days).executeUpdate();
			if (rows > 0)
				sLog.info("All records older than " + days + " days deleted from the message log (" + rows + " records).");
			tx.commit();
		} catch (Throwable t) {
			sLog.warn("Failed to cleanup message log: " + t.getMessage(), t);
			if (tx != null) tx.rollback();
		} finally {
			hibSession.close();
		}
	}
	
	public static void cleanupLogs() {
		cleanupChangeLog(Integer.parseInt(ApplicationProperties.getProperty("unitime.cleanup.changeLog", "366")));
		cleanupQueryLog(Integer.parseInt(ApplicationProperties.getProperty("unitime.cleanup.queryLog", "92")));
		cleanupOnlineSectioningLog(Integer.parseInt(ApplicationProperties.getProperty("unitime.cleanup.sectioningLog", "366")));
		cleanupMessageLog(Integer.parseInt(ApplicationProperties.getProperty("unitime.message.log.cleanup.days", "14")));
	}

}
