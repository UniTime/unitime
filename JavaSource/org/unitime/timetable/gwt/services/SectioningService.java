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
package org.unitime.timetable.gwt.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.unitime.timetable.gwt.client.sectioning.SectioningStatusFilterBox.SectioningStatusFilterRpcRequest;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.CheckCoursesResponse;
import org.unitime.timetable.gwt.shared.DegreePlanInterface;
import org.unitime.timetable.gwt.shared.PageAccessException;
import org.unitime.timetable.gwt.shared.ReservationException;
import org.unitime.timetable.gwt.shared.ReservationInterface;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider.AcademicSessionInfo;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.SpecialRegistrationEligibilityRequest;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.SpecialRegistrationEligibilityResponse;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.CancelSpecialRegistrationRequest;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.CancelSpecialRegistrationResponse;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.ChangeGradeModesRequest;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.ChangeGradeModesResponse;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.RetrieveAllSpecialRegistrationsRequest;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.RetrieveAvailableGradeModesRequest;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.RetrieveAvailableGradeModesResponse;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.RetrieveSpecialRegistrationResponse;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.SubmitSpecialRegistrationRequest;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.SubmitSpecialRegistrationResponse;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.UpdateSpecialRegistrationRequest;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.UpdateSpecialRegistrationResponse;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.VariableTitleCourseInfo;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.VariableTitleCourseRequest;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.VariableTitleCourseResponse;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.AdvisingStudentDetails;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.AdvisorCourseRequestSubmission;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.AdvisorNote;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.SectioningProperties;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.StudentInfo;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.StudentSectioningContext;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.StudentStatusInfo;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * @author Tomas Muller
 */
@RemoteServiceRelativePath("sectioning.gwt")
public interface SectioningService extends RemoteService {
	Collection<ClassAssignmentInterface.CourseAssignment> listCourseOfferings(StudentSectioningContext cx, String query, Integer limit) throws SectioningException, PageAccessException;
	Collection<AcademicSessionProvider.AcademicSessionInfo> listAcademicSessions(boolean sectioning) throws SectioningException, PageAccessException;
	String retrieveCourseDetails(StudentSectioningContext cx, String course) throws SectioningException, PageAccessException;
	Collection<ClassAssignmentInterface.ClassAssignment> listClasses(StudentSectioningContext cx, String course) throws SectioningException, PageAccessException;
	Long retrieveCourseOfferingId(Long sessionId, String course) throws SectioningException, PageAccessException;
	CheckCoursesResponse checkCourses(CourseRequestInterface request) throws SectioningException, PageAccessException;
	ClassAssignmentInterface section(CourseRequestInterface request, ArrayList<ClassAssignmentInterface.ClassAssignment> currentAssignment) throws SectioningException, PageAccessException;
	Collection<ClassAssignmentInterface> computeSuggestions(CourseRequestInterface request, Collection<ClassAssignmentInterface.ClassAssignment> currentAssignment, int selectedAssignment, String filter) throws SectioningException, PageAccessException;
	String logIn(String userName, String password, String pin) throws SectioningException, PageAccessException;
	Boolean logOut() throws SectioningException, PageAccessException;
	String whoAmI() throws SectioningException, PageAccessException;
	OnlineSectioningInterface.EligibilityCheck checkEligibility(StudentSectioningContext cx) throws SectioningException, PageAccessException;
	AcademicSessionProvider.AcademicSessionInfo lastAcademicSession(boolean sectioning) throws SectioningException, PageAccessException;
	CourseRequestInterface saveRequest(CourseRequestInterface request) throws SectioningException, PageAccessException;
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
	Long canEnroll(StudentSectioningContext cx) throws SectioningException, PageAccessException;
	CourseRequestInterface savedRequest(StudentSectioningContext cx) throws SectioningException, PageAccessException;
	ClassAssignmentInterface savedResult(StudentSectioningContext cx) throws SectioningException, PageAccessException;
	Boolean selectSession(Long sessionId) throws SectioningException, PageAccessException;
	List<StudentStatusInfo> lookupStudentSectioningStates() throws SectioningException, PageAccessException;
	Boolean sendEmail(Long sessionId, Long studentId, String subject, String message, String cc, Boolean courseRequests, Boolean classSchedule, Boolean advisorRequests, Boolean optional) throws SectioningException, PageAccessException;
	Boolean changeStatus(List<Long> studentIds, String note, String status) throws SectioningException, PageAccessException;
	Boolean changeStudentGroup(List<Long> studentIds, Long groupId, boolean remove) throws SectioningException, PageAccessException;
	List<ClassAssignmentInterface.SectioningAction> changeLog(String query) throws SectioningException, PageAccessException;
	Boolean massCancel(List<Long> studentIds, String status, String subject, String message, String cc) throws SectioningException, PageAccessException;
	Boolean requestStudentUpdate(List<Long> studentIds) throws SectioningException, PageAccessException;
	Boolean reloadStudent(List<Long> studentIds) throws SectioningException, PageAccessException;
	List<DegreePlanInterface> listDegreePlans(StudentSectioningContext cx) throws SectioningException, PageAccessException;
	ClassAssignmentInterface.Student lookupStudent(boolean online, String studentId) throws SectioningException, PageAccessException;
	ClassAssignmentInterface.Student lookupStudent(boolean online, Long studentId) throws SectioningException, PageAccessException;
	Boolean checkStudentOverrides(List<Long> studentIds) throws SectioningException, PageAccessException;
	Boolean validateStudentOverrides(List<Long> studentIds) throws SectioningException, PageAccessException;
	Boolean recheckCriticalCourses(List<Long> studentIds) throws SectioningException, PageAccessException;
	
	SubmitSpecialRegistrationResponse submitSpecialRequest(SubmitSpecialRegistrationRequest request) throws SectioningException, PageAccessException;
	SpecialRegistrationEligibilityResponse checkSpecialRequestEligibility(SpecialRegistrationEligibilityRequest request) throws SectioningException, PageAccessException;
	List<RetrieveSpecialRegistrationResponse> retrieveAllSpecialRequests(RetrieveAllSpecialRegistrationsRequest request) throws SectioningException, PageAccessException;
	ClassAssignmentInterface section(CourseRequestInterface request, List<ClassAssignmentInterface.ClassAssignment> currentAssignment, List<ClassAssignmentInterface.ClassAssignment> specialRegistration) throws SectioningException, PageAccessException;
	CancelSpecialRegistrationResponse cancelSpecialRequest(CancelSpecialRegistrationRequest request) throws SectioningException, PageAccessException;
	RetrieveAvailableGradeModesResponse retrieveGradeModes(RetrieveAvailableGradeModesRequest request) throws SectioningException, PageAccessException;
	ChangeGradeModesResponse changeGradeModes(ChangeGradeModesRequest request) throws SectioningException, PageAccessException;
	Integer changeCriticalOverride(Long studentId, Long courseId, Integer critical) throws SectioningException, PageAccessException;
	UpdateSpecialRegistrationResponse updateSpecialRequest(UpdateSpecialRegistrationRequest request) throws SectioningException, PageAccessException;
	CheckCoursesResponse waitListCheckValidation(CourseRequestInterface request) throws SectioningException, PageAccessException;
	CourseRequestInterface waitListSubmitOverrides(CourseRequestInterface request, Float neededCredit) throws SectioningException, PageAccessException;
	
	Collection<AcademicSessionInfo> getStudentSessions(String studentExternalId) throws SectioningException, PageAccessException;
	AdvisingStudentDetails getStudentAdvisingDetails(Long sessionId, String studentExternalId) throws SectioningException, PageAccessException;
	StudentInfo getStudentInfo(Long studentId) throws SectioningException, PageAccessException;
	CheckCoursesResponse checkAdvisingDetails(AdvisingStudentDetails details) throws SectioningException, PageAccessException;
	AdvisorCourseRequestSubmission submitAdvisingDetails(AdvisingStudentDetails details, boolean emailStudent) throws SectioningException, PageAccessException;
	CourseRequestInterface getAdvisorRequests(StudentSectioningContext cx) throws SectioningException, PageAccessException;
	List<ReservationInterface> getReservations(boolean online, Long offeringId) throws ReservationException, PageAccessException;
	List<AdvisorNote> lastAdvisorNotes(StudentSectioningContext cx) throws SectioningException, PageAccessException;
	Map<Long, String> getChangeLogTexts(Collection<Long> logIds) throws SectioningException, PageAccessException;
	String getChangeLogMessage(Long logId) throws SectioningException, PageAccessException;
	Collection<VariableTitleCourseInfo> listVariableTitleCourses(StudentSectioningContext cx, String query, int limit) throws SectioningException, PageAccessException;
	VariableTitleCourseInfo getVariableTitleCourse(StudentSectioningContext cx, String course) throws SectioningException, PageAccessException;
	VariableTitleCourseResponse requestVariableTitleCourse(VariableTitleCourseRequest request) throws SectioningException, PageAccessException;
}
