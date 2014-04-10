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

import org.unitime.timetable.model.SolverInfoDef;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseSolverInfoDef implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iName;
	private String iDescription;
	private String iImplementation;


	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_NAME = "name";
	public static String PROP_DESCRIPTION = "description";
	public static String PROP_IMPLEMENTATION = "implementation";

	public BaseSolverInfoDef() {
		initialize();
	}

	public BaseSolverInfoDef(Long uniqueId) {
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

	public String getImplementation() { return iImplementation; }
	public void setImplementation(String implementation) { iImplementation = implementation; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof SolverInfoDef)) return false;
		if (getUniqueId() == null || ((SolverInfoDef)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((SolverInfoDef)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "SolverInfoDef["+getUniqueId()+" "+getName()+"]";
	}

	public String toDebugString() {
		return "SolverInfoDef[" +
			"\n	Description: " + getDescription() +
			"\n	Implementation: " + getImplementation() +
			"\n	Name: " + getName() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
