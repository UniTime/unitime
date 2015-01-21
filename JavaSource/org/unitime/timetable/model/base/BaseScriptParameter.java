/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
*/
package org.unitime.timetable.model.base;

import java.io.Serializable;

import org.unitime.timetable.model.Script;
import org.unitime.timetable.model.ScriptParameter;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
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
