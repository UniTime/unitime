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

import org.unitime.timetable.model.SolverParameter;
import org.unitime.timetable.model.SolverParameterDef;

public abstract class BaseSolverParameter implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iValue;

	private SolverParameterDef iDefinition;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_VALUE = "value";

	public BaseSolverParameter() {
		initialize();
	}

	public BaseSolverParameter(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public String getValue() { return iValue; }
	public void setValue(String value) { iValue = value; }

	public SolverParameterDef getDefinition() { return iDefinition; }
	public void setDefinition(SolverParameterDef definition) { iDefinition = definition; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof SolverParameter)) return false;
		if (getUniqueId() == null || ((SolverParameter)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((SolverParameter)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "SolverParameter["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "SolverParameter[" +
			"\n	Definition: " + getDefinition() +
			"\n	UniqueId: " + getUniqueId() +
			"\n	Value: " + getValue() +
			"]";
	}
}
