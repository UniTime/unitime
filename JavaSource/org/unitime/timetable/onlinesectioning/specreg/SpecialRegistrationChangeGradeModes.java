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

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.ChangeGradeModesRequest;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.ChangeGradeModesResponse;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.SpecialRegistrationGradeMode;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.Lock;
import org.unitime.timetable.onlinesectioning.custom.CustomSpecialRegistrationHolder;
import org.unitime.timetable.onlinesectioning.model.XCourseRequest;
import org.unitime.timetable.onlinesectioning.model.XEnrollment;
import org.unitime.timetable.onlinesectioning.model.XOffering;
import org.unitime.timetable.onlinesectioning.model.XRequest;
import org.unitime.timetable.onlinesectioning.model.XSection;
import org.unitime.timetable.onlinesectioning.model.XStudent;

/**
 * @author Tomas Muller
 */
public class SpecialRegistrationChangeGradeModes implements OnlineSectioningAction<ChangeGradeModesResponse> {
	private static final long serialVersionUID = 1L;
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
	private ChangeGradeModesRequest iRequest;
	
	public SpecialRegistrationChangeGradeModes withRequest(ChangeGradeModesRequest request) {
		iRequest = request;
		return this;
	}

	public ChangeGradeModesRequest getRequest() { return iRequest; }
	public Long getStudentId() { return iRequest.getStudentId(); }
	
	@Override
	public ChangeGradeModesResponse execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		if (!server.getAcademicSession().isSectioningEnabled() || !CustomSpecialRegistrationHolder.hasProvider())
			throw new SectioningException(MSG.exceptionNotSupportedFeature());
		Lock lock = server.lockStudent(getStudentId(), null, name());
		try {
			OnlineSectioningLog.Action.Builder action = helper.getAction();
			
			XStudent student = server.getStudent(getStudentId());

			action.getStudentBuilder().setUniqueId(student.getStudentId())
				.setExternalId(student.getExternalId())
				.setName(student.getName());

			OnlineSectioningLog.Enrollment.Builder enrollment = OnlineSectioningLog.Enrollment.newBuilder();
			enrollment.setType(OnlineSectioningLog.Enrollment.EnrollmentType.REQUESTED);
			for (XRequest request: student.getRequests()) {
				if (request instanceof XCourseRequest && ((XCourseRequest)request).getEnrollment() != null) {
					XEnrollment enrl = ((XCourseRequest)request).getEnrollment();
					XOffering offering = server.getOffering(enrl.getOfferingId());
					for (XSection section: offering.getSections(enrl)) {
						OnlineSectioningLog.Section.Builder sct = OnlineSectioningHelper.toProto(section, enrl);
						String ext = section.getExternalId(enrl.getCourseId());
						SpecialRegistrationGradeMode mode = (ext == null ? null : getRequest().get(ext));
						if (mode != null)
							sct.getClazzBuilder().addParameterBuilder().setKey("selectedGradeMode").setValue(mode.getCode());
						enrollment.addSection(sct);
					}
				}
			}
			action.addEnrollment(enrollment);
			
			ChangeGradeModesResponse response = CustomSpecialRegistrationHolder.getProvider().changeGradeModes(server, helper, student, getRequest());
			
			return response;
		} finally {
			lock.release();
		}
	}

	@Override
	public String name() {
		return "change-grade-modes";
	}

}