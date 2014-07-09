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
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.SavedHQLInterface;
import org.unitime.timetable.gwt.shared.SavedHQLInterface.HQLQueriesRpcRequest;
import org.unitime.timetable.gwt.shared.SavedHQLInterface.Query;
import org.unitime.timetable.model.SavedHQL;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(HQLQueriesRpcRequest.class)
public class HQLQueriesBackend implements GwtRpcImplementation<HQLQueriesRpcRequest, GwtRpcResponseList<Query>>{
	@Autowired 
	private SessionContext sessionContext;
	
	@Override
	@PreAuthorize("checkPermission('HQLReports')")
	public GwtRpcResponseList<Query> execute(HQLQueriesRpcRequest request, SessionContext context) {
		SavedHQL.Flag ap = getAppearanceFlag(request.getAppearance());
		switch (ap) {
		case APPEARANCE_ADMINISTRATION:
			sessionContext.checkPermission(Right.HQLReportsAdministration); break;
		case APPEARANCE_COURSES:
			sessionContext.checkPermission(Right.HQLReportsCourses); break;
		case APPEARANCE_EXAMS:
			sessionContext.checkPermission(Right.HQLReportsExaminations); break;
		case APPEARANCE_SECTIONING:
			sessionContext.checkPermission(Right.HQLReportsStudents); break;
		case APPEARANCE_EVENTS:
			sessionContext.checkPermission(Right.HQLReportsEvents); break;
		}
		GwtRpcResponseList<SavedHQLInterface.Query> ret = new GwtRpcResponseList<SavedHQLInterface.Query>(); 
		for (SavedHQL hql: SavedHQL.listAll(null, ap, sessionContext.hasPermission(Right.HQLReportsAdminOnly))) {
			SavedHQLInterface.Query query = new SavedHQLInterface.Query();
			query.setName(hql.getName());
			query.setDescription(hql.getDescription());
			query.setQuery(hql.getQuery());
			query.setFlags(hql.getType());
			query.setId(hql.getUniqueId());
			ret.add(query);
		}
		return ret;
	}

	private SavedHQL.Flag getAppearanceFlag(String appearance) {
		for (SavedHQL.Flag flag: SavedHQL.Flag.values())
			if (flag.getAppearance() != null && flag.getAppearance().equalsIgnoreCase(appearance))
				return flag;
		return SavedHQL.Flag.APPEARANCE_COURSES;
	}
}
