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

import java.util.List;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.RetrieveSpecialRegistrationResponse;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.RetrieveAllSpecialRegistrationsRequest;
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
public class SpecialRegistrationRetrieveAll implements OnlineSectioningAction<List<RetrieveSpecialRegistrationResponse>> {
	private static final long serialVersionUID = 1L;
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
	private RetrieveAllSpecialRegistrationsRequest iRequest;
	
	public SpecialRegistrationRetrieveAll withRequest(RetrieveAllSpecialRegistrationsRequest request) {
		iRequest = request;
		return this;
	}

	public RetrieveAllSpecialRegistrationsRequest getRequest() { return iRequest; }
	public Long getStudentId() { return iRequest.getStudentId(); }
	
	@Override
	public List<RetrieveSpecialRegistrationResponse> execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		if (!server.getAcademicSession().isSectioningEnabled() || !CustomSpecialRegistrationHolder.hasProvider())
			throw new SectioningException(MSG.exceptionNotSupportedFeature());

		Lock lock = server.lockStudent(getStudentId(), null, name());
		try {
			OnlineSectioningLog.Action.Builder action = helper.getAction();
						
			XStudent student = server.getStudent(getStudentId());

			action.getStudentBuilder().setUniqueId(student.getStudentId())
				.setExternalId(student.getExternalId())
				.setName(student.getName());
			
			List<RetrieveSpecialRegistrationResponse> response = CustomSpecialRegistrationHolder.getProvider().retrieveAllRegistrations(server, helper, student);
			
			return response;
		} finally {
			lock.release();
		}
	}

	@Override
	public String name() {
		return "specreg-getall";
	}

}
