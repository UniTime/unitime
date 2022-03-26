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
package org.unitime.timetable.server.courses;

import java.util.Map;
import java.util.TreeSet;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.CourseOfferingInterface;
import org.unitime.timetable.gwt.shared.CourseOfferingInterface.CoordinatorInterface;
import org.unitime.timetable.gwt.shared.CourseOfferingInterface.GetCourseOfferingRequest;
import org.unitime.timetable.gwt.shared.CourseOfferingInterface.GetCourseOfferingResponse;
import org.unitime.timetable.interfaces.ExternalLinkLookup;
import org.unitime.timetable.model.CourseCreditUnitConfig;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.FixedCreditUnitConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.OfferingCoordinator;
import org.unitime.timetable.model.OverrideType;
import org.unitime.timetable.model.VariableFixedCreditUnitConfig;
import org.unitime.timetable.model.VariableRangeCreditUnitConfig;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.Constants;

@GwtRpcImplements(GetCourseOfferingRequest.class)
public class GetCourseOfferingBackend implements GwtRpcImplementation<GetCourseOfferingRequest, GetCourseOfferingResponse> {
	
	@Override
	public GetCourseOfferingResponse execute(GetCourseOfferingRequest request, SessionContext context) {
		if (!context.hasPermission(request.getCourseOfferingId(), "CourseOffering", Right.EditCourseOffering) && !context.hasPermission(request.getCourseOfferingId(), "CourseOffering", Right.EditCourseOfferingCoordinators) && !context.hasPermission(request.getCourseOfferingId(), "CourseOffering", Right.EditCourseOfferingNote)) {
			//If they don't have any of the permissions, reject based on any of them.
			context.checkPermission(request.getCourseOfferingId(), "CourseOffering", Right.EditCourseOffering);
		}

		GetCourseOfferingResponse response = new GetCourseOfferingResponse();
	
		CourseOffering courseOffering = CourseOffering.findByUniqueId(request.getCourseOfferingId());
		InstructionalOffering instructionalOffering = courseOffering.getInstructionalOffering();
		
		CourseOfferingInterface cof = new CourseOfferingInterface();
		cof.setId(courseOffering.getUniqueId());
		cof.setInstrOfferingId(instructionalOffering.getUniqueId());
		cof.setCourseNbr(courseOffering.getCourseNbr());
		cof.setTitle(courseOffering.getTitle());
		cof.setCourseName(courseOffering.getCourseName());
		cof.setScheduleBookNote(courseOffering.getScheduleBookNote());
		cof.setSubjectAreaAbbv(courseOffering.getSubjectAreaAbbv());
		cof.setSubjectAreaId(courseOffering.getSubjectArea().getUniqueId());
		if (courseOffering.getConsentType() != null) {
			cof.setConsent(courseOffering.getConsentType().getUniqueId());
			cof.setConsentText(courseOffering.getConsentType().getLabel());
		} else {
			cof.setConsent(new Long(-1));
			cof.setConsentText("");
		}

		if (courseOffering.getCredit() != null) {
			CourseCreditUnitConfig credit = courseOffering.getCredit();
			cof.setCreditText(credit.creditText());
			cof.setCreditFormat(credit.getCreditFormat());
			cof.setCreditType(credit.getCreditType().getUniqueId());
			cof.setCreditUnitType(credit.getCreditUnitType().getUniqueId());
			if (credit instanceof FixedCreditUnitConfig){
				cof.setUnits(((FixedCreditUnitConfig) credit).getFixedUnits());
			} else if (credit instanceof VariableFixedCreditUnitConfig){
				cof.setUnits(((VariableFixedCreditUnitConfig) credit).getMinUnits());
				cof.setMaxUnits(((VariableFixedCreditUnitConfig) credit).getMaxUnits());
				if (credit instanceof VariableRangeCreditUnitConfig){
					cof.setFractionalIncrementsAllowed(((VariableRangeCreditUnitConfig) credit).isFractionalIncrementsAllowed());
				}
			}
		}
		
		cof.setExternalId(courseOffering.getExternalUniqueId());
		
		response.setWkEnrollDefault(instructionalOffering.getSession().getLastWeekToEnroll());
		response.setWkChangeDefault(instructionalOffering.getSession().getLastWeekToChange());
		response.setWkDropDefault(instructionalOffering.getSession().getLastWeekToDrop());
		response.setWeekStartDayOfWeek(Localization.getDateFormat("EEEE").format(instructionalOffering.getSession().getSessionBeginDateTime()));
		
		if (courseOffering.getDemandOffering() != null) {
			cof.setDemandOfferingId(courseOffering.getDemandOffering().getUniqueId());
			cof.setDemandOfferingText(courseOffering.getDemandOffering().getCourseNameWithTitle());
		}
		
		if (courseOffering.getAlternativeOffering() != null) {
			cof.setAlternativeCourseOfferingId(courseOffering.getAlternativeOffering().getUniqueId());
		}
		
		if (courseOffering.getFundingDept() != null) {
			cof.setFundingDepartmentId(courseOffering.getFundingDept().getUniqueId());
		}
		
		if (courseOffering.getEffectiveFundingDept() != null) {
			cof.setEffectiveFundingDepartmentId(courseOffering.getEffectiveFundingDept().getUniqueId());
		}
		
		
		if (courseOffering.getCourseType() != null) {
			cof.setCourseTypeId(courseOffering.getCourseType().getUniqueId());
		}

		for (OfferingCoordinator coordinator: new TreeSet<OfferingCoordinator>(instructionalOffering.getOfferingCoordinators())) {
			CoordinatorInterface coordinatorObject = new CoordinatorInterface();
			coordinatorObject.setInstructorId(coordinator.getInstructor().getUniqueId().toString());
			coordinatorObject.setPercShare(coordinator.getPercentShare() == null ? "0" : coordinator.getPercentShare().toString());
			coordinatorObject.setResponsibilityId(coordinator.getResponsibility() == null ? Constants.BLANK_OPTION_VALUE : coordinator.getResponsibility().getUniqueId().toString());
			cof.addCoordinator(coordinatorObject);
		}
		
		cof.setByReservationOnly(instructionalOffering.getByReservationOnly());
		cof.setLastWeekToChange(instructionalOffering.getLastWeekToChange());
		cof.setLastWeekToDrop(instructionalOffering.getLastWeekToDrop());
		cof.setLastWeekToEnroll(instructionalOffering.getLastWeekToEnroll());
		cof.setNotes(instructionalOffering.getNotes());
		cof.setInstrOfferingId(instructionalOffering.getUniqueId());
		
		cof.setIsControl(courseOffering.getIsControl());
		
		try {
			if (cof.getIsControl() == true) {
				@SuppressWarnings("deprecation")
				String linkLookupClass = ApplicationProperty.CourseCatalogLinkProvider.value();
	            if (linkLookupClass!=null && linkLookupClass.trim().length()>0) {
	            	ExternalLinkLookup lookup = (ExternalLinkLookup) (Class.forName(linkLookupClass).newInstance());
	           		Map results = lookup.getLink(instructionalOffering);
	                if (results==null) {
	                    throw new Exception (lookup.getErrorMessage());
	                }
	                
	                cof.setCatalogLinkLabel((String)results.get(ExternalLinkLookup.LINK_LABEL));
	                cof.setCatalogLinkLocation((String)results.get(ExternalLinkLookup.LINK_LOCATION));
	            }
			}
		} catch (Exception e) {}

		cof.setIoNotOffered(instructionalOffering.getNotOffered());

		cof.clearCourseOverrides();
		for (OverrideType override: courseOffering.getDisabledOverrides()) {
			cof.addCourseOverride(override.getUniqueId().toString());
		}
		
		cof.setWaitList(instructionalOffering.getWaitlist());

		response.setCourseOffering(cof);

		return response;
	}
}
