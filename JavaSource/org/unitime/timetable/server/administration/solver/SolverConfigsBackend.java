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
package org.unitime.timetable.server.administration.solver;

import java.util.List;

import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.gwt.client.admin.SolverConfigsPage.SolverConfigsRequest;
import org.unitime.timetable.gwt.client.admin.SolverConfigsPage.SolverConfigsResponse;
import org.unitime.timetable.gwt.client.tables.TableInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.LineInterface;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.model.SolverPredefinedSetting;
import org.unitime.timetable.model.dao.SolverPredefinedSettingDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

@GwtRpcImplements(SolverConfigsRequest.class)
public class SolverConfigsBackend implements GwtRpcImplementation<SolverConfigsRequest, SolverConfigsResponse>{
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);

	@Override
	public SolverConfigsResponse execute(SolverConfigsRequest request, SessionContext context) {
		context.checkPermission(Right.SolverConfigurations);
		SolverConfigsResponse response = new SolverConfigsResponse();
		TableInterface table = new TableInterface();
		table.setId("SolverConfigs");
		table.setDefaultSortCookie(MSG.fieldReference());
		table.setName(MSG.sectSolverConfigurations());
		
		LineInterface header = table.addHeader();
		header.addCell(MSG.fieldReference());
        header.addCell(MSG.fieldName());
        header.addCell(MSG.fieldAppearance());
        for (CellInterface cell: header.getCells()) {
    		cell.setClassName("WebTableHeader");
    		cell.setText(cell.getText().replace("<br>", "\n"));
    		cell.addStyle("white-space: pre-wrap;");
    		cell.setSortable(true);
    	}
        List<SolverPredefinedSetting> list = SolverPredefinedSettingDAO.getInstance().getSession().createQuery(
        		"from SolverPredefinedSetting", SolverPredefinedSetting.class).list();
        if (list.isEmpty())
        	table.setErrorMessage(MSG.infoNoSolverConfigs());
        for (SolverPredefinedSetting setting: list) {
        	LineInterface line = table.addLine();
        	line.setId(setting.getUniqueId());
        	line.setURL("#" + setting.getUniqueId());
        	line.setAnchor("A" + setting.getUniqueId());
        	
        	line.addCell(setting.getName());
        	line.addCell(setting.getDescription());
        	line.addCell(setting.getAppearanceType().getLabel()).setComparable(setting.getAppearance(), setting.getName());
        }
		
		response.setSolverConfigsTable(table);
		return response;
	}

}
