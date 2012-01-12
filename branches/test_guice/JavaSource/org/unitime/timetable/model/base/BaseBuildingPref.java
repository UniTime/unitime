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

import org.unitime.timetable.model.Building;
import org.unitime.timetable.model.BuildingPref;
import org.unitime.timetable.model.Preference;

public abstract class BaseBuildingPref extends Preference implements Serializable {
	private static final long serialVersionUID = 1L;

	private Integer iDistanceFrom;

	private Building iBuilding;

	public static String PROP_DISTANCE_FROM = "distanceFrom";

	public BaseBuildingPref() {
		initialize();
	}

	public BaseBuildingPref(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Integer getDistanceFrom() { return iDistanceFrom; }
	public void setDistanceFrom(Integer distanceFrom) { iDistanceFrom = distanceFrom; }

	public Building getBuilding() { return iBuilding; }
	public void setBuilding(Building building) { iBuilding = building; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof BuildingPref)) return false;
		if (getUniqueId() == null || ((BuildingPref)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((BuildingPref)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "BuildingPref["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "BuildingPref[" +
			"\n	Building: " + getBuilding() +
			"\n	DistanceFrom: " + getDistanceFrom() +
			"\n	Owner: " + getOwner() +
			"\n	PrefLevel: " + getPrefLevel() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
