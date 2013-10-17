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

import org.unitime.timetable.model.SolverParameter;
import org.unitime.timetable.model.SolverPredefinedSetting;

/**
 * @author Tomas Muller
 */
public abstract class BaseSolverPredefinedSetting implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iName;
	private String iDescription;
	private Integer iAppearance;

	private Set<SolverParameter> iParameters;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_NAME = "name";
	public static String PROP_DESCRIPTION = "description";
	public static String PROP_APPEARANCE = "appearance";

	public BaseSolverPredefinedSetting() {
		initialize();
	}

	public BaseSolverPredefinedSetting(Long uniqueId) {
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

	public Integer getAppearance() { return iAppearance; }
	public void setAppearance(Integer appearance) { iAppearance = appearance; }

	public Set<SolverParameter> getParameters() { return iParameters; }
	public void setParameters(Set<SolverParameter> parameters) { iParameters = parameters; }
	public void addToparameters(SolverParameter solverParameter) {
		if (iParameters == null) iParameters = new HashSet<SolverParameter>();
		iParameters.add(solverParameter);
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof SolverPredefinedSetting)) return false;
		if (getUniqueId() == null || ((SolverPredefinedSetting)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((SolverPredefinedSetting)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "SolverPredefinedSetting["+getUniqueId()+" "+getName()+"]";
	}

	public String toDebugString() {
		return "SolverPredefinedSetting[" +
			"\n	Appearance: " + getAppearance() +
			"\n	Description: " + getDescription() +
			"\n	Name: " + getName() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
