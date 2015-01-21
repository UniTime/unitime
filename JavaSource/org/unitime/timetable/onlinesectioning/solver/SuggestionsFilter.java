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
package org.unitime.timetable.onlinesectioning.solver;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.cpsolver.coursett.model.RoomLocation;
import org.cpsolver.studentsct.model.Course;
import org.cpsolver.studentsct.model.Section;
import org.cpsolver.studentsct.online.selection.SuggestionsBranchAndBound;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.server.DayCode;
import org.unitime.timetable.gwt.server.Query;
import org.unitime.timetable.util.Formats;

/**
 * @author Tomas Muller
 */
public class SuggestionsFilter implements SuggestionsBranchAndBound.SuggestionFilter {
	private static StudentSectioningConstants CONSTANTS = Localization.create(StudentSectioningConstants.class);
	private Query iFilter = null;
	private Date iFirstDate = null;

	public SuggestionsFilter(String filter, Date firstDate) {
		iFilter = new Query(filter);
		iFirstDate = firstDate;
	}

	@Override
	public boolean match(Course course, Section section) {
		return iFilter.match(new SectionMatcher(course, section));
	}
	
    private class SectionMatcher implements Query.TermMatcher {
    	private Course iCourse;
    	private Section iSection;
    	
    	public SectionMatcher(Course course, Section section) {
    		iCourse = course;
    		iSection = section;
    	}

		@Override
		public boolean match(String attr, String term) {
			if (term.isEmpty()) return true;
			if (attr == null || attr.equals("crn") || attr.equals("id") || attr.equals("externalId") || attr.equals("exid") || attr.equals("name")) {
				if (iSection.getName(iCourse.getId()) != null && iSection.getName(iCourse.getId()).toLowerCase().startsWith(term.toLowerCase()))
					return true;
			}
			if (attr == null || attr.equals("day")) {
				if (iSection.getTime() == null && term.equalsIgnoreCase("none")) return true;
				if (iSection.getTime() != null) {
					int day = parseDay(term);
					if (day > 0 && (iSection.getTime().getDayCode() & day) == day) return true;
				}
			}
			if (attr == null || attr.equals("time")) {
				if (iSection.getTime() == null && term.equalsIgnoreCase("none")) return true;
				if (iSection.getTime() != null) {
					int start = parseStart(term);
					if (start >= 0 && iSection.getTime().getStartSlot() == start) return true;
				}
			}
			if (attr != null && attr.equals("before")) {
				if (iSection.getTime() != null) {
					int end = parseStart(term);
					if (end >= 0 && iSection.getTime().getStartSlot() + iSection.getTime().getLength() - iSection.getTime().getBreakTime() / 5 <= end) return true;
				}
			}
			if (attr != null && attr.equals("after")) {
				if (iSection.getTime() != null) {
					int start = parseStart(term);
					if (start >= 0 && iSection.getTime().getStartSlot() >= start) return true;
				}
			}
			if (attr == null || attr.equals("date")) {
				if (iSection.getTime() == null && term.equalsIgnoreCase("none")) return true;
				if (iSection.getTime() != null && !iSection.getTime().getWeekCode().isEmpty()) {
					Formats.Format<Date> df = Formats.getDateFormat(Formats.Pattern.DATE_PATTERN);
			    	Calendar cal = Calendar.getInstance(Locale.US); cal.setLenient(true);
			    	cal.setTime(iFirstDate);
			    	for (int i = 0; i < iSection.getTime().getWeekCode().size(); i++) {
			    		if (iSection.getTime().getWeekCode().get(i)) {
			    			DayCode day = null;
			    			switch (cal.get(Calendar.DAY_OF_WEEK)) {
			    			case Calendar.MONDAY:
			    				day = DayCode.MON; break;
			    			case Calendar.TUESDAY:
			    				day = DayCode.TUE; break;
			    			case Calendar.WEDNESDAY:
			    				day = DayCode.WED; break;
			    			case Calendar.THURSDAY:
			    				day = DayCode.THU; break;
			    			case Calendar.FRIDAY:
			    				day = DayCode.FRI; break;
			    			case Calendar.SATURDAY:
			    				day = DayCode.SAT; break;
			    			case Calendar.SUNDAY:
			    				day = DayCode.SUN; break;
			    			}
			    			if ((iSection.getTime().getDayCode() & day.getCode()) == day.getCode()) {
				    			int d = cal.get(Calendar.DAY_OF_MONTH);
				    			int m = cal.get(Calendar.MONTH) + 1;
				    			if (df.format(cal.getTime()).equalsIgnoreCase(term) || eq(d + "." + m + ".",term) || eq(m + "/" + d, term)) return true;
			    			}
			    		}
			    		cal.add(Calendar.DAY_OF_YEAR, 1);
			    	}
				}
			}
			if (attr == null || attr.equals("room")) {
				if ((iSection.getRooms() == null || iSection.getRooms().isEmpty()) && term.equalsIgnoreCase("none")) return true;
				if (iSection.getRooms() != null) {
					for (RoomLocation r: iSection.getRooms()) {
						if (has(r.getName(), term)) return true;
					}
				}
			}
			if (attr == null || attr.equals("instr") || attr.equals("instructor")) {
				if (attr != null && (iSection.getChoice().getInstructorNames() == null || iSection.getChoice().getInstructorNames().isEmpty()) && term.equalsIgnoreCase("none")) return true;
				for (String instructor: iSection.getChoice().getInstructorNames().split(":")) {
					String[] nameEmail = instructor.split("\\|");
					if (has(nameEmail[0], term)) return true;
					if (nameEmail.length == 2) {
						String email = nameEmail[1];
						if (email.indexOf('@') >= 0) email = email.substring(0, email.indexOf('@'));
						if (eq(email, term)) return true;
					}
				}
			}
			if (attr != null && iSection.getTime() != null) {
				int start = parseStart(attr + ":" + term);
				if (start >= 0 && iSection.getTime().getStartSlot() == start) return true;
			}
			return false;
		}
		
		private boolean eq(String name, String term) {
			if (name == null) return false;
			return name.equalsIgnoreCase(term);
		}

		private boolean has(String name, String term) {
			if (name == null) return false;
			for (String t: name.split(" "))
				if (t.equalsIgnoreCase(term)) return true;
			return false;
		}
		
		private int parseDay(String token) {
			int days = 0;
			boolean found = false;
			do {
				found = false;
				for (int i=0; i<CONSTANTS.longDays().length; i++) {
					if (token.toLowerCase().startsWith(CONSTANTS.longDays()[i].toLowerCase())) {
						days |= DayCode.values()[i].getCode(); 
						token = token.substring(CONSTANTS.longDays()[i].length());
						while (token.startsWith(" ")) token = token.substring(1);
						found = true;
					}
				}
				for (int i=0; i<CONSTANTS.days().length; i++) {
					if (token.toLowerCase().startsWith(CONSTANTS.days()[i].toLowerCase())) {
						days |= DayCode.values()[i].getCode(); 
						token = token.substring(CONSTANTS.days()[i].length());
						while (token.startsWith(" ")) token = token.substring(1);
						found = true;
					}
				}
				for (int i=0; i<CONSTANTS.days().length; i++) {
					if (token.toLowerCase().startsWith(CONSTANTS.days()[i].substring(0,2).toLowerCase())) {
						days |= DayCode.values()[i].getCode(); 
						token = token.substring(2);
						while (token.startsWith(" ")) token = token.substring(1);
						found = true;
					}
				}
				for (int i=0; i<CONSTANTS.shortDays().length; i++) {
					if (token.toLowerCase().startsWith(CONSTANTS.shortDays()[i].toLowerCase())) {
						days |= DayCode.values()[i].getCode(); 
						token = token.substring(CONSTANTS.shortDays()[i].length());
						while (token.startsWith(" ")) token = token.substring(1);
						found = true;
					}
				}
				for (int i=0; i<CONSTANTS.freeTimeShortDays().length; i++) {
					if (token.toLowerCase().startsWith(CONSTANTS.freeTimeShortDays()[i].toLowerCase())) {
						days |= DayCode.values()[i].getCode(); 
						token = token.substring(CONSTANTS.freeTimeShortDays()[i].length());
						while (token.startsWith(" ")) token = token.substring(1);
						found = true;
					}
				}
			} while (found);
			return (token.isEmpty() ? days : 0);
		}
		
		private int parseStart(String token) {
			int startHour = 0, startMin = 0;
			String number = "";
			while (!token.isEmpty() && token.charAt(0) >= '0' && token.charAt(0) <= '9') { number += token.substring(0, 1); token = token.substring(1); }
			if (number.isEmpty()) return -1;
			if (number.length() > 2) {
				startHour = Integer.parseInt(number) / 100;
				startMin = Integer.parseInt(number) % 100;
			} else {
				startHour = Integer.parseInt(number);
			}
			while (token.startsWith(" ")) token = token.substring(1);
			if (token.startsWith(":")) {
				token = token.substring(1);
				while (token.startsWith(" ")) token = token.substring(1);
				number = "";
				while (!token.isEmpty() && token.charAt(0) >= '0' && token.charAt(0) <= '9') { number += token.substring(0, 1); token = token.substring(1); }
				if (number.isEmpty()) return -1;
				startMin = Integer.parseInt(number);
			}
			while (token.startsWith(" ")) token = token.substring(1);
			boolean hasAmOrPm = false;
			if (token.toLowerCase().startsWith("am")) { token = token.substring(2); hasAmOrPm = true; }
			if (token.toLowerCase().startsWith("a")) { token = token.substring(1); hasAmOrPm = true; }
			if (token.toLowerCase().startsWith("pm")) { token = token.substring(2); hasAmOrPm = true; if (startHour<12) startHour += 12; }
			if (token.toLowerCase().startsWith("p")) { token = token.substring(1); hasAmOrPm = true; if (startHour<12) startHour += 12; }
			if (startHour < 7 && !hasAmOrPm) startHour += 12;
			if (startMin % 5 != 0) startMin = 5 * ((startMin + 2)/ 5);
			if (startHour == 7 && startMin == 0 && !hasAmOrPm) startHour += 12;
			return (60 * startHour + startMin) / 5;
		}
    }
}
