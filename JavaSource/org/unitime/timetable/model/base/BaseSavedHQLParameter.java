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

import org.unitime.timetable.model.SavedHQL;
import org.unitime.timetable.model.SavedHQLParameter;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseSavedHQLParameter implements Serializable {
	private static final long serialVersionUID = 1L;

	private SavedHQL iSavedHQL;
	private String iName;
	private String iLabel;
	private String iType;
	private String iDefaultValue;


	public static String PROP_LABEL = "label";
	public static String PROP_TYPE = "type";
	public static String PROP_DEFAULT_VALUE = "defaultValue";

	public BaseSavedHQLParameter() {
		initialize();
	}

	protected void initialize() {}

	public SavedHQL getSavedHQL() { return iSavedHQL; }
	public void setSavedHQL(SavedHQL savedHQL) { iSavedHQL = savedHQL; }

	public String getName() { return iName; }
	public void setName(String name) { iName = name; }

	public String getLabel() { return iLabel; }
	public void setLabel(String label) { iLabel = label; }

	public String getType() { return iType; }
	public void setType(String type) { iType = type; }

	public String getDefaultValue() { return iDefaultValue; }
	public void setDefaultValue(String defaultValue) { iDefaultValue = defaultValue; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof SavedHQLParameter)) return false;
		SavedHQLParameter savedHQLParameter = (SavedHQLParameter)o;
		if (getSavedHQL() == null || savedHQLParameter.getSavedHQL() == null || !getSavedHQL().equals(savedHQLParameter.getSavedHQL())) return false;
		if (getName() == null || savedHQLParameter.getName() == null || !getName().equals(savedHQLParameter.getName())) return false;
		return true;
	}

	public int hashCode() {
		if (getSavedHQL() == null || getName() == null) return super.hashCode();
		return getSavedHQL().hashCode() ^ getName().hashCode();
	}

	public String toString() {
		return "SavedHQLParameter[" + getSavedHQL() + ", " + getName() + "]";
	}

	public String toDebugString() {
		return "SavedHQLParameter[" +
			"\n	DefaultValue: " + getDefaultValue() +
			"\n	Label: " + getLabel() +
			"\n	Name: " + getName() +
			"\n	SavedHQL: " + getSavedHQL() +
			"\n	Type: " + getType() +
			"]";
	}
}
