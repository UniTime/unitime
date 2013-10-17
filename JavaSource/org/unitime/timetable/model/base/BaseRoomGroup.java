/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2010 - 2013, UniTime LLC, and individual contributors
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

import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.RoomGroup;
import org.unitime.timetable.model.Session;

/**
 * @author Tomas Muller
 */
public abstract class BaseRoomGroup implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iName;
	private String iAbbv;
	private String iDescription;
	private Boolean iGlobal;
	private Boolean iDefaultGroup;

	private Department iDepartment;
	private Session iSession;
	private Set<Location> iRooms;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_NAME = "name";
	public static String PROP_ABBV = "abbv";
	public static String PROP_DESCRIPTION = "description";
	public static String PROP_GLOBAL = "global";
	public static String PROP_DEFAULT_GROUP = "defaultGroup";

	public BaseRoomGroup() {
		initialize();
	}

	public BaseRoomGroup(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public String getName() { return iName; }
	public void setName(String name) { iName = name; }

	public String getAbbv() { return iAbbv; }
	public void setAbbv(String abbv) { iAbbv = abbv; }

	public String getDescription() { return iDescription; }
	public void setDescription(String description) { iDescription = description; }

	public Boolean isGlobal() { return iGlobal; }
	public Boolean getGlobal() { return iGlobal; }
	public void setGlobal(Boolean global) { iGlobal = global; }

	public Boolean isDefaultGroup() { return iDefaultGroup; }
	public Boolean getDefaultGroup() { return iDefaultGroup; }
	public void setDefaultGroup(Boolean defaultGroup) { iDefaultGroup = defaultGroup; }

	public Department getDepartment() { return iDepartment; }
	public void setDepartment(Department department) { iDepartment = department; }

	public Session getSession() { return iSession; }
	public void setSession(Session session) { iSession = session; }

	public Set<Location> getRooms() { return iRooms; }
	public void setRooms(Set<Location> rooms) { iRooms = rooms; }
	public void addTorooms(Location location) {
		if (iRooms == null) iRooms = new HashSet<Location>();
		iRooms.add(location);
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof RoomGroup)) return false;
		if (getUniqueId() == null || ((RoomGroup)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((RoomGroup)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "RoomGroup["+getUniqueId()+" "+getName()+"]";
	}

	public String toDebugString() {
		return "RoomGroup[" +
			"\n	Abbv: " + getAbbv() +
			"\n	DefaultGroup: " + getDefaultGroup() +
			"\n	Department: " + getDepartment() +
			"\n	Description: " + getDescription() +
			"\n	Global: " + getGlobal() +
			"\n	Name: " + getName() +
			"\n	Session: " + getSession() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
