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
package org.unitime.timetable.gwt.resources;

import com.google.gwt.i18n.client.Constants;

/**
 * @author Tomas Muller
 */
public interface GwtConstants extends Constants {
	
	@DefaultStringValue("4.1")
	String version();

	@DefaultStringValue("&copy; 2008 - 2015 The Apereo Foundation")
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
	
	@DefaultStringValue("MMM d, yyyy")
	String sessionDateFormat();
	
	@DefaultStringValue("MM/dd/yyyy")
	String dateEntryFormat();
	
	@DefaultStringArrayValue({
		"Override: Allow Time Conflict", "Override: Can Assign Over Limit", "Override: Time Conflict & Over Limit"
	})
	String[] reservationOverrideTypeName();
	
	@DefaultStringArrayValue({
		"Time Conflict", "Over Limit", "Time & Limit"
	})
	String[] reservationOverrideTypeAbbv();
}
