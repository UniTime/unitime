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
import java.util.Hashtable;
import java.util.Map;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.SpecialRegistrationEligibilityRequest;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.SpecialRegistrationEligibilityResponse;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.Lock;
import org.unitime.timetable.onlinesectioning.custom.CustomSpecialRegistrationHolder;
import org.unitime.timetable.onlinesectioning.model.XStudent;

/**
 * @author Tomas Muller
 */
public class SpecialRegistrationEligibility implements OnlineSectioningAction<SpecialRegistrationEligibilityResponse> {
	private static final long serialVersionUID = 1L;
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
	private SpecialRegistrationEligibilityRequest iRequest;
	
	public SpecialRegistrationEligibility withRequest(SpecialRegistrationEligibilityRequest request) {
		iRequest = request;
		return this;
	}

	public SpecialRegistrationEligibilityRequest getRequest() { return iRequest; }
	public Collection<ClassAssignmentInterface.ClassAssignment> getAssignment() { return iRequest.getClassAssignments(); }
	public Long getStudentId() { return iRequest.getStudentId(); }
	
	@Override
	public SpecialRegistrationEligibilityResponse execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		if (!server.getAcademicSession().isSectioningEnabled() || !CustomSpecialRegistrationHolder.hasProvider())
			throw new SectioningException(MSG.exceptionNotSupportedFeature());
		Lock lock = server.lockStudent(getStudentId(), null, name());
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
			
			XStudent student = server.getStudent(getStudentId());

			action.getStudentBuilder().setUniqueId(student.getStudentId())
				.setExternalId(student.getExternalId())
				.setName(student.getName());
			
			SpecialRegistrationEligibilityResponse response = CustomSpecialRegistrationHolder.getProvider().checkEligibility(server, helper, student, getRequest());
			
			return response;
		} finally {
			lock.release();
		}
	}

	@Override
	public String name() {
		return "specreg-eligibility";
	}

}
