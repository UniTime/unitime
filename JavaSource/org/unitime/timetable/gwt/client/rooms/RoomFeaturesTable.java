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
import org.unitime.timetable.gwt.shared.RoomInterface.FeatureInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomFeaturesColumn;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;

/**
 * @author Tomas Muller
 */
public class RoomFeaturesTable extends UniTimeTable<FeatureInterface> {
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	protected static final GwtResources RESOURCES =  GWT.create(GwtResources.class);
	private boolean iGlobal;
	
	private RoomFeaturesColumn iSortBy = null;
	private boolean iAsc = true;
	
	public RoomFeaturesTable(boolean isGlobal) {
		setStyleName("unitime-RoomFeatures");
		iGlobal = isGlobal;
		
		List<UniTimeTableHeader> header = new ArrayList<UniTimeTableHeader>();
		for (RoomFeaturesColumn column: RoomFeaturesColumn.values()) {
			int nrCells = getNbrCells(column);
			for (int idx = 0; idx < nrCells; idx++) {
				UniTimeTableHeader h = new UniTimeTableHeader(getColumnName(column, idx), getColumnAlignment(column, idx));
				header.add(h);
			}
		}
		
		for (final RoomFeaturesColumn column: RoomFeaturesColumn.values()) {
			if (RoomFeaturesComparator.isApplicable(column) && getNbrCells(column) > 0) {
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
	
	protected void doSort(RoomFeaturesColumn column) {
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
			iSortBy = RoomFeaturesColumn.values()[sortBy - 1];
			iAsc = true;
		} else {
			iSortBy = RoomFeaturesColumn.values()[-1 - sortBy];
			iAsc = false;
		}
		sort();
	}
	
	public void sort() {
		if (iSortBy == null) return;
		if (getNbrCells(iSortBy) == 0) iSortBy = RoomFeaturesColumn.NAME;
		UniTimeTableHeader header = getHeader(getCellIndex(iSortBy));
		sort(header, new RoomFeaturesComparator(iSortBy, true), iAsc);
	}
	
	protected int getNbrCells(RoomFeaturesColumn column) {
		switch (column) {
		case DEPARTMENT:
			return iGlobal ? 0 : 1;
		default:
			return 1;
		}
	}
	
	public String getColumnName(RoomFeaturesColumn column, int idx) {
		switch (column) {
		case NAME: return MESSAGES.colName();
		case ABBREVIATION: return MESSAGES.colAbbreviation();
		case TYPE: return MESSAGES.colType();
		case DEPARTMENT: return MESSAGES.colDepartment();
		case ROOMS: return MESSAGES.colRooms();
		default: return column.name();
		}
	}
	
	protected HorizontalAlignmentConstant getColumnAlignment(RoomFeaturesColumn column, int idx) {
		switch (column) {
		default:
			return HasHorizontalAlignment.ALIGN_LEFT;
		}
	}
	
	protected int getCellIndex(RoomFeaturesColumn column) {
		int ret = 0;
		for (RoomFeaturesColumn c: RoomFeaturesColumn.values())
			if (c.ordinal() < column.ordinal()) ret += getNbrCells(c);
		return ret;
	}
	
	protected Widget getCell(final FeatureInterface feature, final RoomFeaturesColumn column, final int idx) {
		switch (column) {
		case NAME:
			return new Label(feature.getLabel() == null ? "" : feature.getLabel(), false);
		case ABBREVIATION:
			return new Label(feature.getAbbreviation() == null ? "" : feature.getAbbreviation(), false);
		case TYPE:
			if (feature.getType() == null)
				return null;
			else {
				Label type = new Label(feature.getType().getAbbreviation(), false);
				type.setTitle(feature.getType().getLabel());
				return type;
			}
		case DEPARTMENT:
			return new DepartmentCell(true, feature.getDepartment());
		case ROOMS:
			if (feature.hasRooms())
				return new RoomsCell(feature);
			else
				return null;
		default:
			return null;
		}
	}
	
	public int addFeature(final FeatureInterface feature) {
		List<Widget> widgets = new ArrayList<Widget>();
		
		for (RoomFeaturesColumn column: RoomFeaturesColumn.values()) {
			int nbrCells = getNbrCells(column);
			for (int idx = 0; idx < nbrCells; idx ++) {
				Widget cell = getCell(feature, column, idx);
				if (cell == null)
					cell = new P();
				widgets.add(cell);
			}
		}
		
		int row = addRow(feature, widgets);
		getRowFormatter().setStyleName(row, "row");
		for (int col = 0; col < getCellCount(row); col++)
			getCellFormatter().setStyleName(row, col, "cell");
		
		return row;
	}
	
	public static class RoomsCell extends P {
		public RoomsCell(FeatureInterface feature) {
			super("rooms");
			if (feature.hasRooms()) {
				for (Iterator<FilterRpcResponse.Entity> i = feature.getRooms().iterator(); i.hasNext(); ) {
					FilterRpcResponse.Entity room = i.next();
					add(new RoomGroupsTable.RoomCell(room, i.hasNext()));
				}
			}
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
	
	public FeatureInterface getFeature(Long featureId) {
		if (featureId == null) return null;
		for (int i = 1; i < getRowCount(); i++) {
			if (featureId.equals(getData(i).getId())) return getData(i);
		}
		return null;
	}
	
	public void scrollTo(Long featureId) {
		if (featureId == null) return;
		for (int i = 1; i < getRowCount(); i++) {
			if (featureId.equals(getData(i).getId())) {
				ToolBox.scrollToElement(getRowFormatter().getElement(i));
				return;
			}
		}
	}
}
