/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2011, UniTime LLC, and individual contributors
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
package org.unitime.timetable.onlinesectioning;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.Logger;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.StudentSectioningQueue;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.onlinesectioning.custom.SectionLimitProvider;
import org.unitime.timetable.onlinesectioning.custom.SectionUrlProvider;

/**
 * @author Tomas Muller
 */
public class OnlineSectioningService {
	private static Logger sLog = Logger.getLogger(OnlineSectioningService.class);
	private static Hashtable<Long, OnlineSectioningServer> sInstances = new Hashtable<Long, OnlineSectioningServer>();
	private static Hashtable<Long, OnlineSectioningServerUpdater> sUpdaters = new Hashtable<Long, OnlineSectioningServerUpdater>();
	private static OnlineSectioningServerUpdater sUpdater;
	
    public static SectionLimitProvider sSectionLimitProvider = null;
    public static SectionUrlProvider sSectionUrlProvider = null;
    public static boolean sUpdateLimitsUsingSectionLimitProvider = false;
    
	private static ReentrantReadWriteLock sGlobalLock = new ReentrantReadWriteLock();

	public static void startService() {
		sLog.info("Student Sectioning Service is starting up ...");
		OnlineSectioningLogger.startLogger();
		org.hibernate.Session hibSession = SessionDAO.getInstance().getSession();
		String year = ApplicationProperties.getProperty("unitime.enrollment.year");
		String term = ApplicationProperties.getProperty("unitime.enrollment.term");
		String campus = ApplicationProperties.getProperty("unitime.enrollment.campus");
		try {
			sUpdater = new OnlineSectioningServerUpdater(StudentSectioningQueue.getLastTimeStamp(hibSession, null));
			for (Iterator<Session> i = SessionDAO.getInstance().findAll(hibSession).iterator(); i.hasNext(); ) {
				final Session session = i.next();
				
				if (year != null && !year.equals(session.getAcademicYear())) continue;
				if (term != null && !term.equals(session.getAcademicTerm())) continue;
				if (campus != null && !campus.equals(session.getAcademicInitiative())) continue;
				if (session.getStatusType().isTestSession()) continue;
				if (!session.getStatusType().canSectionAssistStudents() && !session.getStatusType().canOnlineSectionStudents()) continue;

				int nrSolutions = ((Number)hibSession.createQuery(
						"select count(s) from Solution s where s.owner.session.uniqueId=:sessionId")
						.setLong("sessionId", session.getUniqueId()).uniqueResult()).intValue();
				if (nrSolutions == 0) continue;
				final Long sessionId = session.getUniqueId();
				if ("true".equals(ApplicationProperties.getProperty("unitime.enrollment.autostart", "false"))) {
					Thread t = new Thread(new Runnable() {
						public void run() {
							try {
								ApplicationProperties.setSessionId(sessionId);
								OnlineSectioningService.createInstance(sessionId);
							} catch (Exception e) {
								sLog.fatal("Unable to upadte session " + session.getAcademicTerm() + " " + session.getAcademicYear() +
										" (" + session.getAcademicInitiative() + "), reason: "+ e.getMessage(), e);
							} finally {
								ApplicationProperties.setSessionId(null);
							}
						}
					});
					t.setName("CourseLoader[" + session.getAcademicTerm()+session.getAcademicYear()+" "+session.getAcademicInitiative()+"]");
					t.setDaemon(true);
					t.start();
				} else {
					try {
						OnlineSectioningService.createInstance(sessionId);
					} catch (Exception e) {
						sLog.fatal("Unable to upadte session " + session.getAcademicTerm() + " " + session.getAcademicYear() +
								" (" + session.getAcademicInitiative() + "), reason: "+ e.getMessage(), e);
					}
				}
			}
			sUpdater.start();
		} catch (Exception e) {
			throw new RuntimeException("Unable to initialize, reason: "+e.getMessage(), e);
		} finally {
			hibSession.close();
		}
		if (ApplicationProperties.getProperty("unitime.custom.SectionLimitProvider") != null) {
        	try {
        		sSectionLimitProvider = (SectionLimitProvider)Class.forName(ApplicationProperties.getProperty("unitime.custom.SectionLimitProvider")).newInstance();
        	} catch (Exception e) {
        		sLog.fatal("Unable to initialize section limit provider, reason: "+e.getMessage(), e);
        	}
        }
        if (ApplicationProperties.getProperty("unitime.custom.SectionUrlProvider") != null) {
        	try {
        		sSectionUrlProvider = (SectionUrlProvider)Class.forName(ApplicationProperties.getProperty("unitime.custom.SectionUrlProvider")).newInstance();
        	} catch (Exception e) {
        		sLog.fatal("Unable to initialize section URL provider, reason: "+e.getMessage(), e);
        	}
        }
        sUpdateLimitsUsingSectionLimitProvider = "true".equalsIgnoreCase(ApplicationProperties.getProperty("unitime.custom.SectionLimitProvider.updateLimits", "false"));
	}

	public static boolean isEnabled() {
		// if autostart is enabled, just check whether there are some instances already loaded in
		if ("true".equals(ApplicationProperties.getProperty("unitime.enrollment.autostart", "false")))
			return !sInstances.isEmpty();
		
		// quick check for existing instances
		if (!sInstances.isEmpty()) return true;
		
		// otherwise, look for a session that has sectioning enabled
		String year = ApplicationProperties.getProperty("unitime.enrollment.year");
		String term = ApplicationProperties.getProperty("unitime.enrollment.term");
		String campus = ApplicationProperties.getProperty("unitime.enrollment.campus");
		for (Iterator<Session> i = SessionDAO.getInstance().findAll().iterator(); i.hasNext(); ) {
			final Session session = i.next();
			
			if (year != null && !year.equals(session.getAcademicYear())) continue;
			if (term != null && !term.equals(session.getAcademicTerm())) continue;
			if (campus != null && !campus.equals(session.getAcademicInitiative())) continue;
			if (session.getStatusType().isTestSession()) continue;

			if (!session.getStatusType().canSectionAssistStudents() && !session.getStatusType().canOnlineSectionStudents()) continue;

			return true;
		}
		return false;
	}
	
	public static boolean isRegistrationEnabled() {
		for (Session session: SessionDAO.getInstance().findAll()) {
			if (session.getStatusType().isTestSession()) continue;
			if (!session.getStatusType().canOnlineSectionStudents() && !session.getStatusType().canSectionAssistStudents() && session.getStatusType().canPreRegisterStudents()) return true;
		}
		return false;
	}

	public static void createInstance(Long academicSessionId) {
		sGlobalLock.writeLock().lock();
		try {
			OnlineSectioningServer s = new OnlineSectioningServerImpl(academicSessionId, false);
			sInstances.put(academicSessionId, s);
			org.hibernate.Session hibSession = SessionDAO.getInstance().createNewSession();
			try {
				OnlineSectioningServerUpdater updater = new OnlineSectioningServerUpdater(s.getAcademicSession(), StudentSectioningQueue.getLastTimeStamp(hibSession, academicSessionId));
				sUpdaters.put(academicSessionId, updater);
				updater.start();
			} finally {
				hibSession.close();
			}
		} finally {
			sGlobalLock.writeLock().unlock();
		}
	}
	
	public static OnlineSectioningServer getInstance(final Long academicSessionId) throws SectioningException {
		sGlobalLock.readLock().lock();
		try {
			return sInstances.get(academicSessionId);
		} finally {
			sGlobalLock.readLock().unlock();
		}
	}
	
	public static TreeSet<AcademicSessionInfo> getAcademicSessions() {
		sGlobalLock.readLock().lock();
		try {
			TreeSet<AcademicSessionInfo> ret = new TreeSet<AcademicSessionInfo>();
			for (OnlineSectioningServer s : sInstances.values())
				ret.add(s.getAcademicSession());
			return ret;
		} finally {
			sGlobalLock.readLock().unlock();
		}
	}
	
	public static void unload(Long academicSessionId) {
		sGlobalLock.writeLock().lock();
		try {
			OnlineSectioningServerUpdater u = sUpdaters.get(academicSessionId);
			if (u != null)
				u.stopUpdating();
			OnlineSectioningServer s = sInstances.get(academicSessionId);
			if (s != null)
				s.unload();
			sInstances.remove(academicSessionId);
			sUpdaters.remove(academicSessionId);
		} finally {
			sGlobalLock.writeLock().unlock();
		}
	}
	
	public static void stopService() {
		sLog.info("Student Sectioning Service is going down ...");
		sUpdater.stopUpdating();
		sGlobalLock.writeLock().lock();
		try {
			for (OnlineSectioningServerUpdater u: sUpdaters.values()) {
				u.stopUpdating();
				if (u.getAcademicSession() != null) {
					OnlineSectioningServer s = sInstances.get(u.getAcademicSession().getUniqueId());
					if (s != null) s.unload();
				}
			}
			sInstances.clear();
		} finally {
			sGlobalLock.writeLock().unlock();
		}
		OnlineSectioningLogger.stopLogger();
	}
}
