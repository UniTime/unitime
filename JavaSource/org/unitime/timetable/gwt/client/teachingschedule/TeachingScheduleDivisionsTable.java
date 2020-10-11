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

import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.Attribute;
import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.CourseDivision;
import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.CourseGroupDivision;
import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.TeachingSchedule;
import org.unitime.timetable.gwt.client.widgets.NumberBox;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.client.widgets.UniTimeWidget;
import org.unitime.timetable.gwt.resources.GwtResources;
import org.unitime.timetable.gwt.resources.TeachingScheduleMessages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.TextAlign;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;

public class TeachingScheduleDivisionsTable extends UniTimeTable<CourseGroupDivision> {
	public static TeachingScheduleMessages MESSAGES = GWT.create(TeachingScheduleMessages.class);
	public static GwtResources RESOURCES = GWT.create(GwtResources.class);
	private TeachingSchedule iOffering = null;
	
	public TeachingScheduleDivisionsTable() {
		setStyleName("unitime-TeachingScheduleDivisions");
		
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
		case ATTRIBUTE: return MESSAGES.colDivisionAttribute();
		case NAME: return MESSAGES.colDivisionName();
		case TYPE: return MESSAGES.colDivisionType();
		case CLASSES: return MESSAGES.colDivisionClasses();
		case GROUPS: return MESSAGES.colDivisionGroups();
		case HOURS: return MESSAGES.colDivisionHours();
		case PARALLELS: return MESSAGES.colDivisionParallels();
		case OPERATIONS: return "";
		default: return column.name();
		}
	}
	
	protected HorizontalAlignmentConstant getColumnAlignment(Column column, int idx) {
		switch (column) {
		case CLASSES:
		case GROUPS:
		case HOURS:
		case PARALLELS:
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
	
	protected Widget getCell(final CourseGroupDivision division, final Column column, final int idx) {
		switch (column) {
		case NAME:
			final UniTimeWidget<TextBox> name = new UniTimeWidget<TextBox>(new TextBox());
			name.getWidget().setStyleName("unitime-TextBox");
			name.getWidget().setMaxLength(100);
			name.getWidget().setWidth("250px");
			name.getWidget().addValueChangeHandler(new ValueChangeHandler<String>() {
				@Override
				public void onValueChange(ValueChangeEvent<String> event) {
					name.clearHint();
					division.getDivision().setName(event.getValue());
				}
			});
			name.getWidget().setText(division.getDivision().getName());
			return name;
		case TYPE:
			if (!division.isMaster()) return new Label();
			return new Label(division.getGroup().getType());
		case CLASSES:
			if (!division.isMaster()) return new Label();
			return new NumberLabel(division.getGroup().getNrClasses());
		case GROUPS:
			if (!division.isMaster()) return new Label();
			final UniTimeWidget<NumberBox> groups = new UniTimeWidget<NumberBox>(new NumberBox());
			groups.getWidget().setDecimal(false);
			groups.getWidget().setWidth("50px");
			groups.getWidget().setNegative(false);
			groups.getWidget().addValueChangeHandler(new ValueChangeHandler<String>() {
				@Override
				public void onValueChange(ValueChangeEvent<String> event) {
					groups.clearHint();
					division.getGroup().setNrGroups(event.getValue().isEmpty() ? 1 : Integer.valueOf(event.getValue()));
				}
			});
			groups.getWidget().setValue(division.getGroup().getNrGroups());
			return groups;
		case ATTRIBUTE:
			final UniTimeWidget<ListBox> attribute = new UniTimeWidget<ListBox>(new ListBox());
			attribute.getWidget().setMultipleSelect(false);
			attribute.getWidget().setWidth("150px");
			attribute.getWidget().setStyleName("unitime-TextBox");
			attribute.getWidget().addItem("", "");
			if (iOffering.hasAttributes())
				for (Attribute a: iOffering.getAttributes()) {
					attribute.getWidget().addItem(a.getLabel(), a.getReference());
					if (a.getReference().equals(division.getDivision().getAttributeRef()))
						attribute.getWidget().setSelectedIndex(attribute.getWidget().getItemCount() - 1);
				}
			attribute.getWidget().addChangeHandler(new ChangeHandler() {
				@Override
				public void onChange(ChangeEvent event) {
					attribute.clearHint();
					division.getDivision().setAttributeRef(attribute.getWidget().getSelectedValue());
					if (attribute.getWidget().getSelectedIndex() > 0) {
						division.getDivision().setName(attribute.getWidget().getSelectedItemText());
						((UniTimeWidget<TextBox>)getWidget(getRowForWidget(attribute), getCellIndex(Column.NAME))).getWidget().setText(attribute.getWidget().getSelectedItemText());
					} else {
						division.getDivision().setName(division.getGroup().getType());
						((UniTimeWidget<TextBox>)getWidget(getRowForWidget(attribute), getCellIndex(Column.NAME))).getWidget().setText(division.getGroup().getType());
					}
				}
			});
			return attribute;
		case HOURS:
			final UniTimeWidget<NumberBox> hours = new UniTimeWidget<NumberBox>(new NumberBox());
			hours.getWidget().setDecimal(false);
			hours.getWidget().setWidth("50px");
			hours.getWidget().setNegative(false);
			hours.getWidget().addValueChangeHandler(new ValueChangeHandler<String>() {
				@Override
				public void onValueChange(ValueChangeEvent<String> event) {
					hours.clearHint();
					division.getDivision().setHours(event.getValue().isEmpty() ? 1 : Integer.valueOf(event.getValue()));
				}
			});
			hours.getWidget().setValue(division.getDivision().getHours());
			return hours;
		case PARALLELS:
			final UniTimeWidget<NumberBox> parallels = new UniTimeWidget<NumberBox>(new NumberBox());
			parallels.getWidget().setDecimal(false);
			parallels.getWidget().setNegative(false);
			parallels.getWidget().setWidth("50px");
			parallels.getWidget().addValueChangeHandler(new ValueChangeHandler<String>() {
				@Override
				public void onValueChange(ValueChangeEvent<String> event) {
					parallels.clearHint();
					division.getDivision().setNrParalel(event.getValue().isEmpty() ? 1 : Integer.valueOf(event.getValue()));
				}
			});
			parallels.getWidget().setValue(division.getDivision().getNrParalel());
			return parallels;
		case OPERATIONS:
			if (idx == 0) {
				Image add = new Image(RESOURCES.add());
				add.getElement().getStyle().setCursor(Cursor.POINTER);
				add.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						int row = getCellForEvent(event).getRowIndex();
						CourseDivision clone = new CourseDivision(division.getDivision());
						clone.setHours(0);
						division.getGroup().addDivision(division.getDivision().getDivisionIndex() + 1, clone);
						addDivision(new CourseGroupDivision(division.getGroup(), clone, false), insertRow(row + 1), false);
					}
				});
				return add;
			} else if (idx == 1 && !division.isMaster()) {
				Image remove = new Image(RESOURCES.delete());
				remove.getElement().getStyle().setCursor(Cursor.POINTER);
				remove.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						int row = getCellForEvent(event).getRowIndex();
						division.getGroup().removeDivision(division.getDivision());
						removeRow(row);
					}
				});
				return remove;
			} else {
				return null;
			}
		default:
			return null;
		}
	}
	
	protected Widget getReadOnlyCell(final CourseGroupDivision division, final Column column, final int idx) {
		switch (column) {
		case NAME:
			return new Label(division.getDivision().getName());
		case TYPE:
			if (!division.isMaster()) return new Label();
			return new Label(division.getGroup().getType());
		case CLASSES:
			if (!division.isMaster()) return new Label();
			return new NumberLabel(division.getGroup().getNrClasses());
		case GROUPS:
			if (!division.isMaster()) return new Label();
			return new NumberLabel(division.getGroup().getNrGroups());
		case ATTRIBUTE:
			Attribute a = iOffering.getAttribute(division.getDivision().getAttributeRef());
			return new Label(a == null ? "" : a.getLabel());
		case HOURS:
			return new NumberLabel(division.getDivision().getHours());
		case PARALLELS:
			return new NumberLabel(division.getDivision().getNrParalel());
		case OPERATIONS:
			return null;
		default:
			return null;
		}
	}
	
	protected void addDivision(CourseGroupDivision division, int row, boolean readOnly) {
		List<Widget> widgets = new ArrayList<Widget>();
		
		for (Column column: Column.values()) {
			int nbrCells = getNbrCells(column);
			for (int idx = 0; idx < nbrCells; idx ++) {
				Widget cell = (readOnly ? getReadOnlyCell(division, column, idx) : getCell(division, column, idx));
				if (cell == null)
					cell = new P();
				widgets.add(cell);
			}
		}
		
		setRow(row, division, widgets);
		getRowFormatter().setStyleName(row, "row");
		for (int col = 0; col < getCellCount(row); col++)
			getCellFormatter().setStyleName(row, col, "cell");
	}
	
	public void setOffering(TeachingSchedule offering, boolean readOnly) {
		iOffering = offering;
		clearTable(1);
		if (iOffering != null)
			for (CourseGroupDivision d: offering.getDivisions())
				addDivision(d, getRowCount(), readOnly);
	}
	
	public TeachingSchedule getOffering() {
		return iOffering;
	}
	
	public static enum Column {
		NAME,
		TYPE,
		ATTRIBUTE,
		CLASSES,
		GROUPS,
		HOURS,
		PARALLELS,
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
