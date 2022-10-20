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
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.form.BlankForm;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.ExportUtils;
import org.unitime.timetable.webutil.PdfWebTable;
import org.unitime.timetable.webutil.TimetableManagerBuilder;


/**
 * @author Tomas Muller
 */
@Action(value = "timetableManagerList", results = {
		@Result(name = "success", type = "tiles", location = "timetableManagerList.tiles"),
		@Result(name = "add", type = "redirect", location="/timetableManagerEdit.action", params = {
				"op", "${op}"})
	})
@TilesDefinition(name = "timetableManagerList.tiles", extend = "baseLayout", putAttributes =  {
		@TilesPutAttribute(name = "title", value = "Timetable Managers"),
		@TilesPutAttribute(name = "body", value = "/admin/timetableManagerList.jsp")
	})
public class TimetableManagerListAction extends UniTimeAction<BlankForm> {
    private static final long serialVersionUID = -3335607995044212251L;
    protected static final CourseMessages MSG = Localization.create(CourseMessages.class);
    
    private Boolean all;
    
    public Boolean getAll() { return all; }
    public void setAll(Boolean all) { this.all = all; }

	@Override
    public String execute() throws Exception {
        // Check permissions
        sessionContext.checkPermission(Right.TimetableManagers);

		WebTable.setOrder(sessionContext,"timetableManagerList.ord",request.getParameter("order"),1);
		
		if (all == null) {
			all = "1".equals(sessionContext.getUser().getProperty("TimetableManagers.showAll", "0"));
		} else {
			sessionContext.getUser().setProperty("TimetableManagers.showAll", all ? "1" : "0");
		}
		
		if (MSG.actionAddTimetableManager().equals(op)) {
			return "add";
		}
        
        if (MSG.actionExportPdf().equals(op)) {
        	ExportUtils.exportPDF(
        			new TimetableManagerBuilder().getManagersTable(sessionContext, "pdf", all),
        			getOrder(), response, "managers");
        	return null;
        }
        if (MSG.actionExportCsv().equals(op)) {
        	ExportUtils.exportCSV(
        			new TimetableManagerBuilder().getManagersTable(sessionContext, "csv", all),
        			getOrder(), response, "managers");
        	return null;
        }
            
        return "success";
    }
	
	public int getOrder() {
		return WebTable.getOrder(sessionContext,"timetableManagerList.ord");
	}
	
	public String getTable() {
		PdfWebTable table =  new TimetableManagerBuilder().getManagersTable(sessionContext, "html", all);
        return table.printTable(getOrder());
	}
	
	public String getTitle() {
		return MSG.sectManagerList(sessionContext.getUser().getCurrentAuthority().getQualifiers("Session").get(0).getQualifierLabel());
	}
}
