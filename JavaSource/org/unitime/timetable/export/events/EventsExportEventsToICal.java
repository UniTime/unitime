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
package org.unitime.timetable.export.events;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.springframework.stereotype.Service;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.export.ExportHelper;
import org.unitime.timetable.gwt.client.events.EventComparator.EventMeetingSortBy;
import org.unitime.timetable.gwt.shared.EventInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ApprovalStatus;
import org.unitime.timetable.gwt.shared.EventInterface.ContactInterface;
import org.unitime.timetable.gwt.shared.EventInterface.EventLookupRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.EventType;
import org.unitime.timetable.gwt.shared.EventInterface.MeetingInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ResourceType;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.Curriculum;
import org.unitime.timetable.model.CurriculumClassification;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.StudentGroup;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.CurriculumClassificationDAO;
import org.unitime.timetable.model.dao.CurriculumDAO;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.model.dao.LocationDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.StudentGroupDAO;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.util.Constants;

import biweekly.ICalVersion;
import biweekly.ICalendar;
import biweekly.component.VEvent;
import biweekly.io.text.ICalWriter;
import biweekly.parameter.Role;
import biweekly.property.Attendee;
import biweekly.property.CalendarScale;
import biweekly.property.DateEnd;
import biweekly.property.DateStart;
import biweekly.property.DateTimeStamp;
import biweekly.property.ExceptionDates;
import biweekly.property.Method;
import biweekly.property.Organizer;
import biweekly.property.RecurrenceId;
import biweekly.property.Status;
import biweekly.util.Recurrence;
import biweekly.util.Recurrence.DayOfWeek;
import biweekly.util.Recurrence.Frequency;

/**
 * @author Tomas Muller
 */
@Service("org.unitime.timetable.export.Exporter:events.ics")
public class EventsExportEventsToICal extends EventsExporter {
	
	@Override
	public String reference() {
		return "events.ics";
	}
	
	@Override
	protected void print(ExportHelper helper, EventLookupRpcRequest request, List<EventInterface> events, int eventCookieFlags, EventMeetingSortBy sort, boolean asc) throws IOException {
		helper.setup("text/calendar", reference(), false);
		
		ICalendar ical = new ICalendar();
		ical.setVersion(ICalVersion.V2_0);
		ical.setCalendarScale(CalendarScale.gregorian());
		ical.setMethod(new Method("PUBLISH"));
		ical.setExperimentalProperty("X-WR-CALNAME", guessScheduleName(helper, request));
		ical.setExperimentalProperty("X-WR-TIMEZONE", TimeZone.getDefault().getID());
		ical.setProductId("-//UniTime LLC/UniTime " + Constants.getVersion() + " Events//EN");

        for (EventInterface event: events)
			print(ical, event);
		
        ICalWriter writer = new ICalWriter(helper.getWriter(), ICalVersion.V2_0);
    	writer.getTimezoneInfo().setDefaultTimeZone(TimeZone.getDefault());
        try {
        	writer.write(ical);
        	writer.flush();
        } finally {
        	writer.close();
        }
	}
	
	public boolean print(ICalendar ical, EventInterface event) throws IOException {
		return print(ical, event, null);
	}
	
	protected String guessScheduleName(ExportHelper helper, EventLookupRpcRequest request) {
		org.hibernate.Session hibSession = new _RootDAO().getSession();
		String name = MESSAGES.scheduleNameDefault();
		if (request.getResourceType() == ResourceType.PERSON) {
			name = MESSAGES.pagePersonalTimetable();
		} else if (helper.getParameter("name") != null) {
			name  = MESSAGES.scheduleNameForResource(Constants.toInitialCase(helper.getParameter("name")));
		} else {
			String resource = getResourceName(request, hibSession);
			if (resource != null)
				name = MESSAGES.scheduleNameForResource(resource);
			else if (request.getResourceType() != null && request.getResourceType() != ResourceType.ROOM)
				name = CONSTANTS.resourceType()[request.getResourceType().ordinal()];
		}
		boolean allSessions = request.getEventFilter().hasOption("flag") && request.getEventFilter().getOptions("flag").contains("All Sessions");
		if (!allSessions && request.getSessionId() != null) {
			Session session = SessionDAO.getInstance().get(request.getSessionId(), hibSession);
			name = MESSAGES.scheduleNameForSession(name, session.getAcademicTerm(), session.getAcademicYear());
		}
		return name;
	}

	public String getResourceName(EventLookupRpcRequest request, org.hibernate.Session hibSession) {
		if (request.getResourceType() != null && request.getResourceId() != null) {
			switch (request.getResourceType()) {
			case ROOM:
				Location location = LocationDAO.getInstance().get(request.getResourceId(), hibSession);
				if (location != null) return location.getDisplayName() == null ? location.getLabel() : location.getDisplayName();
				break;
			case SUBJECT:
				SubjectArea subject = SubjectAreaDAO.getInstance().get(request.getResourceId(), hibSession);
				if (subject != null) return subject.getSubjectAreaAbbreviation();
				break;
			case COURSE:
				CourseOffering course = CourseOfferingDAO.getInstance().get(request.getResourceId(), hibSession);
				if (course != null) return course.getCourseName();
				break;
			case CURRICULUM:
				Curriculum curriculum = CurriculumDAO.getInstance().get(request.getResourceId(), hibSession);
				if (curriculum != null) return (curriculum.getAbbv() == null ? curriculum.getAcademicArea().getAcademicAreaAbbreviation() : curriculum.getAbbv());
				CurriculumClassification clasf = CurriculumClassificationDAO.getInstance().get(request.getResourceId(), hibSession);
				if (clasf != null) return (clasf.getCurriculum().getAbbv() == null ? clasf.getCurriculum().getAcademicArea().getAcademicAreaAbbreviation() : clasf.getCurriculum().getAbbv()) +
						" " + (clasf.getName() == null ? clasf.getAcademicClassification().getCode() : clasf.getName());
				break;
			case DEPARTMENT:
				Department department = DepartmentDAO.getInstance().get(request.getResourceId(), hibSession);
				if (department != null) return department.getAbbreviation() == null ? department.getDeptCode() : department.getAbbreviation();
				break;
			case GROUP:
				StudentGroup group = StudentGroupDAO.getInstance().get(request.getResourceId(), hibSession);
				if (group != null) return group.getGroupAbbreviation();
				break;
			}
		}
		if (request.getEventFilter().hasOption("room")) {
			Location location = LocationDAO.getInstance().get(Long.valueOf(request.getEventFilter().getOption("room")), hibSession);
			if (location != null) return location.getDisplayName() == null ? location.getLabel() : location.getDisplayName();
		}
		return null;
	}
		
	
	public boolean print(ICalendar ical, EventInterface event, Status status) throws IOException {
		if (event.getType() == EventType.Unavailabile) return false;
		
        TreeSet<ICalendarMeeting> meetings = new TreeSet<ICalendarMeeting>();
        Set<Integer> days = new TreeSet<Integer>();
        if (event.hasMeetings())
            meetings: for (MeetingInterface m: event.getMeetings()) {
            	if (m.isArrangeHours()) continue;
            	if (m.getApprovalStatus() != ApprovalStatus.Approved && m.getApprovalStatus() != ApprovalStatus.Pending) continue;
                ICalendarMeeting x = new ICalendarMeeting(m, status);
                
                for (ICalendarMeeting icm: meetings)
                	if (icm.merge(x)) continue meetings;
                meetings.add(x); days.add(x.getStart().getDayOfWeek());
            }
        
        if (meetings.isEmpty()) return false;
        
        ICalendarMeeting first = meetings.first();
        
        VEvent master = new VEvent();
        master.setDateStart(first.getDateStart());
        master.setDateEnd(first.getDateEnd());
        master.setLocation(first.getLocation());
        master.setStatus(first.getStatus());
        List<VEvent> events = new ArrayList<VEvent>();
        events.add(master);
        
        if (meetings.size() > 1) {
            // last day of the recurrence
            DateTime until = new DateTime(meetings.last().getStart().getYear(), meetings.last().getStart().getMonthOfYear(), meetings.last().getStart().getDayOfMonth(),
            		first.getEnd().getHourOfDay(), first.getEnd().getMinuteOfHour(), first.getEnd().getSecondOfMinute());
            // count meeting days
            int nrMeetingDays = 0;
            for (DateTime date = first.getStart(); !date.isAfter(until); date = date.plusDays(1)) {
            	// skip days of week with no meeting
            	if (days.contains(date.getDayOfWeek())) nrMeetingDays ++;
            }
            // make sure that there is enough meeting days to cover all meetings
            while (nrMeetingDays < meetings.size()) {
            	until = until.plusDays(1);
            	if (days.contains(until.getDayOfWeek())) nrMeetingDays ++;
            }
            
            Recurrence.Builder recur = new Recurrence.Builder(Frequency.WEEKLY);
            for (Iterator<Integer> i = days.iterator(); i.hasNext(); ) {
            	switch (i.next()) {
            	case DateTimeConstants.MONDAY:
            		recur.byDay(DayOfWeek.MONDAY); break;
            	case DateTimeConstants.TUESDAY:
            		recur.byDay(DayOfWeek.TUESDAY); break;
            	case DateTimeConstants.WEDNESDAY:
            		recur.byDay(DayOfWeek.WEDNESDAY); break;
            	case DateTimeConstants.THURSDAY:
            		recur.byDay(DayOfWeek.THURSDAY); break;
            	case DateTimeConstants.FRIDAY:
            		recur.byDay(DayOfWeek.FRIDAY); break;
            	case DateTimeConstants.SATURDAY:
            		recur.byDay(DayOfWeek.SATURDAY); break;
            	case DateTimeConstants.SUNDAY:
            		recur.byDay(DayOfWeek.SUNDAY); break;
            	}
            }
            recur.workweekStarts(DayOfWeek.MONDAY).until(until.toDate());
            master.setRecurrenceRule(recur.build());
            
            ExceptionDates exdates = new ExceptionDates();
            // for all dates till the last date
            dates: for (DateTime date = first.getStart(); !date.isAfter(until); date = date.plusDays(1)) {
            	// skip days of week with no meeting
            	if (!days.contains(date.getDayOfWeek())) continue;
            	// try to find a fully matching meeting
            	for (Iterator<ICalendarMeeting> i = meetings.iterator(); i.hasNext();) {
            		ICalendarMeeting ics = i.next();
            		if (date.getYear() == ics.getStart().getYear() && date.getDayOfYear() == ics.getStart().getDayOfYear() && first.same(ics)) {
            			i.remove();
            			continue dates;
            		}
            	}
            	// try to find a meeting that is on the same day
            	for (Iterator<ICalendarMeeting> i = meetings.iterator(); i.hasNext();) {
            		ICalendarMeeting ics = i.next();
            		if (date.getYear() == ics.getStart().getYear() && date.getDayOfYear() == ics.getStart().getDayOfYear()) {
        				VEvent x = new VEvent();
        				RecurrenceId id = new RecurrenceId(date.toDate(), true);
        				x.setRecurrenceId(id);
        				x.setDateStart(ics.getDateStart());
        				x.setDateEnd(ics.getDateEnd());
        				x.setLocation(ics.getLocation());
        				x.setStatus(ics.getStatus());
        				events.add(x);
        				i.remove();
            			continue dates;
            		}
            	}
            	// add exception
            	exdates.addValue(date.toDate());
            }
            // process remaining meetings
            for (ICalendarMeeting ics: meetings) {
    			VEvent x = new VEvent();
    			x.setDateStart(ics.getDateStart());
    			x.setDateEnd(ics.getDateEnd());
    			x.setLocation(ics.getLocation());
    			x.setStatus(ics.getStatus());
    			// use exception as recurrence if there is one available
    			if (!exdates.getValues().isEmpty()) {
    				RecurrenceId id = new RecurrenceId(exdates.getValues().get(0), true);
    				x.setRecurrenceId(id);
    				exdates.getValues().remove(0);
    			}
    			events.add(x);
            }
            
            if (!exdates.getValues().isEmpty())
            	master.addExceptionDates(exdates);
        }
        
        for (VEvent vevent: events) {
            vevent.setSequence(event.getSequence());
            vevent.setUid(event.getId().toString());
            String name = event.getName();
            String description = (event.hasInstruction() ? event.getInstruction() : event.getType().getName(CONSTANTS));
			if (event.hasCourseTitles() && event.getType() == EventType.Class && ApplicationProperty.EventGridDisplayTitle.isTrue()) {
				name = event.getCourseTitles().get(0);
				if (event.hasInstruction() && event.hasExternalIds())
					description = event.getInstruction() + " " + event.getExternalIds().get(0);
				else if (event.hasInstruction() && event.hasSectionNumber())
					description = event.getInstruction() + " " + event.getSectionNumber();
			}
			if (event.hasInstructors() && ApplicationProperty.EventCalendarDisplayInstructorsInDescription.isTrue()) {
				for (ContactInterface instructor: event.getInstructors())
					description += "\n" + instructor.getName(MESSAGES);
			}
			vevent.setSummary(name);
            vevent.setDescription(description);
            
            if (event.hasTimeStamp()) {
            	DateTimeStamp ts = new DateTimeStamp(event.getTimeStamp());
            	vevent.setDateTimeStamp(ts);
            }
            
            if (ApplicationProperty.EventCalendarSetOrganizer.isTrue()) {
                if (event.hasInstructors()) {
                	int idx = 0;
                	for (ContactInterface instructor: event.getInstructors()) {
                		if (idx++ == 0) {
                			Organizer organizer = new Organizer(instructor.getName(MESSAGES), (instructor.hasEmail() ? instructor.getEmail() : ""));
                			vevent.setOrganizer(organizer);
                		} else {
                    		Attendee attendee = new Attendee(instructor.getName(MESSAGES), (instructor.hasEmail() ? instructor.getEmail() : ""));
                    		attendee.setRole(Role.CHAIR);
                    		vevent.addAttendee(attendee);
                		}
                	}
                } else if (event.hasSponsor()) {
        			Organizer organizer = new Organizer(event.getSponsor().getName(), (event.getSponsor().hasEmail() ? event.getSponsor().getEmail() : ""));
        			vevent.setOrganizer(organizer);
                } else if (event.hasContact()) {
        			Organizer organizer = new Organizer(event.getContact().getName(MESSAGES), (event.getContact().hasEmail() ? event.getContact().getEmail() : ""));
        			vevent.setOrganizer(organizer);
                }
            } else {
                if (event.hasInstructors()) {
                	int idx = 0;
                	for (ContactInterface instructor: event.getInstructors()) {
                		if (idx++ == 0) {
                			Attendee organizer = new Attendee(instructor.getName(MESSAGES), (instructor.hasEmail() ? instructor.getEmail() : ""));
                			organizer.setRole(Role.ORGANIZER);
                			vevent.addAttendee(organizer);
                		} else {
                    		Attendee attendee = new Attendee(instructor.getName(MESSAGES), (instructor.hasEmail() ? instructor.getEmail() : ""));
                    		attendee.setRole(Role.CHAIR);
                    		vevent.addAttendee(attendee);
                		}
                	}
                } else if (event.hasSponsor()) {
                	Attendee organizer = new Attendee(event.getSponsor().getName(), (event.getSponsor().hasEmail() ? event.getSponsor().getEmail() : ""));
        			organizer.setRole(Role.ORGANIZER);
        			vevent.addAttendee(organizer);
                } else if (event.hasContact() && event.getType() != EventType.Class && event.getType() != EventType.FinalExam && event.getType() != EventType.MidtermExam) {
                	Attendee organizer = new Attendee(event.getContact().getName(MESSAGES), (event.getContact().hasEmail() ? event.getContact().getEmail() : ""));
                	organizer.setRole(Role.ORGANIZER);
            		vevent.addAttendee(organizer);
                }
            }

            ical.addEvent(vevent);
        }

        return true;
	}
	
	public class ICalendarMeeting implements Comparable<ICalendarMeeting>{
		private DateTime iStart, iEnd;
		private String iLocation;
		private Status iStatus;
		
		public ICalendarMeeting(MeetingInterface meeting, Status status) {
			if (meeting.getStartTime() != null) {
				iStart = new DateTime(meeting.getStartTime());
			} else {
				iStart = new DateTime(meeting.getMeetingDate()).plusMinutes((5 * meeting.getStartSlot()) + meeting.getStartOffset());
			}
			if (meeting.getStartTime() != null) {
				iEnd = new DateTime(meeting.getStopTime());
			} else {
				iEnd = new DateTime(meeting.getMeetingDate()).plusMinutes((5 * meeting.getEndSlot()) + meeting.getEndOffset());
			}
			if (iStart.getSecondOfMinute() != 0) iStart = iStart.minusSeconds(iStart.getSecondOfMinute());
			if (iEnd.getSecondOfMinute() != 0) iEnd = iEnd.minusSeconds(iEnd.getSecondOfMinute());
			if (iStart.getMillisOfSecond() != 0) iStart = iStart.minusMillis(iStart.getMillisOfSecond());
			if (iEnd.getMillisOfSecond() != 0) iEnd = iEnd.minusMillis(iEnd.getMillisOfSecond());
			iLocation = meeting.getLocationName(MESSAGES);
			iStatus = (status != null ? status : meeting.isApproved() ? Status.confirmed() : Status.tentative());
		}
		
		public DateTime getStart() { return iStart; }
		public DateStart getDateStart() {
			DateStart ds = new DateStart(iStart.toDate(), true);
			return ds;
		}
		
		public DateTime getEnd() { return iEnd; }
		public DateEnd getDateEnd() {
			DateEnd de = new DateEnd(iEnd.toDate(), true);
			return de;
		}

		public String getLocation() { return iLocation; }
		public Status getStatus() { return iStatus; }
		
		public boolean merge(ICalendarMeeting m) {
			if (m.getStart().equals(getStart()) && m.getEnd().equals(getEnd())) {
				if (m.getStatus().isTentative()) iStatus = Status.tentative();
				iLocation += ", " + m.getLocation();
				return true;
			}
			return false;
		}
		
		public boolean same(ICalendarMeeting m) {
			return m.getStart().getSecondOfDay() == getStart().getSecondOfDay() && m.getEnd().getSecondOfDay() == getEnd().getSecondOfDay() &&
					getLocation().equals(m.getLocation()) && getStatus().getValue().equals(m.getStatus().getValue());
		}
		
		public int compareTo(ICalendarMeeting m) {
			int cmp = getStart().compareTo(m.getStart());
			if (cmp != 0) return cmp;
			return getEnd().compareTo(m.getEnd());
		}
		
	}
}
