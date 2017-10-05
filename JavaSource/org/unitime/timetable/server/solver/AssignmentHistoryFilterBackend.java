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
package org.unitime.timetable.server.solver;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.CourseTimetablingSolverInterface.AssignmentHistoryFilterRequest;
import org.unitime.timetable.gwt.shared.CourseTimetablingSolverInterface.AssignmentHistoryFilterResponse;
import org.unitime.timetable.gwt.shared.FilterInterface.FilterParameterInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.PreferenceInterface;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(AssignmentHistoryFilterRequest.class)
public class AssignmentHistoryFilterBackend implements GwtRpcImplementation<AssignmentHistoryFilterRequest, AssignmentHistoryFilterResponse> {
	protected static GwtMessages MESSAGES = Localization.create(GwtMessages.class);

	@Override
	public AssignmentHistoryFilterResponse execute(AssignmentHistoryFilterRequest request, SessionContext context) {
		context.checkPermission(Right.AssignmentHistory);
		AssignmentHistoryFilterResponse response = new AssignmentHistoryFilterResponse();
		
		FilterParameterInterface simplifiedMode = new FilterParameterInterface();
		simplifiedMode.setName("simpleMode");
		simplifiedMode.setType("boolean");
		simplifiedMode.setLabel(MESSAGES.propCourseTimetablingSolverSimplifiedMode());
		simplifiedMode.setDefaultValue(context.getUser().getProperty("SuggestionsModel.simpleMode", "0"));
		response.addParameter(simplifiedMode);
		
		for (PreferenceLevel pref: PreferenceLevel.getPreferenceLevelList(true))
			response.addPreference(new PreferenceInterface(pref.getUniqueId(), PreferenceLevel.prolog2bgColor(pref.getPrefProlog()), pref.getPrefProlog(), pref.getPrefName(), pref.getAbbreviation(), false));
		
		return response;
	}

}