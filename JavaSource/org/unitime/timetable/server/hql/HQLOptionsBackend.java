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
package org.unitime.timetable.server.hql;

import java.util.Collections;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.SavedHQLInterface;
import org.unitime.timetable.gwt.shared.SavedHQLInterface.HQLOptionsInterface;
import org.unitime.timetable.gwt.shared.SavedHQLInterface.HQLOptionsRpcRequest;
import org.unitime.timetable.model.SavedHQL;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(HQLOptionsRpcRequest.class)
public class HQLOptionsBackend implements GwtRpcImplementation<HQLOptionsRpcRequest, HQLOptionsInterface> {
	protected static GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	
    @Autowired 
	private SessionContext sessionContext;

	@Override
	@PreAuthorize("checkPermission('HQLReports')")
	public HQLOptionsInterface execute(HQLOptionsRpcRequest request, SessionContext context) {
		HQLOptionsInterface ret = new HQLOptionsInterface();
		
		for (SavedHQL.Option o: SavedHQL.Option.values()) {
			if (!o.allowSingleSelection() && !o.allowMultiSelection()) continue;
			SavedHQLInterface.Option option = new SavedHQLInterface.Option();
			option.setMultiSelect(o.allowMultiSelection());
			option.setName(getLocalizedText(o));
			option.setType(o.name());
			Map<Long, String> values = o.values(sessionContext.getUser());
			if (values == null || values.isEmpty()) continue;
			for (Map.Entry<Long, String> e: values.entrySet()) {
				SavedHQLInterface.IdValue v = new SavedHQLInterface.IdValue();
				v.setText(e.getValue());
				v.setValue(e.getKey().toString());
				option.values().add(v);
			}
			Collections.sort(option.values());
			ret.addOption(option);
		}
		
		for (SavedHQL.Flag f: SavedHQL.Flag.values()) {
			SavedHQLInterface.Flag flag = new SavedHQLInterface.Flag();
			flag.setValue(f.flag());
			flag.setText(getLocalizedDescription(f));
			flag.setAppearance(f.getAppearance());
			ret.addFlag(flag);
		}
		
		ret.setEditable(sessionContext.hasPermission(Right.HQLReportAdd));
		
		return ret;
	}

	private String getLocalizedText(SavedHQL.Option option) {
		switch (option) {
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
			return option.text();
		}
	}
	
	private String getLocalizedDescription(SavedHQL.Flag flag) {
		switch (flag) {
		case APPEARANCE_COURSES:
			return MESSAGES.flagAppearanceCourses();
		case APPEARANCE_EXAMS:
			return MESSAGES.flagAppearanceExaminations();
		case APPEARANCE_EVENTS:
			return MESSAGES.flagAppearanceEvents();
		case APPEARANCE_SECTIONING:
			return MESSAGES.flagAppearanceStudentSectioning();
		case APPEARANCE_ADMINISTRATION:
			return MESSAGES.flagAppearanceAdministration();
		case ADMIN_ONLY:
			return MESSAGES.flagRestrictionsAdministratorOnly();
		default:
			return flag.description();
		}
	}
}
