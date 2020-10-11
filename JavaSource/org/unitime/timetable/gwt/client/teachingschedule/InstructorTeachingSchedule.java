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
package org.unitime.timetable.gwt.client.teachingschedule;

import java.util.ArrayList;
import java.util.List;

import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.InstructorMeetingAssignment;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtResources;
import org.unitime.timetable.gwt.resources.TeachingScheduleMessages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.TextAlign;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;

public class InstructorTeachingSchedule extends UniTimeTable<InstructorMeetingAssignment> {
	public static TeachingScheduleMessages MESSAGES = GWT.create(TeachingScheduleMessages.class);
	public static GwtResources RESOURCES = GWT.create(GwtResources.class);
	public static GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	
	public InstructorTeachingSchedule() {
		setStyleName("unitime-InstructorMeetingAssignment");
		
		List<UniTimeTableHeader> header = new ArrayList<UniTimeTableHeader>();
		for (Column column: Column.values()) {
			int nrCells = getNbrCells(column);
			for (int idx = 0; idx < nrCells; idx++) {
				UniTimeTableHeader h = new UniTimeTableHeader(getColumnName(column, idx), getColumnAlignment(column, idx));
				header.add(h);
			}
		}
		
		addRow(null, header);
	}
	
	public static String slot2time(int slot) {
		int timeSinceMidnight = 5 * slot;
		int hour = timeSinceMidnight / 60;
	    int min = timeSinceMidnight % 60;
	    if (CONSTANTS.useAmPm())
	    	return (hour==0?12:hour>12?hour-12:hour)+":"+(min<10?"0":"")+min+(hour<24 && hour>=12?"p":"a");
	    else
	    	return hour + ":" + (min < 10 ? "0" : "") + min;
	}
	
	protected int getNbrCells(Column column) {
		switch (column) {
		default:
			return 1;
		}
	}
	
	public String getColumnName(Column column, int idx) {
		switch (column) {
		case COURSE: return MESSAGES.colOffering();
		case GROUP: return MESSAGES.colMeetingGroup();
		case DIVISION: return MESSAGES.colMeetingDivision();
		case DATE: return MESSAGES.colMeetingDate();
		case HOURS: return MESSAGES.colMeetingHours();
		case TIME: return MESSAGES.colMeetingTime();
		case ROOM: return MESSAGES.colMeetingRoom();
		case NOTE: return MESSAGES.colMeetingNote();
		default: return column.name();
		}
	}
	
	protected HorizontalAlignmentConstant getColumnAlignment(Column column, int idx) {
		switch (column) {
		case HOURS:
			return HasHorizontalAlignment.ALIGN_RIGHT;
		default:
			return HasHorizontalAlignment.ALIGN_LEFT;
		}
	}
	
	protected int getCellIndex(Column column) {
		int ret = 0;
		for (Column c: Column.values())
			if (c.ordinal() < column.ordinal()) ret += getNbrCells(c);
		return ret;
	}
	protected Widget getCell(final InstructorMeetingAssignment meeting, final Column column, final int idx) {
		switch (column) {
		case DATE:
			return new Label(meeting.getMeetingDate());
		case TIME:
			return new Label(slot2time(meeting.getHours().getStartSlot()) + " - " + slot2time(meeting.getHours().getEndSlot()));
		case HOURS:
			return new NumberLabel(meeting.getLoad());
		case ROOM:
			return new Label(meeting.getLocation() == null ? "" : meeting.getLocation());
		case DIVISION:
			return new Label(meeting.getDivision() == null ? "" : meeting.getDivision());
		case NOTE:
			return new Label(meeting.getNote() == null ? "" : meeting.getNote());
		case COURSE:
			return new Label(meeting.getName());
		case GROUP:
			return new Label(meeting.getGroup() == null ? "" : meeting.getGroup());
		default:
			return null;
		}
	}

	protected void addInstructorMeetingAssignment(InstructorMeetingAssignment a) {
		List<Widget> widgets = new ArrayList<Widget>();
		
		for (Column column: Column.values()) {
			int nbrCells = getNbrCells(column);
			for (int idx = 0; idx < nbrCells; idx ++) {
				Widget cell = getCell(a, column, idx);
				if (cell == null)
					cell = new P();
				widgets.add(cell);
			}
		}
		
		int row = getRowCount();
		setRow(row, a, widgets);
		getRowFormatter().setStyleName(row, "row");
		for (int col = 0; col < getCellCount(row); col++)
			getCellFormatter().setStyleName(row, col, "cell");
	}

	public static enum Column {
		COURSE,
		GROUP,
		DIVISION,
		HOURS,
		DATE,
		TIME,
		ROOM,
		NOTE,
		;
	}
	
	public static class NumberLabel extends Label {
		NumberLabel(Integer value) {
			super(value == null ? "" : value.toString());
			getElement().getStyle().setTextAlign(TextAlign.RIGHT);
		}
	}
}