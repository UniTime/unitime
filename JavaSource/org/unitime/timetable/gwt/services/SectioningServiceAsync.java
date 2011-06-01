/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
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
package org.unitime.timetable.gwt.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.SectioningException;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Tomas Muller
 */
public interface SectioningServiceAsync {
	void listCourseOfferings(Long sessionId, String query, Integer limit, AsyncCallback<Collection<ClassAssignmentInterface.CourseAssignment>> callback) throws SectioningException;
	void listAcademicSessions(boolean sectioning, AsyncCallback<Collection<String[]>> callback) throws SectioningException;
	void retrieveCourseDetails(Long sessionId, String course, AsyncCallback<String> callback) throws SectioningException;
	void listClasses(Long sessionId, String course, AsyncCallback<Collection<ClassAssignmentInterface.ClassAssignment>> callback) throws IllegalArgumentException;
	void retrieveCourseOfferingId(Long sessionId, String course, AsyncCallback<Long> callback) throws SectioningException;
	void section(CourseRequestInterface request, ArrayList<ClassAssignmentInterface.ClassAssignment> currentAssignment, AsyncCallback<ClassAssignmentInterface> callback) throws SectioningException;
	void checkCourses(CourseRequestInterface request, AsyncCallback<Collection<String>> callback) throws SectioningException;
	void computeSuggestions(CourseRequestInterface request, Collection<ClassAssignmentInterface.ClassAssignment> currentAssignment, int selectedAssignment, AsyncCallback<Collection<ClassAssignmentInterface>> callback) throws SectioningException;
	void logIn(String userName, String password, AsyncCallback<String> callback) throws SectioningException;
	void logOut(AsyncCallback<Boolean> callback) throws SectioningException;
	void whoAmI(AsyncCallback<String> callback) throws SectioningException;
	void lastAcademicSession(boolean sectioning, AsyncCallback<String[]> callback) throws SectioningException;
	void lastRequest(Long sessionId, AsyncCallback<CourseRequestInterface> callback) throws SectioningException;
	void lastResult(Long sessionId, AsyncCallback<ClassAssignmentInterface> callback) throws SectioningException;
    void saveRequest(CourseRequestInterface request, AsyncCallback<Boolean> callback) throws SectioningException;
	void enroll(CourseRequestInterface request, ArrayList<ClassAssignmentInterface.ClassAssignment> currentAssignment, AsyncCallback<ClassAssignmentInterface> callback) throws SectioningException;
	void isAdmin(AsyncCallback<Boolean> isAdmin) throws SectioningException;
	void listEnrollments(Long offeringId, AsyncCallback<List<ClassAssignmentInterface.Enrollment>> callback) throws SectioningException;
	void getEnrollment(Long studentId, AsyncCallback<ClassAssignmentInterface> callback) throws SectioningException;
}
