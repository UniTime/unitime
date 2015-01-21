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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Transaction;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.timetable.defaults.ApplicationProperty;
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
	
	public static void cleanupStudentSectioningQueue(int days) {
		if (days < 0) return;
		org.hibernate.Session hibSession = new _RootDAO().createNewSession();
		Transaction tx = null;
		try {
			tx = hibSession.beginTransaction();
			int rows = hibSession.createQuery(
					"delete from StudentSectioningQueue where timeStamp < " + HibernateUtil.addDate("current_date()", ":days")
					).setInteger("days", - days).executeUpdate();
			if (rows > 0)
				sLog.info("All records older than " + days + " days deleted from the student sectioning queue (" + rows + " records).");
			tx.commit();
		} catch (Throwable t) {
			sLog.warn("Failed to cleanup student sectioning queue: " + t.getMessage(), t);
			if (tx != null) tx.rollback();
		} finally {
			hibSession.close();
		}
	}
	
	public static void cleanupLogs() {
		cleanupChangeLog(ApplicationProperty.LogCleanupChangeLog.intValue());
		cleanupQueryLog(ApplicationProperty.LogCleanupQueryLog.intValue());
		cleanupOnlineSectioningLog(ApplicationProperty.LogCleanupOnlineSchedulingLog.intValue());
		cleanupMessageLog(ApplicationProperty.LogCleanupMessageLog.intValue());
		cleanupStudentSectioningQueue(ApplicationProperty.LogCleanupOnlineSchedulingQueue.intValue());
	}

}
