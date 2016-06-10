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
package org.unitime.timetable.server.instructor;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
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
			String mode = ApplicationProperty.RoomSharingMode.value(String.valueOf(1 + i), i < CONSTANTS.roomSharingModes().length ? CONSTANTS.roomSharingModes()[i] : null);
			if (mode == null || mode.isEmpty()) break;
			model.addMode(new RoomInterface.RoomSharingDisplayMode(mode));
		}
		model.setDefaultEditable(true);
		for (PreferenceLevel pref: PreferenceLevel.getPreferenceLevelList(request.isIncludeNotAvailable())) {
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
