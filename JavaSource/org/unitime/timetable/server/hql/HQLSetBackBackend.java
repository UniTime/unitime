/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2014, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
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
