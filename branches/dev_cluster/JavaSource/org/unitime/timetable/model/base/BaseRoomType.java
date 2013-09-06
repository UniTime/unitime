/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.model.base;

import java.io.Serializable;

import org.unitime.timetable.model.RefTableEntry;
import org.unitime.timetable.model.RoomType;

public abstract class BaseRoomType extends RefTableEntry implements Serializable {
	private static final long serialVersionUID = 1L;

	private Integer iOrd;
	private Boolean iRoom;


	public static String PROP_ORD = "ord";
	public static String PROP_IS_ROOM = "room";

	public BaseRoomType() {
		initialize();
	}

	public BaseRoomType(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Integer getOrd() { return iOrd; }
	public void setOrd(Integer ord) { iOrd = ord; }

	public Boolean isRoom() { return iRoom; }
	public Boolean getRoom() { return iRoom; }
	public void setRoom(Boolean room) { iRoom = room; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof RoomType)) return false;
		if (getUniqueId() == null || ((RoomType)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((RoomType)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "RoomType["+getUniqueId()+" "+getLabel()+"]";
	}

	public String toDebugString() {
		return "RoomType[" +
			"\n	Label: " + getLabel() +
			"\n	Ord: " + getOrd() +
			"\n	Reference: " + getReference() +
			"\n	Room: " + getRoom() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
