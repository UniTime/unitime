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
import org.cpsolver.coursett.model.RoomLocation;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.client.sectioning.SectioningStatusFilterBox.SectioningStatusFilterRpcRequest;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.services.SectioningService;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.ClassAssignment;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.EnrollmentInfo;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.SectioningAction;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.EligibilityCheck;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.EligibilityCheck.EligibilityFlag;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.SectioningProperties;
import org.unitime.timetable.gwt.shared.PageAccessException;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.CourseAssignment;
import org.unitime.timetable.model.AcademicAreaClassification;
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
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.PosMajor;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentAccomodation;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.StudentGroup;
import org.unitime.timetable.model.StudentSectioningStatus;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.StudentSectioningStatus.Option;
import org.unitime.timetable.model.comparators.ClassComparator;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.CurriculumDAO;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.model.dao.StudentSectioningStatusDAO;
import org.unitime.timetable.onlinesectioning.AcademicSessionInfo;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.basic.CheckCourses;
import org.unitime.timetable.onlinesectioning.basic.CheckEligibility;
import org.unitime.timetable.onlinesectioning.basic.GetAssignment;
import org.unitime.timetable.onlinesectioning.basic.GetRequest;
import org.unitime.timetable.onlinesectioning.basic.ListClasses;
import org.unitime.timetable.onlinesectioning.basic.ListEnrollments;
import org.unitime.timetable.onlinesectioning.custom.CourseDetailsProvider;
import org.unitime.timetable.onlinesectioning.custom.CustomStudentEnrollmentHolder;
import org.unitime.timetable.onlinesectioning.custom.DefaultCourseDetailsProvider;
import org.unitime.timetable.onlinesectioning.custom.RequestStudentUpdates;
import org.unitime.timetable.onlinesectioning.match.AbstractCourseMatcher;
import org.unitime.timetable.onlinesectioning.model.XCourse;
import org.unitime.timetable.onlinesectioning.model.XCourseId;
import org.unitime.timetable.onlinesectioning.model.XCourseRequest;
import org.unitime.timetable.onlinesectioning.solver.ComputeSuggestionsAction;
import org.unitime.timetable.onlinesectioning.solver.FindAssignmentAction;
import org.unitime.timetable.onlinesectioning.status.FindEnrollmentAction;
import org.unitime.timetable.onlinesectioning.status.FindEnrollmentInfoAction;
import org.unitime.timetable.onlinesectioning.status.FindStudentInfoAction;
import org.unitime.timetable.onlinesectioning.status.FindOnlineSectioningLogAction;
import org.unitime.timetable.onlinesectioning.status.StatusPageSuggestionsAction;
import org.unitime.timetable.onlinesectioning.updates.ApproveEnrollmentsAction;
import org.unitime.timetable.onlinesectioning.updates.ChangeStudentStatus;
import org.unitime.timetable.onlinesectioning.updates.EnrollStudent;
import org.unitime.timetable.onlinesectioning.updates.MassCancelAction;
import org.unitime.timetable.onlinesectioning.updates.RejectEnrollmentsAction;
import org.unitime.timetable.onlinesectioning.updates.SaveStudentRequests;
import org.unitime.timetable.onlinesectioning.updates.StudentEmail;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.UserAuthority;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.qualifiers.SimpleQualifier;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.service.SolverServerService;
import org.unitime.timetable.solver.service.SolverService;
import org.unitime.timetable.solver.studentsct.StudentSolverProxy;
import org.unitime.timetable.util.LoginManager;
import org.unitime.timetable.util.NameFormat;

/**
 * @author Tomas Muller
 */
@Service("sectioning.gwt")
public class SectioningServlet implements SectioningService, DisposableBean {
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
	private static Logger sLog = Logger.getLogger(SectioningServlet.class);
	private CourseDetailsProvider iCourseDetailsProvider;
	
	public SectioningServlet() {
	}
	
	private CourseDetailsProvider getCourseDetailsProvider() {
		if (iCourseDetailsProvider == null) {
			try {
				String providerClass = ApplicationProperty.CustomizationCourseDetails.value();
				if (providerClass != null)
					iCourseDetailsProvider = (CourseDetailsProvider)Class.forName(providerClass).newInstance();
			} catch (Exception e) {
				sLog.warn("Failed to initialize course detail provider: " + e.getMessage());
				iCourseDetailsProvider = new DefaultCourseDetailsProvider();
			}
		}
		return iCourseDetailsProvider;
	}
	
	private @Autowired AuthenticationManager authenticationManager;
	private AuthenticationManager getAuthenticationManager() { return authenticationManager; }
	private @Autowired SessionContext sessionContext;
	private SessionContext getSessionContext() { return sessionContext; }
	private @Autowired SolverService<StudentSolverProxy> studentSectioningSolverService;
	private StudentSolverProxy getStudentSolver() { return studentSectioningSolverService.getSolver(); }
	private @Autowired SolverServerService solverServerService;
	private OnlineSectioningServer getServerInstance(Long academicSessionId) {
		if (academicSessionId == null) return null;
		return solverServerService.getOnlineStudentSchedulingContainer().getSolver(academicSessionId.toString());
	}

	public Collection<ClassAssignmentInterface.CourseAssignment> listCourseOfferings(Long sessionId, String query, Integer limit) throws SectioningException, PageAccessException {
		if (sessionId==null) throw new SectioningException(MSG.exceptionNoAcademicSession());
		setLastSessionId(sessionId);
		
		CourseMatcher matcher = getCourseMatcher(sessionId);
		
		OnlineSectioningServer server = getServerInstance(sessionId);
		
		if (server == null) {
			String types = "";
			for (String ref: matcher.getAllowedCourseTypes())
				types += (types.isEmpty() ? "" : ", ") + "'" + ref + "'";
			if (!matcher.isAllCourseTypes() && !matcher.isNoCourseType() && types.isEmpty()) throw new SectioningException(MSG.exceptionCourseDoesNotExist(query));
			
			ArrayList<ClassAssignmentInterface.CourseAssignment> results = new ArrayList<ClassAssignmentInterface.CourseAssignment>();
			org.hibernate.Session hibSession = CurriculumDAO.getInstance().getSession();
			for (CourseOffering c: (List<CourseOffering>)hibSession.createQuery(
					"select c from CourseOffering c where " +
					"c.subjectArea.session.uniqueId = :sessionId and (" +
					"lower(c.subjectArea.subjectAreaAbbreviation || ' ' || c.courseNbr) like :q || '%' " +
					(query.length()>2 ? "or lower(c.title) like '%' || :q || '%'" : "") + ") " +
					(matcher.isAllCourseTypes() ? "" : matcher.isNoCourseType() ? types.isEmpty() ? " and c.courseType is null " : " and (c.courseType is null or c.courseType.reference in (" + types + ")) " : " and c.courseType.reference in (" + types + ") ") +
					"order by case " +
					"when lower(c.subjectArea.subjectAreaAbbreviation || ' ' || c.courseNbr) like :q || '%' then 0 else 1 end," + // matches on course name first
					"c.subjectArea.subjectAreaAbbreviation, c.courseNbr")
					.setString("q", query.toLowerCase())
					.setLong("sessionId", sessionId)
					.setCacheable(true).setMaxResults(limit == null || limit < 0 ? Integer.MAX_VALUE : limit).list()) {
				CourseAssignment course = new CourseAssignment();
				course.setCourseId(c.getUniqueId());
				course.setSubject(c.getSubjectAreaAbbv());
				course.setCourseNbr(c.getCourseNbr());
				course.setNote(c.getScheduleBookNote());
				if (c.getCredit() != null) {
					course.setCreditText(c.getCredit().creditText());
					course.setCreditAbbv(c.getCredit().creditAbbv());
				}
				course.setTitle(c.getTitle());
				course.setHasUniqueName(true);
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
			}
			if (results.isEmpty()) {
				throw new SectioningException(MSG.exceptionCourseDoesNotExist(query));
			}
			return results;
		} else {
			ArrayList<ClassAssignmentInterface.CourseAssignment> ret = new ArrayList<ClassAssignmentInterface.CourseAssignment>();
			try {
				for (XCourseId id: server.findCourses(query, limit, matcher)) {
					XCourse c = server.getCourse(id.getCourseId());
					if (c == null) continue;
					CourseAssignment course = new CourseAssignment();
					course.setCourseId(c.getCourseId());
					course.setSubject(c.getSubjectArea());
					course.setCourseNbr(c.getCourseNumber());
					course.setNote(c.getNote());
					course.setCreditAbbv(c.getCreditAbbv());
					course.setCreditText(c.getCreditText());
					course.setTitle(c.getTitle());
					course.setHasUniqueName(c.hasUniqueName());
					course.setLimit(c.getLimit());
					Collection<XCourseRequest> requests = server.getRequests(c.getOfferingId());
					if (requests != null) {
						int enrl = 0;
						for (XCourseRequest r: requests)
							if (r.getEnrollment() != null && r.getEnrollment().getCourseId().equals(course.getCourseId()))
								enrl ++;
						course.setEnrollment(enrl);
					}
					ret.add(course);
				}
			} catch (Exception e) {
				if (e instanceof SectioningException) throw (SectioningException)e;
				sLog.error(e.getMessage(), e);
				throw new SectioningException(MSG.exceptionUnknown(e.getMessage()), e);
			}
			if (ret.isEmpty()) {
				throw new SectioningException(MSG.exceptionCourseDoesNotExist(query));
			}
			return ret;
		}
	}
	
	public CourseMatcher getCourseMatcher(Long sessionId) {
		boolean noCourseType = true, allCourseTypes = false;
		Set<String> allowedCourseTypes = new HashSet<String>();
		if (getSessionContext().hasPermission(Right.StudentSchedulingAdvisor)) {
			allCourseTypes = true;
		} else {
			Long studentId = getStudentId(sessionId);
			Student student = (studentId == null ? null : StudentDAO.getInstance().get(studentId));
			StudentSectioningStatus status = (student == null ? null : student.getSectioningStatus());
			if (status == null) status = SessionDAO.getInstance().get(sessionId).getDefaultSectioningStatus();
			if (status != null) {
				for (CourseType type: status.getTypes())
					allowedCourseTypes.add(type.getReference());
				noCourseType = !status.hasOption(Option.notype);
			}
		}
		return new CourseMatcher(allCourseTypes, noCourseType, allowedCourseTypes);
	}
	
	@SuppressWarnings("unchecked")
	public Collection<ClassAssignmentInterface.ClassAssignment> listClasses(Long sessionId, String course) throws SectioningException, PageAccessException {
		if (sessionId==null) throw new SectioningException(MSG.exceptionNoAcademicSession());
		setLastSessionId(sessionId);
		OnlineSectioningServer server = getServerInstance(sessionId);
		if (server == null) {
			ArrayList<ClassAssignmentInterface.ClassAssignment> results = new ArrayList<ClassAssignmentInterface.ClassAssignment>();
			org.hibernate.Session hibSession = CurriculumDAO.getInstance().getSession();
			CourseOffering courseOffering = null;
			for (CourseOffering c: (List<CourseOffering>)hibSession.createQuery(
					"select c from CourseOffering c where " +
					"c.subjectArea.session.uniqueId = :sessionId and " +
					"lower(c.subjectArea.subjectAreaAbbreviation || ' ' || c.courseNbr) = :course")
					.setString("course", course.toLowerCase())
					.setLong("sessionId", sessionId)
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
				if (!clazz.isEnabledForStudentScheduling()) continue;
				ClassAssignmentInterface.ClassAssignment a = new ClassAssignmentInterface.ClassAssignment();
				a.setClassId(clazz.getUniqueId());
				a.setSubpart(clazz.getSchedulingSubpart().getItypeDesc());
				a.setSection(clazz.getClassSuffix(courseOffering));
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
				a.setLimit(new int[] {clazz.getEnrollment() == 0 ? -1 : clazz.getEnrollment(), limit});
				
				if (p != null && p.getTimeLocation() != null) {
					for (DayCode d: DayCode.toDayCodes(p.getTimeLocation().getDayCode()))
						a.addDay(d.getIndex());
					a.setStart(p.getTimeLocation().getStartSlot());
					a.setLength(p.getTimeLocation().getLength());
					a.setBreakTime(p.getTimeLocation().getBreakTime());
					a.setDatePattern(p.getTimeLocation().getDatePatternName());
				}
				if (p != null && p.getRoomLocations() != null) {
					for (RoomLocation rm: p.getRoomLocations()) {
						a.addRoom(rm.getName());
					}
				}
				if (p != null && p.getRoomLocation() != null) {
					a.addRoom(p.getRoomLocation().getName());
				}
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
				return server.execute(server.createAction(ListClasses.class).forCourseAndStudent(course, getStudentId(sessionId)), currentUser());
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
		if (sectioning) {
			for (String s: solverServerService.getOnlineStudentSchedulingContainer().getSolvers()) {
				OnlineSectioningServer server = solverServerService.getOnlineStudentSchedulingContainer().getSolver(s);
				if (server == null || !server.isReady()) continue;
				Session session = SessionDAO.getInstance().get(Long.valueOf(s));
				ret.add(new AcademicSessionProvider.AcademicSessionInfo(
						session.getUniqueId(),
						session.getAcademicYear(), session.getAcademicTerm(), session.getAcademicInitiative(),
						MSG.sessionName(session.getAcademicYear(), session.getAcademicTerm(), session.getAcademicInitiative())));
			}
		} else {
			for (Session session: SessionDAO.getInstance().findAll()) {
				if (session.getStatusType().isTestSession()) continue;
				if (session.getStatusType().canPreRegisterStudents() && !session.getStatusType().canSectionAssistStudents() && !session.getStatusType().canOnlineSectionStudents())
					ret.add(new AcademicSessionProvider.AcademicSessionInfo(
							session.getUniqueId(),
							session.getAcademicYear(), session.getAcademicTerm(), session.getAcademicInitiative(),
							MSG.sessionName(session.getAcademicYear(), session.getAcademicTerm(), session.getAcademicInitiative())));
			}
		}
		if (ret.isEmpty()) {
			throw new SectioningException(MSG.exceptionNoSuitableAcademicSessions());
		}
		return ret;
	}
	
	public String retrieveCourseDetails(Long sessionId, String course) throws SectioningException, PageAccessException {
		setLastSessionId(sessionId);
		OnlineSectioningServer server = getServerInstance(sessionId); 
		if (server == null) {
			CourseOffering courseOffering = SaveStudentRequests.getCourse(CourseOfferingDAO.getInstance().getSession(), sessionId, course);
			if (courseOffering == null) throw new SectioningException(MSG.exceptionCourseDoesNotExist(course));
			return getCourseDetailsProvider().getDetails(
					new AcademicSessionInfo(courseOffering.getSubjectArea().getSession()),
					courseOffering.getSubjectAreaAbbv(), courseOffering.getCourseNbr());
		} else {
			XCourseId c = getServerInstance(sessionId).getCourse(course);
			if (c == null) throw new SectioningException(MSG.exceptionCourseDoesNotExist(course));
			return server.getCourseDetails(c.getCourseId(), getCourseDetailsProvider());
		}
	}
	
	public Long retrieveCourseOfferingId(Long sessionId, String course) throws SectioningException, PageAccessException {
		setLastSessionId(sessionId);
		XCourseId c = getServerInstance(sessionId).getCourse(course);
		if (c == null) throw new SectioningException(MSG.exceptionCourseDoesNotExist(course));
		return c.getCourseId();
	}

	public ClassAssignmentInterface section(boolean online, CourseRequestInterface request, ArrayList<ClassAssignmentInterface.ClassAssignment> currentAssignment) throws SectioningException, PageAccessException {
		try {
			if (!online) {
				OnlineSectioningServer server = getStudentSolver();
				if (server == null) 
					throw new SectioningException(MSG.exceptionNoSolver());
				request.setStudentId(getStudentId(request.getAcademicSessionId()));
				ClassAssignmentInterface ret = server.execute(server.createAction(FindAssignmentAction.class).forRequest(request).withAssignment(currentAssignment), currentUser()).get(0);
				if (ret != null)
					ret.setCanEnroll(false);
				return ret;
			}
			
			setLastSessionId(request.getAcademicSessionId());
			setLastRequest(request);
			request.setStudentId(getStudentId(request.getAcademicSessionId()));
			OnlineSectioningServer server = getServerInstance(request.getAcademicSessionId());
			if (server == null) throw new SectioningException(MSG.exceptionNoServerForSession());
			ClassAssignmentInterface ret = server.execute(server.createAction(FindAssignmentAction.class).forRequest(request).withAssignment(currentAssignment), currentUser()).get(0);
			if (ret != null) {
				ret.setCanEnroll(server.getAcademicSession().isSectioningEnabled());
				if (ret.isCanEnroll()) {
					if (getStudentId(request.getAcademicSessionId()) == null)
						ret.setCanEnroll(false);
				}
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
	
	public Collection<String> checkCourses(boolean online, CourseRequestInterface request) throws SectioningException, PageAccessException {
		try {
			if (request.getAcademicSessionId() == null) throw new SectioningException(MSG.exceptionNoAcademicSession());
			
			if (!online) {
				OnlineSectioningServer server = getStudentSolver();
				if (server == null) 
					throw new SectioningException(MSG.exceptionNoSolver());
				request.setStudentId(getStudentId(request.getAcademicSessionId()));
				return server.execute(server.createAction(CheckCourses.class).forRequest(request), currentUser());
			}
			
			setLastSessionId(request.getAcademicSessionId());
			setLastRequest(request);
			org.hibernate.Session hibSession = CurriculumDAO.getInstance().getSession();
			OnlineSectioningServer server = getServerInstance(request.getAcademicSessionId());
			if (server == null) {
				ArrayList<String> notFound = new ArrayList<String>();
				CourseMatcher matcher = getCourseMatcher(request.getAcademicSessionId());
				Long studentId = getStudentId(request.getAcademicSessionId());
				for (CourseRequestInterface.Request cr: request.getCourses()) {
					if (!cr.hasRequestedFreeTime() && cr.hasRequestedCourse() && lookupCourse(hibSession, request.getAcademicSessionId(), studentId, cr.getRequestedCourse(), matcher) == null)
						notFound.add(cr.getRequestedCourse());
					if (cr.hasFirstAlternative() && lookupCourse(hibSession, request.getAcademicSessionId(), studentId, cr.getFirstAlternative(), matcher) == null)
						notFound.add(cr.getFirstAlternative());
					if (cr.hasSecondAlternative() && lookupCourse(hibSession, request.getAcademicSessionId(), studentId, cr.getSecondAlternative(), matcher) == null)
						notFound.add(cr.getSecondAlternative());
				}
				for (CourseRequestInterface.Request cr: request.getAlternatives()) {
					if (cr.hasRequestedCourse() && lookupCourse(hibSession, request.getAcademicSessionId(), studentId,  cr.getRequestedCourse(), matcher) == null)
						notFound.add(cr.getRequestedCourse());
					if (cr.hasFirstAlternative() && lookupCourse(hibSession, request.getAcademicSessionId(), studentId, cr.getFirstAlternative(), matcher) == null)
						notFound.add(cr.getFirstAlternative());
					if (cr.hasSecondAlternative() && lookupCourse(hibSession, request.getAcademicSessionId(), studentId,  cr.getSecondAlternative(), matcher) == null)
						notFound.add(cr.getSecondAlternative());
				}
				return notFound;
			} else {
				return server.execute(server.createAction(CheckCourses.class).forRequest(request).withMatcher(getCourseMatcher(request.getAcademicSessionId())), currentUser());
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
		if (studentId != null) {
			for (CourseOffering co: (List<CourseOffering>)hibSession.createQuery(
					"select cr.courseOffering from CourseRequest cr where " +
					"cr.courseDemand.student.uniqueId = :studentId and " +
					"lower(cr.courseOffering.subjectArea.subjectAreaAbbreviation || ' ' || cr.courseOffering.courseNbr) = :course")
					.setString("course", courseName.toLowerCase())
					.setLong("studentId", studentId)
					.setCacheable(true).setMaxResults(1).list()) {
				return co;
			}
		}
		for (CourseOffering co: (List<CourseOffering>)hibSession.createQuery(
				"select c from CourseOffering c where " +
				"c.subjectArea.session.uniqueId = :sessionId and " +
				"lower(c.subjectArea.subjectAreaAbbreviation || ' ' || c.courseNbr) = :course")
				.setString("course", courseName.toLowerCase())
				.setLong("sessionId", sessionId)
				.setCacheable(true).setMaxResults(1).list()) {
			if (courseMatcher != null && !courseMatcher.match(new XCourse(co))) continue;
			return co;
		}
		return null;
	}
	
	public 	Collection<ClassAssignmentInterface> computeSuggestions(boolean online, CourseRequestInterface request, Collection<ClassAssignmentInterface.ClassAssignment> currentAssignment, int selectedAssignmentIndex, String filter) throws SectioningException, PageAccessException {
		try {
			if (!online) {
				OnlineSectioningServer server = getStudentSolver();
				if (server == null) 
					throw new SectioningException(MSG.exceptionNoSolver());
				
				request.setStudentId(getStudentId(request.getAcademicSessionId()));
				ClassAssignmentInterface.ClassAssignment selectedAssignment = ((List<ClassAssignmentInterface.ClassAssignment>)currentAssignment).get(selectedAssignmentIndex);
				
				Collection<ClassAssignmentInterface> ret = server.execute(server.createAction(ComputeSuggestionsAction.class).forRequest(request).withAssignment(currentAssignment).withSelection(selectedAssignment).withFilter(filter), currentUser());
				
				if (ret != null)
					for (ClassAssignmentInterface ca: ret)
						ca.setCanEnroll(false);
				
				return ret;
			}
			
			setLastSessionId(request.getAcademicSessionId());
			setLastRequest(request);
			request.setStudentId(getStudentId(request.getAcademicSessionId()));
			ClassAssignmentInterface.ClassAssignment selectedAssignment = ((List<ClassAssignmentInterface.ClassAssignment>)currentAssignment).get(selectedAssignmentIndex);
			OnlineSectioningServer server = getServerInstance(request.getAcademicSessionId());
			if (server == null) throw new SectioningException(MSG.exceptionNoServerForSession());
			Collection<ClassAssignmentInterface> ret = server.execute(server.createAction(ComputeSuggestionsAction.class).forRequest(request).withAssignment(currentAssignment).withSelection(selectedAssignment).withFilter(filter), currentUser());
			if (ret != null) {
				boolean canEnroll = server.getAcademicSession().isSectioningEnabled();
				if (canEnroll) {
					if (getStudentId(request.getAcademicSessionId()) == null)
						canEnroll = false;
				}
				for (ClassAssignmentInterface ca: ret)
					ca.setCanEnroll(canEnroll);
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
		if (pin != null && !pin.isEmpty())
			getSessionContext().setAttribute("pin", pin);
		else
			getSessionContext().removeAttribute("pin");
		if ("LOOKUP".equals(userName)) {
			getSessionContext().checkPermission(Right.StudentSchedulingAdvisor);
			org.hibernate.Session hibSession = StudentDAO.getInstance().createNewSession();
			try {
				List<Student> student = hibSession.createQuery("select m from Student m where m.externalUniqueId = :uid").setString("uid", password).list();
				if (!student.isEmpty()) {
					UserContext user = getSessionContext().getUser();
					UniTimePrincipal principal = new UniTimePrincipal(user.getExternalUserId(), user.getName());
					for (Student s: student) {
						principal.addStudentId(s.getSession().getUniqueId(), s.getUniqueId());
						principal.setName(NameFormat.defaultFormat().format(s));
					}
					getSessionContext().setAttribute("user", principal);
					getSessionContext().removeAttribute("request");
					return principal.getName();
				}
			} finally {
				hibSession.close();
			}			
		}
		if ("BATCH".equals(userName)) {
			getSessionContext().checkPermission(Right.StudentSectioningSolver);
			OnlineSectioningServer server = getStudentSolver();
			if (server == null) 
				throw new SectioningException(MSG.exceptionNoSolver());
			org.hibernate.Session hibSession = StudentDAO.getInstance().createNewSession();
			try {
				Student student = StudentDAO.getInstance().get(Long.valueOf(password), hibSession);
				if (student == null)
					throw new SectioningException(MSG.exceptionLoginFailed());
				UserContext user = getSessionContext().getUser();
				UniTimePrincipal principal = new UniTimePrincipal(user.getExternalUserId(), user.getName());
				principal.addStudentId(student.getSession().getUniqueId(), student.getUniqueId());
				principal.setName(NameFormat.defaultFormat().format(student));
				getSessionContext().setAttribute("user", principal);
				getSessionContext().removeAttribute("request");
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
		getSessionContext().removeAttribute("user");
		getSessionContext().removeAttribute("pin");
		getSessionContext().removeAttribute("sessionId");
		getSessionContext().removeAttribute("request");
		if (getSessionContext().hasPermission(Right.StudentSchedulingAdvisor)) 
			return false;
		SecurityContextHolder.getContext().setAuthentication(null);
		return true;
	}
	
	public String whoAmI() throws SectioningException, PageAccessException {
		UniTimePrincipal principal = (UniTimePrincipal)getSessionContext().getAttribute("user");
		if (principal != null) return principal.getName();
		UserContext user = getSessionContext().getUser();
		if (user != null) return (user.getName() == null ? user.getUsername() : user.getName());
		return "Guest";
	}
	
	public Long getStudentId(Long sessionId) {
		UniTimePrincipal principal = (UniTimePrincipal)getSessionContext().getAttribute("user");
		if (principal != null)
			return principal.getStudentId(sessionId);
		UserContext user = getSessionContext().getUser();
		if (user == null) return null;
		for (UserAuthority a: user.getAuthorities(Roles.ROLE_STUDENT, new SimpleQualifier("Session", sessionId)))
			return a.getUniqueId();
		return null;
	}
	
	public Long getLastSessionId() {
		Long lastSessionId = (Long)getSessionContext().getAttribute("sessionId");
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
		getSessionContext().setAttribute("sessionId", sessionId);
	}
	
	public CourseRequestInterface getLastRequest() {
		return (CourseRequestInterface)getSessionContext().getAttribute("request");
	}
	
	public void setLastRequest(CourseRequestInterface request) {
		if (request == null || request.getAcademicSessionId() == null)
			getSessionContext().removeAttribute("request");
		else
			getSessionContext().setAttribute("request", request);
	}
	
	public AcademicSessionProvider.AcademicSessionInfo lastAcademicSession(boolean sectioning) throws SectioningException, PageAccessException {
		if (getSessionContext().isHttpSessionNew()) throw new PageAccessException(MSG.exceptionUserNotLoggedIn());
		Long sessionId = getLastSessionId();
		if (sessionId == null) throw new SectioningException(MSG.exceptionNoAcademicSession());
		if (sectioning) {
			OnlineSectioningServer server = getServerInstance(sessionId);
			if (server == null) throw new SectioningException(MSG.exceptionNoServerForSession());
			AcademicSessionInfo s = server.getAcademicSession();
			if (s == null) throw new SectioningException(MSG.exceptionNoServerForSession());
			return new AcademicSessionProvider.AcademicSessionInfo(
					s.getUniqueId(),
					s.getYear(), s.getTerm(), s.getCampus(),
					MSG.sessionName(s.getYear(), s.getTerm(), s.getCampus()));
		} else {
			Session session = SessionDAO.getInstance().get(sessionId);
			if (session == null || session.getStatusType().isTestSession())
				throw new SectioningException(MSG.exceptionNoSuitableAcademicSessions());
			if (!session.getStatusType().canPreRegisterStudents() || session.getStatusType().canSectionAssistStudents() || session.getStatusType().canOnlineSectionStudents())
				throw new SectioningException(MSG.exceptionNoServerForSession());
			return new AcademicSessionProvider.AcademicSessionInfo(
					session.getUniqueId(),
					session.getAcademicYear(), session.getAcademicTerm(), session.getAcademicInitiative(),
					MSG.sessionName(session.getAcademicYear(), session.getAcademicTerm(), session.getAcademicInitiative()));
		}
	}
	
	public CourseRequestInterface lastRequest(boolean online, Long sessionId) throws SectioningException, PageAccessException {
		CourseRequestInterface request = getLastRequest();
		if (request != null && !request.getAcademicSessionId().equals(sessionId)) request = null;
		if (request != null && request.getCourses().isEmpty() && request.getAlternatives().isEmpty()) request = null;
		if (request == null) {
			Long studentId = getStudentId(sessionId);
			if (studentId == null) throw new SectioningException(MSG.exceptionNoStudent());
			
			if (!online) {
				OnlineSectioningServer server = getStudentSolver();
				if (server == null) 
					throw new SectioningException(MSG.exceptionNoSolver());
				return server.execute(server.createAction(GetRequest.class).forStudent(studentId), currentUser());
			}
			
			OnlineSectioningServer server = getServerInstance(sessionId);
			if (server != null) {
				CourseRequestInterface lastRequest = server.execute(server.createAction(GetRequest.class).forStudent(studentId), currentUser());
				if (lastRequest == null)
					throw new SectioningException(MSG.exceptionBadStudentId());
				if (!lastRequest.getCourses().isEmpty())
					return lastRequest;
				throw new SectioningException(MSG.exceptionNoRequests());
			} else {
				org.hibernate.Session hibSession = StudentDAO.getInstance().getSession();
				try {
					Student student = StudentDAO.getInstance().get(studentId, hibSession);
					if (student == null) throw new SectioningException(MSG.exceptionBadStudentId());
					if (!student.getCourseDemands().isEmpty()) {
						request = new CourseRequestInterface();
						request.setAcademicSessionId(sessionId);
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
								if (lastRequest != null && lastRequestPriority == cd.getPriority()) {
									r = lastRequest;
									lastRequest.addRequestedFreeTime(ft);
									lastRequest.setRequestedCourse(lastRequest.getRequestedCourse() + ", " + ft.toString());
								} else {
									r = new CourseRequestInterface.Request();
									r.addRequestedFreeTime(ft);
									r.setRequestedCourse(ft.toString());
									if (cd.isAlternative())
										request.getAlternatives().add(r);
									else
										request.getCourses().add(r);
								}
							} else if (!cd.getCourseRequests().isEmpty()) {
								r = new CourseRequestInterface.Request();
								for (Iterator<CourseRequest> i = cd.getCourseRequests().iterator(); i.hasNext(); ) {
									CourseRequest course = i.next();
									XCourse c = (server == null ? new XCourse(course.getCourseOffering()) : server.getCourse(course.getCourseOffering().getUniqueId()));
									if (c == null) continue;
									switch (course.getOrder()) {
									case 0: 
										r.setRequestedCourse(c.getSubjectArea() + " " + c.getCourseNumber() + (c.hasUniqueName() ? "" : " - " + c.getTitle()));
										break;
									case 1:
										r.setFirstAlternative(c.getSubjectArea() + " " + c.getCourseNumber() + (c.hasUniqueName() ? "" : " - " + c.getTitle()));
										break;
									case 2:
										r.setSecondAlternative(c.getSubjectArea() + " " + c.getCourseNumber() + (c.hasUniqueName() ? "" : " - " + c.getTitle()));
									}
								}
								if (r.hasRequestedCourse()) {
									if (cd.isAlternative())
										request.getAlternatives().add(r);
									else
										request.getCourses().add(r);
								}
							}
							r.setWaitList(cd.getWaitlist());
							lastRequest = r;
							lastRequestPriority = cd.getPriority();
						}
						if (!request.getCourses().isEmpty()) return request;
					}
					if (!student.getClassEnrollments().isEmpty()) {
						TreeSet<XCourse> courses = new TreeSet<XCourse>();
						for (Iterator<StudentClassEnrollment> i = student.getClassEnrollments().iterator(); i.hasNext(); ) {
							StudentClassEnrollment enrl = i.next();
							XCourse c = (server == null ? new XCourse(enrl.getCourseOffering()) : server.getCourse(enrl.getCourseOffering().getUniqueId()));
							if (c != null)  courses.add(c);
						}
						request = new CourseRequestInterface();
						request.setAcademicSessionId(sessionId);
						for (XCourse c: courses) {
							CourseRequestInterface.Request r = new CourseRequestInterface.Request();
							r.setRequestedCourse(c.getSubjectArea() + " " + c.getCourseNumber() + (c.hasUniqueName() ? "" : " - " + c.getTitle()));
							request.getCourses().add(r);
						}
						if (!request.getCourses().isEmpty()) return request;
					}
					throw new SectioningException(MSG.exceptionNoRequests());				
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
		if (!request.getAcademicSessionId().equals(sessionId)) throw new SectioningException(MSG.exceptionBadSession());
		return request;
	}
	
	public ClassAssignmentInterface lastResult(boolean online, Long sessionId) throws SectioningException, PageAccessException {
		Long studentId = getStudentId(sessionId);
		if (studentId == null) throw new SectioningException(MSG.exceptionNoStudent());
		
		if (!online) {
			OnlineSectioningServer server = getStudentSolver();
			if (server == null) 
				throw new SectioningException(MSG.exceptionNoSolver());

			ClassAssignmentInterface ret = server.execute(server.createAction(GetAssignment.class).forStudent(studentId), currentUser());
			ret.setCanEnroll(false);
			return ret;
		}
		
		org.hibernate.Session hibSession = StudentDAO.getInstance().getSession();
		try {
			OnlineSectioningServer server = getServerInstance(sessionId);
			if (server == null) throw new SectioningException(MSG.exceptionBadSession());
			ClassAssignmentInterface ret = server.execute(server.createAction(GetAssignment.class).forStudent(studentId), currentUser());
			if (ret == null) throw new SectioningException(MSG.exceptionBadStudentId());
			ret.setCanEnroll(server.getAcademicSession().isSectioningEnabled());
			if (ret.isCanEnroll()) {
				if (getStudentId(sessionId) == null)
					ret.setCanEnroll(false);
			}
			if (!ret.getCourseAssignments().isEmpty()) return ret;
			/*
			Student student = StudentDAO.getInstance().get(studentId, hibSession);
			if (student == null) throw new SectioningException(SectioningExceptionType.BAD_STUDENT_ID);
			Lock lock = server.readLock();
			try {
				if (!student.getClassEnrollments().isEmpty()) {
					ArrayList<ClassAssignmentInterface.ClassAssignment> ret = new ArrayList<ClassAssignmentInterface.ClassAssignment>();
					for (Iterator<StudentClassEnrollment> i = student.getClassEnrollments().iterator(); i.hasNext(); ) {
						StudentClassEnrollment enrl = i.next();
						XCourse course = server.getCourseInfo(enrl.getCourseOffering().getUniqueId());
						Section section = server.getSection(enrl.getClazz().getUniqueId());
						if (course == null || section == null) continue;
						ClassAssignmentInterface.ClassAssignment ca = new ClassAssignmentInterface.ClassAssignment();
						ca.setCourseId(course.getUniqueId());
						ca.setClassId(section.getId());
						ca.setPinned(true);
						ca.setSubject(course.getSubjectArea());
						ca.setCourseNbr(course.getCourseNbr());
						ca.setSubpart(section.getSubpart().getName());
						ca.setSection(section.getName(course.getUniqueId()));
						ret.add(ca);
					}
					if (!ret.isEmpty()) return ret;
				}
			} finally {
				lock.release();
			}
			*/
			throw new SectioningException(MSG.exceptionNoSchedule());
		} catch (PageAccessException e) {
			throw e;
		} catch (SectioningException e) {
			throw e;
		} catch (Exception e) {
			throw new SectioningException(MSG.exceptionUnknown(e.getMessage()), e);
		} finally {
			hibSession.close();
		}
	}

	public Boolean saveRequest(CourseRequestInterface request) throws SectioningException, PageAccessException {
		OnlineSectioningServer server = getServerInstance(request.getAcademicSessionId());
		if (server != null) {
			if (server.getAcademicSession().isSectioningEnabled()) return false;
			if (ApplicationProperty.OnlineSchedulingSaveRequests.isFalse()) return false;
		}
		Long studentId = getStudentId(request.getAcademicSessionId());
		if (studentId == null && getSessionContext().hasPermission(Right.StudentSchedulingAdvisor))
			studentId = request.getStudentId();
		if (server != null) {
			if (studentId == null)
				throw new SectioningException(MSG.exceptionEnrollNotStudent(server.getAcademicSession().toString()));
			server.execute(server.createAction(SaveStudentRequests.class).forStudent(studentId).withRequest(request), currentUser());
			return true;
		} else {
			if (studentId == null)
				throw new SectioningException(MSG.exceptionEnrollNotStudent(SessionDAO.getInstance().get(request.getAcademicSessionId()).getLabel()));
			org.hibernate.Session hibSession = StudentDAO.getInstance().getSession();
			try {
				Student student = StudentDAO.getInstance().get(studentId, hibSession);
				if (student == null) throw new SectioningException(MSG.exceptionBadStudentId());
				SaveStudentRequests.saveRequest(null, new OnlineSectioningHelper(hibSession, currentUser()), student, request, true);
				hibSession.save(student);
				hibSession.flush();
				return true;
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
		if (request.getStudentId() == null) {
			Long sessionId = request.getAcademicSessionId();
			if (sessionId == null) sessionId = getLastSessionId();
			if (sessionId != null) request.setStudentId(getStudentId(sessionId));
		}
		Long sessionId = canEnroll(true, request.getStudentId());
		if (!request.getAcademicSessionId().equals(sessionId))
			throw new SectioningException(MSG.exceptionBadSession());
		
		OnlineSectioningServer server = getServerInstance(request.getAcademicSessionId());
		if (server == null) throw new SectioningException(MSG.exceptionBadStudentId());
		if (!server.getAcademicSession().isSectioningEnabled())
			throw new SectioningException(MSG.exceptionNotSupportedFeature());
		
		setLastSessionId(request.getAcademicSessionId());
		setLastRequest(request);

		return server.execute(server.createAction(EnrollStudent.class).forStudent(request.getStudentId()).withRequest(request).withAssignment(currentAssignment), currentUser());
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
			
			OnlineSectioningServer server = getServerInstance(offering.getControllingCourseOffering().getSubjectArea().getSessionId());
			
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
				Long offeringId = (clazz == null ? offering.getUniqueId() : clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getUniqueId());
				
				getSessionContext().checkPermission(offering != null ? offering : clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering(), Right.OfferingEnrollments);

				OnlineSectioningServer server = getServerInstance(
						clazz == null ?
						offering.getControllingCourseOffering().getSubjectArea().getSessionId() :
						clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getControllingCourseOffering().getSubjectArea().getSessionId()
						);
				
				if (server == null) {
					NameFormat nameFormat = NameFormat.fromReference(ApplicationProperty.OnlineSchedulingStudentNameFormat.value());
					Map<String, String> approvedBy2name = new Hashtable<String, String>();
					Hashtable<Long, ClassAssignmentInterface.Enrollment> student2enrollment = new Hashtable<Long, ClassAssignmentInterface.Enrollment>();
					boolean canShowExtIds = sessionContext.hasPermission(Right.EnrollmentsShowExternalId);
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
							st.setName(nameFormat.format(enrollment.getStudent()));
							for (AcademicAreaClassification ac: enrollment.getStudent().getAcademicAreaClassifications()) {
								st.addArea(ac.getAcademicArea().getAcademicAreaAbbreviation());
								st.addClassification(ac.getAcademicClassification().getCode());
							}
							for (PosMajor m: enrollment.getStudent().getPosMajors()) {
								st.addMajor(m.getCode());
							}
							for (StudentGroup g: enrollment.getStudent().getGroups()) {
								st.addGroup(g.getGroupAbbreviation());
							}
			    			for (StudentAccomodation a: enrollment.getStudent().getAccomodations()) {
			    				st.addAccommodation(a.getAbbreviation());
			    			}
							e = new ClassAssignmentInterface.Enrollment();
							e.setStudent(st);
							e.setEnrolledDate(enrollment.getTimestamp());
							CourseAssignment c = new CourseAssignment();
							c.setCourseId(enrollment.getCourseOffering().getUniqueId());
							c.setSubject(enrollment.getCourseOffering().getSubjectAreaAbbv());
							c.setCourseNbr(enrollment.getCourseOffering().getCourseNbr());
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
								e.setWaitList(enrollment.getCourseRequest().getCourseDemand().isWaitlist());
							} else {
								e.setPriority(-1);
							}
						}
						ClassAssignmentInterface.ClassAssignment c = e.getCourse().addClassAssignment();
						c.setClassId(enrollment.getClazz().getUniqueId());
						c.setSection(enrollment.getClazz().getClassSuffix(enrollment.getCourseOffering()));
						if (c.getSection() == null)
							c.setSection(enrollment.getClazz().getSectionNumberString(hibSession));
						c.setClassNumber(enrollment.getClazz().getSectionNumberString(hibSession));
						c.setSubpart(enrollment.getClazz().getSchedulingSubpart().getItypeDesc());
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
							st.setName(nameFormat.format(request.getCourseDemand().getStudent()));
							for (AcademicAreaClassification ac: request.getCourseDemand().getStudent().getAcademicAreaClassifications()) {
								st.addArea(ac.getAcademicArea().getAcademicAreaAbbreviation());
								st.addClassification(ac.getAcademicClassification().getCode());
							}
							for (PosMajor m: request.getCourseDemand().getStudent().getPosMajors()) {
								st.addMajor(m.getCode());
							}
							for (StudentGroup g: request.getCourseDemand().getStudent().getGroups()) {
								st.addGroup(g.getGroupAbbreviation());
							}
			    			for (StudentAccomodation a: request.getCourseDemand().getStudent().getAccomodations()) {
			    				st.addAccommodation(a.getAbbreviation());
			    			}
							e = new ClassAssignmentInterface.Enrollment();
							e.setStudent(st);
							CourseAssignment c = new CourseAssignment();
							c.setCourseId(request.getCourseOffering().getUniqueId());
							c.setSubject(request.getCourseOffering().getSubjectAreaAbbv());
							c.setCourseNbr(request.getCourseOffering().getCourseNbr());
							e.setCourse(c);
							e.setWaitList(request.getCourseDemand().isWaitlist());
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
						}
					return new ArrayList<ClassAssignmentInterface.Enrollment>(student2enrollment.values());
				} else {
					return server.execute(server.createAction(ListEnrollments.class)
							.forOffering(offeringId).withSection(clazz == null ? null : clazz.getUniqueId())
							.canShowExternalIds(sessionContext.hasPermission(Right.EnrollmentsShowExternalId)),
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
					OnlineSectioningServer server = getServerInstance(student.getSession().getUniqueId());
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
						Set<StudentClassEnrollment> enrollments = new TreeSet<StudentClassEnrollment>(cmp);
						enrollments.addAll(hibSession.createQuery(
								"from StudentClassEnrollment e where e.student.uniqueId = :studentId order by e.courseOffering.subjectAreaAbbv, e.courseOffering.courseNbr"
								).setLong("studentId", studentId).list());
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
								course.setWaitListed(enrollment.getCourseRequest() != null && enrollment.getCourseRequest().getCourseDemand().getWaitlist() != null && enrollment.getCourseRequest().getCourseDemand().getWaitlist().booleanValue());
								credit = enrollment.getCourseOffering().getCredit();
							}
							ClassAssignment clazz = course.addClassAssignment();
							clazz.setClassId(enrollment.getClazz().getUniqueId());
							clazz.setCourseId(enrollment.getCourseOffering().getUniqueId());
							clazz.setCourseAssigned(true);
							clazz.setCourseNbr(enrollment.getCourseOffering().getCourseNbr());
							clazz.setSubject(enrollment.getCourseOffering().getSubjectAreaAbbv());
							clazz.setSection(enrollment.getClazz().getClassSuffix(enrollment.getCourseOffering()));
							if (clazz.getSection() == null)
								clazz.setSection(enrollment.getClazz().getSectionNumberString(hibSession));
							clazz.setClassNumber(enrollment.getClazz().getSectionNumberString(hibSession));
							clazz.setSubpart(enrollment.getClazz().getSchedulingSubpart().getItypeDesc());
							if (enrollment.getClazz().getParentClass() != null) {
								clazz.setParentSection(enrollment.getClazz().getParentClass().getClassSuffix(enrollment.getCourseOffering()));
								if (clazz.getParentSection() == null)
									clazz.setParentSection(enrollment.getClazz().getParentClass().getSectionNumberString(hibSession));
							}
							if (enrollment.getClazz().getSchedulePrintNote() != null)
								clazz.addNote(enrollment.getClazz().getSchedulePrintNote());
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
							clazz.setLimit(new int[] { enrollment.getClazz().getEnrollment(), limit});
							if (placement != null) {
								if (placement.getTimeLocation() != null) {
									for (DayCode d : DayCode.toDayCodes(placement.getTimeLocation().getDayCode()))
										clazz.addDay(d.getIndex());
									clazz.setStart(placement.getTimeLocation().getStartSlot());
									clazz.setLength(placement.getTimeLocation().getLength());
									clazz.setBreakTime(placement.getTimeLocation().getBreakTime());
									clazz.setDatePattern(placement.getTimeLocation().getDatePatternName());
								}
								if (placement.getNrRooms() == 1) {
									clazz.addRoom(placement.getRoomLocation().getName());
								} else if (placement.getNrRooms() > 1) {
									for (RoomLocation rm: placement.getRoomLocations())
										clazz.addRoom(rm.getName());
								}
							}
							if (enrollment.getClazz().getDisplayInstructor())
								for (ClassInstructor ci : enrollment.getClazz().getClassInstructors()) {
									if (!ci.isLead()) continue;
									clazz.addInstructor(nameFormat.format(ci.getInstructor()));
									clazz.addInstructoEmail(ci.getInstructor().getEmail() == null ? "" : ci.getInstructor().getEmail());
								}
							if (enrollment.getClazz().getSchedulingSubpart().getCredit() != null) {
								clazz.setCredit(enrollment.getClazz().getSchedulingSubpart().getCredit().creditAbbv() + "|" + enrollment.getClazz().getSchedulingSubpart().getCredit().creditText());
							} else if (credit != null) {
								clazz.setCredit(credit.creditAbbv() + "|" + credit.creditText());
							}
							credit = null;
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
								ret.add(course);
								course.setAssigned(false);
								course.setWaitListed(demand.getWaitlist() != null && demand.getWaitlist().booleanValue());
								course.setCourseId(request.getCourseOffering().getUniqueId());
								course.setCourseNbr(request.getCourseOffering().getCourseNbr());
								course.setSubject(request.getCourseOffering().getSubjectAreaAbbv());
								course.setTitle(request.getCourseOffering().getTitle());
								ClassAssignment clazz = course.addClassAssignment();
								clazz.setCourseId(request.getCourseOffering().getUniqueId());
								clazz.setCourseAssigned(false);
								clazz.setCourseNbr(request.getCourseOffering().getCourseNbr());
								clazz.setSubject(request.getCourseOffering().getSubjectAreaAbbv());
							}
						}
						return ret;
					} else {
						return server.execute(server.createAction(GetAssignment.class).forStudent(studentId), currentUser());
					}
				} finally {
					hibSession.close();
				}				
			} else {
				OnlineSectioningServer server = getStudentSolver();
				if (server == null) 
					throw new SectioningException(MSG.exceptionNoSolver());

				return server.execute(server.createAction(GetAssignment.class).forStudent(studentId), currentUser());
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
			
			OnlineSectioningServer server = getServerInstance(offering.getControllingCourseOffering().getSubjectArea().getSessionId());
			
			UserContext user = getSessionContext().getUser();
			String approval = new Date().getTime() + ":" + user.getExternalUserId() + ":" + user.getName();
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
			
			OnlineSectioningServer server = getServerInstance(offering.getControllingCourseOffering().getSubjectArea().getSessionId());
			
			UserContext user = getSessionContext().getUser();
			String approval = new Date().getTime() + ":" + user.getExternalUserId() + ":" + user.getName();
			
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

		if (getSessionContext().hasPermission(Right.HasRole)) return null;
		
		HashSet<Long> courseIds = new HashSet<Long>(CourseOfferingDAO.getInstance().getSession().createQuery(
				"select distinct c.uniqueId from CourseOffering c inner join c.instructionalOffering.coordinators i where " +
				"c.subjectArea.session.uniqueId = :sessionId and i.externalUniqueId = :extId")
				.setLong("sessionId", sessionId).setString("extId", user.getExternalUserId()).setCacheable(true).list());
		
		return courseIds;
	}
	
	private HashSet<Long> getApprovableCourses(Long sessionId) throws SectioningException, PageAccessException {
		UserContext user = getSessionContext().getUser();
		if (user == null)
			throw new PageAccessException(getSessionContext().isHttpSessionNew() ? MSG.exceptionHttpSessionExpired() : MSG.exceptionLoginRequired());

		HashSet<Long> courseIds = new HashSet<Long>(CourseOfferingDAO.getInstance().getSession().createQuery(
				"select distinct c.uniqueId from CourseOffering c inner join c.instructionalOffering.coordinators i where " +
				"c.subjectArea.session.uniqueId = :sessionId and c.consentType.reference = :reference and " +
				"i.externalUniqueId = :extId"
				).setLong("sessionId", sessionId).setString("reference", "IN").setString("extId", user.getExternalUserId()).setCacheable(true).list());
		
		if (!user.getCurrentAuthority().hasRight(Right.HasRole)) return courseIds;
		
		if (user.getCurrentAuthority().hasRight(Right.SessionIndependent))
			return new HashSet<Long>(CourseOfferingDAO.getInstance().getSession().createQuery(
					"select c.uniqueId from CourseOffering c where c.subjectArea.session.uniqueId = :sessionId and c.consentType is not null"
					).setLong("sessionId", sessionId).setCacheable(true).list());
		
		for (Department d: Department.getUserDepartments(user)) {
			courseIds.addAll(CourseOfferingDAO.getInstance().getSession().createQuery(
					"select distinct c.uniqueId from CourseOffering c where " +
					"c.subjectArea.department.uniqueId = :departmentId and c.consentType is not null"
					).setLong("departmentId", d.getUniqueId()).setCacheable(true).list());
		}
		
		return courseIds;
	}
	
	public List<EnrollmentInfo> findEnrollmentInfos(boolean online, String query, SectioningStatusFilterRpcRequest filter, Long courseId) throws SectioningException, PageAccessException {
		try {
			if (online) {
				Long sessionId = getStatusPageSessionId();
				
				OnlineSectioningServer server = getServerInstance(sessionId);
				if (server == null)
					throw new SectioningException(MSG.exceptionBadSession());
				
				if (getSessionContext().isAuthenticated())
					getSessionContext().getUser().setProperty("SectioningStatus.LastStatusQuery", query);
							
				return server.execute(server.createAction(FindEnrollmentInfoAction.class).withParams(
						query,
						courseId,
						getCoordinatingCourses(sessionId),
						query.matches("(?i:.*consent:[ ]?(todo|\\\"to do\\\").*)") ? getApprovableCourses(sessionId) : null)
						.withFilter(filter), currentUser()
				);				
			} else {
				OnlineSectioningServer server = getStudentSolver();
				if (server == null) 
					throw new SectioningException(MSG.exceptionNoSolver());

				if (getSessionContext().isAuthenticated())
					getSessionContext().getUser().setProperty("SectioningStatus.LastStatusQuery", query);
				
				return server.execute(server.createAction(FindEnrollmentInfoAction.class).withParams(query, courseId, null, null).withFilter(filter), currentUser());
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
			if (online) {
				Long sessionId = getStatusPageSessionId();
				
				OnlineSectioningServer server = getServerInstance(sessionId);
				if (server == null)
					throw new SectioningException(MSG.exceptionBadSession());
				
				if (getSessionContext().isAuthenticated())
					getSessionContext().getUser().setProperty("SectioningStatus.LastStatusQuery", query);
							
				return server.execute(server.createAction(FindStudentInfoAction.class).withParams(
						query,
						getCoordinatingCourses(sessionId),
						query.matches("(?i:.*consent:[ ]?(todo|\\\"to do\\\").*)") ? getApprovableCourses(sessionId) : null,
						sessionContext.hasPermission(Right.EnrollmentsShowExternalId))
						.withFilter(filter), currentUser()
				);
			} else {
				OnlineSectioningServer server = getStudentSolver();
				if (server == null) 
					throw new SectioningException(MSG.exceptionNoSolver());

				if (getSessionContext().isAuthenticated())
					getSessionContext().getUser().setProperty("SectioningStatus.LastStatusQuery", query);
				
				return server.execute(server.createAction(FindStudentInfoAction.class).withParams(query, null, null,
						sessionContext.hasPermission(Right.EnrollmentsShowExternalId)).withFilter(filter), currentUser());
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
				
				OnlineSectioningServer server = getServerInstance(sessionId);
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
			if (online) {
				Long sessionId = getStatusPageSessionId();
				
				OnlineSectioningServer server = getServerInstance(sessionId);
				if (server == null)
					throw new SectioningException(MSG.exceptionBadSession());
				
				if (getSessionContext().isAuthenticated())
					getSessionContext().getUser().setProperty("SectioningStatus.LastStatusQuery", query);
				
				return server.execute(server.createAction(FindEnrollmentAction.class).withParams(
						query, courseId, classId, 
						query.matches("(?i:.*consent:[ ]?(todo|\\\"to do\\\").*)") ? getApprovableCourses(sessionId).contains(courseId): false,
						sessionContext.hasPermission(Right.EnrollmentsShowExternalId)).withFilter(filter), currentUser());
			} else {
				OnlineSectioningServer server = getStudentSolver();
				if (server == null) 
					throw new SectioningException(MSG.exceptionNoSolver());
				
				if (getSessionContext().isAuthenticated())
					getSessionContext().getUser().setProperty("SectioningStatus.LastStatusQuery", query);
				
				return server.execute(server.createAction(FindEnrollmentAction.class).withParams(
						query, courseId, classId, false,
						sessionContext.hasPermission(Right.EnrollmentsShowExternalId)).withFilter(filter), currentUser());
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
	public String lastStatusQuery() throws SectioningException, PageAccessException {
		UserContext user = getSessionContext().getUser();
		if (user == null) throw new PageAccessException(
				getSessionContext().isHttpSessionNew() ? MSG.exceptionHttpSessionExpired() : MSG.exceptionLoginRequired());
		
		return user.getProperty("SectioningStatus.LastStatusQuery");
	}

	@Override
	public Long canEnroll(boolean online, Long studentId) throws SectioningException, PageAccessException {
		try {
			if (!online) {
				OnlineSectioningServer server = getStudentSolver();
				if (server == null) 
					throw new SectioningException(MSG.exceptionNoSolver());
				
				CourseRequestInterface request = server.execute(server.createAction(GetRequest.class).forStudent(studentId), currentUser());
				if (request == null)
					throw new SectioningException(MSG.exceptionBadStudentId());

				return server.getAcademicSession().getUniqueId();
			}
			
			EligibilityCheck check = checkEligibility(online, null, studentId, null, false);
			if (check == null || !check.hasFlag(EligibilityFlag.CAN_ENROLL))
				throw new SectioningException(check.getMessage());
			
			if (studentId.equals(getStudentId(check.getSessionId())))
				return check.getSessionId();
			
			if (getSessionContext().hasPermission(Right.StudentSchedulingAdvisor))
				return check.getSessionId();
			
			if (getStudentId(check.getSessionId()) == null)
				throw new PageAccessException(MSG.exceptionEnrollNotStudent(getServerInstance(check.getSessionId()).getAcademicSession().toString()));
			
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

	@Override
	public CourseRequestInterface savedRequest(boolean online, Long sessionId, Long studentId) throws SectioningException, PageAccessException {
		if (online) {
			OnlineSectioningServer server = getServerInstance(sessionId == null ? canEnroll(online, studentId) : sessionId);
			if (studentId == null) {
				studentId = getStudentId(sessionId);
				if (studentId == null) throw new SectioningException(MSG.exceptionNoStudent());
			}
			return server.execute(server.createAction(GetRequest.class).forStudent(studentId), currentUser());
		} else {
			OnlineSectioningServer server = getStudentSolver();
			if (server == null) 
				throw new SectioningException(MSG.exceptionNoSolver());
			if (studentId == null) {
				studentId = getStudentId(sessionId);
				if (studentId == null) throw new SectioningException(MSG.exceptionNoStudent());
			}

			return server.execute(server.createAction(GetRequest.class).forStudent(studentId), currentUser());
		}
	}

	@Override
	public ClassAssignmentInterface savedResult(boolean online, Long sessionId, Long studentId) throws SectioningException, PageAccessException {
		if (online) {
			OnlineSectioningServer server = getServerInstance(sessionId == null ? canEnroll(online, studentId) : sessionId);
			return server.execute(server.createAction(GetAssignment.class).forStudent(studentId), currentUser());
		} else {
			OnlineSectioningServer server = getStudentSolver();
			if (server == null) 
				throw new SectioningException(MSG.exceptionNoSolver());

			ClassAssignmentInterface ret = server.execute(server.createAction(GetAssignment.class).forStudent(studentId), currentUser());
			if (ret != null)
				ret.setCanEnroll(false);
			return ret;
		}
	}
	
	@Override
	public Boolean selectSession(Long sessionId) {
		getSessionContext().setAttribute("sessionId", sessionId);
		UserContext user = getSessionContext().getUser();
		if (user != null && user.getCurrentAuthority() != null) {
			List<? extends UserAuthority> authorities = user.getAuthorities(user.getCurrentAuthority().getRole(), new SimpleQualifier("Session", sessionId));
			if (!authorities.isEmpty()) user.setCurrentAuthority(authorities.get(0));
			else user.setCurrentAuthority(null);
		}
		return true;
	}

	@Override
	public Map<String, String> lookupStudentSectioningStates() throws SectioningException, PageAccessException {
		Map<String, String> ret = new HashMap<String, String>();
		ret.put("", "System Default (All Enabled)");
		for (StudentSectioningStatus s: StudentSectioningStatusDAO.getInstance().findAll()) {
			ret.put(s.getReference(), s.getLabel());
		}
		return ret;
	}

	@Override
	public Boolean sendEmail(Long studentId, String subject, String message, String cc) throws SectioningException, PageAccessException {
		try {
			OnlineSectioningServer server = getServerInstance(getStatusPageSessionId());
			if (server == null) throw new SectioningException(MSG.exceptionNoServerForSession());
			if (ApplicationProperty.OnlineSchedulingEmailConfirmation.isFalse())
				throw new SectioningException(MSG.exceptionStudentEmailsDisabled());
			getSessionContext().checkPermission(server.getAcademicSession(), Right.StudentSchedulingEmailStudent);
			StudentEmail email = server.createAction(StudentEmail.class).forStudent(studentId);
			email.setCC(cc);
			email.setEmailSubject(subject == null || subject.isEmpty() ? MSG.defaulSubject() : subject);
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
	public Boolean changeStatus(List<Long> studentIds, String ref) throws SectioningException, PageAccessException {
		try {
			OnlineSectioningServer server = getServerInstance(getStatusPageSessionId());
			if (server == null) throw new SectioningException(MSG.exceptionNoServerForSession());
			getSessionContext().checkPermission(server.getAcademicSession(), Right.StudentSchedulingChangeStudentStatus);
			return server.execute(server.createAction(ChangeStudentStatus.class).forStudents(studentIds).withStatus(ref), currentUser());
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
		UserContext user = getSessionContext().getUser();
		UniTimePrincipal principal = (UniTimePrincipal)getSessionContext().getAttribute("user");
		String pin = (String)getSessionContext().getAttribute("pin");
		if (user != null) {
			OnlineSectioningLog.Entity.Builder entity = OnlineSectioningLog.Entity.newBuilder()
					.setExternalId(user.getExternalUserId())
					.setName(user.getName() == null ? user.getUsername() : user.getName())
					.setType(getSessionContext().hasPermission(Right.StudentSchedulingAdvisor) ?
							OnlineSectioningLog.Entity.EntityType.MANAGER : OnlineSectioningLog.Entity.EntityType.STUDENT);
			if (pin != null) entity.addParameterBuilder().setKey("pin").setValue(pin);
			return entity.build();
		} else if (principal != null) {
			OnlineSectioningLog.Entity.Builder entity = OnlineSectioningLog.Entity.newBuilder()
					.setExternalId(principal.getExternalId())
					.setName(principal.getName())
					.setType(OnlineSectioningLog.Entity.EntityType.STUDENT);
			if (pin != null) entity.addParameterBuilder().setKey("pin").setValue(pin);
			return entity.build();
		} else {
			return null;
		}
		
	}
	
	@Override
	public List<SectioningAction> changeLog(String query) throws SectioningException, PageAccessException {
		Long sessionId = getStatusPageSessionId();
		OnlineSectioningServer server = getServerInstance(sessionId);
		if (server == null) throw new SectioningException(MSG.exceptionNoServerForSession());
		return server.execute(server.createAction(FindOnlineSectioningLogAction.class).forQuery(query), currentUser());
	}

	@Override
	public Boolean massCancel(List<Long> studentIds, String statusRef, String subject, String message, String cc) throws SectioningException, PageAccessException {
		try {
			OnlineSectioningServer server = getServerInstance(getStatusPageSessionId());
			if (server == null) throw new SectioningException(MSG.exceptionNoServerForSession());
			
			getSessionContext().checkPermission(server.getAcademicSession(), Right.StudentSchedulingMassCancel);
			
			org.hibernate.Session hibSession = StudentDAO.getInstance().getSession();
			StudentSectioningStatus status = (statusRef == null || statusRef.isEmpty() ? null : (StudentSectioningStatus)hibSession.createQuery(
					"from StudentSectioningStatus where reference = :ref").setString("ref", statusRef).uniqueResult());

			return server.execute(server.createAction(MassCancelAction.class).forStudents(studentIds).withStatus(status).withEmail(subject, message, cc), currentUser());
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
		private static final long serialVersionUID = 1L;
		private boolean iAllCourseTypes, iNoCourseType;
		private Set<String> iAllowedCourseTypes;
		
		public CourseMatcher(boolean allCourseTypes, boolean noCourseType, Set<String> allowedCourseTypes) {
			iAllCourseTypes = allCourseTypes; iNoCourseType = noCourseType; iAllowedCourseTypes = allowedCourseTypes;
		}
		
		public boolean isAllCourseTypes() { return iAllCourseTypes; }
		
		public boolean isNoCourseType() { return iNoCourseType; }
		
		public boolean hasAllowedCourseTypes() { return iAllowedCourseTypes != null && !iAllowedCourseTypes.isEmpty(); }
		
		public Set<String> getAllowedCourseTypes() { return iAllowedCourseTypes; }

		@Override
		public boolean match(XCourseId course) {
			return course != null && course.matchType(iAllCourseTypes, iNoCourseType, iAllowedCourseTypes);
		}
	}

	@Override
	public EligibilityCheck checkEligibility(boolean online, Long sessionId, Long studentId, String pin) throws SectioningException, PageAccessException {
		return checkEligibility(online, sessionId, studentId, pin, true);
	}
	
	public EligibilityCheck checkEligibility(boolean online, Long sessionId, Long studentId, String pin, boolean includeCustomCheck) throws SectioningException, PageAccessException {
		try {
			if (pin != null && !pin.isEmpty()) getSessionContext().setAttribute("pin", pin);
			
			if (!online) {
				OnlineSectioningServer server = getStudentSolver();
				if (server == null) 
					return new EligibilityCheck(MSG.exceptionNoSolver());
				
				EligibilityCheck check = new EligibilityCheck();
				check.setSessionId(server.getAcademicSession().getUniqueId());
				check.setStudentId(studentId);
				return check;
			}
			
			if (sessionId == null && studentId != null) {
				// guess session from student
				Student student = StudentDAO.getInstance().get(studentId);
				if (student != null)
					sessionId = student.getSession().getUniqueId();
			}
			if (sessionId == null) {
				// use last used session otherwise
				sessionId = getLastSessionId();
			} else {
				setLastSessionId(sessionId);
			}
			
			if (sessionId == null) return new EligibilityCheck(MSG.exceptionNoAcademicSession());
			
			OnlineSectioningServer server = getServerInstance(sessionId);
			if (server == null)
				return new EligibilityCheck(MSG.exceptionNoServerForSession());
			
			UserContext user = getSessionContext().getUser();
			if (user == null)
				return new EligibilityCheck(getSessionContext().isHttpSessionNew() ? MSG.exceptionHttpSessionExpired() : MSG.exceptionLoginRequired());

			if (studentId == null)
				studentId = getStudentId(sessionId);
			
			EligibilityCheck check = new EligibilityCheck();
			check.setFlag(EligibilityFlag.IS_ADMIN, getSessionContext().hasPermission(Right.StudentSchedulingAdmin));
			check.setFlag(EligibilityFlag.IS_ADVISOR, getSessionContext().hasPermission(Right.StudentSchedulingAdvisor));
			check.setSessionId(sessionId);
			check.setStudentId(studentId);
			
			return server.execute(server.createAction(CheckEligibility.class).forStudent(studentId).withCheck(check).includeCustomCheck(includeCustomCheck), currentUser());
		} catch (Exception e) {
			sLog.error(e.getMessage(), e);
			return new EligibilityCheck(MSG.exceptionUnknown(e.getMessage()));
		}
	}

	@Override
	public void destroy() throws Exception {
		CustomStudentEnrollmentHolder.release();
	}

	@Override
	public SectioningProperties getProperties(Long sessionId) throws SectioningException, PageAccessException {
		SectioningProperties properties = new SectioningProperties();
		properties.setAdmin(getSessionContext().hasPermission(Right.StudentSchedulingAdmin));
		properties.setAdvisor(getSessionContext().hasPermission(Right.StudentSchedulingAdmin));
		if (sessionId == null && getSessionContext().getUser() != null)
			sessionId = getSessionContext().getUser().getCurrentAcademicSessionId();
		properties.setSessionId(sessionId);
		if (sessionId != null) {
			properties.setMassCancel(getSessionContext().hasPermission(sessionId, Right.StudentSchedulingMassCancel));
			properties.setEmail(getSessionContext().hasPermission(sessionId, Right.StudentSchedulingEmailStudent));
			properties.setChangeStatus(getSessionContext().hasPermission(sessionId, Right.StudentSchedulingChangeStudentStatus));
			properties.setRequestUpdate(getSessionContext().hasPermission(sessionId, Right.StudentSchedulingRequestStudentUpdate));
		}
		return properties;
	}

	@Override
	public Boolean requestStudentUpdate(List<Long> studentIds) throws SectioningException, PageAccessException {
		OnlineSectioningServer server = getServerInstance(getStatusPageSessionId());
		if (server == null) throw new SectioningException(MSG.exceptionNoServerForSession());
		
		getSessionContext().checkPermission(server.getAcademicSession(), Right.StudentSchedulingRequestStudentUpdate);
		
		return server.execute(server.createAction(RequestStudentUpdates.class).forStudents(studentIds), currentUser());
	}
}