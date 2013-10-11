/*
 * UniTime 3.5 (University Timetabling Application)
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
package org.unitime.timetable.solver.jgroups;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.StudentSectioningQueue;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.StudentSectioningQueueDAO;
import org.unitime.timetable.onlinesectioning.AcademicSessionInfo;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.ServerCallback;
import org.unitime.timetable.onlinesectioning.updates.ClassAssignmentChanged;
import org.unitime.timetable.onlinesectioning.updates.ExpireReservationsAction;
import org.unitime.timetable.onlinesectioning.updates.PersistExpectedSpacesAction;
import org.unitime.timetable.onlinesectioning.updates.ReloadAllStudents;
import org.unitime.timetable.onlinesectioning.updates.ReloadOfferingAction;
import org.unitime.timetable.onlinesectioning.updates.ReloadStudent;

public class OnlineStudentSchedulingUpdater extends Thread {
	private Logger iLog;
	private long iSleepTimeInSeconds = 5;
	private Date iLastTimeStamp;
	private boolean iRun = true;
	private Long iLastReservationCheck = null;
	
	private OnlineStudentSchedulingContainer iContainer = null;
	private AcademicSessionInfo iSession = null; 
	
	public OnlineStudentSchedulingUpdater(OnlineStudentSchedulingContainer container, AcademicSessionInfo session, Date lastTimeStamp) {
		super();
		iContainer = container;
		iLastTimeStamp = lastTimeStamp;
		iSession = session;
		setDaemon(true);
		setName("Updater[" + getAcademicSession().toCompactString() + "]");
		iSleepTimeInSeconds = Long.parseLong(ApplicationProperties.getProperty("unitime.sectioning.queue.updateInterval", "30"));
		iLog = Logger.getLogger(OnlineStudentSchedulingUpdater.class + ".updater[" + getAcademicSession().toCompactString() + "]"); 
	}
	
	public void run() {
		try {
			iLog.info((getAcademicSession() == null ? "Generic" : getAcademicSession().toString()) + " updater started.");
			if (getAcademicSession() != null)
				ApplicationProperties.setSessionId(getAcademicSession().getUniqueId());
			while (iRun) {
				checkForUpdates();
				checkForExpiredReservations();
				persistExpectedSpaces();
				try {
					sleep(iSleepTimeInSeconds * 1000);
				} catch (InterruptedException e) {}
			}
			iLog.info((getAcademicSession() == null ? "Generic" : getAcademicSession().toString()) + " updater stopped.");
		} catch (Exception e) {
			iLog.error((getAcademicSession() == null ? "Generic" : getAcademicSession().toString()) + " updater failed, " + e.getMessage(), e);
		} finally {
			ApplicationProperties.setSessionId(null);
		}
	}
	
	public AcademicSessionInfo getAcademicSession() {
		return iSession;
	}
	
	public OnlineSectioningServer getServer() {
		if (getAcademicSession() == null) return null;
		return iContainer.getInstance(getAcademicSession().getUniqueId());
	}
	
	public void checkForUpdates() {
		try {
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
		} catch (Exception e) {
			iLog.error("Unable to check for updates: " + e.getMessage(), e);
		}
	}
	
	public void scanForNewServers() {
		try {
			
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
		} catch (Exception e) {
			iLog.error("Unable to check for updates: " + e.getMessage(), e);
		}
	}
	
	public void checkForExpiredReservations() {
		if (getAcademicSession() == null) return; // no work for general updater
		long ts = System.currentTimeMillis(); // current time stamp
		// the check was done within the last hour -> no need to repeat
		if (iLastReservationCheck != null && ts - iLastReservationCheck < 3600000) return;
		
		if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) == 0) {
			// first time after midnight (TODO: allow change)
			OnlineSectioningServer server = iContainer.getInstance(getAcademicSession().getUniqueId());
			if (server != null) {
				iLastReservationCheck = ts;
				try {
					server.execute(new ExpireReservationsAction(), user());
				} catch (Exception e) {
					iLog.error("Expire reservations failed: " + e.getMessage(), e);
				}
			}
		}
	}
	
	protected OnlineSectioningLog.Entity user() {
		return OnlineSectioningLog.Entity.newBuilder()
			.setExternalId(StudentClassEnrollment.SystemChange.SYSTEM.name())
			.setName(StudentClassEnrollment.SystemChange.SYSTEM.getName())
			.setType(OnlineSectioningLog.Entity.EntityType.OTHER).build();
	}
	
	public void persistExpectedSpaces() {
		if (getAcademicSession() == null) return; // no work for general updater
		
		OnlineSectioningServer server = iContainer.getInstance(getAcademicSession().getUniqueId());
		if (server != null) {
			try {
				List<Long> offeringIds = server.getOfferingsToPersistExpectedSpaces(2000 * iSleepTimeInSeconds);
				if (!offeringIds.isEmpty()) {
					server.execute(new PersistExpectedSpacesAction(offeringIds), user(), new ServerCallback<Boolean>() {
						@Override
						public void onSuccess(Boolean result) {}
						@Override
						public void onFailure(Throwable exception) {
							iLog.error("Failed to persist expected spaces: " + exception.getMessage(), exception);
						}
					});
				}
			} catch (Exception e) {
				iLog.error("Failed to persist expected spaces: " + e.getMessage(), e);
			}
		}
	}
	
	protected void processChange(StudentSectioningQueue q) {
		OnlineSectioningServer server = iContainer.getInstance(q.getSessionId());
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
					server.execute(new ReloadAllStudents(), q.getUser());
				} else {
					server.execute(new ReloadStudent(studentIds), q.getUser());
				}
			}
			break;
		case CLASS_ASSIGNMENT_CHANGE:
			if (server != null) {
				server.execute(new ClassAssignmentChanged(q.getIds()), q.getUser());
			}
			break;
		case OFFERING_CHANGE:
			if (server != null) {
				server.execute(new ReloadOfferingAction(q.getIds()), q.getUser());
			}
			break;
		default:
			iLog.error("Student sectioning queue type " + StudentSectioningQueue.Type.values()[q.getType()] + " not known.");
		}
	}
	
	protected void processGenericChange(StudentSectioningQueue q) {
		if (iContainer.getInstance(q.getSessionId()) != null)
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
		String campus = ApplicationProperties.getProperty("unitime.enrollment.campus");
		try {
			final Session session = SessionDAO.getInstance().get(academicSessionId, hibSession);
			
			OnlineSectioningServer server = iContainer.getInstance(academicSessionId);
			
			if (session == null) {
				if (server != null) {
					iLog.info("Unloading " + server.getAcademicSession());
					iContainer.unload(server.getAcademicSession().getUniqueId());
				}
				return;
			}
			iLog.info("Session status changed for " + session.getLabel());
			
			boolean load = true;
			if (year != null && !year.equals(session.getAcademicYear())) load = false;
			if (term != null && !term.equals(session.getAcademicTerm())) load = false;
			if (campus != null && !campus.equals(session.getAcademicInitiative())) load = false;
			if (session.getStatusType().isTestSession()) load = false;
			if (!session.getStatusType().canSectionAssistStudents() && !session.getStatusType().canOnlineSectionStudents()) load = false;

			if ((!load || reload) && server != null) {
				iLog.info("Unloading " + server.getAcademicSession());
				iContainer.unload(server.getAcademicSession().getUniqueId());
				server = null;
			}
			
			if (!load) return;
			
			if (server == null) {
				iContainer.createInstance(academicSessionId, null);
			} else {
				if (server.getAcademicSession().isSectioningEnabled() && !session.getStatusType().canOnlineSectionStudents())
					server.releaseAllOfferingLocks();
				server.getAcademicSession().setSectioningEnabled(session.getStatusType().canOnlineSectionStudents());
				server.getAcademicSession().update(session);
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