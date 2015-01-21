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

import org.unitime.timetable.model.ManagerSettings;
import org.unitime.timetable.model.Settings;
import org.unitime.timetable.model.TimetableManager;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseManagerSettings implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iValue;

	private Settings iKey;
	private TimetableManager iManager;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_VALUE = "value";

	public BaseManagerSettings() {
		initialize();
	}

	public BaseManagerSettings(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public String getValue() { return iValue; }
	public void setValue(String value) { iValue = value; }

	public Settings getKey() { return iKey; }
	public void setKey(Settings key) { iKey = key; }

	public TimetableManager getManager() { return iManager; }
	public void setManager(TimetableManager manager) { iManager = manager; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof ManagerSettings)) return false;
		if (getUniqueId() == null || ((ManagerSettings)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((ManagerSettings)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "ManagerSettings["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "ManagerSettings[" +
			"\n	Key: " + getKey() +
			"\n	Manager: " + getManager() +
			"\n	UniqueId: " + getUniqueId() +
			"\n	Value: " + getValue() +
			"]";
	}
}
