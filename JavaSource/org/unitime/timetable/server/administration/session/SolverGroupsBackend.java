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
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.client.admin.SolverGroupsPage.SolverGroupsRequest;
import org.unitime.timetable.gwt.client.admin.SolverGroupsPage.SolverGroupsResponse;
import org.unitime.timetable.gwt.client.tables.TableInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.LineInterface;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.NaturalOrderComparator;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.SolverGroup;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.util.NameFormat;

@GwtRpcImplements(SolverGroupsRequest.class)
public class SolverGroupsBackend implements GwtRpcImplementation<SolverGroupsRequest, SolverGroupsResponse>{
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);

	@Override
	public SolverGroupsResponse execute(SolverGroupsRequest request, SessionContext context) {
		context.checkPermission(Right.SolverGroups);
		
		TableInterface table = new TableInterface();
		table.setId("SolverGroups");
		table.setDefaultSortCookie(MSG.fieldAbbv());
		table.setName(MSG.sectSolverGroupsForSession(context.getUser().getCurrentAuthority().getQualifiers("Session").get(0).getQualifierLabel()));
		
		SolverGroupsResponse response = new SolverGroupsResponse();
		response.setSolverGroupsTable(table);
		
		LineInterface header = table.addHeader();
		header.addCell(MSG.fieldAbbv());
        header.addCell(MSG.fieldName());
        header.addCell(MSG.fieldDepartments());
        header.addCell(MSG.fieldManagers());
        header.addCell(MSG.fieldCommitted());
        for (CellInterface cell: header.getCells()) {
    		cell.setClassName("WebTableHeader");
    		cell.setText(cell.getText().replace("<br>", "\n"));
    		cell.addStyle("white-space: pre-wrap;");
    		cell.setSortable(true);
    	}
        
        Set<SolverGroup> solverGroups = SolverGroup.findBySessionId(context.getUser().getCurrentAcademicSessionId());
		if (solverGroups.isEmpty()) {
			table.setErrorMessage(MSG.infoNoSolverGroupInThisSession());
		} else {;
			final NameFormat nameFormat = NameFormat.fromReference(context.getUser().getProperty(UserProperty.NameFormat));
			Formats.Format<Date> df = Formats.getDateFormat(Formats.Pattern.DATE_TIME_STAMP);
			for (SolverGroup group: solverGroups) {
				LineInterface line = table.addLine();
				line.setId(group.getUniqueId());
				line.setURL("#" + group.getUniqueId());
				line.setAnchor("A" + group.getUniqueId());
				line.addCell(group.getAbbv());
				line.addCell(group.getName());
				
				CellInterface deptsCell = line.addCell();
				CellInterface c = null;
				for (Department dept: new TreeSet<Department>(group.getDepartments())) {
					if (c != null) c.add(", ");
	        		c = new CellInterface().setInline(false).setNoWrap(false);
	        		if (dept.isExternalManager())
	        			c.addStyle("font-weight: bold;");
	        		c.add(dept.getDeptCode() + (dept.getAbbreviation() == null || dept.getAbbreviation().equals(dept.getDeptCode()) ? "" : ": " + dept.getAbbreviation()))
	        			.setTitle(dept.getLabel()).setNoWrap(true);
	        		deptsCell.addItem(c);
				}
				
				CellInterface mgrsCell = line.addCell();
				c = null;
				List<TimetableManager> managers = new ArrayList<TimetableManager>(group.getTimetableManagers());
	            Collections.sort(managers, new Comparator<TimetableManager>() {
	    			@Override
	    			public int compare(TimetableManager m1, TimetableManager m2) {
	    				int cmp = NaturalOrderComparator.compare(nameFormat.format(m1), nameFormat.format(m2));
	    				if (cmp != 0) return cmp;
	    				return m1.compareTo(m2);
	    			}
	    		});
	            for (TimetableManager mgr: managers) {
	            	if (c != null) c.add(", ");
	        		c = new CellInterface().setInline(false).setNoWrap(false);
	        		String depts = "";
		        	for (Department d: new TreeSet<Department>(mgr.departmentsForSession(context.getUser().getCurrentAcademicSessionId()))) {
		        		depts += (depts.isEmpty() ? "" : ", ") + d.getDeptCode();
		        	}
	        		c.add(nameFormat.format(mgr)).setTitle(nameFormat.format(mgr) + (depts.isEmpty() ? "" : " (" + depts + ")")).setNoWrap(true);
	        		mgrsCell.addItem(c);
	            }
	            
	        	if (group.getCommittedSolution() != null)
	        		line.addCell(df.format(group.getCommittedSolution().getCommitDate())).setComparable(group.getCommittedSolution().getCommitDate().getTime());
	        	else
	        		line.addCell().setComparable(Long.MAX_VALUE);
	        	if (!response.isCanDeleteAll() && group.getSolutions().isEmpty())
	        		response.setCanDeleteAll(true);
			}
		}
		
		for (Department dept: Department.findAll(context.getUser().getCurrentAcademicSessionId())) {
			if (dept.getSolverGroup() == null && (!dept.getSubjectAreas().isEmpty() || dept.isExternalManager())) {
				response.setCanAdd(true);
				response.setCanAutoSetup(true);
			}
		}
		
		return response;
	}

}
