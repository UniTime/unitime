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

import org.dom4j.Document;
import org.unitime.timetable.model.SolverInfo;
import org.unitime.timetable.model.SolverInfoDef;

public abstract class BaseSolverInfo implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Document iValue;
	private String iOpt;

	private SolverInfoDef iDefinition;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_VALUE = "value";
	public static String PROP_OPT = "opt";

	public BaseSolverInfo() {
		initialize();
	}

	public BaseSolverInfo(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public Document getValue() { return iValue; }
	public void setValue(Document value) { iValue = value; }

	public String getOpt() { return iOpt; }
	public void setOpt(String opt) { iOpt = opt; }

	public SolverInfoDef getDefinition() { return iDefinition; }
	public void setDefinition(SolverInfoDef definition) { iDefinition = definition; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof SolverInfo)) return false;
		if (getUniqueId() == null || ((SolverInfo)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((SolverInfo)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "SolverInfo["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "SolverInfo[" +
			"\n	Definition: " + getDefinition() +
			"\n	Opt: " + getOpt() +
			"\n	UniqueId: " + getUniqueId() +
			"\n	Value: " + getValue() +
			"]";
	}
}
