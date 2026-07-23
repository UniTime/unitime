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
package org.unitime.timetable.server.administration.session;

import java.util.TreeSet;

import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.client.admin.LastChangesPage.LastChangesFilterRequest;
import org.unitime.timetable.gwt.client.admin.LastChangesPage.LastChangesFilterResponse;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.FilterInterface.FilterParameterInterface;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.NameFormat;

@GwtRpcImplements(LastChangesFilterRequest.class)
public class LastChangesFilterBackend implements GwtRpcImplementation<LastChangesFilterRequest, LastChangesFilterResponse>{
	protected static CourseMessages MESSAGES = Localization.create(CourseMessages.class);

	@Override
	public LastChangesFilterResponse execute(LastChangesFilterRequest request, SessionContext context) {
		context.checkPermission(Right.LastChanges);
		Long sessionId = context.getUser().getCurrentAcademicSessionId();
		NameFormat nameFormat = NameFormat.fromReference(context.getUser().getProperty(UserProperty.NameFormat));
		
		LastChangesFilterResponse filter = new LastChangesFilterResponse();
		filter.setSessionId(sessionId);
		filter.setSticky(CommonValues.Yes.eq(UserProperty.StickyTables.get(context.getUser())));

		
		FilterParameterInterface depts = new FilterParameterInterface();
		depts.setName("departmentId");
		depts.setLabel(MESSAGES.filterDepartment());
		depts.setType("list");
		depts.setCollapsible(true);
		depts.addOption("-1", MESSAGES.itemAllDepartments());
		depts.setShowSelectItem(false);
		for (Department d: Department.findAll(sessionId))
			depts.addOption(d.getUniqueId().toString(), d.getLabel());
		depts.setDefaultValue(context.getUser().getProperty("LastChanges.departmentId", "-1"));
		filter.addParameter(depts);
		
		FilterParameterInterface subjects = new FilterParameterInterface();
		subjects.setName("subjAreaId");
		subjects.setLabel(MESSAGES.filterSubjectArea());
		subjects.setType("list");
		subjects.setCollapsible(true);
		subjects.addOption("-1", MESSAGES.itemAllSubjects());
		subjects.setShowSelectItem(false);
		if (ApplicationProperty.OfferingsFilterSubjectTitle.isTrue())
			for (SubjectArea subject: new TreeSet<SubjectArea>(SubjectArea.getSubjectAreaList(sessionId)))
				subjects.addOption(subject.getUniqueId().toString(), subject.getLabel());
		else
			for (SubjectArea subject: new TreeSet<SubjectArea>(SubjectArea.getSubjectAreaList(sessionId)))
				subjects.addOption(subject.getUniqueId().toString(), subject.getSubjectAreaAbbreviation());
		subjects.setDefaultValue(context.getUser().getProperty("LastChanges.subjAreaId", "-1"));
		filter.addParameter(subjects);
		
		FilterParameterInterface managers = new FilterParameterInterface();
		managers.setName("managerId");
		managers.setLabel(MESSAGES.filterManager());
		managers.setType("list");
		managers.setCollapsible(true);
		managers.addOption("-1", MESSAGES.itemAllManagers());
		managers.setShowSelectItem(false);
		for (TimetableManager m: TimetableManager.getManagerList())
			managers.addOption(m.getUniqueId().toString(), nameFormat.format(m));
		managers.setDefaultValue(context.getUser().getProperty("LastChanges.managerId", "-1"));
		filter.addParameter(managers);
		
		FilterParameterInterface n = new FilterParameterInterface();
		n.setName("n");
		n.setLabel(MESSAGES.filterNumberOfChanges());
		n.setType("integer");
		n.setDefaultValue(context.getUser().getProperty("LastChanges.n", "100"));
		n.setCollapsible(true);
		filter.addParameter(n);
		
		return filter;
	}

}
