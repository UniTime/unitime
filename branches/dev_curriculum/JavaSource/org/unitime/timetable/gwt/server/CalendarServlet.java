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

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.cpsolver.coursett.model.RoomLocation;
import net.sf.cpsolver.coursett.model.TimeLocation;
import net.sf.cpsolver.studentsct.model.Section;

public class CalendarServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Long sessionId = null;
		if (request.getParameter("sid") != null) {
			sessionId = Long.valueOf(request.getParameter("sid"));
		} else {
			sessionId = (Long)request.getSession().getAttribute("sessionId");
		}
		if (sessionId == null)
			throw new ServletException("No academic session provided.");
		SectioningServer server = SectioningServer.getInstance(sessionId);
		if (server == null)
			throw new ServletException("Wrong academic session provided.");
    	String classIds = request.getParameter("cid");
    	if (classIds == null)
    		throw new ServletException("No classes provided.");
    	String fts = request.getParameter("ft");
   
		response.setContentType("text/calendar");
		// response.setContentLength(data.length);
		response.setHeader( "Content-Disposition", "attachment; filename=\"schedule.ics\"" );
        
        PrintWriter out = new PrintWriter(response.getOutputStream());
        try {
            out.println("BEGIN:VCALENDAR");
            out.println("VERSION:2.0");
            out.println("CALSCALE:GREGORIAN");
            out.println("METHOD:PUBLISH");
            out.println("X-WR-CALNAME:Class Schedule");
            out.println("X-WR-TIMEZONE:"+TimeZone.getDefault().getID());
            out.println("PRODID:-//UniTime 4 Preview/Personal Class Schedule//NONSGML v1.0//EN");
        	for (String classId: classIds.split(",")) {
        		if (classId.isEmpty()) continue;
        		String[] courseAndClassId = classId.split("-");
        		if (courseAndClassId.length != 2) continue;
        		try {
            		CourseInfo course = server.getCourseInfo(Long.valueOf(courseAndClassId[0]));
            		Section section = server.getSection(Long.valueOf(courseAndClassId[1]));
            		if (course == null || section == null) continue;
            		printSection(server, course, section, out);
        		} catch (NumberFormatException e) {}
        	}
        	if (fts != null && !fts.isEmpty()) {
        		for (String ft: fts.split(",")) {
        			if (ft.isEmpty()) continue;
        			String[] daysStartLen = ft.split("-");
        			if (daysStartLen.length != 3) continue;
        			printFreeTime(server, daysStartLen[0], Integer.parseInt(daysStartLen[1]), Integer.parseInt(daysStartLen[2]), out);
        		}
        	}
            out.println("END:VCALENDAR");
        	out.flush();
        } finally {
        	out.close();
        }
	}
	
	private void printSection(SectioningServer server, CourseInfo course, Section section, PrintWriter out) throws IOException {
		TimeLocation time = section.getTime();
		if (time == null || time.getWeekCode().isEmpty()) return;
		
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat tf = new SimpleDateFormat("HHmmss");
        tf.setTimeZone(TimeZone.getTimeZone("UTC"));

    	Calendar cal = Calendar.getInstance(Locale.US); cal.setLenient(true);
    	cal.setTime(server.getAcademicSession().getDatePatternFirstDate());
    	int idx = time.getWeekCode().nextSetBit(0);
    	cal.add(Calendar.DAY_OF_YEAR, idx);
    	cal.set(Calendar.HOUR_OF_DAY, TimeSlot.toHour(time.getStartSlot()));
    	cal.set(Calendar.MINUTE, TimeSlot.toMinute(time.getStartSlot()));
    	cal.set(Calendar.SECOND, 0);
    	Date first = null;
    	while (idx < time.getWeekCode().size() && first == null) {
    		if (time.getWeekCode().get(idx)) {
        		int dow = cal.get(Calendar.DAY_OF_WEEK);
        		switch (dow) {
        		case Calendar.MONDAY:
        			if ((time.getDayCode() & DayCode.MON.getCode()) != 0) first = cal.getTime();
        			break;
        		case Calendar.TUESDAY:
        			if ((time.getDayCode() & DayCode.TUE.getCode()) != 0) first = cal.getTime();
        			break;
        		case Calendar.WEDNESDAY:
        			if ((time.getDayCode() & DayCode.WED.getCode()) != 0) first = cal.getTime();
        			break;
        		case Calendar.THURSDAY:
        			if ((time.getDayCode() & DayCode.THU.getCode()) != 0) first = cal.getTime();
        			break;
        		case Calendar.FRIDAY:
        			if ((time.getDayCode() & DayCode.FRI.getCode()) != 0) first = cal.getTime();
        			break;
        		case Calendar.SATURDAY:
        			if ((time.getDayCode() & DayCode.SAT.getCode()) != 0) first = cal.getTime();
        			break;
        		case Calendar.SUNDAY:
        			if ((time.getDayCode() & DayCode.SUN.getCode()) != 0) first = cal.getTime();
        			break;
        		}
        	}
    		if (first == null) {
        		cal.add(Calendar.DAY_OF_YEAR, 1); idx++;
    		}
    	}
    	if (first == null) return;
    	cal.add(Calendar.MINUTE, TimeSlot.SLOT_LENGTH_MINS.value() * time.getLength() - time.getBreakTime());
    	Date firstEnd = cal.getTime();
    	int fidx = idx;
    	
    	cal.setTime(server.getAcademicSession().getDatePatternFirstDate());
    	idx = time.getWeekCode().length() - 1;
    	cal.add(Calendar.DAY_OF_YEAR, idx);
    	cal.set(Calendar.HOUR_OF_DAY, TimeSlot.toHour(time.getStartSlot()));
    	cal.set(Calendar.MINUTE, TimeSlot.toMinute(time.getStartSlot()));
    	cal.set(Calendar.SECOND, 0);
    	cal.add(Calendar.MINUTE, TimeSlot.SLOT_LENGTH_MINS.value() * time.getLength() - time.getBreakTime());
    	Date last = null;
    	while (idx >= 0 && last == null) {
    		if (time.getWeekCode().get(idx)) {
        		int dow = cal.get(Calendar.DAY_OF_WEEK);
        		switch (dow) {
        		case Calendar.MONDAY:
        			if ((time.getDayCode() & DayCode.MON.getCode()) != 0) last = cal.getTime();
        			break;
        		case Calendar.TUESDAY:
        			if ((time.getDayCode() & DayCode.TUE.getCode()) != 0) last = cal.getTime();
        			break;
        		case Calendar.WEDNESDAY:
        			if ((time.getDayCode() & DayCode.WED.getCode()) != 0) last = cal.getTime();
        			break;
        		case Calendar.THURSDAY:
        			if ((time.getDayCode() & DayCode.THU.getCode()) != 0) last = cal.getTime();
        			break;
        		case Calendar.FRIDAY:
        			if ((time.getDayCode() & DayCode.FRI.getCode()) != 0) last = cal.getTime();
        			break;
        		case Calendar.SATURDAY:
        			if ((time.getDayCode() & DayCode.SAT.getCode()) != 0) last = cal.getTime();
        			break;
        		case Calendar.SUNDAY:
        			if ((time.getDayCode() & DayCode.SUN.getCode()) != 0) last = cal.getTime();
        			break;
        		}
        	}
    		if (last == null) {
        		cal.add(Calendar.DAY_OF_YEAR, -1); idx--;    			
    		}
    	}
    	if (last == null) return;
    	
    	cal.setTime(server.getAcademicSession().getDatePatternFirstDate());
    	idx = fidx;
    	cal.add(Calendar.DAY_OF_YEAR, idx);
    	cal.set(Calendar.HOUR_OF_DAY, TimeSlot.toHour(time.getStartSlot()));
    	cal.set(Calendar.MINUTE, TimeSlot.toMinute(time.getStartSlot()));
    	cal.set(Calendar.SECOND, 0);

        out.println("BEGIN:VEVENT");
        out.println("DTSTART:" + df.format(first) + "T" + tf.format(first) + "Z");
        out.println("DTEND:" + df.format(firstEnd) + "T" + tf.format(firstEnd) + "Z");
        out.print("RRULE:FREQ=WEEKLY;BYDAY=");
        for (Iterator<DayCode> i = DayCode.toDayCodes(time.getDayCode()).iterator(); i.hasNext(); ) {
        	out.print(i.next().getName().substring(0, 2).toUpperCase());
        	if (i.hasNext()) out.print(",");
        }
        out.println(";WKST=MO;UNTIL=" + df.format(last) + "T" + tf.format(last) + "Z");
        ArrayList<ArrayList<String>> extra = new ArrayList<ArrayList<String>>();
    	while (idx < time.getWeekCode().length()) {
    		int dow = cal.get(Calendar.DAY_OF_WEEK);
    		boolean offered = false;
    		switch (dow) {
    		case Calendar.MONDAY:
    			if ((time.getDayCode() & DayCode.MON.getCode()) != 0) offered = true;
    			break;
    		case Calendar.TUESDAY:
    			if ((time.getDayCode() & DayCode.TUE.getCode()) != 0) offered = true;
    			break;
    		case Calendar.WEDNESDAY:
    			if ((time.getDayCode() & DayCode.WED.getCode()) != 0) offered = true;
    			break;
    		case Calendar.THURSDAY:
    			if ((time.getDayCode() & DayCode.THU.getCode()) != 0) offered = true;
    			break;
    		case Calendar.FRIDAY:
    			if ((time.getDayCode() & DayCode.FRI.getCode()) != 0) offered = true;
    			break;
    		case Calendar.SATURDAY:
    			if ((time.getDayCode() & DayCode.SAT.getCode()) != 0) offered = true;
    			break;
    		case Calendar.SUNDAY:
    			if ((time.getDayCode() & DayCode.SUN.getCode()) != 0) offered = true;
    			break;
    		}
    		if (!offered) {
        		cal.add(Calendar.DAY_OF_YEAR, 1); idx++;
        		continue;
    		}
        	cal.set(Calendar.HOUR_OF_DAY, TimeSlot.toHour(time.getStartSlot()));
        	cal.set(Calendar.MINUTE, TimeSlot.toMinute(time.getStartSlot()));
        	cal.set(Calendar.SECOND, 0);
    		if (time.getWeekCode().get(idx)) {
    			if (!tf.format(first).equals(tf.format(cal.getTime()))) {
    				ArrayList<String> x = new ArrayList<String>(); extra.add(x);
    		        x.add("RECURRENCE-ID:" + df.format(cal.getTime()) + "T" + tf.format(first) + "Z");
    		        x.add("DTSTART:" + df.format(cal.getTime()) + "T" + tf.format(cal.getTime()) + "Z");
    		    	cal.add(Calendar.MINUTE, TimeSlot.SLOT_LENGTH_MINS.value() * time.getLength() - time.getBreakTime());
    		        x.add("DTEND:" + df.format(cal.getTime()) + "T" + tf.format(cal.getTime()) + "Z");
    			}
    		} else {
    			out.println("EXDATE:" + df.format(cal.getTime()) + "T" + tf.format(first) + "Z");
    		}
    		cal.add(Calendar.DAY_OF_YEAR, 1); idx++;
    	}
    	printMeetingRest(server, course, section, out);
        for (ArrayList<String> x: extra) {
            out.println("BEGIN:VEVENT");
            for (String s: x) out.println(s);
            printMeetingRest(server, course, section, out);
        }
	}
	
	@SuppressWarnings("unchecked")
	private void printMeetingRest(SectioningServer server, CourseInfo course, Section section, PrintWriter out) throws IOException {
        out.println("UID:" + section.getId());
        out.println("SEQUENCE:0");
        out.println("SUMMARY:" + course.getSubjectArea() + " " + course.getCourseNbr() + " " +
        		section.getSubpart().getName() + " " + server.getSectionName(course.getUniqueId(), section));
        String desc = (course.getTitle() == null ? "" : course.getTitle());
		if (course.getConsent() != null && !course.getConsent().isEmpty())
			desc += " (" + course.getConsent() + ")";
			out.println("DESCRIPTION:" + desc);
        if (section.getRooms() != null && !section.getRooms().isEmpty()) {
        	String loc = "";
        	for (Enumeration<RoomLocation> e = section.getRooms().elements(); e.hasMoreElements(); ) {
        		RoomLocation r = e.nextElement();
        		if (!loc.isEmpty()) loc += ", ";
        		loc += r.getName();
        	}
        	out.println("LOCATION:" + loc);
        }
        try {
        	URL url = server.getSectionUrl(course.getUniqueId(), section);
        	if (url != null) out.println("URL:" + url.toString());
        } catch (Exception e) {
        	e.printStackTrace();
        }
        boolean org = false;
		if (section.getChoice().getInstructorNames() != null && !section.getChoice().getInstructorNames().isEmpty()) {
			String[] instructors = section.getChoice().getInstructorNames().split(":");
			for (String instructor: instructors) {
				String[] nameEmail = instructor.split("\\|");
				//out.println("CONTACT:" + nameEmail[0] + (nameEmail[1].isEmpty() ? "" : " <" + nameEmail[1]) + ">");
				if (!org) {
					out.println("ORGANIZER;ROLE=CHAIR;CN=\"" + nameEmail[0] + "\":MAILTO:" + nameEmail[1]);
					org = true;
				} else {
					out.println("ATTENDEE;ROLE=CHAIR;CN=\"" + nameEmail[0] + "\":MAILTO:" + nameEmail[1]);
				}
			}
		}
		out.println("STATUS:CONFIRMED");	
        out.println("END:VEVENT");
	}
	
	private void printFreeTime(SectioningServer server, String days, int start, int len, PrintWriter out) throws IOException {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat tf = new SimpleDateFormat("HHmmss");
        tf.setTimeZone(TimeZone.getTimeZone("UTC"));

    	Calendar cal = Calendar.getInstance(Locale.US); cal.setLenient(true);
    	cal.setTime(server.getAcademicSession().getDatePatternFirstDate());
    	BitSet weekCode = server.getAcademicSession().getFreeTimePattern();

    	int idx = weekCode.nextSetBit(0);
    	cal.add(Calendar.DAY_OF_YEAR, idx);
    	cal.set(Calendar.HOUR_OF_DAY, TimeSlot.toHour(start));
    	cal.set(Calendar.MINUTE, TimeSlot.toMinute(start));
    	cal.set(Calendar.SECOND, 0);
    	Date first = null;
    	while (idx < weekCode.size() && first == null) {
    		if (weekCode.get(idx)) {
        		int dow = cal.get(Calendar.DAY_OF_WEEK);
        		switch (dow) {
        		case Calendar.MONDAY:
        			if (days.contains(DayCode.MON.getAbbv())) first = cal.getTime();
        			break;
        		case Calendar.TUESDAY:
        			if (days.contains(DayCode.TUE.getAbbv())) first = cal.getTime();
        			break;
        		case Calendar.WEDNESDAY:
        			if (days.contains(DayCode.WED.getAbbv())) first = cal.getTime();
        			break;
        		case Calendar.THURSDAY:
        			if (days.contains(DayCode.THU.getAbbv())) first = cal.getTime();
        			break;
        		case Calendar.FRIDAY:
        			if (days.contains(DayCode.FRI.getAbbv())) first = cal.getTime();
        			break;
        		case Calendar.SATURDAY:
        			if (days.contains(DayCode.SAT.getAbbv())) first = cal.getTime();
        			break;
        		case Calendar.SUNDAY:
        			if (days.contains(DayCode.SUN.getAbbv())) first = cal.getTime();
        			break;
        		}
        	}
    		cal.add(Calendar.DAY_OF_YEAR, 1); idx++;
    	}
    	if (first == null) return;
    	
    	cal.setTime(server.getAcademicSession().getDatePatternFirstDate());
    	idx = weekCode.length() - 1;
    	cal.add(Calendar.DAY_OF_YEAR, idx);
    	cal.set(Calendar.HOUR_OF_DAY, TimeSlot.toHour(start));
    	cal.set(Calendar.MINUTE, TimeSlot.toMinute(start));
    	cal.set(Calendar.SECOND, 0);
    	cal.add(Calendar.MINUTE, TimeSlot.SLOT_LENGTH_MINS.value() * len);
    	Date last = null;
    	while (idx >= 0 && last == null) {
    		if (weekCode.get(idx)) {
        		int dow = cal.get(Calendar.DAY_OF_WEEK);
        		switch (dow) {
        		case Calendar.MONDAY:
        			if (days.contains(DayCode.MON.getAbbv())) last = cal.getTime();
        			break;
        		case Calendar.TUESDAY:
        			if (days.contains(DayCode.TUE.getAbbv())) last = cal.getTime();
        			break;
        		case Calendar.WEDNESDAY:
        			if (days.contains(DayCode.WED.getAbbv())) last = cal.getTime();
        			break;
        		case Calendar.THURSDAY:
        			if (days.contains(DayCode.THU.getAbbv())) last = cal.getTime();
        			break;
        		case Calendar.FRIDAY:
        			if (days.contains(DayCode.FRI.getAbbv())) last = cal.getTime();
        			break;
        		case Calendar.SATURDAY:
        			if (days.contains(DayCode.SAT.getAbbv())) last = cal.getTime();
        			break;
        		case Calendar.SUNDAY:
        			if (days.contains(DayCode.SUN.getAbbv())) last = cal.getTime();
        			break;
        		}
        	}
    		cal.add(Calendar.DAY_OF_YEAR, -1); idx--;
    	}
    	if (last == null) return;
    	
    	out.println("BEGIN:VFREEBUSY");
        out.println("DTSTART:" + df.format(first) + "T" + tf.format(first) + "Z");
    	cal.add(Calendar.MINUTE, TimeSlot.SLOT_LENGTH_MINS.value() * len);
        out.println("DTEND:" + df.format(last) + "T" + tf.format(last) + "Z");
        out.println("COMMENT:Free Time");

    	cal.setTime(server.getAcademicSession().getDatePatternFirstDate());
    	idx = weekCode.nextSetBit(0);
    	cal.add(Calendar.DAY_OF_YEAR, idx);
    	while (idx < weekCode.length()) {
    		if (weekCode.get(idx)) {
        		int dow = cal.get(Calendar.DAY_OF_WEEK);
        		boolean offered = false;
        		switch (dow) {
        		case Calendar.MONDAY:
        			if (days.contains(DayCode.MON.getAbbv())) offered = true;
        			break;
        		case Calendar.TUESDAY:
        			if (days.contains(DayCode.TUE.getAbbv())) offered = true;
        			break;
        		case Calendar.WEDNESDAY:
        			if (days.contains(DayCode.WED.getAbbv())) offered = true;
        			break;
        		case Calendar.THURSDAY:
        			if (days.contains(DayCode.THU.getAbbv())) offered = true;
        			break;
        		case Calendar.FRIDAY:
        			if (days.contains(DayCode.FRI.getAbbv())) offered = true;
        			break;
        		case Calendar.SATURDAY:
        			if (days.contains(DayCode.SAT.getAbbv())) offered = true;
        			break;
        		case Calendar.SUNDAY:
        			if (days.contains(DayCode.SUN.getAbbv())) offered = true;
        			break;
        		}
        		if (offered) {
        	    	cal.set(Calendar.HOUR_OF_DAY, TimeSlot.toHour(start));
        	    	cal.set(Calendar.MINUTE, TimeSlot.toMinute(start));
        	    	cal.set(Calendar.SECOND, 0);
                    out.print("FREEBUSY:" + df.format(cal.getTime()) + "T" + tf.format(cal.getTime()) + "Z");
                	cal.add(Calendar.MINUTE, TimeSlot.SLOT_LENGTH_MINS.value() * len);
                    out.println("/" + df.format(cal.getTime()) + "T" + tf.format(cal.getTime()) + "Z");
        		}
    		}
    		cal.add(Calendar.DAY_OF_YEAR, 1); idx++;
    	}
    	
        out.println("END:VFREEBUSY");
	}

}
