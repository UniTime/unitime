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
package org.unitime.timetable.onlinesectioning.updates;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.TimeZone;

import org.unitime.commons.CalendarVTimeZoneGenerator;
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

import biweekly.ICalVersion;
import biweekly.ICalendar;
import biweekly.component.VEvent;
import biweekly.component.VFreeBusy;
import biweekly.io.text.ICalWriter;
import biweekly.parameter.Role;
import biweekly.property.Attendee;
import biweekly.property.CalendarScale;
import biweekly.property.DateEnd;
import biweekly.property.DateStart;
import biweekly.property.ExceptionDates;
import biweekly.property.Method;
import biweekly.property.Organizer;
import biweekly.property.Status;
import biweekly.util.Recurrence;
import biweekly.util.Recurrence.DayOfWeek;
import biweekly.util.Recurrence.Frequency;

/**
 * @author Tomas Muller
 */
public class CalendarExport implements OnlineSectioningAction<String>{
	private static final long serialVersionUID = 1L;
	private String iClassIds;
	private String iFts;
	
	public CalendarExport withParams(String classIds, String fts) {
		iClassIds = classIds; iFts = fts;
		return this;
	}

	@Override
	public String execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		try {
			ICalendar ical = new ICalendar();
			ical.setVersion(ICalVersion.V2_0);
			ical.setCalendarScale(CalendarScale.gregorian());
			ical.setMethod(new Method("PUBLISH"));
			ical.setExperimentalProperty("X-WR-CALNAME", "UniTime Schedule");
			ical.setExperimentalProperty("X-WR-TIMEZONE", TimeZone.getDefault().getID());
			ical.setProductId("-//UniTime LLC/UniTime " + Constants.getVersion() + " Schedule//EN");

			if (iClassIds != null && !iClassIds.isEmpty()) {
	        	for (String classId: iClassIds.split(",")) {
	        		if (classId.isEmpty()) continue;
	        		String[] courseAndClassId = classId.split("-");
	        		if (courseAndClassId.length != 2) continue;
	        		XCourse course = server.getCourse(Long.valueOf(courseAndClassId[0]));
	        		XOffering offering = (course == null ? null : server.getOffering(course.getOfferingId()));
	        		XSection section = (offering == null ? null : offering.getSection(Long.valueOf(courseAndClassId[1])));
	        		if (course == null || section == null) continue;
	        		printSection(server, course, section, ical);
	        	}
			}
			if (iFts != null && !iFts.isEmpty()) {
        		Date dpFirstDate = server.getAcademicSession().getDatePatternFirstDate();
        		BitSet weekCode = server.getAcademicSession().getFreeTimePattern();
        		for (String ft: iFts.split(",")) {
        			if (ft.isEmpty()) continue;
        			String[] daysStartLen = ft.split("-");
        			if (daysStartLen.length != 3) continue;
        			printFreeTime(dpFirstDate, weekCode, daysStartLen[0], Integer.parseInt(daysStartLen[1]), Integer.parseInt(daysStartLen[2]), ical);
        		}
        	}
			
			StringWriter ret = new StringWriter();
	        ICalWriter writer = new ICalWriter(ret, ICalVersion.V2_0);
	        try {
	        	writer.getTimezoneInfo().setGenerator(new CalendarVTimeZoneGenerator());
	        	writer.getTimezoneInfo().setDefaultTimeZone(TimeZone.getDefault());
			} catch (IllegalArgumentException e) {
				helper.warn("Failed to set default time zone: " + e.getMessage());
	        }
	        try {
	        	writer.write(ical);
	        	writer.flush();
	        } finally {
	        	writer.close();
	        }
	        return ret.toString();
		} catch (IOException e) {
			throw new SectioningException(e.getMessage(), e);
		}
	}
	
	public static String getCalendar(OnlineSectioningServer server, OnlineSectioningHelper helper, XStudent student) throws IOException {
		if (student == null) return null;
		ICalendar ical = new ICalendar();
		ical.setVersion(ICalVersion.V2_0);
		ical.setCalendarScale(CalendarScale.gregorian());
		ical.setMethod(new Method("PUBLISH"));
		ical.setExperimentalProperty("X-WR-CALNAME", "UniTime Schedule");
		ical.setExperimentalProperty("X-WR-TIMEZONE", TimeZone.getDefault().getID());
		ical.setProductId("-//UniTime LLC/UniTime " + Constants.getVersion() + " Schedule//EN");

		for (XRequest request: student.getRequests()) {
			if (request instanceof XCourseRequest) {
				XCourseRequest cr = (XCourseRequest)request;
				XEnrollment enrollment = cr.getEnrollment();
				if (enrollment == null) continue;
				XCourse course = server.getCourse(enrollment.getCourseId());
				XOffering offering = server.getOffering(enrollment.getOfferingId());
				if (course != null && offering != null)
					for (XSection section: offering.getSections(enrollment))
						printSection(server, course, section, ical);
			} else if (request instanceof XFreeTimeRequest) {
				XFreeTimeRequest ft = (XFreeTimeRequest)request;
				printFreeTime(server.getAcademicSession().getDatePatternFirstDate(), server.getAcademicSession().getFreeTimePattern(), 
						DayCode.toString(ft.getTime().getDays()), ft.getTime().getSlot(), ft.getTime().getLength(), ical);
			}
		}
		
		StringWriter ret = new StringWriter();
        ICalWriter writer = new ICalWriter(ret, ICalVersion.V2_0);
        try {
        	writer.getTimezoneInfo().setGenerator(new CalendarVTimeZoneGenerator());
        	writer.getTimezoneInfo().setDefaultTimeZone(TimeZone.getDefault());
        } catch (IllegalArgumentException e) {
			helper.warn("Failed to set default time zone: " + e.getMessage());
        }
        try {
        	writer.write(ical);
        	writer.flush();
        } finally {
        	writer.close();
        }

        return ret.toString();		
	}
	
	private static void printSection(OnlineSectioningServer server, XCourse course, XSection section, ICalendar ical) throws IOException {
		XTime time = section.getTime();
		if (time == null || time.getWeeks().isEmpty()) return;
		
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
    	
    	VEvent vevent = new VEvent();
    	DateStart dstart = new DateStart(first, true);
    	vevent.setDateStart(dstart);
    	DateEnd dend = new DateEnd(firstEnd, true);
    	vevent.setDateEnd(dend);
    	
    	Recurrence.Builder recur = new Recurrence.Builder(Frequency.WEEKLY);
        for (Iterator<DayCode> i = DayCode.toDayCodes(time.getDays()).iterator(); i.hasNext(); ) {
        	switch (i.next()) {
        	case MON:
        		recur.byDay(DayOfWeek.MONDAY); break;
        	case TUE:
        		recur.byDay(DayOfWeek.TUESDAY); break;
        	case WED:
        		recur.byDay(DayOfWeek.WEDNESDAY); break;
        	case THU:
        		recur.byDay(DayOfWeek.THURSDAY); break;
        	case FRI:
        		recur.byDay(DayOfWeek.FRIDAY); break;
        	case SAT:
        		recur.byDay(DayOfWeek.SATURDAY); break;
        	case SUN:
        		recur.byDay(DayOfWeek.SUNDAY); break;
        	}
        }
        recur.workweekStarts(DayOfWeek.MONDAY).until(last);
        vevent.setRecurrenceRule(recur.build());

        ExceptionDates exdates = new ExceptionDates();
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
    		if (!time.getWeeks().get(idx)) {
    			exdates.addValue(cal.getTime());
    		}
    		cal.add(Calendar.DAY_OF_YEAR, 1); idx++;
    	}
    	if (!exdates.getValues().isEmpty())
        	vevent.addExceptionDates(exdates);
    	
    	vevent.setUid(section.getSectionId().toString());
    	vevent.setSequence(0);
    	vevent.setSummary(course.getSubjectArea() + " " + course.getCourseNumber() + " " + section.getSubpartName() + " " + section.getName(course.getCourseId()));
        String desc = (course.getTitle() == null ? "" : course.getTitle());
		if (course.getConsentLabel() != null && !course.getConsentLabel().isEmpty())
			desc += " (" + course.getConsentLabel() + ")";
		vevent.setDescription(desc);
        if (section.getRooms() != null && !section.getRooms().isEmpty()) {
        	String loc = "";
        	for (XRoom r: section.getRooms()) {
        		if (!loc.isEmpty()) loc += ", ";
        		loc += r.getName();
        	}
        	vevent.setLocation(loc);
        }
        try {
        	URL url = CourseDetailsBackend.getCourseUrl(server.getAcademicSession(), course.getSubjectArea(), course.getCourseNumber());
        	if (url != null)
        		vevent.setUrl(url.toString());
        } catch (Exception e) {
        	e.printStackTrace();
        }
        if (section.getInstructors() != null && !section.getInstructors().isEmpty()) {
			for (XInstructor instructor: section.getInstructors()) {
				if (vevent.getOrganizer() == null) {
					Organizer organizer = new Organizer(instructor.getName(), (instructor.getEmail() != null ? instructor.getEmail() : ""));
					vevent.setOrganizer(organizer);
				} else {
					Attendee attendee = new Attendee(instructor.getName(), (instructor.getEmail() != null ? instructor.getEmail() : ""));
					attendee.setRole(Role.CHAIR);
					vevent.addAttendee(attendee);
				}
			}
		}
        vevent.setStatus(Status.confirmed());
        ical.addEvent(vevent);
	}
	
	private static void printFreeTime(Date dpFirstDate, BitSet weekCode, String days, int start, int len, ICalendar ical) throws IOException {
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
    	
    	VFreeBusy vfree = new VFreeBusy();
    	DateStart dstart = new DateStart(first, true);
    	vfree.setDateStart(dstart);
    	Calendar c = Calendar.getInstance(Locale.US); c.setTime(first); c.add(Calendar.MINUTE, Constants.SLOT_LENGTH_MIN * len);
    	DateEnd dend = new DateEnd(c.getTime(), true);
    	vfree.setDateEnd(dend);
    	vfree.addComment("Free Time");
    	ical.addFreeBusy(vfree);

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
        	    	
        	    	vfree = new VFreeBusy();
        	    	dstart = new DateStart(cal.getTime(), true);
        	    	vfree.setDateStart(dstart);
        	    	cal.add(Calendar.MINUTE, Constants.SLOT_LENGTH_MIN * len);
        	    	dend = new DateEnd(cal.getTime(), true);
        	    	vfree.setDateEnd(dend);
        	    	vfree.addComment("Free Time");
        	    	ical.addFreeBusy(vfree);
        		}
    		}
    		cal.add(Calendar.DAY_OF_YEAR, 1); idx++;
    	}
	}

	@Override
	public String name() {
		return "calendar";
	}
}