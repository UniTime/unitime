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
