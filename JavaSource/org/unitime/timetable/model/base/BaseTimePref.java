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
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.TimePref;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseTimePref extends Preference implements Serializable {
	private static final long serialVersionUID = 1L;

	private String iPreference;

	private TimePattern iTimePattern;

	public static String PROP_PREFERENCE = "preference";

	public BaseTimePref() {
		initialize();
	}

	public BaseTimePref(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public String getPreference() { return iPreference; }
	public void setPreference(String preference) { iPreference = preference; }

	public TimePattern getTimePattern() { return iTimePattern; }
	public void setTimePattern(TimePattern timePattern) { iTimePattern = timePattern; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof TimePref)) return false;
		if (getUniqueId() == null || ((TimePref)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((TimePref)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "TimePref["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "TimePref[" +
			"\n	Owner: " + getOwner() +
			"\n	PrefLevel: " + getPrefLevel() +
			"\n	Preference: " + getPreference() +
			"\n	TimePattern: " + getTimePattern() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
