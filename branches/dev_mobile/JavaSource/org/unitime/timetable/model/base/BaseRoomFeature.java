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
import java.util.HashSet;
import java.util.Set;

import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.RoomFeature;
import org.unitime.timetable.model.RoomFeatureType;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseRoomFeature implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iLabel;
	private String iAbbv;

	private RoomFeatureType iFeatureType;
	private Set<Location> iRooms;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_LABEL = "label";
	public static String PROP_ABBV = "abbv";

	public BaseRoomFeature() {
		initialize();
	}

	public BaseRoomFeature(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public String getLabel() { return iLabel; }
	public void setLabel(String label) { iLabel = label; }

	public String getAbbv() { return iAbbv; }
	public void setAbbv(String abbv) { iAbbv = abbv; }

	public RoomFeatureType getFeatureType() { return iFeatureType; }
	public void setFeatureType(RoomFeatureType featureType) { iFeatureType = featureType; }

	public Set<Location> getRooms() { return iRooms; }
	public void setRooms(Set<Location> rooms) { iRooms = rooms; }
	public void addTorooms(Location location) {
		if (iRooms == null) iRooms = new HashSet<Location>();
		iRooms.add(location);
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof RoomFeature)) return false;
		if (getUniqueId() == null || ((RoomFeature)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((RoomFeature)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "RoomFeature["+getUniqueId()+" "+getLabel()+"]";
	}

	public String toDebugString() {
		return "RoomFeature[" +
			"\n	Abbv: " + getAbbv() +
			"\n	FeatureType: " + getFeatureType() +
			"\n	Label: " + getLabel() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
