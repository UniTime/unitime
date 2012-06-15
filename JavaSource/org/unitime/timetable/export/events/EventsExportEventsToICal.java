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
package org.unitime.timetable.export.events;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.TimeZone;
import java.util.TreeSet;

import org.unitime.timetable.export.ExportHelper;
import org.unitime.timetable.gwt.client.events.EventComparator.EventMeetingSortBy;
import org.unitime.timetable.gwt.shared.EventInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ContactInterface;
import org.unitime.timetable.gwt.shared.EventInterface.MeetingInterface;
import org.unitime.timetable.util.Constants;

public class EventsExportEventsToICal extends EventsExporter {
	
	@Override
	public String reference() {
		return "events.ics";
	}
	
	@Override
	protected void print(ExportHelper helper, List<EventInterface> events, int eventCookieFlags, EventMeetingSortBy sort) throws IOException {
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
	
	public void print(PrintWriter out, EventInterface event) throws IOException {
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat tf = new SimpleDateFormat("HHmmss");
        tf.setTimeZone(TimeZone.getTimeZone("UTC"));
        
        Hashtable<String, String> date2loc = new Hashtable<String, String>();
        Hashtable<String, Boolean> approved = new Hashtable<String, Boolean>();
        for (MeetingInterface m: event.getMeetings()) {
        	if (m.isArrangeHours()) continue;
        	Date startTime = new Date(m.getStartTime());
        	Date stopTime = new Date(m.getStopTime());
            String date = df.format(startTime) + "T" + tf.format(startTime) + "Z/" + df.format(stopTime) + "T" + tf.format(stopTime) + "Z";
            String loc = m.getLocationName();
            String l = date2loc.get(date);
            date2loc.put(date, (l == null || l.isEmpty() ? "" : l + ", ") + loc);
            Boolean a = approved.get(date);
            approved.put(date, (a == null || a) && m.isApproved());
        }
        
        String firstDate = null;
        for (String date : new TreeSet<String>(date2loc.keySet())) {
        	String loc = date2loc.get(date);
        	String start = date.substring(0, date.indexOf('/'));
        	String end = date.substring(date.indexOf('/') + 1);
            out.println("BEGIN:VEVENT");
            out.println("SEQUENCE:0");
            out.println("UID:"+event.getId());
            out.println("SUMMARY:"+event.getName());
            out.println("DESCRIPTION:"+(event.getInstruction() != null ? event.getInstruction() : event.getType()));
            out.println("DTSTART:" + start);
            out.println("DTEND:" + end);
            if (firstDate == null) {
            	firstDate = date;
            	String rdate = "";
                for (String d : new TreeSet<String>(date2loc.keySet())) {
                	if (d.equals(date)) continue;
                	if (!rdate.isEmpty()) rdate += ",";
                	rdate += d;
            	}
            	if (!rdate.isEmpty())
            		out.println("RDATE;VALUE=PERIOD:" + rdate);
            } else {
    	        out.println("RECURRENCE-ID:" + start);
            }
            out.println("LOCATION:" + loc);
            out.println("STATUS:" + (approved.get(date) ? "CONFIRMED" : "TENTATIVE"));
            if (event.hasInstructors()) {
            	int idx = 0;
            	for (ContactInterface instructor: event.getInstructors()) {
            		out.println((idx++ == 0 ? "ORGANIZER" : "ATTENDEE") + ";ROLE=CHAIR;CN=\"" + instructor.getName() + "\":MAILTO:" + (instructor.hasEmail() ? instructor.getEmail() : ""));
            	}
            } else if (event.hasSponsor()) {
            	out.println("ORGANIZER;ROLE=CHAIR;CN=\"" + event.getSponsor().getName() + "\":MAILTO:" + (event.getSponsor().hasEmail() ? event.getSponsor().getEmail() : ""));
            }
            out.println("END:VEVENT");	
        }
	}
	
	@Override
	protected boolean checkRights() {
		return false;
	}
}
