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

import java.util.List;

import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.client.admin.LastChangesPage.LastChangesRequest;
import org.unitime.timetable.gwt.client.admin.LastChangesPage.LastChangesResponse;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.LineInterface;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.FilterInterface;
import org.unitime.timetable.gwt.shared.FilterInterface.FilterParameterInterface;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.NameFormat;

@GwtRpcImplements(LastChangesRequest.class)
public class LastChangesBackend implements GwtRpcImplementation<LastChangesRequest, LastChangesResponse>{
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);

	@Override
	public LastChangesResponse execute(LastChangesRequest request, SessionContext context) {
		context.checkPermission(Right.LastChanges);
		Long sessionId = context.getUser().getCurrentAcademicSessionId();
		NameFormat nameFormat = NameFormat.fromReference(context.getUser().getProperty(UserProperty.NameFormat));
		
		if (request.getFilter() instanceof FilterInterface)
			for (FilterParameterInterface p: ((FilterInterface)request.getFilter()).getParameters())
				context.getUser().setProperty("LastChanges." + p.getName(), p.getValue());
		
		LastChangesResponse table = new LastChangesResponse();
		table.setName(MSG.columnLastChanges());
		table.setId("LastChanges");
		table.setDefaultSortCookie("!" + MSG.columnDate());
		
		LineInterface header = table.addHeader();
		header.addCell(MSG.columnDate());
        header.addCell(MSG.columnDepartment());
        header.addCell(MSG.columnSubject());
        header.addCell(MSG.columnManager());
        header.addCell(MSG.columnPage());
        header.addCell(MSG.columnObject());
        header.addCell(MSG.columnOperation());
        for (CellInterface cell: header.getCells()) {
    		cell.setClassName("WebTableHeader");
    		cell.setText(cell.getText().replace("<br>", "\n"));
    		cell.addStyle("white-space: pre-wrap;");
    		cell.setSortable(true);
    	}
        
        Long managerId = Long.valueOf(request.getFilter().getParameterValue("managerId", "-1"));
        Long subjAreaId = Long.valueOf(request.getFilter().getParameterValue("subjAreaId", "-1"));
        Long departmentId = Long.valueOf(request.getFilter().getParameterValue("departmentId", "-1"));
        int n = Integer.valueOf(request.getFilter().getParameterValue("n", "100"));
        
        List<ChangeLog> changes = ChangeLog.findLastNChanges(
                sessionId,
                managerId < 0 ? null : managerId,
                subjAreaId < 0 ? null : subjAreaId,
                departmentId < 0 ? null : departmentId,
                n);
        for (ChangeLog lastChange: changes) {
        	LineInterface line = table.addLine();
        	line.addCell(ChangeLog.sDF.format(lastChange.getTimeStamp())).setComparable(lastChange.getTimeStamp());
        	line.addCell(lastChange.getDepartment() == null ? null : lastChange.getDepartment().getShortLabel())
        		.setTitle(lastChange.getDepartment() == null ? null : lastChange.getDepartment().getLabel());
        	line.addCell(lastChange.getSubjectArea() == null ? null : lastChange.getSubjectArea().getSubjectAreaAbbreviation());
        	line.addCell(lastChange.getManager() == null ? null : nameFormat.format(lastChange.getManager()));
        	line.addCell(lastChange.getSourceTitle());
        	line.addCell(lastChange.getObjectTitle() == null ? null : lastChange.getObjectTitle().replace("&rarr;", "\u2192"));
        	line.addCell(lastChange.getOperationTitle()).setComparable(lastChange.getOperation().ordinal());
        }
		
		return table;
	}

}
