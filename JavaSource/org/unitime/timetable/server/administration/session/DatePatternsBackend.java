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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.gwt.client.admin.DatePatternsPage.DatePatternsRequest;
import org.unitime.timetable.gwt.client.admin.DatePatternsPage.DatePatternsResponse;
import org.unitime.timetable.gwt.client.tables.TableInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.LineInterface;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.Formats;

@GwtRpcImplements(DatePatternsRequest.class)
public class DatePatternsBackend implements GwtRpcImplementation<DatePatternsRequest, DatePatternsResponse>{
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);

	@Override
	public DatePatternsResponse execute(DatePatternsRequest request, SessionContext context) {
		context.checkPermission(Right.DatePatterns);
		
		DatePatternsResponse response = new DatePatternsResponse();
		
		TableInterface table = new TableInterface();
		table.setId("DatePatterns");
		table.setDefaultSortCookie(MSG.columnDatePatternName());
		table.setName(MSG.sectDatePatterns());
		
		boolean hasSet = !DatePattern.findAllParents(context.getUser().getCurrentAcademicSessionId()).isEmpty();
		
		LineInterface header = table.addHeader();
		header.addCell(MSG.columnDatePatternName());
        header.addCell(MSG.columnDatePatternType());
        header.addCell(MSG.columnDatePatternUsed());
        header.addCell(MSG.columnDatePatternWeeks());
        if (request.isExport()) {
        	header.addCell(MSG.columnDatePatternFrom());
        	header.addCell(MSG.columnDatePatternTo());
        }
        if (hasSet) {
        	header.addCell(MSG.columnDatePatternDatesOrPatterns());
        	header.addCell(MSG.columnDatePatternPatternSets());
        } else {
        	header.addCell(MSG.columnDatePatternDates());
        }
        header.addCell(MSG.columnDatePatternDepartments());
        for (CellInterface cell: header.getCells()) {
    		cell.setClassName("WebTableHeader");
    		cell.setText(cell.getText().replace("<br>", "\n"));
    		cell.addStyle("white-space: pre-wrap;");
    		cell.setSortable(true);
    	}
        
        List<DatePattern> patterns = DatePattern.findAll(context.getUser(), null, null);
        Set<DatePattern> used = DatePattern.findAllUsed(context.getUser().getCurrentAcademicSessionId());
		if (patterns.isEmpty()) table.setErrorMessage(MSG.errorNoDatePatterns());
		DecimalFormat df = new DecimalFormat("0.##", new DecimalFormatSymbols(Localization.getJavaLocale()));
		Formats.Format<Date> sdf = Formats.getDateFormat(Formats.Pattern.DATE_EVENT);
		for (DatePattern pattern: patterns) {
			LineInterface line = table.addLine();
			line.setId(pattern.getUniqueId());
			line.setURL("#" + pattern.getUniqueId());
			line.setAnchor("A" + pattern.getUniqueId());
			
			CellInterface name = line.addCell(pattern.getName());
			if (pattern.isDefault())
				name.addStyle("font-weight: bold;");
			line.addCell(pattern.getDatePatternType().getLabel());
			if (used.contains(pattern) || pattern.isDefault()) {
				line.addCell().setComparable(true)
					.addImage().setSource("images/accept.png").setTitle(MSG.infoDatePatternUsed()).setAlt(MSG.altYes());
			} else {
				line.addCell().setComparable(false);
			}
			if (pattern.getNumberOfWeeks() == null) {
				line.addCell(df.format(pattern.getComputedNumberOfWeeks())).addStyle("font-style: italic;");
			} else {
				line.addCell(df.format(pattern.getNumberOfWeeks()));
			}
			
			if (request.isExport()) {
				line.addCell(sdf.format(pattern.getStartDate())).setComparable(pattern.getStartDate());
				line.addCell(sdf.format(pattern.getEndDate())).setComparable(pattern.getEndDate());
			}
			
			if (pattern.isPatternSet()) {
				CellInterface cell = line.addCell();
				CellInterface c = null;
				List<DatePattern> children = pattern.findChildren();
				for (DatePattern child: children) {
					if (c != null) c.add(", ");
	        		c = new CellInterface().setInline(request.isExport() || children.size() > 4).setNoWrap(false);
					c.add(child.getName());
					cell.addItem(c);
            	}
			} else {
            	line.addCell(getPatternCell(pattern));
            }
			
			if (hasSet) {
				CellInterface cell = line.addCell();
				CellInterface c = null;
				for (DatePattern parent: new TreeSet<DatePattern>(pattern.getParents())) {
					if (c != null) c.add(", ");
	        		c = new CellInterface().setInline(!request.isExport() && pattern.getParents().size() > 4).setNoWrap(false);
					c.add(parent.getName());
					cell.addItem(c);
            	}
			}
			
			CellInterface deptsCell = line.addCell();
        	CellInterface c = null;
        	for (Department dept: new TreeSet<Department>(pattern.getDepartments())) {
        		if (c != null) c.add(", ");
        		c = new CellInterface().setInline(!request.isExport() && pattern.getDepartments().size() > 4).setNoWrap(false);
        		if (dept.isExternalManager())
        			c.addStyle("font-weight: bold;");
        		c.add(dept.getDeptCode() + (dept.getAbbreviation() == null || dept.getAbbreviation().equals(dept.getDeptCode()) ? "" : ": " + dept.getAbbreviation()))
        			.setTitle(dept.getLabel()).setNoWrap(true);
        		deptsCell.addItem(c);
        	}
			
			if (!pattern.isVisible())
				for (CellInterface cell: line.getCells())
					cell.setColor("#646464");
		}
        
		response.setTable(table);
		response.setCanAdd(true);
		return response;
	}
	
	protected static CellInterface getPatternCell(DatePattern dp) {
		CellInterface cell = new CellInterface();
		HashMap<Date, Date> dates = dp.getPatternDateStringHashMaps();
		Formats.Format<Date> df = Formats.getDateFormat(Formats.Pattern.DATE_SHORT);
		for (Iterator<Date> i = new TreeSet<Date>(dates.keySet()).iterator(); i.hasNext(); ){
			Date startDate = i.next();
			Date endDate = dates.get(startDate);
			if (startDate.equals(endDate)) {
				cell.add(df.format(startDate) + (i.hasNext() ? ", " : ""));
			} else {
				cell.add(df.format(startDate) + "-" + df.format(endDate) + (i.hasNext() ? ", " : ""));
			}
		}
		return cell;
	}

}
