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
package org.unitime.timetable.server.exams;

import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.ExaminationMessages;
import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.client.exams.ExamsInterface.RoomAvailabilityFilterRequest;
import org.unitime.timetable.gwt.client.offerings.OfferingsInterface.ClassesFilterResponse;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.FilterInterface.FilterParameterInterface;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.webutil.BackTracker;

@GwtRpcImplements(RoomAvailabilityFilterRequest.class)
public class RoomAvailabilityFilterBackend implements GwtRpcImplementation<RoomAvailabilityFilterRequest, ClassesFilterResponse>{
	protected static ExaminationMessages MESSAGES = Localization.create(ExaminationMessages.class);
	protected static GwtMessages GWT = Localization.create(GwtMessages.class);

	@Override
	public ClassesFilterResponse execute(RoomAvailabilityFilterRequest request, SessionContext context) {
		context.checkPermission(Right.RoomAvailability);
		ClassesFilterResponse filter = new ClassesFilterResponse();
		
		FilterParameterInterface examType = new FilterParameterInterface();
		examType.setName("examType");
		examType.setType("list");
		examType.setMultiSelect(false);
		examType.setCollapsible(false);
		examType.setLabel(MESSAGES.propExamType());
        for (ExamType type: ExamType.findAllUsedApplicable(context.getUser(), DepartmentStatusType.Status.ExamView, DepartmentStatusType.Status.ExamTimetable))
        	examType.addOption(type.getUniqueId().toString(), type.getLabel());
        Object et = context.getAttribute(SessionAttribute.ExamType);
        examType.setDefaultValue(et == null ? null : et.toString());
        if (!examType.hasDefaultValue() && examType.hasOptions())
        	examType.setDefaultValue(examType.getOptions().get(0).getValue());
		filter.addParameter(examType);
		
		FilterParameterInterface roomFilter = new FilterParameterInterface();
		roomFilter.setName("roomFilter");
		roomFilter.setLabel(GWT.propRoomFilter());
		roomFilter.setType("text");
		roomFilter.setDefaultValue(context.getUser().getProperty("RoomAvailability.roomFilter", ""));
		roomFilter.setSessionId(context.getUser().getCurrentAcademicSessionId());
		roomFilter.setCollapsible(false);
		filter.addParameter(roomFilter);
		
		filter.addParameter(createToggle(context, "includeExams", MESSAGES.filterDisplayExaminations(), false));
		filter.addParameter(createToggle(context, "compare", MESSAGES.filterCompareExaminations(), false));
		
		filter.setSticky(CommonValues.Yes.eq(UserProperty.StickyTables.get(context.getUser())));
		filter.setSessionId(context.getUser().getCurrentAcademicSessionId());
		filter.setCanExport(true);

		BackTracker.markForBack(context, null, null, false, true); //clear back list
		
		return filter;
	}
	
	protected FilterParameterInterface createToggle(SessionContext context, String name, String label, Boolean defaultValue) {
		FilterParameterInterface toggle = new FilterParameterInterface();
		toggle.setName(name);
		toggle.setLabel(label);
		toggle.setType("boolean");
		toggle.setDefaultValue(context.getUser().getProperty("RoomAvailability." + name, defaultValue == null ? null : defaultValue ? "1" : "0"));
		toggle.setCollapsible(false);
		return toggle;
	}

}
