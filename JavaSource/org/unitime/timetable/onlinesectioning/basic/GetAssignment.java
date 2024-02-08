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
package org.unitime.timetable.onlinesectioning.basic;

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

import org.cpsolver.coursett.model.Placement;
import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.assignment.AssignmentComparator;
import org.cpsolver.ifs.assignment.AssignmentMap;
import org.cpsolver.ifs.util.DistanceMetric;
import org.cpsolver.studentsct.model.Config;
import org.cpsolver.studentsct.model.Course;
import org.cpsolver.studentsct.model.CourseRequest;
import org.cpsolver.studentsct.model.Enrollment;
import org.cpsolver.studentsct.model.FreeTimeRequest;
import org.cpsolver.studentsct.model.Request;
import org.cpsolver.studentsct.model.SctAssignment;
import org.cpsolver.studentsct.model.Section;
import org.cpsolver.studentsct.model.Subpart;
import org.cpsolver.studentsct.model.Unavailability;
import org.cpsolver.studentsct.online.expectations.OverExpectedCriterion;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.server.DayCode;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.ClassAssignment;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.CourseAssignment;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.ErrorMessage;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestedCourse;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestedCourseStatus;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.WaitListMode;
import org.unitime.timetable.interfaces.ExternalClassNameHelperInterface.HasGradableSubpart;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseCreditUnitConfig;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.FixedCreditUnitConfig;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.RoomPref;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.Lock;
import org.unitime.timetable.onlinesectioning.advisors.AdvisorGetCourseRequests;
import org.unitime.timetable.onlinesectioning.custom.CustomClassAttendanceProvider;
import org.unitime.timetable.onlinesectioning.custom.CustomClassAttendanceProvider.StudentClassAttendance;
import org.unitime.timetable.onlinesectioning.custom.CustomCourseRequestsValidationHolder;
import org.unitime.timetable.onlinesectioning.custom.Customization;
import org.unitime.timetable.onlinesectioning.custom.SpecialRegistrationDashboardUrlProvider;
import org.unitime.timetable.onlinesectioning.custom.SpecialRegistrationProvider;
import org.unitime.timetable.onlinesectioning.custom.StudentHoldsCheckProvider;
import org.unitime.timetable.onlinesectioning.custom.WaitListValidationProvider;
import org.unitime.timetable.onlinesectioning.custom.StudentEnrollmentProvider.EnrollmentError;
import org.unitime.timetable.onlinesectioning.custom.StudentEnrollmentProvider.EnrollmentFailure;
import org.unitime.timetable.onlinesectioning.model.XConfig;
import org.unitime.timetable.onlinesectioning.model.XCourse;
import org.unitime.timetable.onlinesectioning.model.XCourseId;
import org.unitime.timetable.onlinesectioning.model.XCourseRequest;
import org.unitime.timetable.onlinesectioning.model.XEnrollment;
import org.unitime.timetable.onlinesectioning.model.XEnrollments;
import org.unitime.timetable.onlinesectioning.model.XExpectations;
import org.unitime.timetable.onlinesectioning.model.XFreeTimeRequest;
import org.unitime.timetable.onlinesectioning.model.XInstructor;
import org.unitime.timetable.onlinesectioning.model.XOffering;
import org.unitime.timetable.onlinesectioning.model.XRequest;
import org.unitime.timetable.onlinesectioning.model.XRoom;
import org.unitime.timetable.onlinesectioning.model.XSection;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.model.XSubpart;
import org.unitime.timetable.onlinesectioning.model.XTime;
import org.unitime.timetable.onlinesectioning.solver.SectioningRequest;
import org.unitime.timetable.onlinesectioning.updates.WaitlistedOnlineSectioningAction;
import org.unitime.timetable.util.Formats;

/**
 * @author Tomas Muller
 */
public class GetAssignment extends WaitlistedOnlineSectioningAction<ClassAssignmentInterface>{
	private static final long serialVersionUID = 1L;
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
	private static StudentSectioningConstants CONSTANTS = Localization.create(StudentSectioningConstants.class);
	
	private Long iStudentId;
	private List<EnrollmentFailure> iMessages;
	private Set<ErrorMessage> iErrors;
	private boolean iIncludeRequest = false;
	private boolean iCustomCheck = false;
	private boolean iWaitListCheck = false;
	private boolean iIncludeAdvisorRequest = false;
	private boolean iCheckHolds = false;
	private boolean iGetSpecRegs = false;
	private WaitListMode iWaitListMode = null;
	private boolean iIncludeWaitListPosition = false;
	
	public GetAssignment forStudent(Long studentId) {
		iStudentId = studentId;
		return this;
	}
	
	public GetAssignment withMessages(List<EnrollmentFailure> messages) {
		iMessages = messages;
		return this;
	}
	
	public GetAssignment withErrors(Set<ErrorMessage> errors) {
		iErrors = errors;
		return this;
	}
	
	public GetAssignment withRequest(boolean includeRequest) {
		iIncludeRequest = includeRequest;
		return this;
	}
	
	public GetAssignment withWaitListPosition(boolean includeWaitListPosition) {
		iIncludeWaitListPosition = includeWaitListPosition;
		return this;
	}
	
	public GetAssignment withAdvisorRequest(boolean includeRequest) {
		iIncludeAdvisorRequest = includeRequest;
		return this;
	}
	
	public GetAssignment withCustomCheck(boolean customCheck) {
		iCustomCheck = customCheck;
		return this;
	}
	
	public GetAssignment withWaitListCheck(boolean waitListCheck) {
		iWaitListCheck = waitListCheck;
		return this;
	}
	
	public GetAssignment checkHolds(boolean check) {
		iCheckHolds = check;
		return this;
	}
	
	public GetAssignment withSpecialRegistrations(boolean specReg) {
		iGetSpecRegs = specReg;
		return this;
	}

	public GetAssignment withWaitListMode(WaitListMode wlMode) {
		iWaitListMode = wlMode;
		return this;
	}

	@Override
	public ClassAssignmentInterface execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		XStudent student = null;
		ClassAssignmentInterface ret = null;
		Lock lock = server.readLock();
		try {
			student = server.getStudent(iStudentId);
			if (student == null) return null;
			if (iWaitListMode == null)
				iWaitListMode = student.getWaitListMode(helper);
			
			ret = computeAssignment(server, helper, student, student.getRequests(), iMessages, iErrors, iIncludeRequest, iWaitListMode);
			
			if (iIncludeAdvisorRequest) {
				ret.setAdvisorRequest(AdvisorGetCourseRequests.getRequest(student, server, helper));
				ret.setAdvisorWaitListedCourseIds(student.getAdvisorWaitListedCourseIds(server));
				if (ret.hasAdvisorRequest())
					for (OnlineSectioningLog.Request log: OnlineSectioningHelper.toProto(ret.getAdvisorRequest()))
						helper.getAction().addRecommendation(log);
			}
		} finally {
			lock.release();
		}
		
		if (ret.hasRequest() && iCustomCheck && CustomCourseRequestsValidationHolder.hasProvider())
			CustomCourseRequestsValidationHolder.getProvider().check(server, helper, ret.getRequest());
		
		if (ret.hasRequest() && iWaitListCheck && iWaitListMode == WaitListMode.WaitList && Customization.WaitListValidationProvider.hasProvider()) {
			WaitListValidationProvider wp = Customization.WaitListValidationProvider.getProvider();
			wp.check(server, helper, ret.getRequest());
		}
		
		if (iCheckHolds && ret.hasRequest() && Customization.StudentHoldsCheckProvider.hasProvider()) {
			try {
				StudentHoldsCheckProvider provider = Customization.StudentHoldsCheckProvider.getProvider();
				ret.getRequest().setErrorMessage(provider.getStudentHoldError(server, helper, student));
			} catch (Exception e) {}
		}
		
		if (iCheckHolds && ret.hasRequest() && Customization.SpecialRegistrationDashboardUrlProvider.hasProvider()) {
			try {
				SpecialRegistrationDashboardUrlProvider provider = Customization.SpecialRegistrationDashboardUrlProvider.getProvider();
				ret.getRequest().setSpecRegDashboardUrl(provider.getDashboardUrl(server, helper, student));
			} catch (Exception e) {}
		}
		
		if (iGetSpecRegs && Customization.SpecialRegistrationProvider.hasProvider()) {
			try {
				SpecialRegistrationProvider sp = Customization.SpecialRegistrationProvider.getProvider();
				ret.setSpecialRegistrations(sp.retrieveAllRegistrations(server, helper, student));
			} catch (Exception e) {
				helper.warn("Failed to retrieve special registrations: " + e.getMessage(), e);
			}
		}
		
		return ret;
	}
	
	public static List<CourseSection> fillUnavailabilitiesIn(ClassAssignmentInterface ret, XStudent student, OnlineSectioningServer server, OnlineSectioningHelper helper, OnlineSectioningLog.Enrollment.Builder eb) {
		if (student.getExternalId() == null || student.getExternalId().isEmpty()) return null;
		List<CourseSection> sections = new ArrayList<CourseSection>();
		Collection<Long> offeringIds = server.getInstructedOfferings(student.getExternalId());
		if (offeringIds != null && !offeringIds.isEmpty()) {
			TreeSet<XOffering> offerings = new TreeSet<XOffering>(new Comparator<XOffering>() {
				@Override
				public int compare(XOffering o1, XOffering o2) {
					int cmp = o1.getName().compareTo(o2.getName());
					if (cmp != 0) return cmp;
					return o1.getOfferingId().compareTo(o2.getOfferingId());
				}
			});
			for (Long offeringId: offeringIds) {
				XOffering offering = server.getOffering(offeringId);
				if (offering != null)
					offerings.add(offering);
			}
			for (XOffering offering: offerings) {
				ClassAssignmentInterface.CourseAssignment ca = new ClassAssignmentInterface.CourseAssignment();
		    	XCourse course = offering.getControllingCourse();
				XEnrollments enrollments = server.getEnrollments(offering.getOfferingId());
				
		    	if (server.isOfferingLocked(course.getOfferingId()))
					ca.setLocked(true);
		    	ca.setCanWaitList(offering.isWaitList());
				ca.setAssigned(true);
				ca.setCourseId(course.getCourseId());
				ca.setSubject(course.getSubjectArea());
				ca.setCourseNbr(course.getCourseNumber());
				ca.setTitle(course.getTitle());
				ca.setTeachingAssignment(true);
				ca.setHasCrossList(offering.hasCrossList());
				ca.setCanWaitList(offering.isWaitList());
				
				Set<Long> added = new HashSet<Long>();
		    	for (XConfig config: offering.getConfigs())
					for (XSubpart subpart: config.getSubparts())
						for (XSection section: subpart.getSections()) {
							if (!section.isCancelled())
								for (XInstructor instructor: section.getAllInstructors())
									if (student.getExternalId().equals(instructor.getExternalId()) && added.add(section.getSectionId())) {
										if (eb != null)
											eb.addSection(OnlineSectioningHelper.toProto(section, null));
										ClassAssignmentInterface.ClassAssignment a = ca.addClassAssignment();
										a.setAlternative(false);
										a.setClassId(section.getSectionId());
										a.setSubpart(subpart.getName());
										a.setClassNumber(section.getName(-1l));
										a.setSection(section.getName(course.getCourseId()));
										a.setExternalId(section.getExternalId(course.getCourseId()));
										a.setCancelled(section.isCancelled());
										a.setLimit(new int[] {enrollments.countEnrollmentsForSection(section.getSectionId()), section.getLimit()});
										if (section.getTime() != null) {
											for (DayCode d : DayCode.toDayCodes(section.getTime().getDays()))
												a.addDay(d.getIndex());
											a.setStart(section.getTime().getSlot());
											a.setLength(section.getTime().getLength());
											a.setBreakTime(section.getTime().getBreakTime());
											a.setDatePattern(section.getTime().getDatePatternName());
										}
										if (section.getRooms() != null) {
											for (XRoom room: section.getRooms()) {
												a.addRoom(room.getUniqueId(), room.getName());
											}
										}
										for (XInstructor instr: section.getInstructors()) {
											a.addInstructor(instr.getName());
											a.addInstructoEmail(instr.getEmail() == null ? "" : instr.getEmail());
										}
										if (section.getParentId() != null)
											a.setParentSection(offering.getSection(section.getParentId()).getName(course.getCourseId()));
										a.setSubpartId(section.getSubpartId());
										a.setHasAlternatives(false);
										a.addNote(course.getNote());
										a.addNote(section.getNote());
										a.setCredit(subpart.getCredit(course.getCourseId()));
										a.setCreditRange(subpart.getCreditMin(course.getCourseId()), subpart.getCreditMax(course.getCourseId()));
										Float creditOverride = section.getCreditOverride(course.getCourseId());
										if (creditOverride != null) a.setCredit(FixedCreditUnitConfig.formatCredit(creditOverride));
										a.setTeachingAssignment(true);
										a.setInstructing(instructor.isInstructing());
										sections.add(new CourseSection(course, section));
								}
						}
		    	ret.add(ca);
			}
		}
		if (server.getConfig().getPropertyBoolean("General.CheckUnavailabilitiesFromOtherSessions", false)) {
			List<StudentClassEnrollment> enrollments = helper.getHibSession().createQuery(
					"select e2 " +
					"from Student s1 inner join s1.session z1, StudentClassEnrollment e2 inner join e2.student s2 inner join s2.session z2 " +
					"where s1.uniqueId = :studentId and s1.externalUniqueId = s2.externalUniqueId and " +
					" z1 != z2 and z1.sessionBeginDateTime <= z2.classesEndDateTime and z2.sessionBeginDateTime <= z1.classesEndDateTime",
					StudentClassEnrollment.class).setParameter("studentId", student.getStudentId()).list();
			if (!enrollments.isEmpty()) {
				Map<Long, CourseAssignment> courses = new HashMap<>();
				Comparator<StudentClassEnrollment> cmp = new Comparator<StudentClassEnrollment>() {
					public boolean isParent(SchedulingSubpart s1, SchedulingSubpart s2) {
						SchedulingSubpart p1 = s1.getParentSubpart();
						if (p1==null) return false;
						if (p1.equals(s2)) return true;
						return isParent(p1, s2);
					}

					@Override
					public int compare(StudentClassEnrollment a, StudentClassEnrollment b) {
						if (a.getCourseOffering().equals(b.getCourseOffering())) {
							SchedulingSubpart s1 = a.getClazz().getSchedulingSubpart();
							SchedulingSubpart s2 = b.getClazz().getSchedulingSubpart();
							if (isParent(s1, s2)) return 1;
							if (isParent(s2, s1)) return -1;
							int cmp = s1.getItype().compareTo(s2.getItype());
							if (cmp != 0) return cmp;
							return Double.compare(s1.getUniqueId(), s2.getUniqueId());
						} else {
							int cmp = a.getCourseOffering().getCourseName().compareTo(b.getCourseOffering().getCourseName());
							if (cmp != 0) return cmp;
							return a.getCourseOffering().getUniqueId().compareTo(b.getCourseOffering().getUniqueId());
						}
					}
				};
				Collections.sort(enrollments, cmp);
				HasGradableSubpart gs = null;
				if (ApplicationProperty.OnlineSchedulingGradableIType.isTrue() && Class_.getExternalClassNameHelper() != null && Class_.getExternalClassNameHelper() instanceof HasGradableSubpart)
					gs = (HasGradableSubpart) Class_.getExternalClassNameHelper();
				CourseCreditUnitConfig credit = null;
				XCourse xc = null;
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
						course.setAssigned(true);
						course.setTeachingAssignment(true);
						credit = enrollment.getCourseOffering().getCredit();
						xc = new XCourse(enrollment.getCourseOffering());
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
						clazz.setSection(enrollment.getClazz().getSectionNumberString(helper.getHibSession()));
					clazz.setExternalId(enrollment.getClazz().getExternalId(enrollment.getCourseOffering()));
					clazz.setClassNumber(enrollment.getClazz().getSectionNumberString(helper.getHibSession()));
					clazz.setSubpart(enrollment.getClazz().getSchedulingSubpart().getItypeDesc().trim());
					if (enrollment.getClazz().getParentClass() != null) {
						clazz.setParentSection(enrollment.getClazz().getParentClass().getClassSuffix(enrollment.getCourseOffering()));
						if (clazz.getParentSection() == null)
							clazz.setParentSection(enrollment.getClazz().getParentClass().getSectionNumberString(helper.getHibSession()));
					}
					if (enrollment.getCourseOffering().getScheduleBookNote() != null)
						clazz.addNote(enrollment.getCourseOffering().getScheduleBookNote());
					if (enrollment.getClazz().getSchedulePrintNote() != null)
						clazz.addNote(enrollment.getClazz().getSchedulePrintNote());
					Placement placement = enrollment.getClazz().getCommittedAssignment() == null ? null : enrollment.getClazz().getCommittedAssignment().getPlacement();
					int minLimit = enrollment.getClazz().getExpectedCapacity();
	            	int maxLimit = enrollment.getClazz().getMaxExpectedCapacity();
	            	int limit = maxLimit;
	            	if (minLimit < maxLimit && placement != null) {
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
							clazz.setDatePattern(XTime.datePatternName(enrollment.getClazz().getCommittedAssignment(), helper.getDatePatternFormat()));
						}
						if (enrollment.getClazz().getCommittedAssignment() != null)
							for (Location loc: enrollment.getClazz().getCommittedAssignment().getRooms())
								clazz.addRoom(loc.getUniqueId(), loc.getLabelWithDisplayName());
					} else {
						for (Iterator<?> i = enrollment.getClazz().effectivePreferences(RoomPref.class).iterator(); i.hasNext(); ) {
			        		RoomPref rp = (RoomPref)i.next();
			        		if (PreferenceLevel.sRequired.equals(rp.getPrefLevel().getPrefProlog())) {
			        			clazz.addRoom(rp.getRoom().getUniqueId(), rp.getRoom().getLabel());
			        		}
			        	}
						DatePattern pattern = enrollment.getClazz().effectiveDatePattern();
						if (pattern != null)
							clazz.setDatePattern(datePatternName(pattern, helper.getDatePatternFormat()));
					}
					if (enrollment.getClazz().getDisplayInstructor())
						for (ClassInstructor ci : enrollment.getClazz().getClassInstructors()) {
							if (!ci.isLead()) continue;
							clazz.addInstructor(helper.getInstructorNameFormat().format(ci.getInstructor()));
							clazz.addInstructoEmail(ci.getInstructor().getEmail() == null ? "" : ci.getInstructor().getEmail());
						}
					if (credit != null && gs != null && gs.isGradableSubpart(enrollment.getClazz().getSchedulingSubpart(), enrollment.getCourseOffering(), helper.getHibSession())) {
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
					clazz.setTeachingAssignment(true);
					sections.add(new CourseSection(xc, new XSection(enrollment.getClazz(), helper)));
				}
			}
		}
		
		return sections;
	}
	
	public ClassAssignmentInterface computeAssignment(OnlineSectioningServer server, OnlineSectioningHelper helper, XStudent student, List<XRequest> studentRequests, List<EnrollmentFailure> messages, Set<ErrorMessage> errors, boolean includeRequest, WaitListMode wlMode) {
		Formats.Format<Date> df = Formats.getDateFormat(Formats.Pattern.DATE_REQUEST);
		DistanceMetric m = server.getDistanceMetric();
		OverExpectedCriterion overExp = server.getOverExpectedCriterion();
		OnlineSectioningLog.Action.Builder action = helper.getAction();
		action.setStudent(OnlineSectioningLog.Entity.newBuilder().setUniqueId(student.getStudentId()));
		action.getStudentBuilder().setExternalId(student.getExternalId());
		action.getStudentBuilder().setName(student.getName());
        ClassAssignmentInterface ret = new ClassAssignmentInterface();
		int nrUnassignedCourses = 0, nrAssignedAlt = 0;
		OnlineSectioningLog.Enrollment.Builder stored = OnlineSectioningLog.Enrollment.newBuilder();
		stored.setType(OnlineSectioningLog.Enrollment.EnrollmentType.STORED);
		boolean setReadOnly = ApplicationProperty.OnlineSchedulingMakeAssignedRequestReadOnly.isTrue();
		if (helper.getUser() != null && helper.getUser().getType() == OnlineSectioningLog.Entity.EntityType.MANAGER)
			setReadOnly = ApplicationProperty.OnlineSchedulingMakeAssignedRequestReadOnlyIfAdmin.isTrue();
		boolean showWaitListPosition = iIncludeWaitListPosition || ApplicationProperty.OnlineSchedulingShowWaitListPosition.isTrue();
		
		List<CourseSection> unavailabilities = fillUnavailabilitiesIn(ret, student, server, helper, stored);
		CustomClassAttendanceProvider provider = Customization.CustomClassAttendanceProvider.getProvider();
		StudentClassAttendance attendance = (provider == null ? null : provider.getCustomClassAttendanceForStudent(StudentDAO.getInstance().get(student.getStudentId(), helper.getHibSession()), helper, null));
		Map<Long, Set<String>> wlOverlaps = null;
		
		float credit = 0f;
		if (student.getMaxCredit() != null)
			for (XRequest request: studentRequests) {
				if (request instanceof XCourseRequest) {
					XCourseRequest r = (XCourseRequest)request;
					XEnrollment enrollment = r.getEnrollment();
					if (enrollment != null) {
						XOffering offering = server.getOffering(enrollment.getOfferingId());
						XCourse course = offering.getCourse(enrollment);
						if (course != null) {
							Float c = course.getMinCredit();
							if (c != null) credit += c;
						}
						
					}
				}
			}
		
		for (XRequest request: studentRequests) {
			action.addRequest(OnlineSectioningHelper.toProto(request));
			ClassAssignmentInterface.CourseAssignment ca = new ClassAssignmentInterface.CourseAssignment();
			if (request instanceof XCourseRequest) {
				XCourseRequest r = (XCourseRequest)request;
				
				XEnrollment enrollment = r.getEnrollment();
				XCourseId courseId = (enrollment == null ? r.getCourseIds().get(0) : enrollment);
				XOffering offering = server.getOffering(courseId.getOfferingId());
				XExpectations expectations = server.getExpectations(courseId.getOfferingId());
				XCourse course = offering.getCourse(courseId);
				
				if (request.isAlternative() && nrAssignedAlt >= nrUnassignedCourses && enrollment == null) continue;
				if (request.isAlternative() && enrollment != null) nrAssignedAlt++;

				if (server.isOfferingLocked(course.getOfferingId()))
					ca.setLocked(true);
				ca.setCanWaitList(offering.isWaitList());
				ca.setAssigned(enrollment != null);
				ca.setCourseId(course.getCourseId());
				ca.setSubject(course.getSubjectArea());
				ca.setCourseNbr(course.getCourseNumber());
				ca.setTitle(course.getTitle());
				ca.setEnrollmentMessage(r.getEnrollmentMessage());
				ca.setRequestedDate(r.getTimeStamp());
				if (offering.isWaitList() && r.isWaitlist(wlMode))
					ca.setWaitListedDate(r.getWaitListedTimeStamp());
				ca.setHasCrossList(offering.hasCrossList());
				if (enrollment == null) {
					if (r.isWaitlist() && offering.isWaitList()) {
						Assignment<Request, Enrollment> assignment = new AssignmentMap<Request, Enrollment>();
						CourseRequest courseRequest = SectioningRequest.convert(assignment, r, server, wlMode, helper);
						Collection<Enrollment> enrls = courseRequest.getEnrollmentsSkipSameTime(assignment);
						for (Course c: courseRequest.getCourses()) {
							XOffering off = (c.getId() == courseId.getCourseId() ? offering : server.getOffering(c.getOffering().getId()));
							if (off == null || !off.isWaitList()) continue;
							TreeSet<Enrollment> overlap = new TreeSet<Enrollment>(new Comparator<Enrollment>() {
								@Override
								public int compare(Enrollment o1, Enrollment o2) {
									return o1.getRequest().compareTo(o2.getRequest());
								}
							});
							Hashtable<CourseRequest, TreeSet<Section>> overlapingSections = new Hashtable<CourseRequest, TreeSet<Section>>();
							Enrollment noConfEnrl = null;
							int nbrEnrl = 0;
							for (Iterator<Enrollment> e = enrls.iterator(); e.hasNext();) {
								Enrollment enrl = e.next();
								if (!c.equals(enrl.getCourse())) continue;
								nbrEnrl ++;
								boolean overlaps = false;
								for (Request q: enrl.getStudent().getRequests()) {
									if (q.equals(enrl.getRequest())) continue;
									Enrollment x = assignment.getValue(q);
									if (q instanceof FreeTimeRequest) {
										if (isFreeTimeOverlapping((FreeTimeRequest)q, enrl)) {
											overlaps = true;
											overlap.add(((FreeTimeRequest)q).createEnrollment());
										}
									} else if (x != null && x.getAssignments() != null && !x.getAssignments().isEmpty()) {
										for (Iterator<SctAssignment> i = x.getAssignments().iterator(); i.hasNext();) {
								        	SctAssignment a = i.next();
											if (a.isOverlapping(enrl.getAssignments())) {
												overlaps = true;
												overlap.add(x);
												if (x.getRequest() instanceof CourseRequest) {
													CourseRequest cr = (CourseRequest)x.getRequest();
													TreeSet<Section> ss = overlapingSections.get(cr);
													if (ss == null) { ss = new TreeSet<Section>(new AssignmentComparator<Section, Request, Enrollment>(assignment)); overlapingSections.put(cr, ss); }
													ss.add((Section)a);
												}
											}
								        }
									}
								}
								if (!overlaps && noConfEnrl == null)
									noConfEnrl = enrl;
							}
							if (noConfEnrl == null) {
								if (wlOverlaps == null) wlOverlaps = new HashMap<Long, Set<String>>();
								Set<String> overlaps = new TreeSet<String>();
								wlOverlaps.put(c.getId(), overlaps);
								for (Enrollment q: overlap) {
									if (q.getRequest() instanceof FreeTimeRequest) {
										String ov = OnlineSectioningHelper.toString((FreeTimeRequest)q.getRequest());
										overlaps.add(ov);
										if (c.getId() == course.getCourseId()) ca.addOverlap(ov);
									} else {
										CourseRequest cr = (CourseRequest)q.getRequest();
										Course o = q.getCourse();
										String ov = MSG.course(o.getSubjectArea(), o.getCourseNumber());
										if (overlapingSections.get(cr).size() == 1)
											for (Iterator<Section> i = overlapingSections.get(cr).iterator(); i.hasNext();) {
												Section s = i.next();
												ov += " " + s.getSubpart().getName();
												if (i.hasNext()) ov += ",";
											}
										overlaps.add(ov);
										if (c.getId() == course.getCourseId()) ca.addOverlap(ov);
									}
								}
								if (nbrEnrl == 0) {
									unavailabilities: for (Unavailability unavailability: courseRequest.getStudent().getUnavailabilities()) {
										for (Config config: c.getOffering().getConfigs())
											for (Subpart subpart: config.getSubparts())
												for (Section section: subpart.getSections()) {
													if (unavailability.isOverlapping(section)) {
														String ov = MSG.teachingAssignment(unavailability.getSection().getName());
														overlaps.add(ov);
														if (c.getId() == course.getCourseId()) ca.addOverlap(ov);
														continue unavailabilities;
													}
												}
									}
								}
							}
						}
						if (student.getMaxCredit() != null) {
							Float minCred = course.getMinCredit();
							for (XCourseId altCourseId: r.getCourseIds()) {
								if (altCourseId.equals(courseId)) continue;
								XOffering altOffering = server.getOffering(altCourseId.getOfferingId());
								XCourse altCourse = altOffering.getCourse(altCourseId);
								Float altMinCred = altCourse.getMinCredit();
								if (altMinCred != null && (minCred == null || minCred > altMinCred))
									minCred = altMinCred;
							}
							if (minCred != null && credit + minCred > student.getMaxCredit()) {
								ca.setOverMaxCredit(student.getMaxCredit());
							}
						}
					} else {
						TreeSet<Enrollment> overlap = new TreeSet<Enrollment>(new Comparator<Enrollment>() {
							@Override
							public int compare(Enrollment o1, Enrollment o2) {
								return o1.getRequest().compareTo(o2.getRequest());
							}
						});
						Hashtable<CourseRequest, TreeSet<Section>> overlapingSections = new Hashtable<CourseRequest, TreeSet<Section>>();
						Assignment<Request, Enrollment> assignment = new AssignmentMap<Request, Enrollment>();
						CourseRequest crq = SectioningRequest.convert(assignment, r, server, wlMode, helper);
						Collection<Enrollment> avEnrls = crq.getAvaiableEnrollmentsSkipSameTime(assignment);
						for (Iterator<Enrollment> e = avEnrls.iterator(); e.hasNext();) {
							Enrollment enrl = e.next();
							for (Request q: enrl.getStudent().getRequests()) {
								if (q.equals(enrl.getRequest())) continue;
								Enrollment x = assignment.getValue(q);
								if (x == null || x.getAssignments() == null || x.getAssignments().isEmpty()) continue;
						        for (Iterator<SctAssignment> i = x.getAssignments().iterator(); i.hasNext();) {
						        	SctAssignment a = i.next();
									if (a.isOverlapping(enrl.getAssignments())) {
										overlap.add(x);
										if (x.getRequest() instanceof CourseRequest) {
											CourseRequest cr = (CourseRequest)x.getRequest();
											TreeSet<Section> ss = overlapingSections.get(cr);
											if (ss == null) { ss = new TreeSet<Section>(new AssignmentComparator<Section, Request, Enrollment>(assignment)); overlapingSections.put(cr, ss); }
											ss.add((Section)a);
										}
									}
						        }
							}
						}
						for (Enrollment q: overlap) {
							if (q.getRequest() instanceof FreeTimeRequest) {
								ca.addOverlap(OnlineSectioningHelper.toString((FreeTimeRequest)q.getRequest()));
							} else {
								CourseRequest cr = (CourseRequest)q.getRequest();
								Course o = q.getCourse();
								String ov = MSG.course(o.getSubjectArea(), o.getCourseNumber());
								if (overlapingSections.get(cr).size() == 1)
									for (Iterator<Section> i = overlapingSections.get(cr).iterator(); i.hasNext();) {
										Section s = i.next();
										ov += " " + s.getSubpart().getName();
										if (i.hasNext()) ov += ",";
									}
								ca.addOverlap(ov);
							}
						}
						if (avEnrls.isEmpty()) {
							ca.setNotAvailable(true);
							if (course.getLimit() >= 0) {
								Collection<XCourseRequest> requests = server.getRequests(course.getOfferingId());
								int enrl = 0;
								if (requests != null) {
									for (XCourseRequest x: requests)
										if (x.getEnrollment() != null && x.getEnrollment().getCourseId().equals(course.getCourseId()))
											enrl ++;
								}
								ca.setFull(enrl >= course.getLimit());
							}
							ca.setHasIncompReqs(SectioningRequest.hasInconsistentRequirements(crq, course.getCourseId()));
						}
						if (student.getMaxCredit() != null) {
							Float minCred = course.getMinCredit();
							for (XCourseId altCourseId: r.getCourseIds()) {
								if (altCourseId.equals(courseId)) continue;
								XOffering altOffering = server.getOffering(altCourseId.getOfferingId());
								XCourse altCourse = altOffering.getCourse(altCourseId);
								Float altMinCred = altCourse.getMinCredit();
								if (altMinCred != null && (minCred == null || minCred > altMinCred))
									minCred = altMinCred;
							}
							if (minCred != null && credit + minCred > student.getMaxCredit())
								ca.setOverMaxCredit(student.getMaxCredit());
						}
					}
					if (!r.isWaitListOrNoSub(wlMode)) nrUnassignedCourses++;
					int alt = nrUnassignedCourses;
					for (XRequest q: studentRequests) {
						if (q instanceof XCourseRequest && !q.equals(request)) {
							XEnrollment otherEnrollment = ((XCourseRequest)q).getEnrollment();
							if (otherEnrollment == null) continue;
							if (q.isAlternative()) {
								if (--alt == 0) {
									XOffering otherOffering = server.getOffering(otherEnrollment.getOfferingId());
									XCourse otherCourse = otherOffering.getCourse(otherEnrollment.getCourseId());
									ca.setInstead(MSG.course(otherCourse.getSubjectArea(), otherCourse.getCourseNumber()));
									break;
								}
							}
						}
					}
				} else {
					List<XSection> sections = offering.getSections(enrollment);
					boolean hasAlt = false;
					if (r.getCourseIds().size() > 1) {
						hasAlt = true;
					} else if (offering.getConfigs().size() > 1) {
						hasAlt = true;
					} else {
						for (XSubpart subpart: offering.getConfigs().get(0).getSubparts()) {
							if (subpart.getSections().size() > 1) { hasAlt = true; break; }
						}
					}
					XEnrollments enrollments = server.getEnrollments(offering.getOfferingId());
					for (XSection section: sections) {
						stored.addSection(OnlineSectioningHelper.toProto(section, enrollment));
						ClassAssignmentInterface.ClassAssignment a = ca.addClassAssignment();
						a.setAlternative(r.isAlternative());
						a.setClassId(section.getSectionId());
						XSubpart subpart = offering.getSubpart(section.getSubpartId());
						a.setSubpart(subpart.getName());
						a.setClassNumber(section.getName(-1l));
						a.setSection(section.getName(course.getCourseId()));
						a.setExternalId(section.getExternalId(course.getCourseId()));
						a.setCancelled(section.isCancelled());
						a.setLimit(new int[] {enrollments.countEnrollmentsForSection(section.getSectionId()), section.getLimit()});
						if (section.getTime() != null) {
							for (DayCode d : DayCode.toDayCodes(section.getTime().getDays()))
								a.addDay(d.getIndex());
							a.setStart(section.getTime().getSlot());
							a.setLength(section.getTime().getLength());
							a.setBreakTime(section.getTime().getBreakTime());
							a.setDatePattern(section.getTime().getDatePatternName());
						}
						if (section.getRooms() != null) {
							for (XRoom room: section.getRooms()) {
								a.addRoom(room.getUniqueId(), room.getName());
							}
						}
						for (XInstructor instructor: section.getInstructors()) {
							a.addInstructor(instructor.getName());
							a.addInstructoEmail(instructor.getEmail() == null ? "" : instructor.getEmail());
						}
						if (section.getParentId() != null)
							a.setParentSection(offering.getSection(section.getParentId()).getName(course.getCourseId()));
						a.setSubpartId(section.getSubpartId());
						a.setHasAlternatives(hasAlt);
						a.addNote(course.getNote());
						a.addNote(section.getNote());
						if (attendance != null)
							a.addNote(attendance.getClassNote(section.getExternalId(course.getCourseId())));
						a.setCredit(subpart.getCredit(course.getCourseId()));
						a.setCreditRange(subpart.getCreditMin(course.getCourseId()), subpart.getCreditMax(course.getCourseId()));
						Float creditOverride = section.getCreditOverride(course.getCourseId());
						if (creditOverride != null) a.setCredit(FixedCreditUnitConfig.formatCredit(creditOverride));
						a.setEnrolledDate(enrollment.getTimeStamp());
						int dist = 0;
						String from = null;
						TreeSet<String> overlap = new TreeSet<String>();
						for (XRequest q: studentRequests) {
							if (q instanceof XCourseRequest) {
								XEnrollment otherEnrollment = ((XCourseRequest)q).getEnrollment();
								if (otherEnrollment == null) continue;
								XOffering otherOffering = server.getOffering(otherEnrollment.getOfferingId());
								for (XSection otherSection: otherOffering.getSections(otherEnrollment)) {
									if (otherSection.equals(section) || otherSection.getTime() == null) continue;
									int d = otherSection.getDistanceInMinutes(section, m);
									if (d > dist) {
										dist = d;
										from = "";
										for (Iterator<XRoom> k = otherSection.getRooms().iterator(); k.hasNext();)
											from += k.next().getName() + (k.hasNext() ? ", " : "");
									}
									if (otherSection.isDistanceConflict(student, section, m))
										a.setDistanceConflict(true);
									if (section.getTime() != null && section.getTime().hasIntersection(otherSection.getTime()) && !section.isToIgnoreStudentConflictsWith(offering.getDistributions(), otherSection.getSectionId())) {
										XCourse otherCourse = otherOffering.getCourse(otherEnrollment.getCourseId());
										XSubpart otherSubpart = otherOffering.getSubpart(otherSection.getSubpartId());
										overlap.add(MSG.clazz(otherCourse.getSubjectArea(), otherCourse.getCourseNumber(), otherSubpart.getName(), otherSection.getName(otherCourse.getCourseId())));
									}
								}
							}
						}
						if (unavailabilities != null)
							for (CourseSection cs: unavailabilities) {
								if (section.getTime() != null && section.getTime().hasIntersection(cs.getSection().getTime())) {
									overlap.add(MSG.teachingAssignment(MSG.clazz(cs.getCourse().getSubjectArea(), cs.getCourse().getCourseNumber(), cs.getSection().getSubpartName(), cs.getSection().getName(cs.getCourse().getCourseId()))));
								}
							}
						if (!overlap.isEmpty()) {
							String note = null;
							for (Iterator<String> j = overlap.iterator(); j.hasNext(); ) {
								String n = j.next();
								if (note == null)
									note = MSG.noteAllowedOverlapFirst(n);
								else if (j.hasNext())
									note += MSG.noteAllowedOverlapMiddle(n);
								else
									note += MSG.noteAllowedOverlapLast(n);
							}
							a.setOverlapNote(note);
						}
						a.setBackToBackDistance(dist);
						a.setBackToBackRooms(from);
						a.setSaved(true);
						if (a.getParentSection() == null) {
							String consent = server.getCourse(course.getCourseId()).getConsentLabel();
							if (consent != null) {
								if (enrollment.getApproval() != null) {
									a.setParentSection(MSG.consentApproved(df.format(enrollment.getApproval().getTimeStamp())));
								} else
									a.setParentSection(MSG.consentWaiting(consent.toLowerCase()));
							}
						}
						a.setExpected(overExp.getExpected(section.getLimit(), expectations.getExpectedSpace(section.getSectionId())));
					}
				}
				
				if (messages != null) {
					XEnrollments enrollments = server.getEnrollments(offering.getOfferingId());
					f: for (EnrollmentFailure f: messages) {
						XSection section = f.getSection();
						if (!f.getCourse().getCourseId().equals(ca.getCourseId())) continue;
						for (ClassAssignmentInterface.ClassAssignment a: ca.getClassAssignments())
							if (f.getSection().getSectionId().equals(a.getClassId())) {
								if (f.isError()) a.addError(f.getMessage());
								else if (f.isWarning()) a.addWarn(f.getMessage());
								else a.addInfo(f.getMessage());
								continue f;
							}
						ca.setAssigned(true);
						ClassAssignmentInterface.ClassAssignment a = ca.addClassAssignment();
						a.setAlternative(r.isAlternative());
						a.setClassId(section.getSectionId());
						XSubpart subpart = offering.getSubpart(section.getSubpartId());
						a.setSubpart(subpart.getName());
						a.setClassNumber(section.getName(-1l));
						a.setSection(section.getName(course.getCourseId()));
						a.setExternalId(section.getExternalId(course.getCourseId()));
						a.setCancelled(section.isCancelled());
						a.setLimit(new int[] {enrollments.countEnrollmentsForSection(section.getSectionId()), section.getLimit()});
						if (section.getTime() != null) {
							for (DayCode d : DayCode.toDayCodes(section.getTime().getDays()))
								a.addDay(d.getIndex());
							a.setStart(section.getTime().getSlot());
							a.setLength(section.getTime().getLength());
							a.setBreakTime(section.getTime().getBreakTime());
							a.setDatePattern(section.getTime().getDatePatternName());
						}
						if (section.getRooms() != null) {
							for (XRoom room: section.getRooms()) {
								a.addRoom(room.getUniqueId(), room.getName());
							}
						}
						for (XInstructor instructor: section.getInstructors()) {
							a.addInstructor(instructor.getName());
							a.addInstructoEmail(instructor.getEmail() == null ? "" : instructor.getEmail());
						}
						if (section.getParentId() != null)
							a.setParentSection(offering.getSection(section.getParentId()).getName(course.getCourseId()));
						a.setSubpartId(section.getSubpartId());
						a.addNote(course.getNote());
						a.addNote(section.getNote());
						if (attendance != null)
							a.addNote(attendance.getClassNote(section.getExternalId(course.getCourseId())));
						a.setCredit(subpart.getCredit(course.getCourseId()));
						a.setCreditRange(subpart.getCreditMin(course.getCourseId()), subpart.getCreditMax(course.getCourseId()));
						Float creditOverride = section.getCreditOverride(course.getCourseId());
						if (creditOverride != null) a.setCredit(FixedCreditUnitConfig.formatCredit(creditOverride));
						int dist = 0;
						String from = null;
						a.setBackToBackDistance(dist);
						a.setBackToBackRooms(from);
						a.setSaved(false);
						a.setDummy(true);
						if (f.isError()) a.setError(f.getMessage());
						else if (f.isWarning()) a.setWarn(f.getMessage());
						else a.setInfo(f.getMessage());
						a.setExpected(overExp.getExpected(section.getLimit(), expectations.getExpectedSpace(section.getSectionId())));
					}
				}
				if (errors != null) {
					for (ErrorMessage f: errors) {
						if (!ca.getCourseName().equals(f.getCourse())) continue;
						for (ClassAssignmentInterface.ClassAssignment a: ca.getClassAssignments())
							if (a.getExternalId().equals(f.getSection()))
								a.addError(f.getMessage());
					}
				}
			} else if (request instanceof XFreeTimeRequest) {
				XFreeTimeRequest r = (XFreeTimeRequest)request;
				ca.setCourseId(null);
				for (XRequest q: studentRequests) {
					if (q instanceof XCourseRequest) {
						XEnrollment otherEnrollment = ((XCourseRequest)q).getEnrollment();
						if (otherEnrollment == null) continue;
						XOffering otherOffering = server.getOffering(otherEnrollment.getOfferingId());
						for (XSection otherSection: otherOffering.getSections(otherEnrollment)) {
							if (otherSection.getTime() != null && otherSection.getTime().hasIntersection(r.getTime())) {
								XCourse otherCourse = otherOffering.getCourse(otherEnrollment.getCourseId());
								XSubpart otherSubpart = otherOffering.getSubpart(otherSection.getSubpartId());
								ca.addOverlap(MSG.clazz(otherCourse.getSubjectArea(), otherCourse.getCourseNumber(), otherSubpart.getName(), otherSection.getName(otherCourse.getCourseId())));
							}
						}
					}
				}
				ca.setAssigned(ca.getOverlaps() == null);
				ClassAssignmentInterface.ClassAssignment a = ca.addClassAssignment();
				a.setAlternative(r.isAlternative());
				for (DayCode d : DayCode.toDayCodes(r.getTime().getDays()))
					a.addDay(d.getIndex());
				a.setStart(r.getTime().getSlot());
				a.setLength(r.getTime().getLength());
			}
			ret.add(ca);
		}
					
		action.addEnrollment(stored);
		
		if (errors != null) {
			for (ErrorMessage e: errors) {
				ret.addError(e);
			}
		}
		
		if (messages != null) {
			Set<String> added = new HashSet<String>();
			for (EnrollmentFailure f: messages) {
				if (f.hasErrors())
					for (EnrollmentError err: f.getErrors())
						ret.addError(new ErrorMessage(f.getCourse().getCourseName(), f.getSection().getExternalId(f.getCourse().getCourseId()), err.getCode(), err.getMessage()));
				for (String fm: f.getMessage().split("\n")) {
					String message = MSG.clazz(f.getCourse().getSubjectArea(), f.getCourse().getCourseNumber(), f.getSection().getSubpartName(), f.getSection().getName(f.getCourse().getCourseId())) + ": " + fm;
					if (added.add(message))
						ret.addMessage(message);
				}
			}
		}
		
		if (includeRequest) {
			CourseRequestInterface request = new CourseRequestInterface();
			request.setStudentId(student.getStudentId());
			request.setSaved(true);
			request.setAcademicSessionId(server.getAcademicSession().getUniqueId());
			request.setMaxCredit(student.getMaxCredit());
			request.setWaitListMode(wlMode);
			if (student.getMaxCreditOverride() != null) {
				request.setMaxCreditOverride(student.getMaxCreditOverride().getValue());
				request.setMaxCreditOverrideExternalId(student.getMaxCreditOverride().getExternalId());
				request.setMaxCreditOverrideTimeStamp(student.getMaxCreditOverride().getTimeStamp());
				Integer status = student.getMaxCreditOverride().getStatus();
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
				else if (status == org.unitime.timetable.model.CourseRequest.CourseRequestOverrideStatus.NOT_NEEDED.ordinal())
					request.setMaxCreditOverrideStatus(RequestedCourseStatus.OVERRIDE_NOT_NEEDED);
				else
					request.setMaxCreditOverrideStatus(RequestedCourseStatus.OVERRIDE_PENDING);
			}
			CourseRequestInterface.Request lastRequest = null;
			int lastRequestPriority = -1;
			for (XRequest cd: studentRequests) {
				CourseRequestInterface.Request r = null;
				if (cd instanceof XFreeTimeRequest) {
					XFreeTimeRequest ftr = (XFreeTimeRequest)cd;
					CourseRequestInterface.FreeTime ft = new CourseRequestInterface.FreeTime();
					ft.setStart(ftr.getTime().getSlot());
					ft.setLength(ftr.getTime().getLength());
					for (DayCode day : DayCode.toDayCodes(ftr.getTime().getDays()))
						ft.addDay(day.getIndex());
					if (lastRequest != null && lastRequestPriority == cd.getPriority() && lastRequest.hasRequestedCourse() && lastRequest.getRequestedCourse(0).isFreeTime()) {
						lastRequest.getRequestedCourse(0).addFreeTime(ft);
					} else {
						r = new CourseRequestInterface.Request();
						RequestedCourse rc = new RequestedCourse();
						r.addRequestedCourse(rc);
						rc.addFreeTime(ft);
						if (cd.isAlternative())
							request.getAlternatives().add(r);
						else
							request.getCourses().add(r);
						lastRequest = r;
						lastRequestPriority = cd.getPriority();
						rc.setStatus(RequestedCourseStatus.SAVED);
					}
				} else if (cd instanceof XCourseRequest) {
					r = new CourseRequestInterface.Request();
					for (XCourseId courseId: ((XCourseRequest)cd).getCourseIds()) {
						XCourse c = server.getCourse(courseId.getCourseId());
						if (c == null) continue;
						RequestedCourse rc = new RequestedCourse();
						rc.setCourseId(c.getCourseId());
						rc.setCourseName(c.getSubjectArea() + " " + c.getCourseNumber() + (c.hasUniqueName() && !CONSTANTS.showCourseTitle() ? "" : " - " + c.getTitle()));
						rc.setCourseTitle(c.getTitle());
						rc.setCredit(c.getMinCredit(), c.getMaxCredit());
						boolean isEnrolled = ((XCourseRequest)cd).getEnrollment() != null && c.getCourseId().equals(((XCourseRequest)cd).getEnrollment().getCourseId());
						if (setReadOnly && isEnrolled)
							rc.setReadOnly(true);
						if (isEnrolled)
							rc.setStatus(RequestedCourseStatus.ENROLLED);
						else {
							Integer status = ((XCourseRequest)cd).getOverrideStatus(courseId);
							if (status == null)
								rc.setStatus(RequestedCourseStatus.SAVED);
							else if (status == org.unitime.timetable.model.CourseRequest.CourseRequestOverrideStatus.APPROVED.ordinal())
								rc.setStatus(RequestedCourseStatus.OVERRIDE_APPROVED);
							else if (status == org.unitime.timetable.model.CourseRequest.CourseRequestOverrideStatus.REJECTED.ordinal())
								rc.setStatus(RequestedCourseStatus.OVERRIDE_REJECTED);
							else if (status == org.unitime.timetable.model.CourseRequest.CourseRequestOverrideStatus.CANCELLED.ordinal())
								rc.setStatus(RequestedCourseStatus.OVERRIDE_CANCELLED);
							else if (status == org.unitime.timetable.model.CourseRequest.CourseRequestOverrideStatus.NOT_CHECKED.ordinal())
								rc.setStatus(RequestedCourseStatus.OVERRIDE_NEEDED);
							else if (status == org.unitime.timetable.model.CourseRequest.CourseRequestOverrideStatus.NOT_NEEDED.ordinal())
								rc.setStatus(RequestedCourseStatus.OVERRIDE_NOT_NEEDED);
							else
								rc.setStatus(RequestedCourseStatus.OVERRIDE_PENDING);
						}
						XOffering offering = server.getOffering(c.getOfferingId());
						rc.setCanWaitList(offering != null && offering.isWaitList());
						rc.setOverrideExternalId(((XCourseRequest)cd).getOverrideExternalId(courseId));
						rc.setOverrideTimeStamp(((XCourseRequest)cd).getOverrideTimeStamp(courseId));
						((XCourseRequest)cd).fillPreferencesIn(rc, courseId);
						r.addRequestedCourse(rc);
						if (showWaitListPosition && rc.isCanWaitList() && ((XCourseRequest)cd).isWaitlist()) {
							rc.setWaitListPosition(getWaitListPosition(offering, student, (XCourseRequest)cd, courseId, server, helper));
						}
					}
					r.setWaitList(((XCourseRequest)cd).isWaitlist(wlMode));
					r.setNoSub(((XCourseRequest)cd).isNoSub(wlMode));
					r.setCritical(((XCourseRequest)cd).getCritical());
					r.setTimeStamp(((XCourseRequest)cd).getTimeStamp());
					if (r.isWaitList())
						r.setWaitListedTimeStamp(((XCourseRequest)cd).getWaitListedTimeStamp());
					r.setWaitListSwapWithCourseOfferingId(((XCourseRequest)cd).getWaitListSwapWithCourseOffering() == null ? null : ((XCourseRequest)cd).getWaitListSwapWithCourseOffering().getCourseId());
					if (r.hasRequestedCourse()) {
						if (cd.isAlternative())
							request.getAlternatives().add(r);
						else
							request.getCourses().add(r);
					}
					lastRequest = r;
					lastRequestPriority = cd.getPriority();
					if (r.isWaitList()) {
						for (RequestedCourse rc: r.getRequestedCourse()) {
							Set<String> overlaps = (wlOverlaps == null || rc.getCourseId() == null ? null : wlOverlaps.get(rc.getCourseId()));
							if (overlaps != null && !overlaps.isEmpty()) {
								String message = null;
								for (Iterator<String> i = overlaps.iterator(); i.hasNext();) {
									String ov = i.next();
									if (message == null)
										message = MSG.conflictWithFirst(ov);
									else if (i.hasNext())
										message += MSG.conflictWithMiddle(ov);
									else
										message += MSG.conflictWithLast(ov);
								}
								request.addConfirmationMessage(rc.getCourseId(), rc.getCourseName(), "WL-OVERLAP", message + ".", 0);
							}
						}
						if (r.isWaitList() && r.getWaitListSwapWithCourseOfferingId() != null && r.hasRequestedCourse()) {
							XEnrollment enrollment = ((XCourseRequest)cd).getEnrollment();
							if (enrollment != null && enrollment.getCourseId().equals(r.getWaitListSwapWithCourseOfferingId())) {
								boolean before = true;
								for (RequestedCourse rc: r.getRequestedCourse()) {
									if (r.getWaitListSwapWithCourseOfferingId().equals(rc.getCourseId())) {
										if (((XCourseRequest)cd).isRequired(enrollment, server.getOffering(enrollment.getOfferingId()))) {
											rc.setStatus(RequestedCourseStatus.WAITLIST_INACTIVE);
											request.addConfirmationMessage(rc.getCourseId(), rc.getCourseName(), "WL-INACTIVE", MSG.waitListRequirementsMet(), 0);
										}
										before = false;
									} else if (!before) {
										rc.setStatus(RequestedCourseStatus.WAITLIST_INACTIVE);
										request.addConfirmationMessage(rc.getCourseId(), rc.getCourseName(), "WL-INACTIVE", MSG.waitListLowPriority(), 0);
									}
								}
							}
						}
					}
				}
			}
			ret.setRequest(request);
		}
		
		return ret;
	}

	@Override
	public String name() {
		return "get-assignment";
	}
	
	public static class CourseSection {
		XCourse iCourse;
		XSection iSection;
		CourseSection(XCourse course, XSection section) {
			iCourse = course; iSection = section;
		}
		public XCourse getCourse() { return iCourse; }
		public XSection getSection() { return iSection; }
	}
	
	public static boolean isFreeTimeOverlapping(FreeTimeRequest r, Enrollment e) {
		if (r.isAlternative() || r.getPriority() >= e.getRequest().getPriority())
			return false;
        if (r.getTime() == null)
            return false;
        for (SctAssignment assignment : e.getAssignments()) {
            if (assignment.isAllowOverlap())
                continue;
            if (assignment.getTime() == null)
                continue;
            if (assignment instanceof FreeTimeRequest)
                return false;
            if (r.getTime().hasIntersection(assignment.getTime()))
                return true;
        }
        return false;
    }
	
	protected static String datePatternName(DatePattern pattern, String datePatternFormat) {
    	if ("never".equals(datePatternFormat)) return pattern.getName();
    	if ("extended".equals(datePatternFormat) && !pattern.isExtended()) return pattern.getName();
    	if ("alternate".equals(datePatternFormat) && pattern.isAlternate()) return pattern.getName();
		Formats.Format<Date> dpf = Formats.getDateFormat(Formats.Pattern.DATE_PATTERN);
		Date first = pattern.getStartDate();
		Date last = pattern.getEndDate();
		return dpf.format(first) + (first.equals(last) ? "" : " - " + dpf.format(last));
	}
}
