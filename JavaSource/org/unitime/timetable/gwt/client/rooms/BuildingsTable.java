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
import java.util.Comparator;
import java.util.List;

import org.unitime.timetable.gwt.client.admin.AdminCookie;
import org.unitime.timetable.gwt.client.rooms.RoomsTable.SortOperation;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader.Operation;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.RoomInterface.BuildingInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.BuildingsColumn;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class BuildingsTable extends UniTimeTable<BuildingInterface>{
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private BuildingsColumn iSortBy = null;
	private boolean iAsc = true;

	public BuildingsTable() {
		List<UniTimeTableHeader> header = new ArrayList<UniTimeTableHeader>();
		for (final BuildingsColumn col: BuildingsColumn.values()) {
			final UniTimeTableHeader h = new UniTimeTableHeader(getColumnName(col));
			if (BuildingComparator.isApplicable(col)) {
				Operation op = new SortOperation() {
					@Override
					public void execute() { doSort(col); }
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
			header.add(h);
		}
		addRow(null, header);
		
		for (int i = 0; i < getCellCount(0); i++)
			getCellFormatter().setStyleName(0, i, "unitime-ClickableTableHeader");
		
		setSortBy(AdminCookie.getInstance().getSortBuildingsBy());

	}
	
	public String getColumnName(BuildingsColumn column) {
		switch (column) {
		case ABBREVIATION: return MESSAGES.colAbbreviation();
		case EXTERNAL_ID: return MESSAGES.colExternalId();
		case NAME: return MESSAGES.colName();
		case COORDINATES: return MESSAGES.colCoordinates();
		default: return column.name();
		}
	}
	
	public Widget getColumnWidget(BuildingsColumn column, BuildingInterface building) {
		switch (column) {
		case ABBREVIATION:
			return new Label(building.getAbbreviation() == null ? "" : building.getAbbreviation());
		case EXTERNAL_ID:
			return new Label(building.getExternalId() == null ? "" : building.getExternalId());
		case NAME:
			return new Label(building.getName() == null ? "" : building.getName());
		case COORDINATES:
			if (!building.hasCoordinates()) return new Label();
			return new Label(MESSAGES.coordinates(building.getX(), building.getY()));
		default:
			return null;
		}
	}
	
	protected void addRow(BuildingInterface building) {
		List<Widget> line = new ArrayList<Widget>();
		for (BuildingsColumn col: BuildingsColumn.values())
			line.add(getColumnWidget(col, building));
		addRow(building, line);
	}
	
	public void setData(List<BuildingInterface> buildings) {
		clearTable(1);
		if (buildings != null)
			for (BuildingInterface building: buildings)
				addRow(building);
		sort();
	}
	
	protected void doSort(BuildingsColumn column) {
		if (column == iSortBy) {
			iAsc = !iAsc;
		} else {
			iSortBy = column;
			iAsc = true;
		}
		AdminCookie.getInstance().setSortBuildingsBy(getSortBy());
		sort();
	}
	
	public boolean hasSortBy() { return iSortBy != null; }
	public int getSortBy() { return iSortBy == null ? 0 : iAsc ? 1 + iSortBy.ordinal() : -1 - iSortBy.ordinal(); }
	public void setSortBy(int sortBy) {
		if (sortBy == 0) {
			iSortBy = null;
			iAsc = true;
		} else if (sortBy > 0) {
			iSortBy = BuildingsColumn.values()[sortBy - 1];
			iAsc = true;
		} else {
			iSortBy = BuildingsColumn.values()[-1 - sortBy];
			iAsc = false;
		}
		sort();
	}
	
	public void sort() {
		if (iSortBy == null) return;
		UniTimeTableHeader header = getHeader(iSortBy.ordinal());
		sort(header, new BuildingComparator(iSortBy, true), iAsc);
	}
	
	public static class BuildingComparator implements Comparator<BuildingInterface>{
		private BuildingsColumn iColumn;
		private boolean iAsc;
		
		public BuildingComparator(BuildingsColumn column, boolean asc) {
			iColumn = column;
			iAsc = asc;
		}
		
		public int compareById(BuildingInterface r1, BuildingInterface r2) {
			return compare(r1.getId(), r2.getId());
		}
		
		public int compareByName(BuildingInterface r1, BuildingInterface r2) {
			return compare(r1.getName(), r2.getName());
		}

		public int compareByAbbreviation(BuildingInterface r1, BuildingInterface r2) {
			return compare(r1.getAbbreviation(), r2.getAbbreviation());
		}
		
		public int compareByExternalId(BuildingInterface r1, BuildingInterface r2) {
			return compare(r1.getExternalId(), r2.getExternalId());
		}
		
		protected int compareByColumn(BuildingInterface r1, BuildingInterface r2) {
			switch (iColumn) {
			case NAME: return compareByName(r1, r2);
			case ABBREVIATION: return compareByAbbreviation(r1, r2);
			case EXTERNAL_ID: return compareByExternalId(r1, r2);
			default: return compareByAbbreviation(r1, r2);
			}
		}
		
		public static boolean isApplicable(BuildingsColumn column) {
			switch (column) {
			case ABBREVIATION:
			case NAME:
			case EXTERNAL_ID:
				return true;
			default:
				return false;
			}
		}
		
		@Override
		public int compare(BuildingInterface r1, BuildingInterface r2) {
			int cmp = compareByColumn(r1, r2);
			if (cmp == 0) cmp = compareByAbbreviation(r1, r2);
			if (cmp == 0) cmp = compareById(r1, r2);
			return (iAsc ? cmp : -cmp);
		}
		
		protected int compare(String s1, String s2) {
			if (s1 == null || s1.isEmpty()) {
				return (s2 == null || s2.isEmpty() ? 0 : 1);
			} else {
				return (s2 == null || s2.isEmpty() ? -1 : s1.compareToIgnoreCase(s2));
			}
		}
		
		protected int compare(Number n1, Number n2) {
			return (n1 == null ? n2 == null ? 0 : -1 : n2 == null ? 1 : Double.compare(n1.doubleValue(), n2.doubleValue())); 
		}
	}

}
