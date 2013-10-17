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
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;

import org.springframework.stereotype.Service;
import org.unitime.timetable.export.ExportHelper;
import org.unitime.timetable.gwt.client.events.EventComparator.EventMeetingSortBy;
import org.unitime.timetable.gwt.shared.EventInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ApprovalStatus;
import org.unitime.timetable.gwt.shared.EventInterface.ContactInterface;
import org.unitime.timetable.gwt.shared.EventInterface.MeetingInterface;
import org.unitime.timetable.util.Constants;

/**
 * @author Tomas Muller
 */
@Service("org.unitime.timetable.export.Exporter:events.ics")
public class EventsExportEventsToICal extends EventsExporter {
	private DateFormat iDateFormat, iTimeFormat;
	private static String[] DAYS = new String[] { "MO", "TU", "WE", "TH", "FR", "SA", "SU" };
	
	public EventsExportEventsToICal() {
		iDateFormat = new SimpleDateFormat("yyyyMMdd", Locale.US);
		iDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		iTimeFormat = new SimpleDateFormat("HHmmss", Locale.US);
		iTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
	}
	
	@Override
	public String reference() {
		return "events.ics";
	}
	
	@Override
	protected void print(ExportHelper helper, List<EventInterface> events, int eventCookieFlags, EventMeetingSortBy sort, boolean asc) throws IOException {
		helper.setup("text/calendar", reference(), false);
		
		PrintWriter out = helper.getWriter();
		out.println("BEGIN:VCALENDAR");
        out.println("VERSION:2.0");
        out.println("CALSCALE:GREGORIAN");
        out.println("METHOD:PUBLISH");
        out.println("X-WR-CALNAME:UniTime Schedule");
        out.println("X-WR-TIMEZONE:"+TimeZone.getDefault().getID());
        out.println("PRODID:-//UniTime " + Constants.getVersion() + "/Events Calendar//NONSGML v1.0//EN");

        for (EventInterface event: events)
			print(out, event);
		
		out.println("END:VCALENDAR");
	}
	
	public boolean print(PrintWriter out, EventInterface event) throws IOException {
		return print(out, event, null);
	}
	
	public boolean print(PrintWriter out, EventInterface event, ICalendarStatus status) throws IOException {
        TreeSet<ICalendarMeeting> meetings = new TreeSet<ICalendarMeeting>();
        Set<Integer> days = new TreeSet<Integer>();
        if (event.hasMeetings())
            meetings: for (MeetingInterface m: event.getMeetings()) {
            	if (m.isArrangeHours()) continue;
            	if (m.getApprovalStatus() != ApprovalStatus.Approved && m.getApprovalStatus() != ApprovalStatus.Pending) continue;
                ICalendarMeeting x = new ICalendarMeeting(m, status != null ? status : m.isApproved() ? ICalendarStatus.CONFIRMED : ICalendarStatus.TENTATIVE);
                
                for (ICalendarMeeting icm: meetings)
                	if (icm.merge(x)) continue meetings;
                meetings.add(x); days.add(x.getDayOfWeek());
            }
        
        if (meetings.isEmpty()) return false;
        
        ICalendarMeeting first = meetings.first();
        
        out.println("BEGIN:VEVENT");

        out.println("SEQUENCE:" + (event.hasNotes() ? event.getNotes().size() : 0));
        out.println("UID:"+event.getId());
        out.println("SUMMARY:"+event.getName());
        out.println("DESCRIPTION:"+(event.getInstruction() != null ? event.getInstruction() : event.getType()));

        out.println("DTSTART:" + first.getStart());
        out.println("DTEND:" + first.getEnd());
        out.print("RRULE:FREQ=WEEKLY;BYDAY=");
        for (Iterator<Integer> i = days.iterator(); i.hasNext(); ) {
        	out.print(DAYS[i.next()]);
        	if (i.hasNext()) out.print(",");
        }
        out.println(";WKST=MO;UNTIL=" + meetings.last().getEndDate() + "T" + first.getEndTime() + "Z");
        
        Calendar cal = Calendar.getInstance(); cal.setTime(first.iStart);
        String date = iDateFormat.format(cal.getTime());
        int dow = first.getDayOfWeek();
        ArrayList<ArrayList<String>> extra = new ArrayList<ArrayList<String>>();
        while (date.compareTo(meetings.last().getEndDate()) <= 0) {
        	boolean found = false;
        	for (ICalendarMeeting ics: meetings) {
        		if (date.equals(ics.getStartDate())) {
        			found = true;
        			if (!first.same(ics)) {
        				ArrayList<String> x = new ArrayList<String>(); extra.add(x);
        		        x.add("RECURRENCE-ID:" + ics.getStartDate() + "T" + first.getStartTime() + "Z");
        		        x.add("DTSTART:" + ics.getStart());
        		    	x.add("DTEND:" + ics.getEnd());
        		    	x.add("LOCATION:" + ics.getLocation());
        	            x.add("STATUS:" + ics.getStatus().name());
        			}
        		}
        	}
        	if (!found && days.contains(dow))
        		out.println("EXDATE:" + date + "T" + first.getStartTime() + "Z");
        	cal.add(Calendar.DAY_OF_YEAR, 1);
        	date = iDateFormat.format(cal.getTime());
        	dow = (dow + 1) % 7;
        }
        out.println("LOCATION:" + first.getLocation());
        out.println("STATUS:" + first.getStatus().name());
        
        if (event.hasInstructors()) {
        	int idx = 0;
        	for (ContactInterface instructor: event.getInstructors()) {
        		out.println((idx++ == 0 ? "ORGANIZER" : "ATTENDEE") + ";ROLE=CHAIR;CN=\"" + instructor.getName(MESSAGES) + "\":MAILTO:" + (instructor.hasEmail() ? instructor.getEmail() : ""));
        	}
        } else if (event.hasSponsor()) {
        	out.println("ORGANIZER;ROLE=CHAIR;CN=\"" + event.getSponsor().getName() + "\":MAILTO:" + (event.getSponsor().hasEmail() ? event.getSponsor().getEmail() : ""));
        } else if (event.hasContact()) {
        	out.println("ORGANIZER;ROLE=CHAIR;CN=\"" + event.getContact().getName(MESSAGES) + "\":MAILTO:" + (event.getContact().hasEmail() ? event.getContact().getEmail() : ""));
        }
        
        out.println("END:VEVENT");
        
        for (ArrayList<String> x: extra) {
            out.println("BEGIN:VEVENT");
            
            out.println("SEQUENCE:" + (event.hasNotes() ? event.getNotes().size() : 0));
            out.println("UID:"+event.getId());
            out.println("SUMMARY:"+event.getName());
            out.println("DESCRIPTION:"+(event.getInstruction() != null ? event.getInstruction() : event.getType()));

            for (String s: x) out.println(s);
            
            if (event.hasInstructors()) {
            	int idx = 0;
            	for (ContactInterface instructor: event.getInstructors()) {
            		out.println((idx++ == 0 ? "ORGANIZER" : "ATTENDEE") + ";ROLE=CHAIR;CN=\"" + instructor.getName(MESSAGES) + "\":MAILTO:" + (instructor.hasEmail() ? instructor.getEmail() : ""));
            	}
            } else if (event.hasSponsor()) {
            	out.println("ORGANIZER;ROLE=CHAIR;CN=\"" + event.getSponsor().getName() + "\":MAILTO:" + (event.getSponsor().hasEmail() ? event.getSponsor().getEmail() : ""));
            } else if (event.hasContact()) {
            	out.println("ORGANIZER;ROLE=CHAIR;CN=\"" + event.getContact().getName(MESSAGES) + "\":MAILTO:" + (event.getContact().hasEmail() ? event.getContact().getEmail() : ""));
            }
            
            out.println("END:VEVENT");
        }
        
        return true;
	}
	
	@Override
	protected boolean checkRights() {
		return false;
	}
	
	public static enum ICalendarStatus {
		CANCELLED,
		TENTATIVE,
		CONFIRMED
	};
	
	public class ICalendarMeeting implements Comparable<ICalendarMeeting>{
		private Date iStart, iEnd;
		private String iLocation;
		private int iDayOfWeek;
		private ICalendarStatus iStatus;
		
		public ICalendarMeeting(MeetingInterface meeting, ICalendarStatus status) {
			iStart = new Date(meeting.getStartTime());
			iEnd = new Date(meeting.getStopTime());
			iDayOfWeek = meeting.getDayOfWeek();
			iLocation = meeting.getLocationName();
			iStatus = status;
		}
		
		public String getStart() { return iDateFormat.format(iStart) + "T" + iTimeFormat.format(iStart) + "Z"; }
		public String getStartTime() { return iTimeFormat.format(iStart); }
		public String getStartDate() { return iDateFormat.format(iStart); }
		
		public String getEnd() { return iDateFormat.format(iEnd) + "T" + iTimeFormat.format(iEnd) + "Z"; }
		public String getEndTime() { return iTimeFormat.format(iEnd); }
		public String getEndDate() { return iDateFormat.format(iEnd); }

		public int getDayOfWeek() { return iDayOfWeek; }
		public String getLocation() { return iLocation; }
		public ICalendarStatus getStatus() { return iStatus; }
		
		public boolean merge(ICalendarMeeting m) {
			if (m.getStart().equals(getStart()) && m.getEnd().equals(getEnd())) {
				if (m.getStatus() == ICalendarStatus.TENTATIVE) iStatus = ICalendarStatus.TENTATIVE;
				iLocation += ", " + m.getLocation();
				return true;
			}
			return false;
		}
		
		public boolean same(ICalendarMeeting m) {
			return getStartTime().equals(m.getStartTime()) && getEndTime().equals(m.getEndTime()) && getLocation().equals(m.getLocation()) && getStatus().equals(m.getStatus());
		}
		
		public int compareTo(ICalendarMeeting m) {
			int cmp = getStart().compareTo(m.getStart());
			if (cmp != 0) return cmp;
			cmp = getEnd().compareTo(m.getEnd());
			if (cmp != 0) return cmp;
			return getStatus().compareTo(m.getStatus());
		}
		
	}
}
