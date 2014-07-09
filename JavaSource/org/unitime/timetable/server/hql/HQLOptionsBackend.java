/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2014, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
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
