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
package org.unitime.timetable.gwt.client.rooms;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.rooms.RoomsTable.DepartmentCell;
import org.unitime.timetable.gwt.client.rooms.RoomsTable.HasRefresh;
import org.unitime.timetable.gwt.client.rooms.RoomsTable.SortOperation;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader.Operation;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.GwtResources;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcResponse;
import org.unitime.timetable.gwt.shared.RoomInterface.GroupInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomGroupsColumn;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;

/**
 * @author Tomas Muller
 */
public class RoomGroupsTable extends UniTimeTable<GroupInterface> {
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	protected static final GwtResources RESOURCES =  GWT.create(GwtResources.class);
	private boolean iGlobal;
	
	private RoomGroupsColumn iSortBy = null;
	private boolean iAsc = true;
	
	public RoomGroupsTable(boolean isGlobal) {
		setStyleName("unitime-RoomGroups");
		iGlobal = isGlobal;
		
		List<UniTimeTableHeader> header = new ArrayList<UniTimeTableHeader>();
		for (RoomGroupsColumn column: RoomGroupsColumn.values()) {
			int nrCells = getNbrCells(column);
			for (int idx = 0; idx < nrCells; idx++) {
				UniTimeTableHeader h = new UniTimeTableHeader(getColumnName(column, idx), getColumnAlignment(column, idx));
				header.add(h);
			}
		}
		
		for (final RoomGroupsColumn column: RoomGroupsColumn.values()) {
			if (RoomGroupsComparator.isApplicable(column) && getNbrCells(column) > 0) {
				final UniTimeTableHeader h = header.get(getCellIndex(column));
				Operation op = new SortOperation() {
					@Override
					public void execute() {
						doSort(column);
					}
					@Override
					public boolean isApplicable() { return getRowCount() > 1 && h.isVisible(); }
					@Override
					public boolean hasSeparator() { return false; }
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
		
		setSortBy(RoomCookie.getInstance().getRoomGroupsSortBy());
	}
	
	protected void doSort(RoomGroupsColumn column) {
		if (column == iSortBy) {
			iAsc = !iAsc;
		} else {
			iSortBy = column;
			iAsc = true;
		}
		RoomCookie.getInstance().setSortRoomGroupsBy(getSortBy());
		sort();
	}
	
	public boolean hasSortBy() { return iSortBy != null; }
	public int getSortBy() { return iSortBy == null ? 0 : iAsc ? 1 + iSortBy.ordinal() : -1 - iSortBy.ordinal(); }
	public void setSortBy(int sortBy) {
		if (sortBy == 0) {
			iSortBy = null;
			iAsc = true;
		} else if (sortBy > 0) {
			iSortBy = RoomGroupsColumn.values()[sortBy - 1];
			iAsc = true;
		} else {
			iSortBy = RoomGroupsColumn.values()[-1 - sortBy];
			iAsc = false;
		}
		sort();
	}
	
	public void sort() {
		if (iSortBy == null) return;
		if (getNbrCells(iSortBy) == 0) iSortBy = RoomGroupsColumn.NAME;
		UniTimeTableHeader header = getHeader(getCellIndex(iSortBy));
		sort(header, new RoomGroupsComparator(iSortBy, true), iAsc);
	}
	
	protected int getNbrCells(RoomGroupsColumn column) {
		switch (column) {
		case DEFAULT:
			return iGlobal ? 1 : 0;
		case DEPARTMENT:
			return iGlobal ? 0 : 1;
		default:
			return 1;
		}
	}
	
	public String getColumnName(RoomGroupsColumn column, int idx) {
		switch (column) {
		case NAME: return MESSAGES.colName();
		case ABBREVIATION: return MESSAGES.colAbbreviation();
		case DEFAULT: return MESSAGES.colDefault();
		case DEPARTMENT: return MESSAGES.colDepartment();
		case ROOMS: return MESSAGES.colRooms();
		case DESCRIPTION: return MESSAGES.colDescription();
		default: return column.name();
		}
	}
	
	protected HorizontalAlignmentConstant getColumnAlignment(RoomGroupsColumn column, int idx) {
		switch (column) {
		default:
			return HasHorizontalAlignment.ALIGN_LEFT;
		}
	}
	
	protected int getCellIndex(RoomGroupsColumn column) {
		int ret = 0;
		for (RoomGroupsColumn c: RoomGroupsColumn.values())
			if (c.ordinal() < column.ordinal()) ret += getNbrCells(c);
		return ret;
	}
	
	protected Widget getCell(final GroupInterface group, final RoomGroupsColumn column, final int idx) {
		switch (column) {
		case NAME:
			return new Label(group.getLabel() == null ? "" : group.getLabel(), false);
		case ABBREVIATION:
			return new Label(group.getAbbreviation() == null ? "" : group.getAbbreviation(), false);
		case DEFAULT:
			if (group.isDefault())
				return new Image(RESOURCES.on());
			else
				return null;
		case DEPARTMENT:
			return new DepartmentCell(true, group.getDepartment());
		case DESCRIPTION:
			if (group.hasDescription()) {
				HTML html = new HTML(group.getDescription());
				html.setStyleName("description");
				return html;
			} else
				return null;
		case ROOMS:
			if (group.hasRooms())
				return new RoomsCell(group);
			else
				return null;
		default:
			return null;
		}
	}
	
	public int addGroup(final GroupInterface group) {
		List<Widget> widgets = new ArrayList<Widget>();
		
		for (RoomGroupsColumn column: RoomGroupsColumn.values()) {
			int nbrCells = getNbrCells(column);
			for (int idx = 0; idx < nbrCells; idx ++) {
				Widget cell = getCell(group, column, idx);
				if (cell == null)
					cell = new P();
				widgets.add(cell);
			}
		}
		
		int row = addRow(group, widgets);
		getRowFormatter().setStyleName(row, "row");
		for (int col = 0; col < getCellCount(row); col++)
			getCellFormatter().setStyleName(row, col, "cell");
		
		return row;
	}
	
	public static class RoomsCell extends P {
		public RoomsCell(GroupInterface group) {
			super("rooms");
			if (group.hasRooms()) {
				for (Iterator<FilterRpcResponse.Entity> i = group.getRooms().iterator(); i.hasNext(); ) {
					FilterRpcResponse.Entity room = i.next();
					add(new RoomCell(room, i.hasNext()));
				}
			}
		}
	}
	
	public static class RoomCell extends P {
		public RoomCell(final FilterRpcResponse.Entity room, boolean hasNext) {
			super("room");
			setText((room.getAbbreviation() != null && !room.getAbbreviation().isEmpty() ? MESSAGES.label(room.getName(), room.getAbbreviation()) : room.getName()) + (hasNext ? "," : ""));
			addMouseOverHandler(new MouseOverHandler() {
				@Override
				public void onMouseOver(MouseOverEvent event) {
					RoomHint.showHint(RoomCell.this.getElement(), room.getUniqueId(), room.getProperty("prefix", null), room.getProperty("distance", null), true);
				}
			});
			addMouseOutHandler(new MouseOutHandler() {
				@Override
				public void onMouseOut(MouseOutEvent event) {
					RoomHint.hideHint();
				}
			});
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
	
	public GroupInterface getGroup(Long groupId) {
		if (groupId == null) return null;
		for (int i = 1; i < getRowCount(); i++) {
			if (groupId.equals(getData(i).getId())) return getData(i);
		}
		return null;
	}
	
	public void scrollTo(Long groupId) {
		if (groupId == null) return;
		for (int i = 1; i < getRowCount(); i++) {
			if (groupId.equals(getData(i).getId())) {
				ToolBox.scrollToElement(getRowFormatter().getElement(i));
				return;
			}
		}
	}
}
