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
import org.unitime.localization.messages.ExaminationMessages;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.form.ExamChangesForm.ExamChange;
import org.unitime.timetable.gwt.client.exams.ExamsInterface.ExamReportsFilterRequest;
import org.unitime.timetable.gwt.client.offerings.OfferingsInterface.ClassesFilterResponse;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.FilterInterface.FilterParameterInterface;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.server.exams.ExamReportsTableBuilder.ExamReport;
import org.unitime.timetable.webutil.BackTracker;

@GwtRpcImplements(ExamReportsFilterRequest.class)
public class ExamReportsFilterBackend implements GwtRpcImplementation<ExamReportsFilterRequest, ClassesFilterResponse>{
	protected static ExaminationMessages MESSAGES = Localization.create(ExaminationMessages.class);
	protected static GwtMessages GWT = Localization.create(GwtMessages.class);

	@Override
	public ClassesFilterResponse execute(ExamReportsFilterRequest request, SessionContext context) {
		context.checkPermission(Right.ExaminationReports);
		ClassesFilterResponse filter = new ClassesFilterResponse();
		
		FilterParameterInterface showSections = new FilterParameterInterface();
		showSections.setName("showSections");
		showSections.setLabel(MESSAGES.filterShowClassesCourses());
		showSections.setType("boolean");
		showSections.setDefaultValue(context.getUser().getProperty("ExamReport.showSections", "1"));
		filter.addParameter(showSections);
		
		FilterParameterInterface examType = new FilterParameterInterface();
		examType.setName("examType");
		examType.setType("list");
		examType.setMultiSelect(false);
		examType.setCollapsible(false);
		examType.setLabel(MESSAGES.propExamType());
        for (ExamType type: ExamType.findAllUsedApplicable(context.getUser(), DepartmentStatusType.Status.ExamView, DepartmentStatusType.Status.ExamTimetable))
        	examType.addOption(type.getUniqueId().toString(), type.getLabel());
        Object et = context.getAttribute(SessionAttribute.ExamType);
        examType.setDefaultValue(et == null ? null : et.toString());
        if (!examType.hasDefaultValue() && examType.hasOptions())
        	examType.setDefaultValue(examType.getOptions().get(0).getValue());
		filter.addParameter(examType);
		
		FilterParameterInterface report = new FilterParameterInterface();
		report.setName("report");
		report.setType("list");
		report.setMultiSelect(false);
		report.setCollapsible(false);
		report.setLabel(MESSAGES.filterReport());
		for (ExamReport r: ExamReport.values())
			report.addOption(r.name(), r.getLabel());
		report.setDefaultValue(context.getUser().getProperty("ExamReport.report"));
		filter.addParameter(report);
		
		FilterParameterInterface textFilter = new FilterParameterInterface();
		textFilter.setName("filter");
		textFilter.setType("text");
		textFilter.setCollapsible(false);
		textFilter.setLabel(MESSAGES.filterTextFilter());
		textFilter.setDefaultValue(context.getUser().getProperty("ExamReport.filter"));
		filter.addParameter(textFilter);

		FilterParameterInterface subjectArea = new FilterParameterInterface();
		subjectArea.setName("subjectArea");
		subjectArea.setType("list");
		subjectArea.setMultiSelect(true);
		subjectArea.setCollapsible(false);
		subjectArea.setLabel(MESSAGES.propExamSubject());
		if (context.getUser().getCurrentAuthority().hasRight(Right.DepartmentIndependent))
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
		
		if (subjectArea.getDefaultValue() == null) {
			subjectArea.setDefaultValue((String)context.getAttribute(SessionAttribute.ClassesSubjectAreas));
		}
		
		filter.setSticky(CommonValues.Yes.eq(UserProperty.StickyTables.get(context.getUser())));
		filter.setMaxSubjectsToSearchAutomatically(ApplicationProperty.MaxSubjectsToSearchAutomatically.intValue());
		filter.setSessionId(context.getUser().getCurrentAcademicSessionId());
		
		filter.setCanExport(true);
		
		BackTracker.markForBack(context, null, null, false, true); //clear back list
		
		return filter;
	}
	
	public String getChangeName(ExamChange ch) {
    	switch (ch) {
		case Best: return MESSAGES.changeBest();
		case Initial: return MESSAGES.changeInitial();
		case Saved: return MESSAGES.changeSaved();
		default:
			return ch.name();
		}
    }
}
