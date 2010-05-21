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
package org.unitime.timetable.gwt.shared;

import org.unitime.timetable.gwt.resources.StudentSectioningExceptions;

import com.google.gwt.core.client.GWT;

public enum SectioningExceptionType {
	COURSE_NOT_EXIST,
	SESSION_NOT_EXIST,
	NO_ACADEMIC_SESSION,
	NO_SUITABLE_ACADEMIC_SESSIONS,
	NO_CLASSES_FOR_COURSE,
	SECTIONING_FAILED,
	LOGIN_TOO_MANY_ATTEMPTS,
	LOGIN_NO_USERNAME,
	LOGIN_FAILED,
	LOGIN_FAILED_UNKNOWN,
	USER_NOT_LOGGED_IN,
	LAST_ACADEMIC_SESSION_FAILED,
	NO_STUDENT,
	BAD_STUDENT_ID,
	NO_STUDENT_REQUESTS,
	NO_STUDENT_SCHEDULE,
	BAD_SESSION,
	ENROLL_NOT_AUTHENTICATED,
	ENROLL_NOT_STUDENT,
	ENROLL_NOT_AVAILABLE,
	FEATURE_NOT_SUPPORTED,
	EMPTY_COURSE_REQUEST,
	NO_SOLUTION,
	UNKNOWN,
	CUSTOM_SECTION_NAMES_FAILURE,
	CUSTOM_COURSE_DETAILS_FAILURE,
	CUSTOM_SECTION_LIMITS_FAILURE,
	NO_CUSTOM_COURSE_DETAILS;
	
	private static StudentSectioningExceptions sMessages = null;
	
	static {
		try {
			sMessages = GWT.create(StudentSectioningExceptions.class);
		} catch (UnsupportedOperationException e) {}
	}
	
	public String message(String problem) {
		if (sMessages == null) return name() + (problem == null ? "" : ": " + problem);
		switch (this) {
		case COURSE_NOT_EXIST: return sMessages.courseDoesNotExist(problem);
		case SESSION_NOT_EXIST: return sMessages.sessionDoesNotExist(problem);
		case NO_ACADEMIC_SESSION: return sMessages.noAcademicSession();
		case NO_SUITABLE_ACADEMIC_SESSIONS: return sMessages.noSuitableAcademicSessions();
		case NO_CLASSES_FOR_COURSE: return sMessages.noClassesForCourse(problem);
		case SECTIONING_FAILED: return sMessages.sectioningFailed(problem);
		case CUSTOM_SECTION_NAMES_FAILURE: return sMessages.customSectionNamesFailed(problem);
		case LOGIN_TOO_MANY_ATTEMPTS: return sMessages.tooManyLoginAttempts();
		case LOGIN_NO_USERNAME: return sMessages.loginNoUsername();
		case LOGIN_FAILED: return sMessages.loginFailed();
		case LOGIN_FAILED_UNKNOWN: return sMessages.loginFailedUnknown(problem);
		case USER_NOT_LOGGED_IN: return sMessages.userNotLoggedIn();
		case LAST_ACADEMIC_SESSION_FAILED: return sMessages.lastAcademicSessionFailed(problem);
		case NO_STUDENT: return sMessages.noStudent();
		case BAD_STUDENT_ID: return sMessages.badStudentId();
		case NO_STUDENT_REQUESTS: return sMessages.noRequests();
		case BAD_SESSION: return sMessages.badSession();
		case NO_STUDENT_SCHEDULE: return sMessages.noSchedule();
		case ENROLL_NOT_AUTHENTICATED: return sMessages.enrollNotAuthenticated();
		case ENROLL_NOT_STUDENT: return sMessages.enrollNotStudent(problem);
		case ENROLL_NOT_AVAILABLE: return sMessages.enrollNotAvailable(problem);
		case FEATURE_NOT_SUPPORTED: return sMessages.notSupportedFeature();
		case EMPTY_COURSE_REQUEST: return sMessages.noCourse();
		case NO_SOLUTION: return sMessages.noSolution();
		case CUSTOM_COURSE_DETAILS_FAILURE: return sMessages.customCourseDetailsFailed(problem);
		case NO_CUSTOM_COURSE_DETAILS: return sMessages.noCustomCourseDetails();
		case CUSTOM_SECTION_LIMITS_FAILURE: return sMessages.customSectionLimitsFailed(problem);
		case UNKNOWN: return sMessages.unknown(problem);
		default: return name() + (problem == null ? "" : ": " + problem);
		}
	}
}
