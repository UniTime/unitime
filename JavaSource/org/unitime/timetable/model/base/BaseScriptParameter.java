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

import org.unitime.timetable.model.Script;
import org.unitime.timetable.model.ScriptParameter;

/**
 * @author Tomas Muller
 */
public abstract class BaseScriptParameter implements Serializable {
	private static final long serialVersionUID = 1L;

	private Script iScript;
	private String iName;
	private String iLabel;
	private String iType;
	private String iDefaultValue;


	public static String PROP_LABEL = "label";
	public static String PROP_TYPE = "type";
	public static String PROP_DEFAULT_VALUE = "defaultValue";

	public BaseScriptParameter() {
		initialize();
	}

	protected void initialize() {}

	public Script getScript() { return iScript; }
	public void setScript(Script script) { iScript = script; }

	public String getName() { return iName; }
	public void setName(String name) { iName = name; }

	public String getLabel() { return iLabel; }
	public void setLabel(String label) { iLabel = label; }

	public String getType() { return iType; }
	public void setType(String type) { iType = type; }

	public String getDefaultValue() { return iDefaultValue; }
	public void setDefaultValue(String defaultValue) { iDefaultValue = defaultValue; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof ScriptParameter)) return false;
		ScriptParameter scriptParameter = (ScriptParameter)o;
		if (getScript() == null || scriptParameter.getScript() == null || !getScript().equals(scriptParameter.getScript())) return false;
		if (getName() == null || scriptParameter.getName() == null || !getName().equals(scriptParameter.getName())) return false;
		return true;
	}

	public int hashCode() {
		if (getScript() == null || getName() == null) return super.hashCode();
		return getScript().hashCode() ^ getName().hashCode();
	}

	public String toString() {
		return "ScriptParameter[" + getScript() + ", " + getName() + "]";
	}

	public String toDebugString() {
		return "ScriptParameter[" +
			"\n	DefaultValue: " + getDefaultValue() +
			"\n	Label: " + getLabel() +
			"\n	Name: " + getName() +
			"\n	Script: " + getScript() +
			"\n	Type: " + getType() +
			"]";
	}
}
