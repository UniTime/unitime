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
import org.unitime.timetable.gwt.client.exams.ExamsInterface.ExamAssignmentChangesFilterRequest;
import org.unitime.timetable.gwt.client.offerings.OfferingsInterface.ClassesFilterResponse;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.FilterInterface.FilterParameterInterface;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.webutil.BackTracker;

@GwtRpcImplements(ExamAssignmentChangesFilterRequest.class)
public class ExamAssignmentChangesFilterBackend implements GwtRpcImplementation<ExamAssignmentChangesFilterRequest, ClassesFilterResponse>{
	protected static ExaminationMessages MESSAGES = Localization.create(ExaminationMessages.class);
	protected static GwtMessages GWT = Localization.create(GwtMessages.class);

	@Override
	public ClassesFilterResponse execute(ExamAssignmentChangesFilterRequest request, SessionContext context) {
		context.checkPermission(Right.ExaminationAssignmentChanges);
		ClassesFilterResponse filter = new ClassesFilterResponse();
		
		FilterParameterInterface showSections = new FilterParameterInterface();
		showSections.setName("showSections");
		showSections.setLabel(MESSAGES.filterShowClassesCourses());
		showSections.setType("boolean");
		showSections.setDefaultValue(context.getUser().getProperty("ExamReport.showSections", "1"));
		filter.addParameter(showSections);

		FilterParameterInterface reverse = new FilterParameterInterface();
		reverse.setName("reverse");
		reverse.setLabel(MESSAGES.filterReverseMode());
		reverse.setType("boolean");
		reverse.setDefaultValue(context.getUser().getProperty("ExamChanges.reverse", "0"));
		reverse.setSuffix(MESSAGES.hintReversedMode().replace("&rarr;", "\u2192"));
		filter.addParameter(reverse);

		FilterParameterInterface subjectArea = new FilterParameterInterface();
		subjectArea.setName("subjectArea");
		subjectArea.setType("list");
		subjectArea.setMultiSelect(true);
		subjectArea.setCollapsible(false);
		subjectArea.setLabel(MESSAGES.propExamSubject());
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
		
		FilterParameterInterface changeType = new FilterParameterInterface();
		changeType.setName("changeType");
		changeType.setType("list");
		changeType.setMultiSelect(false);
		changeType.setCollapsible(false);
		changeType.setLabel(MESSAGES.filterCompareWith());
		for (ExamChange ch: ExamChange.values())
			changeType.addOption(ch.name(), getChangeName(ch));
        changeType.setDefaultValue(context.getUser().getProperty("ExamChanges.changeType", ExamChange.Initial.name()));
		filter.addParameter(changeType);

		
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
