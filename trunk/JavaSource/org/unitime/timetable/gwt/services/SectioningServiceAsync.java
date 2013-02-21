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
import java.util.Map;

import org.unitime.timetable.gwt.shared.AcademicSessionProvider;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.PageAccessException;
import org.unitime.timetable.gwt.shared.SectioningException;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Tomas Muller
 */
public interface SectioningServiceAsync {
	void listCourseOfferings(Long sessionId, String query, Integer limit, AsyncCallback<Collection<ClassAssignmentInterface.CourseAssignment>> callback) throws SectioningException, PageAccessException;
	void listAcademicSessions(boolean sectioning, AsyncCallback<Collection<AcademicSessionProvider.AcademicSessionInfo>> callback) throws SectioningException, PageAccessException;
	void retrieveCourseDetails(Long sessionId, String course, AsyncCallback<String> callback) throws SectioningException, PageAccessException;
	void listClasses(Long sessionId, String course, AsyncCallback<Collection<ClassAssignmentInterface.ClassAssignment>> callback) throws SectioningException, PageAccessException;
	void retrieveCourseOfferingId(Long sessionId, String course, AsyncCallback<Long> callback) throws SectioningException, PageAccessException;
	void section(boolean online, CourseRequestInterface request, ArrayList<ClassAssignmentInterface.ClassAssignment> currentAssignment, AsyncCallback<ClassAssignmentInterface> callback) throws SectioningException, PageAccessException;
	void checkCourses(boolean online, CourseRequestInterface request, AsyncCallback<Collection<String>> callback) throws SectioningException, PageAccessException;
	void computeSuggestions(boolean online, CourseRequestInterface request, Collection<ClassAssignmentInterface.ClassAssignment> currentAssignment, int selectedAssignment, String filter, AsyncCallback<Collection<ClassAssignmentInterface>> callback) throws SectioningException, PageAccessException;
	void logIn(String userName, String password, AsyncCallback<String> callback) throws SectioningException, PageAccessException;
	void logOut(AsyncCallback<Boolean> callback) throws SectioningException, PageAccessException;
	void whoAmI(AsyncCallback<String> callback) throws SectioningException, PageAccessException;
	void lastAcademicSession(boolean sectioning, AsyncCallback<AcademicSessionProvider.AcademicSessionInfo> callback) throws SectioningException, PageAccessException;
	void lastRequest(boolean online, Long sessionId, AsyncCallback<CourseRequestInterface> callback) throws SectioningException, PageAccessException;
	void lastResult(boolean online, Long sessionId, AsyncCallback<ClassAssignmentInterface> callback) throws SectioningException, PageAccessException;
    void saveRequest(CourseRequestInterface request, AsyncCallback<Boolean> callback) throws SectioningException, PageAccessException;
	void enroll(CourseRequestInterface request, ArrayList<ClassAssignmentInterface.ClassAssignment> currentAssignment, AsyncCallback<ClassAssignmentInterface> callback) throws SectioningException, PageAccessException;
	void isAdmin(AsyncCallback<Boolean> callback) throws SectioningException, PageAccessException;
	void isAdminOrAdvisor(AsyncCallback<Boolean> callback) throws SectioningException, PageAccessException;
	void listEnrollments(Long offeringId, AsyncCallback<List<ClassAssignmentInterface.Enrollment>> callback) throws SectioningException, PageAccessException;
	void getEnrollment(boolean online, Long studentId, AsyncCallback<ClassAssignmentInterface> callback) throws SectioningException, PageAccessException;
	void canApprove(Long classOrOfferingId, AsyncCallback<List<Long>> callback) throws SectioningException, PageAccessException;
	void approveEnrollments(Long classOrOfferingId, List<Long> studentIds, AsyncCallback<String> callback) throws SectioningException, PageAccessException;
	void rejectEnrollments(Long classOrOfferingId, List<Long> studentIds, AsyncCallback<Boolean> callback) throws SectioningException, PageAccessException;
	void findEnrollmentInfos(boolean online, String query, Long courseId, AsyncCallback<List<ClassAssignmentInterface.EnrollmentInfo>> callback) throws SectioningException, PageAccessException;
	void findStudentInfos(boolean online, String query, AsyncCallback<List<ClassAssignmentInterface.StudentInfo>> callback) throws SectioningException, PageAccessException;
	void findEnrollments(boolean online, String query, Long courseId, Long classId, AsyncCallback<List<ClassAssignmentInterface.Enrollment>> callback) throws SectioningException, PageAccessException;
	void querySuggestions(boolean online, String query, int limit, AsyncCallback<List<String[]>> callback) throws SectioningException, PageAccessException;
	void lastStatusQuery(AsyncCallback<String> callback) throws SectioningException, PageAccessException;
	void canEnroll(boolean online, Long studentId, AsyncCallback<Long> callback) throws SectioningException, PageAccessException;
	void savedRequest(boolean online, Long studentId, AsyncCallback<CourseRequestInterface> callback) throws SectioningException, PageAccessException;
	void savedResult(boolean online, Long studentId, AsyncCallback<ClassAssignmentInterface> callback) throws SectioningException, PageAccessException;
	void selectSession(Long sessionId, AsyncCallback<Boolean> callback) throws SectioningException, PageAccessException;
	void lookupStudentSectioningStates(AsyncCallback<Map<String, String>> callback) throws SectioningException, PageAccessException;
	void sendEmail(Long studentId, String subject, String message, String cc, AsyncCallback<Boolean> callback) throws SectioningException, PageAccessException;
	void changeStatus(List<Long> studentIds, String status, AsyncCallback<Boolean> callback) throws SectioningException, PageAccessException;
	void changeLog(String query, AsyncCallback<List<ClassAssignmentInterface.SectioningAction>> callback) throws SectioningException, PageAccessException;
	void massCancel(List<Long> studentIds, String status, String subject, String message, String cc, AsyncCallback<Boolean> callback) throws SectioningException, PageAccessException;
}
