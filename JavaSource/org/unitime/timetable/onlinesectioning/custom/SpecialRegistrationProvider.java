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

import java.util.List;

import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.EligibilityCheck;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.SpecialRegistrationEligibilityRequest;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.SpecialRegistrationEligibilityResponse;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.RetrieveSpecialRegistrationRequest;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.RetrieveSpecialRegistrationResponse;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.SubmitSpecialRegistrationRequest;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.SubmitSpecialRegistrationResponse;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.model.XStudent;

/**
 * 
 * @author Tomas Muller
 *
 */
public interface SpecialRegistrationProvider {
	
	public SpecialRegistrationEligibilityResponse checkEligibility(OnlineSectioningServer server, OnlineSectioningHelper helper, XStudent student, SpecialRegistrationEligibilityRequest request) throws SectioningException;
	
	public SubmitSpecialRegistrationResponse submitRegistration(OnlineSectioningServer server, OnlineSectioningHelper helper, XStudent student, SubmitSpecialRegistrationRequest request) throws SectioningException;
	
	public RetrieveSpecialRegistrationResponse retrieveRegistration(OnlineSectioningServer server, OnlineSectioningHelper helper, XStudent student, RetrieveSpecialRegistrationRequest request) throws SectioningException;
	
	public List<RetrieveSpecialRegistrationResponse> retrieveAllRegistrations(OnlineSectioningServer server, OnlineSectioningHelper helper, XStudent student) throws SectioningException;
	
	public void checkEligibility(OnlineSectioningServer server, OnlineSectioningHelper helper, EligibilityCheck check, XStudent student) throws SectioningException;
	
	public void dispose();
}
