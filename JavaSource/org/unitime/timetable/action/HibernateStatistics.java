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
import org.unitime.commons.hibernate.stats.StatsProvider;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.form.BlankForm;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.security.rights.Right;

/** 
 *  @author Tomas Muller
 */
@Action(value = "hibernateStats", results = {
		@Result(name = "show", type = "tiles", location = "hibernateStats.tiles")
	})
@TilesDefinition(name = "hibernateStats.tiles", extend = "baseLayout", putAttributes =  {
		@TilesPutAttribute(name = "title", value = "Hibernate Statistics"),
		@TilesPutAttribute(name = "body", value = "/admin/hibernateStats.jsp")
	})
public class HibernateStatistics extends UniTimeAction<BlankForm>{
	private static final long serialVersionUID = 5335714912646440433L;
	protected static final CourseMessages MSG = Localization.create(CourseMessages.class);
	
	private boolean details = false;
	public boolean getDetails() { return details; }
	public void setDetails(boolean details) { this.details = details; }

	@Override
	public String execute() {
		sessionContext.checkPermission(Right.HibernateStatistics);
		if (MSG.actionEnableStatistics().equals(op)) {
			new _RootDAO().getSession().getSessionFactory().getStatistics().setStatisticsEnabled(true);
		}
		if (MSG.actionDisableStatistics().equals(op)) {
			new _RootDAO().getSession().getSessionFactory().getStatistics().setStatisticsEnabled(false);
		}
		if (MSG.actionHideDetails().equals(op)) {
			details = false;
		}
		if (MSG.actionShowDetails().equals(op)) {
			details = true;
		}
		return "show";
	}
	
	public boolean getEnabled() {
		return new _RootDAO().getSession().getSessionFactory().getStatistics().isStatisticsEnabled();
	}
	
	public String getStats() {
		return StatsProvider.getStatsHtml(!details);
	}
}
