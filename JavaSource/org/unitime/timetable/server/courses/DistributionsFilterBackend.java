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

import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.client.offerings.OfferingsInterface.DistributionsFilterRequest;
import org.unitime.timetable.gwt.client.offerings.OfferingsInterface.DistributionsFilterResponse;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.FilterInterface.FilterParameterInterface;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.DistributionType;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.webutil.BackTracker;

@GwtRpcImplements(DistributionsFilterRequest.class)
public class DistributionsFilterBackend implements GwtRpcImplementation<DistributionsFilterRequest, DistributionsFilterResponse>{
	protected static CourseMessages MESSAGES = Localization.create(CourseMessages.class);
	protected static GwtMessages GWT = Localization.create(GwtMessages.class);

	@Override
	public DistributionsFilterResponse execute(DistributionsFilterRequest request, SessionContext context) {
		context.checkPermission(Right.DistributionPreferences);
		DistributionsFilterResponse filter = new DistributionsFilterResponse();
		
		FilterParameterInterface preference = new FilterParameterInterface();
		preference.setName("prefLevel");
		preference.setType("list");
		preference.setMultiSelect(true);
		preference.setCollapsible(true);
		preference.setLabel(MESSAGES.propertyDistributionPreference());
		for (PreferenceLevel pref: PreferenceLevel.getPreferenceLevelList(false))
			preference.addOption(pref.getPrefId().toString(), pref.getPrefName());
		preference.setDefaultValue(context.getUser().getProperty("Distributions.prefLevel", ""));
		filter.addParameter(preference);
		
		FilterParameterInterface type = new FilterParameterInterface();
		type.setName("distType");
		type.setType("list");
		type.setMultiSelect(true);
		type.setCollapsible(true);
		type.setLabel(MESSAGES.propertyDistributionType());
		for (DistributionType dt: DistributionType.findAll(false, false, true))
			type.addOption(dt.getUniqueId().toString(), dt.getLabel());
		type.setDefaultValue(context.getUser().getProperty("Distributions.distType", ""));
		filter.addParameter(type);
		
		FilterParameterInterface structure = new FilterParameterInterface();
		structure.setName("structure");
		structure.setType("list");
		structure.setMultiSelect(true);
		structure.setCollapsible(true);
		structure.setLabel(MESSAGES.propertyDistributionStructure());
		for (DistributionPref.Structure str: DistributionPref.Structure.values())
			structure.addOption(str.name(), str.getName());
		structure.addOption("instructor", MESSAGES.columnInstructor());
		structure.setDefaultValue(context.getUser().getProperty("Distributions.structure", ""));
		filter.addParameter(structure);
		
		FilterParameterInterface subjectArea = new FilterParameterInterface();
		subjectArea.setName("subjectArea");
		subjectArea.setType("list");
		subjectArea.setMultiSelect(true);
		subjectArea.setCollapsible(false);
		subjectArea.setLabel(MESSAGES.filterSubject());
		for (SubjectArea subject: SubjectArea.getAllSubjectAreas(context.getUser().getCurrentAcademicSessionId()))
			subjectArea.addOption(subject.getUniqueId().toString(), subject.getLabel());
		subjectArea.setDefaultValue((String)context.getAttribute(SessionAttribute.OfferingsSubjectArea));
		subjectArea.setEnterToSubmit(true);
		filter.addParameter(subjectArea);
		
		FilterParameterInterface courseNbr = new FilterParameterInterface();
		courseNbr.setName("courseNbr");
		courseNbr.setLabel(MESSAGES.filterCourseNumber());
		courseNbr.setType("courseNumber");
		courseNbr.setDefaultValue((String)context.getAttribute(SessionAttribute.OfferingsCourseNumber));
		courseNbr.setCollapsible(false);
		courseNbr.setConfig("subjectId=${subjectArea};notOffered=false;");
		courseNbr.setEnterToSubmit(true);
		filter.addParameter(courseNbr);
		
		if (subjectArea.getDefaultValue() == null && courseNbr.getDefaultValue() == null) {
			subjectArea.setDefaultValue((String)context.getAttribute(SessionAttribute.ClassesSubjectAreas));
			courseNbr.setDefaultValue((String)context.getAttribute(SessionAttribute.ClassesCourseNumber));
		}
		
		filter.setSticky(CommonValues.Yes.eq(UserProperty.StickyTables.get(context.getUser())));
		filter.setMaxSubjectsToSearchAutomatically(ApplicationProperty.MaxSubjectsToSearchAutomatically.intValue());
		filter.setCanAdd(context.hasPermission(Right.DistributionPreferenceAdd));
		filter.setSessionId(context.getUser().getCurrentAcademicSessionId());
		
		BackTracker.markForBack(context, null, null, false, true); //clear back list

		return filter;
	}

}
