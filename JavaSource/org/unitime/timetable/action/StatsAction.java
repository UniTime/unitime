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
package org.unitime.timetable.action;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.tiles.annotation.TilesDefinition;
import org.apache.struts2.tiles.annotation.TilesPutAttribute;
import org.unitime.commons.web.WebTable;
import org.unitime.timetable.form.BlankForm;
import org.unitime.timetable.model.QueryLog;
import org.unitime.timetable.security.rights.Right;

/** 
 * @author Tomas Muller
 */
@Action(value = "stats", results = {
		@Result(name = "show", type = "tiles", location = "stats.tiles")
	})
@TilesDefinition(name = "stats.tiles", extend = "baseLayout", putAttributes =  {
		@TilesPutAttribute(name = "title", value = "Page Statistics"),
		@TilesPutAttribute(name = "body", value = "/admin/stats.jsp"),
		@TilesPutAttribute(name = "checkAdmin", value = "true")
	})
public class StatsAction extends UniTimeAction<BlankForm> {
	private static final long serialVersionUID = -1957840172304483386L;

	@Override
	public String execute() {
		sessionContext.checkPermission(Right.PageStatistics);
		WebTable.setOrder(sessionContext,"pageStats.ord",request.getParameter("ord"), 1);
		return "show";
	}
	
	public QueryLog.ChartType[] getChartTypes() {
		return QueryLog.ChartType.values();
	}
	
	public QueryLog.ChartWindow[] getChartWindows() {
		return QueryLog.ChartWindow.values();
	}
	
	public String getChartUrl(QueryLog.ChartWindow w, QueryLog.ChartType t) {
		return QueryLog.getChart(w, t);
	}
	
	public String getQueryTable() {
		return QueryLog.getTopQueries(7).printTable(WebTable.getOrder(sessionContext, "pageStats.ord"));
	}
}
