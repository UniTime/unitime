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

import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.Clazz;
import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.CourseDivision;
import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.CourseGroup;
import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.Instructor;
import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.TeachingMeeting;
import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.MeetingAssignment;
import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.TeachingSchedule;
import org.unitime.timetable.gwt.client.widgets.NumberBox;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.client.widgets.UniTimeWidget;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtResources;
import org.unitime.timetable.gwt.resources.TeachingScheduleMessages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.TextAlign;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;

public class TeachingScheduleAssignmentsTable extends UniTimeTable<MeetingAssignment> {
	public static TeachingScheduleMessages MESSAGES = GWT.create(TeachingScheduleMessages.class);
	public static GwtResources RESOURCES = GWT.create(GwtResources.class);
	public static GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	private TeachingSchedule iOffering = null;
	private Clazz iClazz = null;
	
	public TeachingScheduleAssignmentsTable(TeachingSchedule offering, Clazz clazz, boolean readOnly) {
		setStyleName("unitime-TeachingScheduleAssignments");
		iOffering = offering;
		iClazz = clazz;
		
		List<UniTimeTableHeader> header = new ArrayList<UniTimeTableHeader>();
		for (Column column: Column.values()) {
			int nrCells = getNbrCells(column);
			for (int idx = 0; idx < nrCells; idx++) {
				UniTimeTableHeader h = new UniTimeTableHeader(getColumnName(column, idx), getColumnAlignment(column, idx));
				header.add(h);
			}
		}
		
		addRow(null, header);
		
		for (MeetingAssignment ma: iClazz.getMeetingAssignments()) {
			addMeetingAssignment(ma, getRowCount(), readOnly);
		}
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
		case OPERATIONS:
			return 2;
		default:
			return 1;
		}
	}
	
	public String getColumnName(Column column, int idx) {
		switch (column) {
		case DATE: return MESSAGES.colMeetingDate();
		case TIME: return MESSAGES.colMeetingTime();
		case HOURS: return MESSAGES.colMeetingHours();
		case DIVISION: return MESSAGES.colMeetingDivision();
		case INSTRUCTOR: return MESSAGES.colMeetingInstructor();
		case NOTE: return MESSAGES.colMeetingNote();
		case OPERATIONS: return "";
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
	
	protected void updateTime(int row) {
		MeetingAssignment ma = getData(row);
		if (ma != null) {
			TeachingMeeting meeting = iOffering.getMeeting(ma);
			Label time = (Label)getWidget(row, getCellIndex(Column.TIME));
			time.setText(slot2time(meeting.getHour(ma.getFirstHour()).getStartSlot()) + " - " + slot2time(meeting.getHour(ma.getLastHour()).getEndSlot()));
			UniTimeWidget<NumberBox> hours = (UniTimeWidget<NumberBox>)getWidget(row, getCellIndex(Column.HOURS));
			hours.getWidget().setValue(ma.getLastHour() - ma.getFirstHour() + 1);
		}
	}
	
	protected Widget getReadOnlyCell(final MeetingAssignment ma, final Column column, final int idx) {
		final TeachingMeeting meeting = iOffering.getMeeting(ma);
		switch (column) {
		case DATE:
			return new Label(meeting.getMeetingDate());
		case TIME:
			return new Label(slot2time(meeting.getHour(ma.getFirstHour()).getStartSlot()) + " - " + slot2time(meeting.getHour(ma.getLastHour()).getEndSlot()));
		case HOURS:
			return new NumberCell(ma.getLastHour() - ma.getFirstHour() + 1);
		case DIVISION:
			return new Label(ma.getDivision() == null ? "" : ma.getDivision().getName());
		case INSTRUCTOR:
			P instructors = new P("unitime-Instructors");
			if (ma.hasInstructors()) {
				for (Long id: ma.getInstructor()) {
					Instructor instructor = iOffering.getInstructor(id);
					if (instructor != null) instructors.add(new Label(instructor.getName()));
				}
			}
			return instructors;
		case NOTE:
			return new Label(ma.hasNote() ? ma.getNote() : "");
		case OPERATIONS:
			return null;
		default:
			return null;
		}
	}
	
	protected Widget getCell(final MeetingAssignment ma, final Column column, final int idx) {
		final TeachingMeeting meeting = iOffering.getMeeting(ma);
		final CourseGroup group = iOffering.getGroup(iClazz.getConfigId(), iClazz.getTypeId());
		switch (column) {
		case DATE:
			return new Label(meeting.getMeetingDate());
		case TIME:
			return new Label(slot2time(meeting.getHour(ma.getFirstHour()).getStartSlot()) + " - " + slot2time(meeting.getHour(ma.getLastHour()).getEndSlot()));
		case HOURS:
			final UniTimeWidget<NumberBox> hours = new UniTimeWidget<NumberBox>(new NumberBox());
			hours.getWidget().setDecimal(false);
			hours.getWidget().setWidth("50px");
			hours.getWidget().setNegative(false);
			hours.getWidget().addValueChangeHandler(new ValueChangeHandler<String>() {
				@Override
				public void onValueChange(ValueChangeEvent<String> event) {
					hours.clearHint();
					Integer h = hours.getWidget().toInteger();
					if (h == null || h < 1) {
						h = 1; hours.getWidget().setValue(h);
					}
					int max = meeting.getHours().size() - ma.getFirstHour();
					if (h > max) {
						h = max; hours.getWidget().setValue(h);
					}
					int last = ma.getFirstHour() + h - 1;
					ma.setHours(ma.getFirstHour(), last);
					int row = getRowForWidget(hours) ;
					updateTime(row);
					row ++;
					int first = last + 1;
					while (first < meeting.getHours().size()) {
						MeetingAssignment next = getData(row);
						if (next != null && next.getClassMeetingId().equals(ma.getClassMeetingId())) {
							if (first <= next.getFirstHour()) {
							next.setHours(first, next.getLastHour());
								updateTime(row);
								row++;
								break;
							} else {
								iClazz.getMeetingAssignments().remove(next);
								removeRow(row);
								continue;
							}
						} else {
							next = new MeetingAssignment(ma); next.setHours(first, meeting.getHours().size() - 1);
							addMeetingAssignment(next, insertRow(row), false);
							iClazz.getMeetingAssignments().add(iClazz.getMeetingAssignments().indexOf(ma) + 1, next);
							row ++;
							break;
						}
					}
					MeetingAssignment next = getData(row);
					while (next != null && next.getClassMeetingId().equals(ma.getClassMeetingId())) {
						iClazz.getMeetingAssignments().remove(next);
						removeRow(row);
						next = getData(row);
					}
				}
			});
			hours.getWidget().setValue(ma.getLastHour() - ma.getFirstHour() + 1);
			return hours;
		case DIVISION:
			final UniTimeWidget<ListBox> attribute = new UniTimeWidget<ListBox>(new ListBox());
			attribute.getWidget().setMultipleSelect(false);
			attribute.getWidget().setWidth("150px");
			attribute.getWidget().setStyleName("unitime-TextBox");
			attribute.getWidget().addItem("", "");
			int idxDiv = 0;
			for (CourseDivision division: group.getDivisions()) {
				attribute.getWidget().addItem(division.getName(), String.valueOf(idxDiv));
				idxDiv++;
				if (division.equals(ma.getDivision()))
					attribute.getWidget().setSelectedIndex(attribute.getWidget().getItemCount() - 1);
			}
			if (attribute.getWidget().getItemCount() == 2 && attribute.getWidget().getSelectedIndex() == 0) {
				attribute.getWidget().setSelectedIndex(1);
				ma.setDivision(group.getDivisions().get(0));
				ma.setNote(group.getDivisions().get(0).getName());
			}
			attribute.getWidget().addChangeHandler(new ChangeHandler() {
				@Override
				public void onChange(ChangeEvent event) {
					attribute.clearHint();
					int idx = attribute.getWidget().getSelectedIndex();
					if (idx == 0)
						ma.setDivision(null);
					else
						ma.setDivision(group.getDivisions().get(idx - 1));
					UniTimeNotifications.info("Change: " + (ma.getDivision() == null ? "not set" : ma.getDivision().getDivisionIndex() + " - " + ma.getDivision().getAttributeRef()));
					divisionChanged(getRowForWidget(attribute));
				}
			});
			return attribute;
		case INSTRUCTOR:
			return new TeachingScheduleInstructorAssignments(iOffering, ma);
		case NOTE:
			final UniTimeWidget<TextBox> note = new UniTimeWidget<TextBox>(new TextBox());
			note.getWidget().setStyleName("unitime-TextBox");
			note.getWidget().setMaxLength(200);
			note.getWidget().setWidth("250px");
			if (ma.hasNote()) note.getWidget().setText(ma.getNote());
			note.getWidget().addValueChangeHandler(new ValueChangeHandler<String>() {
				@Override
				public void onValueChange(ValueChangeEvent<String> event) {
					note.clearHint();
					ma.setNote(event.getValue());
				}
			});
			return note;
		case OPERATIONS:
			return null;
		default:
			return null;
		}
	}
	
	protected void divisionChanged(int row) {
		UniTimeWidget<ListBox> division = (UniTimeWidget<ListBox>)getWidget(row, getCellIndex(Column.DIVISION));
		TeachingScheduleInstructorAssignments instructor = (TeachingScheduleInstructorAssignments)getWidget(row, getCellIndex(Column.INSTRUCTOR));
		instructor.assignmentChanged();
		MeetingAssignment ma = getData(row);
		UniTimeWidget<TextBox> note = (UniTimeWidget<TextBox>)getWidget(row, getCellIndex(Column.NOTE));
		CourseGroup group = iOffering.getGroup(iClazz.getConfigId(), iClazz.getTypeId());
		if (division.getWidget().getSelectedIndex() > 0) {
			ma.setNote(group.getDivisions().get(division.getWidget().getSelectedIndex() - 1).getName());
			note.getWidget().setText(ma.getNote() == null ? "" : ma.getNote());
		}
	}
	
	protected void addMeetingAssignment(MeetingAssignment ma, int row, boolean readOnly) {
		List<Widget> widgets = new ArrayList<Widget>();
		
		for (Column column: Column.values()) {
			int nbrCells = getNbrCells(column);
			for (int idx = 0; idx < nbrCells; idx ++) {
				Widget cell = (readOnly ? getReadOnlyCell(ma, column, idx) : getCell(ma, column, idx));
				if (cell == null)
					cell = new P();
				widgets.add(cell);
			}
		}
		
		setRow(row, ma, widgets);
		getRowFormatter().setStyleName(row, "row");
		for (int col = 0; col < getCellCount(row); col++)
			getCellFormatter().setStyleName(row, col, "cell");
	}

	public static enum Column {
		DATE,
		TIME,
		HOURS,
		DIVISION,
		INSTRUCTOR,
		NOTE,
		OPERATIONS
		;
	}
	
	public static class NumberLabel extends Label {
		NumberLabel(Integer value) {
			super(value == null ? "" : value.toString());
			getElement().getStyle().setTextAlign(TextAlign.RIGHT);
		}
	}
}
