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
package org.unitime.timetable.gwt.client.instructor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader.HasColumnName;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader.Operation;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.GwtResources;
import org.unitime.timetable.gwt.shared.InstructorInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.AttributeInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.AttributesColumn;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;

/**
 * @author Tomas Muller
 */
public class InstructorAttributesTable extends UniTimeTable<AttributeInterface>  {
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	protected static final GwtResources RESOURCES =  GWT.create(GwtResources.class);
	
	private AttributesColumn iSortBy = null;
	private boolean iAsc = true;
	
	public InstructorAttributesTable() {
		setStyleName("unitime-InstructorAttributes");
		
		List<UniTimeTableHeader> header = new ArrayList<UniTimeTableHeader>();
		for (AttributesColumn column: AttributesColumn.values()) {
			int nrCells = getNbrCells(column);
			for (int idx = 0; idx < nrCells; idx++) {
				UniTimeTableHeader h = new UniTimeTableHeader(getColumnName(column, idx), getColumnAlignment(column, idx));
				header.add(h);
			}
		}
		
		for (final AttributesColumn column: AttributesColumn.values()) {
			if (InstructorAttributesComparator.isApplicable(column) && getNbrCells(column) > 0) {
				final UniTimeTableHeader h = header.get(getCellIndex(column));
				Operation op = new SortOperation() {
					@Override
					public void execute() {
						doSort(column);
					}
					@Override
					public boolean isApplicable() { return getRowCount() > 1 && h.isVisible(); }
					@Override
					public boolean hasSeparator() { return true; }
					@Override
					public String getName() { return MESSAGES.opSortBy(getColumnName()); }
					@Override
					public String getColumnName() { return h.getHTML().replace("<br>", " "); }
				};
				h.addOperation(op);
			}
		}
		
		addRow(null, header);
		
		for (int i = 0; i < getCellCount(0); i++)
			getCellFormatter().setStyleName(0, i, "unitime-ClickableTableHeader");
		
		setSortBy(InstructorCookie.getInstance().getSortAttributesBy());
	}
	
	protected void doSort(AttributesColumn column) {
		if (column == iSortBy) {
			iAsc = !iAsc;
		} else {
			iSortBy = column;
			iAsc = true;
		}
		InstructorCookie.getInstance().setSortAttributesBy(getSortBy());
		sort();
	}
	
	public boolean hasSortBy() { return iSortBy != null; }
	public int getSortBy() { return iSortBy == null ? 0 : iAsc ? 1 + iSortBy.ordinal() : -1 - iSortBy.ordinal(); }
	public void setSortBy(int sortBy) {
		if (sortBy == 0) {
			iSortBy = null;
			iAsc = true;
		} else if (sortBy > 0) {
			iSortBy = AttributesColumn.values()[sortBy - 1];
			iAsc = true;
		} else {
			iSortBy = AttributesColumn.values()[-1 - sortBy];
			iAsc = false;
		}
		sort();
	}
	
	public void sort() {
		if (iSortBy == null) return;
		if (getNbrCells(iSortBy) == 0) iSortBy = AttributesColumn.NAME;
		UniTimeTableHeader header = getHeader(getCellIndex(iSortBy));
		sort(header, new InstructorAttributesComparator(iSortBy, true), iAsc);
	}
	
	protected int getNbrCells(AttributesColumn column) {
		switch (column) {
		default:
			return 1;
		}
	}
	
	public String getColumnName(AttributesColumn column, int idx) {
		switch (column) {
		case CODE: return MESSAGES.colAbbreviation();
		case NAME: return MESSAGES.colName();
		case TYPE: return MESSAGES.colType();
		case PARENT: return MESSAGES.colParentAttribute();
		case INSTRUCTORS: return MESSAGES.colInstructors();
		default: return column.name();
		}
	}
	
	protected HorizontalAlignmentConstant getColumnAlignment(AttributesColumn column, int idx) {
		switch (column) {
		default:
			return HasHorizontalAlignment.ALIGN_LEFT;
		}
	}
	
	protected int getCellIndex(AttributesColumn column) {
		int ret = 0;
		for (AttributesColumn c: AttributesColumn.values())
			if (c.ordinal() < column.ordinal()) ret += getNbrCells(c);
		return ret;
	}
	
	protected Widget getCell(final AttributeInterface feature, final AttributesColumn column, final int idx) {
		switch (column) {
		case NAME:
			return new Label(feature.getName() == null ? "" : feature.getName(), false);
		case CODE:
			return new Label(feature.getCode() == null ? "" : feature.getCode(), false);
		case TYPE:
			if (feature.getType() == null)
				return null;
			else {
				Label type = new Label(feature.getType().getAbbreviation(), false);
				type.setTitle(feature.getType().getLabel());
				return type;
			}
		case PARENT:
			return new Label(feature.getParentName() == null ? "" : feature.getParentName(), false);
		case INSTRUCTORS:
			if (feature.hasInstructors())
				return new InstructorsCell(feature);
			else
				return null;
		default:
			return null;
		}
	}
	
	public int addAttribute(final AttributeInterface attribute) {
		List<Widget> widgets = new ArrayList<Widget>();
		
		for (AttributesColumn column: AttributesColumn.values()) {
			int nbrCells = getNbrCells(column);
			for (int idx = 0; idx < nbrCells; idx ++) {
				Widget cell = getCell(attribute, column, idx);
				if (cell == null)
					cell = new P();
				widgets.add(cell);
			}
		}
		
		int row = addRow(attribute, widgets);
		getRowFormatter().setStyleName(row, "row");
		for (int col = 0; col < getCellCount(row); col++)
			getCellFormatter().setStyleName(row, col, "cell");
		
		return row;
	}
	
	public static class InstructorsCell extends P {
		public InstructorsCell(AttributeInterface attribute) {
			super("instructors");
			if (attribute.hasInstructors()) {
				for (Iterator<InstructorInterface> i = attribute.getInstructors().iterator(); i.hasNext(); ) {
					InstructorInterface instructor = i.next();
					add(new InstructorCell(instructor, i.hasNext()));
				}
			}
		}
	}
	
	public static class InstructorCell extends P {
		public InstructorCell(final InstructorInterface instructor, boolean hasNext) {
			super("instructor");
			setText(instructor.getFormattedName() + (hasNext ? ";" : ""));
		}
	}

	public void refreshTable() {
		for (int r = 1; r < getRowCount(); r++) {
			for (int c = 0; c < getCellCount(r); c++) {
				Widget w = getWidget(r, c);
				if (w instanceof HasRefresh)
					((HasRefresh)w).refresh();
			}
		}
	}
	
	public AttributeInterface getAttribute(Long attributeId) {
		if (attributeId == null) return null;
		for (int i = 1; i < getRowCount(); i++) {
			if (attributeId.equals(getData(i).getId())) return getData(i);
		}
		return null;
	}
	
	public void scrollTo(Long attributeId) {
		if (attributeId == null) return;
		for (int i = 1; i < getRowCount(); i++) {
			if (attributeId.equals(getData(i).getId())) {
				ToolBox.scrollToElement(getRowFormatter().getElement(i));
				return;
			}
		}
	}
	
	public static interface SortOperation extends Operation, HasColumnName {}
	
	public static interface HasRefresh {
		public void refresh();
	}
}
