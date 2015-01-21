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

import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.PreferenceGroup;
import org.unitime.timetable.model.PreferenceLevel;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BasePreference implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;

	private PreferenceGroup iOwner;
	private PreferenceLevel iPrefLevel;

	public static String PROP_UNIQUEID = "uniqueId";

	public BasePreference() {
		initialize();
	}

	public BasePreference(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public PreferenceGroup getOwner() { return iOwner; }
	public void setOwner(PreferenceGroup owner) { iOwner = owner; }

	public PreferenceLevel getPrefLevel() { return iPrefLevel; }
	public void setPrefLevel(PreferenceLevel prefLevel) { iPrefLevel = prefLevel; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof Preference)) return false;
		if (getUniqueId() == null || ((Preference)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((Preference)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "Preference["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "Preference[" +
			"\n	Owner: " + getOwner() +
			"\n	PrefLevel: " + getPrefLevel() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
