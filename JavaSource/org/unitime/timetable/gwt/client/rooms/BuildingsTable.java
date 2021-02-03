package org.unitime.timetable.gwt.client.rooms;

import java.util.ArrayList;
import java.util.List;

import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.RoomInterface.BuildingInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.BuildingsColumn;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class BuildingsTable extends UniTimeTable<BuildingInterface>{
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	
	public BuildingsTable() {
		List<UniTimeTableHeader> header = new ArrayList<UniTimeTableHeader>();
		for (BuildingsColumn col: BuildingsColumn.values())
			header.add(new UniTimeTableHeader(getColumnName(col)));
		addRow(null, header);
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
	}

}
