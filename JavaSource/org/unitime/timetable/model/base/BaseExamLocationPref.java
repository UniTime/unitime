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

import org.unitime.timetable.model.ExamLocationPref;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.PreferenceLevel;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseExamLocationPref implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;

	private Location iLocation;
	private PreferenceLevel iPrefLevel;
	private ExamPeriod iExamPeriod;

	public static String PROP_UNIQUEID = "uniqueId";

	public BaseExamLocationPref() {
		initialize();
	}

	public BaseExamLocationPref(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public Location getLocation() { return iLocation; }
	public void setLocation(Location location) { iLocation = location; }

	public PreferenceLevel getPrefLevel() { return iPrefLevel; }
	public void setPrefLevel(PreferenceLevel prefLevel) { iPrefLevel = prefLevel; }

	public ExamPeriod getExamPeriod() { return iExamPeriod; }
	public void setExamPeriod(ExamPeriod examPeriod) { iExamPeriod = examPeriod; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof ExamLocationPref)) return false;
		if (getUniqueId() == null || ((ExamLocationPref)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((ExamLocationPref)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "ExamLocationPref["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "ExamLocationPref[" +
			"\n	ExamPeriod: " + getExamPeriod() +
			"\n	Location: " + getLocation() +
			"\n	PrefLevel: " + getPrefLevel() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
