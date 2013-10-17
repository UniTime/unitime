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
package org.unitime.timetable.onlinesectioning.updates;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.TimeZone;

import org.unitime.timetable.gwt.server.DayCode;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.model.XCourse;
import org.unitime.timetable.onlinesectioning.model.XCourseRequest;
import org.unitime.timetable.onlinesectioning.model.XEnrollment;
import org.unitime.timetable.onlinesectioning.model.XFreeTimeRequest;
import org.unitime.timetable.onlinesectioning.model.XInstructor;
import org.unitime.timetable.onlinesectioning.model.XOffering;
import org.unitime.timetable.onlinesectioning.model.XRequest;
import org.unitime.timetable.onlinesectioning.model.XRoom;
import org.unitime.timetable.onlinesectioning.model.XSection;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.model.XTime;
import org.unitime.timetable.server.CourseDetailsBackend;
import org.unitime.timetable.util.Constants;

/**
 * @author Tomas Muller
 */
public class CalendarExport implements OnlineSectioningAction<String>{
	private static final long serialVersionUID = 1L;
	private String iClassIds;
	private String iFts;
	
	public CalendarExport(String classIds, String fts) {
		iClassIds = classIds; iFts = fts;
	}

	@Override
	public String execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		try {
			StringWriter buffer = new StringWriter();
			PrintWriter out = new PrintWriter(buffer);
			if (iClassIds != null && !iClassIds.isEmpty()) {
	        	for (String classId: iClassIds.split(",")) {
	        		if (classId.isEmpty()) continue;
	        		String[] courseAndClassId = classId.split("-");
	        		if (courseAndClassId.length != 2) continue;
	        		XCourse course = server.getCourse(Long.valueOf(courseAndClassId[0]));
	        		XOffering offering = (course == null ? null : server.getOffering(course.getOfferingId()));
	        		XSection section = (offering == null ? null : offering.getSection(Long.valueOf(courseAndClassId[1])));
	        		if (course == null || section == null) continue;
	        		printSection(server, course, section, out);
	        	}
			}
			if (iFts != null && !iFts.isEmpty()) {
        		Date dpFirstDate = server.getAcademicSession().getDatePatternFirstDate();
        		BitSet weekCode = server.getAcademicSession().getFreeTimePattern();
        		for (String ft: iFts.split(",")) {
        			if (ft.isEmpty()) continue;
        			String[] daysStartLen = ft.split("-");
        			if (daysStartLen.length != 3) continue;
        			printFreeTime(dpFirstDate, weekCode, daysStartLen[0], Integer.parseInt(daysStartLen[1]), Integer.parseInt(daysStartLen[2]), out);
        		}
        	}
			out.flush(); out.close();
			return buffer.toString();
		} catch (IOException e) {
			throw new SectioningException(e.getMessage(), e);
		}
	}
	
	public static String getCalendar(OnlineSectioningServer server, XStudent student) throws IOException {
		if (student == null) return null;
		StringWriter buffer = new StringWriter();
		PrintWriter out = new PrintWriter(buffer);
        out.println("BEGIN:VCALENDAR");
        out.println("VERSION:2.0");
        out.println("CALSCALE:GREGORIAN");
        out.println("METHOD:PUBLISH");
        out.println("X-WR-CALNAME:UniTime Schedule");
        out.println("X-WR-TIMEZONE:"+TimeZone.getDefault().getID());
        out.println("PRODID:-//UniTime " + Constants.getVersion() + "/Schedule Calendar//NONSGML v1.0//EN");
		for (XRequest request: student.getRequests()) {
			if (request instanceof XCourseRequest) {
				XCourseRequest cr = (XCourseRequest)request;
				XEnrollment enrollment = cr.getEnrollment();
				if (enrollment == null) continue;
				XCourse course = server.getCourse(enrollment.getCourseId());
				XOffering offering = server.getOffering(enrollment.getOfferingId());
				if (course != null && offering != null)
					for (XSection section: offering.getSections(enrollment))
						printSection(server, course, section, out);
			} else if (request instanceof XFreeTimeRequest) {
				XFreeTimeRequest ft = (XFreeTimeRequest)request;
				printFreeTime(server.getAcademicSession().getDatePatternFirstDate(), server.getAcademicSession().getFreeTimePattern(), 
						DayCode.toString(ft.getTime().getDays()), ft.getTime().getSlot(), ft.getTime().getLength(), out);
			}
		}
	    out.println("END:VCALENDAR");
    	out.flush();
		out.close();
		return buffer.toString();		
	}
	
	private static void printSection(OnlineSectioningServer server, XCourse course, XSection section, PrintWriter out) throws IOException {
		XTime time = section.getTime();
		if (time == null || time.getWeeks().isEmpty()) return;
		
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat tf = new SimpleDateFormat("HHmmss");
        tf.setTimeZone(TimeZone.getTimeZone("UTC"));

    	Calendar cal = Calendar.getInstance(Locale.US); cal.setLenient(true);
    	cal.setTime(server.getAcademicSession().getDatePatternFirstDate());
    	int idx = time.getWeeks().nextSetBit(0);
    	cal.add(Calendar.DAY_OF_YEAR, idx);
    	cal.set(Calendar.HOUR_OF_DAY, Constants.toHour(time.getSlot()));
    	cal.set(Calendar.MINUTE, Constants.toMinute(time.getSlot()));
    	cal.set(Calendar.SECOND, 0);
    	Date first = null;
    	while (idx < time.getWeeks().size() && first == null) {
    		if (time.getWeeks().get(idx)) {
        		int dow = cal.get(Calendar.DAY_OF_WEEK);
        		switch (dow) {
        		case Calendar.MONDAY:
        			if ((time.getDays() & DayCode.MON.getCode()) != 0) first = cal.getTime();
        			break;
        		case Calendar.TUESDAY:
        			if ((time.getDays() & DayCode.TUE.getCode()) != 0) first = cal.getTime();
        			break;
        		case Calendar.WEDNESDAY:
        			if ((time.getDays() & DayCode.WED.getCode()) != 0) first = cal.getTime();
        			break;
        		case Calendar.THURSDAY:
        			if ((time.getDays() & DayCode.THU.getCode()) != 0) first = cal.getTime();
        			break;
        		case Calendar.FRIDAY:
        			if ((time.getDays() & DayCode.FRI.getCode()) != 0) first = cal.getTime();
        			break;
        		case Calendar.SATURDAY:
        			if ((time.getDays() & DayCode.SAT.getCode()) != 0) first = cal.getTime();
        			break;
        		case Calendar.SUNDAY:
        			if ((time.getDays() & DayCode.SUN.getCode()) != 0) first = cal.getTime();
        			break;
        		}
        	}
    		if (first == null) {
        		cal.add(Calendar.DAY_OF_YEAR, 1); idx++;
    		}
    	}
    	if (first == null) return;
    	cal.add(Calendar.MINUTE, Constants.SLOT_LENGTH_MIN * time.getLength() - time.getBreakTime());
    	Date firstEnd = cal.getTime();
    	int fidx = idx;
    	
    	cal.setTime(server.getAcademicSession().getDatePatternFirstDate());
    	idx = time.getWeeks().length() - 1;
    	cal.add(Calendar.DAY_OF_YEAR, idx);
    	cal.set(Calendar.HOUR_OF_DAY, Constants.toHour(time.getSlot()));
    	cal.set(Calendar.MINUTE, Constants.toMinute(time.getSlot()));
    	cal.set(Calendar.SECOND, 0);
    	cal.add(Calendar.MINUTE, Constants.SLOT_LENGTH_MIN * time.getLength() - time.getBreakTime());
    	Date last = null;
    	while (idx >= 0 && last == null) {
    		if (time.getWeeks().get(idx)) {
        		int dow = cal.get(Calendar.DAY_OF_WEEK);
        		switch (dow) {
        		case Calendar.MONDAY:
        			if ((time.getDays() & DayCode.MON.getCode()) != 0) last = cal.getTime();
        			break;
        		case Calendar.TUESDAY:
        			if ((time.getDays() & DayCode.TUE.getCode()) != 0) last = cal.getTime();
        			break;
        		case Calendar.WEDNESDAY:
        			if ((time.getDays() & DayCode.WED.getCode()) != 0) last = cal.getTime();
        			break;
        		case Calendar.THURSDAY:
        			if ((time.getDays() & DayCode.THU.getCode()) != 0) last = cal.getTime();
        			break;
        		case Calendar.FRIDAY:
        			if ((time.getDays() & DayCode.FRI.getCode()) != 0) last = cal.getTime();
        			break;
        		case Calendar.SATURDAY:
        			if ((time.getDays() & DayCode.SAT.getCode()) != 0) last = cal.getTime();
        			break;
        		case Calendar.SUNDAY:
        			if ((time.getDays() & DayCode.SUN.getCode()) != 0) last = cal.getTime();
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
    	cal.set(Calendar.HOUR_OF_DAY, Constants.toHour(time.getSlot()));
    	cal.set(Calendar.MINUTE, Constants.toMinute(time.getSlot()));
    	cal.set(Calendar.SECOND, 0);

        out.println("BEGIN:VEVENT");
        out.println("DTSTART:" + df.format(first) + "T" + tf.format(first) + "Z");
        out.println("DTEND:" + df.format(firstEnd) + "T" + tf.format(firstEnd) + "Z");
        out.print("RRULE:FREQ=WEEKLY;BYDAY=");
        for (Iterator<DayCode> i = DayCode.toDayCodes(time.getDays()).iterator(); i.hasNext(); ) {
        	out.print(i.next().getName().substring(0, 2).toUpperCase());
        	if (i.hasNext()) out.print(",");
        }
        out.println(";WKST=MO;UNTIL=" + df.format(last) + "T" + tf.format(last) + "Z");
        ArrayList<ArrayList<String>> extra = new ArrayList<ArrayList<String>>();
    	while (idx < time.getWeeks().length()) {
    		int dow = cal.get(Calendar.DAY_OF_WEEK);
    		boolean offered = false;
    		switch (dow) {
    		case Calendar.MONDAY:
    			if ((time.getDays() & DayCode.MON.getCode()) != 0) offered = true;
    			break;
    		case Calendar.TUESDAY:
    			if ((time.getDays() & DayCode.TUE.getCode()) != 0) offered = true;
    			break;
    		case Calendar.WEDNESDAY:
    			if ((time.getDays() & DayCode.WED.getCode()) != 0) offered = true;
    			break;
    		case Calendar.THURSDAY:
    			if ((time.getDays() & DayCode.THU.getCode()) != 0) offered = true;
    			break;
    		case Calendar.FRIDAY:
    			if ((time.getDays() & DayCode.FRI.getCode()) != 0) offered = true;
    			break;
    		case Calendar.SATURDAY:
    			if ((time.getDays() & DayCode.SAT.getCode()) != 0) offered = true;
    			break;
    		case Calendar.SUNDAY:
    			if ((time.getDays() & DayCode.SUN.getCode()) != 0) offered = true;
    			break;
    		}
    		if (!offered) {
        		cal.add(Calendar.DAY_OF_YEAR, 1); idx++;
        		continue;
    		}
        	cal.set(Calendar.HOUR_OF_DAY, Constants.toHour(time.getSlot()));
        	cal.set(Calendar.MINUTE, Constants.toMinute(time.getSlot()));
        	cal.set(Calendar.SECOND, 0);
    		if (time.getWeeks().get(idx)) {
    			if (!tf.format(first).equals(tf.format(cal.getTime()))) {
    				ArrayList<String> x = new ArrayList<String>(); extra.add(x);
    		        x.add("RECURRENCE-ID:" + df.format(cal.getTime()) + "T" + tf.format(first) + "Z");
    		        x.add("DTSTART:" + df.format(cal.getTime()) + "T" + tf.format(cal.getTime()) + "Z");
    		    	cal.add(Calendar.MINUTE, Constants.SLOT_LENGTH_MIN * time.getLength() - time.getBreakTime());
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
	private static void printMeetingRest(OnlineSectioningServer server, XCourse course, XSection section, PrintWriter out) throws IOException {
        out.println("UID:" + section.getSectionId());
        out.println("SEQUENCE:0");
        out.println("SUMMARY:" + course.getSubjectArea() + " " + course.getCourseNumber() + " " + section.getSubpartName() + " " + section.getName(course.getCourseId()));
        String desc = (course.getTitle() == null ? "" : course.getTitle());
		if (course.getConsentLabel() != null && !course.getConsentLabel().isEmpty())
			desc += " (" + course.getConsentLabel() + ")";
			out.println("DESCRIPTION:" + desc);
        if (section.getRooms() != null && !section.getRooms().isEmpty()) {
        	String loc = "";
        	for (XRoom r: section.getRooms()) {
        		if (!loc.isEmpty()) loc += ", ";
        		loc += r.getName();
        	}
        	out.println("LOCATION:" + loc);
        }
        try {
        	URL url = CourseDetailsBackend.getCourseUrl(server.getAcademicSession(), course.getSubjectArea(), course.getCourseNumber());
        	if (url != null) out.println("URL:" + url.toString());
        } catch (Exception e) {
        	e.printStackTrace();
        }
        boolean org = false;
        if (section.getInstructors() != null && !section.getInstructors().isEmpty()) {
			for (XInstructor instructor: section.getInstructors()) {
				//out.println("CONTACT:" + nameEmail[0] + (nameEmail[1].isEmpty() ? "" : " <" + nameEmail[1]) + ">");
				if (!org) {
					out.println("ORGANIZER;ROLE=CHAIR;CN=\"" + instructor.getName() + "\":MAILTO:" + ( instructor.getEmail() != null ? instructor.getEmail() : ""));
					org = true;
				} else {
					out.println("ATTENDEE;ROLE=CHAIR;CN=\"" + instructor.getName() + "\":MAILTO:" + ( instructor.getEmail() != null ? instructor.getEmail() : ""));
				}
			}
		}
		out.println("STATUS:CONFIRMED");	
        out.println("END:VEVENT");
	}
	
	private static void printFreeTime(Date dpFirstDate, BitSet weekCode, String days, int start, int len, PrintWriter out) throws IOException {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat tf = new SimpleDateFormat("HHmmss");
        tf.setTimeZone(TimeZone.getTimeZone("UTC"));

    	Calendar cal = Calendar.getInstance(Locale.US); cal.setLenient(true);
    	cal.setTime(dpFirstDate);

    	int idx = weekCode.nextSetBit(0);
    	cal.add(Calendar.DAY_OF_YEAR, idx);
    	cal.set(Calendar.HOUR_OF_DAY, Constants.toHour(start));
    	cal.set(Calendar.MINUTE, Constants.toMinute(start));
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
    	
    	cal.setTime(dpFirstDate);
    	idx = weekCode.length() - 1;
    	cal.add(Calendar.DAY_OF_YEAR, idx);
    	cal.set(Calendar.HOUR_OF_DAY, Constants.toHour(start));
    	cal.set(Calendar.MINUTE, Constants.toMinute(start));
    	cal.set(Calendar.SECOND, 0);
    	cal.add(Calendar.MINUTE, Constants.SLOT_LENGTH_MIN * len);
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
    	cal.add(Calendar.MINUTE, Constants.SLOT_LENGTH_MIN * len);
        out.println("DTEND:" + df.format(last) + "T" + tf.format(last) + "Z");
        out.println("COMMENT:Free Time");

    	cal.setTime(dpFirstDate);
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
        	    	cal.set(Calendar.HOUR_OF_DAY, Constants.toHour(start));
        	    	cal.set(Calendar.MINUTE, Constants.toMinute(start));
        	    	cal.set(Calendar.SECOND, 0);
                    out.print("FREEBUSY:" + df.format(cal.getTime()) + "T" + tf.format(cal.getTime()) + "Z");
                	cal.add(Calendar.MINUTE, Constants.SLOT_LENGTH_MIN * len);
                    out.println("/" + df.format(cal.getTime()) + "T" + tf.format(cal.getTime()) + "Z");
        		}
    		}
    		cal.add(Calendar.DAY_OF_YEAR, 1); idx++;
    	}
    	
        out.println("END:VFREEBUSY");
	}

	@Override
	public String name() {
		return "calendar";
	}
}