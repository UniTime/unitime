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
package org.unitime.timetable.gwt.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.cpsolver.coursett.model.Placement;
import org.hibernate.CacheMode;
import org.hibernate.SessionFactory;
import org.hibernate.type.LongType;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.SecurityMessages;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.client.sectioning.SectioningStatusFilterBox.SectioningStatusFilterRpcRequest;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.services.SectioningService;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.ClassAssignment;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.EnrollmentInfo;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.SectioningAction;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.CheckCoursesResponse;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestedCourse;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestedCourseStatus;
import org.unitime.timetable.gwt.shared.DegreePlanInterface;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.AdvisingStudentDetails;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.AdvisorCourseRequestSubmission;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.AdvisorNote;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.EligibilityCheck;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.EligibilityCheck.EligibilityFlag;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.GradeMode;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.SectioningProperties;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.StudentGroupInfo;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.StudentInfo;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.StudentSectioningContext;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.StudentStatusInfo;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.WaitListMode;
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
import org.unitime.timetable.interfaces.ExternalClassNameHelperInterface.HasGradableSubpart;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.CourseAssignment;
import org.unitime.timetable.model.Advisor;
import org.unitime.timetable.model.AdvisorCourseRequest;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseCreditUnitConfig;
import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.CourseRequest;
import org.unitime.timetable.model.CourseType;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.FixedCreditUnitConfig;
import org.unitime.timetable.model.IndividualReservation;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.LearningCommunityReservation;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Reservation;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentAccomodation;
import org.unitime.timetable.model.StudentAreaClassificationMajor;
import org.unitime.timetable.model.StudentAreaClassificationMinor;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.StudentClassPref;
import org.unitime.timetable.model.StudentGroup;
import org.unitime.timetable.model.StudentGroupReservation;
import org.unitime.timetable.model.StudentGroupType;
import org.unitime.timetable.model.StudentInstrMthPref;
import org.unitime.timetable.model.StudentNote;
import org.unitime.timetable.model.StudentSectioningPref;
import org.unitime.timetable.model.StudentSectioningStatus;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.StudentSectioningStatus.Option;
import org.unitime.timetable.model.comparators.ClassComparator;
import org.unitime.timetable.model.dao.AdvisorCourseRequestDAO;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.CourseDemandDAO;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.CourseTypeDAO;
import org.unitime.timetable.model.dao.CurriculumDAO;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.model.dao.OnlineSectioningLogDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.model.dao.StudentGroupDAO;
import org.unitime.timetable.onlinesectioning.AcademicSessionInfo;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLogger;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.advisors.AdvisorCourseRequestsSubmit;
import org.unitime.timetable.onlinesectioning.advisors.AdvisorCourseRequestsValidate;
import org.unitime.timetable.onlinesectioning.advisors.AdvisorGetCourseRequests;
import org.unitime.timetable.onlinesectioning.basic.CheckCourses;
import org.unitime.timetable.onlinesectioning.basic.CheckEligibility;
import org.unitime.timetable.onlinesectioning.basic.CourseRequestEligibility;
import org.unitime.timetable.onlinesectioning.basic.GetAssignment;
import org.unitime.timetable.onlinesectioning.basic.GetDegreePlans;
import org.unitime.timetable.onlinesectioning.basic.GetRequest;
import org.unitime.timetable.onlinesectioning.basic.ListClasses;
import org.unitime.timetable.onlinesectioning.basic.ListCourseOfferings;
import org.unitime.timetable.onlinesectioning.basic.ListEnrollments;
import org.unitime.timetable.onlinesectioning.custom.CourseDetailsProvider;
import org.unitime.timetable.onlinesectioning.custom.CourseMatcherProvider;
import org.unitime.timetable.onlinesectioning.custom.CustomClassAttendanceProvider;
import org.unitime.timetable.onlinesectioning.custom.CriticalCoursesProvider.CriticalCourses;
import org.unitime.timetable.onlinesectioning.custom.CustomClassAttendanceProvider.StudentClassAttendance;
import org.unitime.timetable.onlinesectioning.custom.CustomCourseLookupHolder;
import org.unitime.timetable.onlinesectioning.custom.CustomCourseRequestsHolder;
import org.unitime.timetable.onlinesectioning.custom.CustomCourseRequestsValidationHolder;
import org.unitime.timetable.onlinesectioning.custom.CustomCriticalCoursesHolder;
import org.unitime.timetable.onlinesectioning.custom.CustomDegreePlansHolder;
import org.unitime.timetable.onlinesectioning.custom.CustomSpecialRegistrationHolder;
import org.unitime.timetable.onlinesectioning.custom.CustomStudentEnrollmentHolder;
import org.unitime.timetable.onlinesectioning.custom.CustomWaitListValidationHolder;
import org.unitime.timetable.onlinesectioning.custom.Customization;
import org.unitime.timetable.onlinesectioning.custom.ExternalTermProvider;
import org.unitime.timetable.onlinesectioning.custom.RequestStudentUpdates;
import org.unitime.timetable.onlinesectioning.custom.SpecialRegistrationDashboardUrlProvider;
import org.unitime.timetable.onlinesectioning.custom.SpecialRegistrationProvider;
import org.unitime.timetable.onlinesectioning.custom.StudentEmailProvider;
import org.unitime.timetable.onlinesectioning.custom.StudentHoldsCheckProvider;
import org.unitime.timetable.onlinesectioning.custom.VariableTitleCourseProvider;
import org.unitime.timetable.onlinesectioning.match.AbstractCourseMatcher;
import org.unitime.timetable.onlinesectioning.model.XCourse;
import org.unitime.timetable.onlinesectioning.model.XCourseId;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.model.XStudentId;
import org.unitime.timetable.onlinesectioning.server.DatabaseServer;
import org.unitime.timetable.onlinesectioning.solver.ComputeSuggestionsAction;
import org.unitime.timetable.onlinesectioning.solver.FindAssignmentAction;
import org.unitime.timetable.onlinesectioning.specreg.SpecialRegistrationCancel;
import org.unitime.timetable.onlinesectioning.specreg.SpecialRegistrationChangeGradeModes;
import org.unitime.timetable.onlinesectioning.specreg.SpecialRegistrationEligibility;
import org.unitime.timetable.onlinesectioning.specreg.SpecialRegistrationRequestVariableTitleCourse;
import org.unitime.timetable.onlinesectioning.specreg.SpecialRegistrationRetrieveAll;
import org.unitime.timetable.onlinesectioning.specreg.SpecialRegistrationRetrieveGradeModes;
import org.unitime.timetable.onlinesectioning.specreg.SpecialRegistrationSubmit;
import org.unitime.timetable.onlinesectioning.specreg.SpecialRegistrationUpdate;
import org.unitime.timetable.onlinesectioning.specreg.WaitListCheckValidation;
import org.unitime.timetable.onlinesectioning.specreg.WaitListSubmitOverrides;
import org.unitime.timetable.onlinesectioning.status.FindEnrollmentAction;
import org.unitime.timetable.onlinesectioning.status.FindEnrollmentInfoAction;
import org.unitime.timetable.onlinesectioning.status.FindStudentInfoAction;
import org.unitime.timetable.onlinesectioning.status.GetReservationsAction;
import org.unitime.timetable.onlinesectioning.status.FindOnlineSectioningLogAction;
import org.unitime.timetable.onlinesectioning.status.StatusPageSuggestionsAction;
import org.unitime.timetable.onlinesectioning.status.db.DbFindEnrollmentAction;
import org.unitime.timetable.onlinesectioning.status.db.DbFindEnrollmentInfoAction;
import org.unitime.timetable.onlinesectioning.status.db.DbFindOnlineSectioningLogAction;
import org.unitime.timetable.onlinesectioning.status.db.DbFindStudentInfoAction;
import org.unitime.timetable.onlinesectioning.updates.ApproveEnrollmentsAction;
import org.unitime.timetable.onlinesectioning.updates.ChangeStudentGroup;
import org.unitime.timetable.onlinesectioning.updates.ChangeStudentStatus;
import org.unitime.timetable.onlinesectioning.updates.EnrollStudent;
import org.unitime.timetable.onlinesectioning.updates.MassCancelAction;
import org.unitime.timetable.onlinesectioning.updates.RejectEnrollmentsAction;
import org.unitime.timetable.onlinesectioning.updates.ReloadStudent;
import org.unitime.timetable.onlinesectioning.updates.SaveStudentRequests;
import org.unitime.timetable.onlinesectioning.updates.StudentEmail;
import org.unitime.timetable.security.Qualifiable;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.UserAuthority;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.UserContext.Chameleon;
import org.unitime.timetable.security.authority.OtherAuthority;
import org.unitime.timetable.security.context.AnonymousUserContext;
import org.unitime.timetable.security.qualifiers.SimpleQualifier;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.service.ProxyHolder;
import org.unitime.timetable.solver.service.SolverServerService;
import org.unitime.timetable.solver.service.SolverService;
import org.unitime.timetable.solver.studentsct.BatchEnrollStudent;
import org.unitime.timetable.solver.studentsct.StudentSolverProxy;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.util.LoginManager;
import org.unitime.timetable.util.NameFormat;

import com.google.protobuf.InvalidProtocolBufferException;

/**
 * @author Tomas Muller
 */
@Service("sectioning.gwt")
public class SectioningServlet implements SectioningService, DisposableBean {
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
	private static StudentSectioningConstants CONSTANTS = Localization.create(StudentSectioningConstants.class);
	private static SecurityMessages SEC_MSG = Localization.create(SecurityMessages.class);
	private static Logger sLog = Logger.getLogger(SectioningServlet.class);
	
	public SectioningServlet() {
	}
	
	private CourseDetailsProvider getCourseDetailsProvider() {
		return Customization.CourseDetailsProvider.getProvider();
	}
	
	private CourseMatcherProvider getCourseMatcherProvider() {
		return Customization.CourseMatcherProvider.getProvider();
	}
	
	private ExternalTermProvider getExternalTermProvider() {
		return Customization.ExternalTermProvider.getProvider();
	}
	
	private @Autowired AuthenticationManager authenticationManager;
	private AuthenticationManager getAuthenticationManager() { return authenticationManager; }
	private @Autowired SessionContext sessionContext;
	private SessionContext getSessionContext() { return sessionContext; }
	private @Autowired SolverService<StudentSolverProxy> studentSectioningSolverService;
	private StudentSolverProxy getStudentSolver() { return studentSectioningSolverService.getSolver(); }
	private @Autowired SolverServerService solverServerService;
	private OnlineSectioningServer getServerInstance(Long academicSessionId, boolean canReturnDummy) {
		if (academicSessionId == null) return null;
		ApplicationProperties.setSessionId(academicSessionId);
		OnlineSectioningServer server =  solverServerService.getOnlineStudentSchedulingContainer().getSolver(academicSessionId.toString());
		if (server != null || !canReturnDummy) return server;
		
		SessionAttribute attribute = SessionAttribute.OnlineSchedulingDummyServer;
		ProxyHolder<Long, OnlineSectioningServer> h = (ProxyHolder<Long, OnlineSectioningServer>)sessionContext.getAttribute(attribute);
		if (h != null && h.isValid(academicSessionId))
			return h.getProxy();
		
		Session session = SessionDAO.getInstance().get(academicSessionId);
		if (session == null)
			throw new SectioningException(MSG.exceptionBadSession()); 
		server = new DatabaseServer(new AcademicSessionInfo(session), false);
		sessionContext.setAttribute(attribute, new ProxyHolder<Long, OnlineSectioningServer>(academicSessionId, server));
		
		return server;
	}
	
	public Collection<ClassAssignmentInterface.CourseAssignment> listCourseOfferings(StudentSectioningContext cx, String query, Integer limit) throws SectioningException, PageAccessException {
		checkContext(cx);
		if (cx.getSessionId()==null) throw new SectioningException(MSG.exceptionNoAcademicSession());
		
		OnlineSectioningServer server = getServerInstance(cx.getSessionId(), false);
		
		CourseMatcher matcher = getCourseMatcher(cx, server);
		
		if (server == null || server instanceof DatabaseServer) {
			org.hibernate.Session hibSession = CurriculumDAO.getInstance().getSession();
			if (query != null && !query.isEmpty() && CustomCourseLookupHolder.hasProvider()) {
				try {
					List<CourseOffering> courses = CustomCourseLookupHolder.getProvider().getCourses(new AcademicSessionInfo(SessionDAO.getInstance().get(cx.getSessionId(), hibSession)), hibSession, query, true);
					if (courses != null && !courses.isEmpty()) {
						ArrayList<ClassAssignmentInterface.CourseAssignment> results = new ArrayList<ClassAssignmentInterface.CourseAssignment>();
						for (CourseOffering c: courses) {
							if (matcher.match(new XCourseId(c))) {
								CourseAssignment course = new CourseAssignment();
								course.setCourseId(c.getUniqueId());
								course.setSubject(c.getSubjectAreaAbbv());
								course.setCourseNbr(c.getCourseNbr());
								course.setTitle(c.getTitle());
								course.setNote(c.getScheduleBookNote());
								if (c.getCredit() != null) {
									course.setCreditText(c.getCredit().creditText());
									course.setCreditAbbv(c.getCredit().creditAbbv());
								}
								course.setTitle(c.getTitle());
								course.setHasUniqueName(true);
								course.setHasCrossList(c.getInstructionalOffering().hasCrossList());
								course.setCanWaitList(c.getInstructionalOffering().effectiveWaitList());
								boolean unlimited = false;
								int courseLimit = 0;
								int snapshotLimit = 0;
								for (Iterator<InstrOfferingConfig> i = c.getInstructionalOffering().getInstrOfferingConfigs().iterator(); i.hasNext(); ) {
									InstrOfferingConfig cfg = i.next();
									if (cfg.isUnlimitedEnrollment()) unlimited = true;
									if (cfg.getLimit() != null) courseLimit += cfg.getLimit();
									Integer snapshot = cfg.getSnapShotLimit();
									if (snapshot != null)
										snapshotLimit += snapshot.intValue();
									
								}
								if (c.getReservation() != null)
									courseLimit = c.getReservation();
					            if (courseLimit >= 9999) unlimited = true;
								course.setLimit(unlimited ? -1 : courseLimit);
								course.setSnapShotLimit(snapshotLimit);
								course.setProjected(c.getProjectedDemand());
								course.setEnrollment(c.getEnrollment());
								course.setLastLike(c.getDemand());
								results.add(course);
								for (InstrOfferingConfig config: c.getInstructionalOffering().getInstrOfferingConfigs()) {
									if (config.getEffectiveInstructionalMethod() != null)
										course.addInstructionalMethod(config.getEffectiveInstructionalMethod().getUniqueId(), config.getEffectiveInstructionalMethod().getLabel());
									else
										course.setHasNoInstructionalMethod(true);
								}
							}
						}
						if (!results.isEmpty()) {
							ListCourseOfferings.setSelection(results);
							return results;
						}
					}
				} catch (Exception e) {
					sLog.error("Failed to use the custom course lookup: " + e.getMessage(), e);
				}
			}
			
			String types = "";
			for (String ref: matcher.getAllowedCourseTypes())
				types += (types.isEmpty() ? "" : ", ") + "'" + ref + "'";
			if (!matcher.isAllCourseTypes() && !matcher.isNoCourseType() && types.isEmpty()) throw new SectioningException(MSG.exceptionCourseDoesNotExist(query));
			
			boolean excludeNotOffered = ApplicationProperty.CourseRequestsShowNotOffered.isFalse();
			ArrayList<ClassAssignmentInterface.CourseAssignment> results = new ArrayList<ClassAssignmentInterface.CourseAssignment>();
			org.unitime.timetable.onlinesectioning.match.CourseMatcher parent = matcher.getParentCourseMatcher();
			for (CourseOffering c: (List<CourseOffering>)hibSession.createQuery(
					"select c from CourseOffering c left outer join c.courseType ct where " +
					(excludeNotOffered ? "c.instructionalOffering.notOffered is false and " : "") +
					"c.subjectArea.session.uniqueId = :sessionId and c.subjectArea.department.allowStudentScheduling = true and (" +
					"(lower(c.subjectArea.subjectAreaAbbreviation || ' ' || c.courseNbr) like :q || '%' or lower(c.subjectArea.subjectAreaAbbreviation || ' ' || c.courseNbr || ' - ' || c.title) like :q || '%') " +
					(query.length()>2 ? "or lower(c.title) like '%' || :q || '%'" : "") + ") " +
					(matcher.isAllCourseTypes() ? "" : matcher.isNoCourseType() ? types.isEmpty() ? " and ct is null " : " and (ct is null or ct.reference in (" + types + ")) " : " and ct.reference in (" + types + ") ") +
					"order by case " +
					"when lower(c.subjectArea.subjectAreaAbbreviation || ' ' || c.courseNbr) like :q || '%' then 0 else 1 end," + // matches on course name first
					"c.subjectArea.subjectAreaAbbreviation, c.courseNbr")
					.setString("q", query.toLowerCase())
					.setLong("sessionId", cx.getSessionId())
					.setCacheable(true).setMaxResults(limit == null || limit <= 0 || parent != null ? Integer.MAX_VALUE : limit).list()) {
				if (parent != null && !parent.match(new XCourseId(c))) continue;
				CourseAssignment course = new CourseAssignment();
				course.setCourseId(c.getUniqueId());
				course.setSubject(c.getSubjectAreaAbbv());
				course.setCourseNbr(c.getCourseNbr());
				course.setTitle(c.getTitle());
				course.setNote(c.getScheduleBookNote());
				if (c.getCredit() != null) {
					course.setCreditText(c.getCredit().creditText());
					course.setCreditAbbv(c.getCredit().creditAbbv());
				}
				course.setTitle(c.getTitle());
				course.setHasUniqueName(true);
				course.setHasCrossList(c.getInstructionalOffering().hasCrossList());
				course.setCanWaitList(c.getInstructionalOffering().effectiveWaitList());
				boolean unlimited = false;
				int courseLimit = 0;
				for (Iterator<InstrOfferingConfig> i = c.getInstructionalOffering().getInstrOfferingConfigs().iterator(); i.hasNext(); ) {
					InstrOfferingConfig cfg = i.next();
					if (cfg.isUnlimitedEnrollment()) unlimited = true;
					if (cfg.getLimit() != null) courseLimit += cfg.getLimit();
				}
				if (c.getReservation() != null)
					courseLimit = c.getReservation();
	            if (courseLimit >= 9999) unlimited = true;
				course.setLimit(unlimited ? -1 : courseLimit);
				course.setProjected(c.getProjectedDemand());
				course.setEnrollment(c.getEnrollment());
				course.setLastLike(c.getDemand());
				results.add(course);
				for (InstrOfferingConfig config: c.getInstructionalOffering().getInstrOfferingConfigs()) {
					if (config.getEffectiveInstructionalMethod() != null)
						course.addInstructionalMethod(config.getEffectiveInstructionalMethod().getUniqueId(), config.getEffectiveInstructionalMethod().getLabel());
					else
						course.setHasNoInstructionalMethod(true);
				}
				if (parent != null && limit != null && limit > 0 && results.size() >= limit) break;
			}
			if (results.isEmpty()) {
				throw new SectioningException(MSG.exceptionCourseDoesNotExist(query));
			}
			return results;
		} else {
			Collection<ClassAssignmentInterface.CourseAssignment> results = null;
			try {
				results = server.execute(server.createAction(ListCourseOfferings.class).forQuery(query).withLimit(limit).withMatcher(matcher), currentUser(cx));
			} catch (PageAccessException e) {
				throw e;
			} catch (SectioningException e) {
				throw e;
			} catch (Exception e) {
				sLog.error(e.getMessage(), e);
				throw new SectioningException(MSG.exceptionUnknown(e.getMessage()), e);
			}
			if (results == null || results.isEmpty()) {
				throw new SectioningException(MSG.exceptionCourseDoesNotExist(query));
			}
			return results;
		}
	}
	
	public CourseMatcher getCourseMatcher(StudentSectioningContext cx, OnlineSectioningServer server) {
		boolean noCourseType = true, allCourseTypes = false;
		Set<String> allowedCourseTypes = new HashSet<String>();
		Set<Long> courseIds = null;
		if (getSessionContext().hasPermissionOtherAuthority(cx.getSessionId(), Right.StudentSchedulingCanLookupAllCourses, getStudentAuthority(cx.getSessionId()))) {
			allCourseTypes = true;
			if (cx.getStudentId() != null) {
				if (server != null && !(server instanceof DatabaseServer)) {
					courseIds = server.getRequestedCourseIds(cx.getStudentId());
				} else {
					Student student = StudentDAO.getInstance().get(cx.getStudentId());
					if (student != null) {
						courseIds = new HashSet<Long>();
						for (CourseDemand cd: student.getCourseDemands()) {
							for (CourseRequest cr: cd.getCourseRequests()) {
								courseIds.add(cr.getCourseOffering().getUniqueId());
							}
						}
						for (AdvisorCourseRequest acr: student.getAdvisorCourseRequests()) {
							if (acr.getCourseOffering() != null) courseIds.add(acr.getCourseOffering().getUniqueId());
						}
					}
				}
			}
		} else {
			org.hibernate.Session hibSession = SessionDAO.getInstance().createNewSession();
			try {
				Student student = (cx.getStudentId() == null ? null : StudentDAO.getInstance().get(cx.getStudentId(), hibSession));
				StudentSectioningStatus status = (student == null ? null : student.getEffectiveStatus());
				if (status != null) {
					for (CourseType type: status.getTypes())
						allowedCourseTypes.add(type.getReference());
					noCourseType = !status.hasOption(Option.notype);
				}
				if (student != null) {
					if (server != null && !(server instanceof DatabaseServer)) {
						courseIds = server.getRequestedCourseIds(cx.getStudentId());
					} else {
						courseIds = new HashSet<Long>();
						for (CourseDemand cd: student.getCourseDemands()) {
							for (CourseRequest cr: cd.getCourseRequests()) {
								courseIds.add(cr.getCourseOffering().getUniqueId());
							}
						}
						for (AdvisorCourseRequest acr: student.getAdvisorCourseRequests()) {
							if (acr.getCourseOffering() != null) courseIds.add(acr.getCourseOffering().getUniqueId());
						}
					}
				}
			} finally {
				hibSession.close();
			}
		}
		CourseMatcher matcher = new CourseMatcher(allCourseTypes, noCourseType, allowedCourseTypes, courseIds);
		
		if (cx.getStudentId() != null) {
			CourseMatcherProvider provider = getCourseMatcherProvider();
			if (provider != null) matcher.setParentCourseMatcher(provider.getCourseMatcher(server, getSessionContext(), cx.getStudentId()));
		}
		
		return matcher;
	}
	
	@SuppressWarnings("unchecked")
	public Collection<ClassAssignmentInterface.ClassAssignment> listClasses(StudentSectioningContext cx, String course) throws SectioningException, PageAccessException {
		checkContext(cx);
		if (cx.getSessionId() == null) throw new SectioningException(MSG.exceptionNoAcademicSession());
		if (!cx.isOnline()) {
			OnlineSectioningServer server = getStudentSolver();
			if (server == null) 
				throw new SectioningException(MSG.exceptionNoSolver());
			else
				return server.execute(server.createAction(ListClasses.class).forCourseAndStudent(course, cx.getStudentId()), currentUser(cx));
		}
		OnlineSectioningServer server = getServerInstance(cx.getSessionId(), false);
		Set<Long> allowedClasses = null;
		if (server == null || server instanceof DatabaseServer) {
			if (!sessionContext.hasPermission(Right.HasRole)) {
				Session session = SessionDAO.getInstance().get(cx.getSessionId());
				if (session != null && !session.canNoRoleReportClass())
					throw new SectioningException(MSG.exceptionClassScheduleNotAvaiable());
			}
			ArrayList<ClassAssignmentInterface.ClassAssignment> results = new ArrayList<ClassAssignmentInterface.ClassAssignment>();
			org.hibernate.Session hibSession = CurriculumDAO.getInstance().getSession();
			CourseOffering courseOffering = null;
			for (CourseOffering c: (List<CourseOffering>)hibSession.createQuery(
					"select c from CourseOffering c where " +
					"c.subjectArea.session.uniqueId = :sessionId and c.subjectArea.department.allowStudentScheduling = true and " +
					"(lower(c.subjectArea.subjectAreaAbbreviation || ' ' || c.courseNbr) = :course or lower(c.subjectArea.subjectAreaAbbreviation || ' ' || c.courseNbr || ' - ' || c.title) = :course)")
					.setString("course", course.toLowerCase())
					.setLong("sessionId", cx.getSessionId())
					.setCacheable(true).setMaxResults(1).list()) {
				courseOffering = c; break;
			}
			if (courseOffering == null) throw new SectioningException(MSG.exceptionCourseDoesNotExist(course));
			List<Class_> classes = new ArrayList<Class_>();
			for (Iterator<InstrOfferingConfig> i = courseOffering.getInstructionalOffering().getInstrOfferingConfigs().iterator(); i.hasNext(); ) {
				InstrOfferingConfig config = i.next();
				for (Iterator<SchedulingSubpart> j = config.getSchedulingSubparts().iterator(); j.hasNext(); ) {
					SchedulingSubpart subpart = j.next();
					classes.addAll(subpart.getClasses());
				}
			}
			Collections.sort(classes, new ClassComparator(ClassComparator.COMPARE_BY_HIERARCHY));
			NameFormat nameFormat = NameFormat.fromReference(ApplicationProperty.OnlineSchedulingInstructorNameFormat.value());
			for (Class_ clazz: classes) {
				if (!clazz.isEnabledForStudentScheduling()) {
					if (cx.getStudentId() != null && allowedClasses == null) {
						allowedClasses = new HashSet<Long>();
						for (Reservation reservation: courseOffering.getInstructionalOffering().getReservations()) {
							if (reservation instanceof StudentGroupReservation) {
								StudentGroupType type = ((StudentGroupReservation)reservation).getGroup().getType();
								if (type != null && type.getAllowDisabledSection() == StudentGroupType.AllowDisabledSection.WithGroupReservation) {
									boolean hasStudent = false;
									for (Student student: ((StudentGroupReservation)reservation).getGroup().getStudents()) {
										if (student.getUniqueId().equals(cx.getStudentId())) {
											hasStudent = true; break;
										}
									}
									if (hasStudent) {
										for (Class_ c: classes)
											if (!c.isEnabledForStudentScheduling() && reservation.isMatching(c))
												allowedClasses.add(c.getUniqueId());
									}
								}
							}
						}
						Student student = StudentDAO.getInstance().get(cx.getStudentId(), hibSession);
						if (student != null) {
							for (StudentGroup group: student.getGroups()) {
								StudentGroupType type = group.getType();
								if (type != null && type.getAllowDisabledSection() == StudentGroupType.AllowDisabledSection.AlwaysAllowed) {
									for (Class_ c: classes)
										if (!c.isEnabledForStudentScheduling())
											allowedClasses.add(c.getUniqueId());
									break;
								}
							}
						}
					}
					if (allowedClasses == null || !allowedClasses.contains(clazz.getUniqueId())) continue;
				}
				ClassAssignmentInterface.ClassAssignment a = new ClassAssignmentInterface.ClassAssignment();
				a.setClassId(clazz.getUniqueId());
				a.setSubpart(clazz.getSchedulingSubpart().getItypeDesc().trim());
				if (clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalMethod() != null)
					a.setSubpart(clazz.getSchedulingSubpart().getItypeDesc().trim() + " (" + clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalMethod().getLabel() + ")");
				a.setSection(clazz.getClassSuffix(courseOffering));
				if (a.getSection() == null)
	    			a.setSection(clazz.getSectionNumberString(hibSession));
				a.setExternalId(clazz.getExternalId(courseOffering));
				if (a.getExternalId() == null)
					a.setExternalId(clazz.getSchedulingSubpart().getItypeDesc().trim() + " " + clazz.getSectionNumberString());
				a.setClassNumber(clazz.getSectionNumberString(hibSession));
				a.addNote(clazz.getSchedulePrintNote());

				Assignment ass = clazz.getCommittedAssignment();
				Placement p = (ass == null ? null : ass.getPlacement());
				
                int minLimit = clazz.getExpectedCapacity();
            	int maxLimit = clazz.getMaxExpectedCapacity();
            	int limit = maxLimit;
            	if (minLimit < maxLimit && p != null) {
            		// int roomLimit = Math.round((clazz.getRoomRatio() == null ? 1.0f : clazz.getRoomRatio()) * p.getRoomSize());
            		int roomLimit = (int) Math.floor(p.getRoomSize() / (clazz.getRoomRatio() == null ? 1.0f : clazz.getRoomRatio()));
            		limit = Math.min(Math.max(minLimit, roomLimit), maxLimit);
            	}
                if (clazz.getSchedulingSubpart().getInstrOfferingConfig().isUnlimitedEnrollment() || limit >= 9999) limit = -1;
                a.setCancelled(clazz.isCancelled());
				a.setLimit(new int[] {clazz.getEnrollment() == 0 ? -1 : clazz.getEnrollment(), limit});
				
				if (p != null && p.getTimeLocation() != null) {
					for (DayCode d: DayCode.toDayCodes(p.getTimeLocation().getDayCode()))
						a.addDay(d.getIndex());
					a.setStart(p.getTimeLocation().getStartSlot());
					a.setLength(p.getTimeLocation().getLength());
					a.setBreakTime(p.getTimeLocation().getBreakTime());
					a.setDatePattern(p.getTimeLocation().getDatePatternName());
				}
				if (ass != null)
					for (Location loc: ass.getRooms())
						a.addRoom(loc.getUniqueId(), loc.getLabelWithDisplayName());
				/*
				if (p != null && p.getRoomLocations() != null) {
					for (RoomLocation rm: p.getRoomLocations()) {
						a.addRoom(rm.getId(), rm.getName());
					}
				}
				if (p != null && p.getRoomLocation() != null) {
					a.addRoom(p.getRoomLocation().getId(), p.getRoomLocation().getName());
				}
				*/
				if (!clazz.getClassInstructors().isEmpty()) {
					for (Iterator<ClassInstructor> i = clazz.getClassInstructors().iterator(); i.hasNext(); ) {
						ClassInstructor instr = i.next();
						a.addInstructor(nameFormat.format(instr.getInstructor()));
						a.addInstructoEmail(instr.getInstructor().getEmail());
					}
				}
				if (clazz.getParentClass() != null)
					a.setParentSection(clazz.getParentClass().getClassSuffix(courseOffering));
				a.setSubpartId(clazz.getSchedulingSubpart().getUniqueId());
				if (a.getParentSection() == null)
					a.setParentSection(courseOffering.getConsentType() == null ? null : courseOffering.getConsentType().getLabel());
				results.add(a);
			}
			return results;
		} else {
			try {
				return server.execute(server.createAction(ListClasses.class).forCourseAndStudent(course, cx.getStudentId()), currentUser(cx));
			} catch (PageAccessException e) {
				throw e;
			} catch (SectioningException e) {
				throw e;
			} catch (Exception e) {
				sLog.error(e.getMessage(), e);
				throw new SectioningException(MSG.exceptionUnknown(e.getMessage()), e);
			}
		}
	}

	public Collection<AcademicSessionProvider.AcademicSessionInfo> listAcademicSessions(boolean sectioning) throws SectioningException, PageAccessException {
		ArrayList<AcademicSessionProvider.AcademicSessionInfo> ret = new ArrayList<AcademicSessionProvider.AcademicSessionInfo>();
		ExternalTermProvider extTerm = getExternalTermProvider();
		UniTimePrincipal principal = (UniTimePrincipal)getSessionContext().getAttribute(SessionAttribute.OnlineSchedulingUser);
		if (sectioning) {
			for (String s: solverServerService.getOnlineStudentSchedulingContainer().getSolvers()) {
				OnlineSectioningServer server = solverServerService.getOnlineStudentSchedulingContainer().getSolver(s);
				if (server == null || !server.isReady()) continue;
				Session session = SessionDAO.getInstance().get(Long.valueOf(s));
				AcademicSessionInfo info = server.getAcademicSession();
				if (principal != null) {
					Long studentId = principal.getStudentId(session.getUniqueId());
					if (studentId == null) continue;
					Student student = StudentDAO.getInstance().get(studentId);
					if (student == null) continue;
					StudentSectioningStatus status = student.getEffectiveStatus();
					if (status != null && !status.hasOption(StudentSectioningStatus.Option.enabled)
						&& (!getSessionContext().hasPermissionAnySession(session, Right.StudentSchedulingAdmin) || !status.hasOption(StudentSectioningStatus.Option.admin))
						&& (!getSessionContext().hasPermissionAnySession(session, Right.StudentSchedulingAdvisor) || !status.hasOption(StudentSectioningStatus.Option.advisor))
						) continue;
				} else {
					if (!getSessionContext().hasPermissionOtherAuthority(session, Right.SchedulingAssistant, getStudentAuthority(session))) continue;
				}
				ret.add(new AcademicSessionProvider.AcademicSessionInfo(
						session.getUniqueId(),
						session.getAcademicYear(), session.getAcademicTerm(), session.getAcademicInitiative(),
						MSG.sessionName(session.getAcademicYear(), session.getAcademicTerm(), session.getAcademicInitiative()),
						session.getSessionBeginDateTime())
						.setExternalCampus(extTerm == null ? null : extTerm.getExternalCampus(info))
						.setExternalTerm(extTerm == null ? null : extTerm.getExternalTerm(info)));
			}
		} else {
			for (Session session: SessionDAO.getInstance().findAll()) {
				if (session.getStatusType().isTestSession()) continue;
				if (session.getStatusType().canPreRegisterStudents()) {
					AcademicSessionInfo info = new AcademicSessionInfo(session);
					if (principal != null) {
						Long studentId = principal.getStudentId(session.getUniqueId());
						if (studentId == null) continue;
						Student student = StudentDAO.getInstance().get(studentId);
						if (student == null) continue;
						StudentSectioningStatus status = student.getEffectiveStatus();
						if (status != null && !status.hasOption(StudentSectioningStatus.Option.regenabled)
							&& (!getSessionContext().hasPermissionAnySession(session, Right.StudentSchedulingAdmin) || !status.hasOption(StudentSectioningStatus.Option.regadmin))
							&& (!getSessionContext().hasPermissionAnySession(session, Right.StudentSchedulingAdvisor) || !status.hasOption(StudentSectioningStatus.Option.regadvisor))
							) continue;
					} else {
						if (!getSessionContext().hasPermissionOtherAuthority(session, Right.CourseRequests, getStudentAuthority(session))) continue;
					}
					ret.add(new AcademicSessionProvider.AcademicSessionInfo(
							session.getUniqueId(),
							session.getAcademicYear(), session.getAcademicTerm(), session.getAcademicInitiative(),
							MSG.sessionName(session.getAcademicYear(), session.getAcademicTerm(), session.getAcademicInitiative()),
							session.getSessionBeginDateTime()
							)
							.setExternalCampus(extTerm == null ? null : extTerm.getExternalCampus(info))
							.setExternalTerm(extTerm == null ? null : extTerm.getExternalTerm(info))
							);
				}
			}
		}
		if (ret.isEmpty()) {
			throw new SectioningException(MSG.exceptionNoSuitableAcademicSessions());
		}
		Collections.sort(ret);
		if (!sectioning) Collections.reverse(ret);
		return ret;
	}
	
	public String retrieveCourseDetails(StudentSectioningContext cx, String course) throws SectioningException, PageAccessException {
		checkContext(cx);
		OnlineSectioningServer server = getServerInstance(cx.getSessionId(), false); 
		if (server == null) {
			CourseOffering courseOffering = lookupCourse(CourseOfferingDAO.getInstance().getSession(), cx.getSessionId(), null, course, null);
			if (courseOffering == null) throw new SectioningException(MSG.exceptionCourseDoesNotExist(course));
			return getCourseDetailsProvider().getDetails(
					new AcademicSessionInfo(courseOffering.getSubjectArea().getSession()),
					courseOffering.getSubjectAreaAbbv(), courseOffering.getCourseNbr());
		} else {
			XCourseId c = server.getCourse(course);
			if (c == null) {
				if (course.indexOf(' ') >= 0) {
					return getCourseDetailsProvider().getDetails(
							new AcademicSessionInfo(SessionDAO.getInstance().get(cx.getSessionId())),
							course.substring(0, course.indexOf(' ')), course.substring(course.indexOf(' ') + 1));
				}
				throw new SectioningException(MSG.exceptionCourseDoesNotExist(course));
			}
			return server.getCourseDetails(c.getCourseId(), getCourseDetailsProvider());
		}
	}
	
	public Long retrieveCourseOfferingId(Long sessionId, String course) throws SectioningException, PageAccessException {
		OnlineSectioningServer server = getServerInstance(sessionId, false); 
		if (server == null) {
			CourseOffering courseOffering = lookupCourse(CourseOfferingDAO.getInstance().getSession(), sessionId, null, course, null);
			if (courseOffering == null) throw new SectioningException(MSG.exceptionCourseDoesNotExist(course));
			return courseOffering.getUniqueId();
		} else {
			XCourseId c = server.getCourse(course);
			if (c == null) throw new SectioningException(MSG.exceptionCourseDoesNotExist(course));
			return c.getCourseId();
		}
	}

	public ClassAssignmentInterface section(CourseRequestInterface request, ArrayList<ClassAssignmentInterface.ClassAssignment> currentAssignment) throws SectioningException, PageAccessException {
		try {
			checkContext(request);
			if (!request.isOnline()) {
				OnlineSectioningServer server = getStudentSolver();
				if (server == null) 
					throw new SectioningException(MSG.exceptionNoSolver());
				ClassAssignmentInterface ret = server.execute(server.createAction(FindAssignmentAction.class).forRequest(request).withAssignment(currentAssignment), currentUser(request)).get(0);
				if (ret != null)
					ret.setCanEnroll(request.getStudentId() != null);
				return ret;
			}
			
			OnlineSectioningServer server = getServerInstance(request.getAcademicSessionId(), true);
			if (server == null) throw new SectioningException(MSG.exceptionNoServerForSession());
			ClassAssignmentInterface ret = server.execute(server.createAction(FindAssignmentAction.class).forRequest(request).withAssignment(currentAssignment), currentUser(request)).get(0);
			if (ret != null) {
				ret.setCanEnroll(server.getAcademicSession().isSectioningEnabled());
				if (ret.isCanEnroll() && request.getStudentId() == null)
					ret.setCanEnroll(false);
			}
			EligibilityCheck last = getLastEligibilityCheck(request);
			if (ret != null && last != null && last.hasCreditHours())
				for (CourseAssignment ca: ret.getCourseAssignments())
					for (ClassAssignment a: ca.getClassAssignments()) {
						Float credit = last.getCreditHour(a);
						a.setCreditHour(credit);
						if (credit != null) a.setCredit(FixedCreditUnitConfig.formatCredit(credit));
					}
			return ret;
		} catch (PageAccessException e) {
			throw e;
		} catch (SectioningException e) {
			throw e;
		} catch (Exception e) {
			sLog.error(e.getMessage(), e);
			throw new SectioningException(MSG.exceptionSectioningFailed(e.getMessage()), e);
		}
	}
	
	public CheckCoursesResponse checkCourses(CourseRequestInterface request) throws SectioningException, PageAccessException {
		try {
			checkContext(request);
			if (request.getAcademicSessionId() == null) throw new SectioningException(MSG.exceptionNoAcademicSession());
			
			if (!request.isOnline()) {
				OnlineSectioningServer server = getStudentSolver();
				if (server == null) 
					throw new SectioningException(MSG.exceptionNoSolver());
				return server.execute(server.createAction(CheckCourses.class).forRequest(request), currentUser(request));
			}
			
			if (!request.isSectioning()) {
				if (request.getStudentId() == null) throw new PageAccessException(MSG.exceptionNoStudent());
				getSessionContext().checkPermissionAnyAuthority(request.getStudentId(), "Student", Right.StudentSchedulingCanRegister);
				EligibilityCheck last = getLastEligibilityCheck(request);
				if (last != null && !last.hasFlag(EligibilityFlag.CAN_REGISTER))
					throw new SectioningException(last.hasMessage() ? last.getMessage() : MSG.exceptionInsufficientPrivileges());
			}
			
			OnlineSectioningServer server = getServerInstance(request.getAcademicSessionId(), false);
			if (server == null) {
				if (!request.isSectioning() && CustomCourseRequestsValidationHolder.hasProvider()) {
					OnlineSectioningServer dummy = getServerInstance(request.getAcademicSessionId(), true);
					return dummy.execute(dummy.createAction(CheckCourses.class).forRequest(request).withMatcher(getCourseMatcher(request, server)).withCustomValidation(true), currentUser(request));
				}
				org.hibernate.Session hibSession = CurriculumDAO.getInstance().getSession();
				CheckCoursesResponse response = new CheckCoursesResponse();
				CourseMatcher matcher = getCourseMatcher(request, server);
				for (CourseRequestInterface.Request cr: request.getCourses()) {
					if (cr.hasRequestedCourse()) {
						for (RequestedCourse rc: cr.getRequestedCourse())
							if (rc.isCourse() && lookupCourse(hibSession, request.getAcademicSessionId(), request.getStudentId(), rc, matcher) == null) {
								response.addError(rc.getCourseId(), rc.getCourseName(), "NOT_FOUND", MSG.validationCourseNotExists(rc.getCourseName()));
								response.setErrorMessage(MSG.validationCourseNotExists(rc.getCourseName()));
							}
					}
				}
				for (CourseRequestInterface.Request cr: request.getAlternatives()) {
					if (cr.hasRequestedCourse()) {
						for (RequestedCourse rc: cr.getRequestedCourse())
							if (rc.isCourse() && lookupCourse(hibSession, request.getAcademicSessionId(), request.getStudentId(), rc, matcher) == null) {
								response.addError(rc.getCourseId(), rc.getCourseName(), "NOT_FOUND", MSG.validationCourseNotExists(rc.getCourseName()));
								response.setErrorMessage(MSG.validationCourseNotExists(rc.getCourseName()));
							}
					}
				}
				return response;
			} else {
				return server.execute(server.createAction(CheckCourses.class).forRequest(request).withMatcher(getCourseMatcher(request, server)).withCustomValidation(!request.isSectioning()), currentUser(request));
			}
		} catch (PageAccessException e) {
			throw e;
		} catch (SectioningException e) {
			throw e;
		} catch (Exception e) {
			sLog.error(e.getMessage(), e);
			throw new SectioningException(MSG.exceptionSectioningFailed(e.getMessage()), e);
		}
	}
	
	public static CourseOffering lookupCourse(org.hibernate.Session hibSession, Long sessionId, Long studentId, String courseName, CourseMatcher courseMatcher) {
		boolean excludeNotOffered = ApplicationProperty.CourseRequestsShowNotOffered.isFalse();
		if (studentId != null) {
			for (CourseOffering co: (List<CourseOffering>)hibSession.createQuery(
					"select cr.courseOffering from CourseRequest cr where " +
					"cr.courseDemand.student.uniqueId = :studentId and " +
					(excludeNotOffered ? "cr.courseOffering.instructionalOffering.notOffered is false and " : "") +
					"(lower(cr.courseOffering.subjectArea.subjectAreaAbbreviation || ' ' || cr.courseOffering.courseNbr) = :course or " +
					"lower(cr.courseOffering.subjectArea.subjectAreaAbbreviation || ' ' || cr.courseOffering.courseNbr || ' - ' || cr.courseOffering.title) = :course)")
					.setString("course", courseName.toLowerCase())
					.setLong("studentId", studentId)
					.setCacheable(true).setMaxResults(1).list()) {
				return co;
			}
		}
		for (CourseOffering co: (List<CourseOffering>)hibSession.createQuery(
				"select c from CourseOffering c where " +
				(excludeNotOffered ? "c.instructionalOffering.notOffered is false and " : "") + 
				"c.subjectArea.session.uniqueId = :sessionId and c.subjectArea.department.allowStudentScheduling = true and " +
				"(lower(c.subjectArea.subjectAreaAbbreviation || ' ' || c.courseNbr) = :course or lower(c.subjectArea.subjectAreaAbbreviation || ' ' || c.courseNbr || ' - ' || c.title) = :course)")
				.setString("course", courseName.toLowerCase())
				.setLong("sessionId", sessionId)
				.setCacheable(true).setMaxResults(1).list()) {
			if (courseMatcher != null && !courseMatcher.match(new XCourse(co))) continue;
			return co;
		}
		return null;
	}
	
	public static CourseOffering lookupCourse(org.hibernate.Session hibSession, Long sessionId, Long studentId, RequestedCourse rc, CourseMatcher courseMatcher) {
		if (rc.hasCourseId()) {
			CourseOffering co = CourseOfferingDAO.getInstance().get(rc.getCourseId(), hibSession);
			if (courseMatcher != null && !courseMatcher.match(new XCourse(co))) return null;
			return co;
		}
		if (rc.hasCourseName())
			return lookupCourse(hibSession, sessionId, studentId, rc.getCourseName(), courseMatcher);
		return null;
	}
	
	public 	Collection<ClassAssignmentInterface> computeSuggestions(CourseRequestInterface request, Collection<ClassAssignmentInterface.ClassAssignment> currentAssignment, int selectedAssignmentIndex, String filter) throws SectioningException, PageAccessException {
		try {
			checkContext(request);
			if (!request.isOnline()) {
				OnlineSectioningServer server = getStudentSolver();
				if (server == null) 
					throw new SectioningException(MSG.exceptionNoSolver());
				
				ClassAssignmentInterface.ClassAssignment selectedAssignment = null;
				if (selectedAssignmentIndex >= 0) {
					selectedAssignment = ((List<ClassAssignmentInterface.ClassAssignment>)currentAssignment).get(selectedAssignmentIndex);
				} else if (request.getLastCourse() != null) {
					XCourseId course = server.getCourse(request.getLastCourse().getCourseId(), request.getLastCourse().getCourseName());
					if (course == null) throw new SectioningException(MSG.exceptionCourseDoesNotExist(request.getLastCourse().getCourseName()));
					selectedAssignment = new ClassAssignmentInterface.ClassAssignment();
					selectedAssignment.setCourseId(course.getCourseId());
				}
				
				Collection<ClassAssignmentInterface> ret = server.execute(server.createAction(ComputeSuggestionsAction.class).forRequest(request).withAssignment(currentAssignment).withSelection(selectedAssignment).withFilter(filter), currentUser(request));
				if (ret != null) {
					for (ClassAssignmentInterface ca: ret)
						ca.setCanEnroll(request.getStudentId() != null);
				}
				return ret;
			}
			
			OnlineSectioningServer server = getServerInstance(request.getAcademicSessionId(), true);
			if (server == null) throw new SectioningException(MSG.exceptionNoServerForSession());
			ClassAssignmentInterface.ClassAssignment selectedAssignment = null;
			if (selectedAssignmentIndex >= 0) {
				selectedAssignment = ((List<ClassAssignmentInterface.ClassAssignment>)currentAssignment).get(selectedAssignmentIndex);
			} else if (request.getLastCourse() != null) {
				XCourseId course = server.getCourse(request.getLastCourse().getCourseId(), request.getLastCourse().getCourseName());
				if (course == null) throw new SectioningException(MSG.exceptionCourseDoesNotExist(request.getLastCourse().getCourseName()));
				selectedAssignment = new ClassAssignmentInterface.ClassAssignment();
				selectedAssignment.setCourseId(course.getCourseId());
			}
			Collection<ClassAssignmentInterface> ret = server.execute(server.createAction(ComputeSuggestionsAction.class).forRequest(request).withAssignment(currentAssignment).withSelection(selectedAssignment).withFilter(filter), currentUser(request));
			if (ret != null) {
				boolean canEnroll = server.getAcademicSession().isSectioningEnabled() && request.getStudentId() != null;
				for (ClassAssignmentInterface ca: ret)
					ca.setCanEnroll(canEnroll);
			}
			EligibilityCheck last = getLastEligibilityCheck(request);
			if (ret != null && last != null && last.hasCreditHours())
				for (ClassAssignmentInterface suggestion: ret)
					for (CourseAssignment ca: suggestion.getCourseAssignments())
						for (ClassAssignment a: ca.getClassAssignments()) {
							Float credit = last.getCreditHour(a);
							a.setCreditHour(credit);
							if (credit != null) a.setCredit(FixedCreditUnitConfig.formatCredit(credit));
						}
			return ret;
		} catch (PageAccessException e) {
			throw e;
		} catch (SectioningException e) {
			throw e;
		} catch (Exception e) {
			sLog.error(e.getMessage(), e);
			throw new SectioningException(MSG.exceptionSectioningFailed(e.getMessage()), e);
		}
	}
	
	public String logIn(String userName, String password, String pin) throws SectioningException, PageAccessException {
		if ("LOOKUP".equals(userName)) {
			getSessionContext().checkPermissionAnySession(Right.StudentSchedulingAdvisor);
			org.hibernate.Session hibSession = StudentDAO.getInstance().createNewSession();
			try {
				List<Student> student = hibSession.createQuery("select m from Student m where m.externalUniqueId = :uid").setString("uid", password).list();
				if (!student.isEmpty()) {
					UserContext user = getSessionContext().getUser();
					UniTimePrincipal principal = new UniTimePrincipal(user.getTrueExternalUserId(), password, user.getTrueName());
					for (Student s: student) {
						if (getSessionContext().hasPermissionAnySession(s.getSession(), Right.StudentSchedulingAdvisor)) {
							principal.addStudentId(s.getSession().getUniqueId(), s.getUniqueId());
							principal.setName(NameFormat.defaultFormat().format(s));
						}
					}
					getSessionContext().setAttribute(SessionAttribute.OnlineSchedulingUser, principal);
					return principal.getName();
				}
			} finally {
				hibSession.close();
			}			
		}
		if ("BATCH".equals(userName)) {
			getSessionContext().checkPermission(Right.StudentSectioningSolverDashboard);
			OnlineSectioningServer server = getStudentSolver();
			if (server == null) 
				throw new SectioningException(MSG.exceptionNoSolver());
			org.hibernate.Session hibSession = StudentDAO.getInstance().createNewSession();
			try {
				XStudent student = server.getStudent(Long.valueOf(password));
				if (student == null)
					throw new SectioningException(MSG.exceptionLoginFailed());
				UserContext user = getSessionContext().getUser();
				UniTimePrincipal principal = new UniTimePrincipal(user.getTrueExternalUserId(), student.getExternalId(), user.getTrueName());
				principal.addStudentId(server.getAcademicSession().getUniqueId(), student.getStudentId());
				principal.setName(student.getName());
				getSessionContext().setAttribute(SessionAttribute.OnlineSchedulingUser, principal);
				return principal.getName();
			} finally {
				hibSession.close();
			}		
		}
		try {
    		Authentication authRequest = new UsernamePasswordAuthenticationToken(userName, password);
    		Authentication authResult = getAuthenticationManager().authenticate(authRequest);
    		SecurityContextHolder.getContext().setAuthentication(authResult);
    		UserContext user = (UserContext)authResult.getPrincipal();
    		if (user.getCurrentAuthority() == null)
    			for (UserAuthority auth: user.getAuthorities(Roles.ROLE_STUDENT)) {
    				if (getLastSessionId() == null || auth.getAcademicSession().getQualifierId().equals(getLastSessionId())) {
    					user.setCurrentAuthority(auth); break;
    				}
    			}
    		LoginManager.loginSuceeded(authResult.getName());
    		return (user.getName() == null ? user.getUsername() : user.getName());
    	} catch (Exception e) {
    		LoginManager.addFailedLoginAttempt(userName, new Date());
    		throw new PageAccessException(e.getMessage(), e);
    	}
	}
	
	public Boolean logOut() throws SectioningException, PageAccessException {
		getSessionContext().removeAttribute(SessionAttribute.OnlineSchedulingUser);
		getSessionContext().removeAttribute(SessionAttribute.OnlineSchedulingEligibility);
		if (getSessionContext().hasPermission(Right.StudentSchedulingAdvisor)) 
			return false;
		SecurityContextHolder.getContext().setAuthentication(null);
		return true;
	}
	
	public String whoAmI() throws SectioningException, PageAccessException {
		UniTimePrincipal principal = (UniTimePrincipal)getSessionContext().getAttribute(SessionAttribute.OnlineSchedulingUser);
		if (principal != null) return principal.getName();
		UserContext user = getSessionContext().getUser();
		if (user == null || user instanceof AnonymousUserContext) return null;
		return (user.getName() == null ? user.getUsername() : user.getName());
	}
	
	public Long getStudentId(Long sessionId) {
		if (sessionId == null) return null;
		UniTimePrincipal principal = (UniTimePrincipal)getSessionContext().getAttribute(SessionAttribute.OnlineSchedulingUser);
		if (principal != null)
			return principal.getStudentId(sessionId);
		UserContext user = getSessionContext().getUser();
		if (user == null) return null;
		for (UserAuthority a: user.getAuthorities(Roles.ROLE_STUDENT, new SimpleQualifier("Session", sessionId)))
			return a.getUniqueId();
		return null;
	}
	
	public Long getLastSessionId() {
		Long lastSessionId = (Long)getSessionContext().getAttribute(SessionAttribute.OnlineSchedulingLastSession);
		if (lastSessionId == null) {
			UserContext user = getSessionContext().getUser();
			if (user != null) {
				Long sessionId = user.getCurrentAcademicSessionId();
				if (sessionId != null)
					lastSessionId = sessionId;
			}
		}
		return lastSessionId;
	}

	public void setLastSessionId(Long sessionId) {
		getSessionContext().setAttribute(SessionAttribute.OnlineSchedulingLastSession, sessionId);
	}
	
	public AcademicSessionProvider.AcademicSessionInfo lastAcademicSession(boolean sectioning) throws SectioningException, PageAccessException {
		if (getSessionContext().isHttpSessionNew()) throw new PageAccessException(MSG.exceptionUserNotLoggedIn());
		Long sessionId = (Long)getSessionContext().getAttribute(SessionAttribute.OnlineSchedulingLastSession);
		if (sessionId == null) throw new SectioningException(MSG.exceptionNoAcademicSession());
		ExternalTermProvider extTerm = getExternalTermProvider();
		if (sectioning) {
			OnlineSectioningServer server = getServerInstance(sessionId, false);
			if (server == null) throw new SectioningException(MSG.exceptionNoServerForSession());
			AcademicSessionInfo s = server.getAcademicSession();
			if (s == null) throw new SectioningException(MSG.exceptionNoServerForSession());
			if (getSessionContext().getAttribute(SessionAttribute.OnlineSchedulingUser) == null)
				getSessionContext().checkPermissionOtherAuthority(s, Right.SchedulingAssistant, getStudentAuthority(s));
			return new AcademicSessionProvider.AcademicSessionInfo(
					s.getUniqueId(),
					s.getYear(), s.getTerm(), s.getCampus(),
					MSG.sessionName(s.getYear(), s.getTerm(), s.getCampus()),
					s.getSessionBeginDate())
					.setExternalCampus(extTerm == null ? null : extTerm.getExternalCampus(s))
					.setExternalTerm(extTerm == null ? null : extTerm.getExternalTerm(s));
		} else {
			Session session = SessionDAO.getInstance().get(sessionId);
			if (session == null || session.getStatusType().isTestSession())
				throw new SectioningException(MSG.exceptionNoSuitableAcademicSessions());
			if (!session.getStatusType().canPreRegisterStudents() || session.getStatusType().canSectionAssistStudents() || session.getStatusType().canOnlineSectionStudents())
				throw new SectioningException(MSG.exceptionNoServerForSession());
			AcademicSessionInfo info = new AcademicSessionInfo(session);
			if (getSessionContext().getAttribute(SessionAttribute.OnlineSchedulingUser) == null)
				getSessionContext().checkPermissionOtherAuthority(session, Right.CourseRequests, getStudentAuthority(session));
			return new AcademicSessionProvider.AcademicSessionInfo(
					session.getUniqueId(),
					session.getAcademicYear(), session.getAcademicTerm(), session.getAcademicInitiative(),
					MSG.sessionName(session.getAcademicYear(), session.getAcademicTerm(), session.getAcademicInitiative()),
					session.getSessionBeginDateTime())
					.setExternalCampus(extTerm == null ? null : extTerm.getExternalCampus(info))
					.setExternalTerm(extTerm == null ? null : extTerm.getExternalTerm(info));
		}
	}
	
	public CourseRequestInterface saveRequest(CourseRequestInterface request) throws SectioningException, PageAccessException {
		checkContext(request);
		if (request.getStudentId() == null) throw new PageAccessException(MSG.exceptionNoStudent());
		getSessionContext().checkPermissionAnyAuthority(request.getStudentId(), "Student", Right.StudentSchedulingCanRegister);
		EligibilityCheck last = getLastEligibilityCheck(request);
		if (last != null && !last.hasFlag(EligibilityFlag.CAN_REGISTER))
			throw new SectioningException(last.hasMessage() ? last.getMessage() : MSG.exceptionInsufficientPrivileges());
		
		OnlineSectioningServer server = getServerInstance(request.getAcademicSessionId(), true);
		Long studentId = request.getStudentId();
		if (studentId == null)
			throw new PageAccessException(MSG.exceptionEnrollNotStudent(SessionDAO.getInstance().get(request.getAcademicSessionId()).getLabel()));
		if (!studentId.equals(getStudentId(request.getAcademicSessionId())))
			getSessionContext().hasPermissionAnySession(request.getAcademicSessionId(), Right.StudentSchedulingAdvisor);
		if (server != null) {
			return server.execute(server.createAction(SaveStudentRequests.class).forStudent(studentId).withRequest(request).withCustomValidation(true), currentUser(request));
		} else {
			org.hibernate.Session hibSession = StudentDAO.getInstance().getSession();
			try {
				Student student = StudentDAO.getInstance().get(studentId, hibSession);
				if (student == null) throw new SectioningException(MSG.exceptionBadStudentId());
				OnlineSectioningHelper helper = new OnlineSectioningHelper(hibSession, currentUser(request));
				CriticalCourses critical = null;
				try {
					if (CustomCriticalCoursesHolder.hasProvider())
						critical = CustomCriticalCoursesHolder.getProvider().getCriticalCourses(getServerInstance(request.getAcademicSessionId(), true), helper, new XStudentId(student, helper));
				} catch (Exception e) {
					helper.warn("Failed to lookup critical courses: " + e.getMessage(), e);
				}
				SaveStudentRequests.saveRequest(null, helper, student, request, true, critical);
				hibSession.save(student);
				hibSession.flush();
				return request;
			} catch (PageAccessException e) {
				throw e;
			} catch (SectioningException e) {
				throw e;
			} catch (Exception e) {
				sLog.error(e.getMessage(), e);
				throw new SectioningException(MSG.exceptionUnknown(e.getMessage()), e);
			} finally {
				hibSession.close();
			}
		}
	}
	
	public ClassAssignmentInterface enroll(CourseRequestInterface request, ArrayList<ClassAssignmentInterface.ClassAssignment> currentAssignment) throws SectioningException, PageAccessException {
		checkContext(request);
		if (request.getStudentId() == null) throw new PageAccessException(MSG.exceptionNoStudent());
		
		Long sessionId = canEnroll(request);
		if (!request.getAcademicSessionId().equals(sessionId))
			throw new SectioningException(MSG.exceptionBadSession());
		
		EligibilityCheck last = getLastEligibilityCheck(request);

		if (!request.isOnline()) {
			OnlineSectioningServer server = getStudentSolver();
			if (server == null) 
				throw new SectioningException(MSG.exceptionNoSolver());

			return server.execute(server.createAction(BatchEnrollStudent.class).forStudent(request.getStudentId())
					.withRequest(request).withAssignment(currentAssignment)
					.withWaitListCheck(last != null && last.hasFlag(EligibilityFlag.WAIT_LIST_VALIDATION)), currentUser(request));
		}
		
		OnlineSectioningServer server = getServerInstance(request.getAcademicSessionId(), false);
		if (server == null) throw new SectioningException(MSG.exceptionBadStudentId());
		if (!server.getAcademicSession().isSectioningEnabled())
			throw new SectioningException(MSG.exceptionNotSupportedFeature());
		
		ClassAssignmentInterface ret = server.execute(server.createAction(EnrollStudent.class).forStudent(request.getStudentId())
				.withRequest(request).withAssignment(currentAssignment)
				.withWaitListCheck(last != null && last.hasFlag(EligibilityFlag.WAIT_LIST_VALIDATION)), currentUser(request));
		
		if (ret != null && last != null) {
			for (CourseAssignment ca: ret.getCourseAssignments())
				for (ClassAssignment a: ca.getClassAssignments()) {
					if (a.getGradeMode() != null)
						last.addGradeMode(a.getExternalId(), a.getGradeMode().getCode(), a.getGradeMode().getLabel(), a.getGradeMode().isHonor());
					if (a.getCreditHour() != null)
						last.addCreditHour(a.getExternalId(), a.getCreditHour());
				}
			last.setCurrentCredit(ret.getCurrentCredit());
		}
		
		return ret;
	}

	public List<Long> canApprove(Long classOrOfferingId) throws SectioningException, PageAccessException {
		try {
			UserContext user = getSessionContext().getUser();
			if (user == null) throw new PageAccessException(
					getSessionContext().isHttpSessionNew() ? MSG.exceptionHttpSessionExpired() : MSG.exceptionLoginRequired());
			
			org.hibernate.Session hibSession = SessionDAO.getInstance().getSession();
			
			InstructionalOffering offering = (classOrOfferingId >= 0 ? InstructionalOfferingDAO.getInstance().get(classOrOfferingId, hibSession) : null);
			if (offering == null) {
				Class_ clazz = (classOrOfferingId < 0 ? Class_DAO.getInstance().get(-classOrOfferingId, hibSession) : null);
				if (clazz == null)
					throw new SectioningException(MSG.exceptionBadClassOrOffering());
				offering = clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering();
			}
			
			OnlineSectioningServer server = getServerInstance(offering.getControllingCourseOffering().getSubjectArea().getSessionId(), false);
			
			if (server == null) return null; //?? !server.getAcademicSession().isSectioningEnabled()
			
			List<Long> coursesToApprove = new ArrayList<Long>();
			for (CourseOffering course: offering.getCourseOfferings()) {
				if (getSessionContext().hasPermissionAnyAuthority(course, Right.ConsentApproval))
					coursesToApprove.add(course.getUniqueId());
			}
			return coursesToApprove;
		} catch (PageAccessException e) {
			throw e;
		} catch (SectioningException e) {
			throw e;
		} catch  (Exception e) {
			sLog.error(e.getMessage(), e);
			throw new SectioningException(MSG.exceptionUnknown(e.getMessage()), e);
		}
	}
	
	public List<ClassAssignmentInterface.Enrollment> listEnrollments(Long classOrOfferingId) throws SectioningException, PageAccessException {
		try {
			UserContext user = getSessionContext().getUser();
			if (user == null) throw new PageAccessException(
					getSessionContext().isHttpSessionNew() ? MSG.exceptionHttpSessionExpired() : MSG.exceptionLoginRequired());
			
			org.hibernate.Session hibSession = SessionDAO.getInstance().getSession();
			try {
				InstructionalOffering offering = (classOrOfferingId >= 0 ? InstructionalOfferingDAO.getInstance().get(classOrOfferingId, hibSession) : null);
				Class_ clazz = (classOrOfferingId < 0 ? Class_DAO.getInstance().get(-classOrOfferingId, hibSession) : null);
				if (offering == null && clazz == null) 
					throw new SectioningException(MSG.exceptionBadClassOrOffering());
				if (offering == null)
					offering = clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering();
				Long offeringId = offering.getUniqueId();
				
				getSessionContext().checkPermission(offering, Right.OfferingEnrollments);

				OnlineSectioningServer server = getServerInstance(offering.getControllingCourseOffering().getSubjectArea().getSessionId(), false);
				
				if (server == null || !offering.isAllowStudentScheduling() || offering.isNotOffered() || offering.getInstrOfferingConfigs().isEmpty()) {
					NameFormat nameFormat = NameFormat.fromReference(ApplicationProperty.OnlineSchedulingStudentNameFormat.value());
					NameFormat instructorNameFormat = NameFormat.fromReference(ApplicationProperty.OnlineSchedulingInstructorNameFormat.value());
					Map<String, String> approvedBy2name = new Hashtable<String, String>();
					Hashtable<Long, ClassAssignmentInterface.Enrollment> student2enrollment = new Hashtable<Long, ClassAssignmentInterface.Enrollment>();
					boolean canShowExtIds = sessionContext.hasPermission(Right.EnrollmentsShowExternalId);
					boolean canRegister = sessionContext.hasPermission(Right.CourseRequests);
					boolean canUseAssistant = sessionContext.hasPermission(Right.SchedulingAssistant);
					for (StudentClassEnrollment enrollment: (List<StudentClassEnrollment>)hibSession.createQuery(
							clazz == null ?
								"from StudentClassEnrollment e where e.courseOffering.instructionalOffering.uniqueId = :offeringId" :
								"select e from StudentClassEnrollment e where e.courseOffering.instructionalOffering.uniqueId = :offeringId and e.student.uniqueId in " +
								"(select f.student.uniqueId from StudentClassEnrollment f where f.clazz.uniqueId = " + clazz.getUniqueId() + ")"
							).setLong("offeringId", offeringId).list()) {
						ClassAssignmentInterface.Enrollment e = student2enrollment.get(enrollment.getStudent().getUniqueId());
						if (e == null) {
							ClassAssignmentInterface.Student st = new ClassAssignmentInterface.Student();
							st.setId(enrollment.getStudent().getUniqueId());
							st.setSessionId(enrollment.getStudent().getSession().getUniqueId());
							st.setExternalId(enrollment.getStudent().getExternalUniqueId());
							st.setCanShowExternalId(canShowExtIds);
							st.setCanRegister(canRegister);
							st.setCanUseAssistant(canUseAssistant);
							st.setName(nameFormat.format(enrollment.getStudent()));
							for (StudentAreaClassificationMajor acm: new TreeSet<StudentAreaClassificationMajor>(enrollment.getStudent().getAreaClasfMajors())) {
								st.addArea(acm.getAcademicArea().getAcademicAreaAbbreviation(), acm.getAcademicArea().getTitle());
								st.addClassification(acm.getAcademicClassification().getCode(), acm.getAcademicClassification().getName());
								st.addMajor(acm.getMajor().getCode(), acm.getMajor().getName());
								st.addConcentration(acm.getConcentration() == null ? null : acm.getConcentration().getCode(), acm.getConcentration() == null ? null : acm.getConcentration().getName());
								st.addDegree(acm.getDegree() == null ? null : acm.getDegree().getReference(), acm.getDegree() == null ? null : acm.getDegree().getLabel());
							}
							for (StudentAreaClassificationMinor acm: new TreeSet<StudentAreaClassificationMinor>(enrollment.getStudent().getAreaClasfMinors())) {
								st.addMinor(acm.getMinor().getCode(), acm.getMinor().getName());
							}
							for (StudentGroup g: enrollment.getStudent().getGroups()) {
								if (g.getType() == null)
									st.addGroup(g.getGroupAbbreviation(), g.getGroupName());
								else
									st.addGroup(g.getType().getReference(), g.getGroupAbbreviation(), g.getGroupName());
							}
							for (StudentAccomodation a: enrollment.getStudent().getAccomodations()) {
								st.addAccommodation(a.getAbbreviation(), a.getName());
							}
			    			for (Advisor a: enrollment.getStudent().getAdvisors()) {
			    				if (a.getLastName() != null)
			    					st.addAdvisor(instructorNameFormat.format(a));
			    			}
			    			
							e = new ClassAssignmentInterface.Enrollment();
							e.setStudent(st);
							e.setEnrolledDate(enrollment.getTimestamp());
							CourseAssignment c = new CourseAssignment();
							c.setCourseId(enrollment.getCourseOffering().getUniqueId());
							c.setSubject(enrollment.getCourseOffering().getSubjectAreaAbbv());
							c.setCourseNbr(enrollment.getCourseOffering().getCourseNbr());
							c.setTitle(enrollment.getCourseOffering().getTitle());
							c.setHasCrossList(enrollment.getCourseOffering().getInstructionalOffering().hasCrossList());
							c.setCanWaitList(enrollment.getCourseOffering().getInstructionalOffering().effectiveWaitList());
							e.setCourse(c);
							student2enrollment.put(enrollment.getStudent().getUniqueId(), e);
							if (enrollment.getCourseRequest() != null) {
								e.setPriority(1 + enrollment.getCourseRequest().getCourseDemand().getPriority());
								if (enrollment.getCourseRequest().getCourseDemand().getCourseRequests().size() > 1) {
									CourseRequest first = null;
									for (CourseRequest r: enrollment.getCourseRequest().getCourseDemand().getCourseRequests()) {
										if (first == null || r.getOrder().compareTo(first.getOrder()) < 0) first = r;
									}
									if (!first.equals(enrollment.getCourseRequest()))
										e.setAlternative(first.getCourseOffering().getCourseName());
								}
								if (enrollment.getCourseRequest().getCourseDemand().isAlternative()) {
									CourseDemand first = enrollment.getCourseRequest().getCourseDemand();
									demands: for (CourseDemand cd: enrollment.getStudent().getCourseDemands()) {
										if (!cd.isAlternative() && cd.getPriority().compareTo(first.getPriority()) < 0 && !cd.getCourseRequests().isEmpty()) {
											for (CourseRequest cr: cd.getCourseRequests())
												if (cr.getClassEnrollments().isEmpty()) continue demands;
											first = cd;
										}
									}
									CourseRequest alt = null;
									for (CourseRequest r: first.getCourseRequests()) {
										if (alt == null || r.getOrder().compareTo(alt.getOrder()) < 0) alt = r;
									}
									e.setAlternative(alt.getCourseOffering().getCourseName());
								}
								e.setRequestedDate(enrollment.getCourseRequest().getCourseDemand().getTimestamp());
								e.setCritical(enrollment.getCourseRequest().getCourseDemand().getEffectiveCritical().ordinal());
								e.setApprovedDate(enrollment.getApprovedDate());
								if (enrollment.getApprovedBy() != null) {
									String name = approvedBy2name.get(enrollment.getApprovedBy());
									if (name == null) {
										TimetableManager mgr = (TimetableManager)hibSession.createQuery(
												"from TimetableManager where externalUniqueId = :externalId")
												.setString("externalId", enrollment.getApprovedBy())
												.setMaxResults(1).uniqueResult();
										if (mgr != null) {
											name = mgr.getName();
										} else {
											DepartmentalInstructor instr = (DepartmentalInstructor)hibSession.createQuery(
													"from DepartmentalInstructor where externalUniqueId = :externalId and department.session.uniqueId = :sessionId")
													.setString("externalId", enrollment.getApprovedBy())
													.setLong("sessionId", enrollment.getStudent().getSession().getUniqueId())
													.setMaxResults(1).uniqueResult();
											if (instr != null)
												name = instr.nameLastNameFirst();
										}
										if (name != null)
											approvedBy2name.put(enrollment.getApprovedBy(), name);
									}
									e.setApprovedBy(name == null ? enrollment.getApprovedBy() : name);
								}
								e.setWaitList(enrollment.getCourseRequest().getCourseDemand().effectiveWaitList());
								e.setNoSub(enrollment.getCourseRequest().getCourseDemand().effectiveNoSub());
							} else {
								e.setPriority(-1);
							}
						}
						ClassAssignmentInterface.ClassAssignment c = e.getCourse().addClassAssignment();
						c.setClassId(enrollment.getClazz().getUniqueId());
						c.setSection(enrollment.getClazz().getClassSuffix(enrollment.getCourseOffering()));
						if (c.getSection() == null)
							c.setSection(enrollment.getClazz().getSectionNumberString(hibSession));
						c.setExternalId(enrollment.getClazz().getExternalId(enrollment.getCourseOffering()));
						c.setClassNumber(enrollment.getClazz().getSectionNumberString(hibSession));
						c.setSubpart(enrollment.getClazz().getSchedulingSubpart().getItypeDesc().trim());
					}
					if (classOrOfferingId >= 0)
						for (CourseRequest request: (List<CourseRequest>)hibSession.createQuery(
							"from CourseRequest r where r.courseOffering.instructionalOffering.uniqueId = :offeringId").setLong("offeringId", classOrOfferingId).list()) {
							ClassAssignmentInterface.Enrollment e = student2enrollment.get(request.getCourseDemand().getStudent().getUniqueId());
							if (e != null) continue;
							ClassAssignmentInterface.Student st = new ClassAssignmentInterface.Student();
							st.setId(request.getCourseDemand().getStudent().getUniqueId());
							st.setSessionId(request.getCourseDemand().getStudent().getSession().getUniqueId());
							st.setExternalId(request.getCourseDemand().getStudent().getExternalUniqueId());
							st.setCanShowExternalId(canShowExtIds);
							st.setCanRegister(canRegister);
							st.setCanUseAssistant(canUseAssistant);
							st.setName(nameFormat.format(request.getCourseDemand().getStudent()));
							for (StudentAreaClassificationMajor acm: new TreeSet<StudentAreaClassificationMajor>(request.getCourseDemand().getStudent().getAreaClasfMajors())) {
								st.addArea(acm.getAcademicArea().getAcademicAreaAbbreviation(), acm.getAcademicArea().getTitle());
								st.addClassification(acm.getAcademicClassification().getCode(), acm.getAcademicClassification().getName());
								st.addMajor(acm.getMajor().getCode(), acm.getMajor().getName());
								st.addConcentration(acm.getConcentration() == null ? null : acm.getConcentration().getCode(), acm.getConcentration() == null ? null : acm.getConcentration().getName());
								st.addDegree(acm.getDegree() == null ? null : acm.getDegree().getReference(), acm.getDegree() == null ? null : acm.getDegree().getLabel());
							}
							for (StudentAreaClassificationMinor acm: new TreeSet<StudentAreaClassificationMinor>(request.getCourseDemand().getStudent().getAreaClasfMinors())) {
								st.addMinor(acm.getMinor().getCode(), acm.getMinor().getName());
							}
							for (StudentGroup g: request.getCourseDemand().getStudent().getGroups()) {
								if (g.getType() == null)
									st.addGroup(g.getGroupAbbreviation(), g.getGroupName());
								else
									st.addGroup(g.getType().getReference(), g.getGroupAbbreviation(), g.getGroupName());
							}
			    			for (StudentAccomodation a: request.getCourseDemand().getStudent().getAccomodations()) {
			    				st.addAccommodation(a.getAbbreviation(), a.getName());
			    			}
			    			for (Advisor a: request.getCourseDemand().getStudent().getAdvisors()) {
			    				if (a.getLastName() != null)
			    					st.addAdvisor(instructorNameFormat.format(a));
			    			}
							e = new ClassAssignmentInterface.Enrollment();
							e.setStudent(st);
							CourseAssignment c = new CourseAssignment();
							c.setCourseId(request.getCourseOffering().getUniqueId());
							c.setSubject(request.getCourseOffering().getSubjectAreaAbbv());
							c.setCourseNbr(request.getCourseOffering().getCourseNbr());
							c.setTitle(request.getCourseOffering().getTitle());
							c.setHasCrossList(request.getCourseOffering().getInstructionalOffering().hasCrossList());
							c.setCanWaitList(request.getCourseOffering().getInstructionalOffering().effectiveWaitList());
							e.setCourse(c);
							e.setWaitList(request.getCourseDemand().effectiveWaitList());
							e.setNoSub(request.getCourseDemand().effectiveNoSub());
							student2enrollment.put(request.getCourseDemand().getStudent().getUniqueId(), e);
							e.setPriority(1 + request.getCourseDemand().getPriority());
							if (request.getCourseDemand().getCourseRequests().size() > 1) {
								CourseRequest first = null;
								for (CourseRequest r: request.getCourseDemand().getCourseRequests()) {
									if (first == null || r.getOrder().compareTo(first.getOrder()) < 0) first = r;
								}
								if (!first.equals(request))
									e.setAlternative(first.getCourseOffering().getCourseName());
							}
							if (request.getCourseDemand().isAlternative()) {
								CourseDemand first = request.getCourseDemand();
								demands: for (CourseDemand cd: request.getCourseDemand().getStudent().getCourseDemands()) {
									if (!cd.isAlternative() && cd.getPriority().compareTo(first.getPriority()) < 0 && !cd.getCourseRequests().isEmpty()) {
										for (CourseRequest cr: cd.getCourseRequests())
											if (cr.getClassEnrollments().isEmpty()) continue demands;
										first = cd;
									}
								}
								CourseRequest alt = null;
								for (CourseRequest r: first.getCourseRequests()) {
									if (alt == null || r.getOrder().compareTo(alt.getOrder()) < 0) alt = r;
								}
								e.setAlternative(alt.getCourseOffering().getCourseName());
							}
							e.setRequestedDate(request.getCourseDemand().getTimestamp());
							e.setCritical(request.getCourseDemand().getEffectiveCritical().ordinal());
							e.setWaitListedDate(request.getCourseDemand().getWaitlistedTimeStamp());
						}
					return new ArrayList<ClassAssignmentInterface.Enrollment>(student2enrollment.values());
				} else {
					return server.execute(server.createAction(ListEnrollments.class)
							.forOffering(offeringId).withSection(clazz == null ? null : clazz.getUniqueId())
							.canShowExternalIds(sessionContext.hasPermission(Right.EnrollmentsShowExternalId))
							.canRegister(sessionContext.hasPermission(Right.CourseRequests))
							.canUseAssistant(sessionContext.hasPermission(Right.SchedulingAssistant))
							.withPermissions(getSessionContext().hasPermissionAnySession(Right.StudentSchedulingAdmin),
									getSessionContext().hasPermissionAnySession(Right.StudentSchedulingAdvisor),
									getSessionContext().hasPermission(Right.StudentSchedulingAdvisorCanModifyMyStudents),
									getSessionContext().hasPermission(Right.StudentSchedulingAdvisorCanModifyAllStudents)),
							currentUser());
				}
			} finally {
				hibSession.close();
			}
		} catch (PageAccessException e) {
			throw e;
		} catch (SectioningException e) {
			throw e;
		} catch  (Exception e) {
			sLog.error(e.getMessage(), e);
			throw new SectioningException(MSG.exceptionUnknown(e.getMessage()), e);
		}
	}
	
	public ClassAssignmentInterface getEnrollment(boolean online, Long studentId) throws SectioningException, PageAccessException {
		try {
			if (online) {
				getSessionContext().checkPermission(studentId, "Student", Right.StudentEnrollments);
				org.hibernate.Session hibSession = SessionDAO.getInstance().getSession();
				try {
					Student student = StudentDAO.getInstance().get(studentId, hibSession);
					if (student == null) 
						throw new SectioningException(MSG.exceptionBadStudentId());
					StudentSectioningStatus status = student.getEffectiveStatus();
					WaitListMode wlMode = student.getWaitListMode();
					OnlineSectioningServer server = getServerInstance(student.getSession().getUniqueId(), false);
					if (server == null) {
						Comparator<StudentClassEnrollment> cmp = new Comparator<StudentClassEnrollment>() {
							public boolean isParent(SchedulingSubpart s1, SchedulingSubpart s2) {
								SchedulingSubpart p1 = s1.getParentSubpart();
								if (p1==null) return false;
								if (p1.equals(s2)) return true;
								return isParent(p1, s2);
							}

							@Override
							public int compare(StudentClassEnrollment a, StudentClassEnrollment b) {
								SchedulingSubpart s1 = a.getClazz().getSchedulingSubpart();
								SchedulingSubpart s2 = b.getClazz().getSchedulingSubpart();
								if (isParent(s1, s2)) return 1;
								if (isParent(s2, s1)) return -1;
								int cmp = s1.getItype().compareTo(s2.getItype());
								if (cmp != 0) return cmp;
								return Double.compare(s1.getUniqueId(), s2.getUniqueId());
							}
						};
						NameFormat nameFormat = NameFormat.fromReference(ApplicationProperty.OnlineSchedulingInstructorNameFormat.value());
						ClassAssignmentInterface ret = new ClassAssignmentInterface();
						Hashtable<Long, CourseAssignment> courses = new Hashtable<Long, ClassAssignmentInterface.CourseAssignment>();
						CourseCreditUnitConfig credit = null;
						HasGradableSubpart gs = null;
						if (ApplicationProperty.OnlineSchedulingGradableIType.isTrue() && Class_.getExternalClassNameHelper() != null && Class_.getExternalClassNameHelper() instanceof HasGradableSubpart)
							gs = (HasGradableSubpart) Class_.getExternalClassNameHelper();
						Set<StudentClassEnrollment> enrollments = new TreeSet<StudentClassEnrollment>(cmp);
						enrollments.addAll(hibSession.createQuery(
								"from StudentClassEnrollment e where e.student.uniqueId = :studentId order by e.courseOffering.subjectAreaAbbv, e.courseOffering.courseNbr"
								).setLong("studentId", studentId).list());
						CustomClassAttendanceProvider provider = Customization.CustomClassAttendanceProvider.getProvider();
						StudentClassAttendance attendance = (provider == null ? null : provider.getCustomClassAttendanceForStudent(student, null, getSessionContext()));
						for (StudentClassEnrollment enrollment: enrollments) {
							CourseAssignment course = courses.get(enrollment.getCourseOffering().getUniqueId());
							if (course == null) {
								course = new CourseAssignment();
								courses.put(enrollment.getCourseOffering().getUniqueId(), course);
								ret.add(course);
								course.setAssigned(true);
								course.setCourseId(enrollment.getCourseOffering().getUniqueId());
								course.setCourseNbr(enrollment.getCourseOffering().getCourseNbr());
								course.setSubject(enrollment.getCourseOffering().getSubjectAreaAbbv());
								course.setTitle(enrollment.getCourseOffering().getTitle());
								course.setHasCrossList(enrollment.getCourseOffering().getInstructionalOffering().hasCrossList());
								course.setCanWaitList(enrollment.getCourseOffering().getInstructionalOffering().effectiveWaitList());
								credit = enrollment.getCourseOffering().getCredit();
								if (enrollment.getCourseRequest() != null) {
									course.setRequestedDate(enrollment.getCourseRequest().getCourseDemand().getTimestamp());
								}
							}
							ClassAssignment clazz = course.addClassAssignment();
							clazz.setClassId(enrollment.getClazz().getUniqueId());
							clazz.setCourseId(enrollment.getCourseOffering().getUniqueId());
							clazz.setCourseAssigned(true);
							clazz.setCourseNbr(enrollment.getCourseOffering().getCourseNbr());
							clazz.setTitle(enrollment.getCourseOffering().getTitle());
							clazz.setSubject(enrollment.getCourseOffering().getSubjectAreaAbbv());
							clazz.setSection(enrollment.getClazz().getClassSuffix(enrollment.getCourseOffering()));
							if (clazz.getSection() == null)
								clazz.setSection(enrollment.getClazz().getSectionNumberString(hibSession));
							clazz.setExternalId(enrollment.getClazz().getExternalId(enrollment.getCourseOffering()));
							clazz.setClassNumber(enrollment.getClazz().getSectionNumberString(hibSession));
							clazz.setSubpart(enrollment.getClazz().getSchedulingSubpart().getItypeDesc().trim());
							if (enrollment.getClazz().getParentClass() != null) {
								clazz.setParentSection(enrollment.getClazz().getParentClass().getClassSuffix(enrollment.getCourseOffering()));
								if (clazz.getParentSection() == null)
									clazz.setParentSection(enrollment.getClazz().getParentClass().getSectionNumberString(hibSession));
							}
							if (enrollment.getCourseOffering().getScheduleBookNote() != null)
								clazz.addNote(enrollment.getCourseOffering().getScheduleBookNote());
							if (enrollment.getClazz().getSchedulePrintNote() != null)
								clazz.addNote(enrollment.getClazz().getSchedulePrintNote());
							if (attendance != null)
								clazz.addNote(attendance.getClassNote(clazz.getExternalId()));
							Placement placement = enrollment.getClazz().getCommittedAssignment() == null ? null : enrollment.getClazz().getCommittedAssignment().getPlacement();
							int minLimit = enrollment.getClazz().getExpectedCapacity();
		                	int maxLimit = enrollment.getClazz().getMaxExpectedCapacity();
		                	int limit = maxLimit;
		                	if (minLimit < maxLimit && placement != null) {
		                		// int roomLimit = Math.round((enrollment.getClazz().getRoomRatio() == null ? 1.0f : enrollment.getClazz().getRoomRatio()) * placement.getRoomSize());
		                		int roomLimit = (int) Math.floor(placement.getRoomSize() / (enrollment.getClazz().getRoomRatio() == null ? 1.0f : enrollment.getClazz().getRoomRatio()));
		                		limit = Math.min(Math.max(minLimit, roomLimit), maxLimit);
		                	}
		                    if (enrollment.getClazz().getSchedulingSubpart().getInstrOfferingConfig().isUnlimitedEnrollment() || limit >= 9999) limit = -1;
		                    clazz.setCancelled(enrollment.getClazz().isCancelled());
							clazz.setLimit(new int[] { enrollment.getClazz().getEnrollment(), limit});
							clazz.setEnrolledDate(enrollment.getTimestamp());
							if (placement != null) {
								if (placement.getTimeLocation() != null) {
									for (DayCode d : DayCode.toDayCodes(placement.getTimeLocation().getDayCode()))
										clazz.addDay(d.getIndex());
									clazz.setStart(placement.getTimeLocation().getStartSlot());
									clazz.setLength(placement.getTimeLocation().getLength());
									clazz.setBreakTime(placement.getTimeLocation().getBreakTime());
									clazz.setDatePattern(placement.getTimeLocation().getDatePatternName());
								}
								if (enrollment.getClazz().getCommittedAssignment() != null)
									for (Location loc: enrollment.getClazz().getCommittedAssignment().getRooms())
										clazz.addRoom(loc.getUniqueId(), loc.getLabelWithDisplayName());
								/*
								if (placement.getNrRooms() == 1) {
									clazz.addRoom(placement.getRoomLocation().getId(), placement.getRoomLocation().getName());
								} else if (placement.getNrRooms() > 1) {
									for (RoomLocation rm: placement.getRoomLocations())
										clazz.addRoom(rm.getId(), rm.getName());
								}
								*/
							}
							if (enrollment.getClazz().getDisplayInstructor())
								for (ClassInstructor ci : enrollment.getClazz().getClassInstructors()) {
									if (!ci.isLead()) continue;
									clazz.addInstructor(nameFormat.format(ci.getInstructor()));
									clazz.addInstructoEmail(ci.getInstructor().getEmail() == null ? "" : ci.getInstructor().getEmail());
								}
							if (credit != null && gs != null && gs.isGradableSubpart(enrollment.getClazz().getSchedulingSubpart(), enrollment.getCourseOffering(), hibSession)) {
								clazz.setCredit(credit.creditAbbv() + "|" + credit.creditText());
								clazz.setCreditRange(credit.getMinCredit(), credit.getMaxCredit());;
								credit = null;
							} else if (credit != null && gs == null) {
								clazz.setCredit(credit.creditAbbv() + "|" + credit.creditText());
								clazz.setCreditRange(credit.getMinCredit(), credit.getMaxCredit());
								credit = null;
							}
							if (enrollment.getClazz().getSchedulingSubpart().getCredit() != null) {
								clazz.setCredit(enrollment.getClazz().getSchedulingSubpart().getCredit().creditAbbv() + "|" + enrollment.getClazz().getSchedulingSubpart().getCredit().creditText());
								clazz.setCreditRange(enrollment.getClazz().getSchedulingSubpart().getCredit().getMinCredit(), enrollment.getClazz().getSchedulingSubpart().getCredit().getMaxCredit());
								credit = null;
							}
							Float creditOverride = enrollment.getClazz().getCredit(enrollment.getCourseOffering());
							if (creditOverride != null) clazz.setCredit(FixedCreditUnitConfig.formatCredit(creditOverride));
							if (clazz.getParentSection() == null)
								clazz.setParentSection(enrollment.getCourseOffering().getConsentType() == null ? null : enrollment.getCourseOffering().getConsentType().getLabel());
						}
						demands: for (CourseDemand demand: (List<CourseDemand>)hibSession.createQuery(
								"from CourseDemand d where d.student.uniqueId = :studentId order by d.priority"
								).setLong("studentId", studentId).list()) {
							if (demand.getFreeTime() != null) {
								CourseAssignment course = new CourseAssignment();
								course.setAssigned(true);
								ClassAssignment clazz = course.addClassAssignment();
								clazz.setLength(demand.getFreeTime().getLength());
								for (DayCode d: DayCode.toDayCodes(demand.getFreeTime().getDayCode()))
									clazz.addDay(d.getIndex());
								clazz.setStart(demand.getFreeTime().getStartSlot());
								ca: for (CourseAssignment ca: ret.getCourseAssignments()) {
									for (ClassAssignment c: ca.getClassAssignments()) {
										if (!c.isAssigned()) continue;
										for (int d: c.getDays())
											if (clazz.getDays().contains(d)) {
												if (c.getStart() + c.getLength() > clazz.getStart() && clazz.getStart() + clazz.getLength() > c.getStart()) {
													course.setAssigned(false);
													break ca;
												}
											}
									}
								}
								course.setRequestedDate(demand.getTimestamp());
								ret.add(course);
							} else {
								CourseRequest request = null;
								for (CourseRequest r: demand.getCourseRequests()) {
									if (courses.containsKey(r.getCourseOffering().getUniqueId())) continue demands;
									if (request == null || r.getOrder().compareTo(request.getOrder()) < 0)
										request = r;
								}
								if (request == null) continue;
								CourseAssignment course = new CourseAssignment();
								courses.put(request.getCourseOffering().getUniqueId(), course);
								course.setRequestedDate(demand.getTimestamp());
								if (demand.effectiveWaitList())
									course.setWaitListedDate(demand.getWaitlistedTimeStamp());
								ret.add(course);
								course.setAssigned(false);
								course.setCourseId(request.getCourseOffering().getUniqueId());
								course.setCourseNbr(request.getCourseOffering().getCourseNbr());
								course.setSubject(request.getCourseOffering().getSubjectAreaAbbv());
								course.setTitle(request.getCourseOffering().getTitle());
								course.setHasCrossList(request.getCourseOffering().getInstructionalOffering().hasCrossList());
								course.setCanWaitList(request.getCourseOffering().getInstructionalOffering().effectiveWaitList());
								ClassAssignment clazz = course.addClassAssignment();
								clazz.setCourseId(request.getCourseOffering().getUniqueId());
								clazz.setCourseAssigned(false);
								clazz.setCourseNbr(request.getCourseOffering().getCourseNbr());
								clazz.setTitle(request.getCourseOffering().getTitle());
								clazz.setSubject(request.getCourseOffering().getSubjectAreaAbbv());
							}
						}
						
						ret.setRequest(getRequest(student));
						ret.setCanSetCriticalOverrides(getSessionContext().hasPermission(student, Right.StudentSchedulingChangeCriticalOverride));
						ret.setAdvisorRequest(AdvisorGetCourseRequests.getRequest(student, hibSession));
						ret.setAdvisorWaitListedCourseIds(student.getAdvisorWaitListedCourseIds(null));
						if (Customization.StudentHoldsCheckProvider.hasProvider()) {
							try {
								OnlineSectioningHelper helper = new OnlineSectioningHelper(hibSession, currentUser());
								try {
									StudentHoldsCheckProvider holds = Customization.StudentHoldsCheckProvider.getProvider();
									ret.getRequest().setErrorMessage(holds.getStudentHoldError(
											getServerInstance(student.getSession().getUniqueId(), true), helper, new XStudentId(student, helper)));
								} catch (Exception e) {
									helper.warn("Failed to lookup critical courses: " + e.getMessage(), e);
								}
							} catch (Exception e) {}
						}
						if (Customization.SpecialRegistrationDashboardUrlProvider.hasProvider()) {
							try {
								SpecialRegistrationDashboardUrlProvider dash = Customization.SpecialRegistrationDashboardUrlProvider.getProvider();
								ret.getRequest().setSpecRegDashboardUrl(dash.getDashboardUrl(student));
							} catch (Exception e) {}
						}
						if (ret.getRequest() != null)
							ret.getRequest().setWaitListMode(wlMode);
						if (ret.getAdvisorRequest() != null) {
							String advWlMode = ApplicationProperty.AdvisorRecommendationsWaitListMode.value(student.getSession());
							if ("Student".equalsIgnoreCase(advWlMode))
								ret.getAdvisorRequest().setWaitListMode(wlMode);
							else
								ret.getAdvisorRequest().setWaitListMode(WaitListMode.valueOf(advWlMode));
						}
						
						for (StudentNote n: student.getNotes()) {
							ClassAssignmentInterface.Note note = new ClassAssignmentInterface.Note();
							note.setTimeStamp(n.getTimeStamp());
							note.setId(n.getUniqueId());
							note.setMessage(n.getTextNote());
							note.setOwner(n.getUserId());
							TimetableManager manager = TimetableManager.findByExternalId(n.getUserId());
							if (manager != null) {
								note.setOwner(nameFormat.format(manager));
							} else {
								Advisor advisor = Advisor.findByExternalId(n.getUserId(), student.getSession().getUniqueId());
								if (advisor != null) note.setOwner(nameFormat.format(advisor));
							}
							ret.addNote(note);
						}
						
						if (status != null && status.hasOption(Option.specreg) && Customization.SpecialRegistrationProvider.hasProvider()) {
							try {
								SpecialRegistrationProvider sp = Customization.SpecialRegistrationProvider.getProvider();
								OnlineSectioningHelper helper = new OnlineSectioningHelper(hibSession, currentUser());
								server = getServerInstance(student.getSession().getUniqueId(), true);
								ret.setSpecialRegistrations(sp.retrieveAllRegistrations(server, helper, new XStudent(student, helper, server.getAcademicSession().getFreeTimePattern())));
							} catch (Exception e) {}
						}
						
						return ret;
					} else {
						ClassAssignmentInterface ret = server.execute(server.createAction(GetAssignment.class).forStudent(studentId)
								.withRequest(true).withCustomCheck(true).withWaitListCheck(true).withAdvisorRequest(true).checkHolds(true).withWaitListMode(wlMode)
								.withSpecialRegistrations(status != null && status.hasOption(Option.specreg)).withWaitListPosition(true), currentUser());
						ret.setCanSetCriticalOverrides(getSessionContext().hasPermission(student, Right.StudentSchedulingChangeCriticalOverride));
						if (ret.getRequest() != null)
							ret.getRequest().setWaitListMode(wlMode);
						if (ret.getAdvisorRequest() != null) {
							String advWlMode = ApplicationProperty.AdvisorRecommendationsWaitListMode.value(student.getSession());
							if ("Student".equalsIgnoreCase(advWlMode))
								ret.getAdvisorRequest().setWaitListMode(wlMode);
							else
								ret.getAdvisorRequest().setWaitListMode(WaitListMode.valueOf(advWlMode));
						}
						
						NameFormat nameFormat = NameFormat.fromReference(ApplicationProperty.OnlineSchedulingInstructorNameFormat.value());
						for (StudentNote n: student.getNotes()) {
							ClassAssignmentInterface.Note note = new ClassAssignmentInterface.Note();
							note.setTimeStamp(n.getTimeStamp());
							note.setId(n.getUniqueId());
							note.setMessage(n.getTextNote());
							note.setOwner(n.getUserId());
							TimetableManager manager = TimetableManager.findByExternalId(n.getUserId());
							if (manager != null) {
								note.setOwner(nameFormat.format(manager));
							} else {
								Advisor advisor = Advisor.findByExternalId(n.getUserId(), server.getAcademicSession().getUniqueId());
								if (advisor != null) note.setOwner(nameFormat.format(advisor));
							}
							ret.addNote(note);
						}
						
						return ret;
					}
				} finally {
					hibSession.close();
				}				
			} else {
				OnlineSectioningServer server = getStudentSolver();
				if (server == null) 
					throw new SectioningException(MSG.exceptionNoSolver());
				
				WaitListMode wlMode = WaitListMode.NoSubs;
				
				ClassAssignmentInterface ret = server.execute(server.createAction(GetAssignment.class).forStudent(studentId).withRequest(true).withAdvisorRequest(true).withWaitListMode(wlMode), currentUser());
				
				if (ret.getRequest() != null)
					ret.getRequest().setWaitListMode(wlMode);
				if (ret.getAdvisorRequest() != null) {
					String advWlMode = ApplicationProperty.AdvisorRecommendationsWaitListMode.value(server.getAcademicSession());
					if ("Student".equalsIgnoreCase(advWlMode))
						ret.getAdvisorRequest().setWaitListMode(wlMode);
					else
						ret.getAdvisorRequest().setWaitListMode(WaitListMode.valueOf(advWlMode));
				}

				return ret;
			}
		} catch (PageAccessException e) {
			throw e;
		} catch (SectioningException e) {
			throw e;
		} catch (AccessDeniedException e) {
			throw new PageAccessException(e.getMessage());
		} catch  (Exception e) {
			sLog.error(e.getMessage(), e);
			throw new SectioningException(MSG.exceptionUnknown(e.getMessage()), e);
		}
	}

	@Override
	public String approveEnrollments(Long classOrOfferingId, List<Long> studentIds) throws SectioningException, PageAccessException {
		try {
			org.hibernate.Session hibSession = SessionDAO.getInstance().getSession();
			
			List<Long> courseIdsCanApprove = canApprove(classOrOfferingId);
			
			if (courseIdsCanApprove == null || courseIdsCanApprove.isEmpty())
				throw new SectioningException(MSG.exceptionInsufficientPrivileges());

			InstructionalOffering offering = (classOrOfferingId >= 0 ? InstructionalOfferingDAO.getInstance().get(classOrOfferingId, hibSession) : null);
			if (offering == null) {
				Class_ clazz = (classOrOfferingId < 0 ? Class_DAO.getInstance().get(-classOrOfferingId, hibSession) : null);
				if (clazz == null)
					throw new SectioningException(MSG.exceptionBadClassOrOffering());
				offering = clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering();
			}
			
			OnlineSectioningServer server = getServerInstance(offering.getControllingCourseOffering().getSubjectArea().getSessionId(), false);
			
			UserContext user = getSessionContext().getUser();
			String approval = new Date().getTime() + ":" + user.getTrueExternalUserId() + ":" + user.getTrueName();
			server.execute(server.createAction(ApproveEnrollmentsAction.class).withParams(offering.getUniqueId(), studentIds, courseIdsCanApprove, approval), currentUser());
			
			return approval;
		} catch (PageAccessException e) {
			throw e;
		} catch (SectioningException e) {
			throw e;
		} catch  (Exception e) {
			sLog.error(e.getMessage(), e);
			throw new SectioningException(MSG.exceptionUnknown(e.getMessage()), e);
		}
	}

	@Override
	public Boolean rejectEnrollments(Long classOrOfferingId, List<Long> studentIds) throws SectioningException, PageAccessException {
		try {
			org.hibernate.Session hibSession = SessionDAO.getInstance().getSession();
			
			List<Long> courseIdsCanApprove = canApprove(classOrOfferingId);
			if (courseIdsCanApprove == null || courseIdsCanApprove.isEmpty())
				throw new SectioningException(MSG.exceptionInsufficientPrivileges());
			
			InstructionalOffering offering = (classOrOfferingId >= 0 ? InstructionalOfferingDAO.getInstance().get(classOrOfferingId, hibSession) : null);
			if (offering == null) {
				Class_ clazz = (classOrOfferingId < 0 ? Class_DAO.getInstance().get(-classOrOfferingId, hibSession) : null);
				if (clazz == null)
					throw new SectioningException(MSG.exceptionBadClassOrOffering());
				offering = clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering();
			}
			
			OnlineSectioningServer server = getServerInstance(offering.getControllingCourseOffering().getSubjectArea().getSessionId(), false);
			
			UserContext user = getSessionContext().getUser();
			String approval = new Date().getTime() + ":" + user.getTrueExternalUserId() + ":" + user.getTrueName();
			
			return server.execute(server.createAction(RejectEnrollmentsAction.class).withParams(offering.getUniqueId(), studentIds, courseIdsCanApprove, approval), currentUser());
		} catch (PageAccessException e) {
			throw e;
		} catch (SectioningException e) {
			throw e;
		} catch  (Exception e) {
			sLog.error(e.getMessage(), e);
			throw new SectioningException(MSG.exceptionUnknown(e.getMessage()), e);
		}
	}
	
	private Long getStatusPageSessionId() throws SectioningException, PageAccessException {
		UserContext user = getSessionContext().getUser();
		if (user == null)
			throw new PageAccessException(getSessionContext().isHttpSessionNew() ? MSG.exceptionHttpSessionExpired() : MSG.exceptionLoginRequired());
		if (user.getCurrentAcademicSessionId() == null) {
			Long sessionId = getLastSessionId();
			if (sessionId != null) return sessionId;
		} else {
			return user.getCurrentAcademicSessionId();
		}
		throw new SectioningException(MSG.exceptionNoAcademicSession());
	}
	
	private HashSet<Long> getCoordinatingCourses(Long sessionId) throws SectioningException, PageAccessException {
		UserContext user = getSessionContext().getUser();
		if (user == null)
			throw new PageAccessException(getSessionContext().isHttpSessionNew() ? MSG.exceptionHttpSessionExpired() : MSG.exceptionLoginRequired());

		if (getSessionContext().hasPermission(Right.HasRole)) return null; // only applies to users without a role
		
		HashSet<Long> courseIds = new HashSet<Long>(CourseOfferingDAO.getInstance().getSession().createQuery(
				"select distinct c.uniqueId from CourseOffering c inner join c.instructionalOffering.offeringCoordinators oc where " +
				"c.subjectArea.session.uniqueId = :sessionId and c.subjectArea.department.allowStudentScheduling = true and oc.instructor.externalUniqueId = :extId")
				.setLong("sessionId", sessionId).setString("extId", user.getExternalUserId()).setCacheable(true).list());
		
		return courseIds;
	}
	
	private Set<String> getSubjectAreas() throws SectioningException, PageAccessException {
		UserContext user = getSessionContext().getUser();
		if (user == null)
			throw new PageAccessException(getSessionContext().isHttpSessionNew() ? MSG.exceptionHttpSessionExpired() : MSG.exceptionLoginRequired());

		if (!getSessionContext().hasPermission(Right.HasRole) || getSessionContext().hasPermission(Right.DepartmentIndependent)) return null; // only applies to users with a role that is department dependent
		
		// scheduling advisors and admins can see all courses of a student regardless of whether they have the Department Independent permission or not
		if (getSessionContext().hasPermission(Right.StudentSchedulingAdmin) || getSessionContext().hasPermission(Right.StudentSchedulingAdvisor)) return null; 
		
		HashSet<String> subjects = new HashSet<String>();
		for (SubjectArea subject: SubjectArea.getUserSubjectAreas(user)) {
			subjects.add(subject.getSubjectAreaAbbreviation());
		}
		return subjects;
	}
	
	private HashSet<Long> getApprovableCourses(Long sessionId) throws SectioningException, PageAccessException {
		UserContext user = getSessionContext().getUser();
		if (user == null)
			throw new PageAccessException(getSessionContext().isHttpSessionNew() ? MSG.exceptionHttpSessionExpired() : MSG.exceptionLoginRequired());

		HashSet<Long> courseIds = new HashSet<Long>(CourseOfferingDAO.getInstance().getSession().createQuery(
				"select distinct c.uniqueId from CourseOffering c inner join c.instructionalOffering.offeringCoordinators oc where " +
				"c.subjectArea.session.uniqueId = :sessionId and c.subjectArea.department.allowStudentScheduling = true and c.consentType.reference = :reference and " +
				"oc.instructor.externalUniqueId = :extId"
				).setLong("sessionId", sessionId).setString("reference", "IN").setString("extId", user.getExternalUserId()).setCacheable(true).list());
		
		if (!user.getCurrentAuthority().hasRight(Right.HasRole)) return courseIds;
		
		if (user.getCurrentAuthority().hasRight(Right.SessionIndependent))
			return new HashSet<Long>(CourseOfferingDAO.getInstance().getSession().createQuery(
					"select c.uniqueId from CourseOffering c where c.subjectArea.session.uniqueId = :sessionId and c.subjectArea.department.allowStudentScheduling = true and c.consentType is not null"
					).setLong("sessionId", sessionId).setCacheable(true).list());
		
		for (Department d: Department.getUserDepartments(user)) {
			courseIds.addAll(CourseOfferingDAO.getInstance().getSession().createQuery(
					"select distinct c.uniqueId from CourseOffering c where " +
					"c.subjectArea.department.uniqueId = :departmentId and c.subjectArea.department.allowStudentScheduling = true and c.consentType is not null"
					).setLong("departmentId", d.getUniqueId()).setCacheable(true).list());
		}
		
		return courseIds;
	}
	
	private HashSet<Long> getMyStudents(Long sessionId) throws SectioningException, PageAccessException {
		UserContext user = getSessionContext().getUser();
		if (user == null || user.getCurrentAuthority() == null)
			throw new PageAccessException(getSessionContext().isHttpSessionNew() ? MSG.exceptionHttpSessionExpired() : MSG.exceptionLoginRequired());

		return new HashSet<Long>(CourseOfferingDAO.getInstance().getSession().createQuery(
				"select s.uniqueId from Advisor a inner join a.students s where " +
				"a.externalUniqueId = :user and a.role.reference = :role and a.session.uniqueId = :sessionId"
				).setLong("sessionId", sessionId).setString("user", user.getExternalUserId()).setString("role", user.getCurrentAuthority().getRole()).setCacheable(true).list());
	}
	
	public List<EnrollmentInfo> findEnrollmentInfos(boolean online, String query, SectioningStatusFilterRpcRequest filter, Long courseId) throws SectioningException, PageAccessException {
		try {
			if (filter != null && sessionContext.isAuthenticated()) {
				filter.setOption("user", sessionContext.getUser().getExternalUserId());
				if (sessionContext.getUser().getCurrentAuthority() != null)
					filter.setOption("role", sessionContext.getUser().getCurrentAuthority().getRole());
			}
			if (online) {
				final Long sessionId = getStatusPageSessionId();
				
				OnlineSectioningServer server = getServerInstance(sessionId, true);
				if (server == null)
					throw new SectioningException(MSG.exceptionBadSession());
				
				if (server instanceof DatabaseServer) {
					return server.execute(server.createAction(DbFindEnrollmentInfoAction.class).withParams(
							query,
							courseId,
							getCoordinatingCourses(sessionId),
							query.matches("(?i:.*consent:[ ]?(todo|\\\"to do\\\").*)") ? getApprovableCourses(sessionId) : null,
							getMyStudents(sessionId),
							getSubjectAreas())
							.showUnmatchedClasses(CommonValues.Yes.eq(UserProperty.StudentDashboardShowUnmatchedClasses.get(sessionContext.getUser())))
							.withFilter(filter), currentUser()
					);	
				}
							
				return server.execute(server.createAction(FindEnrollmentInfoAction.class).withParams(
						query,
						courseId,
						getCoordinatingCourses(sessionId),
						query.matches("(?i:.*consent:[ ]?(todo|\\\"to do\\\").*)") ? getApprovableCourses(sessionId) : null,
						getMyStudents(sessionId),
						getSubjectAreas())
						.showUnmatchedClasses(CommonValues.Yes.eq(UserProperty.StudentDashboardShowUnmatchedClasses.get(sessionContext.getUser())))
						.withFilter(filter), currentUser()
				);				
			} else {
				OnlineSectioningServer server = getStudentSolver();
				if (server == null) 
					throw new SectioningException(MSG.exceptionNoSolver());

				return server.execute(server.createAction(FindEnrollmentInfoAction.class)
						.withParams(query, courseId, null, null, getMyStudents(server.getAcademicSession().getUniqueId()), getSubjectAreas())
						.showUnmatchedClasses(CommonValues.Yes.eq(UserProperty.StudentDashboardShowUnmatchedClasses.get(sessionContext.getUser())))
						.withFilter(filter), currentUser());
			}
		} catch (PageAccessException e) {
			throw e;
		} catch (SectioningException e) {
			throw e;
		} catch  (Exception e) {
			sLog.error(e.getMessage(), e);
			throw new SectioningException(MSG.exceptionUnknown(e.getMessage()), e);
		}
	}
	
	public List<ClassAssignmentInterface.StudentInfo> findStudentInfos(boolean online, String query, SectioningStatusFilterRpcRequest filter) throws SectioningException, PageAccessException {
		try {
			if (filter != null && sessionContext.isAuthenticated()) {
				filter.setOption("user", sessionContext.getUser().getExternalUserId());
				if (sessionContext.getUser().getCurrentAuthority() != null)
					filter.setOption("role", sessionContext.getUser().getCurrentAuthority().getRole());
			}
			if (online) {
				Long sessionId = getStatusPageSessionId();
				
				OnlineSectioningServer server = getServerInstance(sessionId, true);
				if (server == null)
					throw new SectioningException(MSG.exceptionBadSession());
				
				if (server instanceof DatabaseServer) {
					return server.execute(server.createAction(DbFindStudentInfoAction.class).withParams(
							query,
							getCoordinatingCourses(sessionId),
							query.matches("(?i:.*consent:[ ]?(todo|\\\"to do\\\").*)") ? getApprovableCourses(sessionId) : null,
									getMyStudents(sessionId),
							getSubjectAreas(),
							sessionContext.hasPermission(Right.EnrollmentsShowExternalId),
							sessionContext.hasPermission(Right.CourseRequests),
							sessionContext.hasPermission(Right.SchedulingAssistant))
							.withFilter(filter)
							.withPermissions(getSessionContext().hasPermissionAnySession(sessionId, Right.StudentSchedulingAdmin),
									getSessionContext().hasPermissionAnySession(sessionId, Right.StudentSchedulingAdvisor),
									getSessionContext().hasPermission(Right.StudentSchedulingAdvisorCanModifyMyStudents),
									getSessionContext().hasPermission(Right.StudentSchedulingAdvisorCanModifyAllStudents),
									getSessionContext().hasPermission(Right.StudentSchedulingChangeStudentStatus) || getSessionContext().hasPermission(Right.StudentSchedulingEmailStudent)),
							currentUser()
					);
				}
				
				return server.execute(server.createAction(FindStudentInfoAction.class).withParams(
						query,
						getCoordinatingCourses(sessionId),
						query.matches("(?i:.*consent:[ ]?(todo|\\\"to do\\\").*)") ? getApprovableCourses(sessionId) : null,
						getMyStudents(sessionId),
						getSubjectAreas(),
						sessionContext.hasPermission(Right.EnrollmentsShowExternalId),
						sessionContext.hasPermission(Right.CourseRequests),
						sessionContext.hasPermission(Right.SchedulingAssistant))
						.withFilter(filter)
						.withPermissions(getSessionContext().hasPermissionAnySession(sessionId, Right.StudentSchedulingAdmin),
								getSessionContext().hasPermissionAnySession(sessionId, Right.StudentSchedulingAdvisor),
								getSessionContext().hasPermission(Right.StudentSchedulingAdvisorCanModifyMyStudents),
								getSessionContext().hasPermission(Right.StudentSchedulingAdvisorCanModifyAllStudents),
								getSessionContext().hasPermission(Right.StudentSchedulingChangeStudentStatus) || getSessionContext().hasPermission(Right.StudentSchedulingEmailStudent)),
						currentUser()
				);
			} else {
				OnlineSectioningServer server = getStudentSolver();
				if (server == null) 
					throw new SectioningException(MSG.exceptionNoSolver());

				return server.execute(server.createAction(FindStudentInfoAction.class).withParams(query, null, null, getMyStudents(server.getAcademicSession().getUniqueId()), getSubjectAreas(),
						sessionContext.hasPermission(Right.EnrollmentsShowExternalId), false, true).withFilter(filter)
						.withPermissions(getSessionContext().hasPermissionAnySession(server.getAcademicSession().getUniqueId(), Right.StudentSchedulingAdmin),
								getSessionContext().hasPermissionAnySession(server.getAcademicSession().getUniqueId(), Right.StudentSchedulingAdvisor),
								getSessionContext().hasPermission(Right.StudentSchedulingAdvisorCanModifyMyStudents),
								getSessionContext().hasPermission(Right.StudentSchedulingAdvisorCanModifyAllStudents),
								getSessionContext().hasPermission(Right.StudentSchedulingChangeStudentStatus) || getSessionContext().hasPermission(Right.StudentSchedulingEmailStudent)),
						currentUser());
			}
		} catch (PageAccessException e) {
			throw e;
		} catch (SectioningException e) {
			throw e;
		} catch  (Exception e) {
			sLog.error(e.getMessage(), e);
			throw new SectioningException(MSG.exceptionUnknown(e.getMessage()), e);
		}
	}
	public List<String[]> querySuggestions(boolean online, String query, int limit) throws SectioningException, PageAccessException {
		try {
			if (online) {
				Long sessionId = getStatusPageSessionId();
				
				OnlineSectioningServer server = getServerInstance(sessionId, true);
				if (server == null)
					throw new SectioningException(MSG.exceptionBadSession());
				
				UserContext user = getSessionContext().getUser();
				return server.execute(server.createAction(StatusPageSuggestionsAction.class).withParams(
						user.getExternalUserId(), user.getName(),
						query, limit), currentUser());				
			} else {
				OnlineSectioningServer server = getStudentSolver();
				if (server == null) 
					throw new SectioningException(MSG.exceptionNoSolver());

				UserContext user = getSessionContext().getUser();
				return server.execute(server.createAction(StatusPageSuggestionsAction.class).withParams(
						user.getExternalUserId(), user.getName(),
						query, limit), currentUser());				
			}
		} catch (PageAccessException e) {
			throw e;
		} catch (SectioningException e) {
			throw e;
		} catch  (Exception e) {
			sLog.error(e.getMessage(), e);
			throw new SectioningException(MSG.exceptionUnknown(e.getMessage()), e);
		}
	}

	@Override
	public List<org.unitime.timetable.gwt.shared.ClassAssignmentInterface.Enrollment> findEnrollments(
			boolean online, String query, SectioningStatusFilterRpcRequest filter, Long courseId, Long classId)
			throws SectioningException, PageAccessException {
		try {
			if (filter != null && sessionContext.isAuthenticated()) {
				filter.setOption("user", sessionContext.getUser().getExternalUserId());
				if (sessionContext.getUser().getCurrentAuthority() != null)
					filter.setOption("role", sessionContext.getUser().getCurrentAuthority().getRole());
			}
			if (online) {
				Long sessionId = getStatusPageSessionId();
				
				OnlineSectioningServer server = getServerInstance(sessionId, true);
				if (server == null)
					throw new SectioningException(MSG.exceptionBadSession());
				
				if (getSessionContext().isAuthenticated())
					getSessionContext().getUser().setProperty("SectioningStatus.LastStatusQuery", query);
				
				if (server instanceof DatabaseServer) {
					return server.execute(server.createAction(DbFindEnrollmentAction.class).withParams(
							query, courseId, classId, 
							query.matches("(?i:.*consent:[ ]?(todo|\\\"to do\\\").*)") ? getApprovableCourses(sessionId).contains(courseId): false,
							sessionContext.hasPermission(Right.EnrollmentsShowExternalId),
							sessionContext.hasPermission(Right.CourseRequests),
							sessionContext.hasPermission(Right.SchedulingAssistant),
							getMyStudents(sessionId)).withFilter(filter)
							.withPermissions(getSessionContext().hasPermissionAnySession(sessionId, Right.StudentSchedulingAdmin),
									getSessionContext().hasPermissionAnySession(sessionId, Right.StudentSchedulingAdvisor),
									getSessionContext().hasPermission(Right.StudentSchedulingAdvisorCanModifyMyStudents),
									getSessionContext().hasPermission(Right.StudentSchedulingAdvisorCanModifyAllStudents),
									getSessionContext().hasPermission(Right.StudentSchedulingChangeStudentStatus) || getSessionContext().hasPermission(Right.StudentSchedulingEmailStudent)),
							currentUser());
				}
				
				return server.execute(server.createAction(FindEnrollmentAction.class).withParams(
						query, courseId, classId, 
						query.matches("(?i:.*consent:[ ]?(todo|\\\"to do\\\").*)") ? getApprovableCourses(sessionId).contains(courseId): false,
						sessionContext.hasPermission(Right.EnrollmentsShowExternalId),
						sessionContext.hasPermission(Right.CourseRequests),
						sessionContext.hasPermission(Right.SchedulingAssistant),
						getMyStudents(sessionId)).withFilter(filter)
						.withPermissions(getSessionContext().hasPermissionAnySession(sessionId, Right.StudentSchedulingAdmin),
								getSessionContext().hasPermissionAnySession(sessionId, Right.StudentSchedulingAdvisor),
								getSessionContext().hasPermission(Right.StudentSchedulingAdvisorCanModifyMyStudents),
								getSessionContext().hasPermission(Right.StudentSchedulingAdvisorCanModifyAllStudents),
								getSessionContext().hasPermission(Right.StudentSchedulingChangeStudentStatus) || getSessionContext().hasPermission(Right.StudentSchedulingEmailStudent)),
						currentUser());
			} else {
				OnlineSectioningServer server = getStudentSolver();
				if (server == null) 
					throw new SectioningException(MSG.exceptionNoSolver());
				
				if (getSessionContext().isAuthenticated())
					getSessionContext().getUser().setProperty("SectioningStatus.LastStatusQuery", query);
				
				return server.execute(server.createAction(FindEnrollmentAction.class).withParams(
						query, courseId, classId, false,
						sessionContext.hasPermission(Right.EnrollmentsShowExternalId), false, true,
						getMyStudents(server.getAcademicSession().getUniqueId())).withFilter(filter)
						.withPermissions(getSessionContext().hasPermissionAnySession(server.getAcademicSession().getUniqueId(), Right.StudentSchedulingAdmin),
								getSessionContext().hasPermissionAnySession(server.getAcademicSession().getUniqueId(), Right.StudentSchedulingAdvisor),
								getSessionContext().hasPermission(Right.StudentSchedulingAdvisorCanModifyMyStudents),
								getSessionContext().hasPermission(Right.StudentSchedulingAdvisorCanModifyAllStudents),
								getSessionContext().hasPermission(Right.StudentSchedulingChangeStudentStatus) || getSessionContext().hasPermission(Right.StudentSchedulingEmailStudent)),
						currentUser());
			}
		} catch (PageAccessException e) {
			throw e;
		} catch (SectioningException e) {
			throw e;
		} catch  (Exception e) {
			sLog.error(e.getMessage(), e);
			throw new SectioningException(MSG.exceptionUnknown(e.getMessage()), e);
		}
	}
	
	protected EligibilityCheck getLastEligibilityCheck(StudentSectioningContext cx) throws SectioningException, PageAccessException {
		EligibilityCheck last = (EligibilityCheck)getSessionContext().getAttribute(SessionAttribute.OnlineSchedulingEligibility);
		if (last == null) return checkEligibility(cx);
		if (cx.getSessionId() != null && !cx.getSessionId().equals(last.getSessionId())) return checkEligibility(cx);
		if (cx.getStudentId() != null && !cx.getStudentId().equals(last.getStudentId())) return checkEligibility(cx);
		return last;
	}

	@Override
	public Long canEnroll(StudentSectioningContext cx) throws SectioningException, PageAccessException {
		try {
			checkContext(cx);
			if (!cx.isOnline()) {
				OnlineSectioningServer server = getStudentSolver();
				if (server == null) 
					throw new SectioningException(MSG.exceptionNoSolver());
				
				CourseRequestInterface request = server.execute(server.createAction(GetRequest.class).forStudent(cx.getStudentId()).withWaitListMode(WaitListMode.NoSubs), currentUser(cx));
				if (request == null)
					throw new SectioningException(MSG.exceptionBadStudentId());

				return server.getAcademicSession().getUniqueId();
			}
			
			boolean recheckCustomEligibility = ApplicationProperty.OnlineSchedulingCustomEligibilityRecheck.isTrue();
			if (!recheckCustomEligibility) {
				EligibilityCheck last = getLastEligibilityCheck(cx);
				if (last != null && (last.hasFlag(EligibilityFlag.RECHECK_BEFORE_ENROLLMENT) || !last.hasFlag(EligibilityFlag.CAN_ENROLL)))
					recheckCustomEligibility = true;
			}
			
			EligibilityCheck check = checkEligibility(cx, recheckCustomEligibility);
			if (check == null || !check.hasFlag(EligibilityFlag.CAN_ENROLL) || check.hasFlag(EligibilityFlag.RECHECK_BEFORE_ENROLLMENT)) {
				if (check.hasFlag(EligibilityFlag.PIN_REQUIRED))
					throw new SectioningException(check.getMessage() == null ? MSG.exceptionAuthenticationPinNotProvided() : check.getMessage()).withEligibilityCheck(check);
				else
					throw new PageAccessException(check.getMessage() == null ? MSG.exceptionInsufficientPrivileges() : check.getMessage());
			}
			
			if (cx.getStudentId() == null || cx.getStudentId().equals(getStudentId(check.getSessionId())))
				return check.getSessionId();
			
			if (getSessionContext().hasPermissionAnySession(check.getSessionId(), Right.StudentSchedulingAdvisor))
				return check.getSessionId();
			
			OnlineSectioningServer server = getServerInstance(check.getSessionId(), false);
			if (server == null)
				throw new SectioningException(MSG.exceptionNoServerForSession());
			
			if (getStudentId(check.getSessionId()) == null)
				throw new PageAccessException(MSG.exceptionEnrollNotStudent(server.getAcademicSession().toString()));
			
			throw new PageAccessException(MSG.exceptionInsufficientPrivileges());
		} catch (PageAccessException e) {
			throw e;
		} catch (SectioningException e) {
			throw e;
		} catch (Exception e) {
			sLog.error(e.getMessage(), e);
			throw new SectioningException(MSG.exceptionUnknown(e.getMessage()), e);
		}
	}
	
	protected CourseRequestInterface getRequest(Student student) {
		CourseRequestInterface request = new CourseRequestInterface();
		request.setAcademicSessionId(student.getSession().getUniqueId());
		request.setStudentId(student.getUniqueId());
		request.setSaved(true);
		request.setMaxCredit(student.getMaxCredit());
		request.setWaitListMode(student.getWaitListMode());
		if (student.getOverrideMaxCredit() != null) {
			request.setMaxCreditOverride(student.getOverrideMaxCredit());
			request.setMaxCreditOverrideExternalId(student.getOverrideExternalId());
			request.setMaxCreditOverrideTimeStamp(student.getOverrideTimeStamp());
			Integer status = student.getOverrideStatus();
			if (status == null)
				request.setMaxCreditOverrideStatus(RequestedCourseStatus.OVERRIDE_PENDING);
			else if (status == org.unitime.timetable.model.CourseRequest.CourseRequestOverrideStatus.APPROVED.ordinal())
				request.setMaxCreditOverrideStatus(RequestedCourseStatus.OVERRIDE_APPROVED);
			else if (status == org.unitime.timetable.model.CourseRequest.CourseRequestOverrideStatus.REJECTED.ordinal())
				request.setMaxCreditOverrideStatus(RequestedCourseStatus.OVERRIDE_REJECTED);
			else if (status == org.unitime.timetable.model.CourseRequest.CourseRequestOverrideStatus.CANCELLED.ordinal())
				request.setMaxCreditOverrideStatus(RequestedCourseStatus.OVERRIDE_CANCELLED);
			else if (status == org.unitime.timetable.model.CourseRequest.CourseRequestOverrideStatus.NOT_CHECKED.ordinal())
				request.setMaxCreditOverrideStatus(RequestedCourseStatus.OVERRIDE_NEEDED);
			else
				request.setMaxCreditOverrideStatus(RequestedCourseStatus.OVERRIDE_PENDING);
		}
		boolean setReadOnlyWhenReserved = ApplicationProperty.OnlineSchedulingMakeReservedRequestReadOnly.isTrue();
		if (getSessionContext().hasPermissionAnySession(student.getSession().getUniqueId(), Right.StudentSchedulingAdmin) || getSessionContext().hasPermissionAnySession(student.getSession().getUniqueId(), Right.StudentSchedulingAdvisor)) {
			setReadOnlyWhenReserved = ApplicationProperty.OnlineSchedulingMakeReservedRequestReadOnlyIfAdmin.isTrue();
		}
		boolean reservedNoPriority = ApplicationProperty.OnlineSchedulingReservedRequestNoPriorityChanges.isTrue();
		boolean reservedNoAlternatives = ApplicationProperty.OnlineSchedulingReservedRequestNoAlternativeChanges.isTrue();
		boolean enrolledNoPriority = ApplicationProperty.OnlineSchedulingAssignedRequestNoPriorityChanges.isTrue();
		boolean enrolledNoAlternatives = ApplicationProperty.OnlineSchedulingAssignedRequestNoAlternativeChanges.isTrue();
		Set<Long> courseIds = new HashSet<Long>();
		if (!student.getCourseDemands().isEmpty()) {
			TreeSet<CourseDemand> demands = new TreeSet<CourseDemand>(new Comparator<CourseDemand>() {
				public int compare(CourseDemand d1, CourseDemand d2) {
					if (d1.isAlternative() && !d2.isAlternative()) return 1;
					if (!d1.isAlternative() && d2.isAlternative()) return -1;
					int cmp = d1.getPriority().compareTo(d2.getPriority());
					if (cmp != 0) return cmp;
					return d1.getUniqueId().compareTo(d2.getUniqueId());
				}
			});
			demands.addAll(student.getCourseDemands());
			CourseRequestInterface.Request lastRequest = null;
			int lastRequestPriority = -1;
			for (CourseDemand cd: demands) {
				CourseRequestInterface.Request r = null;
				if (cd.getFreeTime() != null) {
					CourseRequestInterface.FreeTime ft = new CourseRequestInterface.FreeTime();
					ft.setStart(cd.getFreeTime().getStartSlot());
					ft.setLength(cd.getFreeTime().getLength());
					for (DayCode day : DayCode.toDayCodes(cd.getFreeTime().getDayCode()))
						ft.addDay(day.getIndex());
					if (lastRequest != null && lastRequestPriority == cd.getPriority() && lastRequest.hasRequestedCourse() && lastRequest.getRequestedCourse(0).isFreeTime()) {
						lastRequest.getRequestedCourse(0).addFreeTime(ft);
					} else {
						r = new CourseRequestInterface.Request();
						RequestedCourse rc = new RequestedCourse();
						rc.addFreeTime(ft);
						r.addRequestedCourse(rc);
						if (cd.isAlternative())
							request.getAlternatives().add(r);
						else
							request.getCourses().add(r);
						lastRequest = r;
						lastRequestPriority = cd.getPriority();
						rc.setStatus(RequestedCourseStatus.SAVED);
					}
				} else if (!cd.getCourseRequests().isEmpty()) {
					r = new CourseRequestInterface.Request();
					for (CourseRequest course: new TreeSet<CourseRequest>(cd.getCourseRequests())) {
						courseIds.add(course.getCourseOffering().getUniqueId());
						RequestedCourse rc = new RequestedCourse();
						rc.setCourseId(course.getCourseOffering().getUniqueId());
						rc.setCourseName(course.getCourseOffering().getSubjectAreaAbbv() + " " + course.getCourseOffering().getCourseNbr() + (!CONSTANTS.showCourseTitle() ? "" : " - " + course.getCourseOffering().getTitle()));
						rc.setCourseTitle(course.getCourseOffering().getTitle());
						CourseCreditUnitConfig credit = course.getCourseOffering().getCredit(); 
						if (credit != null) rc.setCredit(credit.getMinCredit(), credit.getMaxCredit());
						boolean hasEnrollments = !course.getClassEnrollments().isEmpty(); 
						rc.setReadOnly(hasEnrollments);
						rc.setCanDelete(!hasEnrollments);
						rc.setCanWaitList(course.getCourseOffering().getInstructionalOffering().effectiveWaitList());
						if (hasEnrollments)
							rc.setStatus(RequestedCourseStatus.ENROLLED);
						else if (course.getOverrideStatus() != null)
							rc.setStatus(
									course.isRequestApproved() ? RequestedCourseStatus.OVERRIDE_APPROVED :
									course.isRequestRejected() ? RequestedCourseStatus.OVERRIDE_REJECTED :
									course.isRequestCancelled() ? RequestedCourseStatus.OVERRIDE_CANCELLED :
									course.isRequestNeeded() ? RequestedCourseStatus.OVERRIDE_NEEDED :
									RequestedCourseStatus.OVERRIDE_PENDING);
						else
							rc.setStatus(RequestedCourseStatus.SAVED);
						if (hasEnrollments) {
							if (enrolledNoAlternatives) rc.setCanChangeAlternatives(false);
							if (enrolledNoPriority) rc.setCanChangePriority(false);
						}
						if (setReadOnlyWhenReserved) {
							for (Reservation reservation: course.getCourseOffering().getInstructionalOffering().getReservations()) {
								if (reservation instanceof IndividualReservation || reservation instanceof StudentGroupReservation || reservation instanceof LearningCommunityReservation) {
									if (reservation.isMustBeUsed() && !reservation.isExpired() && reservation.isApplicable(student, course)) {
										rc.setReadOnly(true);
										rc.setCanDelete(false);
										if (reservedNoAlternatives) rc.setCanChangeAlternatives(false);
										if (reservedNoPriority) rc.setCanChangePriority(false);
										break;
									}
								}
							}
						}
						rc.setOverrideExternalId(course.getOverrideExternalId());
						rc.setOverrideTimeStamp(course.getOverrideTimeStamp());
						if (course.getPreferences() != null)
							for (StudentSectioningPref ssp: course.getPreferences()) {
								if (ssp instanceof StudentClassPref) {
									StudentClassPref scp = (StudentClassPref)ssp;
									rc.setSelectedClass(scp.getClazz().getUniqueId(), scp.getClazz().getClassPrefLabel(course.getCourseOffering()), scp.isRequired(), true);
								} else if (ssp instanceof StudentInstrMthPref) {
									StudentInstrMthPref imp = (StudentInstrMthPref)ssp;
									rc.setSelectedIntructionalMethod(imp.getInstructionalMethod().getUniqueId(), imp.getInstructionalMethod().getLabel(), imp.isRequired(), true);
								}
							}
						r.addRequestedCourse(rc);
					}
					if (r.hasRequestedCourse()) {
						if (cd.isAlternative())
							request.getAlternatives().add(r);
						else
							request.getCourses().add(r);
					}
					r.setWaitList(cd.effectiveWaitList());
					r.setNoSub(cd.effectiveNoSub());
					if (cd.getCriticalOverride() != null)
						r.setCritical(cd.getCriticalOverride());
					else
						r.setCritical(cd.getCritical());
					r.setTimeStamp(cd.getTimestamp());
					r.setWaitListedTimeStamp(cd.getWaitlistedTimeStamp());
					lastRequest = r;
					lastRequestPriority = cd.getPriority();
				}
			}
		}
		if (!student.getClassEnrollments().isEmpty()) {
			TreeSet<CourseOffering> courses = new TreeSet<CourseOffering>();
			for (Iterator<StudentClassEnrollment> i = student.getClassEnrollments().iterator(); i.hasNext(); ) {
				StudentClassEnrollment enrl = i.next();
				if (courseIds.contains(enrl.getCourseOffering().getUniqueId())) continue;
				courses.add(enrl.getCourseOffering());
			}
			for (CourseOffering c: courses) {
				CourseRequestInterface.Request r = new CourseRequestInterface.Request();
				RequestedCourse rc = new RequestedCourse();
				rc.setCourseId(c.getUniqueId());
				rc.setCourseName(c.getSubjectAreaAbbv() + " " + c.getCourseNbr() + (!CONSTANTS.showCourseTitle() ? "" : " - " + c.getTitle()));
				rc.setCourseTitle(c.getTitle());
				rc.setCanWaitList(c.getInstructionalOffering().effectiveWaitList());
				CourseCreditUnitConfig credit = c.getCredit(); 
				if (credit != null) rc.setCredit(credit.getMinCredit(), credit.getMaxCredit());
				r.addRequestedCourse(rc);
				request.getCourses().add(r);
				rc.setReadOnly(true); rc.setCanDelete(false); rc.setStatus(RequestedCourseStatus.ENROLLED);
			}
		}
		
		if (CustomCourseRequestsValidationHolder.hasProvider()) {
			OnlineSectioningServer server = getServerInstance(student.getSession().getUniqueId(), true);
			if (server != null) {
				try {
					return server.execute(server.createAction(CustomCourseRequestsValidationHolder.Check.class).withRequest(request), currentUser());
				} catch (SectioningException e) {
					sLog.warn("Failed to validate course requests: " + e.getMessage(), e);
				}
			}
		}
		return request;
	}

	@Override
	public CourseRequestInterface savedRequest(StudentSectioningContext cx) throws SectioningException, PageAccessException {
		checkContext(cx);
		if (cx.getStudentId() == null) throw new SectioningException(MSG.exceptionNoStudent());
		if (!cx.isOnline()) {
			OnlineSectioningServer server = getStudentSolver();
			if (server == null) 
				throw new SectioningException(MSG.exceptionNoSolver());
			return server.execute(server.createAction(GetRequest.class).forStudent(cx.getStudentId(), cx.isSectioning()).withWaitListMode(WaitListMode.NoSubs), currentUser(cx));
		}
		OnlineSectioningServer server = getServerInstance(cx.getSessionId() == null ? canEnroll(cx) : cx.getSessionId(), false);
		EligibilityCheck last = getLastEligibilityCheck(cx);
		boolean includeAdvisorRequests = (cx.isSectioning() && ApplicationProperty.AdvisorCourseRequestsPrepopulateSchedulingAssistant.isTrue()) ||
					(!cx.isSectioning() && last != null && last.hasFlag(EligibilityFlag.CAN_REGISTER) && ApplicationProperty.AdvisorCourseRequestsPrepopulateCourseRequests.isTrue());
		if (server != null) {
			return server.execute(server.createAction(GetRequest.class).forStudent(cx.getStudentId(), cx.isSectioning())
					.withCustomValidation(!cx.isSectioning())
					.withWaitListValidation(cx.isSectioning())
					.withCourseMatcher(getCourseMatcher(cx, server))
					.withAdvisorRequests(includeAdvisorRequests), currentUser(cx));
		} else {
			org.hibernate.Session hibSession = StudentDAO.getInstance().getSession();
			try {
				Student student = StudentDAO.getInstance().get(cx.getStudentId(), hibSession);
				if (student == null) throw new SectioningException(MSG.exceptionBadStudentId());
				CourseRequestInterface request = getRequest(student);
				
				if (request.isEmpty() && CustomCourseRequestsHolder.hasProvider()) {
					OnlineSectioningHelper helper = new OnlineSectioningHelper(hibSession, currentUser(cx));
					CourseRequestInterface r = CustomCourseRequestsHolder.getProvider().getCourseRequests(getServerInstance(cx.getSessionId(), true), helper,
							new XStudentId(student, helper), getCourseMatcher(cx, server));
					if (r != null && !r.isEmpty()) return r;
				}
				
				if (includeAdvisorRequests && student.getLastChangedByStudent() == null) {
					if (request.applyAdvisorRequests(AdvisorGetCourseRequests.getRequest(student, hibSession)))
						request.setPopupMessage(ApplicationProperty.PopupMessageCourseRequestsPrepopulatedWithAdvisorRecommendations.value());
				}
				
				return request;
			} catch (PageAccessException e) {
				throw e;
			} catch (SectioningException e) {
				throw e;
			} catch (Exception e) {
				sLog.error(e.getMessage(), e);
				throw new SectioningException(MSG.exceptionUnknown(e.getMessage()), e);
			} finally {
				hibSession.close();
			}
		}		
	}

	@Override
	public ClassAssignmentInterface savedResult(StudentSectioningContext cx) throws SectioningException, PageAccessException {
		checkContext(cx);
		if (cx.isOnline()) {
			OnlineSectioningServer server = getServerInstance(cx.getSessionId() == null ? canEnroll(cx) : cx.getSessionId(), false);
			ClassAssignmentInterface ret = server.execute(server.createAction(GetAssignment.class).forStudent(cx.getStudentId()), currentUser(cx));
			
			EligibilityCheck last = getLastEligibilityCheck(cx);
			if (ret != null && last != null && last.hasGradeModes())
				for (CourseAssignment ca: ret.getCourseAssignments())
					for (ClassAssignment a: ca.getClassAssignments()) {
						GradeMode m = last.getGradeMode(a);
						if (m != null) a.setGradeMode(m);
					}
			if (ret != null && last != null && last.hasCreditHours())
				for (CourseAssignment ca: ret.getCourseAssignments())
					for (ClassAssignment a: ca.getClassAssignments()) {
						Float credit = last.getCreditHour(a);
						a.setCreditHour(credit);
						if (credit != null) a.setCredit(FixedCreditUnitConfig.formatCredit(credit));
					}
			
			return ret;
		} else {
			OnlineSectioningServer server = getStudentSolver();
			if (server == null) 
				throw new SectioningException(MSG.exceptionNoSolver());
			
			ClassAssignmentInterface ret = server.execute(server.createAction(GetAssignment.class).forStudent(cx.getStudentId()).withWaitListMode(WaitListMode.NoSubs), currentUser(cx));
			if (ret != null)
				ret.setCanEnroll(cx.getStudentId() != null);
			
			return ret;
		}
	}
	
	@Override
	public Boolean selectSession(Long sessionId) {
		getSessionContext().setAttribute(SessionAttribute.OnlineSchedulingLastSession, sessionId);
		UserContext user = getSessionContext().getUser();
		if (user != null && user.getCurrentAuthority() != null) {
			List<? extends UserAuthority> authorities = user.getAuthorities(user.getCurrentAuthority().getRole(), new SimpleQualifier("Session", sessionId));
			if (!authorities.isEmpty()) user.setCurrentAuthority(authorities.get(0));
			else user.setCurrentAuthority(null);
		}
		return true;
	}
	
	private StudentStatusInfo toStudentStatusInfo(StudentSectioningStatus status, List<CourseType> types, boolean admin, boolean advisor) {
		StudentStatusInfo info = new StudentStatusInfo();
		info.setUniqueId(status.getUniqueId());
		info.setReference(status.getReference());
		info.setLabel(status.getLabel());
		info.setCanAccessAssistantPage(status.hasOption(StudentSectioningStatus.Option.enabled));
		info.setCanAccessRequestsPage(status.hasOption(StudentSectioningStatus.Option.regenabled));
		info.setCanStudentEnroll(status.hasOption(StudentSectioningStatus.Option.enrollment));
		info.setCanStudentRegister(status.hasOption(StudentSectioningStatus.Option.registration));
		info.setCanAdvisorEnroll(status.hasOption(StudentSectioningStatus.Option.advisor));
		info.setCanAdvisorRegister(status.hasOption(StudentSectioningStatus.Option.regadvisor));
		info.setCanAdminEnroll(status.hasOption(StudentSectioningStatus.Option.admin));
		info.setCanAdminRegister(status.hasOption(StudentSectioningStatus.Option.regadmin));
		info.setWaitList(status.hasOption(StudentSectioningStatus.Option.waitlist));
		info.setNoSubs(status.hasOption(StudentSectioningStatus.Option.nosubs));
		info.setCanRequire(status.hasOption(StudentSectioningStatus.Option.canreq));
		info.setEmail(status.hasOption(StudentSectioningStatus.Option.email));
		info.setNoSchedule(status.hasOption(StudentSectioningStatus.Option.noschedule));
		info.setMessage(status.getMessage());
		if (status.getFallBackStatus() != null)
			info.setFallback(status.getFallBackStatus().getLabel());
		if (!status.hasOption(Option.notype)) { // all but
			Set<String> prohibited = new TreeSet<String>();
			for (CourseType type: types)
				if (status.getTypes() == null || !status.getTypes().contains(type))
					prohibited.add(type.getReference());
			if (!prohibited.isEmpty()) {
				String refs = "";
				for (String ref: prohibited)
					refs += (refs.isEmpty() ? "" : ", ") + ref; 
				info.setCourseTypes(MSG.courseTypesAllBut(refs));
			}
		} else {
			Set<String> allowed = new TreeSet<String>();
			for (CourseType type: status.getTypes())
				allowed.add(type.getReference());
			if (allowed.isEmpty()) {
				info.setCourseTypes(MSG.courseTypesNoneAllowed());
			} else {
				String refs = "";
				for (String ref: allowed)
					refs += (refs.isEmpty() ? "" : ", ") + ref;
				info.setCourseTypes(MSG.courseTypesAllowed(refs));
			}
		}
		if (status.getEffectiveStartDate() != null || status.getEffectiveStartPeriod() != null) {
			if (status.getEffectiveStartDate() == null)
				info.setEffectiveStart(Constants.slot2str(status.getEffectiveStartPeriod()));
			else if (status.getEffectiveStartPeriod() == null)
				info.setEffectiveStart(Formats.getDateFormat(Formats.Pattern.DATE_EVENT).format(status.getEffectiveStartDate()));
			else
				info.setEffectiveStart(Formats.getDateFormat(Formats.Pattern.DATE_EVENT).format(status.getEffectiveStartDate()) + " " + Constants.slot2str(status.getEffectiveStartPeriod()));
		}
		if (status.getEffectiveStopDate() != null || status.getEffectiveStopPeriod() != null) {
			if (status.getEffectiveStopDate() == null)
				info.setEffectiveStop(Constants.slot2str(status.getEffectiveStopPeriod()));
			else if (status.getEffectiveStopPeriod() == null)
				info.setEffectiveStop(Formats.getDateFormat(Formats.Pattern.DATE_EVENT).format(status.getEffectiveStopDate()));
			else
				info.setEffectiveStop(Formats.getDateFormat(Formats.Pattern.DATE_EVENT).format(status.getEffectiveStopDate()) + " " + Constants.slot2str(status.getEffectiveStopPeriod()));
		}
		if (getSessionContext().hasPermission(Right.SchedulingAssistant)) {
			info.setCanUseAssistant(
					StudentSectioningStatus.hasEffectiveOption(status, null, StudentSectioningStatus.Option.enabled)
					|| (admin && StudentSectioningStatus.hasEffectiveOption(status, null, StudentSectioningStatus.Option.admin))
					|| (advisor && StudentSectioningStatus.hasEffectiveOption(status, null, StudentSectioningStatus.Option.advisor)));
		}
		if (getSessionContext().hasPermission(Right.CourseRequests)) {
			info.setCanRegister(
					StudentSectioningStatus.hasEffectiveOption(status, null, StudentSectioningStatus.Option.regenabled)
					|| (admin && StudentSectioningStatus.hasEffectiveOption(status, null, StudentSectioningStatus.Option.regadmin))
					|| (advisor && StudentSectioningStatus.hasEffectiveOption(status, null, StudentSectioningStatus.Option.regadvisor)));
		}
		return info;
	}

	@Override
	public List<StudentStatusInfo> lookupStudentSectioningStates() throws SectioningException, PageAccessException {
		List<CourseType> courseTypes = CourseTypeDAO.getInstance().getSession().createQuery(
				"select distinct t from CourseOffering c inner join c.courseType t where c.instructionalOffering.session = :sessionId order by t.reference"
				).setLong("sessionId", getStatusPageSessionId()).setCacheable(true).list();
		List<StudentStatusInfo> ret = new ArrayList<StudentStatusInfo>();
		boolean advisor = getSessionContext().hasPermissionAnySession(getStatusPageSessionId(), Right.StudentSchedulingAdvisor);
		boolean admin = getSessionContext().hasPermissionAnySession(getStatusPageSessionId(), Right.StudentSchedulingAdmin);
		boolean email = true;
		boolean waitlist = CustomStudentEnrollmentHolder.isAllowWaitListing();
		boolean specreg = CustomSpecialRegistrationHolder.hasProvider();
		boolean reqval = CustomCourseRequestsValidationHolder.hasProvider();
		if (admin) {
			Session session = SessionDAO.getInstance().get(getStatusPageSessionId());
			StudentStatusInfo info = null;
			if (session.getDefaultSectioningStatus() != null) {
				StudentSectioningStatus s = session.getDefaultSectioningStatus();
				info = toStudentStatusInfo(s, courseTypes, admin, advisor);
				info.setUniqueId(null);
				info.setReference("");
				info.setLabel(MSG.studentStatusSessionDefault(session.getDefaultSectioningStatus().getLabel()));
				info.setEmail(email && s.hasOption(StudentSectioningStatus.Option.email));
				info.setWaitList(waitlist && s.hasOption(StudentSectioningStatus.Option.waitlist));
				info.setSpecialRegistration(specreg && s.hasOption(StudentSectioningStatus.Option.specreg));
				info.setRequestValiadtion(reqval && s.hasOption(StudentSectioningStatus.Option.reqval));
			} else {
				info = new StudentStatusInfo();
				info.setReference("");
				info.setLabel(MSG.studentStatusSystemDefault());
				info.setAllEnabled();
				info.setEmail(email);
				info.setWaitList(waitlist);
				info.setSpecialRegistration(specreg);
				info.setRequestValiadtion(reqval);
			}
			ret.add(info);
		}
		for (StudentSectioningStatus s: StudentSectioningStatus.findAll(getStatusPageSessionId())) {
			if (s.isPast()) continue;
			if (advisor && !admin && !s.hasOption(StudentSectioningStatus.Option.advcanset)) continue;
			StudentStatusInfo info = toStudentStatusInfo(s, courseTypes, admin, advisor);
			info.setEmail(email && s.hasOption(StudentSectioningStatus.Option.email));
			info.setWaitList(waitlist && s.hasOption(StudentSectioningStatus.Option.waitlist));
			info.setSpecialRegistration(specreg && s.hasOption(StudentSectioningStatus.Option.specreg));
			info.setRequestValiadtion(reqval && s.hasOption(StudentSectioningStatus.Option.reqval));
			ret.add(info);
		}
		return ret;
	}

	@Override
	public Boolean sendEmail(Long sessionId, Long studentId, String subject, String message, String cc, Boolean courseRequests, Boolean classSchedule, Boolean advisorRequests, Boolean optional) throws SectioningException, PageAccessException {
		try {
			if (sessionId == null) sessionId = getStatusPageSessionId();
			
			OnlineSectioningServer server = getServerInstance(sessionId, true);
			if (server == null) throw new SectioningException(MSG.exceptionNoServerForSession());
			getSessionContext().checkPermissionAnySession(server.getAcademicSession(), Right.StudentSchedulingEmailStudent);
			
			if (!getSessionContext().hasPermissionAnySession(sessionId, Right.StudentSchedulingAdmin) &&
					getSessionContext().hasPermissionAnySession(sessionId, Right.StudentSchedulingAdvisor) &&
					!getSessionContext().hasPermission(Right.StudentSchedulingAdvisorCanModifyAllStudents)) {
					getSessionContext().checkPermission(Right.StudentSchedulingAdvisorCanModifyMyStudents);
					Set<Long> myStudentIds = getMyStudents(sessionId);
					if (!myStudentIds.contains(studentId)) {
						Student student = StudentDAO.getInstance().get(studentId);
						throw new PageAccessException(SEC_MSG.permissionCheckFailed(Right.StudentSchedulingEmailStudent.toString(),
								(student == null ? studentId.toString() : student.getName(NameFormat.LAST_FIRST_MIDDLE.reference()))));
					}
				}
			
			StudentEmail email = server.createAction(StudentEmail.class).forStudent(studentId);
			if (courseRequests != null && classSchedule != null && advisorRequests != null) {
				email.overridePermissions(courseRequests, classSchedule, advisorRequests);
				if (advisorRequests && !courseRequests && !classSchedule)
					email.includeAdvisorRequestsPDF();
			}
			email.setCC(cc);
			email.setEmailSubject(subject == null || subject.isEmpty() ?
					(classSchedule ? MSG.defaulSubject() : courseRequests ? MSG.defaulSubjectCourseRequests() : advisorRequests ? MSG.defaulSubjectAdvisorRequests() : MSG.defaulSubjectOther())
					: subject);
			email.setOptional(optional);
			email.setMessage(message);
			return server.execute(email, currentUser());
		} catch (PageAccessException e) {
			throw e;
		} catch (SectioningException e) {
			throw e;
		} catch (Exception e) {
			sLog.error(e.getMessage(), e);
			throw new SectioningException(MSG.exceptionUnknown(e.getMessage()), e);
		}
	}

	@Override
	public Boolean changeStatus(List<Long> studentIds, String note, String ref) throws SectioningException, PageAccessException {
		try {
			OnlineSectioningServer server = getServerInstance(getStatusPageSessionId(), true);
			if (server == null) throw new SectioningException(MSG.exceptionNoServerForSession());
			getSessionContext().checkPermission(server.getAcademicSession(), Right.StudentSchedulingChangeStudentStatus);
			if (!getSessionContext().hasPermissionAnySession(getStatusPageSessionId(), Right.StudentSchedulingAdmin) &&
				getSessionContext().hasPermissionAnySession(getStatusPageSessionId(), Right.StudentSchedulingAdvisor) &&
				!getSessionContext().hasPermission(Right.StudentSchedulingAdvisorCanModifyAllStudents)) {
				getSessionContext().checkPermission(Right.StudentSchedulingAdvisorCanModifyMyStudents);
				Set<Long> myStudentIds = getMyStudents(getStatusPageSessionId());
				for (Long studentId: studentIds) {
					if (!myStudentIds.contains(studentId)) {
						Student student = StudentDAO.getInstance().get(studentId);
						throw new PageAccessException(SEC_MSG.permissionCheckFailed(Right.StudentSchedulingChangeStudentStatus.toString(),
								(student == null ? studentId.toString() : student.getName(NameFormat.LAST_FIRST_MIDDLE.reference()))));
					}
				}
			}
			Boolean ret = server.execute(server.createAction(ChangeStudentStatus.class).forStudents(studentIds).withStatus(ref).withNote(note), currentUser());
			try {
		        SessionFactory hibSessionFactory = SessionDAO.getInstance().getSession().getSessionFactory();
		        for (Long studentId: studentIds)
		        	hibSessionFactory.getCache().evictEntity(Student.class, studentId);
	        } catch (Exception e) {
	        	sLog.warn("Failed to evict cache: " + e.getMessage());
	        }
			return ret;
		} catch (PageAccessException e) {
			throw e;
		} catch (SectioningException e) {
			throw e;
		} catch (Exception e) {
			sLog.error(e.getMessage(), e);
			throw new SectioningException(MSG.exceptionUnknown(e.getMessage()), e);
		}
	}
	
	@Override
	public Boolean changeStudentGroup(List<Long> studentIds, Long groupId, boolean remove) throws SectioningException, PageAccessException {
		try {
			OnlineSectioningServer server = getServerInstance(getStatusPageSessionId(), true);
			if (server == null) throw new SectioningException(MSG.exceptionNoServerForSession());
			getSessionContext().checkPermission(server.getAcademicSession(), Right.StudentSchedulingChangeStudentGroup);
			if (!getSessionContext().hasPermissionAnySession(getStatusPageSessionId(), Right.StudentSchedulingAdmin) &&
				getSessionContext().hasPermissionAnySession(getStatusPageSessionId(), Right.StudentSchedulingAdvisor) &&
				!getSessionContext().hasPermission(Right.StudentSchedulingAdvisorCanModifyAllStudents)) {
				getSessionContext().checkPermission(Right.StudentSchedulingAdvisorCanModifyMyStudents);
				Set<Long> myStudentIds = getMyStudents(getStatusPageSessionId());
				for (Long studentId: studentIds) {
					if (!myStudentIds.contains(studentId)) {
						Student student = StudentDAO.getInstance().get(studentId);
						throw new PageAccessException(SEC_MSG.permissionCheckFailed(Right.StudentSchedulingChangeStudentGroup.toString(),
								(student == null ? studentId.toString() : student.getName(NameFormat.LAST_FIRST_MIDDLE.reference()))));
					}
				}
			}
			return server.execute(server.createAction(ChangeStudentGroup.class).forStudents(studentIds).withGroup(groupId, remove), currentUser());
		} catch (PageAccessException e) {
			throw e;
		} catch (SectioningException e) {
			throw e;
		} catch (Exception e) {
			sLog.error(e.getMessage(), e);
			throw new SectioningException(MSG.exceptionUnknown(e.getMessage()), e);
		}

	}
	
	private OnlineSectioningLog.Entity currentUser() {
		return currentUser(null);
	}
	
	private OnlineSectioningLog.Entity currentUser(StudentSectioningContext cx) {
		UserContext user = getSessionContext().getUser();
		UniTimePrincipal principal = (UniTimePrincipal)getSessionContext().getAttribute(SessionAttribute.OnlineSchedulingUser);
		if (user != null) {
			OnlineSectioningLog.Entity.Builder entity = OnlineSectioningLog.Entity.newBuilder()
					.setExternalId(user.getTrueExternalUserId())
					.setName(user.getTrueName() == null ? user.getUsername() : user.getTrueName())
					.setType(user instanceof Chameleon || principal != null ?
							OnlineSectioningLog.Entity.EntityType.MANAGER : OnlineSectioningLog.Entity.EntityType.STUDENT);
			if (cx != null && cx.hasPin())
				entity.addParameterBuilder().setKey("pin").setValue(cx.getPin());
			if (principal != null && principal.getStudentExternalId() != null) entity.addParameterBuilder().setKey("student").setValue(principal.getStudentExternalId());
			return entity.build();
		} else if (principal != null) {
			OnlineSectioningLog.Entity.Builder entity = OnlineSectioningLog.Entity.newBuilder()
					.setExternalId(principal.getExternalId())
					.setName(principal.getName())
					.setType(OnlineSectioningLog.Entity.EntityType.MANAGER);
			if (cx != null && cx.hasPin())
				entity.addParameterBuilder().setKey("pin").setValue(cx.getPin());
			return entity.build();
		} else {
			return null;
		}
		
	}
	
	@Override
	public List<SectioningAction> changeLog(String query) throws SectioningException, PageAccessException {
		Long sessionId = getStatusPageSessionId();
		OnlineSectioningServer server = getServerInstance(sessionId, true);
		if (server == null) throw new SectioningException(MSG.exceptionNoServerForSession());
		if (server instanceof DatabaseServer)
			return server.execute(server.createAction(DbFindOnlineSectioningLogAction.class).forQuery(query, sessionContext.hasPermission(Right.EnrollmentsShowExternalId)), currentUser());
		return server.execute(server.createAction(FindOnlineSectioningLogAction.class).forQuery(query, sessionContext.hasPermission(Right.EnrollmentsShowExternalId)), currentUser());
	}

	@Override
	public Boolean massCancel(List<Long> studentIds, String statusRef, String subject, String message, String cc) throws SectioningException, PageAccessException {
		try {
			OnlineSectioningServer server = getServerInstance(getStatusPageSessionId(), false);
			if (server == null) throw new SectioningException(MSG.exceptionNoServerForSession());
			
			getSessionContext().checkPermission(server.getAcademicSession(), Right.StudentSchedulingMassCancel);
			
			if (!getSessionContext().hasPermissionAnySession(getStatusPageSessionId(), Right.StudentSchedulingAdmin) &&
					getSessionContext().hasPermissionAnySession(getStatusPageSessionId(), Right.StudentSchedulingAdvisor) &&
					!getSessionContext().hasPermission(Right.StudentSchedulingAdvisorCanModifyAllStudents)) {
					getSessionContext().checkPermission(Right.StudentSchedulingAdvisorCanModifyMyStudents);
					Set<Long> myStudentIds = getMyStudents(getStatusPageSessionId());
					for (Long studentId: studentIds) {
						if (!myStudentIds.contains(studentId)) {
							Student student = StudentDAO.getInstance().get(studentId);
							throw new PageAccessException(SEC_MSG.permissionCheckFailed(Right.StudentSchedulingMassCancel.toString(),
									(student == null ? studentId.toString() : student.getName(NameFormat.LAST_FIRST_MIDDLE.reference()))));
						}
					}
				}
			
			return server.execute(server.createAction(MassCancelAction.class).forStudents(studentIds).withStatus(statusRef).withEmail(subject, message, cc), currentUser());
		} catch (PageAccessException e) {
			throw e;
		} catch (SectioningException e) {
			throw e;
		} catch (Exception e) {
			sLog.error(e.getMessage(), e);
			throw new SectioningException(MSG.exceptionUnknown(e.getMessage()), e);
		}
	}

	static class CourseMatcher extends AbstractCourseMatcher {
		private org.unitime.timetable.onlinesectioning.match.CourseMatcher iParent;
		private static final long serialVersionUID = 1L;
		private boolean iAllCourseTypes, iNoCourseType;
		private Set<String> iAllowedCourseTypes;
		private Set<Long> iAllowedCourseIds;
		
		public CourseMatcher(boolean allCourseTypes, boolean noCourseType, Set<String> allowedCourseTypes, Set<Long> allowedCourseIds) {
			iAllCourseTypes = allCourseTypes; iNoCourseType = noCourseType; iAllowedCourseTypes = allowedCourseTypes; iAllowedCourseIds = allowedCourseIds;
		}
		
		public boolean isAllCourseTypes() { return iAllCourseTypes; }
		
		public boolean isNoCourseType() { return iNoCourseType; }
		
		public boolean hasAllowedCourseTypes() { return iAllowedCourseTypes != null && !iAllowedCourseTypes.isEmpty(); }
		
		public Set<String> getAllowedCourseTypes() { return iAllowedCourseTypes; }
		
		public Set<Long> getAllowedCourseIds() { return iAllowedCourseIds; }
		
		public boolean isAllowedCourseId(XCourseId course) {
			return iAllowedCourseIds != null && course != null && iAllowedCourseIds.contains(course.getCourseId());
		}
		
		public org.unitime.timetable.onlinesectioning.match.CourseMatcher getParentCourseMatcher() { return iParent; }
		
		public void setParentCourseMatcher(org.unitime.timetable.onlinesectioning.match.CourseMatcher parent) { iParent = parent; }
		
		@Override
		public void setServer(OnlineSectioningServer server) {
			super.setServer(server);
			if (iParent != null) iParent.setServer(server);
		}

		@Override
		public boolean match(XCourseId course) {
			if (isAllowedCourseId(course)) return true;
			return course != null && course.matchType(iAllCourseTypes, iNoCourseType, iAllowedCourseTypes) && (iParent == null || iParent.match(course));
		}
	}

	@Override
	public EligibilityCheck checkEligibility(StudentSectioningContext cx) throws SectioningException, PageAccessException {
		return checkEligibility(cx, true);
	}
	
	protected void checkContext(StudentSectioningContext cx) {
		if (cx.getSessionId() == null && cx.getStudentId() != null) {
			// guess session from student
			Student student = StudentDAO.getInstance().get(cx.getStudentId());
			if (student != null) {
				cx.setSessionId(student.getSession().getUniqueId());
				sLog.warn("ContextCheck: session " + student.getSession().getLabel() + " guessed from student id " + student.getExternalUniqueId());
			}
		}
		if (cx.getSessionId() == null) {
			// use last used session otherwise
			cx.setSessionId(getLastSessionId());
			sLog.warn("ContextCheck: no session id, using last/current session instead (" + cx.getSessionId() + ")");
		} else {
			setLastSessionId(cx.getSessionId());
		}
		if (cx.getStudentId() == null && cx.getSessionId() != null) {
			cx.setStudentId(getStudentId(cx.getSessionId()));
			sLog.debug("ContextCheck: student id " + cx.getStudentId() + " guessed from session id " + cx.getSessionId());
		}
		if (cx.getStudentId() == null)
			sLog.debug("ContextCheck: no student id (assuming guess access)");
		if (cx.getStudentId() != null && !cx.getStudentId().equals(getStudentId(cx.getSessionId()))) {
			sLog.warn("ContextCheck: different student id, permission check: " + getSessionContext().hasPermissionAnySession(cx.getSessionId(), Right.StudentSchedulingAdvisor));
		}
	}
	
	public EligibilityCheck checkEligibility(StudentSectioningContext cx, boolean includeCustomCheck) throws SectioningException, PageAccessException {
		try {
			checkContext(cx);
			if (includeCustomCheck) getSessionContext().removeAttribute(SessionAttribute.OnlineSchedulingEligibility);
			
			if (!cx.isOnline()) {
				StudentSolverProxy server = getStudentSolver();
				if (server == null) 
					return new EligibilityCheck(MSG.exceptionNoSolver());
				
				EligibilityCheck check = new EligibilityCheck();
				check.setSessionId(server.getAcademicSession().getUniqueId());
				check.setStudentId(cx.getStudentId());
				check.setFlag(EligibilityFlag.CAN_USE_ASSISTANT, true);
				check.setFlag(EligibilityFlag.CAN_ENROLL, !server.isPublished());
				check.setFlag(EligibilityFlag.CAN_WAITLIST, false);
				check.setFlag(EligibilityFlag.CAN_NO_SUBS, true);
				check.setFlag(EligibilityFlag.CAN_RESET, ApplicationProperty.SolverDashboardAllowScheduleReset.isTrue());
				check.setFlag(EligibilityFlag.CONFIRM_DROP, ApplicationProperty.OnlineSchedulingConfirmCourseDrop.isTrue());
				check.setFlag(EligibilityFlag.QUICK_ADD_DROP, ApplicationProperty.OnlineSchedulingQuickAddDrop.isTrue());
				check.setFlag(EligibilityFlag.ALTERNATIVES_DROP, ApplicationProperty.OnlineSchedulingAlternativesDrop.isTrue());
				check.setFlag(EligibilityFlag.GWT_CONFIRMATIONS, ApplicationProperty.OnlineSchedulingGWTConfirmations.isTrue());
				check.setFlag(EligibilityFlag.DEGREE_PLANS, CustomDegreePlansHolder.hasProvider());
				check.setFlag(EligibilityFlag.NO_REQUEST_ARROWS, ApplicationProperty.OnlineSchedulingNoRequestArrows.isTrue());
				check.setFlag(EligibilityFlag.CAN_REQUIRE, true);
				return check;
			}
			
			if (cx.getSessionId() == null) return new EligibilityCheck(MSG.exceptionNoAcademicSession());
			
			UserContext user = getSessionContext().getUser();
			if (user == null)
				return new EligibilityCheck(getSessionContext().isHttpSessionNew() ? MSG.exceptionHttpSessionExpired() : MSG.exceptionLoginRequired());

			EligibilityCheck check = new EligibilityCheck();
			check.setFlag(EligibilityFlag.IS_ADMIN, getSessionContext().hasPermissionAnySession(cx.getSessionId(), Right.StudentSchedulingAdmin));
			check.setFlag(EligibilityFlag.IS_ADVISOR, getSessionContext().hasPermissionAnySession(cx.getSessionId(), Right.StudentSchedulingAdvisor));
			check.setFlag(EligibilityFlag.IS_GUEST, user instanceof AnonymousUserContext);
			check.setFlag(EligibilityFlag.CAN_RESET, ApplicationProperty.OnlineSchedulingAllowScheduleReset.isTrue());
			if (!check.hasFlag(EligibilityFlag.CAN_RESET) && (check.hasFlag(EligibilityFlag.IS_ADMIN) || check.hasFlag(EligibilityFlag.IS_ADVISOR)))
				check.setFlag(EligibilityFlag.CAN_RESET, ApplicationProperty.OnlineSchedulingAllowScheduleResetIfAdmin.isTrue());
			check.setFlag(EligibilityFlag.CONFIRM_DROP, ApplicationProperty.OnlineSchedulingConfirmCourseDrop.isTrue());
			check.setFlag(EligibilityFlag.QUICK_ADD_DROP, ApplicationProperty.OnlineSchedulingQuickAddDrop.isTrue());
			check.setFlag(EligibilityFlag.ALTERNATIVES_DROP, ApplicationProperty.OnlineSchedulingAlternativesDrop.isTrue());
			check.setFlag(EligibilityFlag.GWT_CONFIRMATIONS, ApplicationProperty.OnlineSchedulingGWTConfirmations.isTrue());
			check.setFlag(EligibilityFlag.DEGREE_PLANS, CustomDegreePlansHolder.hasProvider());
			check.setFlag(EligibilityFlag.NO_REQUEST_ARROWS, ApplicationProperty.OnlineSchedulingNoRequestArrows.isTrue());
			check.setSessionId(cx.getSessionId());
			check.setStudentId(cx.getStudentId());
			
			if (!cx.isSectioning()) {
				OnlineSectioningServer server = getServerInstance(cx.getSessionId(), true);
				if (server == null) {
					Student student = (cx.getStudentId() == null ? null : StudentDAO.getInstance().get(cx.getStudentId()));
					if (student == null) {
						if (!check.hasFlag(EligibilityFlag.IS_ADMIN) && !check.hasFlag(EligibilityFlag.IS_ADVISOR))
							check.setMessage(MSG.exceptionEnrollNotStudent(SessionDAO.getInstance().get(cx.getSessionId()).getLabel()));
						return check;
					}
					StudentSectioningStatus status = student.getEffectiveStatus();
					if (status == null) {
						check.setFlag(EligibilityFlag.CAN_USE_ASSISTANT, true);
						check.setFlag(EligibilityFlag.CAN_WAITLIST, true);
					} else {
						check.setFlag(EligibilityFlag.CAN_USE_ASSISTANT,
								status.hasOption(StudentSectioningStatus.Option.regenabled) || 
								(check.hasFlag(EligibilityFlag.IS_ADMIN) && status.hasOption(StudentSectioningStatus.Option.regadmin)) ||
								(check.hasFlag(EligibilityFlag.IS_ADVISOR) && status.hasOption(StudentSectioningStatus.Option.regadvisor)));
						check.setFlag(EligibilityFlag.CAN_WAITLIST, status.hasOption(StudentSectioningStatus.Option.waitlist));
						check.setFlag(EligibilityFlag.CAN_NO_SUBS, status.hasOption(StudentSectioningStatus.Option.nosubs));
						check.setMessage(status.getMessage());
					}
					check.setFlag(EligibilityFlag.CAN_REGISTER, getSessionContext().hasPermissionAnyAuthority(student, Right.StudentSchedulingCanRegister));
					check.setFlag(EligibilityFlag.CAN_REQUIRE, getSessionContext().hasPermissionAnyAuthority(student, Right.StudentSchedulingCanRequirePreferences));
					check.setAdvisorWaitListedCourseIds(student.getAdvisorWaitListedCourseIds(null));
				} else {
					check = server.execute(server.createAction(CourseRequestEligibility.class).forStudent(cx.getStudentId()).withCheck(check).includeCustomCheck(includeCustomCheck)
							.withPermission(
									getSessionContext().hasPermissionAnyAuthority(cx.getStudentId(), "Student", Right.StudentSchedulingCanRegister),
									getSessionContext().hasPermissionAnyAuthority(cx.getStudentId(), "Student", Right.StudentSchedulingCanRequirePreferences)), currentUser(cx));
				}
				if (includeCustomCheck) getSessionContext().setAttribute(SessionAttribute.OnlineSchedulingEligibility, check);
				return check;
			}
			
			OnlineSectioningServer server = getServerInstance(cx.getSessionId(), false);
			if (server == null)
				return new EligibilityCheck(MSG.exceptionNoServerForSession());
			
			EligibilityCheck ret = server.execute(server.createAction(CheckEligibility.class).forStudent(cx.getStudentId()).withCheck(check).includeCustomCheck(includeCustomCheck)
					.withPermission(getSessionContext().hasPermissionAnyAuthority(cx.getStudentId(), "Student", Right.StudentSchedulingCanEnroll),
							getSessionContext().hasPermissionAnyAuthority(cx.getStudentId(), "Student", Right.StudentSchedulingCanRequirePreferences)), currentUser(cx));
			if (includeCustomCheck) getSessionContext().setAttribute(SessionAttribute.OnlineSchedulingEligibility, ret);
			
			return ret;
		} catch (Exception e) {
			sLog.error(e.getMessage(), e);
			return new EligibilityCheck(MSG.exceptionUnknown(e.getMessage() != null && !e.getMessage().isEmpty() ? e.getMessage() : e.getCause() != null ? e.getCause().getClass().getSimpleName() : e.getClass().getSimpleName()));
		}
	}

	@Override
	public void destroy() throws Exception {
		for (Customization customization: Customization.values())
			customization.release();
	}

	@Override
	public SectioningProperties getProperties(Long sessionId) throws SectioningException, PageAccessException {
		SectioningProperties properties = new SectioningProperties();
		properties.setAdmin(getSessionContext().hasPermissionAnySession(sessionId, Right.StudentSchedulingAdmin));
		properties.setAdvisor(getSessionContext().hasPermissionAnySession(sessionId, Right.StudentSchedulingAdvisor));
		if (sessionId == null && getSessionContext().getUser() != null)
			sessionId = getSessionContext().getUser().getCurrentAcademicSessionId();
		properties.setSessionId(sessionId);
		if (sessionId != null) {
			Session session = SessionDAO.getInstance().get(sessionId);
			properties.setChangeLog(properties.isAdmin() && getServerInstance(sessionId, true) != null);
			properties.setMassCancel(getSessionContext().hasPermission(sessionId, Right.StudentSchedulingMassCancel));
			properties.setEmail(getSessionContext().hasPermission(sessionId, Right.StudentSchedulingEmailStudent));
			properties.setChangeStatus(getSessionContext().hasPermission(sessionId, Right.StudentSchedulingChangeStudentStatus));
			properties.setRequestUpdate(getSessionContext().hasPermission(sessionId, Right.StudentSchedulingRequestStudentUpdate));
			properties.setReloadStudent(getSessionContext().hasPermission(sessionId, Right.StudentSchedulingReloadStudent));
			properties.setCheckStudentOverrides(getSessionContext().hasPermission(sessionId, Right.StudentSchedulingCheckStudentOverrides));
			properties.setValidateStudentOverrides(getSessionContext().hasPermission(sessionId, Right.StudentSchedulingValidateStudentOverrides));
			properties.setRecheckCriticalCourses(getSessionContext().hasPermission(sessionId, Right.StudentSchedulingRecheckCriticalCourses));
			properties.setAdvisorCourseRequests(getSessionContext().hasPermission(sessionId, Right.AdvisorCourseRequests) && session != null && 
					(session.getStatusType().canPreRegisterStudents() || session.getStatusType().canOnlineSectionStudents()));
			if (properties.isEmail() && Customization.StudentEmailProvider.hasProvider()) {
				StudentEmailProvider email = Customization.StudentEmailProvider.getProvider();
				properties.setEmailOptionalToggleCaption(email.getToggleCaptionIfOptional());
				properties.setEmailOptionalToggleDefault(email.isOptionCheckedByDefault());
			}
			if (getSessionContext().hasPermission(sessionId, Right.StudentSchedulingChangeStudentGroup))
				for (StudentGroup g: (List<StudentGroup>)StudentGroupDAO.getInstance().getSession().createQuery(
						"from StudentGroup g where g.type.advisorsCanSet = true and g.session = :sessionId order by g.groupAbbreviation"
						).setLong("sessionId", sessionId).setCacheable(true).list()) {
					properties.addEditableGroup(new StudentGroupInfo(g.getUniqueId(), g.getGroupAbbreviation(), g.getGroupName(), g.getType() == null ? null: g.getType().getReference()));
				}
		}
		return properties;
	}

	@Override
	public Boolean requestStudentUpdate(List<Long> studentIds) throws SectioningException, PageAccessException {
		OnlineSectioningServer server = getServerInstance(getStatusPageSessionId(), true);
		if (server == null) throw new SectioningException(MSG.exceptionNoServerForSession());
		
		getSessionContext().checkPermission(server.getAcademicSession(), Right.StudentSchedulingRequestStudentUpdate);
		
		return server.execute(server.createAction(RequestStudentUpdates.class).forStudents(studentIds), currentUser());
	}
	
	@Override
	public Boolean reloadStudent(List<Long> studentIds) throws SectioningException, PageAccessException {
		OnlineSectioningServer server = getServerInstance(getStatusPageSessionId(), false);
		if (server == null) throw new SectioningException(MSG.exceptionNoServerForSession());
		
		getSessionContext().checkPermission(server.getAcademicSession(), Right.StudentSchedulingReloadStudent);
		
		return server.execute(server.createAction(ReloadStudent.class).forStudents(studentIds), currentUser());
	}
	
	@Override
	public Boolean checkStudentOverrides(List<Long> studentIds) throws SectioningException, PageAccessException {
		OnlineSectioningServer server = getServerInstance(getStatusPageSessionId(), true);
		if (server == null) throw new SectioningException(MSG.exceptionNoServerForSession());
		
		getSessionContext().checkPermission(server.getAcademicSession(), Right.StudentSchedulingCheckStudentOverrides);
		
		Boolean r1 = server.execute(server.createAction(CustomCourseRequestsValidationHolder.Update.class).forStudents(studentIds), currentUser());
		
		Boolean r2 = server.execute(server.createAction(CustomWaitListValidationHolder.Update.class).forStudents(studentIds), currentUser());
		
		return r1 || r2;
	}
	
	@Override
	public Boolean validateStudentOverrides(List<Long> studentIds) throws SectioningException, PageAccessException {
		OnlineSectioningServer server = getServerInstance(getStatusPageSessionId(), true);
		if (server == null) throw new SectioningException(MSG.exceptionNoServerForSession());
		
		getSessionContext().checkPermission(server.getAcademicSession(), Right.StudentSchedulingValidateStudentOverrides);
		
		Boolean r1 = server.execute(server.createAction(CustomCourseRequestsValidationHolder.Validate.class).forStudents(studentIds), currentUser());
		
		Boolean r2 = server.execute(server.createAction(CustomWaitListValidationHolder.Validate.class).forStudents(studentIds), currentUser());
		
		return r1 || r2;
	}
	
	@Override
	public Boolean recheckCriticalCourses(List<Long> studentIds) throws SectioningException, PageAccessException {
		OnlineSectioningServer server = getServerInstance(getStatusPageSessionId(), true);
		if (server == null) throw new SectioningException(MSG.exceptionNoServerForSession());
		
		getSessionContext().checkPermission(server.getAcademicSession(), Right.StudentSchedulingRecheckCriticalCourses);
		
		return server.execute(server.createAction(CustomCriticalCoursesHolder.CheckCriticalCourses.class).forStudents(studentIds), currentUser());
	}

	@Override
	public List<DegreePlanInterface> listDegreePlans(StudentSectioningContext cx) throws SectioningException, PageAccessException {
		checkContext(cx);
		if (cx.getStudentId() == null)
			throw new PageAccessException(MSG.exceptionNoStudent());
		if (!cx.getStudentId().equals(getStudentId(cx.getSessionId()))) {
			if (!getSessionContext().hasPermissionAnySession(cx.getSessionId(), Right.AdvisorCourseRequests))
				getSessionContext().checkPermissionAnySession(cx.getSessionId(), Right.StudentSchedulingAdvisor);
		}
		
		OnlineSectioningServer server = null;
		if (!cx.isOnline()) {
			server = getStudentSolver();
			if (server == null) 
				throw new SectioningException(MSG.exceptionNoSolver());
		} else {
			server = getServerInstance(cx.getSessionId(), true);
			if (server == null)
				throw new SectioningException(MSG.exceptionNoServerForSession());
		}
		
		CourseMatcher matcher = getCourseMatcher(cx, server);
		return server.execute(server.createAction(GetDegreePlans.class).forStudent(cx.getStudentId()).withMatcher(matcher), currentUser(cx));
	}

	@Override
	public ClassAssignmentInterface.Student lookupStudent(boolean online, String studentId) throws SectioningException, PageAccessException {
		if (getSessionContext().getUser() == null || getSessionContext().getUser().getCurrentAcademicSessionId() == null) return null;
		Student student = Student.findByExternalId(getSessionContext().getUser().getCurrentAcademicSessionId(), studentId);
		if (student == null) return null;
		getSessionContext().checkPermission(student, Right.StudentEnrollments);
		ClassAssignmentInterface.Student st = new ClassAssignmentInterface.Student();
		st.setId(student.getUniqueId());
		st.setSessionId(getSessionContext().getUser().getCurrentAcademicSessionId());
		st.setExternalId(student.getExternalUniqueId());
		st.setCanShowExternalId(getSessionContext().hasPermission(Right.EnrollmentsShowExternalId));
		st.setCanRegister(getSessionContext().hasPermission(Right.CourseRequests) && getSessionContext().hasPermission(student, Right.StudentSchedulingCanRegister));
		st.setCanUseAssistant(online
				? getSessionContext().hasPermission(Right.SchedulingAssistant) && getSessionContext().hasPermission(student, Right.StudentSchedulingCanEnroll)
				: getStudentSolver() != null);
		st.setName(student.getName(ApplicationProperty.OnlineSchedulingStudentNameFormat.value()));
		StudentSectioningStatus status = student.getEffectiveStatus();
		if (CustomStudentEnrollmentHolder.isAllowWaitListing() && (status == null || status.hasOption(Option.waitlist))) {
			st.setWaitListMode(WaitListMode.WaitList);
		} else if (status != null && status.hasOption(Option.nosubs)) {
			st.setWaitListMode(WaitListMode.NoSubs);
		} else {
			st.setWaitListMode(WaitListMode.None);
		}
		for (StudentAreaClassificationMajor acm: new TreeSet<StudentAreaClassificationMajor>(student.getAreaClasfMajors())) {
			st.addArea(acm.getAcademicArea().getAcademicAreaAbbreviation(), acm.getAcademicArea().getTitle());
			st.addClassification(acm.getAcademicClassification().getCode(), acm.getAcademicClassification().getName());
			st.addMajor(acm.getMajor().getCode(), acm.getMajor().getName());
			st.addConcentration(acm.getConcentration() == null ? null : acm.getConcentration().getCode(), acm.getConcentration() == null ? null : acm.getConcentration().getName());
			st.addDegree(acm.getDegree() == null ? null : acm.getDegree().getReference(), acm.getDegree() == null ? null : acm.getDegree().getLabel());
		}
		for (StudentAreaClassificationMinor acm: new TreeSet<StudentAreaClassificationMinor>(student.getAreaClasfMinors())) {
			st.addMinor(acm.getMinor().getCode(), acm.getMinor().getName());
		}
		for (StudentGroup g: student.getGroups()) {
			if (g.getType() == null)
				st.addGroup(g.getGroupAbbreviation(), g.getGroupName());
			else
				st.addGroup(g.getType().getReference(), g.getGroupAbbreviation(), g.getGroupName());
		}
		for (StudentAccomodation a: student.getAccomodations()) {
			st.addAccommodation(a.getAbbreviation(), a.getName());
		}
		for (Advisor a: student.getAdvisors()) {
			if (a.getLastName() != null)
				st.addAdvisor(NameFormat.fromReference(ApplicationProperty.OnlineSchedulingInstructorNameFormat.value()).format(a));
		}
		return st;
	}

	@Override
	public SubmitSpecialRegistrationResponse submitSpecialRequest(SubmitSpecialRegistrationRequest request) throws SectioningException, PageAccessException {
		checkContext(request);
		if (request.getSessionId() == null) throw new PageAccessException(MSG.exceptionNoAcademicSession());
		if (request.getStudentId() == null) throw new PageAccessException(MSG.exceptionNoStudent());
		getSessionContext().checkPermissionAnyAuthority(request.getStudentId(), "Student", Right.StudentSchedulingCanEnroll);
		
		OnlineSectioningServer server = getServerInstance(request.getSessionId(), false);
		if (server == null) throw new SectioningException(MSG.exceptionNoServerForSession());
		if (!server.getAcademicSession().isSectioningEnabled() || !CustomSpecialRegistrationHolder.hasProvider())
			throw new SectioningException(MSG.exceptionNotSupportedFeature());
		
		return server.execute(server.createAction(SpecialRegistrationSubmit.class).withRequest(request), currentUser());
	}

	@Override
	public SpecialRegistrationEligibilityResponse checkSpecialRequestEligibility(SpecialRegistrationEligibilityRequest request) throws SectioningException, PageAccessException {
		checkContext(request);
		if (request.getSessionId() == null) throw new PageAccessException(MSG.exceptionNoAcademicSession());
		if (request.getStudentId() == null) throw new PageAccessException(MSG.exceptionNoStudent());
		getSessionContext().checkPermissionAnyAuthority(request.getStudentId(), "Student", Right.StudentSchedulingCanEnroll);
		
		OnlineSectioningServer server = getServerInstance(request.getSessionId(), false);
		if (server == null) throw new SectioningException(MSG.exceptionNoServerForSession());
		if (!server.getAcademicSession().isSectioningEnabled() || !CustomSpecialRegistrationHolder.hasProvider())
			throw new SectioningException(MSG.exceptionNotSupportedFeature());
		
		return server.execute(server.createAction(SpecialRegistrationEligibility.class).withRequest(request), currentUser());
	}

	@Override
	public List<RetrieveSpecialRegistrationResponse> retrieveAllSpecialRequests(RetrieveAllSpecialRegistrationsRequest request) throws SectioningException, PageAccessException {
		checkContext(request);
		if (request.getSessionId() == null) throw new PageAccessException(MSG.exceptionNoAcademicSession());
		if (request.getStudentId() == null) throw new PageAccessException(MSG.exceptionNoStudent());
		getSessionContext().checkPermissionAnyAuthority(request.getStudentId(), "Student", Right.StudentSchedulingCanEnroll);
		
		OnlineSectioningServer server = getServerInstance(request.getSessionId(), false);
		if (server == null) throw new SectioningException(MSG.exceptionNoServerForSession());
		if (!server.getAcademicSession().isSectioningEnabled() || !CustomSpecialRegistrationHolder.hasProvider())
			throw new SectioningException(MSG.exceptionNotSupportedFeature());

		return server.execute(server.createAction(SpecialRegistrationRetrieveAll.class).withRequest(request), currentUser());
	}

	@Override
	public org.unitime.timetable.gwt.shared.ClassAssignmentInterface.Student lookupStudent(boolean online, Long studentId) throws SectioningException, PageAccessException {
		if (getSessionContext().getUser() == null || getSessionContext().getUser().getCurrentAcademicSessionId() == null) return null;
		Student student = StudentDAO.getInstance().get(studentId);
		if (student == null) return null;
		getSessionContext().checkPermission(student, Right.StudentEnrollments);
		ClassAssignmentInterface.Student st = new ClassAssignmentInterface.Student();
		st.setId(student.getUniqueId());
		st.setSessionId(getSessionContext().getUser().getCurrentAcademicSessionId());
		st.setExternalId(student.getExternalUniqueId());
		st.setCanShowExternalId(getSessionContext().hasPermission(Right.EnrollmentsShowExternalId));
		st.setCanRegister(getSessionContext().hasPermission(Right.CourseRequests) && getSessionContext().hasPermission(student, Right.StudentSchedulingCanRegister));
		st.setCanUseAssistant(online
				? getSessionContext().hasPermission(Right.SchedulingAssistant) && getSessionContext().hasPermission(student, Right.StudentSchedulingCanEnroll)
				: getStudentSolver() != null);
		st.setName(student.getName(ApplicationProperty.OnlineSchedulingStudentNameFormat.value()));
		StudentSectioningStatus status = student.getEffectiveStatus();
		if (CustomStudentEnrollmentHolder.isAllowWaitListing() && (status == null || status.hasOption(Option.waitlist))) {
			st.setWaitListMode(WaitListMode.WaitList);
		} else if (status != null && status.hasOption(Option.nosubs)) {
			st.setWaitListMode(WaitListMode.NoSubs);
		} else {
			st.setWaitListMode(WaitListMode.None);
		}
		for (StudentAreaClassificationMajor acm: new TreeSet<StudentAreaClassificationMajor>(student.getAreaClasfMajors())) {
			st.addArea(acm.getAcademicArea().getAcademicAreaAbbreviation(), acm.getAcademicArea().getTitle());
			st.addClassification(acm.getAcademicClassification().getCode(), acm.getAcademicClassification().getName());
			st.addMajor(acm.getMajor().getCode(), acm.getMajor().getName());
			st.addConcentration(acm.getConcentration() == null ? null : acm.getConcentration().getCode(), acm.getConcentration() == null ? null : acm.getConcentration().getName());
			st.addDegree(acm.getDegree() == null ? null : acm.getDegree().getReference(), acm.getDegree() == null ? null : acm.getDegree().getLabel());
		}
		for (StudentAreaClassificationMinor acm: new TreeSet<StudentAreaClassificationMinor>(student.getAreaClasfMinors())) {
			st.addMinor(acm.getMinor().getCode(), acm.getMinor().getName());
		}
		for (StudentGroup g: student.getGroups()) {
			if (g.getType() == null)
				st.addGroup(g.getGroupAbbreviation(), g.getGroupName());
			else
				st.addGroup(g.getType().getReference(), g.getGroupAbbreviation(), g.getGroupName());
		}
		for (StudentAccomodation a: student.getAccomodations()) {
			st.addAccommodation(a.getAbbreviation(), a.getName());
		}
		for (Advisor a: student.getAdvisors()) {
			if (a.getLastName() != null)
				st.addAdvisor(NameFormat.fromReference(ApplicationProperty.OnlineSchedulingInstructorNameFormat.value()).format(a));
		}
		st.setCanSelect(
				getSessionContext().hasPermission(student.getSession(), Right.AdvisorCourseRequests) ||
				getSessionContext().hasPermission(student.getSession(), Right.StudentSchedulingEmailStudent));
		return st;
	}

	@Override
	public ClassAssignmentInterface section(CourseRequestInterface request, List<ClassAssignment> currentAssignment, List<ClassAssignment> specialRegistration) throws SectioningException, PageAccessException {
		try {
			checkContext(request);
			OnlineSectioningServer server = getServerInstance(request.getAcademicSessionId(), true);
			if (server == null) throw new SectioningException(MSG.exceptionNoServerForSession());
			ClassAssignmentInterface ret = server.execute(server.createAction(FindAssignmentAction.class).forRequest(request).withAssignment(currentAssignment).withSpecialRegistration(specialRegistration), currentUser(request)).get(0);
			if (ret != null) {
				ret.setCanEnroll(server.getAcademicSession().isSectioningEnabled());
				if (ret.isCanEnroll() && request.getStudentId() == null)
					ret.setCanEnroll(false);
			}
			return ret;
		} catch (PageAccessException e) {
			throw e;
		} catch (SectioningException e) {
			throw e;
		} catch (Exception e) {
			sLog.error(e.getMessage(), e);
			throw new SectioningException(MSG.exceptionSectioningFailed(e.getMessage()), e);
		}
	}

	@Override
	public CancelSpecialRegistrationResponse cancelSpecialRequest(CancelSpecialRegistrationRequest request) throws SectioningException, PageAccessException {
		checkContext(request);
		if (request.getSessionId() == null) throw new PageAccessException(MSG.exceptionNoAcademicSession());
		if (request.getStudentId() == null) throw new PageAccessException(MSG.exceptionNoStudent());
		getSessionContext().checkPermissionAnyAuthority(request.getStudentId(), "Student", Right.StudentSchedulingCanEnroll);
		
		OnlineSectioningServer server = getServerInstance(request.getSessionId(), false);
		if (server == null) throw new SectioningException(MSG.exceptionNoServerForSession());
		if (!server.getAcademicSession().isSectioningEnabled() || !CustomSpecialRegistrationHolder.hasProvider())
			throw new SectioningException(MSG.exceptionNotSupportedFeature());
		
		return server.execute(server.createAction(SpecialRegistrationCancel.class).withRequest(request), currentUser());
	}

	@Override
	public RetrieveAvailableGradeModesResponse retrieveGradeModes(RetrieveAvailableGradeModesRequest request) throws SectioningException, PageAccessException {
		checkContext(request);
		if (request.getSessionId() == null) throw new PageAccessException(MSG.exceptionNoAcademicSession());
		if (request.getStudentId() == null) throw new PageAccessException(MSG.exceptionNoStudent());
		getSessionContext().checkPermissionAnyAuthority(request.getStudentId(), "Student", Right.StudentSchedulingCanEnroll);
		
		OnlineSectioningServer server = getServerInstance(request.getSessionId(), false);
		if (server == null) throw new SectioningException(MSG.exceptionNoServerForSession());
		if (!server.getAcademicSession().isSectioningEnabled() || !CustomSpecialRegistrationHolder.hasProvider())
			throw new SectioningException(MSG.exceptionNotSupportedFeature());

		return server.execute(server.createAction(SpecialRegistrationRetrieveGradeModes.class).withRequest(request), currentUser());
	}

	@Override
	public ChangeGradeModesResponse changeGradeModes(ChangeGradeModesRequest request) throws SectioningException, PageAccessException {
		checkContext(request);
		if (request.getSessionId() == null) throw new PageAccessException(MSG.exceptionNoAcademicSession());
		if (request.getStudentId() == null) throw new PageAccessException(MSG.exceptionNoStudent());
		getSessionContext().checkPermissionAnyAuthority(request.getStudentId(), "Student", Right.StudentSchedulingCanEnroll);
		
		OnlineSectioningServer server = getServerInstance(request.getSessionId(), false);
		if (server == null) throw new SectioningException(MSG.exceptionNoServerForSession());
		if (!server.getAcademicSession().isSectioningEnabled() || !CustomSpecialRegistrationHolder.hasProvider())
			throw new SectioningException(MSG.exceptionNotSupportedFeature());
		
		ChangeGradeModesResponse ret = server.execute(server.createAction(SpecialRegistrationChangeGradeModes.class).withRequest(request), currentUser());
		
		EligibilityCheck last = getLastEligibilityCheck(request);
		if (ret != null && ret.hasGradeModes() && last != null)
			for (Map.Entry<String, GradeMode> e: ret.getGradeModes().toMap().entrySet())
				last.addGradeMode(e.getKey(), e.getValue().getCode(), e.getValue().getLabel(), e.getValue().isHonor());
		if (ret != null && ret.hasCreditHours() && last != null)
			for (Map.Entry<String, Float> e: ret.getGradeModes().getCreditHours().entrySet())
				last.addCreditHour(e.getKey(), e.getValue());
		if (ret != null && ret.hasGradeModes() && last != null)
			last.setCurrentCredit(ret.getGradeModes().getCurrentCredit());
		
		return ret;
	}

	@Override
	public Integer changeCriticalOverride(Long studentId, Long courseId, Integer critical) throws SectioningException, PageAccessException {
		getSessionContext().checkPermission(studentId, Right.StudentSchedulingChangeCriticalOverride);
		
		try {
			
			org.hibernate.Session hibSession = CourseDemandDAO.getInstance().getSession();
			CourseDemand cd = (CourseDemand)hibSession.createQuery(
					"select cr.courseDemand from CourseRequest cr where cr.courseOffering = :courseId and cr.courseDemand.student = :studentId"
					).setLong("studentId", studentId).setLong("courseId", courseId).setMaxResults(1).uniqueResult();
			if (cd == null) return null;
			
			cd.setCriticalOverride(critical);
			hibSession.save(cd);
			hibSession.flush();
			
			OnlineSectioningServer server = getServerInstance(cd.getStudent().getSession().getUniqueId(), false);
			if (server != null)
				server.execute(server.createAction(ReloadStudent.class).forStudents(studentId), currentUser());
			
			return cd.getEffectiveCritical().ordinal();
		} catch (PageAccessException e) {
			throw e;
		} catch (SectioningException e) {
			throw e;
		} catch (Exception e) {
			sLog.error(e.getMessage(), e);
			throw new SectioningException("Failed to update critical status: " + e.getMessage(), e);
		}
	}

	@Override
	public UpdateSpecialRegistrationResponse updateSpecialRequest(UpdateSpecialRegistrationRequest request) throws SectioningException, PageAccessException {
		checkContext(request);
		if (request.getSessionId() == null) throw new PageAccessException(MSG.exceptionNoAcademicSession());
		if (request.getStudentId() == null) throw new PageAccessException(MSG.exceptionNoStudent());
		if (request.isPreReg())
			getSessionContext().checkPermissionOtherAuthority(request.getSessionId(), "Session", Right.CourseRequests, getStudentAuthority(request.getSessionId()));
		else
			getSessionContext().checkPermissionOtherAuthority(request.getSessionId(), "Session", Right.SchedulingAssistant, getStudentAuthority(request.getSessionId()));
		
		OnlineSectioningServer server = getServerInstance(request.getSessionId(), true);
		if (server == null) throw new SectioningException(MSG.exceptionNoServerForSession());

		return server.execute(server.createAction(SpecialRegistrationUpdate.class).withRequest(request), currentUser());
	}
	
	@Override
	public StudentInfo getStudentInfo(Long studentId) throws SectioningException, PageAccessException {
		Student student = StudentDAO.getInstance().get(studentId);
		if (student == null)  throw new SectioningException(MSG.exceptionNoStudent());
		
		getSessionContext().checkPermissionAnySession(student.getSession(), Right.AdvisorCourseRequests);

		Long sessionId = student.getSession().getUniqueId();
		
		StudentInfo ret = new StudentInfo();
		ret.setSessionId(sessionId);
		ret.setStudentId(student.getUniqueId());
		ret.setStudentName(student.getName(NameFormat.LAST_FIRST_MIDDLE.reference()));
		ret.setStudentExternalId(student.getExternalUniqueId());
		ret.setStudentEmail(student.getEmail());
		ret.setSessionName(student.getSession().getLabel());
		
		return ret;
	}
	
	@Override
	public AdvisingStudentDetails getStudentAdvisingDetails(Long sessionId, String studentExternalId) throws SectioningException, PageAccessException {
		SessionDAO.getInstance().getSession().setCacheMode(CacheMode.REFRESH);

		if (sessionId == null)
			sessionId = getLastSessionId();
		getSessionContext().checkPermissionAnySession(sessionId, Right.AdvisorCourseRequests);
		
		Student student = Student.findByExternalId(sessionId, studentExternalId);
		if (student == null)  throw new SectioningException(MSG.exceptionNoStudent());
		
		AdvisingStudentDetails ret = new AdvisingStudentDetails();
		ret.setSessionId(sessionId);
		ret.setStudentId(student.getUniqueId());
		ret.setStudentName(student.getName(NameFormat.LAST_FIRST_MIDDLE.reference()));
		ret.setStudentExternalId(student.getExternalUniqueId());
		ret.setStudentEmail(student.getEmail());
		ret.setSessionName(student.getSession().getLabel());
		
		TimetableManager manager = TimetableManager.findByExternalId(getSessionContext().getUser().getExternalUserId());
		Advisor advisor = Advisor.findByExternalId(getSessionContext().getUser().getExternalUserId(), sessionId);
		if (manager != null && manager.getEmailAddress() != null && !manager.getEmailAddress().isEmpty()) {
			ret.setAdvisorEmail(manager.getEmailAddress());
		} else if (advisor != null && advisor.getEmail() != null && !advisor.getEmail().isEmpty()) {
			ret.setAdvisorEmail(advisor.getEmail());
		} else {
			String email = null;
			for (Advisor a: student.getAdvisors()) {
				if (a.getEmail() != null) {
					email = (email == null ? "" : email + "\n") + a.getEmail();
					if (getSessionContext().getUser().getExternalUserId().equals(a.getExternalUniqueId())) {
						email = a.getEmail();
						break;
					}
				}
			}
			ret.setAdvisorEmail(email);
		}
		
		ret.setCanUpdate(false);
		ret.setDegreePlan(CustomDegreePlansHolder.hasProvider() && getSessionContext().hasPermissionAnySession(sessionId, Right.StudentSchedulingAdvisor));
		if (Customization.StudentEmailProvider.hasProvider()) {
			StudentEmailProvider email = Customization.StudentEmailProvider.getProvider();
			ret.setEmailOptionalToggleCaption(email.getToggleCaptionIfOptional());
			ret.setEmailOptionalToggleDefault(email.isOptionCheckedByDefault());
		}
		try {
			String advWlMode = ApplicationProperty.AdvisorRecommendationsWaitListMode.value(student.getSession());
			if ("Student".equalsIgnoreCase(advWlMode))
				ret.setWaitListMode(student.getWaitListMode());
			else
				ret.setWaitListMode(WaitListMode.valueOf(advWlMode));
		} catch (Exception e) {}
		 
		if (getSessionContext().hasPermissionAnySession(sessionId, Right.StudentSchedulingAdmin)) {
			ret.setCanUpdate(true);
		} else if (getSessionContext().hasPermissionAnySession(sessionId, Right.StudentSchedulingAdvisor)) {
			if (getSessionContext().hasPermission(Right.StudentSchedulingAdvisorCanModifyAllStudents))
				ret.setCanUpdate(true);
			else if (getSessionContext().hasPermission(Right.StudentSchedulingAdvisorCanModifyMyStudents) && advisor != null && advisor.getStudents().contains(student))
				ret.setCanUpdate(true);
		}
		if (!student.getSession().getStatusType().canPreRegisterStudents() && !student.getSession().getStatusType().canOnlineSectionStudents()) {
			ret.setCanUpdate(false);
			ret.setDegreePlan(false);
		}
		ret.setCanEmail(getSessionContext().hasPermissionAnySession(student.getSession(), Right.StudentSchedulingEmailStudent));
		ret.setCanRequire(getSessionContext().hasPermissionAnySession(student, Right.StudentSchedulingCanRequirePreferences));
		
		List<CourseType> courseTypes = CourseTypeDAO.getInstance().getSession().createQuery(
				"select distinct t from CourseOffering c inner join c.courseType t where c.instructionalOffering.session = :sessionId order by t.reference"
				).setLong("sessionId", getStatusPageSessionId()).setCacheable(true).list();
		boolean adv = getSessionContext().hasPermissionAnySession(getStatusPageSessionId(), Right.StudentSchedulingAdvisor);
		boolean admin = getSessionContext().hasPermissionAnySession(getStatusPageSessionId(), Right.StudentSchedulingAdmin);
		
		if (student.getSectioningStatus() != null) {
			ret.setStatus(toStudentStatusInfo(student.getSectioningStatus(), courseTypes, admin, adv));
		} else if (student.getSession().getDefaultSectioningStatus() != null) {
			StudentStatusInfo info = toStudentStatusInfo(student.getSession().getDefaultSectioningStatus(), courseTypes, admin, adv);
			info.setUniqueId(null);
			info.setReference("");
			info.setLabel(MSG.studentStatusSessionDefault(student.getSession().getDefaultSectioningStatus().getLabel()));
			info.setEffectiveStart(null); info.setEffectiveStop(null);
			ret.setStatus(info);
		} else {
			StudentStatusInfo info = new StudentStatusInfo();
			info.setReference("");
			info.setLabel(MSG.studentStatusSystemDefault());
			info.setAllEnabled();
			ret.setStatus(info);
		}

		if (ret.isCanUpdate() && getSessionContext().hasPermissionAnySession(sessionId, Right.StudentSchedulingChangeStudentStatus)) {
			boolean canChange = true;
			if (admin) {
				Session session = student.getSession();
				StudentStatusInfo info = null;
				if (session.getDefaultSectioningStatus() != null) {
					info = toStudentStatusInfo(session.getDefaultSectioningStatus(), courseTypes, admin, adv);
					info.setUniqueId(null);
					info.setReference("");
					info.setLabel(MSG.studentStatusSessionDefault(session.getDefaultSectioningStatus().getLabel()));
					info.setEffectiveStart(null); info.setEffectiveStop(null);
				} else {
					info = new StudentStatusInfo();
					info.setReference("");
					info.setLabel(MSG.studentStatusSystemDefault());
					info.setAllEnabled();
				}
				ret.addStatus(info);
			} else if (ApplicationProperty.AdvisorCourseRequestsRestrictedStatusChange.isTrue()) {
				StudentSectioningStatus status = (student.getSectioningStatus() == null ? student.getSession().getDefaultSectioningStatus() : student.getSectioningStatus());
				canChange = (status != null && status.hasOption(StudentSectioningStatus.Option.advcanset));
			}
			if (canChange) {
				for (StudentSectioningStatus s: StudentSectioningStatus.findAll(sessionId)) {
					if (s.isPast()) continue;
					if (!admin && !s.hasOption(StudentSectioningStatus.Option.advcanset)) continue;
					ret.addStatus(toStudentStatusInfo(s, courseTypes, admin, adv));
				}
			}
		}
		
		OnlineSectioningServer server = getServerInstance(sessionId, true);
		if (server != null) {
			ret.setRequest(server.execute(server.createAction(AdvisorGetCourseRequests.class)
					.forStudent(student.getUniqueId()).checkDemands(true).checkHolds(ret.isCanUpdate()), currentUser()));
		}
		
		if (server != null && !(server instanceof DatabaseServer)) {
			ret.setStudentRequest(server.execute(server.createAction(GetRequest.class).forStudent(student.getUniqueId(), false)
					.withCustomValidation(true).withWaitListValidation(true).withCustomRequest(false).withAdvisorRequests(false).withWaitListMode(student.getWaitListMode()), currentUser()));
		} else if (ret.isCanUpdate()) {
			ret.setStudentRequest(getRequest(student));
		}
		
		if (ret.hasStudentRequest()) {
			StudentSectioningStatus status = student.getEffectiveStatus();
			if (CustomStudentEnrollmentHolder.isAllowWaitListing() && (status == null || status.hasOption(Option.waitlist))) {
				ret.getStudentRequest().setWaitListMode(WaitListMode.WaitList);
			} else if (status != null && status.hasOption(Option.nosubs)) {
				ret.getStudentRequest().setWaitListMode(WaitListMode.NoSubs);
			} else {
				ret.getStudentRequest().setWaitListMode(WaitListMode.None);
			}
			if (ret.getRequest() != null) {
				if (ret.getWaitListMode() == WaitListMode.WaitList && server != null && server.getConfig().getPropertyBoolean("Load.UseAdvisorWaitLists", false))
					ret.setAdvisorWaitListedCourseIds(ret.getRequest().getWaitListedCourseIds());
				if (ret.getWaitListMode() == WaitListMode.NoSubs && server != null && server.getConfig().getPropertyBoolean("Load.UseAdvisorNoSubs", false))
					ret.setAdvisorWaitListedCourseIds(ret.getRequest().getNoSubCourseIds());
			}
		}
		
		// has pin but was not advised yet >> set the pin released default to true
		if (ret.getRequest() != null && ret.getRequest().hasPin() && student.getAdvisorCourseRequests().isEmpty())
			ret.getRequest().setPinReleased(true);
		
		// put default note when no note is provided
		if (student.getAdvisorCourseRequests().isEmpty()) {
			String defaultNote = ApplicationProperty.AdvisorCourseRequestsDefaultNote.valueOfSession(sessionId);
			if (defaultNote != null && !defaultNote.isEmpty())
				ret.getRequest().setCreditNote(defaultNote);
		}
		
		return ret;
	}
	
	@Override
	public CheckCoursesResponse checkAdvisingDetails(AdvisingStudentDetails details) throws SectioningException, PageAccessException {
		getSessionContext().checkPermissionAnySession(details.getSessionId(), Right.AdvisorCourseRequests);
		
		OnlineSectioningServer server = getServerInstance(details.getSessionId(), true);
		if (server == null) throw new SectioningException(MSG.exceptionNoServerForSession());
		
		return server.execute(server.createAction(AdvisorCourseRequestsValidate.class).withDetails(details), currentUser());
	}

	@Override
	public AdvisorCourseRequestSubmission submitAdvisingDetails(AdvisingStudentDetails details, boolean emailStudent) throws SectioningException, PageAccessException {
		getSessionContext().checkPermissionAnySession(details.getSessionId(), Right.AdvisorCourseRequests);
		
		OnlineSectioningServer server = getServerInstance(details.getSessionId(), true);
		if (server == null) throw new SectioningException(MSG.exceptionNoServerForSession());
		
		AdvisorCourseRequestSubmission ret = server.execute(server.createAction(AdvisorCourseRequestsSubmit.class).withDetails(details), currentUser());
		
		if (emailStudent && details.isCanEmail()) {
			StudentEmail email = server.createAction(StudentEmail.class)
					.forStudent(details.getStudentId())
					.fromAction("advisor-submit")
					.overridePermissions(false, false, true)
					.includeAdvisorRequestsPDF();
			email.setEmailSubject(MSG.defaulSubjectAdvisorRequests());
			UserContext user = getSessionContext().getUser();
			if (user != null && user instanceof Chameleon)
				email.setCC(((Chameleon)user).getOriginalUserContext().getEmail());
			else if (user != null)
				email.setCC(user.getEmail());
			server.execute(email, currentUser());
		}
		
		try {
	        SessionFactory hibSessionFactory = SessionDAO.getInstance().getSession().getSessionFactory();
	        hibSessionFactory.getCache().evictEntity(Student.class, details.getStudentId());
        } catch (Exception e) {
        	sLog.warn("Failed to evict cache: " + e.getMessage());
        }

		return ret;
	}

	@Override
	public Collection<AcademicSessionProvider.AcademicSessionInfo> getStudentSessions(String studentExternalId) throws SectioningException, PageAccessException {
		ArrayList<AcademicSessionProvider.AcademicSessionInfo> ret = new ArrayList<AcademicSessionProvider.AcademicSessionInfo>();
		ExternalTermProvider extTerm = getExternalTermProvider();
		Set<Long> sessionIds = new HashSet<Long>();
		for (Student student: (List<Student>)StudentDAO.getInstance().getSession().createQuery(
				"from Student where externalUniqueId = :id")
				.setString("id", studentExternalId).setCacheable(true).list()) {
			Session session = student.getSession();
			if (session.getStatusType().isTestSession()) continue;
			if (session.getStatusType().canPreRegisterStudents() || session.getStatusType().canOnlineSectionStudents()) {
				if (!getSessionContext().hasPermissionAnySession(session, Right.AdvisorCourseRequests)) continue;
				AcademicSessionInfo info = new AcademicSessionInfo(session);
				ret.add(new AcademicSessionProvider.AcademicSessionInfo(
						session.getUniqueId(),
						session.getAcademicYear(), session.getAcademicTerm(), session.getAcademicInitiative(),
						MSG.sessionName(session.getAcademicYear(), session.getAcademicTerm(), session.getAcademicInitiative()),
						session.getSessionBeginDateTime()
						)
						.setExternalCampus(extTerm == null ? null : extTerm.getExternalCampus(info))
						.setExternalTerm(extTerm == null ? null : extTerm.getExternalTerm(info))
						);
				sessionIds.add(session.getUniqueId());
			}
		}
		for (Student student: (List<Student>)StudentDAO.getInstance().getSession().createQuery(
				"from Student s where s.externalUniqueId = :id and s.advisorCourseRequests is not empty")
				.setString("id", studentExternalId)
				.setCacheable(true).list()) {
			Session session = student.getSession();
			if (session.getStatusType().isTestSession()) continue;
			if (sessionIds.contains(session.getUniqueId())) continue;
			if (!getSessionContext().hasPermissionAnySession(session, Right.AdvisorCourseRequests)) continue;
			AcademicSessionInfo info = new AcademicSessionInfo(session);
			ret.add(new AcademicSessionProvider.AcademicSessionInfo(
					session.getUniqueId(),
					session.getAcademicYear(), session.getAcademicTerm(), session.getAcademicInitiative(),
					MSG.sessionName(session.getAcademicYear(), session.getAcademicTerm(), session.getAcademicInitiative()),
					session.getSessionBeginDateTime()
					)
					.setExternalCampus(extTerm == null ? null : extTerm.getExternalCampus(info))
					.setExternalTerm(extTerm == null ? null : extTerm.getExternalTerm(info))
					);
		}
		Collections.sort(ret);
		return ret;
	}

	@Override
	public CourseRequestInterface getAdvisorRequests(StudentSectioningContext cx) throws SectioningException, PageAccessException {
		checkContext(cx);
		if (cx.getStudentId() == null)
			throw new PageAccessException(MSG.exceptionNoStudent());
		OnlineSectioningServer server = getServerInstance(cx.getSessionId() == null ? canEnroll(cx) : cx.getSessionId(), true);
		if (server == null) throw new SectioningException(MSG.exceptionNoAcademicSession());
		
		CourseRequestInterface ret = server.execute(server.createAction(AdvisorGetCourseRequests.class).forStudent(cx.getStudentId()).checkDemands(false), currentUser(cx));
		try {
			String advWlMode = ApplicationProperty.AdvisorRecommendationsWaitListMode.value(server.getAcademicSession());
			if ("Student".equalsIgnoreCase(advWlMode))
				ret.setWaitListMode(StudentDAO.getInstance().get(cx.getStudentId()).getWaitListMode());
			else
				ret.setWaitListMode(WaitListMode.valueOf(advWlMode));
		} catch (Exception e) {
			ret.setWaitListMode(WaitListMode.None);
		}
		return ret;
	}

	@Override
	public List<ReservationInterface> getReservations(boolean online, Long offeringId) throws ReservationException, PageAccessException {
		if (online) {
			sessionContext.checkPermission(Right.Reservations);
			OnlineSectioningServer server = getServerInstance(sessionContext.getUser().getCurrentAcademicSessionId(), true);
			if (server != null)
				return server.execute(server.createAction(GetReservationsAction.class).forOfferingId(offeringId), currentUser());
			return new ReservationServlet().withSessionContext(sessionContext).getReservations(offeringId);
		} else {
			StudentSolverProxy server = getStudentSolver();
			if (server == null) 
				throw new SectioningException(MSG.exceptionNoSolver());
			return server.getReservations(offeringId);
		}
	}

	@Override
	public List<AdvisorNote> lastAdvisorNotes(StudentSectioningContext cx) throws SectioningException, PageAccessException {
		checkContext(cx);
		getSessionContext().checkPermissionAnySession(cx.getAcademicSessionId(), Right.AdvisorCourseRequests);
		
		if (!ApplicationProperty.AdvisorCourseRequestsLastNotes.isTrue()) return null;
		
		List<AdvisorNote> ret = new ArrayList<AdvisorNote>();
		String defaultNote = ApplicationProperty.AdvisorCourseRequestsDefaultNote.valueOfSession(cx.getAcademicSessionId());
		Student student = (cx.getStudentId() == null ? null : StudentDAO.getInstance().get(cx.getStudentId()));
		
		List<Object[]> notes = (List<Object[]>)AdvisorCourseRequestDAO.getInstance().getSession().createQuery(
				"select replace(acr.notes, acr.student.pin, '$PIN$'), count(acr), max(acr.timestamp) " +
				"from AdvisorCourseRequest acr where acr.priority = -1 and acr.changedBy = :externalId and acr.student.session = :sessionId " +
				"group by replace(acr.notes, acr.student.pin, '$PIN$') " +
				"order by max(acr.timestamp) desc")
				.setString("externalId", sessionContext.getUser().getExternalUserId())
				.setLong("sessionId", cx.getAcademicSessionId())
				.setCacheable(false).setMaxResults(50).list();
		for (Object[] o: notes) {
			String note = (String)o[0];
			Integer count = ((Number)o[1]).intValue();
			Date ts = (Date)o[2];
			if (note == null || note.isEmpty()) continue;
			String dispNote = note;
			if (defaultNote != null && note.contains(defaultNote))
				dispNote = note.replace(defaultNote, "\u2026");
			if (defaultNote != null && note.contains(defaultNote.replace("\r", "")))
				dispNote = note.replace(defaultNote.replace("\r", ""), "\u2026");
			AdvisorNote an = new AdvisorNote();
			an.setCount(count);
			an.setDisplayString(dispNote.replace("$PIN$", "XXXXXX"));
			an.setReplaceString(note.replace("$PIN$", student == null || student.getPin() == null ? "XXXXXX" : student.getPin()));
			an.setTimeStamp(ts);
			ret.add(an);
		}
		
		return ret;
	}

	@Override
	public String getChangeLogMessage(Long logId) throws SectioningException, PageAccessException {
		getSessionContext().checkPermission(Right.SchedulingDashboard);
		org.unitime.timetable.model.OnlineSectioningLog log = OnlineSectioningLogDAO.getInstance().get(logId);
		if (log != null) {
			try {
				OnlineSectioningLog.Action action = OnlineSectioningLog.Action.parseFrom(log.getAction());
				if (action != null) {
					return FindOnlineSectioningLogAction.getHTML(action);
				} else {
					throw new SectioningException("Failed to load log message: Log message has no details.");
				}
			} catch (InvalidProtocolBufferException e) {
				throw new SectioningException("Failed to parse log message: " + e.getMessage(), e);
			}
		} else {
			throw new SectioningException("Failed to load log message: Log message does not exist.");
		}
	}

	@Override
	public Map<Long, String> getChangeLogTexts(Collection<Long> logIds) throws SectioningException, PageAccessException {
		getSessionContext().checkPermission(Right.SchedulingDashboard);
		Map<Long, String> ret = new HashMap<Long, String>();
		for (Object[] o: (List<Object[]>)OnlineSectioningLogDAO.getInstance().getSession().createQuery(
				"select uniqueId, action from OnlineSectioningLog where uniqueId in :logIds"
				).setParameterList("logIds", logIds, LongType.INSTANCE).list()) {
			Long id = (Long)o[0];
			try {
				OnlineSectioningLog.Action action = OnlineSectioningLog.Action.parseFrom((byte[])o[1]);
				String message = OnlineSectioningLogger.getMessage(action);
				if (message != null && !message.isEmpty())
					ret.put(id, message);
			} catch (InvalidProtocolBufferException e) {
			}
		}
		return ret;
	}

	@Override
	public Collection<VariableTitleCourseInfo> listVariableTitleCourses(StudentSectioningContext cx, String query, int limit) throws SectioningException, PageAccessException {
		checkContext(cx);
		if (cx.getSessionId() == null) throw new PageAccessException(MSG.exceptionNoAcademicSession());
		if (cx.getStudentId() == null) throw new PageAccessException(MSG.exceptionNoStudent());
		getSessionContext().checkPermissionAnyAuthority(cx.getStudentId(), "Student", Right.StudentSchedulingCanEnroll);
		
		OnlineSectioningServer server = getServerInstance(cx.getSessionId(), false);
		if (server == null) throw new SectioningException(MSG.exceptionNoServerForSession());
		if (!server.getAcademicSession().isSectioningEnabled() || !Customization.VariableTitleCourseProvider.hasProvider())
			throw new SectioningException(MSG.exceptionNotSupportedFeature());

		VariableTitleCourseProvider provider = Customization.VariableTitleCourseProvider.getProvider();
		OnlineSectioningHelper helper = new OnlineSectioningHelper(SessionDAO.getInstance().getSession(), currentUser(cx));
		
		return provider.getVariableTitleCourses(query, limit, server, helper);
	}
	
	@Override
	public VariableTitleCourseInfo getVariableTitleCourse(StudentSectioningContext cx, String course) throws SectioningException, PageAccessException {
		checkContext(cx);
		if (cx.getSessionId() == null) throw new PageAccessException(MSG.exceptionNoAcademicSession());
		if (cx.getStudentId() == null) throw new PageAccessException(MSG.exceptionNoStudent());
		getSessionContext().checkPermissionAnyAuthority(cx.getStudentId(), "Student", Right.StudentSchedulingCanEnroll);
		
		OnlineSectioningServer server = getServerInstance(cx.getSessionId(), false);
		if (server == null) throw new SectioningException(MSG.exceptionNoServerForSession());
		if (!server.getAcademicSession().isSectioningEnabled() || !Customization.VariableTitleCourseProvider.hasProvider())
			throw new SectioningException(MSG.exceptionNotSupportedFeature());

		VariableTitleCourseProvider provider = Customization.VariableTitleCourseProvider.getProvider();
		OnlineSectioningHelper helper = new OnlineSectioningHelper(SessionDAO.getInstance().getSession(), currentUser(cx));
		
		return provider.getVariableTitleCourse(course, server, helper);
	}

	@Override
	public VariableTitleCourseResponse requestVariableTitleCourse(VariableTitleCourseRequest request) throws SectioningException, PageAccessException {
		checkContext(request);
		if (request.getSessionId() == null) throw new PageAccessException(MSG.exceptionNoAcademicSession());
		if (request.getStudentId() == null) throw new PageAccessException(MSG.exceptionNoStudent());
		getSessionContext().checkPermissionAnyAuthority(request.getStudentId(), "Student", Right.StudentSchedulingCanEnroll);
		
		OnlineSectioningServer server = getServerInstance(request.getSessionId(), false);
		if (server == null) throw new SectioningException(MSG.exceptionNoServerForSession());
		if (!server.getAcademicSession().isSectioningEnabled() || !Customization.VariableTitleCourseProvider.hasProvider())
			throw new SectioningException(MSG.exceptionNotSupportedFeature());
		
		return server.execute(server.createAction(SpecialRegistrationRequestVariableTitleCourse.class).withRequest(request), currentUser());
	}

	@Override
	public CheckCoursesResponse waitListCheckValidation(CourseRequestInterface request) throws SectioningException, PageAccessException {
		checkContext(request);
		if (request.getSessionId() == null) throw new PageAccessException(MSG.exceptionNoAcademicSession());
		if (request.getStudentId() == null) throw new PageAccessException(MSG.exceptionNoStudent());
		getSessionContext().checkPermissionAnyAuthority(request.getStudentId(), "Student", Right.StudentSchedulingCanEnroll);
		
		OnlineSectioningServer server = getServerInstance(request.getSessionId(), false);
		if (server == null) throw new SectioningException(MSG.exceptionNoServerForSession());
		if (!server.getAcademicSession().isSectioningEnabled() || !Customization.WaitListValidationProvider.hasProvider())
			throw new SectioningException(MSG.exceptionNotSupportedFeature());
		
		return server.execute(server.createAction(WaitListCheckValidation.class).withRequest(request), currentUser());
	}

	@Override
	public CourseRequestInterface waitListSubmitOverrides(CourseRequestInterface request, Float neededCredit) throws SectioningException, PageAccessException {
		checkContext(request);
		if (request.getSessionId() == null) throw new PageAccessException(MSG.exceptionNoAcademicSession());
		if (request.getStudentId() == null) throw new PageAccessException(MSG.exceptionNoStudent());
		getSessionContext().checkPermissionAnyAuthority(request.getStudentId(), "Student", Right.StudentSchedulingCanEnroll);
		
		OnlineSectioningServer server = getServerInstance(request.getSessionId(), false);
		if (server == null) throw new SectioningException(MSG.exceptionNoServerForSession());
		if (!server.getAcademicSession().isSectioningEnabled() || !Customization.WaitListValidationProvider.hasProvider())
			throw new SectioningException(MSG.exceptionNotSupportedFeature());
		
		return server.execute(server.createAction(WaitListSubmitOverrides.class).withRequest(request).withCredit(neededCredit), currentUser());
	}

	protected OtherAuthority getStudentAuthority(Long sessionId) {
		return new StudentAuthority(getSessionContext(), new SimpleQualifier("Session", sessionId));
	}
	
	protected OtherAuthority getStudentAuthority(Qualifiable session) {
		return new StudentAuthority(getSessionContext(), session);
	}
	
	protected static class StudentAuthority implements OtherAuthority {
		private String iRole = null;
		private boolean iAllowNoRole = false;
		private Qualifiable[] iFilter = null;

		protected StudentAuthority(SessionContext context, Qualifiable... filter) {
			UserContext user = context.getUser();
			iFilter = filter;
			if (user != null && user.getCurrentAuthority() != null) {
				iRole = user.getCurrentAuthority().getRole();
				iAllowNoRole = true;
				authorities: for (UserAuthority authority: user.getAuthorities()) {
					for (Qualifiable q: filter)
						if (!authority.hasQualifier(q)) continue authorities;
					if (Roles.ROLE_STUDENT.equals(authority.getRole())) {
						iAllowNoRole = false;
						break authorities;
					}
				}
			}
		}

		@Override
		public boolean isMatch(UserAuthority authority) {
			if (iRole == null) return false;
			
			for (Qualifiable q: iFilter)
				if (!authority.hasQualifier(q)) return false;
			
			// allow current role
			if (iRole.equals(authority.getRole())) return true;
			
			// allow student role
			if (Roles.ROLE_STUDENT.equals(authority.getRole())) return true;
			
			// allow no role (when no matching student role was found)
			if (iAllowNoRole && Roles.ROLE_NONE.equals(authority.getRole())) return true;
			
			return false;
		}
	}
}