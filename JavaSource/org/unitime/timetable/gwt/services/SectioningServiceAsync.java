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
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.AdvisingStudentDetails;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.AdvisorCourseRequestSubmission;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.AdvisorNote;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.SectioningProperties;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.StudentInfo;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.StudentSectioningContext;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.StudentStatusInfo;
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
import org.unitime.timetable.gwt.shared.StudentSchedulingPreferencesInterface;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Tomas Muller
 */
public interface SectioningServiceAsync {
	void listCourseOfferings(StudentSectioningContext cx, String query, Integer limit, AsyncCallback<Collection<ClassAssignmentInterface.CourseAssignment>> callback) throws SectioningException, PageAccessException;
	void listAcademicSessions(boolean sectioning, AsyncCallback<Collection<AcademicSessionProvider.AcademicSessionInfo>> callback) throws SectioningException, PageAccessException;
	void retrieveCourseDetails(StudentSectioningContext cx, String course, AsyncCallback<String> callback) throws SectioningException, PageAccessException;
	void listClasses(StudentSectioningContext cx, String course, AsyncCallback<Collection<ClassAssignmentInterface.ClassAssignment>> callback) throws SectioningException, PageAccessException;
	void retrieveCourseOfferingId(Long sessionId, String course, AsyncCallback<Long> callback) throws SectioningException, PageAccessException;
	void section(CourseRequestInterface request, ArrayList<ClassAssignmentInterface.ClassAssignment> currentAssignment, AsyncCallback<ClassAssignmentInterface> callback) throws SectioningException, PageAccessException;
	void checkCourses(CourseRequestInterface request, AsyncCallback<CheckCoursesResponse> callback) throws SectioningException, PageAccessException;
	void computeSuggestions(CourseRequestInterface request, Collection<ClassAssignmentInterface.ClassAssignment> currentAssignment, int selectedAssignment, String filter, AsyncCallback<Collection<ClassAssignmentInterface>> callback) throws SectioningException, PageAccessException;
	void logIn(String userName, String password, String pin, AsyncCallback<String> callback) throws SectioningException, PageAccessException;
	void logOut(AsyncCallback<Boolean> callback) throws SectioningException, PageAccessException;
	void whoAmI(AsyncCallback<String> callback) throws SectioningException, PageAccessException;
	void checkEligibility(StudentSectioningContext cx, AsyncCallback<OnlineSectioningInterface.EligibilityCheck> callback) throws SectioningException, PageAccessException;
	void lastAcademicSession(boolean sectioning, AsyncCallback<AcademicSessionProvider.AcademicSessionInfo> callback) throws SectioningException, PageAccessException;
    void saveRequest(CourseRequestInterface request, AsyncCallback<CourseRequestInterface> callback) throws SectioningException, PageAccessException;
	void enroll(CourseRequestInterface request, ArrayList<ClassAssignmentInterface.ClassAssignment> currentAssignment, AsyncCallback<ClassAssignmentInterface> callback) throws SectioningException, PageAccessException;
	void getProperties(Long sessionId, AsyncCallback<SectioningProperties> callback) throws SectioningException, PageAccessException;
	void listEnrollments(Long offeringId, AsyncCallback<List<ClassAssignmentInterface.Enrollment>> callback) throws SectioningException, PageAccessException;
	void getEnrollment(boolean online, Long studentId, AsyncCallback<ClassAssignmentInterface> callback) throws SectioningException, PageAccessException;
	void canApprove(Long classOrOfferingId, AsyncCallback<List<Long>> callback) throws SectioningException, PageAccessException;
	void approveEnrollments(Long classOrOfferingId, List<Long> studentIds, AsyncCallback<String> callback) throws SectioningException, PageAccessException;
	void rejectEnrollments(Long classOrOfferingId, List<Long> studentIds, AsyncCallback<Boolean> callback) throws SectioningException, PageAccessException;
	void findEnrollmentInfos(boolean online, String query, SectioningStatusFilterRpcRequest filter, Long courseId, AsyncCallback<List<ClassAssignmentInterface.EnrollmentInfo>> callback) throws SectioningException, PageAccessException;
	void findStudentInfos(boolean online, String query, SectioningStatusFilterRpcRequest filter, AsyncCallback<List<ClassAssignmentInterface.StudentInfo>> callback) throws SectioningException, PageAccessException;
	void findEnrollments(boolean online, String query, SectioningStatusFilterRpcRequest filter, Long courseId, Long classId, AsyncCallback<List<ClassAssignmentInterface.Enrollment>> callback) throws SectioningException, PageAccessException;
	@Deprecated
	void querySuggestions(boolean online, String query, int limit, AsyncCallback<List<String[]>> callback) throws SectioningException, PageAccessException;
	void canEnroll(StudentSectioningContext cx, AsyncCallback<Long> callback) throws SectioningException, PageAccessException;
	void savedRequest(StudentSectioningContext cx, AsyncCallback<CourseRequestInterface> callback) throws SectioningException, PageAccessException;
	void savedResult(StudentSectioningContext cx, AsyncCallback<ClassAssignmentInterface> callback) throws SectioningException, PageAccessException;
	void selectSession(Long sessionId, AsyncCallback<Boolean> callback) throws SectioningException, PageAccessException;
	void lookupStudentSectioningStates(AsyncCallback<List<StudentStatusInfo>> callback) throws SectioningException, PageAccessException;
	void sendEmail(Long sessionId, Long studentId, String subject, String message, String cc, Boolean courseRequests, Boolean classSchedule, Boolean advisorRequests, Boolean optional, String sourceOperation, AsyncCallback<Boolean> callback) throws SectioningException, PageAccessException;
	void changeStatus(List<Long> studentIds, String note, String status, AsyncCallback<Boolean> callback) throws SectioningException, PageAccessException;
	void changeStudentGroup(List<Long> studentIds, Long groupId, boolean remove, AsyncCallback<Boolean> callback) throws SectioningException, PageAccessException;
	void changeLog(String query, AsyncCallback<List<ClassAssignmentInterface.SectioningAction>> callback) throws SectioningException, PageAccessException;
	void massCancel(List<Long> studentIds, String status, String subject, String message, String cc, AsyncCallback<Boolean> callback) throws SectioningException, PageAccessException;
	void requestStudentUpdate(List<Long> studentIds, AsyncCallback<Boolean> callback) throws SectioningException, PageAccessException;
	void reloadStudent(List<Long> studentIds, AsyncCallback<Boolean> callback) throws SectioningException, PageAccessException;
	void listDegreePlans(StudentSectioningContext cx, AsyncCallback<List<DegreePlanInterface>> callback) throws SectioningException, PageAccessException;
	void lookupStudent(boolean online, String studentId, AsyncCallback<ClassAssignmentInterface.Student> callback) throws SectioningException, PageAccessException;
	void lookupStudent(boolean online, Long studentId, AsyncCallback<ClassAssignmentInterface.Student> callback) throws SectioningException, PageAccessException;
	void checkStudentOverrides(List<Long> studentIds, AsyncCallback<Boolean> callback) throws SectioningException, PageAccessException;
	void validateStudentOverrides(List<Long> studentIds, AsyncCallback<Boolean> callback) throws SectioningException, PageAccessException;
	void recheckCriticalCourses(List<Long> studentIds, AsyncCallback<Boolean> callback) throws SectioningException, PageAccessException;
	
	void submitSpecialRequest(SubmitSpecialRegistrationRequest request, AsyncCallback<SubmitSpecialRegistrationResponse> callback) throws SectioningException, PageAccessException;
	void checkSpecialRequestEligibility(SpecialRegistrationEligibilityRequest request, AsyncCallback<SpecialRegistrationEligibilityResponse> callback) throws SectioningException, PageAccessException;
	void retrieveAllSpecialRequests(RetrieveAllSpecialRegistrationsRequest request, AsyncCallback<List<RetrieveSpecialRegistrationResponse>> callback) throws SectioningException, PageAccessException;
	void section(CourseRequestInterface request, List<ClassAssignmentInterface.ClassAssignment> currentAssignment, List<ClassAssignmentInterface.ClassAssignment> specialRegistration, AsyncCallback<ClassAssignmentInterface> callback) throws SectioningException, PageAccessException;
	void cancelSpecialRequest(CancelSpecialRegistrationRequest request, AsyncCallback<CancelSpecialRegistrationResponse> callback) throws SectioningException, PageAccessException;
	void retrieveGradeModes(RetrieveAvailableGradeModesRequest request, AsyncCallback<RetrieveAvailableGradeModesResponse> callback) throws SectioningException, PageAccessException;
	void changeGradeModes(ChangeGradeModesRequest request, AsyncCallback<ChangeGradeModesResponse> callback) throws SectioningException, PageAccessException;
	void changeCriticalOverride(Long studentId, Long courseId, Integer critical, AsyncCallback<Integer> callback) throws SectioningException, PageAccessException;
	void updateSpecialRequest(UpdateSpecialRegistrationRequest request, AsyncCallback<UpdateSpecialRegistrationResponse> callback) throws SectioningException, PageAccessException;
	void waitListCheckValidation(CourseRequestInterface request, AsyncCallback<CheckCoursesResponse> callback) throws SectioningException, PageAccessException;
	void waitListSubmitOverrides(CourseRequestInterface request, Float neededCredit, AsyncCallback<CourseRequestInterface> callback) throws SectioningException, PageAccessException;	
	
	void getStudentSessions(String studentExternalId, AsyncCallback<Collection<AcademicSessionInfo>> callback) throws SectioningException, PageAccessException;
	void getStudentAdvisingDetails(Long sessionId, String studentExternalId, AsyncCallback<AdvisingStudentDetails> callback) throws SectioningException, PageAccessException;
	void getStudentInfo(Long studentId, AsyncCallback<StudentInfo> callback) throws SectioningException, PageAccessException;
	void checkAdvisingDetails(AdvisingStudentDetails details, AsyncCallback<CheckCoursesResponse> callback) throws SectioningException, PageAccessException;
	void submitAdvisingDetails(AdvisingStudentDetails details, boolean emailStudent, AsyncCallback<AdvisorCourseRequestSubmission> callback) throws SectioningException, PageAccessException;
	void getAdvisorRequests(StudentSectioningContext cx, AsyncCallback<CourseRequestInterface> callback) throws SectioningException, PageAccessException;
	void getReservations(boolean online, Long offeringId, AsyncCallback<List<ReservationInterface>> callback) throws ReservationException, PageAccessException;
	void lastAdvisorNotes(StudentSectioningContext cx, AsyncCallback<List<AdvisorNote>> callback) throws SectioningException, PageAccessException;
	void getChangeLogTexts(Collection<Long> logIds, AsyncCallback<Map<Long, String>> callback) throws SectioningException, PageAccessException;
	void getChangeLogMessage(Long logId, AsyncCallback<String> callback) throws SectioningException, PageAccessException;
	void listVariableTitleCourses(StudentSectioningContext cx, String query, int limit, AsyncCallback<Collection<VariableTitleCourseInfo>> callback) throws SectioningException, PageAccessException;
	void getVariableTitleCourse(StudentSectioningContext cx, String course, AsyncCallback<VariableTitleCourseInfo> callback) throws SectioningException, PageAccessException;
	void requestVariableTitleCourse(VariableTitleCourseRequest request, AsyncCallback<VariableTitleCourseResponse> callback) throws SectioningException, PageAccessException;
	void getCoursesFromRequest(StudentSectioningContext cx, CourseRequestInterface.Request query, AsyncCallback<Collection<ClassAssignmentInterface.CourseAssignment>> callback) throws SectioningException, PageAccessException;
	
	void getStudentSchedulingPreferences(StudentSectioningContext cx, AsyncCallback<StudentSchedulingPreferencesInterface> callback) throws SectioningException, PageAccessException;
	void setStudentSchedulingPreferences(StudentSectioningContext cx, StudentSchedulingPreferencesInterface preferences, AsyncCallback<Boolean> callback) throws SectioningException, PageAccessException;
}
