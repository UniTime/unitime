/*
 * UniTime 3.4 (University Timetabling Application)
 * Copyright (C) 2012, UniTime LLC, and individual contributors
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
package org.unitime.timetable.security.permissions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Meeting;
import org.unitime.timetable.model.RoomDept;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Solution;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.rights.Right;

public class EventPermissions {
	
	@PermissionForRight(Right.PersonalSchedule)
	public static class PersonalSchedule implements Permission<Session> {
		@Autowired PermissionSession permissionSession;

		@Override
		public boolean check(UserContext user, Session source) {
			return
				(permissionSession.check(user, source, DepartmentStatusType.Status.ReportClasses) && Solution.hasTimetable(source.getSessionId())) ||
				(permissionSession.check(user, source, DepartmentStatusType.Status.ReportExamsFinal) && Exam.hasTimetable(source.getUniqueId(), ExamType.sExamTypeFinal)) ||
				(permissionSession.check(user, source, DepartmentStatusType.Status.ReportExamsMidterm) && Exam.hasTimetable(source.getUniqueId(), ExamType.sExamTypeMidterm));
		}

		@Override
		public Class<Session> type() { return Session.class; }
		
	}
	
	@PermissionForRight(Right.PersonalScheduleLookup)
	public static class PersonalScheduleLookup extends PersonalSchedule {}
	
	protected static abstract class EventPermission<T> implements Permission<T> {
		@Autowired PermissionSession permissionSession;
		
		protected Date today() {
			Calendar cal = Calendar.getInstance(Localization.getJavaLocale());
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			return cal.getTime();
		}
		
		protected Date begin(Session session) {
			return session.getEventBeginDate();
		}
		
		protected Date end(Session session) {
			Calendar cal = Calendar.getInstance(Localization.getJavaLocale());
			cal.setTime(session.getEventEndDate());
			cal.add(Calendar.DAY_OF_YEAR, 1);
			return cal.getTime();
		}
		
		protected boolean isOutside(Date date, Session session) {
			return date == null || date.before(begin(session)) || !date.before(end(session));
		}
		
		protected boolean isPast(Date date) {
			return date == null || date.before(today());
		}
		
		protected List<Long> locations(Long sessionId) {
			if (sessionId == null) return new ArrayList<Long>();
			return (List<Long>) SessionDAO.getInstance().getSession().createQuery(
					"select l.uniqueId " +
					"from Location l, RoomTypeOption o " +
					"where l.eventDepartment.allowEvents = true and o.status = 1 and o.roomType = l.roomType and o.session = l.session and l.session.uniqueId = :sessionId")
					.setLong("sessionId", sessionId).setCacheable(true).list();
		}
	}
	
	@PermissionForRight(Right.Events)
	public static class Events extends EventPermission<Session> {
		@Override
		public boolean check(UserContext user, Session source) {
			return source.getStatusType().canNoRoleReport() || (user.getCurrentAuthority().hasRight(Right.EventAnyLocation) || !locations(source.getUniqueId()).isEmpty());
		}
		
		@Override
		public Class<Session> type() { return Session.class; }
	}

	@PermissionForRight(Right.EventAddSpecial)
	public static class EventAddSpecial extends EventPermission<Session> {
		@Override
		public boolean check(UserContext user, Session source) {
			return (!isPast(end(source)) || user.getCurrentAuthority().hasRight(Right.EventEditPast)) &&
					(user.getCurrentAuthority().hasRight(Right.EventAnyLocation) || !locations(source.getUniqueId()).isEmpty());
		}
		
		@Override
		public Class<Session> type() { return Session.class; }
	}
	
	@PermissionForRight(Right.EventAddCourseRelated)
	public static class EventAddCourseRelated extends EventAddSpecial { }
	
	@PermissionForRight(Right.EventDetail)
	public static class EventDetail implements Permission<Event> {
		
		@Override
		public boolean check(UserContext user, Event source) {
			return user.getCurrentAuthority().hasRight(Right.EventLookupContact) || user.getExternalUserId().equals(source.getMainContact().getExternalUniqueId());
		}
		
		@Override
		public Class<Event> type() { return Event.class; }

	}
	
	@PermissionForRight(Right.EventEdit)
	public static class EventEdit extends EventPermission<Event> {
		@Autowired PermissionSession permissionSession;
		@Autowired Permission<Date> permissionEventDate;
		
		@Override
		public boolean check(UserContext user, Event source) {
			// Examination and class events cannot be edited just yet
			switch (source.getEventType()) {
			case Event.sEventTypeClass:
			case Event.sEventTypeFinalExam:
			case Event.sEventTypeMidtermExam:
				return false;
			}
			
			// Must be the owner or an event admin
			if (!user.getCurrentAuthority().hasRight(Right.EventLookupContact) && !user.getExternalUserId().equals(source.getMainContact().getExternalUniqueId()))
				return false;
			
			// Check academic session
			Session session = source.getSession();
			if (session != null) {
				return permissionSession.check(user, session);
			} else {
				boolean noLocation = true, hasDate = false;
				for (Meeting meeting: source.getMeetings()) {
					Location location = meeting.getLocation();
					if (location != null) {
						noLocation = false;
						if (permissionSession.check(user, location.getSession())) return true;
					} else {
						if (!hasDate && permissionEventDate.check(user, meeting.getMeetingDate()))
							hasDate = true;
					}
				}
				return noLocation && hasDate;
			}
		}

		@Override
		public Class<Event> type() { return Event.class; }

	}
	
	@PermissionForRight(Right.EventDate)
	public static class EventDate extends EventPermission<Date> {

		@Override
		public boolean check(UserContext user, Date source) {
			return (!isPast(source) || user.getCurrentAuthority().hasRight(Right.EventEditPast)) &&
					!isOutside(source, SessionDAO.getInstance().get(user.getCurrentAcademicSessionId()));
		}
		
		@Override
		public Class<Date> type() { return Date.class; }
	}
	
	@PermissionForRight(Right.EventLocation)
	public static class EventLocation extends EventPermission<Location> {
		@Override
		public boolean check(UserContext user, Location source) {
			return source == null || user.getCurrentAuthority().hasRight(Right.EventAnyLocation) || locations(user.getCurrentAcademicSessionId()).contains(source.getUniqueId());
		}
		
		@Override
		public Class<Location> type() { return Location.class; }
	}
	
	@PermissionForRight(Right.EventLocationApprove)
	public static class EventLocationApprove extends EventPermission<Location> {
		@Autowired Permission<Location> permissionEventLocation;
		@Autowired PermissionDepartment permissionDepartment;
		
		@Override
		public boolean check(UserContext user, Location source) {
			if (source == null) return true;
			
			if (!permissionEventLocation.check(user, source)) return false;
			
			if (user.getCurrentAuthority().hasRight(Right.EventAnyLocation)) return true;
			
			for (RoomDept rd: source.getRoomDepts())
				if (permissionDepartment.check(user, rd.getDepartment())) return true;

			return false;
		}
		
		@Override
		public Class<Location> type() { return Location.class; }
	}

	@PermissionForRight(Right.EventLocationOverbook)
	public static class EventLocationOverbook extends EventLocationApprove { }
	
	@PermissionForRight(Right.EventMeetingEdit)
	public static class EventMeetingEdit extends EventPermission<Meeting> {
		@Autowired Permission<Event> permissionEventEdit;
		@Autowired Permission<Date> permissionEventDate;
		@Autowired Permission<Location> permissionEventLocation;

		@Override
		public boolean check(UserContext user, Meeting source) {
			return  permissionEventEdit.check(user, source.getEvent()) &&
					permissionEventDate.check(user, source.getMeetingDate()) &&
					permissionEventLocation.check(user, source.getLocation());
		}
		
		@Override
		public Class<Meeting> type() { return Meeting.class; }
	}
	
	@PermissionForRight(Right.EventMeetingApprove)
	public static class EventMeetingApprove extends EventPermission<Meeting> {
		@Autowired Permission<Event> permissionEventEdit;
		@Autowired Permission<Location> permissionEventLocationApprove;
		
		@Override
		public boolean check(UserContext user, Meeting source) {
			return permissionEventEdit.check(user, source.getEvent()) &&
					!isOutside(source.getMeetingDate(), SessionDAO.getInstance().get(user.getCurrentAcademicSessionId())) &&
					(user.getCurrentAuthority().hasRight(Right.EventApprovePast) || !isPast(source.getMeetingDate())) &&
					permissionEventLocationApprove.check(user, source.getLocation());
		}
		
		@Override
		public Class<Meeting> type() { return Meeting.class; }
	}
	
}
