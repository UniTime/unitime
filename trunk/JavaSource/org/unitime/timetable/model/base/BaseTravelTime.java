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

import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.TravelTime;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseTravelTime implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Long iLocation1Id;
	private Long iLocation2Id;
	private Integer iDistance;

	private Session iSession;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_LOC1_ID = "location1Id";
	public static String PROP_LOC2_ID = "location2Id";
	public static String PROP_DISTANCE = "distance";

	public BaseTravelTime() {
		initialize();
	}

	public BaseTravelTime(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public Long getLocation1Id() { return iLocation1Id; }
	public void setLocation1Id(Long location1Id) { iLocation1Id = location1Id; }

	public Long getLocation2Id() { return iLocation2Id; }
	public void setLocation2Id(Long location2Id) { iLocation2Id = location2Id; }

	public Integer getDistance() { return iDistance; }
	public void setDistance(Integer distance) { iDistance = distance; }

	public Session getSession() { return iSession; }
	public void setSession(Session session) { iSession = session; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof TravelTime)) return false;
		if (getUniqueId() == null || ((TravelTime)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((TravelTime)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "TravelTime["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "TravelTime[" +
			"\n	Distance: " + getDistance() +
			"\n	Location1Id: " + getLocation1Id() +
			"\n	Location2Id: " + getLocation2Id() +
			"\n	Session: " + getSession() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
