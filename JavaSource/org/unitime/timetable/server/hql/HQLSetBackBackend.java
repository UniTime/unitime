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
package org.unitime.timetable.server.hql;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseNull;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.SavedHQLInterface.HQLSetBackRpcRequest;
import org.unitime.timetable.model.SavedHQL;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.webutil.BackTracker;
import org.unitime.timetable.webutil.Navigation;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(HQLSetBackRpcRequest.class)
public class HQLSetBackBackend implements GwtRpcImplementation<HQLSetBackRpcRequest, GwtRpcResponseNull>{
	protected static GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	
    @Autowired 
	private SessionContext sessionContext;

	@Override
	@PreAuthorize("checkPermission('HQLReports')")
	public GwtRpcResponseNull execute(HQLSetBackRpcRequest request, SessionContext context) {
		String title = MESSAGES.pageCourseReports();
		switch (getAppearanceFlag(request.getAppearance())) {
		case APPEARANCE_COURSES:
			title = MESSAGES.pageCourseReports(); break;
		case APPEARANCE_EXAMS:
			title = MESSAGES.pageExaminationReports(); break;
		case APPEARANCE_SECTIONING:
			title = MESSAGES.pageStudentSectioningReports(); break;
		case APPEARANCE_EVENTS:
			title = MESSAGES.pageEventReports(); break;
		case APPEARANCE_ADMINISTRATION:
			title = MESSAGES.pageAdministrationReports(); break;
		}
		BackTracker.markForBack(sessionContext, "gwt.jsp?page=hql&appearance=" + request.getAppearance() + "#" + request.getHistory(), title, true, true);
		if ("__Class".equals(request.getType()))
			Navigation.set(sessionContext, Navigation.sClassLevel, request.getIds());
		else if ("__Offering".equals(request.getType()))
			Navigation.set(sessionContext, Navigation.sInstructionalOfferingLevel, request.getIds());
		else if ("__Subpart".equals(request.getType()))
			Navigation.set(sessionContext, Navigation.sSchedulingSubpartLevel, request.getIds());
		else if ("__Room".equals(request.getType()))
			Navigation.set(sessionContext, Navigation.sInstructionalOfferingLevel, request.getIds());
		else if ("__Instructor".equals(request.getType()))
			Navigation.set(sessionContext, Navigation.sInstructionalOfferingLevel, request.getIds());
		else if ("__Exam".equals(request.getType()))
			Navigation.set(sessionContext, Navigation.sInstructionalOfferingLevel, request.getIds());
		else if ("__Event".equals(request.getType()))
			Navigation.set(sessionContext, Navigation.sInstructionalOfferingLevel, request.getIds());
		return null;
	}

	private SavedHQL.Flag getAppearanceFlag(String appearance) {
		for (SavedHQL.Flag flag: SavedHQL.Flag.values())
			if (flag.getAppearance() != null && flag.getAppearance().equalsIgnoreCase(appearance))
				return flag;
		return SavedHQL.Flag.APPEARANCE_COURSES;
	}

}
