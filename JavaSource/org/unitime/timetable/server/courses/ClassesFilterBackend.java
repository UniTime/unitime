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
import org.unitime.timetable.gwt.client.offerings.OfferingsInterface.ClassesFilterRequest;
import org.unitime.timetable.gwt.client.offerings.OfferingsInterface.ClassesFilterResponse;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.FilterInterface.FilterParameterInterface;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.ItypeDesc;
import org.unitime.timetable.model.LearningManagementSystemInfo;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.comparators.ClassCourseComparator;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.webutil.BackTracker;

@GwtRpcImplements(ClassesFilterRequest.class)
public class ClassesFilterBackend implements GwtRpcImplementation<ClassesFilterRequest, ClassesFilterResponse>{
	protected static CourseMessages MESSAGES = Localization.create(CourseMessages.class);
	protected static GwtMessages GWT = Localization.create(GwtMessages.class);

	@Override
	public ClassesFilterResponse execute(ClassesFilterRequest request, SessionContext context) {
		context.checkPermission(Right.Classes);
		ClassesFilterResponse filter = new ClassesFilterResponse();
		
		filter.addParameter(createToggle(context, "divSec", MESSAGES.columnExternalId(), false));
		
		filter.addParameter(createToggle(context, "demand", MESSAGES.columnDemand(), true));
		filter.addParameter(createToggle(context, "limit", MESSAGES.columnLimit(), true));
		filter.addParameter(createToggle(context, "snapshotLimit", MESSAGES.columnSnapshotLimit(), false));
		filter.addParameter(createToggle(context, "roomLimit", MESSAGES.columnRoomRatio(), true));
		
		filter.addParameter(createToggle(context, "manager", MESSAGES.columnManager(), true));
		if (ApplicationProperty.CoursesFundingDepartmentsEnabled.isTrue())
			filter.addParameter(createToggle(context, "fundingDepartment", MESSAGES.columnFundingDepartment(), false));
		
		filter.addParameter(createToggle(context, "datePattern", MESSAGES.columnDatePattern(), true));
		filter.addParameter(createToggle(context, "timePattern", MESSAGES.columnTimePattern(), true));
		
		filter.addParameter(createToggle(context, "preferences", MESSAGES.columnPreferences(), true));
		filter.addParameter(createToggle(context, "instructorAssignment", MESSAGES.includeInstructorScheduling(), false));
		filter.addParameter(createToggle(context, "instructor", MESSAGES.columnInstructor(), true));
		filter.addParameter(createToggle(context, "timetable", MESSAGES.columnTimetable(), true));
		
		filter.addParameter(createToggle(context, "schedulePrintNote", MESSAGES.columnSchedulePrintNote(), true));
		
		if (LearningManagementSystemInfo.isLmsInfoDefinedForSession(context.getUser().getCurrentAcademicSessionId()))
			filter.addParameter(createToggle(context, "lms", MESSAGES.columnLms(), false));
		
		filter.addParameter(createToggle(context, "note", MESSAGES.columnNote(), false));
		
		if (context.hasPermission(Right.Examinations))
			filter.addParameter(createToggle(context, "exams", MESSAGES.columnExams(), false));
		
		FilterParameterInterface manager = new FilterParameterInterface();
		manager.setName("filterManager");
		manager.setLabel(MESSAGES.filterManager());
		manager.setType("list");
		manager.setMultiSelect(true);
		manager.addOption("-2", MESSAGES.dropDeptDepartment());
		for (Department d: Department.findAllExternal(context.getUser().getCurrentAcademicSessionId()))
			manager.addOption(d.getUniqueId().toString(), d.getExternalMgrAbbv() + " - " + d.getExternalMgrLabel());
		String defaultManager = context.getUser().getProperty("ClassList.filterManager");
		if (defaultManager == null || defaultManager.isEmpty())
			manager.setDefaultValue(defaultManager);
		else {
			String adjustedDefaultManager = null;
			for (String option: defaultManager.split(",")) {
				if (manager.getOptionText(option) != null) {
					if (adjustedDefaultManager == null)
						adjustedDefaultManager = option;
					else
						adjustedDefaultManager += "," + option;
				}
			}
			manager.setDefaultValue(adjustedDefaultManager);
		}
		manager.setMaxLinesToShow(3);
		filter.addParameter(manager);
		
		FilterParameterInterface itype = new FilterParameterInterface();
		itype.setName("filterIType");
		itype.setLabel(MESSAGES.filterInstructionalType());
		itype.addOption("", MESSAGES.dropITypeAll());
		for (ItypeDesc t: ItypeDesc.findAll(true))
			itype.addOption(t.getItype().toString(), t.getDesc());
		itype.setDefaultValue(context.getUser().getProperty("ClassList.filterIType", ""));
		filter.addParameter(itype);
		
		FilterParameterInterface instructor = new FilterParameterInterface();
		instructor.setName("filterInstructor");
		instructor.setLabel(MESSAGES.filterInstructor());
		instructor.setType("text");
		instructor.setDefaultValue(context.getUser().getProperty("ClassList.filterInstructor", ""));
		filter.addParameter(instructor);
		
		FilterParameterInterface days = new FilterParameterInterface();
		days.setName("filterDayCode");
		days.setLabel(MESSAGES.filterAssignedTime());
		days.setType("dayCode");
		days.setDefaultValue(context.getUser().getProperty("ClassList.filterDayCode", ""));
		filter.addParameter(days);
		
		FilterParameterInterface startTime = new FilterParameterInterface();
		startTime.setName("filterStartTime");
		startTime.setLabel(MESSAGES.filterAssignedTime());
		startTime.setType("time");
		startTime.setDefaultValue(context.getUser().getProperty("ClassList.filterStartTime", ""));
		startTime.setPrefix(GWT.propFrom());
		startTime.setComposite(true);
		filter.addParameter(startTime);
		
		FilterParameterInterface endTime = new FilterParameterInterface();
		endTime.setName("filterEndTime");
		endTime.setLabel(MESSAGES.filterAssignedTime());
		endTime.setType("time");
		endTime.setDefaultValue(context.getUser().getProperty("ClassList.filterEndTime", ""));
		endTime.setPrefix(GWT.propTo());
		endTime.setComposite(true);
		filter.addParameter(endTime);

		FilterParameterInterface room = new FilterParameterInterface();
		room.setName("filterAssignedRoom");
		room.setLabel(MESSAGES.filterAssignedRoom());
		room.setType("text");
		room.setDefaultValue(context.getUser().getProperty("ClassList.filterAssignedRoom", ""));
		filter.addParameter(room);

		FilterParameterInterface sortBy = new FilterParameterInterface();
		sortBy.setName("sortBy");
		sortBy.setLabel(MESSAGES.filterSortBy());
		sortBy.setType("list");
		for (String name: ClassCourseComparator.getNames())
			sortBy.addOption(name, name);
		sortBy.setDefaultValue(context.getUser().getProperty("ClassList.sortBy",ClassCourseComparator.getName(ClassCourseComparator.SortBy.NAME)));
		filter.addParameter(sortBy);
		FilterParameterInterface sortByKeepSubparts = createToggle(context, "sortByKeepSubparts", MESSAGES.checkSortWithinSubparts(), true);
		sortByKeepSubparts.setLabel(MESSAGES.filterSortBy());
		filter.addParameter(sortByKeepSubparts);
		
		FilterParameterInterface crossList = createToggle(context, "showCrossListedClasses", MESSAGES.showCrossListedClasses(), false);
		crossList.setLabel(MESSAGES.filterCrossList());
		filter.addParameter(crossList);
		FilterParameterInterface includeCancelledClasses = createToggle(context, "includeCancelledClasses", MESSAGES.showCancelledClasses(), true);
		includeCancelledClasses.setLabel(MESSAGES.filterCancelledClasses());
		filter.addParameter(includeCancelledClasses);
		FilterParameterInterface filterNeedInstructor = createToggle(context, "filterNeedInstructor", MESSAGES.showNeedInstructorClasses(), false);
		filterNeedInstructor.setLabel(MESSAGES.filterNeedInstructorAssignment());
		filter.addParameter(filterNeedInstructor);
		
		FilterParameterInterface subjectArea = new FilterParameterInterface();
		subjectArea.setName("subjectArea");
		subjectArea.setType("list");
		subjectArea.setMultiSelect(true);
		subjectArea.setCollapsible(false);
		subjectArea.setLabel(MESSAGES.filterSubject());
		for (SubjectArea subject: SubjectArea.getUserSubjectAreas(context.getUser()))
			subjectArea.addOption(subject.getUniqueId().toString(), subject.getLabel());
		subjectArea.setDefaultValue((String)context.getAttribute(SessionAttribute.ClassesSubjectAreas));
		subjectArea.setEnterToSubmit(true);
		filter.addParameter(subjectArea);
		
		FilterParameterInterface courseNbr = new FilterParameterInterface();
		courseNbr.setName("courseNbr");
		courseNbr.setLabel(MESSAGES.filterCourseNumber());
		courseNbr.setType("courseNumber");
		courseNbr.setDefaultValue((String)context.getAttribute(SessionAttribute.ClassesCourseNumber));
		courseNbr.setCollapsible(false);
		courseNbr.setConfig("subjectId=${subjectArea};notOffered=false;crossListed=${showCrossListedClasses}");
		courseNbr.setEnterToSubmit(true);
		filter.addParameter(courseNbr);
		
		if (subjectArea.getDefaultValue() == null && courseNbr.getDefaultValue() == null) {
			subjectArea.setDefaultValue((String)context.getAttribute(SessionAttribute.OfferingsSubjectArea));
			courseNbr.setDefaultValue((String)context.getAttribute(SessionAttribute.OfferingsCourseNumber));
		}
		
		filter.setSticky(CommonValues.Yes.eq(UserProperty.StickyTables.get(context.getUser())));
		filter.setMaxSubjectsToSearchAutomatically(ApplicationProperty.MaxSubjectsToSearchAutomatically.intValue());
		filter.setCanExport(context.hasPermission(Right.ClassesExportPDF));
		filter.setSessionId(context.getUser().getCurrentAcademicSessionId());
		
		BackTracker.markForBack(context, null, null, false, true); //clear back list
		
		return filter;
	}
	
	protected FilterParameterInterface createToggle(SessionContext context, String name, String label, Boolean defaultValue) {
		FilterParameterInterface toggle = new FilterParameterInterface();
		toggle.setName(name);
		toggle.setLabel(MESSAGES.filterOptionalColumns());
		toggle.setType("boolean");
		toggle.setSuffix(label);
		toggle.setDefaultValue(context.getUser().getProperty("ClassList." + name, defaultValue == null ? null : defaultValue ? "1" : "0"));
		return toggle;
	}
}
