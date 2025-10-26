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
import org.unitime.localization.messages.CourseMessages;
import org.unitime.localization.messages.ExaminationMessages;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.client.exams.ExamsInterface.ExamDistributionsFilterRequest;
import org.unitime.timetable.gwt.client.offerings.OfferingsInterface.DistributionsFilterResponse;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.FilterInterface.FilterParameterInterface;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.DistributionType;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.webutil.BackTracker;

@GwtRpcImplements(ExamDistributionsFilterRequest.class)
public class ExamDistributionsFilterBackend implements GwtRpcImplementation<ExamDistributionsFilterRequest, DistributionsFilterResponse>{
	protected static CourseMessages MESSAGES = Localization.create(CourseMessages.class);
	protected static ExaminationMessages XMSG = Localization.create(ExaminationMessages.class);
	protected static GwtMessages GWT = Localization.create(GwtMessages.class);

	@Override
	public DistributionsFilterResponse execute(ExamDistributionsFilterRequest request, SessionContext context) {
		context.checkPermission(Right.ExaminationDistributionPreferences);
		DistributionsFilterResponse filter = new DistributionsFilterResponse();
		
		FilterParameterInterface preference = new FilterParameterInterface();
		preference.setName("prefLevel");
		preference.setType("list");
		preference.setMultiSelect(true);
		preference.setCollapsible(true);
		preference.setLabel(MESSAGES.propertyDistributionPreference());
		for (PreferenceLevel pref: PreferenceLevel.getPreferenceLevelList(false))
			preference.addOption(pref.getPrefId().toString(), pref.getPrefName());
		preference.setDefaultValue(context.getUser().getProperty("ExamDistributions.prefLevel", ""));
		filter.addParameter(preference);
		
		FilterParameterInterface distType = new FilterParameterInterface();
		distType.setName("distType");
		distType.setType("list");
		distType.setMultiSelect(true);
		distType.setCollapsible(true);
		distType.setLabel(MESSAGES.propertyDistributionType());
		for (DistributionType dt: DistributionType.findAll(false, true, true))
			distType.addOption(dt.getUniqueId().toString(), dt.getLabel());
		distType.setDefaultValue(context.getUser().getProperty("ExamDistributions.distType", ""));
		filter.addParameter(distType);
		
		FilterParameterInterface examType = new FilterParameterInterface();
		examType.setName("examType");
		examType.setType("list");
		examType.setMultiSelect(false);
		examType.setCollapsible(false);
		examType.setLabel(XMSG.propExamType());
        for (ExamType type: ExamType.findAllUsedApplicable(context.getUser(), DepartmentStatusType.Status.ExamView, DepartmentStatusType.Status.ExamTimetable))
        	examType.addOption(type.getUniqueId().toString(), type.getLabel());
        Object et = context.getAttribute(SessionAttribute.ExamType);
        examType.setDefaultValue(et == null ? null : et.toString());
        if (!examType.hasDefaultValue() && examType.hasOptions())
        	examType.setDefaultValue(examType.getOptions().get(0).getValue());
		filter.addParameter(examType);
		
		FilterParameterInterface subjectArea = new FilterParameterInterface();
		subjectArea.setName("subjectArea");
		subjectArea.setType("list");
		subjectArea.setMultiSelect(true);
		subjectArea.setCollapsible(false);
		subjectArea.setLabel(MESSAGES.filterSubject());
		subjectArea.addOption("-1", GWT.itemAllSubjectAreas());
		if (ApplicationProperty.OfferingsFilterSubjectTitle.isTrue())
			for (SubjectArea subject: SubjectArea.getUserSubjectAreas(context.getUser()))
				subjectArea.addOption(subject.getUniqueId().toString(), subject.getLabel());
		else
			for (SubjectArea subject: SubjectArea.getUserSubjectAreas(context.getUser()))
				subjectArea.addOption(subject.getUniqueId().toString(), subject.getSubjectAreaAbbreviation());
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
