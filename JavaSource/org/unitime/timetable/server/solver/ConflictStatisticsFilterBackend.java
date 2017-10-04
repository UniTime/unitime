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

import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.CourseTimetablingSolverInterface.ConflictStatisticsFilterRequest;
import org.unitime.timetable.gwt.shared.CourseTimetablingSolverInterface.ConflictStatisticsFilterResponse;
import org.unitime.timetable.gwt.shared.FilterInterface.FilterParameterInterface;
import org.unitime.timetable.gwt.shared.SolverInterface.SolverType;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.PreferenceInterface;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.SuggestionProperties;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.SolverProxy;
import org.unitime.timetable.solver.service.SolverService;
import org.unitime.timetable.util.Constants;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(ConflictStatisticsFilterRequest.class)
public class ConflictStatisticsFilterBackend implements GwtRpcImplementation<ConflictStatisticsFilterRequest, ConflictStatisticsFilterResponse> {
	protected static GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	protected static GwtConstants CONSTANTS = Localization.create(GwtConstants.class);
	
	@Autowired SolverService<SolverProxy> courseTimetablingSolverService;

	@Override
	public ConflictStatisticsFilterResponse execute(ConflictStatisticsFilterRequest request, SessionContext context) {
		context.checkPermission(Right.ConflictStatistics);
		ConflictStatisticsFilterResponse response = new ConflictStatisticsFilterResponse();
		
		FilterParameterInterface mode = new FilterParameterInterface();
		mode.setName("mode");
		mode.setType("list");
		mode.setMultiSelect(false);
		mode.addOption("0", MESSAGES.modeCBSVariables());
		mode.addOption("1", MESSAGES.modeCBSConstraints());
		mode.setDefaultValue(context.getUser().getProperty("Cbs.type", "0"));
		mode.setLabel(MESSAGES.propConflictStatisticsMode());
		response.addParameter(mode);
		
		FilterParameterInterface limit = new FilterParameterInterface();
		limit.setName("limit");
		limit.setType("double");
		limit.setDefaultValue(context.getUser().getProperty("Cbs.limit", "25.0"));
		limit.setLabel(MESSAGES.propConflictStatisticsLimit());
		limit.setSuffix(CONSTANTS.percentageSign());
		response.addParameter(limit);
		
		SuggestionProperties properties = new SuggestionProperties();
		for (PreferenceLevel pref: PreferenceLevel.getPreferenceLevelList(false)) {
			properties.addPreference(new PreferenceInterface(
					pref.getUniqueId(),
					PreferenceLevel.prolog2color(pref.getPrefProlog()),
					pref.getPrefProlog(),
					pref.getPrefName(),
					pref.getAbbreviation(),
					Constants.preference2preferenceLevel(pref.getPrefProlog())));
		}
		properties.setSolver(courseTimetablingSolverService.getSolver() != null);
		response.setSuggestionProperties(properties);
		
		SolverPageBackend.fillSolverWarnings(context, courseTimetablingSolverService.getSolver(), SolverType.COURSE, response);
		
		return response;
	}

}