/*
 * UniTime 3.4 - 3.5 (University Timetabling Application)
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
package org.unitime.timetable.gwt.resources;

import com.google.gwt.i18n.client.Messages;

/**
 * @author Tomas Muller
 */
public interface GwtAriaMessages extends Messages {
	
	@DefaultMessage("{0} dialog opened.")
	String dialogOpened(String name);
	
	@DefaultMessage("{0} dialog closed.")
	String dialogClosed(String name);
	
	@DefaultMessage("Selected {0}")
	String suggestionSelected(String suggestion);

	@DefaultMessage("There is one suggestion {0}. Use enter to select it.")
	String showingOneSuggestion(String suggestion);
	
	@DefaultMessage("There are {0, number} suggestions to {1}. Use up and down arrows to navigate. First suggestion is {2}.")
	String showingMultipleSuggestions(@PluralCount int nbrSuggestions, String query, String suggestion);
	
	@DefaultMessage("There are {0, number} suggestions. Use up and down arrows to navigate. First suggestion is {1}.")
	String showingMultipleSuggestionsNoQuery(@PluralCount int nbrSuggestion, String suggestion);
	
	@DefaultMessage("There are {0, number} suggestions. Use up and down arrows to navigate.")
	String showingMultipleSuggestionsNoQueryNoneSelected(@PluralCount int nbrSuggestion);
	
	@DefaultMessage("There are {0, number} suggestions to {1}. Use up and down arrows to navigate.")
	String showingMultipleSuggestionsNoneSelected(@PluralCount int nbrSuggestions, String query);

	@DefaultMessage("Suggestion {0, number} of {1, number}. {2}")
	String onSuggestion(@PluralCount int index, @PluralCount int nbrSuggestions, String suggestion);
	
	@DefaultMessage("Suggestion: {0}")
	String onSuggestionNoCount(String suggestion);
	
	@DefaultMessage("Logged in as {0}, click here to log out.")
	String userAuthenticated(String user);
	
	@DefaultMessage("Logged in as guest, click here to log in.")
	String userGuest();
	
	@DefaultMessage("Not authenticated, click here to log in.")
	String userNotAuthenticated();

	@DefaultMessage("No academic session selected, click here to change the session.")
	String sessionNoSession();
	
	@DefaultMessage("Current academic session is {1} {0} campus {2}, click here to change the session.")
	String sessionCurrent(String year, String term, String campus);
	
	@DefaultMessage("User name")
	String propUserName();
	
	@DefaultMessage("Password")
	String propPassword();
	
	@DefaultMessage("Pin number")
	String propPinNumber();
	
	@DefaultMessage("User authentication dialog opened, please enter your user name and password.")
	String authenticationDialogOpened();
	
	@DefaultMessage("Log in as guest, no user name or password needed.")
	String buttonLogInAsGuest();
	
	@DefaultMessage("Academic session selection dialog opened, please select an academic session. Use Alt + Up and Alt + Down to navigate, Alt + Enter to confirm the selection.")
	String sessionSelectorDialogOpened();

	@DefaultMessage("Academic session selection dialog opened, please select an academic session. Use Alt + Up and Alt + Down to navigate, Alt + Enter to confirm the selection. Academic session {0} of {1}: {3} {2} campus {4}.")
	String sessionSelectorDialogOpenedWithSelection(@PluralCount int index, @PluralCount int nbrSuggestions, String year, String term, String campus);
	
	@DefaultMessage("Academic session {0} of {1}: {3} {2} campus {4}.")
	String sessionSelectorShowingSession(@PluralCount int index, @PluralCount int nbrSuggestions, String year, String term, String campus);
	
	@DefaultMessage("Academic session {1} {0} campus {2} selected.")
	String sessionSelectorDialogSelected(String year, String term, String campus);
	
	@DefaultMessage("Priority {0, number} course or free time request.")
	String titleRequestedCourse(@PluralCount int priority);
	
	@DefaultMessage("First alternative to the priority {0, number} course request.")
	String titleRequestedCourseFirstAlternative(@PluralCount int priority);

	@DefaultMessage("Second alternative to the priority {0, number} course request.")
	String titleRequestedCourseSecondAlternative(@PluralCount int priority);
	
	@DefaultMessage("Priority {0, number} alternate course request. Access key {1}.")
	String titleRequestedAlternate(@PluralCount int priority, String accessKey);
	
	@DefaultMessage("First alternative to the priority {0, number} alternate course request.")
	String titleRequestedAlternateFirstAlternative(@PluralCount int priority);

	@DefaultMessage("Second alternative to the priority {0, number} alternate course request.")
	String titleRequestedAlternateSecondAlternative(@PluralCount int priority);
	
	@DefaultMessage("Check to wait list priority {0, number} course request, if it is not available.")
	String titleRequestedWaitList(@PluralCount int priority);
	
	@DefaultMessage("Check to wait list for {0} course request.")
	String titleRequestedWaitListForCourse(String course);
	
	@DefaultMessage("Opens Course Finder dialog for priority {0, number} course or free time request.")
	String altRequestedCourseFinder(@PluralCount int priority);

	@DefaultMessage("Opens Course Finder dialog for priority {0, number} first alternative course request.")
	String altRequestedCourseFirstAlternativeFinder(@PluralCount int priority);

	@DefaultMessage("Opens Course Finder dialog for priority {0, number} second alternative course request.")
	String altRequestedCourseSecondAlternativeFinder(@PluralCount int priority);
	
	@DefaultMessage("Opens Course Finder dialog for priority {0, number} alternate course request.")
	String altRequestedAlternateFinder(@PluralCount int priority);

	@DefaultMessage("Opens Course Finder dialog for priority {0, number} first alternative alternate course request.")
	String altRequestedAlternateFirstFinder(@PluralCount int priority);

	@DefaultMessage("Opens Course Finder dialog for priority {0, number} second alternative alternate course request.")
	String altRequestedAlternateSecondFinder(@PluralCount int priority);
	
	@DefaultMessage("Swaps priority {0, number} course request with priority {1, number} course request including alternatives and wait list information.")
	String altSwapCourseRequest(@PluralCount int p1, @PluralCount int p2);
	
	@DefaultMessage("Swaps priority {0, number} alternate course request with priority {1, number} alternate course request including alternatives and wait list information.")
	String altSwapAlternateRequest(@PluralCount int p1, @PluralCount int p2);
	
	@DefaultMessage("Swaps priority {0, number} course request with priority {1, number} alternate course request including alternatives and wait list information.")
	String altSwapCourseAlternateRequest(@PluralCount int p1, @PluralCount int p2);	
	
	@DefaultMessage("Course Finder dialog opened.")
	String courseFinderDialogOpened();
	
	@DefaultMessage("Course Finder Filter. Enter a text to look for a course or a free time. Press Ctrl + Alt + C for course selection, Ctrl + Alt + T for free time selection.")
	String courseFinderFilterAllowsFreeTime();
	
	@DefaultMessage("Course Finder Filter. Enter a text to look for a course.")
	String courseFinderFilter();
	
	@DefaultMessage("Looking for a course. On a selected course press Ctrl + Alt + D for details, Ctrl + Alt + L for a list of classes.")
	String courseFinderCoursesTab();
	
	@DefaultMessage("Looking for a free time.")
	String courseFinderFreeTimeTab();
	
	@DefaultMessage("No course is selected.")
	String courseFinderNoCourse();
	
	@DefaultMessage("Course {0} of {1}: {2} {3}")
	String courseFinderSelected(@PluralCount int index, @PluralCount int nbrSuggestions, String subject, String course);

	@DefaultMessage("Course {0} of {1}: {2} {3} entitled {4}")
	String courseFinderSelectedWithTitle(@PluralCount int index, @PluralCount int nbrSuggestions, String subject, String course, String title);

	@DefaultMessage("Course {0} of {1}: {2} {3} entitled {4} with note {5}")
	String courseFinderSelectedWithTitleAndNote(@PluralCount int index, @PluralCount int nbrSuggestions, String subject, String course, String title, String note);

	@DefaultMessage("Course {0} of {1}: {2} {3} with note {4}")
	String courseFinderSelectedWithNote(@PluralCount int index, @PluralCount int nbrSuggestions, String subject, String course, String note);
	
	@DefaultMessage("Free Time {0}")
	String courseFinderSelectedFreeTime(String ft);
	
	@DefaultMessage("Check to pin the class {0} in the assigned time and room.")
	String classPin(String clazz);
	
	@DefaultMessage("Check to pin the class {0} in the assigned time and room.")
	String freeTimePin(String freeeTime);
	
	@DefaultMessage("Previous assignment of {0}: {1}")
	String previousAssignment(String clazz, String assignment);
	
	@DefaultMessage("{0}: {1}")
	String classAssignment(String clazz, String assignment);
	
	@DefaultMessage("Class {0} of {1}: {2}")
	String classSelected(@PluralCount int index, @PluralCount int nbrSuggestions, String clazz);
	
	@DefaultMessage("Free Time {0}")
	String freeTimeAssignment(String assignment);
	
	@DefaultMessage("{0}: {1}")
	String courseUnassginment(String clazz, String message);

	@DefaultMessage("Free Time {0}: {1}")
	String freeTimeUnassignment(String clazz, String message);
	
	@DefaultMessage("Available")
	String colLimit();
	
	@DefaultMessage("Current Time")
	String colTimeCurrent();
	
	@DefaultMessage("New Time")
	String colTimeNew();
	
	@DefaultMessage("Current Date")
	String colDateCurrent();
	
	@DefaultMessage("New Date")
	String colDateNew();

	@DefaultMessage("Current Room")
	String colRoomCurrent();
	
	@DefaultMessage("New Room")
	String colRoomNew();
	
	@DefaultMessage("There are no alternatives for {0}. Press Escape to hide alternatives.")
	String suggestionsNoAlternative(String source);

	@DefaultMessage("There are no alternatives for {0} matching {1}. Change the filter and click the Search button. Press Escape to hide alternatives.")
	String suggestionsNoAlternativeWithFilter(String source, String filter);
	
	@DefaultMessage("There are {0, number} alternatives to {1}. Use up and down arrows to navigate. To select an alternative press enter. Press Escape to hide alternatives. To filter alternatives type in a text and click the Search button.")
	String showingAlternatives(@PluralCount int nbrSuggestions, String query);
	
	@DefaultMessage("Alternative {0, number} of {1, number}. {2}")
	String showingAlternative(@PluralCount int index, @PluralCount int nbrAlternatives, String alternative);

	@DefaultMessage("Selected alternative {0}")
	String selectedAlternative(String alternative);
	
	@DefaultMessage("Assigned {0}. ")
	String assigned(String assignment);
	
	@DefaultMessage("Unassigned {0}. ")
	String unassigned(String assignment);
	
	@DefaultMessage("Arrange Hours")
	String arrangeHours();
	
	@DefaultMessage("{0} {1}")
	String courseFinderCourse(String subject, String course);

	@DefaultMessage("{0} {1} entitled {2}")
	String courseFinderCourseWithTitle(String subject, String course, String title);

	@DefaultMessage("{0} {1} entitled {2} with note {3}")
	String courseFinderCourseWithTitleAndNote(String subject, String course, String title, String note);

	@DefaultMessage("{0} {1} with note {2}")
	String courseFinderCourseWithNote(String subject, String course, String note);
	
	@DefaultMessage("{0} {1} available {2}")
	String courseFinderClassAvailable(String clazz, String assignment, String availability);
	
	@DefaultMessage("{0} {1} not available")
	String courseFinderClassNotAvailable(String clazz, String assignment);
	
	@DefaultMessage("Showing List of Classes. Use Alt + Up and Alt + Down to navigate, Alt + Enter to open Suggestions for the selected class.")
	String listOfClasses();
	
	@DefaultMessage("Showing Timetable grid.")
	String timetable();
	
	@DefaultMessage("Showing Course Requests.")
	String courseRequests();
	
	@DefaultMessage("add {0} {1}")
	String chipAdd(String command, String value);
	
	@DefaultMessage("change {0} to {1}")
	String chipReplace(String command, String value);
	
	@DefaultMessage("remove {0} {1}")
	String chipDelete(String command, String value);
	
	@DefaultMessage("blank")
	String emptyFilter();
	
	@DefaultMessage("Room filter: {0}")
	String roomFilter(String value);
	
	@DefaultMessage("Event filter: {0}")
	String eventFilter(String value);
	
	@DefaultMessage("Academic session: {0}")
	String academicSession(String value);
	
	@DefaultMessage("Weeks: {0}")
	String weekSelection(String value);

	@DefaultMessage("Rooms: {0}")
	String roomSelection(String value);
	
	@DefaultMessage("Tab {0} of {1}: {2}, press enter to select")
	String tabNotSelected(@PluralCount int index, @PluralCount int nbrTabs, String name);
	
	@DefaultMessage("Tab {0} of {1}: {2} selected")
	String tabSelected(@PluralCount int index, @PluralCount int nbrTabs, String name);
	
	@DefaultMessage("Selected tab {0}")
	String onTabSelected(String name);
	
	@DefaultMessage("Show column {0}")
	String opShow(String column);

	@DefaultMessage("Hide column {0}")
	String opHide(String column);
	
	@DefaultMessage("Enable {0}")
	String opCheck(String column);

	@DefaultMessage("Disable {0}")
	String opUncheck(String column);

	@DefaultMessage("Every {0} of {1}")
	String datesDayOfWeekSelection(String dayOfWeek, String month);
	
	@DefaultMessage("Week {0} starting {1}")
	String datesWeekSelection(@PluralCount int weekNumber, String firstDate);
	
	@DefaultMessage("No dates are selected.")
	String datesNothingSelected();
	
	@DefaultMessage("Selected {0}")
	String datesSelected(String date);
	
	@DefaultMessage("Unselected {0}")
	String datesUnselected(String date);
	
	@DefaultMessage("Selected work days for {0}")
	String datesSelectedWorkDays(String date);
	
	@DefaultMessage("Selected work days for {0} starting today")
	String datesSelectedWorkDaysFuture(String date);

	@DefaultMessage("Selected all but holidays for {0}")
	String datesSelectedAllButVacations(String date);

	@DefaultMessage("Selected all days for {0}")
	String datesSelectedAll(String date);
	
	@DefaultMessage("Selected all class days for {0}")
	String datesSelectedAllClassDays(String date);
	
	@DefaultMessage("Selected all class days for {0} starting today")
	String datesSelectedAllClassDaysFuture(String date);
	
	@DefaultMessage("Dates selection")
	String datesSelection();
	
	@DefaultMessage("Dates selection, selected {0}, cursor at {1}.")
	String datesSelectionWithSelection(String selection, String cursor);

	@DefaultMessage("Dates selection, no dates are selected, cursor at {0}.")
	String datesSelectionNoSelection(String cursor);
	
	@DefaultMessage("Dates selection, selected {0}")
	String datesSelectionWithSelectionNoCursor(String selection);
		
	@DefaultMessage("Start Time")
	String startTime();
	
	@DefaultMessage("End Time")
	String endTime();
	
	@DefaultMessage("{0} from {1} to {2} in {3} ({4})")
	String dateTimeRoomSelection(String date, String start, String end, String room, String hint);
	
	@DefaultMessage("Selected {0}")
	String selectedSelection(String selection);
	
	@DefaultMessage("{0} in {1}")
	String dateRoomSelection(String date, String room);
	
	@DefaultMessage("from {0} to {1}")
	String timeSelection(String start, String end);
	
	@DefaultMessage("available")
	String selectionAvailable();
	
	@DefaultMessage("Select meetings. Showing rooms {1} to {2} and {0} days. Use arrows to navigate, enter to select.")
	String meetingSelectionDescriptionRoomsHorizontal(@PluralCount int nrDays, @PluralCount int firstRoom, @PluralCount int lastRoom);
	
	@DefaultMessage("Select meetings. Showing days {1} to {2} and {0} rooms. Use arrows to navigate, enter to select.")
	String meetingSelectionDescriptionDatesHorizontal(@PluralCount int nrRooms, @PluralCount int firstDay, @PluralCount int lastDay);
	
	@DefaultMessage("Selected {0} in {1}")
	String dateRoomSelected(String date, String room);
	
	@DefaultMessage("Unselected {0} in {1}")
	String dateRoomUnselected(String date, String room);
	
	@DefaultMessage("Can not select {0} in {1}, room is not available.")
	String dateRoomCanNotSelect(String date, String room);
	
	@DefaultMessage("No meetings are selected.")
	String meetingSelectionNothingSelected();
	
	@DefaultMessage("Selected meetings: {0}.")
	String meetingSelectionSelected(String meetings);
	
	@DefaultMessage("Suggestion: {0}")
	String singleDateCursor(String date);
	
	@DefaultMessage("Selected: {0}")
	String singleDateSelected(String date);
	
	@DefaultMessage("Calendar opened, user arrows to navigate, enter to select the suggested date. Or type in a particular date. Showing {0}")
	String singleDatePopupOpenedNoDateSelected(String month);
	
	@DefaultMessage("Calendar opened, user arrows to navigate, enter to select the suggested date. Or type in a particular date. Suggestion: {0}")
	String singleDatePopupOpenedDateSelected(String date);

	@DefaultMessage("People lookup. Enter name, use arrows to navigate through suggested people, enter to select.")
	String peopleLookupName();
}
