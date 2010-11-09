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

import org.unitime.timetable.model.FreeTime;
import org.unitime.timetable.model.Session;

public abstract class BaseFreeTime implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iName;
	private Integer iDayCode;
	private Integer iStartSlot;
	private Integer iLength;
	private Integer iCategory;

	private Session iSession;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_NAME = "name";
	public static String PROP_DAY_CODE = "dayCode";
	public static String PROP_START_SLOT = "startSlot";
	public static String PROP_LENGTH = "length";
	public static String PROP_CATEGORY = "category";

	public BaseFreeTime() {
		initialize();
	}

	public BaseFreeTime(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public String getName() { return iName; }
	public void setName(String name) { iName = name; }

	public Integer getDayCode() { return iDayCode; }
	public void setDayCode(Integer dayCode) { iDayCode = dayCode; }

	public Integer getStartSlot() { return iStartSlot; }
	public void setStartSlot(Integer startSlot) { iStartSlot = startSlot; }

	public Integer getLength() { return iLength; }
	public void setLength(Integer length) { iLength = length; }

	public Integer getCategory() { return iCategory; }
	public void setCategory(Integer category) { iCategory = category; }

	public Session getSession() { return iSession; }
	public void setSession(Session session) { iSession = session; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof FreeTime)) return false;
		if (getUniqueId() == null || ((FreeTime)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((FreeTime)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "FreeTime["+getUniqueId()+" "+getName()+"]";
	}

	public String toDebugString() {
		return "FreeTime[" +
			"\n	Category: " + getCategory() +
			"\n	DayCode: " + getDayCode() +
			"\n	Length: " + getLength() +
			"\n	Name: " + getName() +
			"\n	Session: " + getSession() +
			"\n	StartSlot: " + getStartSlot() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
