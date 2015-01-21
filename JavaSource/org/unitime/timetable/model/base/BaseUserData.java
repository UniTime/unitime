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

import org.unitime.timetable.model.UserData;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseUserData implements Serializable {
	private static final long serialVersionUID = 1L;

	private String iExternalUniqueId;
	private String iName;
	private String iValue;


	public static String PROP_VALUE = "value";

	public BaseUserData() {
		initialize();
	}

	protected void initialize() {}

	public String getExternalUniqueId() { return iExternalUniqueId; }
	public void setExternalUniqueId(String externalUniqueId) { iExternalUniqueId = externalUniqueId; }

	public String getName() { return iName; }
	public void setName(String name) { iName = name; }

	public String getValue() { return iValue; }
	public void setValue(String value) { iValue = value; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof UserData)) return false;
		UserData userData = (UserData)o;
		if (getExternalUniqueId() == null || userData.getExternalUniqueId() == null || !getExternalUniqueId().equals(userData.getExternalUniqueId())) return false;
		if (getName() == null || userData.getName() == null || !getName().equals(userData.getName())) return false;
		return true;
	}

	public int hashCode() {
		if (getExternalUniqueId() == null || getName() == null) return super.hashCode();
		return getExternalUniqueId().hashCode() ^ getName().hashCode();
	}

	public String toString() {
		return "UserData[" + getExternalUniqueId() + ", " + getName() + "]";
	}

	public String toDebugString() {
		return "UserData[" +
			"\n	ExternalUniqueId: " + getExternalUniqueId() +
			"\n	Name: " + getName() +
			"\n	Value: " + getValue() +
			"]";
	}
}
