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
package org.unitime.timetable.onlinesectioning.advisors;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.CheckCoursesResponse;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.CourseMessage;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.AdvisingStudentDetails;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.custom.AdvisorCourseRequestsValidationProvider;
import org.unitime.timetable.onlinesectioning.custom.Customization;

/**
 * @author Tomas Muller
 */
public class AdvisorCourseRequestsValidate implements OnlineSectioningAction<CheckCoursesResponse> {
	private static final long serialVersionUID = 1L;
	protected static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
	protected static StudentSectioningConstants CONST = Localization.create(StudentSectioningConstants.class);
	private AdvisingStudentDetails iDetails;
	
	public AdvisorCourseRequestsValidate withDetails(AdvisingStudentDetails details) {
		iDetails = details;
		return this;
	}
	
	public AdvisingStudentDetails getDetails() { return iDetails; }

	@Override
	public CheckCoursesResponse execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		try {
		
			OnlineSectioningLog.Action.Builder action = helper.addAction(this, server.getAcademicSession());
			action.setStudent(OnlineSectioningLog.Entity.newBuilder()
				.setUniqueId(getDetails().getStudentId())
				.setExternalId(getDetails().getStudentExternalId())
				.setName(getDetails().getStudentName()));
			if (getDetails().getStatus() != null && getDetails().getStatus().getUniqueId() != null) {
				action.addOther(OnlineSectioningLog.Entity.newBuilder()
						.setUniqueId(getDetails().getStatus().getUniqueId())
						.setName(getDetails().getStatus().getLabel())
						.setExternalId(getDetails().getStatus().getReference())
						.setType(OnlineSectioningLog.Entity.EntityType.OTHER));
			}
			if (getDetails().getRequest() != null)
				for (OnlineSectioningLog.Request r: OnlineSectioningHelper.toProto(getDetails().getRequest()))
					action.addRequest(r);
			
			AdvisorCourseRequestsValidationProvider provider = Customization.AdvisorCourseRequestsValidationProvider.getProvider();
			
			if (provider == null)
				return null;
			
			CheckCoursesResponse ret = new CheckCoursesResponse();
			provider.validateAdvisorRecommendations(server, helper, getDetails(), ret);
			
			if (ret.hasMessages())
				for (CourseMessage cm: ret.getMessages())
					if (cm.hasCourse()) {
						action.addMessage(OnlineSectioningLog.Message.newBuilder()
								.setLevel(cm.isError() ? OnlineSectioningLog.Message.Level.ERROR : cm.isConfirm() ? OnlineSectioningLog.Message.Level.WARN : OnlineSectioningLog.Message.Level.INFO)
								.setText(cm.toString())
								.setTimeStamp(System.currentTimeMillis()));
					}
			if (ret.hasCreditNote())
				action.addMessage(OnlineSectioningLog.Message.newBuilder()
						.setLevel(OnlineSectioningLog.Message.Level.INFO)
						.setText(ret.getCreditNote())
						.setTimeStamp(System.currentTimeMillis()));
			if (ret.hasCreditWarning())
				action.addMessage(OnlineSectioningLog.Message.newBuilder()
						.setLevel(OnlineSectioningLog.Message.Level.WARN)
						.setText(ret.getCreditWarning())
						.setTimeStamp(System.currentTimeMillis()));
			if (ret.hasErrorMessage())
				action.addMessage(OnlineSectioningLog.Message.newBuilder()
						.setLevel(OnlineSectioningLog.Message.Level.ERROR)
						.setText(ret.getErrorMessage())
						.setTimeStamp(System.currentTimeMillis()));
			
			if (ret.isError())
				action.setResult(OnlineSectioningLog.Action.ResultType.FAILURE);
			else if (ret.isConfirm())
				action.setResult(OnlineSectioningLog.Action.ResultType.FALSE);
			else
				action.setResult(OnlineSectioningLog.Action.ResultType.TRUE);

			return ret;
		} catch (Exception e) {
			helper.error("Failed to validate: " + e.getMessage(), e);
			throw new SectioningException(e.getMessage(), e);
		}
	}

	@Override
	public String name() {
		return "advisor-validate";
	}

}
