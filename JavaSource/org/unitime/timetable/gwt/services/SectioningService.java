/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2010 - 2013, UniTime LLC, and individual contributors
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

import org.unitime.timetable.gwt.client.sectioning.SectioningStatusFilterBox.SectioningStatusFilterRpcRequest;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.PageAccessException;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.SectioningProperties;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * @author Tomas Muller
 */
@RemoteServiceRelativePath("sectioning.gwt")
public interface SectioningService extends RemoteService {
	Collection<ClassAssignmentInterface.CourseAssignment> listCourseOfferings(Long sessionId, String query, Integer limit) throws SectioningException, PageAccessException;
	Collection<AcademicSessionProvider.AcademicSessionInfo> listAcademicSessions(boolean sectioning) throws SectioningException, PageAccessException;
	String retrieveCourseDetails(Long sessionId, String course) throws SectioningException, PageAccessException;
	Collection<ClassAssignmentInterface.ClassAssignment> listClasses(Long sessionId, String course) throws SectioningException, PageAccessException;
	Long retrieveCourseOfferingId(Long sessionId, String course) throws SectioningException, PageAccessException;
	Collection<String> checkCourses(boolean online, CourseRequestInterface request) throws SectioningException, PageAccessException;
	ClassAssignmentInterface section(boolean online, CourseRequestInterface request, ArrayList<ClassAssignmentInterface.ClassAssignment> currentAssignment) throws SectioningException, PageAccessException;
	Collection<ClassAssignmentInterface> computeSuggestions(boolean online, CourseRequestInterface request, Collection<ClassAssignmentInterface.ClassAssignment> currentAssignment, int selectedAssignment, String filter) throws SectioningException, PageAccessException;
	String logIn(String userName, String password, String pin) throws SectioningException, PageAccessException;
	Boolean logOut() throws SectioningException, PageAccessException;
	String whoAmI() throws SectioningException, PageAccessException;
	OnlineSectioningInterface.EligibilityCheck checkEligibility(boolean online, Long sessionId, Long studentId, String pin) throws SectioningException, PageAccessException;
	AcademicSessionProvider.AcademicSessionInfo lastAcademicSession(boolean sectioning) throws SectioningException, PageAccessException;
	CourseRequestInterface lastRequest(boolean online, Long sessionId) throws SectioningException, PageAccessException;
	ClassAssignmentInterface lastResult(boolean online, Long sessionId) throws SectioningException, PageAccessException;
    Boolean saveRequest(CourseRequestInterface request) throws SectioningException, PageAccessException;
    ClassAssignmentInterface enroll(CourseRequestInterface request, ArrayList<ClassAssignmentInterface.ClassAssignment> currentAssignment) throws SectioningException, PageAccessException;
    SectioningProperties getProperties(Long sessionId) throws SectioningException, PageAccessException;
	List<ClassAssignmentInterface.Enrollment> listEnrollments(Long offeringId) throws SectioningException, PageAccessException;
	ClassAssignmentInterface getEnrollment(boolean online, Long studentId) throws SectioningException, PageAccessException;
	List<Long> canApprove(Long classOrOfferingId) throws SectioningException, PageAccessException;
	String approveEnrollments(Long classOrOfferingId, List<Long> studentIds) throws SectioningException, PageAccessException;
	Boolean rejectEnrollments(Long classOrOfferingId, List<Long> studentIds) throws SectioningException, PageAccessException;
	List<ClassAssignmentInterface.EnrollmentInfo> findEnrollmentInfos(boolean online, String query, SectioningStatusFilterRpcRequest filter, Long courseId) throws SectioningException, PageAccessException;
	List<ClassAssignmentInterface.StudentInfo> findStudentInfos(boolean online, String query, SectioningStatusFilterRpcRequest filter) throws SectioningException, PageAccessException;
	List<ClassAssignmentInterface.Enrollment> findEnrollments(boolean online, String query, SectioningStatusFilterRpcRequest filter, Long courseId, Long classId) throws SectioningException, PageAccessException;
	List<String[]> querySuggestions(boolean online, String query, int limit) throws SectioningException, PageAccessException;
	String lastStatusQuery() throws SectioningException, PageAccessException;
	Long canEnroll(boolean online, Long studentId) throws SectioningException, PageAccessException;
	CourseRequestInterface savedRequest(boolean online, Long sessionId, Long studentId) throws SectioningException, PageAccessException;
	ClassAssignmentInterface savedResult(boolean online, Long sessionId, Long studentId) throws SectioningException, PageAccessException;
	Boolean selectSession(Long sessionId) throws SectioningException, PageAccessException;
	Map<String, String> lookupStudentSectioningStates() throws SectioningException, PageAccessException;
	Boolean sendEmail(Long studentId, String subject, String message, String cc) throws SectioningException, PageAccessException;
	Boolean changeStatus(List<Long> studentIds, String status) throws SectioningException, PageAccessException;
	List<ClassAssignmentInterface.SectioningAction> changeLog(String query) throws SectioningException, PageAccessException;
	Boolean massCancel(List<Long> studentIds, String status, String subject, String message, String cc) throws SectioningException, PageAccessException;
	Boolean requestStudentUpdate(List<Long> studentIds) throws SectioningException, PageAccessException;
}
