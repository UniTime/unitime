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
package org.unitime.timetable.solver.studentsct;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.studentsct.StudentSectioningModel;
import org.cpsolver.studentsct.model.Config;
import org.cpsolver.studentsct.model.Course;
import org.cpsolver.studentsct.model.CourseRequest;
import org.cpsolver.studentsct.model.Enrollment;
import org.cpsolver.studentsct.model.FreeTimeRequest;
import org.cpsolver.studentsct.model.Request;
import org.cpsolver.studentsct.model.SctAssignment;
import org.cpsolver.studentsct.model.Section;
import org.cpsolver.studentsct.model.Student;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.basic.GetAssignment;
import org.unitime.timetable.onlinesectioning.custom.StudentEnrollmentProvider.EnrollmentFailure;
import org.unitime.timetable.onlinesectioning.model.XCourse;
import org.unitime.timetable.onlinesectioning.model.XOffering;
import org.unitime.timetable.onlinesectioning.updates.EnrollStudent;

public class BatchEnrollStudent extends EnrollStudent {
	private static final long serialVersionUID = 1L;
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);

	@Override
	public ClassAssignmentInterface execute(OnlineSectioningServer server, final OnlineSectioningHelper helper) {
		if (getRequest().getStudentId() == null)
			throw new SectioningException(MSG.exceptionNoStudent());
		
		StudentSolver solver = (StudentSolver) server;
		StudentSectioningModel model = (StudentSectioningModel) solver.currentSolution().getModel();
		Student student = null;
		for (Student s: model.getStudents()) {
			if (s.getId() == getRequest().getStudentId()) { student = s; break; }
		}
		
		if (student == null)
			throw new SectioningException(MSG.exceptionBadStudentId());
		
		List<EnrollmentFailure> failures = new ArrayList<EnrollmentFailure>();
		
		Map<CourseRequest, List<Section>> enrollments = new HashMap<CourseRequest, List<Section>>();
		assignments: for (ClassAssignmentInterface.ClassAssignment assignment: getAssignment()) {
			if (assignment == null || !assignment.isAssigned()) continue;
			if (assignment.isFreeTime()) {
			} else {
				CourseRequest request = null;
				Course course = null;
				requests: for (Request r: student.getRequests()) {
					if (r instanceof CourseRequest) {
						for (Course c: ((CourseRequest)r).getCourses()) {
							if (c.getId() == assignment.getCourseId()) {
								course = c;
								request = (CourseRequest)r;
								break requests;
							}
						}
					}
				}
				if (request == null) {
					XCourse c = server.getCourse(assignment.getCourseId());
					XOffering offering = server.getOffering(c.getOfferingId());
					failures.add(new EnrollmentFailure(c, offering.getSection(assignment.getClassId()), "Adding courses is not supported at the moment.", false));
					continue assignments;
				}
				Section section = course.getOffering().getSection(assignment.getClassId());
				List<Section> sections = enrollments.get(request);
				if (sections == null) {
					sections = new ArrayList<Section>();
					enrollments.put(request, sections);
				}
				sections.add(section);
			}
		}
		
		Assignment<Request, Enrollment> assignment = solver.currentSolution().getAssignment();
		for (Request request: student.getRequests()) {
			Enrollment enrollment = assignment.getValue(request);
			if (enrollment != null)
				assignment.unassign(0l, request);
		}
		for (Request request: student.getRequests()) {
			if (request instanceof CourseRequest) {
				CourseRequest cr = (CourseRequest)request;
				List<Section> sections = enrollments.get(cr);
				if (sections != null) {
					Section section = sections.get(0);
					int pririty = 0;
					Config config = section.getSubpart().getConfig();
					for (int i = 0; i < cr.getCourses().size(); i++) {
						if (cr.getCourses().get(i).getOffering().equals(config.getOffering())) {
							pririty = i;
							break;
						}
					}
					assignment.assign(0l, new Enrollment(cr, pririty, config, new HashSet<SctAssignment>(sections), assignment));
				}
			} else {
				FreeTimeRequest ft = (FreeTimeRequest)request;
				Enrollment enrollment = ft.createEnrollment();
				if (!model.inConflict(assignment, enrollment))
					assignment.assign(0l, enrollment);
			}
		}
		
		return server.execute(server.createAction(GetAssignment.class).forStudent(getStudentId()).withMessages(failures), helper.getUser());
	}
}
