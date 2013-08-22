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

import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.RoomFeature;
import org.unitime.timetable.model.RoomFeaturePref;

public abstract class BaseRoomFeaturePref extends Preference implements Serializable {
	private static final long serialVersionUID = 1L;

	private RoomFeature iRoomFeature;


	public BaseRoomFeaturePref() {
		initialize();
	}

	public BaseRoomFeaturePref(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public RoomFeature getRoomFeature() { return iRoomFeature; }
	public void setRoomFeature(RoomFeature roomFeature) { iRoomFeature = roomFeature; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof RoomFeaturePref)) return false;
		if (getUniqueId() == null || ((RoomFeaturePref)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((RoomFeaturePref)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "RoomFeaturePref["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "RoomFeaturePref[" +
			"\n	Owner: " + getOwner() +
			"\n	PrefLevel: " + getPrefLevel() +
			"\n	RoomFeature: " + getRoomFeature() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
