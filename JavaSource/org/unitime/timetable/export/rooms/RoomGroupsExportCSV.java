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
package org.unitime.timetable.export.rooms;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import org.springframework.stereotype.Service;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.events.EventAction.EventContext;
import org.unitime.timetable.export.CSVPrinter;
import org.unitime.timetable.export.ExportHelper;
import org.unitime.timetable.export.Exporter;
import org.unitime.timetable.gwt.client.rooms.RoomGroupsComparator;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcResponse.Entity;
import org.unitime.timetable.gwt.shared.EventInterface.RoomFilterRpcRequest;
import org.unitime.timetable.gwt.shared.RoomInterface.DepartmentInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.GroupInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomGroupsColumn;
import org.unitime.timetable.gwt.shared.RoomInterface.SearchRoomGroupsRequest;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.server.rooms.RoomGroupsBackend;

/**
 * @author Tomas Muller
 */
@Service("org.unitime.timetable.export.Exporter:roomgroups.csv")
public class RoomGroupsExportCSV implements Exporter {
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	
	@Override
	public String reference() { return "roomgroups.csv"; }

	@Override
	public void export(ExportHelper helper) throws IOException {
		Long sessionId = helper.getAcademicSessionId();
		if (sessionId == null)
			throw new IllegalArgumentException("Academic session not provided, please set the term parameter.");
		
		Session session = SessionDAO.getInstance().get(sessionId);
		if (session == null)
			throw new IllegalArgumentException("Given academic session no longer exists.");

		SearchRoomGroupsRequest request = new SearchRoomGroupsRequest();
		request.setFilter(new RoomFilterRpcRequest());
		for (Enumeration<String> e = helper.getParameterNames(); e.hasMoreElements(); ) {
			String command = e.nextElement();
			if (command.equals("r:text")) {
				request.getFilter().setText(helper.getParameter("r:text"));
    		} else if (command.startsWith("r:")) {
    			for (String value: helper.getParameterValues(command))
    				request.getFilter().addOption(command.substring(2), value);
    		}
    	}
		request.getFilter().setSessionId(sessionId);
		
    	EventContext context = new EventContext(helper.getSessionContext(), helper.getSessionContext().getUser(), sessionId);
    	if (context.isAuthenticated() && context.getUser().getExternalUserId() != null)
    		request.getFilter().setOption("user", context.getUser().getExternalUserId());
    	GwtRpcResponseList<GroupInterface> groups = new RoomGroupsBackend().execute(request, context);
    	
    	int dm = 0;
    	if (helper.getParameter("dm") != null)
    		dm = Integer.parseInt(helper.getParameter("dm"));
    	
    	if (helper.getParameter("sort") != null) {
    		RoomGroupsComparator cmp = null;
    		try {
    			int sort = Integer.parseInt(helper.getParameter("sort"));
    			if (sort > 0) {
    				cmp = new RoomGroupsComparator(RoomGroupsColumn.values()[sort - 1], true);
    			} else if (sort < 0) {
    				cmp = new RoomGroupsComparator(RoomGroupsColumn.values()[-1 - sort], false);
    			}
    		} catch (Exception e) {}
    		if (cmp != null)
    			Collections.sort(groups, cmp);
    	}
    	
    	print(helper, groups, dm, request.getFilter().getOption("department"));
	}
	
	protected void print(ExportHelper helper, List<GroupInterface> groups, int dm, String department) throws IOException {
		Printer out = new CSVPrinter(helper, false);
		helper.setup(out.getContentType(), reference(), false);
		print(out, groups, dm, department);
		out.flush(); out.close();
	}
	
	protected void printHeader(Printer out) throws IOException {
		out.printHeader(
				MESSAGES.colName(),
				MESSAGES.colAbbreviation(),
				MESSAGES.colDepartment(),
				MESSAGES.colDescription(),
				MESSAGES.colRooms()
				);
	}
	
	protected void print(Printer out, List<GroupInterface> groups, int dm, String department) throws IOException {
		printHeader(out);
		for (GroupInterface group: groups) {
			if (group.isDepartmental()) continue;
			printLine(out, group, dm);
		}
		
		for (GroupInterface group: groups) {
			if (!group.isDepartmental()) continue;
			if (department != null && !department.equals(group.getDepartment().getDeptCode())) continue;
			if (department == null && !group.hasRooms()) continue;
			printLine(out, group, dm);
		}
	}
	
	protected void printLine(Printer out, GroupInterface group, int dm) throws IOException {
		String[] line = new String[4 + (group.hasRooms() ? group.getRooms().size() : 0)];
		int idx = 0;
		line[idx++] = group.getLabel();
		line[idx++] = group.getAbbreviation();
		if (group.isDepartmental())
			line[idx++] = dept2string(group.getDepartment(), dm);
		else
			line[idx++] = group.isDefault() ? MESSAGES.exportDefaultRoomGroup() : MESSAGES.exportGlobalRoomGroup();
		line[idx++] = group.getDescription();
		if (group.hasRooms())
			for (Entity room: group.getRooms())
				line[idx++] = name(room);
		out.printLine(line);
	}
	
	protected String dept2string(DepartmentInterface d, int dm) {
		if (d == null) return "";
		switch (dm) {
		case 0: return d.getDeptCode();
		case 1: return d.getExtAbbreviationWhenExist();
		case 2: return d.getExtLabelWhenExist();
		case 3: return d.getExtAbbreviationWhenExist() + " - " + d.getExtLabelWhenExist();
		case 4: return d.getDeptCode() + " - " + d.getExtLabelWhenExist();
		default: return d.getDeptCode();
		}
	}
	
	protected String name(Entity room) {
		return (room.getAbbreviation() != null && !room.getAbbreviation().isEmpty() ? MESSAGES.label(room.getName(), room.getAbbreviation()) : room.getName());
	}
}
