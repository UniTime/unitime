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

import java.util.Comparator;

import org.unitime.timetable.gwt.shared.RoomInterface.RoomDetailInterface;

/**
 * @author Tomas Muller
 */
public class RoomsComparator implements Comparator<RoomDetailInterface> {
	public static enum Column {
		NAME, TYPE, CAPACITY, EXAM_CAPACITY, DISTANCE, ROOM_CHECK, CONTROL, EVENT_DEPT, EVENT_STATUS, EVENT_MESSAGE, BREAK_TIME,
	}
	private Column iColumn;
	private boolean iAsc;
	
	public int compareById(RoomDetailInterface r1, RoomDetailInterface r2) {
		return compare(r1.getUniqueId(), r2.getUniqueId());
	}
	
	public int compareByName(RoomDetailInterface r1, RoomDetailInterface r2) {
		return compare(r1.getLabel(), r2.getLabel());
	}
	
	public int compareByType(RoomDetailInterface r1, RoomDetailInterface r2) {
		return compare(r1.getRoomType().getLabel(), r2.getRoomType().getLabel());
	}
	
	public int compareByCapacity(RoomDetailInterface r1, RoomDetailInterface r2) {
		return -compare(r1.getCapacity(), r2.getCapacity());
	}
	
	public int compareByExamCapacity(RoomDetailInterface r1, RoomDetailInterface r2) {
		return -compare(r1.getExamCapacity() == null ? r1.getCapacity() : r1.getExamCapacity(),
				r2.getExamCapacity() == null ? r2.getCapacity() : r2.getExamCapacity());
	}
	
	public int compareByDistance(RoomDetailInterface r1, RoomDetailInterface r2) {
		return -compare(r1.isIgnoreTooFar(), r2.isIgnoreTooFar());
	}
	
	public int compareByRoomCheck(RoomDetailInterface r1, RoomDetailInterface r2) {
		return -compare(r1.isIgnoreRoomCheck(), r2.isIgnoreRoomCheck());
	}
	
	public int compareByControl(RoomDetailInterface r1, RoomDetailInterface r2) {
		return compare(r1.getControlDepartment() == null ? null : r1.getControlDepartment().getAbbreviationOrCode(),
				r2.getControlDepartment() == null ? null : r2.getControlDepartment().getAbbreviationOrCode());
	}
	
	public int compareByEventDepartment(RoomDetailInterface r1, RoomDetailInterface r2) {
		return compare(r1.getEventDepartment() == null ? null : r1.getEventDepartment().getAbbreviationOrCode(),
				r2.getEventDepartment() == null ? null : r2.getEventDepartment().getAbbreviationOrCode());
	}
	
	public int compareByEventStatus(RoomDetailInterface r1, RoomDetailInterface r2) {
		return compare(r1.getEventStatus(), r2.getEventStatus());
	}
	
	public int compareByEventMessage(RoomDetailInterface r1, RoomDetailInterface r2) {
		return compare(r1.getEventNote(), r2.getEventNote());
	}
	
	public int compareByBreakTime(RoomDetailInterface r1, RoomDetailInterface r2) {
		return compare(r1.getBreakTime(), r2.getBreakTime());
	}
	
	protected int compareByColumn(RoomDetailInterface r1, RoomDetailInterface r2) {
		switch (iColumn) {
		case NAME: return compareByName(r1, r2);
		case TYPE: return compareByType(r1, r2);
		case CAPACITY: return compareByCapacity(r1, r2);
		case EXAM_CAPACITY: return compareByExamCapacity(r1, r2);
		case DISTANCE: return compareByDistance(r1, r2);
		case ROOM_CHECK: return compareByRoomCheck(r1, r2);
		case CONTROL: return compareByControl(r1, r2);
		case EVENT_DEPT: return compareByEventDepartment(r1, r2);
		case EVENT_STATUS: return compareByEventStatus(r1, r2);
		case EVENT_MESSAGE: return compareByEventMessage(r1, r2);
		case BREAK_TIME: return compareByBreakTime(r1, r2);
		default: return compareByName(r1, r2);
		}
	}
	
	public RoomsComparator(Column column, boolean asc) {
		iColumn = column;
		iAsc = asc;
	}

	@Override
	public int compare(RoomDetailInterface r1, RoomDetailInterface r2) {
		int cmp = compareByColumn(r1, r2);
		if (cmp == 0) cmp = compareByName(r1, r2);
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
		return (n1 == null ? n2 == null ? 0 : -1 : n2 == null ? -1 : Double.compare(n1.doubleValue(), n2.doubleValue())); 
	}
	
	protected int compare(Boolean b1, Boolean b2) {
		return (b1 == null ? b2 == null ? 0 : -1 : b2 == null ? -1 : (b1.booleanValue() == b2.booleanValue()) ? 0 : (b1.booleanValue() ? 1 : -1));
	}

}
