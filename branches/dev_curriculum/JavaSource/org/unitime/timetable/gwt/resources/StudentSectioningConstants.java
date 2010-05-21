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
package org.unitime.timetable.gwt.resources;

import com.google.gwt.i18n.client.Constants;

public interface StudentSectioningConstants extends Constants {
	@DefaultStringArrayValue({
		"Tip: Use Ctrl+1 (or Ctrl+Alt+1) to navigate to the first course, Ctrl+2 to the second, Ctrl+A to the first alternative, Ctr+B to the second alternative, etc.",
		"Tip: Use Ctrl+Arrow to navigate, Ctrl+Shift+Up and Ctrl+Shith+Down to move a line around.",
		"Tip: Use Ctrl+F to open the Course Finder dialog.",
		"Tip: Use Ctrl+N (or Ctrl+Alt+N in some browsers) to validate the screen and go next.",
		"Tip: Start entering the name (e.g., ENGL 10600) of the course or a part of its title (e.g., History) to see suggestions.",
		"Tip: The Alternatives below are here to ensure that you get a desired number of courses even when a course (and its alternatives) are not available.",
		"Tip: Enter a free time to aviod getting classes in time you need for something else.",
		"Tip: All courses above a free time should not overlap with the free time (you will get the course even when the only possibility is to break the free time).",
		"Tip: All courses below a free time can not overlap with the free time (you will only get the course if there are sections that do not break the free time).",
		"Tip: Clik this tip to see another tip.",
		"Tip: There are no alternative free times.",
		"Tip: Try not to break too many standard time patterns with a free time (see the numbers in the Course Finder dialog).",
		"Tip: Use Esc to hide suggestions, Ctrl+S to show suggestions."
	})
	String[] tips();
	
	@DefaultStringArrayValue({
		"Tip: Use Up and Down to navigate through courses, Enter to select one.",
		"Tip: Start entering the name of a course (e.g., ENGL 10600) or a part of its title (e.g., History) to see suggestions.",
		"Tip: Click on a course to see its details.",
		"Tip: Doubleclik on a course to select it.",
		"Tip: Press Esc to close the dialog, Enter to select the inputed text or the selected course."})
	String[] courseTips();

	@DefaultStringArrayValue({
		"Tip: Enter a free time (e.g., Monday 8am - 10am) or use the mouse to select it.",
		"Tip: The numbers in the selected times counts the number of overlapping standard time patterns (3x50, 2x75, 1x150), try to avoid overlapping too many of those."})
	String[] freeTimeTips();

	@DefaultStringValue("Free ")
	String freePrefix();

	@DefaultStringArrayValue({
		"7:30a", "8:00a", "8:30a", "9:00a", "9:30a", "10:00a", "10:30a", "11:00a", "11:30a", "12:00p",
		"12:30p", "1:00p", "1:30p", "2:00p", "2:30p", "3:00p", "3:30p", "4:00p", "4:30p", "5:00p", "5:30p",
		"6:00p", "6:30p", "7:00p", "7:30p"
	})
	String[] freeTimePeriods();
	
	@DefaultStringArrayValue({"Mon", "Tue", "Wed", "Thu", "Fri"})
	String[] freeTimeDays();
	
	@DefaultStringArrayValue({"M", "T", "W", "R", "F"})
	String[] freeTimeShortDays();
	
	@DefaultStringArrayValue({"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"})
	String[] freeTimeLongDays();

	@DefaultStringArrayValue({"0", "2", "6", "8", "12", "14", "15", "16", "17", "18", "19", "20"})
	String[] freeTimeOneDay150();
	
	@DefaultStringArrayValue({"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"})
	String[] longDays();

	@DefaultStringArrayValue({"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"})
	String[] days();

	@DefaultStringArrayValue({"M", "T", "W", "R", "F", "S", "U"})
	String[] shortDays();

	@DefaultStringArrayValue({
		"blue", "green", "orange", "yellow", "pink",
		"purple", "teal", "darkpurple", "steelblue", "lightblue",
		"lightgreen", "yellowgreen", "redorange", "lightbrown", "lightpurple",
		"grey", "bluegrey", "lightteal", "yellowgrey", "brown"})
	String[] meetingColors();
	
	@DefaultStringValue("red")
	String freeTimeColor();
	
	@DefaultBooleanValue(false)
	boolean printReportShowUserName();
}
