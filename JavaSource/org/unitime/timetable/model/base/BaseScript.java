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
import java.util.HashSet;
import java.util.Set;

import org.unitime.timetable.model.Script;
import org.unitime.timetable.model.ScriptParameter;

public abstract class BaseScript implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iName;
	private String iDescription;
	private String iEngine;
	private String iPermission;
	private String iScript;

	private Set<ScriptParameter> iParameters;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_NAME = "name";
	public static String PROP_DESCRIPTION = "description";
	public static String PROP_ENGINE = "engine";
	public static String PROP_PERMISSION = "permission";
	public static String PROP_SCRIPT = "script";

	public BaseScript() {
		initialize();
	}

	public BaseScript(Long uniqueId) {
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

	public String getEngine() { return iEngine; }
	public void setEngine(String engine) { iEngine = engine; }

	public String getPermission() { return iPermission; }
	public void setPermission(String permission) { iPermission = permission; }

	public String getScript() { return iScript; }
	public void setScript(String script) { iScript = script; }

	public Set<ScriptParameter> getParameters() { return iParameters; }
	public void setParameters(Set<ScriptParameter> parameters) { iParameters = parameters; }
	public void addToparameters(ScriptParameter scriptParameter) {
		if (iParameters == null) iParameters = new HashSet<ScriptParameter>();
		iParameters.add(scriptParameter);
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof Script)) return false;
		if (getUniqueId() == null || ((Script)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((Script)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "Script["+getUniqueId()+" "+getName()+"]";
	}

	public String toDebugString() {
		return "Script[" +
			"\n	Description: " + getDescription() +
			"\n	Engine: " + getEngine() +
			"\n	Name: " + getName() +
			"\n	Permission: " + getPermission() +
			"\n	Script: " + getScript() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
