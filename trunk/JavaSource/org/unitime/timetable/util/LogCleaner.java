package org.unitime.timetable.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Transaction;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.dao._RootDAO;

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
					).setInteger("days", days).executeUpdate();
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
					).setInteger("days", days).executeUpdate();
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
					).setInteger("days", days).executeUpdate();
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
	
	public static void cleanupLogs() {
		cleanupChangeLog(Integer.parseInt(ApplicationProperties.getProperty("unitime.cleanup.changeLog", "183")));
		cleanupQueryLog(Integer.parseInt(ApplicationProperties.getProperty("unitime.cleanup.queryLog", "92")));
		cleanupChangeLog(Integer.parseInt(ApplicationProperties.getProperty("unitime.cleanup.sectioningLog", "366")));
	}

}
