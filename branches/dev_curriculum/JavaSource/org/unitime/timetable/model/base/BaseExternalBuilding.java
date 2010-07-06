/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.model.base;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.unitime.timetable.model.ExternalBuilding;
import org.unitime.timetable.model.ExternalRoom;
import org.unitime.timetable.model.Session;

public abstract class BaseExternalBuilding implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iExternalUniqueId;
	private String iAbbreviation;
	private Integer iCoordinateX;
	private Integer iCoordinateY;
	private String iDisplayName;

	private Session iSession;
	private Set<ExternalRoom> iRooms;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_EXTERNAL_UID = "externalUniqueId";
	public static String PROP_ABBREVIATION = "abbreviation";
	public static String PROP_COORDINATE_X = "coordinateX";
	public static String PROP_COORDINATE_Y = "coordinateY";
	public static String PROP_DISPLAY_NAME = "displayName";

	public BaseExternalBuilding() {
		initialize();
	}

	public BaseExternalBuilding(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public String getExternalUniqueId() { return iExternalUniqueId; }
	public void setExternalUniqueId(String externalUniqueId) { iExternalUniqueId = externalUniqueId; }

	public String getAbbreviation() { return iAbbreviation; }
	public void setAbbreviation(String abbreviation) { iAbbreviation = abbreviation; }

	public Integer getCoordinateX() { return iCoordinateX; }
	public void setCoordinateX(Integer coordinateX) { iCoordinateX = coordinateX; }

	public Integer getCoordinateY() { return iCoordinateY; }
	public void setCoordinateY(Integer coordinateY) { iCoordinateY = coordinateY; }

	public String getDisplayName() { return iDisplayName; }
	public void setDisplayName(String displayName) { iDisplayName = displayName; }

	public Session getSession() { return iSession; }
	public void setSession(Session session) { iSession = session; }

	public Set<ExternalRoom> getRooms() { return iRooms; }
	public void setRooms(Set<ExternalRoom> rooms) { iRooms = rooms; }
	public void addTorooms(ExternalRoom externalRoom) {
		if (iRooms == null) iRooms = new HashSet();
		iRooms.add(externalRoom);
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof ExternalBuilding)) return false;
		if (getUniqueId() == null || ((ExternalBuilding)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((ExternalBuilding)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "ExternalBuilding["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "ExternalBuilding[" +
			"\n	Abbreviation: " + getAbbreviation() +
			"\n	CoordinateX: " + getCoordinateX() +
			"\n	CoordinateY: " + getCoordinateY() +
			"\n	DisplayName: " + getDisplayName() +
			"\n	ExternalUniqueId: " + getExternalUniqueId() +
			"\n	Session: " + getSession() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
