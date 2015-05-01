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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.events.EventAction.EventContext;
import org.unitime.timetable.export.ExportHelper;
import org.unitime.timetable.export.Exporter;
import org.unitime.timetable.gwt.client.rooms.RoomsComparator;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcResponse;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcResponse.Entity;
import org.unitime.timetable.gwt.shared.RoomInterface.DepartmentInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.ExamTypeInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.FeatureInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.GroupInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomDetailInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomFilterRpcRequest;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomFlag;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomsPageMode;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.UserAuthority;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.context.UniTimeUserContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.server.rooms.RoomDetailsBackend;
import org.unitime.timetable.webutil.RequiredTimeTable;

/**
 * @author Tomas Muller
 */
public abstract class RoomsExporter implements Exporter {
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	protected static final GwtConstants CONSTANTS = Localization.create(GwtConstants.class);

	@Override
	public void export(ExportHelper helper) throws IOException {
		Long sessionId = helper.getAcademicSessionId();
		if (sessionId == null)
			throw new IllegalArgumentException("Academic session not provided, please set the term parameter.");
		
		Session session = SessionDAO.getInstance().get(sessionId);
		if (session == null)
			throw new IllegalArgumentException("Given academic session no longer exists.");

		RoomFilterRpcRequest request = new RoomFilterRpcRequest();
		request.setCommand(FilterRpcRequest.Command.ENUMERATE);
    	request.setSessionId(sessionId);
    	for (Enumeration<String> e = helper.getParameterNames(); e.hasMoreElements(); ) {
    		String command = e.nextElement();
    		if (command.equals("r:text")) {
    			request.setText(helper.getParameter("r:text"));
    		} else if (command.startsWith("r:")) {
    			for (String value: helper.getParameterValues(command))
    				request.addOption(command.substring(2), value);
    		}
    	}
    	request.setOption("flag", "gridAsText");
    	
    	UserContext u = helper.getSessionContext().getUser();
    	String user = helper.getParameter("user");
    	if (u == null && user != null && !checkRights(helper)) {
    		u = new UniTimeUserContext(user, null, null, null);
    		String role = helper.getParameter("role");
    		if (role != null) {
    			for (UserAuthority a: u.getAuthorities()) {
    				if (a.getAcademicSession() != null && a.getAcademicSession().getQualifierId().equals(sessionId) && role.equals(a.getRole())) {
    					u.setCurrentAuthority(a); break;
    				}
    			}
    		}
    	}
    	EventContext context = new EventContext(helper.getSessionContext(), u, sessionId);
    	if (u != null && u.getExternalUserId() != null)
    		request.setOption("user", u.getExternalUserId());
    	
    	int deptMode = 0;
    	if (helper.getParameter("dm") != null)
    		deptMode = Integer.parseInt(helper.getParameter("dm"));
    	
    	if (!helper.isRequestEncoded())
    		context.checkPermission(Right.Rooms);
    	
    	FilterRpcResponse response = new FilterRpcResponse();
    	new RoomDetailsBackend().enumarate(request, response, context); 
    	
    	List<RoomDetailInterface> rooms = new ArrayList<RoomDetailInterface>();
    	if (response.hasResults()) {
    		for (Entity e: response.getResults())
    			rooms.add((RoomDetailInterface)e);
    	}
    	
    	if (helper.getParameter("sort") != null) {
    		RoomsComparator cmp = null;
    		try {
    			int sort = Integer.parseInt(helper.getParameter("sort"));
    			if (sort > 0) {
    				cmp = new RoomsComparator(RoomsComparator.Column.values()[sort - 1], true);
    			} else if (sort < 0) {
    				cmp = new RoomsComparator(RoomsComparator.Column.values()[-1 - sort], false);
    			}
    		} catch (Exception e) {}
    		if (cmp != null)
    			Collections.sort(rooms, cmp);
    	}
    	
    	int roomCookieFlags = (helper.getParameter("flags") == null ? RoomsPageMode.COURSES.getFlags() : Integer.parseInt(helper.getParameter("flags")));
    	
    	boolean vertical = true;
    	if (helper.getParameter("horizontal") != null)
    		vertical = "0".equals(helper.getParameter("horizontal"));
    	else if (context.getUser() != null)
    		vertical = CommonValues.VerticalGrid.eq(context.getUser().getProperty(UserProperty.GridOrientation));
    	
    	String mode = helper.getParameter("mode");
    	if (mode == null && context.getUser() != null)
    		mode = RequiredTimeTable.getTimeGridSize(context.getUser());
    	
    	boolean gridAsText = (context.getUser() == null ? false : CommonValues.TextGrid.eq(UserProperty.GridOrientation.get(context.getUser())));
    	
    	print(helper, rooms, request.getOption("department"), roomCookieFlags, deptMode, gridAsText, vertical, mode);
	}
	
	protected abstract void print(ExportHelper helper, List<RoomDetailInterface> rooms, String department, int roomCookieFlags, int deptMode, boolean gridAsText, boolean vertical, String mode) throws IOException;
	
	protected boolean checkRights(ExportHelper helper) {
		return !helper.isRequestEncoded();
	}
	
	protected void hideColumns(Printer out, List<RoomDetailInterface> rooms, int roomCookieFlags) {
		for (RoomFlag flag: RoomFlag.values()) {
			if (!flag.in(roomCookieFlags)) hideColumn(out, rooms, flag);
		}
	}
	
	protected void hideColumn(Printer out, List<RoomDetailInterface> rooms, RoomFlag flag) {}
	
	protected String dept2string(DepartmentInterface d, int deptMode) {
		if (d == null) return "";
		switch (deptMode) {
		case 0: return d.getDeptCode();
		case 1: return d.getExtAbbreviationWhenExist();
		case 2: return d.getExtLabelWhenExist();
		case 3: return d.getExtAbbreviationWhenExist() + " - " + d.getExtLabelWhenExist();
		case 4: return d.getDeptCode() + " - " + d.getExtLabelWhenExist();
		default: return d.getDeptCode();
		}
	}
	
	protected String dept2string(Collection<DepartmentInterface> departments, int deptMode, String separator) {
		if (departments == null || departments.isEmpty()) return "";
		String ret = "";
		for (DepartmentInterface d: departments) {
			ret += (ret.isEmpty() ? "" : separator) + dept2string(d, deptMode);
		}
		return ret;
	}
	
	protected String pref2string(Collection<DepartmentInterface> departments, int deptMode, String separator) {
		if (departments == null || departments.isEmpty()) return "";
		String ret = "";
		for (DepartmentInterface d: departments) {
			if (d.getPreference() == null) continue;
			ret += (ret.isEmpty() ? "" : separator) + d.getPreference().getName() + " " + dept2string(d, deptMode);
		}
		return ret;
	}
	
	protected String examTypes2string(Collection<ExamTypeInterface> types, String separator) {
		if (types == null || types.isEmpty()) return "";
		String ret = "";
		for (ExamTypeInterface t: types) {
			ret += (ret.isEmpty() ? "" : separator) + t.getLabel();
		}
		return ret;
	}
	
	protected String features2string(Collection<FeatureInterface> features, int deptMode, String separator) {
		if (features == null || features.isEmpty()) return "";
		String ret = "";
		for (FeatureInterface f: features) {
			if (f.getType() != null)
				ret += (ret.isEmpty() ? "" : separator) + f.getLabel() + " (" + f.getType().getAbbreviation() + ")";
			else if (f.getDepartment() != null)
				ret += (ret.isEmpty() ? "" : separator) + f.getLabel() + " (" + dept2string(f.getDepartment(), deptMode) + ")";
			else
				ret += (ret.isEmpty() ? "" : separator) + f.getLabel();
		}
		return ret;
	}
	
	protected String groups2string(Collection<GroupInterface> groups, int deptMode, String separator) {
		if (groups == null || groups.isEmpty()) return "";
		String ret = "";
		for (GroupInterface g: groups) {
			ret += (ret.isEmpty() ? "" : separator) + g.getLabel() + (g.getDepartment() == null ? "" : " (" + dept2string(g.getDepartment(), deptMode) + ")");
		}
		return ret;
	}
}