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
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.ClassAssignment;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.Lock;
import org.unitime.timetable.onlinesectioning.model.XConfig;
import org.unitime.timetable.onlinesectioning.model.XCourse;
import org.unitime.timetable.onlinesectioning.model.XCourseId;
import org.unitime.timetable.onlinesectioning.model.XEnrollment;
import org.unitime.timetable.onlinesectioning.model.XEnrollments;
import org.unitime.timetable.onlinesectioning.model.XExpectations;
import org.unitime.timetable.onlinesectioning.model.XInstructor;
import org.unitime.timetable.onlinesectioning.model.XOffering;
import org.unitime.timetable.onlinesectioning.model.XRoom;
import org.unitime.timetable.onlinesectioning.model.XSection;
import org.unitime.timetable.onlinesectioning.model.XSubpart;

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

	@Override
	public Collection<ClassAssignment> execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		ArrayList<ClassAssignmentInterface.ClassAssignment> ret = new ArrayList<ClassAssignmentInterface.ClassAssignment>();
		Lock lock = server.readLock();
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
			for (XConfig config: offering.getConfigs())
				for (XSubpart subpart: config.getSubparts())
					for (XSection section: subpart.getSections()) {
						if (section.getLimit() == 0) continue;
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
						a.setClassNumber(section.getName(-1l));
						a.setCancelled(section.isCancelled());
						a.setLimit(new int[] { enrollments.countEnrollmentsForSection(section.getSectionId()), section.getLimit()});
						if (getStudentId() != null) {
							for (XEnrollment enrollment: enrollments.getEnrollmentsForSection(section.getSectionId())) {
								if (enrollment.getStudentId().equals(getStudentId())) { a.setSaved(true); break; }
							}
						}
						a.addNote(c.getNote());
						a.addNote(section.getNote());
						a.setCredit(subpart.getCredit(c.getCourseId()));
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
								a.addRoom(rm.getName());
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
