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
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.CourseTimetablingSolverInterface.AssignedClassesFilterRequest;
import org.unitime.timetable.gwt.shared.CourseTimetablingSolverInterface.AssignedClassesFilterResponse;
import org.unitime.timetable.gwt.shared.FilterInterface.FilterParameterInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.PreferenceInterface;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.Constants;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(AssignedClassesFilterRequest.class)
public class AssignedClassesFilterBackend implements GwtRpcImplementation<AssignedClassesFilterRequest, AssignedClassesFilterResponse> {
	protected static GwtMessages MESSAGES = Localization.create(GwtMessages.class);

	@Override
	public AssignedClassesFilterResponse execute(AssignedClassesFilterRequest request, SessionContext context) {
		context.checkPermission(Right.AssignedClasses);
		AssignedClassesFilterResponse response = new AssignedClassesFilterResponse();
		
		FilterParameterInterface simplifiedMode = new FilterParameterInterface();
		simplifiedMode.setName("simpleMode");
		simplifiedMode.setType("boolean");
		simplifiedMode.setLabel(MESSAGES.propCourseTimetablingSolverSimplifiedMode());
		simplifiedMode.setDefaultValue(context.getUser().getProperty("SuggestionsModel.simpleMode", "0"));
		response.addParameter(simplifiedMode);
		
		FilterParameterInterface subjectArea = new FilterParameterInterface();
		subjectArea.setName("subjectArea");
		subjectArea.setType("list");
		subjectArea.setMultiSelect(true);
		subjectArea.setCollapsible(false);
		subjectArea.setLabel(MESSAGES.propSubjectArea());
		subjectArea.addOption(Constants.ALL_OPTION_VALUE, MESSAGES.itemAllSubjectAreas());
		for (SubjectArea subject: SubjectArea.getUserSubjectAreas(context.getUser()))
			subjectArea.addOption(subject.getUniqueId().toString(), subject.getSubjectAreaAbbreviation());
		subjectArea.setDefaultValue((String)context.getAttribute(SessionAttribute.OfferingsSubjectArea));
		response.addParameter(subjectArea);
		
		for (PreferenceLevel pref: PreferenceLevel.getPreferenceLevelList(true))
			response.addPreference(new PreferenceInterface(pref.getUniqueId(), PreferenceLevel.prolog2bgColor(pref.getPrefProlog()), pref.getPrefProlog(), pref.getPrefName(), pref.getAbbreviation(), false));
		
		return response;
	}

}
