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

import org.unitime.timetable.model.SavedHQL;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseSavedHQL implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iName;
	private String iDescription;
	private String iQuery;
	private Integer iType;


	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_NAME = "name";
	public static String PROP_DESCRIPTION = "description";
	public static String PROP_QUERY = "query";
	public static String PROP_TYPE = "type";

	public BaseSavedHQL() {
		initialize();
	}

	public BaseSavedHQL(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public String getName() { return iName; }
	public void setName(String name) { iName = name; }

	public String getDescription() { return iDescription; }
	public void setDescription(String description) { iDescription = description; }

	public String getQuery() { return iQuery; }
	public void setQuery(String query) { iQuery = query; }

	public Integer getType() { return iType; }
	public void setType(Integer type) { iType = type; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof SavedHQL)) return false;
		if (getUniqueId() == null || ((SavedHQL)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((SavedHQL)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "SavedHQL["+getUniqueId()+" "+getName()+"]";
	}

	public String toDebugString() {
		return "SavedHQL[" +
			"\n	Description: " + getDescription() +
			"\n	Name: " + getName() +
			"\n	Query: " + getQuery() +
			"\n	Type: " + getType() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
