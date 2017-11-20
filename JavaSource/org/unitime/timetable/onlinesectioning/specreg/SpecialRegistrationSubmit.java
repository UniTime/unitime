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
package org.unitime.timetable.onlinesectioning.specreg;

import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.SubmitSpecialRegistrationRequest;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.SubmitSpecialRegistrationResponse;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.Lock;
import org.unitime.timetable.onlinesectioning.custom.CustomSpecialRegistrationHolder;
import org.unitime.timetable.onlinesectioning.model.XCourse;
import org.unitime.timetable.onlinesectioning.model.XStudent;

/**
 * @author Tomas Muller
 */
public class SpecialRegistrationSubmit implements OnlineSectioningAction<SubmitSpecialRegistrationResponse> {
	private static final long serialVersionUID = 1L;
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
	private SubmitSpecialRegistrationRequest iRequest;
	
	public SpecialRegistrationSubmit withRequest(SubmitSpecialRegistrationRequest request) {
		iRequest = request;
		return this;
	}

	public SubmitSpecialRegistrationRequest getRequest() { return iRequest; }
	public Collection<ClassAssignmentInterface.ClassAssignment> getAssignment() { return iRequest.getClassAssignments(); }
	public Long getStudentId() { return iRequest.getStudentId(); }
	public CourseRequestInterface getCourses() { return iRequest.getCourses(); }
	
	@Override
	public SubmitSpecialRegistrationResponse execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		if (!server.getAcademicSession().isSectioningEnabled() || !CustomSpecialRegistrationHolder.hasProvider())
			throw new SectioningException(MSG.exceptionNotSupportedFeature());
		Set<Long> offeringIds = new HashSet<Long>();
		for (ClassAssignmentInterface.ClassAssignment ca: getAssignment())
			if (ca != null && !ca.isFreeTime() && !ca.isDummy() && !ca.isTeachingAssignment()) {
				XCourse course = server.getCourse(ca.getCourseId());
				if (course == null)
					throw new SectioningException(MSG.exceptionEnrollNotAvailable(MSG.clazz(ca.getSubject(), ca.getCourseNbr(), ca.getSubpart(), ca.getSection())));
				if (!server.isOfferingLocked(course.getOfferingId())) {
					offeringIds.add(course.getOfferingId());
				}
			}
		Lock lock = server.lockStudent(getStudentId(), offeringIds, name());
		try {
			OnlineSectioningLog.Action.Builder action = helper.getAction();
			
			if (getRequest().getStudentId() != null)
				action.setStudent(OnlineSectioningLog.Entity.newBuilder().setUniqueId(getStudentId()));
			
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
			if (getCourses() != null)
				for (OnlineSectioningLog.Request r: OnlineSectioningHelper.toProto(getCourses()))
					action.addRequest(r);
			
			XStudent student = server.getStudent(getStudentId());

			action.getStudentBuilder().setUniqueId(student.getStudentId())
				.setExternalId(student.getExternalId())
				.setName(student.getName());
			
			SubmitSpecialRegistrationResponse response = CustomSpecialRegistrationHolder.getProvider().submitRegistration(server, helper, student, getRequest());
			
			return response;
		} finally {
			lock.release();
		}
	}

	@Override
	public String name() {
		return "specreg-submit";
	}

}
