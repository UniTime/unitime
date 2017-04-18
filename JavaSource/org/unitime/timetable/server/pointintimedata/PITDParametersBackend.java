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
package org.unitime.timetable.server.pointintimedata;

import java.util.Collections;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.PointInTimeDataReportsInterface;
import org.unitime.timetable.gwt.shared.PointInTimeDataReportsInterface.PITDParametersInterface;
import org.unitime.timetable.gwt.shared.PointInTimeDataReportsInterface.PITDParametersRpcRequest;
import org.unitime.timetable.reports.pointintimedata.BasePointInTimeDataReports;
import org.unitime.timetable.security.SessionContext;

/**
 * @author Stephanie Schluttenhofer
 */
@GwtRpcImplements(PITDParametersRpcRequest.class)
public class PITDParametersBackend implements GwtRpcImplementation<PITDParametersRpcRequest, PITDParametersInterface> {
	protected static GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	
    @Autowired 
	private SessionContext sessionContext;

	@Override
	@PreAuthorize("checkPermission('PointInTimeDataReports')")
	public PITDParametersInterface execute(PITDParametersRpcRequest request, SessionContext context) {
		PITDParametersInterface ret = new PITDParametersInterface();
		
		for (BasePointInTimeDataReports.Parameter p: BasePointInTimeDataReports.Parameter.values()) {
			if (!p.allowSingleSelection() && !p.allowMultiSelection()) continue;
			PointInTimeDataReportsInterface.Parameter parameter = new PointInTimeDataReportsInterface.Parameter();
			parameter.setMultiSelect(p.allowMultiSelection());
			parameter.setName(getLocalizedText(p));
			parameter.setType(p.name());
			parameter.setTextField(p.isTextField());
			parameter.setDefaultTextValue(p.defaultValue(sessionContext.getUser()));
			Map<Long, String> values = p.values(sessionContext.getUser());
			if (p.isTextField()){
				ret.addParameter(parameter);
			}
			if (values == null || values.isEmpty()) continue;
			for (Map.Entry<Long, String> e: values.entrySet()) {
				PointInTimeDataReportsInterface.IdValue v = new PointInTimeDataReportsInterface.IdValue();
				v.setText(e.getValue());
				v.setValue(e.getKey().toString());
				parameter.values().add(v);
			}
			Collections.sort(parameter.values());
			ret.addParameter(parameter);
			
		}
				
		ret.setEditable(false);
		
		return ret;
	}

	private String getLocalizedText(BasePointInTimeDataReports.Parameter parameter) {
		switch (parameter) {
		case PITD:
			return MESSAGES.optionPointInTimeData();
		case PITD2:
			return MESSAGES.optionPointInTimeDataComparison();
		case BUILDING:
			return MESSAGES.optionBuilding();
		case BUILDINGS:
			return MESSAGES.optionBuildings();
		case DEPARTMENT:
			return MESSAGES.optionDepartment();
		case DEPARTMENTS:
			return MESSAGES.optionDepartments();
		case ROOM:
			return MESSAGES.optionRoom();
		case ROOMS:
			return MESSAGES.optionRooms();
		case SESSION:
			return MESSAGES.optionAcademicSession();
		case SUBJECT:
			return MESSAGES.optionSubjectArea();
		case SUBJECTS:
			return MESSAGES.optionSubjectAreas();
		case MINUTES_IN_REPORTING_HOUR:
			return(MESSAGES.optionMinutesInReportingHour());
		case WEEKS_IN_REPORTING_TERM:
			return(MESSAGES.optionWeeksInReportingTerm());
		case MINIMUM_LOCATION_CAPACITY:
			return(MESSAGES.optionMinimumLocationCapacity());
		case MAXIMUM_LOCATION_CAPACITY:
			return(MESSAGES.optionMaximumLocationCapacity());
		case DistributionType:
			return MESSAGES.optionDistributionType();
		case DistributionTypes:
			return MESSAGES.optionDistributionTypes();
		case DemandOfferingType:
			return MESSAGES.optionDemandOfferingType();
		case DemandOfferingTypes:
			return MESSAGES.optionDemandOfferingTypes();
		case OfferingConsentType:
			return MESSAGES.optionOfferingConsentType();
		case OfferingConsentTypes:
			return MESSAGES.optionOfferingConsentTypes();
		case CourseCreditFormat:
			return MESSAGES.optionCourseCreditFormat();
		case CourseCreditFormats:
			return MESSAGES.optionCourseCreditFormats();
		case CourseCreditType:
			return MESSAGES.optionCourseCreditType();
		case CourseCreditTypes:
			return MESSAGES.optionCourseCreditTypes();
		case CourseCreditUnitType:
			return MESSAGES.optionCourseCreditUnitType();
		case CourseCreditUnitTypes:
			return MESSAGES.optionCourseCreditUnitTypes();
		case PositionType:
			return MESSAGES.optionPositionType();
		case PositionTypes:
			return MESSAGES.optionPositionTypes();
		case DepartmentStatusType:
			return MESSAGES.optionDepartmentStatusType();
		case DepartmentStatusTypes:
			return MESSAGES.optionDepartmentStatusTypes();
		case RoomType:
			return MESSAGES.optionRoomType();
		case RoomTypes:
			return MESSAGES.optionRoomTypes();
		case StudentSectioningStatus:
			return MESSAGES.optionStudentSectioningStatus();
		case StudentSectioningStatuses:
			return MESSAGES.optionStudentSectioningStatuses();
		case ExamType:
			return MESSAGES.optionExamType();
		case ExamTypes:
			return MESSAGES.optionExamTypes();
		case RoomFeatureType:
			return MESSAGES.optionRoomFeatureType();
		case RoomFeatureTypes:
			return MESSAGES.optionRoomFeatureTypes();
		case CourseType:
			return MESSAGES.optionCourseType();
		case CourseTypes:
			return MESSAGES.optionCourseTypes();
		default:
			return parameter.text();
		}
	}
	
}
