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

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.gwt.client.admin.DistributionTypesPage.DistributionTypesRequest;
import org.unitime.timetable.gwt.client.admin.DistributionTypesPage.DistributionTypesResponse;
import org.unitime.timetable.gwt.client.tables.TableInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.LineInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface.Alignment;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DistributionType;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

@GwtRpcImplements(DistributionTypesRequest.class)
public class DistributionTypesBackend implements GwtRpcImplementation<DistributionTypesRequest, DistributionTypesResponse>{
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	
	@Override
	public DistributionTypesResponse execute(DistributionTypesRequest request, SessionContext context) {
		context.checkPermission(Right.DistributionTypes);
		boolean canEdit = context.hasPermission(Right.DistributionTypeEdit);
		Long sessionId = context.getUser().getCurrentAcademicSessionId();
		
		DistributionTypesResponse response = new DistributionTypesResponse();
		TableInterface table = new TableInterface();
		table.setId("DistributionTypes");
		table.setDefaultSortCookie(MSG.fieldId());
		table.setName(MSG.sectDistributionTypes());
		
		LineInterface header = table.addHeader();
		header.addCell(MSG.fieldId());
        header.addCell(MSG.fieldReference());
        header.addCell(MSG.fieldAbbreviation());
        header.addCell(MSG.fieldName());
        header.addCell(MSG.fieldType());
        header.addCell(MSG.fieldVisible()).setTextAlignment(Alignment.CENTER);
        header.addCell(MSG.fieldAllowInstructorPreference()).setTextAlignment(Alignment.CENTER);
        header.addCell(MSG.fieldAllowInstructorSurvey()).setTextAlignment(Alignment.CENTER);
        header.addCell(MSG.fieldSequencingRequired()).setTextAlignment(Alignment.CENTER);
        header.addCell(MSG.fieldAllowPreferences()).setTextAlignment(Alignment.CENTER);
        header.addCell(MSG.fieldDepartments());
        header.addCell(MSG.fieldDescription());
        for (CellInterface cell: header.getCells()) {
    		cell.setClassName("WebTableHeader");
    		cell.setText(cell.getText().replace("<br>", "\n"));
    		cell.addStyle("white-space: pre-wrap;");
    		cell.setSortable(true);
    	}
		
		List<DistributionType> distTypes = new ArrayList<DistributionType>();
		distTypes.addAll(DistributionType.findAll(false, false, null));
		distTypes.addAll(DistributionType.findAll(false, true, null));
		for (DistributionType d: distTypes) {
			LineInterface line = table.addLine();
			line.setId(d.getUniqueId());
			if (canEdit) line.setURL("#" + d.getUniqueId());
			line.setAnchor("A" + d.getUniqueId());
			
			line.addCell(d.getRequirementId().toString()).setComparable(d.getRequirementId());
			line.addCell(d.getReference());
			line.addCell(d.getAbbreviation());
			line.addCell(d.getLabel());
			line.addCell(d.isExamPref() ? MSG.itemDistTypeExams() : MSG.itemDistTypeCourses()).setComparable(!d.isExamPref());
			if (d.isVisible()) {
				line.addCell().setComparable(1, d.getRequirementId()).setTextAlignment(Alignment.CENTER)
					.addImage().setSource("images/accept.png").setTitle(MSG.yes()).setAlt(MSG.yes());
			} else {
				line.addCell().setComparable(2, d.getRequirementId()).setTextAlignment(Alignment.CENTER)
					.addImage().setSource("images/cross.png").setTitle(MSG.yes()).setAlt(MSG.no());
			}
			if (d.isExamPref()) {
				line.addCell().setComparable(3, d.getRequirementId()).setTitle(MSG.notApplicable()).setTextAlignment(Alignment.CENTER);
				// not applicable
			} else if (d.isInstructorPref()) {
				line.addCell().setComparable(1, d.getRequirementId()).setTextAlignment(Alignment.CENTER)
					.addImage().setSource("images/accept.png").setTitle(MSG.yes()).setAlt(MSG.yes());
			} else {
				line.addCell().setComparable(2, d.getRequirementId()).setTextAlignment(Alignment.CENTER)
					.addImage().setSource("images/cross.png").setTitle(MSG.yes()).setAlt(MSG.no());
			}
			
			if (d.isExamPref() || !d.isInstructorPref()) {
				// not applicable
				line.addCell().setComparable(3, d.getRequirementId()).setTitle(MSG.notApplicable()).setTextAlignment(Alignment.CENTER);
			} else if (d.effectiveSurvey()) {
				line.addCell().setComparable(1, d.getRequirementId()).setTextAlignment(Alignment.CENTER)
					.addImage().setSource("images/accept.png").setTitle(MSG.yes()).setAlt(MSG.yes());
			} else {
				line.addCell().setComparable(2, d.getRequirementId()).setTextAlignment(Alignment.CENTER)
					.addImage().setSource("images/cross.png").setTitle(MSG.yes()).setAlt(MSG.no());
			}

			if (d.isSequencingRequired()) {
				line.addCell().setComparable(1, d.getRequirementId()).setTextAlignment(Alignment.CENTER)
					.addImage().setSource("images/accept.png").setTitle(MSG.yes()).setAlt(MSG.yes());
			} else {
				line.addCell().setComparable(2, d.getRequirementId()).setTextAlignment(Alignment.CENTER)
					.addImage().setSource("images/cross.png").setTitle(MSG.yes()).setAlt(MSG.no());
			}
			
			CellInterface allowedPref = line.addCell().setTextAlignment(Alignment.CENTER);
			if (d.getAllowedPref() == null || d.getAllowedPref().isEmpty()) {
				allowedPref.add(MSG.itemNone()).addStyle("font-style: italic;").setComparable("");
			} else if ("P43210R".equals(d.getAllowedPref()) || "R01234P".equals(d.getAllowedPref())) {
				allowedPref.add(MSG.itemAll()).addStyle("font-style: italic;").setComparable("--all");
			} else {
		    	for (PreferenceLevel p: PreferenceLevel.getPreferenceLevelList()) {
		    		if (d.getAllowedPref().indexOf(PreferenceLevel.prolog2char(p.getPrefProlog())) < 0) continue;
		    		allowedPref.add(p.getPrefName()).setColor(PreferenceLevel.prolog2color(p.getPrefProlog()))
		    				.setInline(false).setNoWrap(true)
		    				.setTextAlignment(Alignment.CENTER);
		    	}
			}
			
			CellInterface deptsCell = line.addCell();
        	if (d.getDepartments().isEmpty()) {
        		deptsCell.add(MSG.itemAll()).addStyle("font-style: italic;").setComparable("");
        	} else {
            	CellInterface c = null;
            	for (Department dept: new TreeSet<Department>(d.getDepartments())) {
            		if (!dept.getSessionId().equals(sessionId)) continue;
            		if (c != null) c.add(", ");
            		c = new CellInterface().setInline(!request.isExport() && d.getDepartments().size() > 4).setNoWrap(false);
            		if (dept.isExternalManager())
            			c.addStyle("font-weight: bold;");
            		c.add(dept.getDeptCode() + (dept.getAbbreviation() == null || dept.getAbbreviation().equals(dept.getDeptCode()) ? "" : ": " + dept.getAbbreviation()))
            			.setTitle(dept.getLabel()).setNoWrap(true);
            		deptsCell.addItem(c);
            	}
        	}
        	
        	line.addCell().setHtml(d.getDescr()).addStyle("white-space: pre-wrap;")
        		.setTitle(d.getDescr() == null ? null : d.getDescr().replaceAll("<[bB][rR]>", "\n").replace("&rarr;", "\u2192").replace("&rarr;", "\u2190")
        				.replaceAll("<[bBiI]>", "").replaceAll("</[bBiI]>", ""));
			
			if (!d.isVisible())
				for (CellInterface cell: line.getCells())
					cell.setColor("#646464");
			
		}
		
		response.setTable(table);
		return response;
	}

}
