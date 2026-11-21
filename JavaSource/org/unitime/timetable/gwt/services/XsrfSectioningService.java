package org.unitime.timetable.gwt.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.unitime.timetable.gwt.client.sectioning.SectioningStatusFilterBox.SectioningStatusFilterRpcRequest;
import org.unitime.timetable.gwt.command.client.XsrfBaseWrapper;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider.AcademicSessionInfo;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.CheckCoursesResponse;
import org.unitime.timetable.gwt.shared.DegreePlanInterface;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.AdvisingStudentDetails;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.AdvisorCourseRequestSubmission;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.AdvisorNote;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.SectioningProperties;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.StudentInfo;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.StudentSectioningContext;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.StudentStatusInfo;
import org.unitime.timetable.gwt.shared.PageAccessException;
import org.unitime.timetable.gwt.shared.ReservationException;
import org.unitime.timetable.gwt.shared.ReservationInterface;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.CancelSpecialRegistrationRequest;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.CancelSpecialRegistrationResponse;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.ChangeGradeModesRequest;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.ChangeGradeModesResponse;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.RetrieveAllSpecialRegistrationsRequest;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.RetrieveAvailableGradeModesRequest;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.RetrieveAvailableGradeModesResponse;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.RetrieveSpecialRegistrationResponse;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.SpecialRegistrationEligibilityRequest;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.SpecialRegistrationEligibilityResponse;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.SubmitSpecialRegistrationRequest;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.SubmitSpecialRegistrationResponse;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.UpdateSpecialRegistrationRequest;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.UpdateSpecialRegistrationResponse;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.VariableTitleCourseInfo;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.VariableTitleCourseRequest;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.VariableTitleCourseResponse;
import org.unitime.timetable.gwt.shared.StudentSchedulingPreferencesInterface;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.HasRpcToken;

public class XsrfSectioningService extends XsrfBaseWrapper implements SectioningServiceAsync  {
	SectioningServiceAsync iRPC;
	
	public XsrfSectioningService(SectioningServiceAsync rpc) {
		super((HasRpcToken)rpc);
		iRPC = rpc;
	}
	
	@Override
	public void listCourseOfferings(final StudentSectioningContext cx, final String query, final Integer limit, final AsyncCallback<Collection<ClassAssignmentInterface.CourseAssignment>> callback) throws SectioningException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.listCourseOfferings(cx, query, limit, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void listAcademicSessions(final boolean sectioning, final AsyncCallback<Collection<AcademicSessionProvider.AcademicSessionInfo>> callback)  throws SectioningException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.listAcademicSessions(sectioning, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void retrieveCourseDetails(final StudentSectioningContext cx, final String course, final AsyncCallback<String> callback)  throws SectioningException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.retrieveCourseDetails(cx, course, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void listClasses(final StudentSectioningContext cx, final String course, final AsyncCallback<Collection<ClassAssignmentInterface.ClassAssignment>> callback)  throws SectioningException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.listClasses(cx, course, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void retrieveCourseOfferingId(final Long sessionId, final String course, final AsyncCallback<Long> callback)  throws SectioningException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.retrieveCourseOfferingId(sessionId, course, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void section(final CourseRequestInterface request, final ArrayList<ClassAssignmentInterface.ClassAssignment> currentAssignment, final AsyncCallback<ClassAssignmentInterface> callback)  throws SectioningException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.section(request, currentAssignment, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void checkCourses(final CourseRequestInterface request, final AsyncCallback<CheckCoursesResponse> callback)  throws SectioningException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.checkCourses(request, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}
	
	@Override
	public void computeSuggestions(final CourseRequestInterface request, final Collection<ClassAssignmentInterface.ClassAssignment> currentAssignment, final int selectedAssignment, final String filter, final AsyncCallback<Collection<ClassAssignmentInterface>> callback) throws SectioningException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.computeSuggestions(request, currentAssignment, selectedAssignment, filter, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void logIn(final String userName, final String password, final String pin, final AsyncCallback<String> callback)  throws SectioningException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.logIn(userName, password, pin, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void logOut(final AsyncCallback<Boolean> callback)  throws SectioningException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.logOut(callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void whoAmI(final AsyncCallback<String> callback)  throws SectioningException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.whoAmI(callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void checkEligibility(final StudentSectioningContext cx, final AsyncCallback<OnlineSectioningInterface.EligibilityCheck> callback)  throws SectioningException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.checkEligibility(cx, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void lastAcademicSession(final boolean sectioning, final AsyncCallback<AcademicSessionProvider.AcademicSessionInfo> callback)  throws SectioningException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.lastAcademicSession(sectioning, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}
	
	@Override
    public void saveRequest(final CourseRequestInterface request, final AsyncCallback<CourseRequestInterface> callback) throws SectioningException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.saveRequest(request, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void enroll(final CourseRequestInterface request, final ArrayList<ClassAssignmentInterface.ClassAssignment> currentAssignment, final AsyncCallback<ClassAssignmentInterface> callback)  throws SectioningException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.enroll(request, currentAssignment, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void getProperties(final Long sessionId, final AsyncCallback<SectioningProperties> callback)  throws SectioningException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.getProperties(sessionId, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void listEnrollments(final Long offeringId, final AsyncCallback<List<ClassAssignmentInterface.Enrollment>> callback)  throws SectioningException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.listEnrollments(offeringId, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void getEnrollment(final boolean online, final Long studentId, final AsyncCallback<ClassAssignmentInterface> callback)  throws SectioningException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.getEnrollment(online, studentId, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void canApprove(final Long classOrOfferingId, final AsyncCallback<List<Long>> callback)  throws SectioningException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.canApprove(classOrOfferingId, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void approveEnrollments(final Long classOrOfferingId, final List<Long> studentIds, final AsyncCallback<String> callback)  throws SectioningException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.approveEnrollments(classOrOfferingId, studentIds, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void rejectEnrollments(final Long classOrOfferingId, final List<Long> studentIds, final AsyncCallback<Boolean> callback)  throws SectioningException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.rejectEnrollments(classOrOfferingId, studentIds, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}
	
	@Override
	public void findEnrollmentInfos(final boolean online, final String query, final SectioningStatusFilterRpcRequest filter, final Long courseId, final AsyncCallback<List<ClassAssignmentInterface.EnrollmentInfo>> callback) throws SectioningException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.findEnrollmentInfos(online, query, filter, courseId, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void findStudentInfos(final boolean online, final String query, final SectioningStatusFilterRpcRequest filter, final AsyncCallback<List<ClassAssignmentInterface.StudentInfo>> callback)  throws SectioningException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.findStudentInfos(online, query, filter, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void findEnrollments(final boolean online, final String query, final SectioningStatusFilterRpcRequest filter, final Long courseId, final Long classId, final AsyncCallback<List<ClassAssignmentInterface.Enrollment>> callback) throws SectioningException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.findEnrollments(online, query, filter, courseId, classId, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void canEnroll(final StudentSectioningContext cx, final AsyncCallback<Long> callback)  throws SectioningException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.canEnroll(cx, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void savedRequest(final StudentSectioningContext cx, final AsyncCallback<CourseRequestInterface> callback)  throws SectioningException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.savedRequest(cx, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void savedResult(final StudentSectioningContext cx, final AsyncCallback<ClassAssignmentInterface> callback)  throws SectioningException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.savedResult(cx, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void selectSession(final Long sessionId, final AsyncCallback<Boolean> callback)  throws SectioningException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.selectSession(sessionId, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void lookupStudentSectioningStates(final AsyncCallback<List<StudentStatusInfo>> callback)  throws SectioningException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.lookupStudentSectioningStates(callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void sendEmail(final Long sessionId, final Long studentId, final String subject, final String message, final String cc, final Boolean courseRequests, final Boolean classSchedule, final Boolean advisorRequests, final Boolean optional, final String sourceOperation, final AsyncCallback<Boolean> callback) throws SectioningException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.sendEmail(sessionId, studentId, subject, message, cc, courseRequests, classSchedule, advisorRequests, optional, sourceOperation, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void changeStatus(final List<Long> studentIds, final String note, final String status, final AsyncCallback<Boolean> callback)  throws SectioningException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.changeStatus(studentIds, note, status, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void changeStudentGroup(final List<Long> studentIds, final Long groupId, final boolean remove, final AsyncCallback<Boolean> callback)  throws SectioningException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.changeStudentGroup(studentIds, groupId, remove, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void changeLog(final String query, final AsyncCallback<List<ClassAssignmentInterface.SectioningAction>> callback)  throws SectioningException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.changeLog(query, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}
	
	@Override
	public void massCancel(final List<Long> studentIds, final String status, final String subject, final String message, final String cc, final AsyncCallback<Boolean> callback) throws SectioningException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.massCancel(studentIds, status, subject, message, cc, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void requestStudentUpdate(final List<Long> studentIds, final AsyncCallback<Boolean> callback)  throws SectioningException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.requestStudentUpdate(studentIds, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void reloadStudent(final List<Long> studentIds, final AsyncCallback<Boolean> callback)  throws SectioningException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.reloadStudent(studentIds, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void listDegreePlans(final StudentSectioningContext cx, final AsyncCallback<List<DegreePlanInterface>> callback)  throws SectioningException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.listDegreePlans(cx, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void lookupStudent(final boolean online, final String studentId, final AsyncCallback<ClassAssignmentInterface.Student> callback)  throws SectioningException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.lookupStudent(online, studentId, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void lookupStudent(final boolean online, final Long studentId, final AsyncCallback<ClassAssignmentInterface.Student> callback)  throws SectioningException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.lookupStudent(online, studentId, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void checkStudentOverrides(final List<Long> studentIds, final AsyncCallback<Boolean> callback)  throws SectioningException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.checkStudentOverrides(studentIds, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void validateStudentOverrides(final List<Long> studentIds, final AsyncCallback<Boolean> callback)  throws SectioningException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.validateStudentOverrides(studentIds, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void recheckCriticalCourses(final List<Long> studentIds, final AsyncCallback<Boolean> callback)  throws SectioningException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.recheckCriticalCourses(studentIds, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}
	

	@Override
	public void submitSpecialRequest(final SubmitSpecialRegistrationRequest request, final AsyncCallback<SubmitSpecialRegistrationResponse> callback)  throws SectioningException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.submitSpecialRequest(request, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void checkSpecialRequestEligibility(final SpecialRegistrationEligibilityRequest request, final AsyncCallback<SpecialRegistrationEligibilityResponse> callback)  throws SectioningException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.checkSpecialRequestEligibility(request, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void retrieveAllSpecialRequests(final RetrieveAllSpecialRegistrationsRequest request, final AsyncCallback<List<RetrieveSpecialRegistrationResponse>> callback)  throws SectioningException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.retrieveAllSpecialRequests(request, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void section(final CourseRequestInterface request, final List<ClassAssignmentInterface.ClassAssignment> currentAssignment, final List<ClassAssignmentInterface.ClassAssignment> specialRegistration, final AsyncCallback<ClassAssignmentInterface> callback)  throws SectioningException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.section(request, currentAssignment, specialRegistration, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void cancelSpecialRequest(final CancelSpecialRegistrationRequest request, final AsyncCallback<CancelSpecialRegistrationResponse> callback)  throws SectioningException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.cancelSpecialRequest(request, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void retrieveGradeModes(final RetrieveAvailableGradeModesRequest request, final AsyncCallback<RetrieveAvailableGradeModesResponse> callback)  throws SectioningException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.retrieveGradeModes(request, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void changeGradeModes(final ChangeGradeModesRequest request, final AsyncCallback<ChangeGradeModesResponse> callback)  throws SectioningException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.changeGradeModes(request, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void changeCriticalOverride(final Long studentId, final Long courseId, final Integer critical, final AsyncCallback<Integer> callback)  throws SectioningException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.changeCriticalOverride(studentId, courseId, critical, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void updateSpecialRequest(final UpdateSpecialRegistrationRequest request, final AsyncCallback<UpdateSpecialRegistrationResponse> callback)  throws SectioningException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.updateSpecialRequest(request, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void waitListCheckValidation(final CourseRequestInterface request, final AsyncCallback<CheckCoursesResponse> callback)  throws SectioningException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.waitListCheckValidation(request, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void waitListSubmitOverrides(final CourseRequestInterface request, final Float neededCredit, final AsyncCallback<CourseRequestInterface> callback)  throws SectioningException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.waitListSubmitOverrides(request, neededCredit, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}	
	

	@Override
	public void getStudentSessions(final String studentExternalId, final AsyncCallback<Collection<AcademicSessionInfo>> callback)  throws SectioningException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.getStudentSessions(studentExternalId, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void getStudentAdvisingDetails(final Long sessionId, final String studentExternalId, final AsyncCallback<AdvisingStudentDetails> callback)  throws SectioningException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.getStudentAdvisingDetails(sessionId, studentExternalId, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void getStudentInfo(final Long studentId, final AsyncCallback<StudentInfo> callback)  throws SectioningException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.getStudentInfo(studentId, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void checkAdvisingDetails(final AdvisingStudentDetails details, final AsyncCallback<CheckCoursesResponse> callback)  throws SectioningException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.checkAdvisingDetails(details, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void submitAdvisingDetails(final AdvisingStudentDetails details, final boolean emailStudent, final AsyncCallback<AdvisorCourseRequestSubmission> callback)  throws SectioningException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.submitAdvisingDetails(details, emailStudent, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void getAdvisorRequests(final StudentSectioningContext cx, final AsyncCallback<CourseRequestInterface> callback)  throws SectioningException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.getAdvisorRequests(cx, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void getReservations(final boolean online, final Long offeringId, final AsyncCallback<List<ReservationInterface>> callback)  throws ReservationException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.getReservations(online, offeringId, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void lastAdvisorNotes(final StudentSectioningContext cx, final AsyncCallback<List<AdvisorNote>> callback)  throws SectioningException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.lastAdvisorNotes(cx, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void getChangeLogTexts(final Collection<Long> logIds, final AsyncCallback<Map<Long, String>> callback)  throws SectioningException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.getChangeLogTexts(logIds, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void getChangeLogMessage(final Long logId, final AsyncCallback<String> callback)  throws SectioningException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.getChangeLogMessage(logId, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void listVariableTitleCourses(final StudentSectioningContext cx, final String query, final int limit, final AsyncCallback<Collection<VariableTitleCourseInfo>> callback)  throws SectioningException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.listVariableTitleCourses(cx, query, limit, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void getVariableTitleCourse(final StudentSectioningContext cx, final String course, final AsyncCallback<VariableTitleCourseInfo> callback)  throws SectioningException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.getVariableTitleCourse(cx, course, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void requestVariableTitleCourse(final VariableTitleCourseRequest request, final AsyncCallback<VariableTitleCourseResponse> callback)  throws SectioningException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.requestVariableTitleCourse(request, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void getCoursesFromRequest(final StudentSectioningContext cx, final CourseRequestInterface.Request query, final AsyncCallback<Collection<ClassAssignmentInterface.CourseAssignment>> callback)  throws SectioningException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.getCoursesFromRequest(cx, query, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}
	

	@Override
	public void getStudentSchedulingPreferences(final StudentSectioningContext cx, final AsyncCallback<StudentSchedulingPreferencesInterface> callback)  throws SectioningException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.getStudentSchedulingPreferences(cx, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void setStudentSchedulingPreferences(final StudentSectioningContext cx, final StudentSchedulingPreferencesInterface preferences, final AsyncCallback<Boolean> callback)  throws SectioningException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.setStudentSchedulingPreferences(cx, preferences, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Deprecated
	@Override
	public void querySuggestions(final boolean online, final String query, final int limit, final AsyncCallback<List<String[]>> callback) throws SectioningException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.querySuggestions(online, query, limit, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}
}
