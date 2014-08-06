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

import org.unitime.timetable.model.TimePatternDays;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseTimePatternDays implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Integer iDayCode;


	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_DAY_CODE = "dayCode";

	public BaseTimePatternDays() {
		initialize();
	}

	public BaseTimePatternDays(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public Integer getDayCode() { return iDayCode; }
	public void setDayCode(Integer dayCode) { iDayCode = dayCode; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof TimePatternDays)) return false;
		if (getUniqueId() == null || ((TimePatternDays)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((TimePatternDays)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "TimePatternDays["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "TimePatternDays[" +
			"\n	DayCode: " + getDayCode() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
