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
package org.unitime.timetable.onlinesectioning.custom.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.EligibilityCheck;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.GradingModes;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.custom.StudentEnrollmentProvider;
import org.unitime.timetable.onlinesectioning.model.XCourse;
import org.unitime.timetable.onlinesectioning.model.XEnrollment;
import org.unitime.timetable.onlinesectioning.model.XSection;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.solver.SectioningRequest;

/**
 * @author Tomas Muller
 */
public class NoDropStudentEnrollmentProvider implements StudentEnrollmentProvider {

	@Override
	public void checkEligibility(OnlineSectioningServer server, OnlineSectioningHelper helper, EligibilityCheck check, XStudent student) throws SectioningException {
	}

	@Override
	public List<EnrollmentFailure> enroll(OnlineSectioningServer server, OnlineSectioningHelper helper, XStudent student, List<EnrollmentRequest> enrollments, Set<Long> lockedCourses, GradingModes gradingModes) throws SectioningException {
		Student dbStudent = StudentDAO.getInstance().get(student.getStudentId());
		List<EnrollmentFailure> failures = new ArrayList<EnrollmentFailure>();
		e: for (StudentClassEnrollment e: dbStudent.getClassEnrollments()) {
			for (EnrollmentRequest request: enrollments)
				if (request.getCourse().getCourseId().equals(e.getCourseOffering().getUniqueId())) continue e;
			failures.add(new EnrollmentFailure(new XCourse(e.getCourseOffering()), new XSection(e.getClazz(), helper), "Cannot drop " + e.getClazz().getClassLabel(e.getCourseOffering()) + ".", true));
		}
		return failures;
	}

	@Override
	public XEnrollment resection(OnlineSectioningServer server, OnlineSectioningHelper helper, SectioningRequest sectioningRequest, XEnrollment enrollment) throws SectioningException {
		return null;
	}

	@Override
	public boolean requestUpdate(OnlineSectioningServer server, OnlineSectioningHelper helper, Collection<XStudent> students) throws SectioningException {
		return false;
	}

	@Override
	public void dispose() {
	}

	@Override
	public boolean isAllowWaitListing() {
		return false;
	}

	@Override
	public boolean isCanRequestUpdates() {
		return false;
	}

}
