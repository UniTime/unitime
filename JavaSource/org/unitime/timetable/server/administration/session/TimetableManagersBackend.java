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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.client.admin.TimetableManagersPage.TimetableManagersRequest;
import org.unitime.timetable.gwt.client.admin.TimetableManagersPage.TimetableManagersResponse;
import org.unitime.timetable.gwt.client.tables.TableInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.LineInterface;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.ManagerRole;
import org.unitime.timetable.model.SolverGroup;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.comparators.RolesComparator;
import org.unitime.timetable.model.dao.TimetableManagerDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.NameFormat;

@GwtRpcImplements(TimetableManagersRequest.class)
public class TimetableManagersBackend implements GwtRpcImplementation<TimetableManagersRequest, TimetableManagersResponse>{
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);

	@Override
	public TimetableManagersResponse execute(TimetableManagersRequest request, SessionContext context) {
		context.checkPermission(Right.TimetableManagers);
		
		Long currentAcadSession = context.getUser().getCurrentAcademicSessionId();
		
		TableInterface table = new TableInterface();
		table.setId("TimetableManagers");
		table.setDefaultSortCookie(MSG.columnRoles());
		table.setName(MSG.sectManagerList(currentAcadSession == null ? "" : context.getUser().getCurrentAuthority().getQualifiers("Session").get(0).getQualifierLabel()));
		
		boolean dispLastChanges = CommonValues.Yes.eq(UserProperty.DisplayLastChanges.get(context.getUser()));
		NameFormat nameFormat = NameFormat.fromReference(context.getUser().getProperty(UserProperty.NameFormat));
		
		List<TimetableManager> empList = TimetableManagerDAO.getInstance().getSession().createQuery(
				"from TimetableManager order by lastName, firstName", TimetableManager.class).list();
		
		LineInterface header = table.addHeader();
		header.addCell(MSG.columnRoles());
        header.addCell(MSG.columnExternalId());
        header.addCell(MSG.columnManagerName());
        header.addCell(MSG.columnEmailAddress());
        header.addCell(MSG.columnDepartment());
        header.addCell(MSG.columnSubjectArea());
        header.addCell(MSG.columnSolverGroup());
        if (dispLastChanges) header.addCell(MSG.columnLastChange());
        for (CellInterface cell: header.getCells()) {
    		cell.setClassName("WebTableHeader");
    		cell.setText(cell.getText().replace("<br>", "\n"));
    		cell.addStyle("white-space: pre-wrap;");
    		cell.setSortable(true);
    	}
        
        for (TimetableManager manager: empList) {
        	if (!request.isShowAllManagers()) {
            	boolean sessionIndependent = false;
            	for (ManagerRole mgrRole: manager.getManagerRoles()) {
            		if (mgrRole.getRole().hasRight(Right.SessionIndependent) || (mgrRole.getRole().hasRight(Right.SessionIndependentIfNoSessionGiven) && manager.getDepartments().isEmpty())) {
    		        	sessionIndependent = true;
    		        	break;
            		}
            	}
            	boolean departmental = false;
            	for (Department dept: manager.getDepartments()) {
            		if (!dept.getSession().getUniqueId().equals(currentAcadSession)) continue;
    		        departmental = true;
    		        break;
            	}
            	if (!sessionIndependent && !departmental) continue;
        	}

			LineInterface line = table.addLine();
			line.setId(manager.getUniqueId());
			if (context.hasPermission(manager, Right.TimetableManagerEdit))
				line.setURL("#" + manager.getUniqueId());
			line.setAnchor("A" + manager.getUniqueId());
			
			CellInterface rolesCell = line.addCell();
        	ArrayList<ManagerRole> roles = new ArrayList<ManagerRole>(manager.getManagerRoles());
        	Collections.sort(roles, new RolesComparator());
        	CellInterface c = null;
        	for (ManagerRole mgrRole: roles) {
        		if (c != null) c.add(", ");
        		c = new CellInterface().setNoWrap(true).setInline(false);
        		c.setText(mgrRole.getRole().getAbbv() + (Boolean.TRUE.equals(mgrRole.isReceiveEmails()) ? "" : "*"));
        		c.setTitle(mgrRole.getRole().getAbbv() + (roles.size() > 1 && Boolean.TRUE.equals(mgrRole.isPrimary()) ? " - " + MSG.flagPrimaryRole() : "")
        				+ (Boolean.TRUE.equals(mgrRole.isReceiveEmails()) ? "" : ", " + MSG.explNoEmailForThisRole()));
        		rolesCell.addItem(c);
        	}
        	rolesCell.setComparable(rolesCell.toString(), manager.getExternalUniqueId());
        	
        	line.addCell(manager.getExternalUniqueId());
        	line.addCell(nameFormat.format(manager));
        	line.addCell(manager.getEmailAddress());
        	CellInterface deptsCell = line.addCell();
        	ArrayList<Department> departments = new ArrayList<Department>();
        	ArrayList<SubjectArea> subjects = new ArrayList<SubjectArea>();
        	for (Department dept: manager.getDepartments()) {
        		if (!dept.getSession().getUniqueId().equals(currentAcadSession)) continue;
        		departments.add(dept);
        	}
        	Collections.sort(departments);
        	c = null;
        	for (Department dept: departments) {
        		if (c != null) c.add(", ");
        		c = new CellInterface().setInline(!request.isExport() && departments.size() > 4).setNoWrap(false);
        		if (dept.isExternalManager())
        			c.addStyle("font-weight: bold;");
        		c.add(dept.getDeptCode() + (dept.getAbbreviation() == null || dept.getAbbreviation().equals(dept.getDeptCode()) ? "" : ": " + dept.getAbbreviation()))
        			.setTitle(dept.getLabel()).setNoWrap(true);
        		deptsCell.addItem(c);
        		subjects.addAll(dept.getSubjectAreas());
        	}
        	deptsCell.setComparable(deptsCell.toString(), manager.getExternalUniqueId());
        	
        	Collections.sort(subjects);
        	CellInterface subjectsCell = line.addCell();
        	c = null;
        	for (SubjectArea sa: subjects) {
        		if (c != null) c.add(", ");
        		c = new CellInterface().setInline(!request.isExport() && subjects.size() > 4).setNoWrap(false);
        		c.add(sa.getSubjectAreaAbbreviation()).setTitle(sa.getTitle()).setNoWrap(true);
        		subjectsCell.addItem(c);
        	}
        	subjectsCell.setComparable(subjectsCell.toString(), manager.getExternalUniqueId());
        	
        	CellInterface solvGrpCell = line.addCell();
        	List<SolverGroup> solverGroups = new ArrayList<SolverGroup>();
        	for (SolverGroup sg: manager.getSolverGroups()) {
        		if (!sg.getSession().getUniqueId().equals(currentAcadSession)) continue;
        		solverGroups.add(sg);
        	}
        	Collections.sort(solverGroups);
        	c = null;
        	for (SolverGroup sg: solverGroups) {
        		if (c != null) c.add(", ");
        		c = new CellInterface().setInline(!request.isExport() && solverGroups.size() > 4).setNoWrap(false);
        		c.add(sg.getAbbv()).setTitle(sg.getName()).setNoWrap(true);
        		solvGrpCell.addItem(c);
        	}
        	solvGrpCell.setComparable(solvGrpCell.toString(), manager.getExternalUniqueId());
        	
        	if (dispLastChanges) {
                List<ChangeLog> changes = ChangeLog.findLastNChanges(currentAcadSession, manager.getUniqueId(), null, null, 1);
                ChangeLog lastChange = (changes==null || changes.isEmpty()?null:(ChangeLog)changes.get(0));
                if (lastChange != null) {
                    line.addCell(MSG.formatLastChange(lastChange.getSourceTitle(), lastChange.getOperationTitle(), ChangeLog.sDFdate.format(lastChange.getTimeStamp())))
                	.setTitle(lastChange.getLabel()).setComparable(lastChange.getTimeStamp().getTime(), manager.getExternalUniqueId());
                } else {
                	line.addCell().setComparable(Long.MAX_VALUE, manager.getExternalUniqueId());
                }
        	}
        }
        
        TimetableManagersResponse response = new TimetableManagersResponse();
		response.setManagersTable(table);
		response.setCanAdd(context.hasPermission(Right.TimetableManagerAdd));
		
		return response;
	}

}
