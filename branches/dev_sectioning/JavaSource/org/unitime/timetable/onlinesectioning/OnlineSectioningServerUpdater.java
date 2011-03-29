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
package org.unitime.timetable.onlinesectioning;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.StudentSectioningQueue;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.StudentSectioningQueueDAO;
import org.unitime.timetable.onlinesectioning.updates.ClassAssignmentChanged;
import org.unitime.timetable.onlinesectioning.updates.ReloadAllStudents;
import org.unitime.timetable.onlinesectioning.updates.ReloadOfferingAction;
import org.unitime.timetable.onlinesectioning.updates.ReloadStudent;

/**
 * @author Tomas Muller
 */
public class OnlineSectioningServerUpdater extends Thread {
	private Logger iLog;
	private long iSleepTimeInSeconds = 5;
	private Date iLastTimeStamp;
	private boolean iRun = true;
	
	private AcademicSessionInfo iSession = null; 
	
	public OnlineSectioningServerUpdater(AcademicSessionInfo session, Date lastTimeStamp) {
		super();
		iLastTimeStamp = lastTimeStamp;
		iSession = session;
		setDaemon(true);
		setName("Updater[" + getAcademicSession().toCompactString() + "]");
		iSleepTimeInSeconds = Long.parseLong(ApplicationProperties.getProperty("unitime.sectioning.queue.updateInterval", "30"));
		iLog = Logger.getLogger(OnlineSectioningServerUpdater.class + ".updater[" + getAcademicSession().toCompactString() + "]"); 
	}
	
	public OnlineSectioningServerUpdater(Date lastTimeStamp) {
		super();
		iLastTimeStamp = lastTimeStamp;
		iSession = null;
		setDaemon(true);
		setName("Updater[generic]");
		iSleepTimeInSeconds = Long.parseLong(ApplicationProperties.getProperty("unitime.sectioning.queue.loadInterval", "600"));
		iLog = Logger.getLogger(OnlineSectioningServerUpdater.class + ".updater[generic]"); 
	}
	
	public void run() {
		try {
			iLog.info((getAcademicSession() == null ? "Generic" : getAcademicSession().toString()) + " updater started.");
			while (iRun) {
				checkForUpdates();
				try {
					sleep(iSleepTimeInSeconds * 1000);
				} catch (InterruptedException e) {}
			}
			iLog.info((getAcademicSession() == null ? "Generic" : getAcademicSession().toString()) + " updater stopped.");
		} catch (Exception e) {
			iLog.error((getAcademicSession() == null ? "Generic" : getAcademicSession().toString()) + " updater failed, " + e.getMessage(), e);
		}
	}
	
	public AcademicSessionInfo getAcademicSession() {
		return iSession;
	}
	
	public OnlineSectioningServer getServer() {
		if (getAcademicSession() == null) return null;
		return OnlineSectioningService.getInstance(getAcademicSession().getUniqueId());
	}
	
	public void checkForUpdates() {
		org.hibernate.Session hibSession = StudentSectioningQueueDAO.getInstance().createNewSession();
		try {
			if (getAcademicSession() != null) {
				for (StudentSectioningQueue q: StudentSectioningQueue.getItems(hibSession, getAcademicSession().getUniqueId(), iLastTimeStamp)) {
					try {
						processChange(q);
					} catch (Exception e) {
						iLog.error("Update failed: " + e.getMessage(), e);
					}
					iLastTimeStamp = q.getTimeStamp();
				}
			} else {
				for (StudentSectioningQueue q: StudentSectioningQueue.getItems(hibSession, null, iLastTimeStamp)) {
					try {
						processGenericChange(q);
					} catch (Exception e) {
						iLog.error("Update failed: " + e.getMessage(), e);
					}
					iLastTimeStamp = q.getTimeStamp();
				}
			}
		} finally {
			hibSession.close();
		}
	}
	
	protected void processChange(StudentSectioningQueue q) {
		OnlineSectioningServer server = OnlineSectioningService.getInstance(q.getSessionId());
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
					iLog.info("All students changed for " + server.getAcademicSession());
					server.execute(new ReloadAllStudents());
				} else {
					server.execute(new ReloadStudent(studentIds));
				}
			}
			break;
		case CLASS_ASSIGNMENT_CHANGE:
			if (server != null) {
				server.execute(new ClassAssignmentChanged(q.getIds()));
			}
			break;
		case OFFERING_CHANGE:
			if (server != null) {
				server.execute(new ReloadOfferingAction(q.getIds()));
			}
			break;
		default:
			iLog.error("Student sectioning queue type " + StudentSectioningQueue.Type.values()[q.getType()] + " not known.");
		}
	}
	
	protected void processGenericChange(StudentSectioningQueue q) {
		if (OnlineSectioningService.getInstance(q.getSessionId()) != null)
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
	
	private void sessionStatusChanged(Long academicSessionId, boolean reload) {
		org.hibernate.Session hibSession = SessionDAO.getInstance().createNewSession();
		String year = ApplicationProperties.getProperty("unitime.enrollment.year");
		String term = ApplicationProperties.getProperty("unitime.enrollment.term");
		try {
			final Session session = SessionDAO.getInstance().get(academicSessionId, hibSession);
			
			OnlineSectioningServer server = OnlineSectioningService.getInstance(academicSessionId);
			
			if (session == null) {
				if (server != null) {
					iLog.info("Unloading " + server.getAcademicSession());
					OnlineSectioningService.unload(server.getAcademicSession().getUniqueId());
				}
				return;
			}
			iLog.info("Session status changed for " + session.getLabel());
			
			boolean load = true;
			if (year != null && !year.equals(session.getAcademicYear())) load = false;
			if (term != null && !term.equals(session.getAcademicTerm())) load = false;
			if (!session.getStatusType().canSectionAssistStudents() && !session.getStatusType().canOnlineSectionStudents()) load = false;

			if ((!load || reload) && server != null) {
				iLog.info("Unloading " + server.getAcademicSession());
				OnlineSectioningService.unload(server.getAcademicSession().getUniqueId());
				server = null;
			}

			if (!load) return;
			
			if (server == null) { 
				OnlineSectioningService.createInstance(academicSessionId);
			} else {
				if (server.getAcademicSession().isSectioningEnabled() && !session.getStatusType().canOnlineSectionStudents())
					server.releaseAllOfferingLocks();
				server.getAcademicSession().setSectioningEnabled(session.getStatusType().canOnlineSectionStudents());
			}
		} finally {
			hibSession.close();
		}
	}
	
	public void stopUpdating() {
		iRun = false;
		interrupt();
	}
}
