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

import com.google.gwt.i18n.client.Messages;

public interface StudentSectioningExceptions extends Messages {
	@DefaultMessage("Course {0} does not exist.")
	String courseDoesNotExist(String course);
	
	@DefaultMessage("Academic session {0} does not exist.")
	String sessionDoesNotExist(String session);
	
	@DefaultMessage("Academic session not selected.")
	String noAcademicSession();
	
	@DefaultMessage("No suitable academic sessions found.")
	String noSuitableAcademicSessions();
	
	@DefaultMessage("No classes found for {0}.")
	String noClassesForCourse(String course);
	
	@DefaultMessage("Unable to compute a schedule ({0}).")
	String sectioningFailed(String message);
	
	@DefaultMessage("Too many bad attempts, login disabled.")
	String tooManyLoginAttempts();
	
	@DefaultMessage("User name not provided.")
	String loginNoUsername();
	
	@DefaultMessage("Wrong username and/or password.")
	String loginFailed();
	
	@DefaultMessage("Login failed ({0}).")
	String loginFailedUnknown(String message);
	
	@DefaultMessage("User is not logged in.")
	String userNotLoggedIn();

	@DefaultMessage("Unable to load section information ({0}).")
	String customSectionNamesFailed(String reason);
	
	@DefaultMessage("Unable to retrive course details ({0}).")
	String customCourseDetailsFailed(String reason);
	
	@DefaultMessage("Unable to retrive class details ({0}).")
	String customSectionLimitsFailed(String reason);

	@DefaultMessage("Course detail interface not provided.")
	String noCustomCourseDetails();
	
	@DefaultMessage("Last academic session failed ({0}).")
	String lastAcademicSessionFailed(String message);
	
	@DefaultMessage("Not a student.")
	String noStudent();
	
	@DefaultMessage("Wrong student id.")
	String badStudentId();
	
	@DefaultMessage("No requests stored for the student.")
	String noRequests();
	
	@DefaultMessage("Wrong academic session.")
	String badSession();
	
	@DefaultMessage("Your are not authenticated, please log in first.")
	String enrollNotAuthenticated();
	
	@DefaultMessage("Your are not registered as a student in {0}.")
	String enrollNotStudent(String session);
	
	@DefaultMessage("Unable to enroll into {0}, the class is no longer available.")
	String enrollNotAvailable(String clazz);
	
	@DefaultMessage("This feature is not supported in the current environment.")
	String notSupportedFeature();
	
	@DefaultMessage("No schedule stored for the student.")
	String noSchedule();
	
	@DefaultMessage("No courses provided.")
	String noCourse();
	
	@DefaultMessage("Unable to compute a schedule (no solution found).")
	String noSolution();

	@DefaultMessage("{0}")
	String unknown(String reason);
}
