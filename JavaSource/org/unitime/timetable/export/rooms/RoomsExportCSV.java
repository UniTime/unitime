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
import java.util.List;

import org.springframework.stereotype.Service;
import org.unitime.timetable.export.CSVPrinter;
import org.unitime.timetable.export.ExportHelper;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomDetailInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomFlag;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@Service("org.unitime.timetable.export.Exporter:rooms.csv")
public class RoomsExportCSV extends RoomsExporter {

	@Override
	public String reference() { return "rooms.csv"; }

	@Override
	protected void print(ExportHelper helper, List<RoomDetailInterface> rooms, String department, int roomCookieFlags, int deptMode, boolean gridAsText, boolean vertical, String mode) throws IOException {
		if (checkRights(helper))
			helper.getSessionContext().hasPermission(Right.RoomsExportCsv);
		
		Printer printer = new CSVPrinter(helper.getWriter(), false);
		helper.setup(printer.getContentType(), reference(), false);
		hideColumns(printer, rooms, roomCookieFlags);
		print(printer, rooms, deptMode);
	}
	
	@Override
	protected void hideColumn(Printer out, List<RoomDetailInterface> rooms, RoomFlag flag) {
		switch (flag) {
		case SHOW_TYPE: out.hideColumn(1); break;
		case SHOW_CAPACITY: out.hideColumn(2); break;
		case SHOW_EXAM_CAPACITY: out.hideColumn(3); break;
		case SHOW_AREA: out.hideColumn(4); break;
		case SHOW_COORDINATES: out.hideColumn(5); out.hideColumn(6); break;
		case SHOW_IGNORE_DISTANCES: out.hideColumn(7); break;
		case SHOW_IGNORE_ROOM_CHECK: out.hideColumn(8); break;
		case SHOW_PREFERENCE: out.hideColumn(9); break;
		case SHOW_AVAILABILITY: out.hideColumn(10); break;
		case SHOW_DEPARTMENTS: out.hideColumn(11); break;
		case SHOW_CONTROLLING_DEPARTMENT: out.hideColumn(12); break;
		case SHOW_EXAM_TYPES: out.hideColumn(13); break;
		case SHOW_PERIOD_PREFERENCES: out.hideColumn(14); break;
		case SHOW_EVENT_DEPARTMENT: out.hideColumn(15); break;
		case SHOW_EVENT_STATUS: out.hideColumn(16); break;
		case SHOW_EVENT_AVAILABILITY: out.hideColumn(17); break;
		case SHOW_EVENT_MESSAGE: out.hideColumn(18); break;
		case SHOW_BREAK_TIME: out.hideColumn(19); break;
		case SHOW_GROUPS: out.hideColumn(20); break;
		case SHOW_FEATURES: out.hideColumn(21); break;
		}
	}
	
	protected void print(Printer out, List<RoomDetailInterface> rooms, int deptMode) throws IOException {
		out.printHeader(
				/*  0 */ MESSAGES.colName().replace("<br>", "\n"),
				/*  1 */ MESSAGES.colType().replace("<br>", "\n"),
				/*  2 */ MESSAGES.colCapacity().replace("<br>", "\n"),
				/*  3 */ MESSAGES.colExaminationCapacity().replace("<br>", "\n"),
				/*  4 */ MESSAGES.colArea().replace("<br>", "\n").replace("&sup2;", "2"),
				/*  5 */ MESSAGES.colCoordinateX().replace("<br>", "\n"),
				/*  6 */ MESSAGES.colCoordinateY().replace("<br>", "\n"),
				/*  7 */ MESSAGES.colDistances().replace("<br>", "\n"),
				/*  8 */ MESSAGES.colRoomCheck().replace("<br>", "\n"),
				/*  9 */ MESSAGES.colPreference().replace("<br>", "\n"),
				/* 10 */ MESSAGES.colAvailability().replace("<br>", "\n"),
				/* 11 */ MESSAGES.colDepartments().replace("<br>", "\n"),
				/* 12 */ MESSAGES.colControl().replace("<br>", "\n"),
				/* 13 */ MESSAGES.colExamTypes().replace("<br>", "\n"),
				/* 14 */ MESSAGES.colPeriodPreferences().replace("<br>", "\n"),
				/* 15 */ MESSAGES.colEventDepartment().replace("<br>", "\n"),
				/* 16 */ MESSAGES.colEventStatus().replace("<br>", "\n"),
				/* 17 */ MESSAGES.colEventAvailability().replace("<br>", "\n"),
				/* 18 */ MESSAGES.colEventMessage().replace("<br>", "\n"),
				/* 19 */ MESSAGES.colBreakTime().replace("<br>", "\n"),
				/* 20 */ MESSAGES.colGroups().replace("<br>", "\n"),
				/* 21 */ MESSAGES.colFeatures().replace("<br>", "\n")
				);
		
		
		for (RoomDetailInterface room: rooms) {
			out.printLine(
					room.hasDisplayName() ? MESSAGES.label(room.getLabel(), room.getDisplayName()) : room.getLabel(),
					room.getRoomType().getLabel(),
					room.getCapacity() == null ? "0" : room.getCapacity().toString(),
					room.getExamCapacity() == null ? "" : room.getExamCapacity().toString(),
					room.getArea() == null ? "" : room.getArea().toString(),
					room.getX() == null ? "" : room.getX().toString(),
					room.getY() == null ? "" : room.getY().toString(),
					room.isIgnoreRoomCheck() ? MESSAGES.exportFalse() : MESSAGES.exportTrue(),
					room.isIgnoreTooFar() ? MESSAGES.exportFalse() : MESSAGES.exportTrue(),
					pref2string(room.getDepartments(), deptMode, "\n"),
					room.getAvailability(),
					dept2string(room.getDepartments(), deptMode, "\n"),
					dept2string(room.getControlDepartment(), deptMode),
					examTypes2string(room.getExamTypes(), "\n"),
					room.getPeriodPreference(),
					dept2string(room.getEventDepartment(), deptMode),
					room.getEventStatus() != null ? CONSTANTS.eventStatusAbbv()[room.getEventStatus()] : room.getDefaultEventStatus() != null ? CONSTANTS.eventStatusAbbv()[room.getDefaultEventStatus()] : "",
					room.getEventAvailability(),
					room.getEventNote() != null ? room.getEventNote() : room.getDefaultEventNote(),
					room.getBreakTime() != null ? room.getBreakTime().toString() : room.getDefaultBreakTime() != null ? room.getDefaultBreakTime().toString() : "",
					groups2string(room.getGroups(), "\n"),
					features2string(room.getFeatures(), "\n")
					);
			out.flush();
		}
		out.close();
	}
}
