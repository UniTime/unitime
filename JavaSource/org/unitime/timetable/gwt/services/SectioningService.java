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

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * @author Tomas Muller
 */
@RemoteServiceRelativePath("sectioning.gwt")
public interface SectioningService extends RemoteService {
	Collection<ClassAssignmentInterface.CourseAssignment> listCourseOfferings(Long sessionId, String query, Integer limit) throws SectioningException;
	Collection<String[]> listAcademicSessions(boolean sectioning) throws SectioningException;
	String retrieveCourseDetails(Long sessionId, String course) throws SectioningException;
	Collection<ClassAssignmentInterface.ClassAssignment> listClasses(Long sessionId, String course) throws IllegalArgumentException;
	Long retrieveCourseOfferingId(Long sessionId, String course) throws SectioningException;
	Collection<String> checkCourses(CourseRequestInterface request) throws SectioningException;
	ClassAssignmentInterface section(CourseRequestInterface request, ArrayList<ClassAssignmentInterface.ClassAssignment> currentAssignment) throws SectioningException;
	Collection<ClassAssignmentInterface> computeSuggestions(CourseRequestInterface request, Collection<ClassAssignmentInterface.ClassAssignment> currentAssignment, int selectedAssignment) throws SectioningException;
	String logIn(String userName, String password) throws SectioningException;
	Boolean logOut() throws SectioningException;
	String whoAmI() throws SectioningException;
	String[] lastAcademicSession(boolean sectioning) throws SectioningException;
	CourseRequestInterface lastRequest(Long sessionId) throws SectioningException;
	ClassAssignmentInterface lastResult(Long sessionId) throws SectioningException;
    Boolean saveRequest(CourseRequestInterface request) throws SectioningException;
    ClassAssignmentInterface enroll(CourseRequestInterface request, ArrayList<ClassAssignmentInterface.ClassAssignment> currentAssignment) throws SectioningException;
	Boolean isAdmin() throws SectioningException;
	List<ClassAssignmentInterface.Enrollment> listEnrollments(Long offeringId) throws SectioningException;
	ClassAssignmentInterface getEnrollment(Long studentId) throws SectioningException;
}
