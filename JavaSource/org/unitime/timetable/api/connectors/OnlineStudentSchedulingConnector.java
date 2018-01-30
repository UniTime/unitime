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
package org.unitime.timetable.api.connectors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.unitime.timetable.api.ApiConnector;
import org.unitime.timetable.api.ApiHelper;
import org.unitime.timetable.gwt.client.sectioning.SectioningStatusFilterBox;
import org.unitime.timetable.gwt.server.UniTimePrincipal;
import org.unitime.timetable.gwt.services.SectioningService;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.DegreePlanInterface;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.CheckCoursesResponse;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.security.UserAuthority;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.NameFormat;

/**
 * @author Tomas Muller
 */
@Service("/api/sectioning")
public class OnlineStudentSchedulingConnector extends ApiConnector {
	
	protected @Autowired ApplicationContext applicationContext;
	
	protected SectioningService getSectioningService() {
		return applicationContext.getBean("sectioning.gwt", SectioningService.class);
	}
	
	protected void execute(ApiHelper helper, Flag type) throws IOException {
		String operation = helper.getParameter("operation");
		if (operation == null)
			throw new IllegalArgumentException("Parameter 'operation' was not provided.");
		
		Operation op = null;
		try {
			op = Operation.valueOf(operation);
		} catch (Exception e) {
			 new IllegalArgumentException("Operation '" + operation + "' is not a valid " + type.name() + " operation.");
		}
		if (op == null || !op.hasFlag(type))
			 new IllegalArgumentException("Operation '" + operation + "' is not a valid " + type.name() + " operation.");
		
		if (op.hasFlag(Flag.NO_SESSION)) {
			helper.getSessionContext().checkPermissionAnyAuthority(Right.ApiOnlineStudentScheduliung);
			op.execute(getSectioningService(), helper, type, null, null);
			return;
		}
		
		Long sessionId = helper.getAcademicSessionId();
		if (sessionId == null && helper.getSessionContext().getUser() != null)
			sessionId = helper.getSessionContext().getUser().getCurrentAcademicSessionId();
		if (sessionId == null)
			throw new IllegalArgumentException("Academic session not provided, please set the term parameter.");
		
		helper.getSessionContext().checkPermissionAnyAuthority(sessionId, "Session", Right.ApiOnlineStudentScheduliung);
		
		for (UserAuthority authority: helper.getSessionContext().getUser().getAuthorities())
			if (authority.getAcademicSession() != null && sessionId.equals(authority.getAcademicSession().getQualifierId()) && authority.hasRight(Right.ApiOnlineStudentScheduliung)) {
				helper.getSessionContext().getUser().setCurrentAuthority(authority);
				if (helper.getSessionContext().hasPermission(Right.StudentSchedulingAdvisor))
					break;
			}
		
		helper.getSessionContext().setAttribute("sessionId", sessionId);
		
		String studentId = helper.getParameter("studentId");
		UniTimePrincipal principal = null;
		if (studentId != null && helper.getSessionContext().hasPermission(Right.StudentSchedulingAdvisor)) {
			org.hibernate.Session hibSession = StudentDAO.getInstance().createNewSession();
			try {
				List<Student> student = hibSession.createQuery("select m from Student m where m.externalUniqueId = :uid").setString("uid", studentId).setCacheable(true).list();
				if (!student.isEmpty()) {
					UserContext user = helper.getSessionContext().getUser();
					principal = new UniTimePrincipal(user.getExternalUserId(), studentId, user.getName());
					for (Student s: student) {
						principal.addStudentId(s.getSession().getUniqueId(), s.getUniqueId());
						principal.setName(NameFormat.defaultFormat().format(s));
					}
					helper.getSessionContext().setAttribute("user", principal);
				} else {
					UserContext user = helper.getSessionContext().getUser();
					principal = new UniTimePrincipal(user.getExternalUserId(), studentId, user.getName());
					helper.getSessionContext().setAttribute("user", principal);
				}
			} finally {
				hibSession.close();
			}
		}
		
		String pin = helper.getParameter("pin");
		if (pin != null)
			helper.getSessionContext().setAttribute("pin", pin);
		
		op.execute(getSectioningService(), helper, type, sessionId, principal == null ? null : principal.getStudentId(sessionId));
	}

	@Override
	public void doGet(ApiHelper helper) throws IOException {
		execute(helper, Flag.GET);
	}
	
	@Override
	public void doPost(ApiHelper helper) throws IOException {
		execute(helper, Flag.POST);
	}
	
	public static enum Flag {
		GET,
		POST,
		NO_SESSION,
		;
		
		public int toInt() { return 1 << ordinal(); }
		public boolean has(int flags) { return (flags & toInt()) == toInt(); }
	}
	
	public static interface OpExecution<R> {
		public R execute(SectioningService service, ApiHelper helper, Flag type, Long sessionId, Long studentId) throws IOException;
	}
	
	public static enum Operation {
		listCourseOfferings(new OpExecution<Collection<ClassAssignmentInterface.CourseAssignment>>() {
			@Override
			public Collection<ClassAssignmentInterface.CourseAssignment> execute(SectioningService service, ApiHelper helper, Flag type, Long sessionId, Long studentId) throws IOException {
				return service.listCourseOfferings(sessionId, helper.getOptinalParameter("query", ""), helper.getOptinalParameterInteger("limit", null));
			}
		}, Flag.GET),
		listAcademicSessions(new OpExecution<Collection<AcademicSessionProvider.AcademicSessionInfo>>() {
			@Override
			public Collection<AcademicSessionProvider.AcademicSessionInfo> execute(SectioningService service, ApiHelper helper, Flag type, Long sessionId, Long studentId) throws IOException {
				return service.listAcademicSessions(helper.getOptinalParameterBoolean("sectioning", true));
			}
		}, Flag.GET, Flag.NO_SESSION),
		retrieveCourseDetails(new OpExecution<String>() {
			@Override
			public String execute(SectioningService service, ApiHelper helper, Flag type, Long sessionId, Long studentId) throws IOException {
				return service.retrieveCourseDetails(sessionId, helper.getRequiredParameter("course"));
			}
		}, Flag.GET),
		listClasses(new OpExecution<Collection<ClassAssignmentInterface.ClassAssignment>>() {
			@Override
			public Collection<ClassAssignmentInterface.ClassAssignment> execute(SectioningService service, ApiHelper helper, Flag type, Long sessionId, Long studentId) throws IOException {
				return service.listClasses(sessionId, helper.getRequiredParameter("course"));
			}
		}, Flag.GET),
		retrieveCourseOfferingId(new OpExecution<Long>() {
			@Override
			public Long execute(SectioningService service, ApiHelper helper, Flag type, Long sessionId, Long studentId) throws IOException {
				return service.retrieveCourseOfferingId(sessionId, helper.getRequiredParameter("course"));
			}
		}, Flag.GET),
		checkCourses(new OpExecution<CheckCoursesResponse>() {
			@Override
			public CheckCoursesResponse execute(SectioningService service, ApiHelper helper, Flag type, Long sessionId, Long studentId) throws IOException {
				CourseRequestInterface request = helper.getRequest(CourseRequestInterface.class);
				request.setAcademicSessionId(sessionId);
				if (request.getStudentId() == null && studentId != null)
					request.setStudentId(studentId);
				return service.checkCourses(helper.getOptinalParameterBoolean("online", true), helper.getOptinalParameterBoolean("sectioning", false), request);
			}
		}, Flag.POST),
		section(new OpExecution<ClassAssignmentInterface>() {
			@Override
			public ClassAssignmentInterface execute(SectioningService service, ApiHelper helper, Flag type, Long sessionId, Long studentId) throws IOException {
				SectionRequest request = helper.getRequest(SectionRequest.class);
				if (request.request != null) {
					request.request.setAcademicSessionId(sessionId);
					if (request.request.getStudentId() == null && studentId != null)
						request.request.setStudentId(studentId);
				}
				return service.section(
						helper.getOptinalParameterBoolean("online", request.online == null ? true : request.online),
						request.request,
						request.currentAssignment);
			}
		}, Flag.POST),
		computeSuggestions(new OpExecution<Collection<ClassAssignmentInterface>>() {
			@Override
			public Collection<ClassAssignmentInterface> execute(SectioningService service, ApiHelper helper, Flag type, Long sessionId, Long studentId) throws IOException {
				SectionRequest request = helper.getRequest(SectionRequest.class);
				if (request.request != null) {
					request.request.setAcademicSessionId(sessionId);
					if (request.request.getStudentId() == null && studentId != null)
						request.request.setStudentId(studentId);
				}
				return service.computeSuggestions(
						helper.getOptinalParameterBoolean("online", request.online == null ? true : request.online),
						request.request,
						request.currentAssignment,
						helper.getOptinalParameterInteger("selectedAssignment", request.selectedAssignment == null ? 0 : request.selectedAssignment),
						helper.getOptinalParameter("filter", request.filter));
			}
		}, Flag.POST),
		checkEligibility(new OpExecution<OnlineSectioningInterface.EligibilityCheck>() {
			@Override
			public OnlineSectioningInterface.EligibilityCheck execute(SectioningService service, ApiHelper helper, Flag type, Long sessionId, Long studentId) throws IOException {
				return service.checkEligibility(
						helper.getOptinalParameterBoolean("online", true),
						helper.getOptinalParameterBoolean("sectioning", false),
						sessionId,
						studentId,
						helper.getOptinalParameter("pin", null));
			}
		}, Flag.GET),
		saveRequest(new OpExecution<Boolean>() {
			@Override
			public Boolean execute(SectioningService service, ApiHelper helper, Flag type, Long sessionId, Long studentId) throws IOException {
				CourseRequestInterface request = helper.getRequest(CourseRequestInterface.class);
				request.setAcademicSessionId(sessionId);
				if (request.getStudentId() == null && studentId != null)
					request.setStudentId(studentId);
				return service.saveRequest(request);
			}
		}, Flag.POST),
		enroll(new OpExecution<ClassAssignmentInterface>() {
			@Override
			public ClassAssignmentInterface execute(SectioningService service, ApiHelper helper, Flag type, Long sessionId, Long studentId) throws IOException {
				SectionRequest request = helper.getRequest(SectionRequest.class);
				if (request.request != null) {
					request.request.setAcademicSessionId(sessionId);
					if (request.request.getStudentId() == null && studentId != null)
						request.request.setStudentId(studentId);
				}
				return service.enroll(
						helper.getOptinalParameterBoolean("online", request.online == null ? true : request.online),
						request.request,
						request.currentAssignment);
			}
		}, Flag.POST),
		getProperties(new OpExecution<OnlineSectioningInterface.SectioningProperties>() {
			@Override
			public OnlineSectioningInterface.SectioningProperties execute(SectioningService service, ApiHelper helper, Flag type, Long sessionId, Long studentId) throws IOException {
				return service.getProperties(sessionId);
			}
		}, Flag.GET),
		listEnrollments(new OpExecution<List<ClassAssignmentInterface.Enrollment>>() {
			@Override
			public List<ClassAssignmentInterface.Enrollment> execute(SectioningService service, ApiHelper helper, Flag type, Long sessionId, Long studentId) throws IOException {
				return service.listEnrollments(helper.getRequiredParameterLong("offeringId"));
			}
		}, Flag.GET),
		getEnrollment(new OpExecution<ClassAssignmentInterface>() {
			@Override
			public ClassAssignmentInterface execute(SectioningService service, ApiHelper helper, Flag type, Long sessionId, Long studentId) throws IOException {
				return service.getEnrollment(helper.getOptinalParameterBoolean("online", true), studentId);
			}
		}, Flag.GET),
		canApprove(new OpExecution<List<Long>>() {
			@Override
			public List<Long> execute(SectioningService service, ApiHelper helper, Flag type, Long sessionId, Long studentId) throws IOException {
				return service.canApprove(helper.getRequiredParameterLong("classOrOfferingId"));
			}
		}, Flag.GET),
		approveEnrollments(new OpExecution<String>() {
			@Override
			public String execute(SectioningService service, ApiHelper helper, Flag type, Long sessionId, Long studentId) throws IOException {
				ApproveEnrollmentsRequest request = null;
				if (Flag.POST == type)
					request = helper.getRequest(ApproveEnrollmentsRequest.class);
				if (request == null)
					request = new ApproveEnrollmentsRequest();
				if (request.studentIds == null) {
					request.studentIds = new ArrayList<Long>();
				}
				if (request.studentIds.isEmpty() && studentId != null)
					request.studentIds.add(studentId);
				return service.approveEnrollments(
						helper.getOptinalParameterLong("classOrOfferingId", request.classOrOfferingId),
						request.studentIds);
			}
		}, Flag.GET, Flag.POST),
		rejectEnrollments(new OpExecution<Boolean>() {
			@Override
			public Boolean execute(SectioningService service, ApiHelper helper, Flag type, Long sessionId, Long studentId) throws IOException {
				ApproveEnrollmentsRequest request = null;
				if (Flag.POST == type)
					request = helper.getRequest(ApproveEnrollmentsRequest.class);
				if (request == null)
					request = new ApproveEnrollmentsRequest();
				if (request.studentIds == null) {
					request.studentIds = new ArrayList<Long>();
				}
				if (request.studentIds.isEmpty() && studentId != null)
					request.studentIds.add(studentId);
				return service.rejectEnrollments(
						helper.getOptinalParameterLong("classOrOfferingId", request.classOrOfferingId),
						request.studentIds);
			}
		}, Flag.GET, Flag.POST),
		findEnrollmentInfos(new OpExecution<List<ClassAssignmentInterface.EnrollmentInfo>>() {
			@Override
			public List<ClassAssignmentInterface.EnrollmentInfo> execute(SectioningService service, ApiHelper helper, Flag type, Long sessionId, Long studentId) throws IOException {
				FindInfosRequest request = null;
				if (type == Flag.POST)
					request = helper.getRequest(FindInfosRequest.class);
				if (request == null)
					request = new FindInfosRequest();
				return service.findEnrollmentInfos(
						helper.getOptinalParameterBoolean("online", (request.online == null ? true : request.online)),
						helper.getOptinalParameter("query", (request.query == null ? "" : request.query)),
						request.filter,
						helper.getOptinalParameterLong("courseId", request.courseId));
			}
		}, Flag.GET, Flag.POST),
		findStudentInfos(new OpExecution<List<ClassAssignmentInterface.StudentInfo>>() {
			@Override
			public List<ClassAssignmentInterface.StudentInfo> execute(SectioningService service, ApiHelper helper, Flag type, Long sessionId, Long studentId) throws IOException {
				FindInfosRequest request = null;
				if (type == Flag.POST)
					request = helper.getRequest(FindInfosRequest.class);
				if (request == null)
					request = new FindInfosRequest();
				return service.findStudentInfos(
						helper.getOptinalParameterBoolean("online", (request.online == null ? true : request.online)),
						helper.getOptinalParameter("query", (request.query == null ? "" : request.query)),
						request.filter);
			}
		}, Flag.GET, Flag.POST),
		findEnrollments(new OpExecution<List<ClassAssignmentInterface.Enrollment>>() {
			@Override
			public List<ClassAssignmentInterface.Enrollment> execute(SectioningService service, ApiHelper helper, Flag type, Long sessionId, Long studentId) throws IOException {
				FindInfosRequest request = null;
				if (type == Flag.POST)
					request = helper.getRequest(FindInfosRequest.class);
				if (request == null)
					request = new FindInfosRequest();
				return service.findEnrollments(
						helper.getOptinalParameterBoolean("online", (request.online == null ? true : request.online)),
						helper.getOptinalParameter("query", (request.query == null ? "" : request.query)),
						request.filter,
						helper.getOptinalParameterLong("courseId", request.courseId),
						helper.getOptinalParameterLong("classId", request.classId));
			}
		}, Flag.GET, Flag.POST),
		querySuggestions(new OpExecution<List<String[]>>() {
			@Override
			public List<String[]> execute(SectioningService service, ApiHelper helper, Flag type, Long sessionId, Long studentId) throws IOException {
				return service.querySuggestions(helper.getOptinalParameterBoolean("online", true), helper.getOptinalParameter("query", ""), helper.getOptinalParameterInteger("limit", null));
			}
		}, Flag.GET),
		canEnroll(new OpExecution<Long>() {
			@Override
			public Long execute(SectioningService service, ApiHelper helper, Flag type, Long sessionId, Long studentId) throws IOException {
				return service.canEnroll(helper.getOptinalParameterBoolean("online", true), studentId);
			}
		}, Flag.GET),
		savedRequest(new OpExecution<CourseRequestInterface>() {
			@Override
			public CourseRequestInterface execute(SectioningService service, ApiHelper helper, Flag type, Long sessionId, Long studentId) throws IOException {
				return service.savedRequest(helper.getOptinalParameterBoolean("online", true), helper.getOptinalParameterBoolean("sectioning", true), sessionId, studentId);
			}
		}, Flag.GET),
		savedResult(new OpExecution<ClassAssignmentInterface>() {
			@Override
			public ClassAssignmentInterface execute(SectioningService service, ApiHelper helper, Flag type, Long sessionId, Long studentId) throws IOException {
				return service.savedResult(helper.getOptinalParameterBoolean("online", true), sessionId, studentId);
			}
		}, Flag.GET),
		lookupStudentSectioningStates(new OpExecution<Map<String, String>>() {
			@Override
			public Map<String, String> execute(SectioningService service, ApiHelper helper, Flag type, Long sessionId, Long studentId) throws IOException {
				return service.lookupStudentSectioningStates();
			}
		}, Flag.GET),
		sendEmail(new OpExecution<Boolean>() {
			@Override
			public Boolean execute(SectioningService service, ApiHelper helper, Flag type, Long sessionId, Long studentId) throws IOException {
				return service.sendEmail(studentId, helper.getOptinalParameter("subject", null), helper.getOptinalParameter("message", null), helper.getOptinalParameter("cc", null));
			}
		}, Flag.GET, Flag.POST),
		changeStatus(new OpExecution<Boolean>() {
			@Override
			public Boolean execute(SectioningService service, ApiHelper helper, Flag type, Long sessionId, Long studentId) throws IOException {
				ChangeStatusRequest request = helper.getRequest(ChangeStatusRequest.class);
				if (request == null)
					request = new ChangeStatusRequest();
				if (request.studentIds == null)
					request.studentIds = new ArrayList<Long>();
				if (request.studentIds.isEmpty() && studentId != null)
					request.studentIds.add(studentId);
				return service.changeStatus(request.studentIds, helper.getOptinalParameter("note", request.status), helper.getOptinalParameter("status", request.status));
			}
		}, Flag.GET, Flag.POST),
		changeLog(new OpExecution<List<ClassAssignmentInterface.SectioningAction>>() {
			@Override
			public List<ClassAssignmentInterface.SectioningAction> execute(SectioningService service, ApiHelper helper, Flag type, Long sessionId, Long studentId) throws IOException {
				String sid = helper.getOptinalParameter("studentId", null);
				return service.changeLog(helper.getOptinalParameter("query", (sid == null ? "" : "id:" + sid)));
			}
		}, Flag.GET),
		massCancel(new OpExecution<Boolean>() {
			@Override
			public Boolean execute(SectioningService service, ApiHelper helper, Flag type, Long sessionId, Long studentId) throws IOException {
				MassCancelRequest request = null;
				if (type == Flag.POST)
					request = helper.getRequest(MassCancelRequest.class);
				if (request == null)
					request = new MassCancelRequest();
				if (request.studentIds == null)
					request.studentIds = new ArrayList<Long>();
				if (request.studentIds.isEmpty() && studentId != null)
					request.studentIds.add(studentId);
				return service.massCancel(
						request.studentIds,
						helper.getOptinalParameter("status", request.status),
						helper.getOptinalParameter("subject", request.subject),
						helper.getOptinalParameter("message", request.message),
						helper.getOptinalParameter("cc", request.cc));
			}
		}, Flag.GET, Flag.POST),
		requestStudentUpdate(new OpExecution<Boolean>() {
			@Override
			public Boolean execute(SectioningService service, ApiHelper helper, Flag type, Long sessionId, Long studentId) throws IOException {
				StudentIdsRequest request = null;
				if (type == Flag.POST)
					request = helper.getRequest(StudentIdsRequest.class);
				if (request == null)
					request = new StudentIdsRequest();
				if (request.studentIds == null)
					request.studentIds = new ArrayList<Long>();
				if (request.studentIds.isEmpty() && studentId != null)
					request.studentIds.add(studentId);
				return service.requestStudentUpdate(request.studentIds);
			}
		}, Flag.GET, Flag.POST),
		listDegreePlans(new OpExecution<List<DegreePlanInterface>>() {
			@Override
			public List<DegreePlanInterface> execute(SectioningService service, ApiHelper helper, Flag type, Long sessionId, Long studentId) throws IOException {
				return service.listDegreePlans(helper.getOptinalParameterBoolean("online", true), sessionId, studentId);
			}
		}, Flag.GET),
		;
		
		int iFlags = 0;
		OpExecution iExecution = null;
		
		Operation(OpExecution execution, Flag... flags) {
			iExecution = execution;
			for (Flag f: flags) {
				iFlags |= f.toInt();
			}
		}
		
		public boolean hasFlag(Flag f) {
			return f.has(iFlags);
		}
		
		public void execute(SectioningService service, ApiHelper helper, Flag type, Long sessionId, Long studentId) throws IOException {
			helper.setResponse(iExecution.execute(service, helper, type, sessionId, studentId));
		}
	}

	@Override
	protected String getName() {
		return "sectioning";
	}
	
	public static class SectionRequest {
		Boolean online;
		CourseRequestInterface request;
		ArrayList<ClassAssignmentInterface.ClassAssignment> currentAssignment;
		Integer selectedAssignment;
		String filter;
	}
	
	public static class StudentIdsRequest {
		List<Long> studentIds;
	}
	
	public static class ApproveEnrollmentsRequest extends StudentIdsRequest {
		Long classOrOfferingId;
	}
	
	public static class ChangeStatusRequest extends StudentIdsRequest {
		String status;
	}
	
	public static class MassCancelRequest extends StudentIdsRequest {
		String status;
		String subject;
		String message;
		String cc;
	}
	
	public static class FindInfosRequest {
		Boolean online;
		String query;
		SectioningStatusFilterBox.SectioningStatusFilterRpcRequest filter;
		Long courseId;
		Long classId;
	}
}
