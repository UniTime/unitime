/*
 * UniTime 3.4 - 3.5 (University Timetabling Application)
 * Copyright (C) 2012 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.server.instructor;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.client.instructor.InstructorAvailabilityWidget.InstructorAvailabilityModel;
import org.unitime.timetable.gwt.client.instructor.InstructorAvailabilityWidget.InstructorAvailabilityRequest;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.RoomInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomSharingOption;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.TimePref;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.webutil.RequiredTimeTable;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(InstructorAvailabilityRequest.class)
public class InstructorAvailabilityBackend implements GwtRpcImplementation<InstructorAvailabilityRequest, InstructorAvailabilityModel>{
	protected static final GwtConstants CONSTANTS = Localization.create(GwtConstants.class);
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);

	@Override
	public InstructorAvailabilityModel execute(InstructorAvailabilityRequest request, SessionContext context) {
		InstructorAvailabilityModel model = new InstructorAvailabilityModel();
		for (int i = 0; true; i++) {
			String mode = ApplicationProperties.getProperty("unitime.room.sharingMode" + (1 + i), i < CONSTANTS.roomSharingModes().length ? CONSTANTS.roomSharingModes()[i] : null);
			if (mode == null || mode.isEmpty()) break;
			model.addMode(new RoomInterface.RoomSharingDisplayMode(mode));
		}
		model.setDefaultEditable(true);
		for (PreferenceLevel pref: PreferenceLevel.getPreferenceLevelList()) {
			if (PreferenceLevel.sRequired.equals(pref.getPrefProlog())) continue;
			RoomSharingOption option = new RoomSharingOption(model.char2id(PreferenceLevel.prolog2char(pref.getPrefProlog())), pref.prefcolor(), "", pref.getPrefName(), true); 
			model.addOption(option);
			if (PreferenceLevel.sNeutral.equals(pref.getPrefProlog()))
				model.setDefaultOption(option);		
		}
		String defaultGridSize = RequiredTimeTable.getTimeGridSize(context.getUser());
		if (defaultGridSize != null)
			for (int i = 0; i < model.getModes().size(); i++) {
				if (model.getModes().get(i).getName().equals(defaultGridSize)) {
					model.setDefaultMode(i); break;
				}
			}
		model.setDefaultHorizontal(CommonValues.HorizontalGrid.eq(context.getUser().getProperty(UserProperty.GridOrientation)));
		model.setNoteEditable(false);
		
		if (request.getInstructorId() != null) {
			if (request.getInstructorId().length() > 200) {
				model.setPattern(request.getInstructorId());
			} else {
				DepartmentalInstructor instructor = DepartmentalInstructorDAO.getInstance().get(Long.valueOf(request.getInstructorId()));
				for (Preference pref: instructor.getPreferences()) {
					if (pref instanceof TimePref) {
						model.setPattern(((TimePref) pref).getPreference());
						break;
					}
				}
			}
		}
		
		return model;
	}

}
