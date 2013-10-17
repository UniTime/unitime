/*
 * UniTime 3.4 - 3.5 (University Timetabling Application)
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
package org.unitime.timetable.events;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Transaction;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.EventNote;
import org.unitime.timetable.model.Meeting;
import org.unitime.timetable.model.dao.EventDAO;

/**
 * @author Tomas Muller
 */
public class EventExpirationService extends Thread {
	protected static GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	private static Logger sLog = Logger.getLogger(EventExpirationService.class); 
	private long iSleepTimeInMinutes = 5;
	private boolean iActive = true;
	private static EventExpirationService sInstance;
	private Long iLastExpirationCheck = null;
	
	private EventExpirationService() {
		setName("EventExpirationService");
		setDaemon(true);
		iSleepTimeInMinutes = Long.parseLong(ApplicationProperties.getProperty("unitime.events.expiration.updateIntervalInMinutes", "5"));
	}
	
	protected void checkForExpiredEventsIfNeeded() {
		long ts = System.currentTimeMillis(); // current time stamp
		// the check was done within the last hour -> no need to repeat
		if (iLastExpirationCheck != null && ts - iLastExpirationCheck < 3600000) return;
		
		if (iLastExpirationCheck == null || Calendar.getInstance().get(Calendar.HOUR_OF_DAY) == 0) {
			// first time after midnight or when executed for the first time (TODO: allow change)
			try {
				sLog.info("Checking for expired events ...");
				checkForExpiredEvents();
				iLastExpirationCheck = ts;
			} catch (Exception e) {
				sLog.error("Expired events check failed: " + e.getMessage(), e);
			}
		}
	}
	
	protected void checkForExpiredEvents() throws Exception {
		org.hibernate.Session hibSession = EventDAO.getInstance().getSession();
		try {
			Transaction tx = hibSession.beginTransaction();
			try {
				Date now = new Date();
				for (Event event: (List<Event>)hibSession.createQuery(
						"select distinct e from Event e inner join e.meetings m " +
						"where e.expirationDate is not null and m.approvalStatus = 0 and e.expirationDate < " + HibernateUtil.date(new Date())).list()) {
					
					Set<Meeting> affectedMeetings = new HashSet<Meeting>();
					String affectedMeetingStr = "";
			        for (Meeting meeting: event.getMeetings()) {
			        	if (meeting.getStatus() == Meeting.Status.PENDING) {
			        		meeting.setStatus(Meeting.Status.CANCELLED);
							meeting.setApprovalDate(now);
							hibSession.saveOrUpdate(meeting);
							affectedMeetings.add(meeting);
							if (!affectedMeetingStr.isEmpty()) affectedMeetingStr += "<br>";
							affectedMeetingStr += meeting.getTimeLabel() + (meeting.getLocation() == null ? "" : " " + meeting.getLocation().getLabel());
			        	}
			        }

					EventNote note = new EventNote();
					note.setEvent(event);
					note.setNoteType(EventNote.sEventNoteTypeCancel);
					note.setTimeStamp(now);
					note.setUser("SYSTEM");
					note.setAffectedMeetings(affectedMeetings);
					note.setMeetings(affectedMeetingStr);
					note.setTextNote(MESSAGES.noteEventExpired());

					event.getNotes().add(note);
					hibSession.saveOrUpdate(note);
				
					hibSession.update(event);
					
					try {
						EventEmail.eventExpired(event, affectedMeetings);
					} catch (Exception e) {
						sLog.warn("Failed to sent notification for " + event.getEventName(), e);
					}
				}
				
				tx.commit(); tx = null;
			} catch (Exception e) {
				if (tx != null) tx.rollback();
				sLog.error("Failed to expire some events: " + e.getMessage(), e);
				throw e;
			}
		} finally {
			hibSession.close();
		}
	}
	
	public static EventExpirationService getInstance() {
		if (sInstance == null)
			sInstance = new EventExpirationService();
		return sInstance;
	}
	
	@Override
	public void run() {
		try {
			sLog.info("Event expiration service started.");
			while (iActive) {
				checkForExpiredEventsIfNeeded();
				try {
					sleep(iSleepTimeInMinutes * 60000);
				} catch (InterruptedException e) {}
			}
			sLog.info("Event expiration service stopped.");
		} catch (Exception e) {
			sLog.info("Event expiration service failed, " + e.getMessage(), e);
		}
	}

	@Override
	public void interrupt() {
		iActive = false;
		super.interrupt();
		try { join(); } catch (InterruptedException e) {}
	}
}
