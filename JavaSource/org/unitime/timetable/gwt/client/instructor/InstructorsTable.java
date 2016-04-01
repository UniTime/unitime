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
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.instructor.InstructorAttributesTable.HasRefresh;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader.HasColumnName;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader.Operation;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.GwtResources;
import org.unitime.timetable.gwt.shared.InstructorInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.AttributeInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.AttributeTypeInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.InstructorAttributePropertiesInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.InstructorsColumn;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
import com.google.gwt.user.client.ui.Image;

/**
 * @author Tomas Muller
 */
public class InstructorsTable extends UniTimeTable<InstructorInterface>  {
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	protected static final GwtResources RESOURCES = GWT.create(GwtResources.class);
	protected static final GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	
	private InstructorsColumn iSortBy = null;
	private boolean iAsc = true;
	private boolean iSelectable = false;
	private InstructorAttributePropertiesInterface iProperties = null;
	
	public InstructorsTable(InstructorAttributePropertiesInterface properties, boolean selectable) {
		setStyleName("unitime-Instructorss");
		iSelectable = selectable;
		iProperties = properties;
		
		List<UniTimeTableHeader> header = new ArrayList<UniTimeTableHeader>();
		for (InstructorsColumn column: InstructorsColumn.values()) {
			int nrCells = getNbrCells(column);
			for (int idx = 0; idx < nrCells; idx++) {
				UniTimeTableHeader h = new UniTimeTableHeader(getColumnName(column, idx), getColumnAlignment(column, idx));
				header.add(h);
			}
		}
		
		for (final InstructorsColumn column: InstructorsColumn.values()) {
			if (InstructorComparator.isApplicable(column) && getNbrCells(column) > 0) {
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
		
		if (iSelectable) {
			header.get(0).addOperation(new Operation() {
				@Override
				public void execute() {
					for (int row = 1; row < getRowCount(); row++) {
						Widget w =  getWidget(row, 0);
						if (w != null && w instanceof CheckBox) {
							CheckBox ch = (CheckBox)w;
							ch.setValue(true);
						}
						setSelected(row, true);
					}
				}
				@Override
				public boolean isApplicable() {
					for (int row = 1; row < getRowCount(); row++) {
						Widget w =  getWidget(row, 0);
						if (w != null && w instanceof CheckBox) {
							CheckBox ch = (CheckBox)w;
							if (!ch.getValue()) return true;
						}
					}
					return false;
				}
				@Override
				public boolean hasSeparator() {
					return false;
				}
				@Override
				public String getName() {
					return MESSAGES.opSelectAll();
				}
			});
			header.get(0).addOperation(new Operation() {
				@Override
				public void execute() {
					for (int row = 1; row < getRowCount(); row++) {
						Widget w =  getWidget(row, 0);
						if (w != null && w instanceof CheckBox) {
							CheckBox ch = (CheckBox)w;
							ch.setValue(false);
						}
						setSelected(row, false);
					}
				}
				@Override
				public boolean isApplicable() {
					for (int row = 1; row < getRowCount(); row++) {
						Widget w =  getWidget(row, 0);
						if (w != null && w instanceof CheckBox) {
							CheckBox ch = (CheckBox)w;
							if (ch.getValue()) return true;
						}
					}
					return false;
				}
				@Override
				public boolean hasSeparator() {
					return false;
				}
				@Override
				public String getName() {
					return MESSAGES.opClearSelection();
				}
			});

			setAllowSelection(true);
			addMouseClickListener(new MouseClickListener<InstructorInterface>() {
				@Override
				public void onMouseClick(TableEvent<InstructorInterface> event) {
					selectInstructor(event.getRow(), isSelected(event.getRow()));
				}
			});
		}
	}
	
	protected void doSort(InstructorsColumn column) {
		if (column == iSortBy) {
			iAsc = !iAsc;
		} else {
			iSortBy = column;
			iAsc = true;
		}
		InstructorCookie.getInstance().setSortInstructorsBy(getSortBy());
		sort();
	}
	
	public boolean hasSortBy() { return iSortBy != null; }
	public int getSortBy() { return iSortBy == null ? 0 : iAsc ? 1 + iSortBy.ordinal() : -1 - iSortBy.ordinal(); }
	public void setSortBy(int sortBy) {
		if (sortBy == 0) {
			iSortBy = null;
			iAsc = true;
		} else if (sortBy > 0) {
			iSortBy = InstructorsColumn.values()[sortBy - 1];
			iAsc = true;
		} else {
			iSortBy = InstructorsColumn.values()[-1 - sortBy];
			iAsc = false;
		}
		sort();
	}
	
	public void sort() {
		if (iSortBy == null) return;
		if (getNbrCells(iSortBy) == 0) iSortBy = InstructorsColumn.NAME;
		UniTimeTableHeader header = getHeader(getCellIndex(iSortBy));
		Comparator<InstructorInterface> ic = null;
		if (iSelectable && iSortBy == InstructorsColumn.SELECTION) {
			ic = new Comparator<InstructorInterface>() {
				private Comparator<InstructorInterface> iIC = new InstructorComparator(iSortBy, true);
				@Override
				public int compare(InstructorInterface r1, InstructorInterface r2) {
					boolean s1 = isInstructorSelected(r1), s2 = isInstructorSelected(r2);
					if (s1 != s2)
						return s1 ? -1 : 1;
					return iIC.compare(r1, r2);
				}
			};
		} else {
			ic = new InstructorComparator(iSortBy, true);
		}
		sort(header, ic, iAsc);
	}
	
	protected int getNbrCells(InstructorsColumn column) {
		switch (column) {
		case SELECTION:
			return iSelectable ? 1 : 0;
		case ATTRIBUTES:
			return (iProperties == null ? 0 : iProperties.getAttributeTypes().size());
		default:
			return 1;
		}
	}
	
	protected boolean isVisible(InstructorsColumn column, int idx) {
		switch (column) {
		case ATTRIBUTES:
			return false;
		default:
			return true;
		}
	}
	
	public String getColumnName(InstructorsColumn column, int idx) {
		switch (column) {
		case SELECTION: return MESSAGES.colSelection();
		case ID: return MESSAGES.colExternalId();
		case NAME: return MESSAGES.colNamePerson();
		case POSITION: return MESSAGES.colPosition();
		case TEACHING_PREF: return MESSAGES.colTeachingPreference();
		case MAX_LOAD: return MESSAGES.colMaxLoad();
		case ATTRIBUTES: return iProperties.getAttributeTypes().get(idx).getLabel();
		default: return column.name();
		}
	}
	
	protected HorizontalAlignmentConstant getColumnAlignment(InstructorsColumn column, int idx) {
		switch (column) {
		default:
			return HasHorizontalAlignment.ALIGN_LEFT;
		}
	}
	
	protected int getCellIndex(InstructorsColumn column) {
		int ret = 0;
		for (InstructorsColumn c: InstructorsColumn.values())
			if (c.ordinal() < column.ordinal()) ret += getNbrCells(c);
		return ret;
	}
	
	protected Widget getCell(final InstructorInterface instructor, final InstructorsColumn column, final int idx) {
		switch (column) {
		case ID:
			if (instructor.getExternalId() == null) {
				Image warning = new Image(RESOURCES.warning());
				warning.setTitle(MESSAGES.warnInstructorHasNoExternalId(instructor.getFormattedName()));
				return warning;
			} else {
				return new Label(instructor.getExternalId());
			}
		case NAME:
			return new Label(instructor.getFormattedName());
		case POSITION:
			return new Label(instructor.getPosition() == null ? "" : instructor.getPosition().getLabel());
		case TEACHING_PREF:
			if (instructor.getTeachingPreference() == null) {
				return new Label("");
			} else {
				Label pref = new Label(instructor.getTeachingPreference().getName());
				if (instructor.getTeachingPreference().getColor() != null)
					pref.getElement().getStyle().setColor(instructor.getTeachingPreference().getColor());
				return pref;
			}
		case MAX_LOAD:
			return new Label(instructor.hasMaxLoad() ? NumberFormat.getFormat(CONSTANTS.teachingLoadFormat()).format(instructor.getMaxLoad()) : "");
		case SELECTION:
			return new SelectableCell(instructor);
		case ATTRIBUTES:
			AttributeTypeInterface type = iProperties.getAttributeTypes().get(idx);
			List<AttributeInterface> attributes = instructor.getAttributes(type);
			if (!attributes.isEmpty() && !isColumnVisible(getCellIndex(column) + idx)) {
				setColumnVisible(getCellIndex(column) + idx, true);
			}
			return new AttributesCell(attributes);
		default:
			return null;
		}
	}
	
	public void resetVisibility() {
		int col = 0;
		for (InstructorsColumn column: InstructorsColumn.values()) {
			int nbrCells = getNbrCells(column);
			for (int idx = 0; idx < nbrCells; idx ++) {
				if (!isVisible(column, idx))
					setColumnVisible(col, false);
				col++;
			}
		}
	}
	
	public int addInstructor(final InstructorInterface attribute) {
		List<Widget> widgets = new ArrayList<Widget>();
		
		for (InstructorsColumn column: InstructorsColumn.values()) {
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
			setText(instructor.getFormattedName() + (hasNext ? "," : ""));
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
	
	public InstructorInterface getAttribute(Long instructorId) {
		if (instructorId == null) return null;
		for (int i = 1; i < getRowCount(); i++) {
			if (instructorId.equals(getData(i).getId())) return getData(i);
		}
		return null;
	}
	
	public void scrollTo(Long instructorId) {
		if (instructorId == null) return;
		for (int i = 1; i < getRowCount(); i++) {
			if (instructorId.equals(getData(i).getId())) {
				ToolBox.scrollToElement(getRowFormatter().getElement(i));
				return;
			}
		}
	}
	
	public static interface SortOperation extends Operation, HasColumnName {}
	
	public static class AttributesCell extends P {
		public AttributesCell(List<? extends AttributeInterface> attributes) {
			super();
			setStyleName("attributes");
			for (AttributeInterface attribute: attributes) {
				P p = new P("attribute");
				p.setText(attribute.getName());
				add(p);
			}
		}
	}
	
	public Boolean isInstructorSelected(int row) {
		Widget w = getWidget(row, 0);
		if (w != null && w instanceof CheckBox) {
			return ((CheckBox)w).getValue();
		} else {
			return null;
		}
	}
	
	public boolean isInstructorSelected(InstructorInterface instructor) {
		if (!iSelectable) return false;
		for (int row = 1; row < getRowCount(); row++ ) {
			if (getData(row).getId().equals(instructor.getId()))
				return isInstructorSelected(row);
		}
		return false;
	}
	
	public int getRow(InstructorInterface instructor) {
		for (int row = 1; row < getRowCount(); row++ )
			if (getData(row).getId().equals(instructor.getId()))
				return row;
		return -1;
	}
	
	public void selectInstructor(InstructorInterface instructor, boolean value) {
		if (!iSelectable) return;
		for (int row = 1; row < getRowCount(); row++ ) {
			if (getData(row).getId().equals(instructor.getId())) {
				selectInstructor(row, value);
				break;
			}
		}
	}
	
	public void selectInstructor(int row, boolean value) {
		Widget w = getWidget(row, 0);
		if (w != null && w instanceof CheckBox) {
			((CheckBox)w).setValue(value);
		}
	}
	
	public CheckBox getInstructorSelection(int row) {
		Widget w = getWidget(row, 0);
		if (w != null && w instanceof CheckBox) {
			return (CheckBox)w;
		} else {
			return null;
		}
	}
	
	class SelectableCell extends CheckBox {
		SelectableCell(final InstructorInterface instructor) {
			addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					event.stopPropagation();
				}
			});
			addValueChangeHandler(new ValueChangeHandler<Boolean>() {
				@Override
				public void onValueChange(ValueChangeEvent<Boolean> event) {
					setSelected(getRow(instructor), event.getValue());
				}
			});
		}
	}
}