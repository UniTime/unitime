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

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("sectioningService")
public interface SectioningService extends RemoteService {
	Collection<ClassAssignmentInterface.CourseAssignment> listCourseOfferings(Long sessionId, String query, Integer limit) throws SectioningException;
	Collection<String[]> listAcademicSessions() throws SectioningException;
	String retrieveCourseDetails(Long sessionId, String course) throws SectioningException;
	Collection<ClassAssignmentInterface.ClassAssignment> listClasses(Long sessionId, String course) throws IllegalArgumentException;
	Long retrieveCourseOfferingId(Long sessionId, String course) throws SectioningException;
	Collection<String> checkCourses(CourseRequestInterface request) throws SectioningException;
	ClassAssignmentInterface section(CourseRequestInterface request, ArrayList<ClassAssignmentInterface.ClassAssignment> currentAssignment) throws SectioningException;
	Collection<ClassAssignmentInterface> computeSuggestions(CourseRequestInterface request, Collection<ClassAssignmentInterface.ClassAssignment> currentAssignment, int selectedAssignment) throws SectioningException;
	String logIn(String userName, String password) throws SectioningException;
	Boolean logOut() throws SectioningException;
	String whoAmI() throws SectioningException;
	String[] lastAcademicSession() throws SectioningException;
	CourseRequestInterface lastRequest(Long sessionId) throws SectioningException;
	ArrayList<ClassAssignmentInterface.ClassAssignment> lastResult(Long sessionId) throws SectioningException;
    Boolean saveRequest(CourseRequestInterface request) throws SectioningException;
	ArrayList<Long> enroll(CourseRequestInterface request, ArrayList<ClassAssignmentInterface.ClassAssignment> currentAssignment) throws SectioningException;
}
