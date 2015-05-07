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
package org.unitime.timetable.export.rooms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.unitime.timetable.export.CSVPrinter;
import org.unitime.timetable.export.ExportHelper;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomDetailInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomsColumn;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@Service("org.unitime.timetable.export.Exporter:rooms.csv")
public class RoomsExportCSV extends RoomsExporter {

	@Override
	public String reference() { return "rooms.csv"; }
	
	@Override
	protected void print(ExportHelper helper, List<RoomDetailInterface> rooms, ExportContext context) throws IOException {
		if (checkRights(helper))
			helper.getSessionContext().hasPermission(Right.RoomsExportCsv);
		
		List<Integer> columns = new ArrayList<Integer>();
		for (int i = 0; i < getNbrColumns(context); i ++)
			if (isColumnVisible(i, context)) columns.add(i);
		
		Printer printer = new CSVPrinter(helper.getWriter(), false);
		helper.setup(printer.getContentType(), reference(), false);
		
		String[] header = new String[columns.size()];
		for (int i = 0; i < columns.size(); i++)
			header[i] = getColumnName(columns.get(i), context);
		printer.printHeader(header);
		printer.flush();
		
		for (RoomDetailInterface room: rooms) {
			String[] row = new String[columns.size()];
			for (int i = 0; i < columns.size(); i++)
				row[i] = getCell(room, columns.get(i), context);
			printer.printLine(row);
			printer.flush();
		}
		printer.close();
	}
	
	protected int getNbrColumns(ExportContext context) {
		return 22 + context.getRoomFeatureTypes().size();
	}
	
	protected String getColumnName(int column, ExportContext context) {
		switch (column) {
		case  0: return MESSAGES.colName().replace("<br>", "\n");
		case  1: return MESSAGES.colType().replace("<br>", "\n");
		case  2: return MESSAGES.colCapacity().replace("<br>", "\n");
		case  3: return MESSAGES.colExaminationCapacity().replace("<br>", "\n");
		case  4: return MESSAGES.colArea(CONSTANTS.roomAreaUnitsShortPlainText()).replace("<br>", "\n");
		case  5: return MESSAGES.colCoordinateX().replace("<br>", "\n");
		case  6: return MESSAGES.colCoordinateY().replace("<br>", "\n");
		case  7: return MESSAGES.colDistances().replace("<br>", "\n");
		case  8: return MESSAGES.colRoomCheck().replace("<br>", "\n");
		case  9: return MESSAGES.colPreference().replace("<br>", "\n");
		case 10: return MESSAGES.colAvailability().replace("<br>", "\n");
		case 11: return MESSAGES.colDepartments().replace("<br>", "\n");
		case 12: return MESSAGES.colControl().replace("<br>", "\n");
		case 13: return MESSAGES.colExamTypes().replace("<br>", "\n");
		case 14: return MESSAGES.colPeriodPreferences().replace("<br>", "\n");
		case 15: return MESSAGES.colEventDepartment().replace("<br>", "\n");
		case 16: return MESSAGES.colEventStatus().replace("<br>", "\n");
		case 17: return MESSAGES.colEventAvailability().replace("<br>", "\n");
		case 18: return MESSAGES.colEventMessage().replace("<br>", "\n");
		case 19: return MESSAGES.colBreakTime().replace("<br>", "\n");
		case 20: return MESSAGES.colGroups().replace("<br>", "\n");
		case 21: return MESSAGES.colFeatures().replace("<br>", "\n");
		default: return context.getRoomFeatureTypes().get(column - 22).getAbbreviation();
		}
	}
	
	protected boolean isColumnVisible(int column, ExportContext context) {
		int flags = context.getRoomCookieFlags();
		switch(column) {
		case 1: return RoomsColumn.TYPE.in(flags);
		case 2: return RoomsColumn.CAPACITY.in(flags);
		case 3: return RoomsColumn.EXAM_CAPACITY.in(flags);
		case 4: return RoomsColumn.AREA.in(flags);
		case 5: return RoomsColumn.COORDINATES.in(flags);
		case 6: return RoomsColumn.COORDINATES.in(flags);
		case 7: return RoomsColumn.DISTANCE_CHECK.in(flags);
		case 8: return RoomsColumn.ROOM_CHECK.in(flags);
		case 9: return RoomsColumn.PREFERENCE.in(flags);
		case 10: return RoomsColumn.AVAILABILITY.in(flags);
		case 11: return RoomsColumn.DEPARTMENTS.in(flags);
		case 12: return RoomsColumn.CONTROL_DEPT.in(flags);
		case 13: return RoomsColumn.EXAM_TYPES.in(flags);
		case 14: return RoomsColumn.PERIOD_PREF.in(flags);
		case 15: return RoomsColumn.EVENT_DEPARTMENT.in(flags);
		case 16: return RoomsColumn.EVENT_STATUS.in(flags);
		case 17: return RoomsColumn.EVENT_AVAILABILITY.in(flags);
		case 18: return RoomsColumn.EVENT_MESSAGE.in(flags);
		case 19: return RoomsColumn.BREAK_TIME.in(flags);
		case 20: return RoomsColumn.GROUPS.in(flags);
		case 21: return RoomsColumn.FEATURES.in(flags);
		default:
			if (column > 21) {
				int flag = (1 << (column - 22 + RoomsColumn.values().length));
				return (flags & flag) == 0;
			} else {
				return true;
			}
		}
	}
	
	protected String getCell(RoomDetailInterface room, int column, ExportContext context) {
		switch (column) {
		case  0:
			return room.hasDisplayName() ? MESSAGES.label(room.getLabel(), room.getDisplayName()) : room.getLabel();
		case 1:
			return room.getRoomType().getLabel();
		case 2:
			return room.getCapacity() == null ? "0" : room.getCapacity().toString();
		case 3:
			return room.getExamCapacity() == null ? "" : room.getExamCapacity().toString();
		case 4:
			return room.getArea() == null ? "" : room.getArea().toString();
		case 5:
			return room.getX() == null ? "" : room.getX().toString();
		case 6:
			return room.getY() == null ? "" : room.getY().toString();
		case 7:
			return room.isIgnoreRoomCheck() ? MESSAGES.exportFalse() : MESSAGES.exportTrue();
		case 8:
			return room.isIgnoreTooFar() ? MESSAGES.exportFalse() : MESSAGES.exportTrue();
		case 9:
			return context.pref2string(room.getDepartments());
		case 10:
			return room.getAvailability();
		case 11:
			return context.dept2string(room.getDepartments());
		case 12:
			return context.dept2string(room.getControlDepartment());
		case 13:
			return context.examTypes2string(room.getExamTypes());
		case 14:
			return room.getPeriodPreference();
		case 15:
			return context.dept2string(room.getEventDepartment());
		case 16:
			return room.getEventStatus() != null ? CONSTANTS.eventStatusAbbv()[room.getEventStatus()] : room.getDefaultEventStatus() != null ? CONSTANTS.eventStatusAbbv()[room.getDefaultEventStatus()] : "";
		case 17:
			return room.getEventAvailability();
		case 18:
			return room.getEventNote() != null ? room.getEventNote() : room.getDefaultEventNote();
		case 19:
			return room.getBreakTime() != null ? room.getBreakTime().toString() : room.getDefaultBreakTime() != null ? room.getDefaultBreakTime().toString() : "";
		case 20:
			return context.groups2string(room.getGroups());
		case 21:
			return context.features2string(room.getFeatures(), null);
		default:
			return context.features2string(room.getFeatures(), context.getRoomFeatureTypes().get(column - 22));
		}
	}
}
