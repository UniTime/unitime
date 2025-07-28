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
package org.unitime.timetable.onlinesectioning.updates;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.assignment.DefaultSingleAssignment;
import org.cpsolver.studentsct.model.Enrollment;
import org.cpsolver.studentsct.model.Request;
import org.cpsolver.studentsct.model.Section;
import org.hibernate.CacheMode;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.server.DayCode;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.ClassAssignment;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.CourseAssignment;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.ErrorMessage;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestedCourse;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.GradeMode;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.GradeModes;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.WaitListMode;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.model.AdvisorCourseRequest;
import org.unitime.timetable.model.ClassWaitList;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.CourseRequest;
import org.unitime.timetable.model.CourseRequest.CourseRequestOverrideIntent;
import org.unitime.timetable.model.CourseRequest.CourseRequestOverrideStatus;
import org.unitime.timetable.model.CourseRequestOption;
import org.unitime.timetable.model.FixedCreditUnitConfig;
import org.unitime.timetable.model.FreeTime;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.StudentEnrollmentMessage;
import org.unitime.timetable.model.StudentSectioningStatus.NotificationType;
import org.unitime.timetable.model.WaitList;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.onlinesectioning.HasCacheMode;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.Lock;
import org.unitime.timetable.onlinesectioning.basic.GetAssignment;
import org.unitime.timetable.onlinesectioning.custom.CustomStudentEnrollmentHolder;
import org.unitime.timetable.onlinesectioning.custom.Customization;
import org.unitime.timetable.onlinesectioning.custom.CriticalCoursesProvider.CriticalCourses;
import org.unitime.timetable.onlinesectioning.custom.CustomCriticalCoursesHolder;
import org.unitime.timetable.onlinesectioning.custom.StudentEnrollmentProvider.EnrollmentFailure;
import org.unitime.timetable.onlinesectioning.custom.StudentEnrollmentProvider.EnrollmentRequest;
import org.unitime.timetable.onlinesectioning.model.XConfig;
import org.unitime.timetable.onlinesectioning.model.XCourse;
import org.unitime.timetable.onlinesectioning.model.XCourseId;
import org.unitime.timetable.onlinesectioning.model.XCourseRequest;
import org.unitime.timetable.onlinesectioning.model.XEnrollment;
import org.unitime.timetable.onlinesectioning.model.XExpectations;
import org.unitime.timetable.onlinesectioning.model.XOffering;
import org.unitime.timetable.onlinesectioning.model.XRequest;
import org.unitime.timetable.onlinesectioning.model.XSection;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.model.XSubpart;
import org.unitime.timetable.onlinesectioning.solver.CheckAssignmentAction;
import org.unitime.timetable.onlinesectioning.solver.SectioningRequest;
import org.unitime.timetable.onlinesectioning.solver.FindAssignmentAction.IdPair;
import org.unitime.timetable.onlinesectioning.specreg.WaitListCheckValidation;
import org.unitime.timetable.onlinesectioning.specreg.WaitListSubmitOverrides;

/**
 * @author Tomas Muller
 */
public class EnrollStudent implements OnlineSectioningAction<ClassAssignmentInterface>, HasCacheMode {
	private static final long serialVersionUID = 1L;
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
	private Long iStudentId;
	private CourseRequestInterface iRequest;
	private List<ClassAssignmentInterface.ClassAssignment> iAssignment;
	private boolean iCheckWaitLists = false;
	
	public EnrollStudent forStudent(Long studentId) {
		iStudentId = studentId;
		return this;
	}
	
	public EnrollStudent withRequest(CourseRequestInterface request) {
		iRequest = request;
		return this;
	}

	public EnrollStudent withAssignment(List<ClassAssignmentInterface.ClassAssignment> assignment) {
		iAssignment = assignment;
		return this;
	}
	
	public EnrollStudent withWaitListCheck(boolean checkWaitLists) {
		iCheckWaitLists = checkWaitLists;
		return this;
	}

	public Long getStudentId() { return iStudentId; }
	public CourseRequestInterface getRequest() { return iRequest; }
	public List<ClassAssignmentInterface.ClassAssignment> getAssignment() { return iAssignment; }
	
	@Override
	public ClassAssignmentInterface execute(OnlineSectioningServer server, final OnlineSectioningHelper helper) {
		if (!server.getAcademicSession().isSectioningEnabled())
			throw new SectioningException(MSG.exceptionNotSupportedFeature());
		Set<Long> offeringIds = new HashSet<Long>();
		Set<Long> lockedCourses = new HashSet<Long>();
		List<EnrollmentFailure> failures = null;
		boolean includeRequestInTheReturnMessage = false;
		boolean rescheduling = server.getConfig().getPropertyBoolean("Enrollment.ReSchedulingEnabled", false) &&
				server.getConfig().getPropertyBoolean("Enrollment.WaitListLockedCourse", true);
		getRequest().removeDuplicates();
		for (ClassAssignmentInterface.ClassAssignment ca: getAssignment())
			if (ca != null && !ca.isFreeTime() && !ca.isDummy() && !ca.isTeachingAssignment()) {
				XCourse course = server.getCourse(ca.getCourseId());
				if (course == null)
					throw new SectioningException(MSG.exceptionEnrollNotAvailable(MSG.clazz(ca.getSubject(), ca.getCourseNbr(), ca.getSubpart(), ca.getSection())));
				if (server.isOfferingLocked(course.getOfferingId())) {
					lockedCourses.add(course.getCourseId());
					if (rescheduling) {
						XOffering offering = server.getOffering(course.getOfferingId());
						if (offering != null && offering.isWaitList()) {
							for (CourseRequestInterface.Request r: getRequest().getCourses())
								if (!r.isWaitList() && r.hasRequestedCourse()) {
									for (RequestedCourse rc: r.getRequestedCourse())
										if (rc.hasCourseId()) {
											if (rc.getCourseId().equals(course.getCourseId())) r.setWaitList(true);
										} else if (rc.hasCourseName()) {
											if (course.matchCourse(rc.getCourseName())) r.setWaitList(true);
										}
								}
						}
					}
				} else {
					offeringIds.add(course.getOfferingId());
				}
			}
		
		OnlineSectioningServer.ServerCallback<Boolean> offeringChecked = new OnlineSectioningServer.ServerCallback<Boolean>() {
			@Override
			public void onFailure(Throwable exception) {
				helper.error("Offering check failed: " + exception.getMessage(), exception);
			}
			@Override
			public void onSuccess(Boolean result) {
			}
		};
		
		CriticalCourses cc = null;
		boolean checkCritical = ApplicationProperty.EnrollmentCheckCritical.isTrue();
		try {
			if (CustomCriticalCoursesHolder.hasProvider() && checkCritical)
				cc = CustomCriticalCoursesHolder.getProvider().getCriticalCourses(server, helper, server.getStudent(getStudentId()));
		} catch (Exception e) {
			helper.warn("Failed to lookup critical courses: " + e.getMessage(), e);
		}
		WaitListMode wlMode = WaitListMode.None;
		boolean hasWaitList = false;
		boolean enrollmentChanged = false;
		
		Set<ErrorMessage> checkErrors = (getRequest().areTimeConflictsAllowed() || getRequest().areSpaceConflictsAllowed() || getRequest().areLinkedConflictsAllowed() ? new TreeSet<ErrorMessage>() : null);
		Lock lock = server.lockStudent(getStudentId(), offeringIds, name());
		GradeModes gradeModes = new GradeModes();
		try {
			helper.beginTransaction();
			try {
				OnlineSectioningLog.Action.Builder action = helper.getAction();
				
				if (getRequest().getStudentId() != null)
					action.setStudent(
							OnlineSectioningLog.Entity.newBuilder()
							.setUniqueId(getStudentId()));
				
				OnlineSectioningLog.Enrollment.Builder requested = OnlineSectioningLog.Enrollment.newBuilder();
				requested.setType(OnlineSectioningLog.Enrollment.EnrollmentType.REQUESTED);
				Map<Long, OnlineSectioningLog.CourseRequestOption.Builder> options = new Hashtable<Long, OnlineSectioningLog.CourseRequestOption.Builder>();
				for (ClassAssignmentInterface.ClassAssignment assignment: getAssignment())
					if (assignment != null) {
						OnlineSectioningLog.Section s = OnlineSectioningHelper.toProto(assignment); 
						requested.addSection(s);
						if (!assignment.isFreeTime() && !assignment.isDummy() && !assignment.isTeachingAssignment()) {
							OnlineSectioningLog.CourseRequestOption.Builder option = options.get(assignment.getCourseId());
							if (option == null) {
								option = OnlineSectioningLog.CourseRequestOption.newBuilder().setType(OnlineSectioningLog.CourseRequestOption.OptionType.ORIGINAL_ENROLLMENT);
								options.put(assignment.getCourseId(), option);
							}
							option.addSection(s);
						}
					}
				action.addEnrollment(requested);
				for (OnlineSectioningLog.Request r: OnlineSectioningHelper.toProto(getRequest()))
					action.addRequest(r);

				List<EnrollmentRequest> enrlCheck = server.createAction(CheckAssignmentAction.class).forStudent(getStudentId()).withAssignment(getAssignment()).check(server, helper, checkErrors);
				
				Student student = helper.getHibSession().createQuery(
						"select s from Student s " +
						"left join fetch s.courseDemands as cd " +
	                    "left join fetch cd.courseRequests as cr " +
	                    "left join fetch cd.freeTime as ft " +
	                    "left join fetch cr.courseOffering as co " +
	                    "left join fetch cr.courseRequestOptions as cro " +
	                    "left join fetch cr.classWaitLists as cwl " + 
	                    "left join fetch s.classEnrollments as e " +
	                    "left join fetch e.clazz as c " +
	                    "left join fetch c.managingDept as cmd " +
	                    "left join fetch c.schedulingSubpart as ss " +
						"where s.uniqueId = :studentId", Student.class).setParameter("studentId", getStudentId()).uniqueResult();
				if (student == null) throw new SectioningException(MSG.exceptionBadStudentId());
				wlMode = student.getWaitListMode();
				
				XStudent oldStudent = server.getStudent(getStudentId());

				action.getStudentBuilder().setUniqueId(student.getUniqueId())
					.setExternalId(oldStudent.getExternalId())
					.setName(oldStudent.getName());
				
				boolean hasWaitListedCourses = false;
				if (wlMode == WaitListMode.WaitList) {
					r: for (CourseRequestInterface.Request r: getRequest().getCourses()) {
						if (r.isWaitList()) {
							hasWaitListedCourses = true; break;
						}
						if (r.hasRequestedCourse())
							for (CourseRequestInterface.RequestedCourse rc: r.getRequestedCourse()) {
								if (rc.isCanWaitList()) {
									hasWaitListedCourses = true; break r;
								}
							}
					}
					if (!hasWaitListedCourses)
						for (XRequest r: oldStudent.getRequests())
							if (!r.isAlternative() && r instanceof XCourseRequest && ((XCourseRequest)r).isWaitlist()) {
								hasWaitListedCourses = true;
								break;
							}
					if (student.getOverrideExternalId() != null && student.getMaxCreditOverrideIntent() == CourseRequestOverrideIntent.WAITLIST && student.getMaxCreditOverrideStatus() == CourseRequestOverrideStatus.PENDING)
						hasWaitList = true; // may need to cancel the pending max credit override
				}
				
				if (CustomStudentEnrollmentHolder.hasProvider()) {
					failures = CustomStudentEnrollmentHolder.getProvider().enroll(server, helper, oldStudent, enrlCheck, lockedCourses, gradeModes, hasWaitListedCourses);
					for (Iterator<ClassAssignmentInterface.ClassAssignment> i = getAssignment().iterator(); i.hasNext(); ) {
						ClassAssignmentInterface.ClassAssignment ca = i.next();
						if (ca == null || ca.isFreeTime() || ca.getClassId() == null || ca.isDummy() || ca.isTeachingAssignment()) continue;
						for (EnrollmentFailure f: failures) {
							if (!f.isEnrolled() && f.getSection().getSectionId().equals(ca.getClassId())) {
								i.remove();
								break;
							}
						}
					}
					failures: for (EnrollmentFailure f: failures) {
						if (!f.isEnrolled()) continue;
						for (ClassAssignmentInterface.ClassAssignment ca: getAssignment())
							if (ca != null && f.getSection().getSectionId().equals(ca.getClassId())) continue failures;
						ClassAssignment ca = new ClassAssignment();
						ca.setClassId(f.getSection().getSectionId());
						ca.setCourseId(f.getCourse().getCourseId());
						getAssignment().add(ca);
					}
				}
				
				Map<IdPair, StudentClassEnrollment> oldEnrollments = new HashMap<IdPair, StudentClassEnrollment>();
				Map<Long, Object[]> oldApprovals = new HashMap<Long, Object[]>();
				for (StudentClassEnrollment e: student.getClassEnrollments()) {
					oldEnrollments.put(new IdPair(e.getCourseOffering().getUniqueId(), e.getClazz().getUniqueId()), e);
					if (e.getApprovedBy() != null && !oldApprovals.containsKey(e.getCourseOffering().getUniqueId())) {
						oldApprovals.put(e.getCourseOffering().getUniqueId(), new Object[] {e.getApprovedBy(), e.getApprovedDate()});
					}
				}
				
				Map<Long, Class_> classes = new HashMap<Long, Class_>();
				String classIds = null;
				for (ClassAssignmentInterface.ClassAssignment ca: getAssignment()) {
					if (ca == null || ca.isFreeTime() || ca.getClassId() == null || ca.isDummy() || ca.isTeachingAssignment() || oldEnrollments.containsKey(new IdPair(ca.getCourseId(), ca.getClassId()))) continue;
					if (classIds == null)
						classIds = ca.getClassId().toString();
					else
						classIds += "," + ca.getClassId();
				}
				if (classIds != null) {
					for (Class_ clazz: helper.getHibSession().createQuery(
							"select c from Class_ c " +
							"left join fetch c.studentEnrollments as e " +
							"left join fetch c.schedulingSubpart as s " +
							"where c.uniqueId in (" + classIds + ")", Class_.class).list()) {
						classes.put(clazz.getUniqueId(), clazz);
					}
				}
				Map<Long, Long> courseDemandId2courseId = new HashMap<Long, Long>();
				
				Set<CourseDemand> remaining = new TreeSet<CourseDemand>(student.getCourseDemands());
				int priority = 0;
				Date ts = new Date();
				Map<Long, CourseRequest> course2request = new HashMap<Long, CourseRequest>();
				Map<Long, CourseDemand> alt2demand = new HashMap<Long, CourseDemand>();
				for (CourseRequestInterface.Request r: getRequest().getCourses()) {
					List<XCourseId> courses = new ArrayList<XCourseId>();
					if (r.hasRequestedCourse()) {
						for (RequestedCourse rc: r.getRequestedCourse()) {
							if (rc.isFreeTime()) {
								for (CourseRequestInterface.FreeTime ft: rc.getFreeTime()) {
									CourseDemand cd = null;
									for (Iterator<CourseDemand> i = remaining.iterator(); i.hasNext(); ) {
										CourseDemand adept = i.next();
										if (adept.getFreeTime() == null) continue;
										cd = adept; i.remove(); break;
									}
									if (cd == null) {
										cd = new CourseDemand();
										cd.setTimestamp(ts);
										cd.setChangedBy(helper.getUser() == null ? null : helper.getUser().getExternalId());
										student.getCourseDemands().add(cd);
										cd.setStudent(student);
									}
									cd.setAlternative(false);
									cd.setPriority(priority);
									cd.setWaitlist(false);
									cd.setNoSub(false);
									cd.setCritical(0);
									FreeTime free = cd.getFreeTime();
									if (free == null) {
										free = new FreeTime();
										cd.setFreeTime(free);
									}
									free.setCategory(0);
									free.setDayCode(DayCode.toInt(DayCode.toDayCodes(ft.getDays())));
									free.setStartSlot(ft.getStart());
									free.setLength(ft.getLength());
									free.setSession(student.getSession());
									free.setName(ft.toString());
									if (free.getUniqueId() == null)
										helper.getHibSession().persist(free);
									else
										helper.getHibSession().merge(free);
									if (cd.getUniqueId() == null)
										helper.getHibSession().persist(cd);
									else
										helper.getHibSession().merge(cd);
								}
								priority++;
							} else {
								XCourseId c = server.getCourse(rc.getCourseId(), rc.getCourseName());
								if (c != null)
									courses.add(c);
							}
						}
					}
					if (courses.isEmpty()) continue;
					
					CourseDemand cd = null;
					adepts: for (Iterator<CourseDemand> i = remaining.iterator(); i.hasNext(); ) {
						CourseDemand adept = i.next();
						if (adept.getFreeTime() != null) continue;
						for (CourseRequest cr: adept.getCourseRequests())
							if (cr.getCourseOffering().getUniqueId().equals(courses.get(0).getCourseId())) {
								cd = adept; i.remove();  break adepts;
							}
					}
					if (cd == null) {
						cd = new CourseDemand();
						cd.setTimestamp(ts);
						cd.setChangedBy(helper.getUser() == null ? null : helper.getUser().getExternalId());
						cd.setCourseRequests(new HashSet<CourseRequest>());
						cd.setStudent(student);
						cd.setEnrollmentMessages(new HashSet<StudentEnrollmentMessage>());
						student.getCourseDemands().add(cd);
					} else {
						for (Iterator<StudentEnrollmentMessage> i = cd.getEnrollmentMessages().iterator(); i.hasNext(); ) {
							StudentEnrollmentMessage message = i.next();
							helper.getHibSession().remove(message);
							i.remove();
						}
					}
					cd.setAlternative(false);
					cd.setPriority(priority);
					Boolean oldWaitList = cd.isWaitlist();
					if (Boolean.TRUE.equals(cd.isWaitlist()) || r.isWaitList()) hasWaitList = true;
					if (r.isWaitList() && !Boolean.TRUE.equals(cd.getWaitlist()))
						cd.setWaitlistedTimeStamp(ts);
					if (r.isWaitList()) {
						cd.setWaitListSwapWithCourseOffering(r.getWaitListSwapWithCourseOfferingId() == null ? null : CourseOfferingDAO.getInstance().get(r.getWaitListSwapWithCourseOfferingId(), helper.getHibSession()));
					} else {
						cd.setWaitListSwapWithCourseOffering(null);
					}
					cd.setWaitlist(r.isWaitList());
					cd.setNoSub(r.isNoSub());
					if (checkCritical) cd.setCritical(isCritical(courses, cc));
					Iterator<CourseRequest> requests = new TreeSet<CourseRequest>(cd.getCourseRequests()).iterator();
					int order = 0;
					for (XCourseId co: courses) {
						CourseRequest cr = null;
						if (requests.hasNext()) {
							cr = requests.next();
							if (cr.getClassWaitLists() != null)
								for (Iterator<ClassWaitList> i = cr.getClassWaitLists().iterator(); i.hasNext(); ) {
									helper.getHibSession().remove(i.next());
									i.remove();
								}
						} else {
							cr = new CourseRequest();
							cd.getCourseRequests().add(cr);
							cr.setCourseDemand(cd);
							cr.setCourseRequestOptions(new HashSet<CourseRequestOption>());
						}
						cr.updateCourseRequestOption(OnlineSectioningLog.CourseRequestOption.OptionType.ORIGINAL_ENROLLMENT, options.get(co.getCourseId()));
						cr.setAllowOverlap(false);
						cr.setCredit(0);
						cr.setOrder(order++);
						if (cr.getCourseOffering() == null || !cr.getCourseOffering().getUniqueId().equals(co.getCourseId())) {
							cr.setCourseOffering(CourseOfferingDAO.getInstance().get(co.getCourseId(), helper.getHibSession()));
							if (r.isWaitList() && Customization.WaitListValidationProvider.hasProvider()) {
								cr.setCourseRequestOverrideStatus(CourseRequestOverrideStatus.NOT_CHECKED);
								cr.setCourseRequestOverrideIntent(CourseRequestOverrideIntent.WAITLIST);
								cr.setOverrideExternalId("TBD");
								cr.setOverrideTimeStamp(ts);
							}
						}
						if (Boolean.TRUE.equals(cd.getWaitlist()) && !Boolean.TRUE.equals(oldWaitList) && Customization.WaitListValidationProvider.hasProvider()) {
							cr.setCourseRequestOverrideStatus(CourseRequestOverrideStatus.NOT_CHECKED);
							cr.setCourseRequestOverrideIntent(CourseRequestOverrideIntent.WAITLIST);
							cr.setOverrideExternalId("TBD");
							cr.setOverrideTimeStamp(ts);
						}
						if (!Boolean.TRUE.equals(cd.getWaitlist()) && cr.getCourseRequestOverrideIntent() == CourseRequestOverrideIntent.WAITLIST) {
							cr.setOverrideStatus(null);
							cr.setOverrideExternalId(null);
							cr.setOverrideTimeStamp(null);
							cr.setOverrideIntent(null);
						}
						cr.updatePreferences(r.getRequestedCourse(co.getCourseId()), helper.getHibSession());
						
						if (failures != null) {
							String message = null;
							for (EnrollmentFailure f:failures) {
								if (co.getCourseId().equals(f.getCourse().getCourseId()))
									if (message == null)
										message = f.getMessage();
									else if (!message.contains(f.getMessage()))
										message += "\n" + f.getMessage();
							}
							if (message != null && !message.isEmpty()) {
								StudentEnrollmentMessage m = new StudentEnrollmentMessage();
								m.setCourseDemand(cd);
								m.setLevel(0);
								m.setType(0);
								m.setTimestamp(ts);
								m.setMessage(message.length() > 255 ? message.substring(0, 252) + "..." : message);
								m.setOrder(0);
								cd.getEnrollmentMessages().add(m);
							}
						}
					}
					while (requests.hasNext()) {
						CourseRequest cr = requests.next();
						cd.getCourseRequests().remove(cr);
						helper.getHibSession().remove(cr);
					}
					if (cd.getUniqueId() == null)
						helper.getHibSession().persist(cd);
					else
						helper.getHibSession().merge(cd);
					for (CourseRequest cr: cd.getCourseRequests())
						course2request.put(cr.getCourseOffering().getUniqueId(), cr);
					
					if (helper.isAlternativeCourseEnabled() && cd.getCourseRequests().size() == 1) {
						CourseOffering alt = cd.getCourseRequests().iterator().next().getCourseOffering().getAlternativeOffering();
						if (alt != null) alt2demand.put(alt.getUniqueId(), cd);
					}
					priority++;
				}
				
				for (CourseRequestInterface.Request r: getRequest().getAlternatives()) {
					List<XCourseId> courses = new ArrayList<XCourseId>();
					if (r.hasRequestedCourse()) {
						for (RequestedCourse rc: r.getRequestedCourse()) {
							if (rc.isFreeTime()) {
								for (CourseRequestInterface.FreeTime ft: rc.getFreeTime()) {
									CourseDemand cd = null;
									for (Iterator<CourseDemand> i = remaining.iterator(); i.hasNext(); ) {
										CourseDemand adept = i.next();
										if (adept.getFreeTime() == null) continue;
										cd = adept; i.remove(); break;
									}
									if (cd == null) {
										cd = new CourseDemand();
										cd.setTimestamp(ts);
										cd.setChangedBy(helper.getUser() == null ? null : helper.getUser().getExternalId());
										student.getCourseDemands().add(cd);
										cd.setStudent(student);
									}
									cd.setAlternative(true);
									cd.setPriority(priority);
									cd.setWaitlist(false);
									cd.setNoSub(false);
									cd.setCritical(0);
									FreeTime free = cd.getFreeTime();
									if (free == null) {
										free = new FreeTime();
										cd.setFreeTime(free);
									}
									free.setCategory(0);
									free.setDayCode(DayCode.toInt(DayCode.toDayCodes(ft.getDays())));
									free.setStartSlot(ft.getStart());
									free.setLength(ft.getLength());
									free.setSession(student.getSession());
									free.setName(ft.toString());
									if (free.getUniqueId() == null)
										helper.getHibSession().persist(free);
									else
										helper.getHibSession().merge(free);
									if (cd.getUniqueId() == null)
										helper.getHibSession().persist(cd);
									else
										helper.getHibSession().merge(cd);
								}
								priority ++;
							} else if (rc.isCourse()) {
								XCourseId c = server.getCourse(rc.getCourseId(), rc.getCourseName());
								if (c != null)
									courses.add(c);
							}
						}
					}
					if (courses.isEmpty()) continue;
					
					CourseDemand cd = null;
					adepts: for (Iterator<CourseDemand> i = remaining.iterator(); i.hasNext(); ) {
						CourseDemand adept = i.next();
						if (adept.getFreeTime() != null) continue;
						for (CourseRequest cr: adept.getCourseRequests())
							if (cr.getCourseOffering().getUniqueId().equals(courses.get(0).getCourseId())) {
								cd = adept; i.remove();  break adepts;
							}
					}
					if (cd == null) {
						cd = new CourseDemand();
						cd.setTimestamp(ts);
						cd.setChangedBy(helper.getUser() == null ? null : helper.getUser().getExternalId());
						cd.setCourseRequests(new HashSet<CourseRequest>());
						cd.setStudent(student);
						student.getCourseDemands().add(cd);
					} else {
						for (Iterator<StudentEnrollmentMessage> i = cd.getEnrollmentMessages().iterator(); i.hasNext(); ) {
							StudentEnrollmentMessage message = i.next();
							helper.getHibSession().remove(message);
							i.remove();
						}
					}
					cd.setAlternative(true);
					cd.setPriority(priority);
					if (Boolean.TRUE.equals(cd.isWaitlist()) || r.isWaitList()) hasWaitList = true;
					if (r.isWaitList() && !Boolean.TRUE.equals(cd.getWaitlist()))
						cd.setWaitlistedTimeStamp(ts);
					if (r.isWaitList()) {
						cd.setWaitListSwapWithCourseOffering(r.getWaitListSwapWithCourseOfferingId() == null ? null : CourseOfferingDAO.getInstance().get(r.getWaitListSwapWithCourseOfferingId(), helper.getHibSession()));
					} else {
						cd.setWaitListSwapWithCourseOffering(null);
					}
					cd.setWaitlist(r.isWaitList());
					cd.setNoSub(r.isNoSub());
					cd.setCritical(0);
					Iterator<CourseRequest> requests = new TreeSet<CourseRequest>(cd.getCourseRequests()).iterator();
					int order = 0;
					for (XCourseId co: courses) {
						CourseRequest cr = null;
						if (requests.hasNext()) {
							cr = requests.next();
							if (cr.getClassWaitLists() != null)
								for (Iterator<ClassWaitList> i = cr.getClassWaitLists().iterator(); i.hasNext(); ) {
									helper.getHibSession().remove(i.next());
									i.remove();
								}
						} else {
							cr = new CourseRequest();
							cd.getCourseRequests().add(cr);
							cr.setCourseDemand(cd);
						}
						cr.updateCourseRequestOption(OnlineSectioningLog.CourseRequestOption.OptionType.ORIGINAL_ENROLLMENT, options.get(co.getCourseId()));
						cr.setAllowOverlap(false);
						cr.setCredit(0);
						cr.setOrder(order++);
						if (cr.getCourseOffering() == null || !cr.getCourseOffering().getUniqueId().equals(co.getCourseId())) {
							cr.setCourseOffering(CourseOfferingDAO.getInstance().get(co.getCourseId(), helper.getHibSession()));
							cr.setOverrideStatus(null);
							cr.setOverrideIntent(null);
							cr.setOverrideExternalId(null);
							cr.setOverrideTimeStamp(null);
						}
						
						cr.updatePreferences(r.getRequestedCourse(co.getCourseId()), helper.getHibSession());
					}
					while (requests.hasNext()) {
						CourseRequest cr = requests.next();
						cd.getCourseRequests().remove(cr);
						helper.getHibSession().remove(cr);
					}
					if (cd.getUniqueId() == null)
						helper.getHibSession().persist(cd);
					else
						helper.getHibSession().merge(cd);
					for (CourseRequest cr: cd.getCourseRequests())
						course2request.put(cr.getCourseOffering().getUniqueId(), cr);
					priority++;
				}
				
				if (cc != null && student.getAdvisorCourseRequests() != null) {
					for (AdvisorCourseRequest acr: student.getAdvisorCourseRequests()) {
						int crit = acr.isCritical(cc);
						if (acr.getCritical() == null || acr.getCritical().intValue() != crit) {
							acr.setCritical(crit); helper.getHibSession().merge(acr);
						}
					}
				}
				
				for (ClassAssignmentInterface.ClassAssignment ca: getAssignment()) {
					if (ca == null || ca.isFreeTime() || ca.getClassId() == null || ca.isDummy() || ca.isTeachingAssignment()) continue;
					CourseRequest cr = course2request.get(ca.getCourseId());
					if (cr == null) {
						CourseDemand cd = alt2demand.get(ca.getCourseId());
						if (cd == null) {
							adepts: for (Iterator<CourseDemand> i = remaining.iterator(); i.hasNext(); ) {
								CourseDemand adept = i.next();
								if (adept.getFreeTime() != null) continue;
								for (CourseRequest r: adept.getCourseRequests())
									if (r.getCourseOffering().getUniqueId().equals(ca.getCourseId())) {
										cd = adept; cr = r; i.remove(); break adepts;
									}
							}
							if (cd == null) {
								cd = new CourseDemand();
								cd.setTimestamp(ts);
								cd.setChangedBy(helper.getUser() == null ? null : helper.getUser().getExternalId());
								cd.setCourseRequests(new HashSet<CourseRequest>());
								cd.setStudent(student);
								student.getCourseDemands().add(cd);
							}						
							cd.setAlternative(false);
							cd.setPriority(priority++);
							if (Boolean.TRUE.equals(cd.isWaitlist())) hasWaitList = true;
							cd.setWaitlist(false);
							cd.setNoSub(false);
							if (checkCritical) cd.setCritical(isCritical(ca, cc));
						}
						if (cr == null) {
							cr = new CourseRequest();
							cd.getCourseRequests().add(cr);
							cr.setCourseDemand(cd);
							cr.updateCourseRequestOption(OnlineSectioningLog.CourseRequestOption.OptionType.ORIGINAL_ENROLLMENT, options.get(ca.getCourseId()));
							cr.setAllowOverlap(false);
							cr.setCredit(0);
							cr.setOrder(cd.getCourseRequests().size());
							cr.setCourseOffering(CourseOfferingDAO.getInstance().get(ca.getCourseId(), helper.getHibSession()));
						}
						if (cd.getUniqueId() == null)
							helper.getHibSession().persist(cd);
						else
							helper.getHibSession().merge(cd);
						course2request.put(ca.getCourseId(), cr);
						courseDemandId2courseId.put(cd.getUniqueId(), ca.getCourseId());
						includeRequestInTheReturnMessage = true;
					} else {
						Long courseId = courseDemandId2courseId.get(cr.getCourseDemand().getUniqueId());
						if (courseId == null)
							courseDemandId2courseId.put(cr.getCourseDemand().getUniqueId(), ca.getCourseId());
						else if (!courseId.equals(ca.getCourseId())) {
							cr.getCourseDemand().getCourseRequests().remove(cr);
							CourseDemand cd = new CourseDemand();
							cd.setTimestamp(ts);
							cd.setChangedBy(helper.getUser() == null ? null : helper.getUser().getExternalId());
							cd.setCourseRequests(new HashSet<CourseRequest>());
							cd.setStudent(student);
							student.getCourseDemands().add(cd);
							cd.setAlternative(false);
							cd.setPriority(priority++);
							cd.setWaitlist(false);
							cd.setNoSub(false);
							if (checkCritical) cd.setCritical(isCritical(ca, cc));
							cr.setCourseDemand(cd);
							cd.getCourseRequests().add(cr);
							if (cd.getUniqueId() == null)
								helper.getHibSession().persist(cd);
							else
								helper.getHibSession().merge(cd);
							courseDemandId2courseId.put(cd.getUniqueId(), ca.getCourseId());
							includeRequestInTheReturnMessage = true;
						}
					}
					
					StudentClassEnrollment enrl = oldEnrollments.remove(new IdPair(ca.getCourseId(), ca.getClassId()));
					if (enrl != null) {
						if (!cr.equals(enrl.getCourseRequest())) {
							enrl.setCourseRequest(cr);
							helper.getHibSession().merge(enrl);
						}
						continue;
					}

					Class_ clazz = classes.get(ca.getClassId());
					if (clazz == null) continue;

					if (lockedCourses.contains(ca.getCourseId())) {
						ClassWaitList cwl = new ClassWaitList();
						cwl.setClazz(clazz);
						cwl.setCourseRequest(cr);
						cwl.setStudent(student);
						cwl.setType(ClassWaitList.Type.LOCKED.ordinal());
						cwl.setTimestamp(ts);
						if (cr.getClassWaitLists() == null)
							cr.setClassWaitLists(new HashSet<ClassWaitList>());
						cr.getClassWaitLists().add(cwl);
						helper.getHibSession().persist(cwl);
						continue;
					}

					enrl = new StudentClassEnrollment();
					enrl.setClazz(clazz);
					enrl.setStudent(student);
					enrl.setCourseOffering(cr.getCourseOffering());
					clazz.getStudentEnrollments().add(enrl);
					student.getClassEnrollments().add(enrl);
					enrl.setTimestamp(ts);
					enrl.setChangedBy(helper.getUser() == null ? null : helper.getUser().getExternalId());
					Object[] approval = oldApprovals.get(ca.getCourseId());
					if (approval != null) {
						enrl.setApprovedBy((String)approval[0]);
						enrl.setApprovedDate((Date)approval[1]);
					}
					enrl.setCourseRequest(cr);
					helper.getHibSession().persist(enrl);
					enrollmentChanged = true;
				}
				
				for (CourseDemand cd: remaining) {
					if (cd.getFreeTime() != null)
						helper.getHibSession().remove(cd.getFreeTime());
					for (CourseRequest cr: cd.getCourseRequests())
						helper.getHibSession().remove(cr);
					if (Boolean.TRUE.equals(cd.isWaitlist())) hasWaitList = true;
					student.getCourseDemands().remove(cd);
					helper.getHibSession().remove(cd);
				}
				
				for (StudentClassEnrollment enrl: oldEnrollments.values()) {
					enrl.getClazz().getStudentEnrollments().remove(enrl);
					student.getClassEnrollments().remove(enrl);
					helper.getHibSession().remove(enrl);
					enrollmentChanged = true;
				}
				
				helper.getHibSession().merge(student);
				
				if (wlMode == WaitListMode.WaitList) {
					helper.getHibSession().flush();
					student.resetWaitLists(
							WaitList.WaitListType.SCHEDULING_ASSISTANT,
							helper.getUser().getExternalId(),
							ts,
							helper.getHibSession());
				}
				
				// Reload student
				XStudent newStudent = new XStudent(oldStudent, student.getCourseDemands(), helper, server.getAcademicSession().getFreeTimePattern());
		    	for (Iterator<XRequest> i = newStudent.getRequests().iterator(); i.hasNext(); ) {
		    		XRequest request = i.next();
		    		if (request instanceof XCourseRequest) {
		    			XCourseRequest courseRequest = (XCourseRequest)request;
		    			for (Iterator<XCourseId> j = courseRequest.getCourseIds().iterator(); j.hasNext(); ) {
		    				XCourseId course = j.next();
		    				XOffering offering = server.getOffering(course.getOfferingId());
		                    if (offering == null) {
		                    	helper.warn("Student " + helper.getStudentNameFormat().format(student) + " (" + student.getExternalUniqueId() + ") requests course " + course.getCourseName() + " that is not loaded.");
		                    	j.remove();
		                    }
		    			}
		    			if (courseRequest.getCourseIds().isEmpty()) {
		    				i.remove();
		    				continue;
		    			}
		    			XEnrollment enrollment = courseRequest.getEnrollment();
		    			if (enrollment != null && enrollment.getReservation() == null) {
		    				XOffering offering = server.getOffering(enrollment.getOfferingId());
		    				if (offering != null && !offering.getReservations().isEmpty())
		    					enrollment.setReservation(offering.guessReservation(server.getRequests(enrollment.getOfferingId()), newStudent, enrollment));
		    			}
		    		}
		    	}
		    	server.update(newStudent, true);

				for (XRequest oldRequest: oldStudent.getRequests()) {
					XEnrollment oldEnrollment = (oldRequest instanceof XCourseRequest ? ((XCourseRequest)oldRequest).getEnrollment() : null);
					if (oldEnrollment == null) continue; // free time or not assigned
					XCourseRequest newRequest = null;
					XEnrollment newEnrollment = null;
					if (newStudent != null)
						for (XRequest r: newStudent.getRequests()) {
							XEnrollment e = (r instanceof XCourseRequest ? ((XCourseRequest)r).getEnrollment() : null);
							if (e != null && e.getOfferingId().equals(oldEnrollment.getOfferingId())) {
								newRequest = (XCourseRequest)r; newEnrollment = e; break;
							}
						}
					
					Set<Long> oldSections;
					if (newEnrollment == null) {
						oldSections = oldEnrollment.getSectionIds();
					} else {
						oldSections = new HashSet<Long>();
						for (Long sectionId: oldEnrollment.getSectionIds())
							if (!newEnrollment.getSectionIds().contains(sectionId))
								oldSections.add(sectionId);
					}
					
					if (oldSections.isEmpty()) continue; // no change detected
					
					XOffering offering = server.getOffering(oldEnrollment.getOfferingId());

					if (CheckOfferingAction.isCheckNeeded(server, helper, oldEnrollment, newEnrollment))
						server.execute(server.createAction(CheckOfferingAction.class).forOfferings(oldEnrollment.getOfferingId()).skipStudents(getStudentId()), helper.getUser(), offeringChecked);
					
					updateSpace(server,
							newEnrollment == null ? null : SectioningRequest.convert(newStudent, newRequest, server, offering, newEnrollment, wlMode, helper),
							oldEnrollment == null ? null : SectioningRequest.convert(oldStudent, (XCourseRequest)oldRequest, server, offering, oldEnrollment, wlMode, helper),
							offering);
					server.persistExpectedSpaces(oldEnrollment.getOfferingId());
				}
				OnlineSectioningLog.Enrollment.Builder previous = OnlineSectioningLog.Enrollment.newBuilder();
				previous.setType(OnlineSectioningLog.Enrollment.EnrollmentType.PREVIOUS);
				for (XRequest oldRequest: oldStudent.getRequests()) {
					XEnrollment oldEnrollment = (oldRequest instanceof XCourseRequest ? ((XCourseRequest)oldRequest).getEnrollment() : null);
					if (oldEnrollment != null)
						for (XSection section: server.getOffering(oldEnrollment.getOfferingId()).getSections(oldEnrollment))
							previous.addSection(OnlineSectioningHelper.toProto(section, oldEnrollment));
				}
				action.addEnrollment(previous);
				
				requests: for (XRequest newRequest: newStudent.getRequests()) {
					XEnrollment newEnrollment = (newRequest instanceof XCourseRequest ? ((XCourseRequest)newRequest).getEnrollment() : null);
					if (newEnrollment == null) continue; // free time or not assigned
					if (oldStudent != null)
						for (XRequest oldRequest: oldStudent.getRequests()) {
							XEnrollment oldEnrollment = (oldRequest instanceof XCourseRequest ? ((XCourseRequest)oldRequest).getEnrollment() : null);
							if (oldEnrollment != null && oldEnrollment.getOfferingId().equals(newEnrollment.getOfferingId()))
								continue requests;
						}
					XOffering offering = server.getOffering(newEnrollment.getOfferingId());
					updateSpace(server,
							SectioningRequest.convert(newStudent, (XCourseRequest)newRequest, server, offering, newEnrollment, wlMode, helper),
							null, offering);
					server.persistExpectedSpaces(newEnrollment.getOfferingId());
				}
				OnlineSectioningLog.Enrollment.Builder stored = OnlineSectioningLog.Enrollment.newBuilder();
				stored.setType(OnlineSectioningLog.Enrollment.EnrollmentType.STORED);
				for (XRequest newRequest: newStudent.getRequests()) {
					XEnrollment newEnrollment = (newRequest instanceof XCourseRequest ? ((XCourseRequest)newRequest).getEnrollment() : null);
					if (newEnrollment != null)
						for (XSection section: server.getOffering(newEnrollment.getOfferingId()).getSections(newEnrollment))
							stored.addSection(OnlineSectioningHelper.toProto(section, newEnrollment));
				}
				action.addEnrollment(stored);
				
				if (enrollmentChanged)
						server.execute(server.createAction(NotifyStudentAction.class)
								.forStudent(newStudent)
								.fromAction(name())
								.withType(helper.isAdmin() ? NotificationType.AdminChangeEnrollment : NotificationType.StudentChangeEnrollment)
								.skipWhenNoChange(true)
								.oldStudent(oldStudent), helper.getUser());
				helper.commitTransaction();
			} catch (Exception e) {
				helper.rollbackTransaction();
				if (e instanceof SectioningException) {
					SectioningException se = (SectioningException)e;
					if (checkErrors != null && !checkErrors.isEmpty())
						for (ErrorMessage em: checkErrors)
							se.addError(em);
					throw se;
				}
				helper.error("Failed to enroll student " + getStudentId() + ": " + e.getMessage(), e);
				throw new SectioningException(MSG.exceptionUnknown(e.getMessage()), e);
			}
		} finally {
			lock.release();
		}
		
		if (!includeRequestInTheReturnMessage) {
			if (ApplicationProperty.OnlineSchedulingMakeAssignedRequestReadOnly.isTrue())
				includeRequestInTheReturnMessage = true;
			else if (helper.getUser() != null && helper.getUser().getType() == OnlineSectioningLog.Entity.EntityType.MANAGER &&
				ApplicationProperty.OnlineSchedulingMakeAssignedRequestReadOnlyIfAdmin.isTrue())
				includeRequestInTheReturnMessage = true;
			else if (ApplicationProperty.StudentSchedulingAlternativeCourse.isTrue())
				includeRequestInTheReturnMessage = true;
			else if (wlMode == WaitListMode.WaitList && hasWaitList)
				includeRequestInTheReturnMessage = true;
		}
		
		ClassAssignmentInterface ret = server.execute(server.createAction(GetAssignment.class).forStudent(getStudentId()).withMessages(failures).withErrors(checkErrors).withRequest(includeRequestInTheReturnMessage).withWaitListMode(wlMode), helper.getUser());
		if (ret != null && gradeModes.hasGradeModes()) {
			for (CourseAssignment ca: ret.getCourseAssignments())
				for (ClassAssignment a: ca.getClassAssignments()) {
					GradeMode m = gradeModes.getGradeMode(a);
					if (m != null) a.setGradeMode(m);
				}
		}
		if (ret != null && gradeModes.hasCreditHours()) {
			for (CourseAssignment ca: ret.getCourseAssignments())
				for (ClassAssignment a: ca.getClassAssignments()) {
					Float credit = gradeModes.getCreditHour(a);
					a.setCreditHour(credit);
					if (credit != null) a.setCredit(FixedCreditUnitConfig.formatCredit(credit));
				}
		}
		if (ret != null && gradeModes.hasCurrentCredit())
			ret.setCurrentCredit(gradeModes.getCurrentCredit());
		if (iCheckWaitLists && hasWaitList && ret != null && ret.getRequest() != null && wlMode == WaitListMode.WaitList && Customization.WaitListValidationProvider.hasProvider()) {
			ret.getRequest().setWaitListChecks(server.execute(server.createAction(WaitListCheckValidation.class).withRequest(ret.getRequest()), helper.getUser()));
			if (ret.getRequest().hasWaitListChecks() && !ret.getRequest().getWaitListChecks().isConfirm() && !ret.getRequest().getWaitListChecks().isError())
				ret.setRequest(server.execute(server.createAction(WaitListSubmitOverrides.class).withRequest(ret.getRequest()).withCredit(ret.getRequest().getWaitListChecks().getMaxCreditNeeded()), helper.getUser()));
		}
		return ret;
	}
	
	public static int getLimit(Enrollment enrollment, Map<Long, XSection> sections) {
		Integer limit = null;
		for (Section s: enrollment.getSections()) {
			XSection section = sections.get(s.getId());
			if (section != null && section.getLimit() >= 0) {
				if (limit == null)
					limit = section.getLimit();
				else
					limit = Math.min(limit, section.getLimit());
			}
		}
		return (limit == null ? -1 : limit);
	}
	
	public static void updateSpace(OnlineSectioningServer server, Enrollment newEnrollment, Enrollment oldEnrollment, XOffering offering) {
		updateSpace(server, newEnrollment, oldEnrollment, offering, offering);
	}
	
    public static void updateSpace(OnlineSectioningServer server, Enrollment newEnrollment, Enrollment oldEnrollment, XOffering newOffering, XOffering oldOffering) {
    	if (newEnrollment == null && oldEnrollment == null) return;
    	XExpectations expectations = server.getExpectations((newEnrollment == null ? oldEnrollment : newEnrollment).getOffering().getId());
    	Assignment<Request, Enrollment> assignment = new DefaultSingleAssignment<Request, Enrollment>();
    	if (oldEnrollment != null) {
        	Map<Long, XSection> sections = new HashMap<Long, XSection>();
        	if (oldOffering != null)
            	for (XConfig config: oldOffering.getConfigs())
            		for (XSubpart subpart: config.getSubparts())
            			for (XSection section: subpart.getSections())
            				sections.put(section.getSectionId(), section);
            List<Enrollment> feasibleEnrollments = new ArrayList<Enrollment>();
            int totalLimit = 0;
            for (Enrollment enrl : oldEnrollment.getRequest().values(assignment)) {
            	if (!enrl.getCourse().equals(oldEnrollment.getCourse())) continue;
                boolean overlaps = false;
                for (Request otherRequest : oldEnrollment.getRequest().getStudent().getRequests()) {
                    if (otherRequest.equals(oldEnrollment.getRequest()) || !(otherRequest instanceof org.cpsolver.studentsct.model.CourseRequest))
                        continue;
                    Enrollment otherErollment = otherRequest.getInitialAssignment();
                    if (otherErollment == null)
                        continue;
                    if (enrl.isOverlapping(otherErollment)) {
                        overlaps = true;
                        break;
                    }
                }
                if (!overlaps) {
                    feasibleEnrollments.add(enrl);
                    if (totalLimit >= 0) {
                    	int limit = getLimit(enrl, sections);
                        if (limit < 0) totalLimit = -1;
                        else totalLimit += limit;
                    }
                }
            }
            double increment = 1.0 / (totalLimit > 0 ? totalLimit : feasibleEnrollments.size());
            for (Enrollment feasibleEnrollment : feasibleEnrollments)
                for (Section section : feasibleEnrollment.getSections()) {
                	if (totalLimit > 0) {
                		expectations.incExpectedSpace(section.getId(), increment * getLimit(feasibleEnrollment, sections));
                    } else {
                    	expectations.incExpectedSpace(section.getId(), increment);
                    }
                }
    	}
    	if (newEnrollment != null) {
        	Map<Long, XSection> sections = new HashMap<Long, XSection>();
        	if (newOffering != null)
            	for (XConfig config: newOffering.getConfigs())
            		for (XSubpart subpart: config.getSubparts())
            			for (XSection section: subpart.getSections())
            				sections.put(section.getSectionId(), section);
            for (Section section : newEnrollment.getSections())
                section.setSpaceHeld(section.getSpaceHeld() - 1.0);
            List<Enrollment> feasibleEnrollments = new ArrayList<Enrollment>();
            int totalLimit = 0;
            for (Enrollment enrl : newEnrollment.getRequest().values(assignment)) {
            	if (!enrl.getCourse().equals(newEnrollment.getCourse())) continue;
                boolean overlaps = false;
                for (Request otherRequest : newEnrollment.getRequest().getStudent().getRequests()) {
                    if (otherRequest.equals(newEnrollment.getRequest()) || !(otherRequest instanceof org.cpsolver.studentsct.model.CourseRequest))
                        continue;
					Enrollment otherErollment = assignment.getValue(otherRequest);
                    if (otherErollment == null)
                        continue;
                    if (enrl.isOverlapping(otherErollment)) {
                        overlaps = true;
                        break;
                    }
                }
                if (!overlaps) {
                    feasibleEnrollments.add(enrl);
                    if (totalLimit >= 0) {
                    	int limit = getLimit(enrl, sections);
                        if (limit < 0) totalLimit = -1;
                        else totalLimit += limit;
                    }
                }
            }
            double decrement = 1.0 / (totalLimit > 0 ? totalLimit : feasibleEnrollments.size());
            for (Enrollment feasibleEnrollment : feasibleEnrollments)
                for (Section section : feasibleEnrollment.getSections()) {
                	if (totalLimit > 0) {
                		expectations.incExpectedSpace(section.getId(), - decrement * getLimit(feasibleEnrollment, sections));
                    } else {
                    	expectations.incExpectedSpace(section.getId(), - decrement);
                    }
                }
    	}
    	server.update(expectations);
    }
    
    protected static int isCritical(List<XCourseId> courses, CriticalCourses critical) {
		if (critical == null) return 0;
		for (XCourseId co: courses)
			return critical.isCritical(co);
		return 0;
	}
    
    protected static int isCritical(ClassAssignmentInterface.ClassAssignment course, CriticalCourses critical) {
		if (critical == null) return 0;
		if (course == null || course.getCourseId() == null) return 0;
		return critical.isCritical(new XCourseId(null, course.getCourseId(), course.getSubject(), course.getCourseNbr()));
	}
    
	@Override
	public String name() {
		return "enroll";
	}

	@Override
	public CacheMode getCacheMode() {
		return CacheMode.IGNORE;
	}
}
