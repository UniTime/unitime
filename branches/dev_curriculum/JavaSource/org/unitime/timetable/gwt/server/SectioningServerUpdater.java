/*
 * UniTime 4.0 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.gwt.server;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.StudentSectioningQueue;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.StudentSectioningQueueDAO;

public class SectioningServerUpdater extends Thread {
	private static Logger sLog = Logger.getLogger(SectioningServerUpdater.class);
	private long iSleepTimeInSeconds = 5;
	private Date iLastTimeStamp;
	private boolean iRun = true;
	
	private AcademicSessionInfo iSession = null; 
	
	public SectioningServerUpdater(AcademicSessionInfo session, Date lastTimeStamp) {
		super();
		iLastTimeStamp = lastTimeStamp;
		iSession = session;
		setDaemon(true);
		setName("Updater[" + getAcademicSession().toCompactString() + "]");
		iSleepTimeInSeconds = Long.parseLong(ApplicationProperties.getProperty("unitime.sectioning.queue.updateInterval", "30"));
	}
	
	public SectioningServerUpdater(Date lastTimeStamp) {
		super();
		iLastTimeStamp = lastTimeStamp;
		iSession = null;
		setDaemon(true);
		setName("Updater[generic]");
		iSleepTimeInSeconds = Long.parseLong(ApplicationProperties.getProperty("unitime.sectioning.queue.loadInterval", "600"));
	}
	
	public void run() {
		try {
			sLog.info((getAcademicSession() == null ? "Generic" : getAcademicSession().toString()) + " updater started.");
			while (iRun) {
				checkForUpdates();
				try {
					sleep(iSleepTimeInSeconds * 1000);
				} catch (InterruptedException e) {}
			}
			sLog.info((getAcademicSession() == null ? "Generic" : getAcademicSession().toString()) + " updater stopped.");
		} catch (Exception e) {
			sLog.error((getAcademicSession() == null ? "Generic" : getAcademicSession().toString()) + " updater failed, " + e.getMessage(), e);
		}
	}
	
	public AcademicSessionInfo getAcademicSession() {
		return iSession;
	}
	
	public SectioningServer getServer() {
		if (getAcademicSession() == null) return null;
		return SectioningServer.getInstance(getAcademicSession().getUniqueId());
	}
	

	
	public void checkForUpdates() {
		org.hibernate.Session hibSession = StudentSectioningQueueDAO.getInstance().createNewSession();
		try {
			if (getAcademicSession() != null) {
				for (StudentSectioningQueue q: StudentSectioningQueue.getItems(hibSession, getAcademicSession().getUniqueId(), iLastTimeStamp)) {
					processChange(q);
					iLastTimeStamp = q.getTimeStamp();
				}
			} else {
				for (StudentSectioningQueue q: StudentSectioningQueue.getItems(hibSession, null, iLastTimeStamp)) {
					processGenericChange(q);
					iLastTimeStamp = q.getTimeStamp();
				}
			}
		} finally {
			hibSession.close();
		}
	}
	
	protected void processChange(StudentSectioningQueue q) {
		SectioningServer server = SectioningServer.getInstance(q.getSessionId());
		switch (StudentSectioningQueue.Type.values()[q.getType()]) {
		case SESSION_RELOAD:
			sessionStatusChanged(q.getSessionId(), true);
			break;
		case SESSION_STATUS_CHANGE:
			sessionStatusChanged(q.getSessionId(), false);
			break;
		case STUDENT_ENROLLMENT_CHANGE:
			if (server != null) {
				List<Long> studentIds = q.getIds();
				if (studentIds == null || studentIds.isEmpty()) {
					sLog.info("All students changed for " + server.getAcademicSession());
					server.allStudentsChanged();
				} else {
					server.studentChanged(studentIds);
				}
			}
			break;
		case CLASS_ASSIGNMENT_CHANGE:
			if (server != null) {
				server.classChanged(q.getIds());
			}
			break;
		default:
			sLog.error("Student sectioning queue type " + StudentSectioningQueue.Type.values()[q.getType()] + " not known.");
		}
	}
	
	protected void processGenericChange(StudentSectioningQueue q) {
		if (SectioningServer.getInstance(q.getSessionId()) != null)
			return; // a server already exists
		
		// only process events that may load the server
		switch (StudentSectioningQueue.Type.values()[q.getType()]) {
		case SESSION_RELOAD:
			sessionStatusChanged(q.getSessionId(), true);
			break;
		case SESSION_STATUS_CHANGE:
			sessionStatusChanged(q.getSessionId(), false);
			break;
		}
	}
	
	private static void sessionStatusChanged(Long academicSessionId, boolean reload) {
		org.hibernate.Session hibSession = SessionDAO.getInstance().createNewSession();
		String year = ApplicationProperties.getProperty("unitime.enrollment.year");
		String term = ApplicationProperties.getProperty("unitime.enrollment.term");
		try {
			final Session session = SessionDAO.getInstance().get(academicSessionId, hibSession);
			
			SectioningServer server = SectioningServer.getInstance(academicSessionId);
			
			if (session == null) {
				if (server != null) {
					sLog.info("Unloading " + server.getAcademicSession());
					server.unload();
				}
				return;
			}
			sLog.info("Session status changed for " + session.getLabel());
			
			boolean load = true;
			if (year != null && !year.equals(session.getAcademicYear())) load = false;
			if (term != null && !term.equals(session.getAcademicTerm())) load = false;
			if (!session.getStatusType().canSectioningStudents()) load = false;

			if (!load) {
				if (server != null) {
					sLog.info("Unloading " + server.getAcademicSession());
					server.unload();
				}
				return;
			}
			
			if (server == null || reload)
				SectioningServer.createInstance(academicSessionId);
						
		} finally {
			hibSession.close();
		}
	}
	
	public void stopUpdating() {
		iRun = false;
		interrupt();
	}
}
