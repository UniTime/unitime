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

import org.cpsolver.studentsct.online.expectations.OverExpectedCriterion;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.server.DayCode;
import org.unitime.timetable.gwt.server.Query;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.ClassAssignment;
import org.unitime.timetable.model.FixedCreditUnitConfig;
import org.unitime.timetable.model.StudentSchedulingRule;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.Lock;
import org.unitime.timetable.onlinesectioning.model.XConfig;
import org.unitime.timetable.onlinesectioning.model.XCourse;
import org.unitime.timetable.onlinesectioning.model.XCourseId;
import org.unitime.timetable.onlinesectioning.model.XCourseRequest;
import org.unitime.timetable.onlinesectioning.model.XEnrollment;
import org.unitime.timetable.onlinesectioning.model.XEnrollments;
import org.unitime.timetable.onlinesectioning.model.XExpectations;
import org.unitime.timetable.onlinesectioning.model.XInstructor;
import org.unitime.timetable.onlinesectioning.model.XOffering;
import org.unitime.timetable.onlinesectioning.model.XReservation;
import org.unitime.timetable.onlinesectioning.model.XRoom;
import org.unitime.timetable.onlinesectioning.model.XSection;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.model.XSubpart;
import org.unitime.timetable.onlinesectioning.status.StatusPageSuggestionsAction.StudentMatcher;

/**
 * @author Tomas Muller
 */
public class ListClasses implements OnlineSectioningAction<Collection<ClassAssignmentInterface.ClassAssignment>> {
	private static final long serialVersionUID = 1L;
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);

	private String iCourse;
	private Long iStudentId;
	
	public ListClasses forCourseAndStudent(String course, Long studentId) {
		iCourse = course;
		iStudentId = studentId;
		return this;
	}
	
	public String getCourse() {
		return iCourse;
	}
	
	public Long getStudentId() {
		return iStudentId;
	}
	
	protected boolean isAllowDisabled(XEnrollments enrollments, XStudent student, XOffering offering, XCourseId course, XConfig config, XSection section) {
		if (student == null) return false;
		if (student.isAllowDisabled()) return true;
		for (XReservation reservation: offering.getReservations())
			if (reservation.isAllowDisabled() && reservation.isApplicable(student, course) && reservation.isIncluded(offering, config.getConfigId(), section)) {
				return true;
			}
		for (XEnrollment enrollment: enrollments.getEnrollmentsForSection(section.getSectionId()))
			if (enrollment.getStudentId().equals(getStudentId())) {
				return true;
			}
		return false;
	}
	
	protected boolean isAvailable(XEnrollments enrollments, XStudent student, XOffering offering, XCourse course, XConfig config, XSection section, XEnrollment enrollment) {
		boolean hasMustBeUsed = false;
		boolean hasReservation = false;
		boolean canOverLimit = false;
		for (XReservation r: offering.getReservations()) {
			if (student != null && !r.isApplicable(student, course)) continue; // reservation does not apply to this student
			boolean mustBeUsed = (r.mustBeUsed() && (r.isAlwaysExpired() || !r.isExpired()));
			if (mustBeUsed && !hasMustBeUsed) {
				hasReservation = false; hasMustBeUsed = true; canOverLimit = false;
			}
			if (hasMustBeUsed && !mustBeUsed) continue; // student must use a reservation, but this one is not it
			if (r.getLimit() >= 0 && r.getLimit() <= enrollments.countEnrollmentsForReservation(r.getReservationId())) continue; // reservation is full
			if (r.isIncluded(offering, config.getConfigId(), section)) {
				hasReservation = true;
				if (r.canAssignOverLimit()) canOverLimit = true;
			}
		}
		boolean hasSection = (enrollment != null && enrollment.getSectionIds().contains(section.getSectionId()));
		boolean hasConfig = (enrollment != null && config.getConfigId().equals(enrollment.getConfigId()));
		boolean hasCourse = (enrollment != null && course.getCourseId().equals(enrollment.getCourseId()));
		if (!canOverLimit) {
			if (!hasSection && section.getLimit() >= 0 && enrollments.countEnrollmentsForSection(section.getSectionId()) >= section.getLimit()) return false;
			if (!hasConfig && config.getLimit() >= 0 && enrollments.countEnrollmentsForConfig(config.getConfigId()) >= config.getLimit()) return false;
			if (!hasCourse && course.getLimit() >= 0 && enrollments.countEnrollmentsForCourse(course.getCourseId()) >= course.getLimit()) return false;
		}
		if (hasReservation) return true;
		if (hasMustBeUsed) return true;
		if (!hasCourse && offering.getUnreservedSpace(enrollments) <= 0) return false;
		if (!hasConfig && offering.getUnreservedConfigSpace(config.getConfigId(), enrollments) <= 0) return false;
		if (!hasSection && offering.getUnreservedSectionSpace(section.getSectionId(), enrollments) <= 0) return false;
		return true;
	}

	@Override
	public Collection<ClassAssignment> execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		ArrayList<ClassAssignmentInterface.ClassAssignment> ret = new ArrayList<ClassAssignmentInterface.ClassAssignment>();
		Lock lock = server.readLock();
		boolean checkAvailability = server.getConfig().getPropertyBoolean("ListClasses.CheckClasAvailability", true);
		try {
			XCourseId id = server.getCourse(getCourse());
			if (id == null) throw new SectioningException(MSG.exceptionCourseDoesNotExist(getCourse()));
			XOffering offering = server.getOffering(id.getOfferingId());
			XCourse c = offering.getCourse(id.getCourseId());
			XEnrollments enrollments = server.getEnrollments(c.getOfferingId());
			XExpectations expectations = server.getExpectations(c.getOfferingId());
			OverExpectedCriterion overExp = server.getOverExpectedCriterion();
			ClassAssignmentInterface.CourseAssignment courseAssign = new ClassAssignmentInterface.CourseAssignment();
			courseAssign.setCourseId(c.getCourseId());
			courseAssign.setCourseNbr(c.getCourseNumber());
			courseAssign.setTitle(c.getTitle());
			courseAssign.setSubject(c.getSubjectArea());
			courseAssign.setHasCrossList(offering.hasCrossList());
			courseAssign.setCanWaitList(offering.isWaitList());
			XStudent student = (getStudentId() == null ? null : server.getStudent(getStudentId()));
			
			String imFilter = null;
			StudentSchedulingRule rule = null;
			if (student != null) {
				rule = StudentSchedulingRule.getRuleFilter(student, server, helper);
				if (rule == null) {
					String filter = server.getConfig().getProperty("Filter.OnlineOnlyStudentFilter", null);
					if (filter != null && !filter.isEmpty()) {
						if (new Query(filter).match(new StudentMatcher(student, server.getAcademicSession().getDefaultSectioningStatus(), server, false))) {
							imFilter = server.getConfig().getProperty("Filter.OnlineOnlyInstructionalModeRegExp");
						} else if (server.getConfig().getPropertyBoolean("Filter.OnlineOnlyExclusiveCourses", false)) {
							imFilter = server.getConfig().getProperty("Filter.ResidentialInstructionalModeRegExp");
						}
					}
					if (imFilter != null) {
						if (helper.hasAdminPermission() && server.getConfig().getPropertyBoolean("Filter.OnlineOnlyAdminOverride", false))
							imFilter = null;
						else if (helper.hasAvisorPermission() && server.getConfig().getPropertyBoolean("Filter.OnlineOnlyAdvisorOverride", false))
							imFilter = null;
					}
				}
			}
			XEnrollment enrollment = null;
			if (student != null) {
				XCourseRequest r = student.getRequestForCourse(id.getCourseId());
				enrollment = (r == null ? null : r.getEnrollment());
			}

			for (XConfig config: offering.getConfigs()) {
				boolean imAvailable = true;
				if (rule != null) {
					if (rule.isDisjunctive()) {
						if (rule.hasCourseName() && rule.matchesCourseName(id.getCourseName())) {
						} else if (rule.hasCourseType() && rule.matchesCourseType(id.getType())) {
						} else if (rule.hasInstructionalMethod() && rule.matchesInstructionalMethod(config.getInstructionalMethod())) {
						} else {
							if (enrollment == null || !config.getConfigId().equals(enrollment.getConfigId())) continue;
							imAvailable = false;
						}
					} else {
						if (!rule.matchesInstructionalMethod(config.getInstructionalMethod())) {
							if (enrollment == null || !config.getConfigId().equals(enrollment.getConfigId())) continue;
							imAvailable = false;
						}
					}
				} else if (imFilter != null) {
					String imRef = (config.getInstructionalMethod() == null ? null : config.getInstructionalMethod().getReference());
        			if (imFilter.isEmpty()) {
        				if (imRef != null && !imRef.isEmpty()) {
        					if (enrollment == null || !config.getConfigId().equals(enrollment.getConfigId())) continue;
        					imAvailable = false;
        				}
        			} else {
        				if (imRef == null || !imRef.matches(imFilter)) {
        					if (enrollment == null || !config.getConfigId().equals(enrollment.getConfigId())) continue;
        					imAvailable = false;
        				}
        			}
				}
				for (XSubpart subpart: config.getSubparts())
					for (XSection section: subpart.getSections()) {
						if (!section.isEnabledForScheduling() && !isAllowDisabled(enrollments, student, offering, id, config, section)) continue;
						String room = null;
						if (section.getRooms() != null) {
							for (XRoom rm: section.getRooms()) {
								if (room == null) room = ""; else room += ", ";
								room += rm.getName();
							}
						}
						ClassAssignmentInterface.ClassAssignment a = courseAssign.addClassAssignment();
						a.setClassId(section.getSectionId());
						a.setSubpart(subpart.getName());
						a.setSection(section.getName(c.getCourseId()));
						a.setExternalId(section.getExternalId(c.getCourseId()));
						a.setClassNumber(section.getName(-1l));
						a.setCancelled(section.isCancelled());
						a.setLimit(new int[] { enrollments.countEnrollmentsForSection(section.getSectionId()), section.getLimit()});
						a.setSaved(enrollment != null && enrollment.getSectionIds().contains(section.getSectionId()));
						if (!a.isSaved() && checkAvailability)
							a.setAvailable(imAvailable && isAvailable(enrollments, student, offering, c, config, section, enrollment));
						a.addNote(section.getNote());
						a.setCredit(subpart.getCredit(c.getCourseId()));
						a.setCreditRange(subpart.getCreditMin(c.getCourseId()), subpart.getCreditMax(c.getCourseId()));
						Float creditOverride = section.getCreditOverride(c.getCourseId());
						if (creditOverride != null) a.setCredit(FixedCreditUnitConfig.formatCredit(creditOverride));
						if (section.getTime() != null) {
							for (DayCode d: DayCode.toDayCodes(section.getTime().getDays()))
								a.addDay(d.getIndex());
							a.setStart(section.getTime().getSlot());
							a.setLength(section.getTime().getLength());
							a.setBreakTime(section.getTime().getBreakTime());
							a.setDatePattern(section.getTime().getDatePatternName());
						}
						if (section.getRooms() != null) {
							for (XRoom rm: section.getRooms()) {
								a.addRoom(rm.getUniqueId(), rm.getName());
							}
						}
						for (XInstructor instructor: section.getInstructors()) {
							a.addInstructor(instructor.getName());
							a.addInstructoEmail(instructor.getEmail() == null ? "" : instructor.getEmail());
						}
						if (section.getParentId() != null)
							a.setParentSection(offering.getSection(section.getParentId()).getName(c.getCourseId()));
						a.setSubpartId(subpart.getSubpartId());
						if (a.getParentSection() == null)
							a.setParentSection(c.getConsentLabel());
						a.setExpected(overExp.getExpected(section.getLimit(), expectations.getExpectedSpace(section.getSectionId())));
						ret.add(a);
					}
			}
		} finally {
			lock.release();
		}
		if (ret.isEmpty())
			throw new SectioningException(MSG.exceptionNoClassesForCourse(getCourse()));
		return ret;
	}

	@Override
	public String name() {
		return "list-classes";
	}

}
