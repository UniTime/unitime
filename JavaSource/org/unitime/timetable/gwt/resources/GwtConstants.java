/*
 * UniTime 3.3 - 3.5 (University Timetabling Application)
 * Copyright (C) 2011 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.gwt.resources;

import com.google.gwt.i18n.client.Constants;

/**
 * @author Tomas Muller
 */
public interface GwtConstants extends Constants {
	
	@DefaultStringValue("3.5")
	String version();

	@DefaultStringValue("&copy; 2008 - 2013 UniTime LLC")
	String copyright();
	
	@DefaultBooleanValue(true)
	boolean useAmPm();

	@DefaultBooleanValue(false)
	boolean firstDayThenMonth();

	@DefaultStringArrayValue({"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"})
	String[] days();

	@DefaultStringValue("MM/dd/yyyy")
	String eventDateFormat();
	
	@DefaultStringValue("MM/dd")
	String eventDateFormatShort();

	@DefaultStringValue("MM/dd, yyyy")
	String eventDateFormatLong();

	@DefaultStringValue("EEE MM/dd, yyyy")
	String meetingDateFormat();
	
	@DefaultStringValue("EEE MM/dd")
	String examPeriodDateFormat();
	
	@DefaultStringArrayValue({ "EEE", "MM/dd" })
	String[] examPeriodPreferenceDateFormat();
	
	@DefaultStringValue("MM/dd/yyyy hh:mmaa")
	String timeStampFormat();
	
	@DefaultStringValue("MM/dd hh:mmaa")
	String timeStampFormatShort();
	
	@DefaultIntValue(3)
	int eventSlotIncrement();
	
	@DefaultIntValue(90)
	int eventStartDefault();
	
	@DefaultIntValue(210)
	int eventStopDefault();
	
	@DefaultIntValue(12)
	int eventLengthDefault();
	
	@DefaultIntValue(10000)
	int maxMeetings();
	
	@DefaultStringArrayValue({"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"})
	String[] longDays();
	
	@DefaultStringArrayValue({"M", "T", "W", "Th", "F", "S", "Su"})
	String[] shortDays();
	
	@DefaultStringValue("midnight")
	String timeMidnight();

	@DefaultStringValue("noon")
	String timeNoon();
	
	@DefaultStringValue("all day")
	String timeAllDay();
	
	@DefaultStringValue("pm")
	String timePm();
	
	@DefaultStringValue("am")
	String timeAm();
	
	@DefaultStringValue("p")
	String timeShortPm();
	
	@DefaultStringValue("a")
	String timeShortAm();

	@DefaultStringArrayValue({ "h", "hr", "hrs", "hour", "hours" })
	String[] parseTimeHours();

	@DefaultStringArrayValue({ "m", "min", "mins", "minute", "minutes" })
	String[] parseTimeMinutes();

	@DefaultStringArrayValue({ "am", "a" })
	String[] parseTimeAm();

	@DefaultStringArrayValue({ "pm", "p" })
	String[] parseTimePm();
	
	@DefaultStringArrayValue({ "noon" })
	String[] parseTimeNoon();

	@DefaultStringArrayValue({ "midnight" })
	String[] parseTimeMidnight();

	@DefaultStringValue("Daily")
	String daily();
	
	@DefaultStringValue("Arrange Hours")
	String arrangeHours();

	@DefaultStringArrayValue({
		"blue", "green", "orange", "yellow", "pink",
		"purple", "teal", "darkpurple", "steelblue", "lightblue",
		"lightgreen", "yellowgreen", "redorange", "lightbrown", "lightpurple",
		"grey", "bluegrey", "lightteal", "yellowgrey", "brown", "red"})
	String[] meetingColors();
	
	@DefaultStringArrayValue({
		"Room Timetable", "Subject Timetable", "Curriculum Timetable", "Departmental Timetable", "Personal Timetable", "Course Timetable"
	})
	String[] resourceType();

	@DefaultStringArrayValue({
		"Room", "Subject", "Curriculum", "Department", "Person", "Course"
	})
	String[] resourceName();
	
	@DefaultStringArrayValue({
		"Class Event", "Final Examination Event", "Midterm Examination Event", "Course Related Event", "Special Event", "Not Available", "Message"
	})
	String[] eventTypeName();
	
	@DefaultStringArrayValue({
		"Class", "Final Examination", "Midterm Examination", "Course", "Special", "Not Available", "Message"
	})
	String[] eventTypeAbbv();
	
	@DefaultStringArrayValue({
		"Class", "Final", "Midterm", "Course", "Special", "N/A", "Message"
	})
	String[] eventTypeShort();
	
	@DefaultStringArrayValue({
		"Pending", "Approved", "Rejected", "Cancelled"
	})
	String[] eventApprovalStatus();

	// firstDay|lastDay|firstSlot|lastSlot|step
	@DefaultStringArrayValue({
			"Workdays \u00d7 Daytime|0|4|90|222|6",
			"All Week \u00d7 Daytime|0|6|90|222|6",
			"Workdays \u00d7 Evening|0|4|222|288|6",
			"All Week \u00d7 Evening|0|5|222|288|6",
			"All Week \u00d7 All Times|0|6|0|288|6"
	})
	String[] roomSharingModes();
	
	@DefaultStringValue("MMMM d")
	String weekSelectionDateFormat();
	
	@DefaultStringValue("EEEE MMMM d")
	String dateSelectionDateFormat();
	
	@DefaultStringValue("EEEE MMMM d yyyy")
	String singleDateSelectionFormat();
	
	@DefaultStringValue("M/d")
	String dateFormatShort();
	
	@DefaultStringValue("h:mma")
	String timeFormatShort();
}
