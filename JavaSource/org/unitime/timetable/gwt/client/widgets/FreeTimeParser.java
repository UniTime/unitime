/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2014, UniTime LLC, and individual contributors
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
package org.unitime.timetable.gwt.client.widgets;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.FreeTime;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Tomas Muller
 */
public class FreeTimeParser implements DataProvider<String, List<CourseRequestInterface.FreeTime>> {
	protected static final StudentSectioningConstants CONSTANTS = GWT.create(StudentSectioningConstants.class);
	protected static final StudentSectioningMessages MESSAGES = GWT.create(StudentSectioningMessages.class);
	private Set<String> iValidCourseNames = null;

	@Override
	public void getData(String source, AsyncCallback<List<FreeTime>> callback) {
		try {
			callback.onSuccess(parseFreeTime(source));
		} catch (IllegalArgumentException e) {
			callback.onFailure(e);
		}
	}
	
	public void setValidCourseNames(Set<String> validCourseNames) { iValidCourseNames = validCourseNames; }
	
	public ArrayList<CourseRequestInterface.FreeTime> parseFreeTime(String text) throws IllegalArgumentException {
		if (iValidCourseNames != null && iValidCourseNames.contains(text))
			throw new IllegalArgumentException(MESSAGES.notFreeTimeIsCourse(text));
		ArrayList<CourseRequestInterface.FreeTime> ret = new ArrayList<CourseRequestInterface.FreeTime>();
		if (text.isEmpty()) throw new IllegalArgumentException(MESSAGES.courseSelectionNoFreeTime());
		ArrayList<Integer> lastDays = new ArrayList<Integer>();
		String tokens[] = text.split("[,;]");
		for (String token: tokens) {
			String original = token;
			if (token.toLowerCase().startsWith(CONSTANTS.freePrefix().toLowerCase())) token = token.substring(CONSTANTS.freePrefix().length());
			ArrayList<Integer> days = new ArrayList<Integer>();
			while (token.startsWith(" ")) token = token.substring(1);
			boolean found = false;
			do {
				found = false;
				for (int i=0; i<CONSTANTS.longDays().length; i++) {
					if (token.toLowerCase().startsWith(CONSTANTS.longDays()[i].toLowerCase())) {
						days.add(i);
						token = token.substring(CONSTANTS.longDays()[i].length());
						while (token.startsWith(" ")) token = token.substring(1);
						found = true;
					}
				}
				for (int i=0; i<CONSTANTS.days().length; i++) {
					if (token.toLowerCase().startsWith(CONSTANTS.days()[i].toLowerCase())) {
						days.add(i);
						token = token.substring(CONSTANTS.days()[i].length());
						while (token.startsWith(" ")) token = token.substring(1);
						found = true;
					}
				}
				for (int i=0; i<CONSTANTS.days().length; i++) {
					if (token.toLowerCase().startsWith(CONSTANTS.days()[i].substring(0,2).toLowerCase())) {
						days.add(i);
						token = token.substring(2);
						while (token.startsWith(" ")) token = token.substring(1);
						found = true;
					}
				}
				for (int i=0; i<CONSTANTS.shortDays().length; i++) {
					if (token.toLowerCase().startsWith(CONSTANTS.shortDays()[i].toLowerCase())) {
						days.add(i);
						token = token.substring(CONSTANTS.shortDays()[i].length());
						while (token.startsWith(" ")) token = token.substring(1);
						found = true;
					}
				}
				for (int i=0; i<CONSTANTS.freeTimeShortDays().length; i++) {
					if (token.toLowerCase().startsWith(CONSTANTS.freeTimeShortDays()[i].toLowerCase())) {
						days.add(i);
						token = token.substring(CONSTANTS.freeTimeShortDays()[i].length());
						while (token.startsWith(" ")) token = token.substring(1);
						found = true;
					}
				}
			} while (found);
			int startHour = 0, startMin = 0;
			while (token.startsWith(" ")) token = token.substring(1);
			String number = "";
			while (!token.isEmpty() && token.charAt(0) >= '0' && token.charAt(0) <= '9') { number += token.substring(0, 1); token = token.substring(1); }
			if (number.isEmpty()) throw new IllegalArgumentException(MESSAGES.invalidFreeTimeExpectedDayOrNumber(original, 1 + original.lastIndexOf(token)));
			if (number.length()>2) {
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
				if (number.isEmpty()) throw new IllegalArgumentException(MESSAGES.invalidFreeTimeExpectedNumber(original, 1 + original.lastIndexOf(token)));
				startMin = Integer.parseInt(number);
			}
			while (token.startsWith(" ")) token = token.substring(1);
			boolean hasAmOrPm = false;
			if (token.toLowerCase().startsWith("am")) {token = token.substring(2); hasAmOrPm = true; }
			if (token.toLowerCase().startsWith("a")) {token = token.substring(1); hasAmOrPm = true; }
			if (token.toLowerCase().startsWith("pm")) { token = token.substring(2); hasAmOrPm = true; if (startHour<12) startHour += 12; }
			if (token.toLowerCase().startsWith("p")) { token = token.substring(1); hasAmOrPm = true; if (startHour<12) startHour += 12; }
			if (startHour < 7 && !hasAmOrPm) startHour += 12;
			//if (startMin < 29) startMin = 0; else startMin = 30;
			if (startMin % 5 != 0) startMin = 5 * ((startMin + 2)/ 5);
			if (startHour == 7 && startMin == 0 && !hasAmOrPm) startHour += 12;
			int startTime = (60 * startHour + startMin) / 5; // (60 * startHour + startMin) / 30 - 15
			int endTime = startTime;
			while (token.startsWith(" ")) token = token.substring(1);
			if (token.startsWith("-")) {
				int endHour = 0, endMin = 0;
				token = token.substring(1);
				while (token.startsWith(" ")) token = token.substring(1);
				number = "";
				while (!token.isEmpty() && token.charAt(0) >= '0' && token.charAt(0) <= '9') { number += token.substring(0, 1); token = token.substring(1); }
				if (number.isEmpty()) throw new IllegalArgumentException(MESSAGES.invalidFreeTimeExpectedNumber(original, 1 + original.lastIndexOf(token)));
				if (number.length()>2) {
					endHour = Integer.parseInt(number) / 100;
					endMin = Integer.parseInt(number) % 100;
				} else {
					endHour = Integer.parseInt(number);
				}
				while (token.startsWith(" ")) token = token.substring(1);
				if (token.startsWith(":")) {
					token = token.substring(1);
					while (token.startsWith(" ")) token = token.substring(1);
					number = "";
					while (!token.isEmpty() && token.charAt(0) >= '0' && token.charAt(0) <= '9') { number += token.substring(0, 1); token = token.substring(1); }
					if (number.isEmpty()) throw new IllegalArgumentException(MESSAGES.invalidFreeTimeExpectedNumber(original, 1 + original.lastIndexOf(token)));
					endMin = Integer.parseInt(number);
				}
				while (token.startsWith(" ")) token = token.substring(1);
				hasAmOrPm = false;
				if (token.toLowerCase().startsWith("am")) { token = token.substring(2); hasAmOrPm = true; if (endHour == 12) endHour += 12; }
				if (token.toLowerCase().startsWith("a")) { token = token.substring(1); hasAmOrPm = true; if (endHour == 12) endHour += 12; }
				if (token.toLowerCase().startsWith("pm")) { token = token.substring(2); hasAmOrPm = true; if (endHour < 12) endHour += 12; }
				if (token.toLowerCase().startsWith("p")) { token = token.substring(1); hasAmOrPm = true; if (endHour < 12) endHour += 12; }
				if (endHour <= 7 && !hasAmOrPm) endHour += 12;
				// if (endMin < 29) endMin = 0; else endMin = 30;
				if (endMin % 5 != 0) endMin = 5 * ((endMin + 2)/ 5);
				endTime = (60 * endHour + endMin) / 5; // (60 * endHour + endMin) / 30 - 15
			}
			while (token.startsWith(" ")) token = token.substring(1);
			if (!token.isEmpty()) throw new IllegalArgumentException(MESSAGES.invalidFreeTimeGeneric(original, 1 + original.lastIndexOf(token)));
			if (days.isEmpty()) days = lastDays;
			if (days.isEmpty()) {
				for (int i=0; i<CONSTANTS.freeTimeDays().length; i++)
					days.add(i);
			}
			if (startTime == endTime) {
				endTime += 6;
				if ((days.contains(0) || days.contains(2) || days.contains(4)) && !days.contains(1) && !days.contains(3)) {
					if (startTime % 12 == 6) endTime += 6;
				} else if ((days.contains(1) || days.contains(3)) && !days.contains(0) && !days.contains(2) && !days.contains(4)) {
					if (startTime % 18 == 0) endTime += 12;
					else if (startTime % 18 == 6) endTime += 6;
				}
			}
			if (startTime < 0 || startTime > 24 * 12)
				throw new IllegalArgumentException(MESSAGES.invalidFreeTimeInvalidStartTime(original));
			if (endTime < 0 || endTime > 24 * 12)
				throw new IllegalArgumentException(MESSAGES.invalidFreeTimeInvalidEndTime(original));
			/*
			if (startTime < 0)
				throw new IllegalArgumentException(MESSAGES.invalidFreeTimeStartBeforeFirst(original, CONSTANTS.freeTimePeriods()[0]));
			if (startTime >= CONSTANTS.freeTimePeriods().length - 1)
				throw new IllegalArgumentException(MESSAGES.invalidFreeTimeStartAfterLast(original, CONSTANTS.freeTimePeriods()[CONSTANTS.freeTimePeriods().length - 2]));
			if (endTime < 0)
				throw new IllegalArgumentException(MESSAGES.invalidFreeTimeEndBeforeFirst(original, CONSTANTS.freeTimePeriods()[0]));
			if (endTime >= CONSTANTS.freeTimePeriods().length) 
				throw new IllegalArgumentException(MESSAGES.invalidFreeTimeEndAfterLast(original, CONSTANTS.freeTimePeriods()[CONSTANTS.freeTimePeriods().length - 1]));
			*/
			if (startTime >= endTime)
				throw new IllegalArgumentException(MESSAGES.invalidFreeTimeStartNotBeforeEnd(original));
			CourseRequestInterface.FreeTime f = new CourseRequestInterface.FreeTime();
			for (int day: days)
				f.addDay(day);
			f.setStart(startTime); // 6 * (startTime + 15));
			f.setLength(endTime - startTime); // 6 * (endTime - startTime));
			ret.add(f);
			lastDays = days;
		}
		return ret;
	}

	public String freeTimesToString(List<CourseRequestInterface.FreeTime> freeTimes) {
		String ret = "";
		String lastDays = null;
		for (CourseRequestInterface.FreeTime ft: freeTimes) {
			if (ret.length() > 0) ret += ", ";
			String days = ft.getDaysString(CONSTANTS.shortDays(), "");
			if (ft.getDays().size() == CONSTANTS.freeTimeDays().length && !ft.getDays().contains(5) && !ft.getDays().contains(6)) days = "";
			ret += (days.isEmpty() || days.equals(lastDays) ? "" : days + " ") + ft.getStartString(CONSTANTS.useAmPm()) + " - " + ft.getEndString(CONSTANTS.useAmPm());
			lastDays = days;
		}
		return CONSTANTS.freePrefix() + ret;
	}
}
