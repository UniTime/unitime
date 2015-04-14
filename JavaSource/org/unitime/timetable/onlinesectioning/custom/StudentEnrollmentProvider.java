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
package org.unitime.timetable.onlinesectioning.custom;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.cpsolver.ifs.util.ToolBox;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.EligibilityCheck;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.model.XCourse;
import org.unitime.timetable.onlinesectioning.model.XEnrollment;
import org.unitime.timetable.onlinesectioning.model.XSection;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.solver.SectioningRequest;

public interface StudentEnrollmentProvider {

	public void checkEligibility(OnlineSectioningServer server, OnlineSectioningHelper helper, EligibilityCheck check, XStudent student) throws SectioningException;
	
	public List<EnrollmentFailure> enroll(OnlineSectioningServer server, OnlineSectioningHelper helper, XStudent student, List<EnrollmentRequest> enrollments, Set<Long> lockedCourses) throws SectioningException;
	
	public XEnrollment resection(OnlineSectioningServer server, OnlineSectioningHelper helper, SectioningRequest sectioningRequest, XEnrollment enrollment) throws SectioningException;
	
	public boolean requestUpdate(OnlineSectioningServer server, OnlineSectioningHelper helper, Collection<XStudent> students) throws SectioningException;
	
	public void dispose();
	
	public boolean isAllowWaitListing();
	
	public boolean isCanRequestUpdates();
	
	public static class EnrollmentFailure implements Serializable {
		private static final long serialVersionUID = 1L;
		private XCourse iCourse;
		private XSection iSection;
		private String iMessage;
		private boolean iEnrolled;
		
		public EnrollmentFailure(XCourse course, XSection section, String message, boolean enrolled) {
			iCourse = course;
			iSection = section;
			iMessage = message;
			iEnrolled = enrolled;
		}
		
		public XCourse getCourse() { return iCourse; }
		public XSection getSection() { return iSection; }
		public String getMessage() { return iMessage; }
		public boolean isEnrolled() { return iEnrolled; }
		
		public String toString() {
			return getCourse().getCourseName() + " " + getSection().getSubpartName() + " " + getSection().getName(getCourse().getCourseId()) + ": " + getMessage() + (isEnrolled() ? " (e)" : "");
		}
	}
	
	public static class EnrollmentRequest implements Serializable {
		private static final long serialVersionUID = 1L;
		private XCourse iCourse;
		private List<XSection> iSections;
		
		public EnrollmentRequest(XCourse course, List<XSection> sections) {
			iCourse = course; iSections = sections;
		}
		
		public XCourse getCourse() { return iCourse; }
		public List<XSection> getSections() { return iSections; }
		
		public String toString() {
			return getCourse().getCourseName() + ": " + ToolBox.col2string(getSections(), 2);
		}
	}
}
