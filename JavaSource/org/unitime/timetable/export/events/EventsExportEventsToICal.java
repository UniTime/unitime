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
import org.unitime.timetable.export.ExportHelper;
import org.unitime.timetable.gwt.client.events.EventComparator.EventMeetingSortBy;
import org.unitime.timetable.gwt.shared.EventInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ApprovalStatus;
import org.unitime.timetable.gwt.shared.EventInterface.ContactInterface;
import org.unitime.timetable.gwt.shared.EventInterface.EventType;
import org.unitime.timetable.gwt.shared.EventInterface.MeetingInterface;
import org.unitime.timetable.util.Constants;

import biweekly.Biweekly;
import biweekly.ICalendar;
import biweekly.component.VEvent;
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
import biweekly.property.Version;
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
	protected void print(ExportHelper helper, List<EventInterface> events, int eventCookieFlags, EventMeetingSortBy sort, boolean asc) throws IOException {
		helper.setup("text/calendar", reference(), false);
		
		ICalendar ical = new ICalendar();
		ical.setVersion(Version.v2_0());
		ical.setCalendarScale(CalendarScale.gregorian());
		ical.setMethod(new Method("PUBLISH"));
		ical.setExperimentalProperty("X-WR-CALNAME", "UniTime Schedule");
		ical.setExperimentalProperty("X-WR-TIMEZONE", TimeZone.getDefault().getID());
		ical.setProductId("-//UniTime LLC/UniTime " + Constants.getVersion() + " Events//EN");

        for (EventInterface event: events)
			print(ical, event);
		
        Biweekly.write(ical).go(helper.getWriter());
	}
	
	public boolean print(ICalendar ical, EventInterface event) throws IOException {
		return print(ical, event, null);
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
            
            ExceptionDates exdates = new ExceptionDates(true);
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
        				id.setLocalTime(false);
        				id.setTimezoneId(TimeZone.getDefault().getID());
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
    				id.setLocalTime(false);
    				id.setTimezoneId(TimeZone.getDefault().getID());
    				x.setRecurrenceId(id);
    				exdates.getValues().remove(0);
    			}
    			events.add(x);
            }
            
            if (!exdates.getValues().isEmpty())
            	master.addExceptionDates(exdates);
        }
        
        for (VEvent vevent: events) {
            vevent.setSequence(event.hasNotes() ? event.getNotes().size() : 0);
            vevent.setUid(event.getId().toString());
            vevent.setSummary(event.getName());
            vevent.setDescription(event.getInstruction() != null ? event.getInstruction() : event.getType().getName(CONSTANTS));
            
            if (event.hasNotes()) {
            	DateTimeStamp ts = new DateTimeStamp(event.getNotes().last().getDate());
            	vevent.setDateTimeStamp(ts);
            }
            
            if (event.hasInstructors()) {
            	int idx = 0;
            	for (ContactInterface instructor: event.getInstructors()) {
            		if (idx++ == 0) {
            			Organizer organizer = new Organizer("mailto:" + (instructor.hasEmail() ? instructor.getEmail() : ""));
            			organizer.setCommonName(instructor.getName(MESSAGES));
            			vevent.setOrganizer(organizer);
            		} else {
                		Attendee attendee = new Attendee("mailto:" + (instructor.hasEmail() ? instructor.getEmail() : ""));
                		attendee.setCommonName(instructor.getName(MESSAGES));
                		attendee.setRole(Role.CHAIR);
                		vevent.addAttendee(attendee);
            		}
            	}
            } else if (event.hasSponsor()) {
    			Organizer organizer = new Organizer("mailto:" + (event.getSponsor().hasEmail() ? event.getSponsor().getEmail() : ""));
    			organizer.setCommonName(event.getSponsor().getName());
    			vevent.setOrganizer(organizer);
            } else if (event.hasContact()) {
    			Organizer organizer = new Organizer("mailto:" + (event.getContact().hasEmail() ? event.getContact().getEmail() : ""));
    			organizer.setCommonName(event.getContact().getName(MESSAGES));
    			vevent.setOrganizer(organizer);
            }
            ical.addEvent(vevent);
        }

        return true;
	}
	
	@Override
	protected boolean checkRights() {
		return false;
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
			iLocation = meeting.getLocationName();
			iStatus = (status != null ? status : meeting.isApproved() ? Status.confirmed() : Status.tentative());
		}
		
		public DateTime getStart() { return iStart; }
		public DateStart getDateStart() {
			DateStart ds = new DateStart(iStart.toDate(), true);
			ds.setLocalTime(false);
			ds.setTimezoneId(TimeZone.getDefault().getID());
			return ds;
		}
		
		public DateTime getEnd() { return iEnd; }
		public DateEnd getDateEnd() {
			DateEnd de = new DateEnd(iEnd.toDate(), true);
			de.setLocalTime(false);
			de.setTimezoneId(TimeZone.getDefault().getID());
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
