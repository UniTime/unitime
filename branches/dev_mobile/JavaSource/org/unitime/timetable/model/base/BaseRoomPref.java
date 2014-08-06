/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2010 - 2014, UniTime LLC, and individual contributors
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

import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.RoomPref;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseRoomPref extends Preference implements Serializable {
	private static final long serialVersionUID = 1L;

	private Location iRoom;


	public BaseRoomPref() {
		initialize();
	}

	public BaseRoomPref(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Location getRoom() { return iRoom; }
	public void setRoom(Location room) { iRoom = room; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof RoomPref)) return false;
		if (getUniqueId() == null || ((RoomPref)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((RoomPref)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "RoomPref["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "RoomPref[" +
			"\n	Owner: " + getOwner() +
			"\n	PrefLevel: " + getPrefLevel() +
			"\n	Room: " + getRoom() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
