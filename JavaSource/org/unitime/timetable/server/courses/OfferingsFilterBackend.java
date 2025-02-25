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
import org.unitime.timetable.gwt.client.offerings.OfferingsInterface.OfferingsFilterRequest;
import org.unitime.timetable.gwt.client.offerings.OfferingsInterface.OfferingsFilterResponse;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.FilterInterface;
import org.unitime.timetable.gwt.shared.FilterInterface.FilterParameterInterface;
import org.unitime.timetable.model.LearningManagementSystemInfo;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.comparators.ClassCourseComparator;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.webutil.BackTracker;

@GwtRpcImplements(OfferingsFilterRequest.class)
public class OfferingsFilterBackend implements GwtRpcImplementation<OfferingsFilterRequest, OfferingsFilterResponse>{
	protected static CourseMessages MESSAGES = Localization.create(CourseMessages.class);

	@Override
	public OfferingsFilterResponse execute(OfferingsFilterRequest request, SessionContext context) {
		context.checkPermission(Right.InstructionalOfferings);
		OfferingsFilterResponse filter = new OfferingsFilterResponse();
		
		filter.addParameter(createToggle(context, "divSec", MESSAGES.columnExternalId(), false));
		
		filter.addParameter(createToggle(context, "enrollmentInformation", MESSAGES.columnEnrollmentInformation(), null));
		filter.addParameter(createToggle(context, "demand", MESSAGES.columnDemand(), true, "enrollmentInformation"));
		filter.addParameter(createToggle(context, "projectedDemand", MESSAGES.columnProjectedDemand(), true, "enrollmentInformation"));
		filter.addParameter(createToggle(context, "limit", MESSAGES.columnLimit(), true, "enrollmentInformation"));
		filter.addParameter(createToggle(context, "snapshotLimit", MESSAGES.columnSnapshotLimit(), true, "enrollmentInformation"));
		filter.addParameter(createToggle(context, "roomLimit", MESSAGES.columnRoomRatio(), true, "enrollmentInformation"));
		setParentDefault(filter, "enrollmentInformation");
		
		filter.addParameter(createToggle(context, "manager", MESSAGES.columnManager(), true));
		if (ApplicationProperty.CoursesFundingDepartmentsEnabled.isTrue())
			filter.addParameter(createToggle(context, "fundingDepartment", MESSAGES.columnFundingDepartment(), false));
		
		filter.addParameter(createToggle(context, "dateTimeInformation", MESSAGES.columnDateTimeInformation(), null));
		filter.addParameter(createToggle(context, "datePattern", MESSAGES.columnDatePattern(), true, "datePattern"));
		filter.addParameter(createToggle(context, "minPerWk", MESSAGES.columnMinPerWk(), true, "datePattern"));
		filter.addParameter(createToggle(context, "timePattern", MESSAGES.columnTimePattern(), true, "datePattern"));
		setParentDefault(filter, "dateTimeInformation");
		
		filter.addParameter(createToggle(context, "preferences", MESSAGES.columnPreferences(), true));
		filter.addParameter(createToggle(context, "instructorAssignment", MESSAGES.includeInstructorScheduling(), false));
		filter.addParameter(createToggle(context, "instructor", MESSAGES.columnInstructor(), true));
		filter.addParameter(createToggle(context, "timetable", MESSAGES.columnTimetable(), true));
		
		filter.addParameter(createToggle(context, "catalogInformation", MESSAGES.columnCatalogInformation(), false));
		filter.addParameter(createToggle(context, "title", MESSAGES.columnTitle(), false, "catalogInformation"));
		filter.addParameter(createToggle(context, "credit", MESSAGES.columnOfferingCredit(), false, "catalogInformation"));
		filter.addParameter(createToggle(context, "subpartCredit", MESSAGES.columnSubpartCredit(), false, "catalogInformation"));
		filter.addParameter(createToggle(context, "consent", MESSAGES.columnConsent(), false, "catalogInformation"));
		filter.addParameter(createToggle(context, "schedulePrintNote", MESSAGES.columnSchedulePrintNote(), true, "catalogInformation"));
		setParentDefault(filter, "catalogInformation");
		
		if (LearningManagementSystemInfo.isLmsInfoDefinedForSession(context.getUser().getCurrentAcademicSessionId()))
			filter.addParameter(createToggle(context, "lms", MESSAGES.columnLms(), false));
		
		filter.addParameter(createToggle(context, "note", MESSAGES.columnNote(), false));
		
		if (context.hasPermission(Right.Examinations))
			filter.addParameter(createToggle(context, "exams", MESSAGES.columnExams(), false));
		
		if (ApplicationProperty.OfferingWaitListShowFilter.isTrue())
			filter.addParameter(createToggle(context, "waitlistMode", MESSAGES.columnWaitlistMode(), false));
		
		FilterParameterInterface sortBy = new FilterParameterInterface();
		sortBy.setName("sortBy");
		sortBy.setLabel(MESSAGES.filterSortBy());
		sortBy.setType("list");
		for (String name: ClassCourseComparator.getNames())
			sortBy.addOption(name, name);
		sortBy.setDefaultValue(context.getUser().getProperty("InstructionalOfferingList.sortBy",ClassCourseComparator.getName(ClassCourseComparator.SortBy.NAME)));
		
		filter.addParameter(sortBy);
		
		if (ApplicationProperty.OfferingWaitListShowFilter.isTrue()) {
			FilterParameterInterface waitlist = new FilterParameterInterface();
			waitlist.setName("waitlist");
			waitlist.setLabel(MESSAGES.filterWaitlist());
			waitlist.setType("list");
			waitlist.setDefaultValue(context.getUser().getProperty("InstructionalOfferingList.waitlist", "A"));
			waitlist.addOption("A", MESSAGES.itemWaitListAllCourses());
			waitlist.addOption("W", MESSAGES.itemWaitListWaitListed());
			waitlist.addOption("N", MESSAGES.itemWaitListNotWaitListed());
			waitlist.addOption("R", MESSAGES.itemWaitListReschedule());
			waitlist.addOption("Z", MESSAGES.itemWaitListNotWaitListedReschedule());
			waitlist.addOption("X", MESSAGES.itemWaitListNotReschedule());
			waitlist.setCollapsible(false);
			filter.addParameter(waitlist);
		}
		
		FilterParameterInterface subjectArea = new FilterParameterInterface();
		subjectArea.setName("subjectArea");
		subjectArea.setType("list");
		subjectArea.setMultiSelect(true);
		subjectArea.setCollapsible(false);
		subjectArea.setLabel(MESSAGES.filterSubject());
		for (SubjectArea subject: SubjectArea.getUserSubjectAreas(context.getUser()))
			subjectArea.addOption(subject.getUniqueId().toString(), subject.getLabel());
		subjectArea.setDefaultValue((String)context.getAttribute(SessionAttribute.OfferingsSubjectArea));
		filter.addParameter(subjectArea);
		
		FilterParameterInterface courseNbr = new FilterParameterInterface();
		courseNbr.setName("courseNbr");
		courseNbr.setLabel(MESSAGES.filterCourseNumber());
		courseNbr.setType("courseNumber");
		courseNbr.setDefaultValue((String)context.getAttribute(SessionAttribute.OfferingsCourseNumber));
		courseNbr.setCollapsible(false);
		courseNbr.setConfig("subjectId=${subjectArea};notOffered=include;waitlist=${waitlist}");
		filter.addParameter(courseNbr);
		
		filter.setSticky(CommonValues.Yes.eq(UserProperty.StickyTables.get(context.getUser())));
		filter.setMaxSubjectsToSearchAutomatically(ApplicationProperty.MaxSubjectsToSearchAutomatically.intValue());
		filter.setCanAdd(context.hasPermission(Right.AddCourseOffering));
		filter.setCanExport(context.hasPermission(Right.InstructionalOfferingsExportPDF));
		filter.setCanWorksheet(context.hasPermission(Right.InstructionalOfferingsWorksheetPDF));
		filter.setSessionId(context.getUser().getCurrentAcademicSessionId());
		
		BackTracker.markForBack(context, null, null, false, true); //clear back list
		
		return filter;
	}
	
	protected FilterParameterInterface createToggle(SessionContext context, String name, String label, Boolean defaultValue) {
		return createToggle(context, name, label, defaultValue, null);
	}
	
	protected FilterParameterInterface createToggle(SessionContext context, String name, String label, Boolean defaultValue, String parent) {
		FilterParameterInterface toggle = new FilterParameterInterface();
		toggle.setName(name);
		toggle.setLabel(MESSAGES.filterOptionalColumns());
		toggle.setType("boolean");
		toggle.setSuffix(label);
		toggle.setDefaultValue(context.getUser().getProperty("InstructionalOfferingList." + name, defaultValue == null ? null : defaultValue ? "0" : "1"));
		if (parent != null)
			toggle.setParent(parent);
		return toggle;
	}
	
	protected void setParentDefault(FilterInterface filter, String parent) {
		FilterParameterInterface toggle = null;
		int checked = 0;
		for (FilterParameterInterface p: filter.getParameters()) {
			if (parent.equals(p.getName())) {
				toggle = p;
			} else if (parent.equals(p.getParent()) && "1".equals(p.getDefaultValue())) {
				checked ++;
			}
		}
		if (toggle != null)
			toggle.setDefaultValue(checked > 0 ? "1" : "0");
	}

}
