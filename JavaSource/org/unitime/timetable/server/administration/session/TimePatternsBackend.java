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
import java.util.Set;
import java.util.TreeSet;

import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.gwt.client.admin.TimePatternsPage.TimePatternsRequest;
import org.unitime.timetable.gwt.client.admin.TimePatternsPage.TimePatternsResponse;
import org.unitime.timetable.gwt.client.tables.TableInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.LineInterface;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.TimePatternDays;
import org.unitime.timetable.model.TimePatternTime;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.Constants;

@GwtRpcImplements(TimePatternsRequest.class)
public class TimePatternsBackend implements GwtRpcImplementation<TimePatternsRequest, TimePatternsResponse> {
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	protected final static GwtConstants CONSTANTS = Localization.create(GwtConstants.class);

	@Override
	public TimePatternsResponse execute(TimePatternsRequest request, SessionContext context) {
		context.checkPermission(Right.TimePatterns);
		
		TimePatternsResponse response = new TimePatternsResponse();
		
		TableInterface table = new TableInterface();
		table.setId("TimePatterns");
		table.setDefaultSortCookie(MSG.columnTimePatternType());
		table.setName(MSG.sectTimePatterns());
		
		LineInterface header = table.addHeader();
		header.addCell(MSG.columnTimePatternName());
        header.addCell(MSG.columnTimePatternType());
        if (request.isExport())
        	header.addCell(MSG.columnTimePatternVisible());	
        header.addCell(MSG.columnTimePatternUsed());
        header.addCell(MSG.columnTimePatternNbrMtgs());
        header.addCell(MSG.columnTimePatternMinPerMtg());
        header.addCell(MSG.columnTimePatternSlotsPerMtg());
        header.addCell(MSG.columnTimePatternBreakTime());
        header.addCell(MSG.columnTimePatternDays());
        header.addCell(MSG.columnTimePatternTimes());
        header.addCell(MSG.columnTimePatternDepartments());
        for (CellInterface cell: header.getCells()) {
    		cell.setClassName("WebTableHeader");
    		cell.setText(cell.getText().replace("<br>", "\n"));
    		cell.addStyle("white-space: pre-wrap;");
    		cell.setSortable(true);
    	}
        
        List<TimePattern> patterns = TimePattern.findAll(context.getUser().getCurrentAcademicSessionId(), null);
        Set<TimePattern> used = TimePattern.findAllUsed(context.getUser().getCurrentAcademicSessionId());
        if (patterns.isEmpty())
        	table.setErrorMessage(MSG.errorNoTimePatternsDefined());
        for (TimePattern pattern: patterns) {
			LineInterface line = table.addLine();
			line.setId(pattern.getUniqueId());
			line.setURL("#" + pattern.getUniqueId());
			line.setAnchor("A" + pattern.getUniqueId());
			
			line.addCell(pattern.getName());
			line.addCell(pattern.getTimePatternType().getLabel()).setComparable(pattern.getTimePatternType().ordinal(), pattern.getName());
			if (request.isExport()) {
				if (pattern.isVisible())
					line.addCell().setComparable(true)
					.addImage().setSource("images/accept.png").setAlt(MSG.altYes()).setTitle(MSG.yes());
				else
					line.addCell().setComparable(false).setTitle(MSG.no());
			}
			if (used.contains(pattern)) {
				line.addCell().setComparable(true)
					.addImage().setSource("images/accept.png").setTitle(request.isExport() ? MSG.yes() : MSG.hintTimePatternUsed()).setAlt(MSG.altYes());
			} else {
				line.addCell().setComparable(false).setTitle(request.isExport() ? MSG.no() : null);
			}
			line.addCell(pattern.getNrMeetings().toString()).setComparable(pattern.getNrMeetings())
				.setComparable(pattern.getNrMeetings(), pattern.getSlotsPerMtg(), pattern.getType(), pattern.getName());
			line.addCell(pattern.getMinPerMtg().toString()).setComparable(pattern.getMinPerMtg())
				.setComparable(pattern.getSlotsPerMtg(), pattern.getNrMeetings(), pattern.getType(), pattern.getName());
			line.addCell(pattern.getSlotsPerMtg().toString()).setComparable(pattern.getSlotsPerMtg())
				.setComparable(pattern.getSlotsPerMtg(), pattern.getNrMeetings(), pattern.getType(), pattern.getName());
			line.addCell(pattern.getBreakTime().toString()).setComparable(pattern.getBreakTime())
				.setComparable(pattern.getBreakTime(), pattern.getType(), pattern.getName());
			
			CellInterface daysCell = line.addCell();
        	CellInterface c = null;
        	for (TimePatternDays days: new TreeSet<TimePatternDays>(pattern.getDays())) {
        		if (c != null) c.add(", ");
        		c = new CellInterface().setNoWrap(false);
        		int dayCode = days.getDayCode();
        		int nrDays = 0;
    			for (int j=0;j<Constants.NR_DAYS;j++)
    				if ((dayCode&Constants.DAY_CODES[j])!=0) nrDays++;
    			String daysStr = "";
    			for (int j=0;j<Constants.NR_DAYS;j++) {
    				if ((Constants.DAY_CODES[j]&dayCode)==0) continue;
    				daysStr += (nrDays==1?CONSTANTS.days()[j]:CONSTANTS.shortDays()[j]);
    			}
    			c.add(daysStr).setNoWrap(true);
        		daysCell.addItem(c);
        	}
        	
        	CellInterface timesCell = line.addCell();
        	c = null;
        	for (TimePatternTime days: new TreeSet<TimePatternTime>(pattern.getTimes())) {
        		if (c != null) c.add(", ");
        		c = new CellInterface().setNoWrap(false);
        		int startSlot = days.getStartSlot();
        		int min = startSlot*Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN;
    			int time = 100*(min/60) + (min%60);
    			c.add(String.valueOf(time)).setNoWrap(true);
    			timesCell.addItem(c);
        	}
        	
        	CellInterface deptsCell = line.addCell();
        	c = null;
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

}
