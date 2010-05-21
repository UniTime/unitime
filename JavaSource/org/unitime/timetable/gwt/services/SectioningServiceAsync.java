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
package org.unitime.timetable.gwt.services;

import java.util.ArrayList;
import java.util.Collection;

import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.SectioningException;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface SectioningServiceAsync {
	void listCourseOfferings(Long sessionId, String query, Integer limit, AsyncCallback<Collection<ClassAssignmentInterface.CourseAssignment>> callback) throws SectioningException;
	void listAcademicSessions(AsyncCallback<Collection<String[]>> callback) throws SectioningException;
	void retrieveCourseDetails(Long sessionId, String course, AsyncCallback<String> callback) throws SectioningException;
	void listClasses(Long sessionId, String course, AsyncCallback<Collection<ClassAssignmentInterface.ClassAssignment>> callback) throws IllegalArgumentException;
	void retrieveApplicationProperties(String startsWith, AsyncCallback<Collection<String[]>> callback) throws SectioningException;
	void retrieveCourseOfferingId(Long sessionId, String course, AsyncCallback<Long> callback) throws SectioningException;
	void section(CourseRequestInterface request, ArrayList<ClassAssignmentInterface.ClassAssignment> currentAssignment, AsyncCallback<ClassAssignmentInterface> callback) throws SectioningException;
	void checkCourses(CourseRequestInterface request, AsyncCallback<Collection<String>> callback) throws SectioningException;
	void computeSuggestions(CourseRequestInterface request, Collection<ClassAssignmentInterface.ClassAssignment> currentAssignment, int selectedAssignment, AsyncCallback<Collection<ClassAssignmentInterface>> callback) throws SectioningException;
	void logIn(String userName, String password, AsyncCallback<String> callback) throws SectioningException;
	void logOut(AsyncCallback<Boolean> callback) throws SectioningException;
	void whoAmI(AsyncCallback<String> callback) throws SectioningException;
	void lastAcademicSession(AsyncCallback<String[]> callback) throws SectioningException;
	void lastRequest(Long sessionId, AsyncCallback<CourseRequestInterface> callback) throws SectioningException;
	void lastResult(Long sessionId, AsyncCallback<ArrayList<ClassAssignmentInterface.ClassAssignment>> callback) throws SectioningException;
    void saveRequest(CourseRequestInterface request, AsyncCallback<Boolean> callback) throws SectioningException;
	void enroll(CourseRequestInterface request, ArrayList<ClassAssignmentInterface.ClassAssignment> currentAssignment, AsyncCallback<ArrayList<Long>> callback) throws SectioningException;
}
