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

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

import java.io.Serializable;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.TimePref;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseTimePref extends Preference implements Serializable {
	private static final long serialVersionUID = 1L;

	private String iPreference;

	private TimePattern iTimePattern;

	public BaseTimePref() {
	}

	public BaseTimePref(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Column(name = "preference", nullable = true, length = 2048)
	public String getPreference() { return iPreference; }
	public void setPreference(String preference) { iPreference = preference; }

	@ManyToOne(optional = true, fetch = FetchType.EAGER)
	@JoinColumn(name = "time_pattern_id", nullable = true)
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, include = "non-lazy")
	public TimePattern getTimePattern() { return iTimePattern; }
	public void setTimePattern(TimePattern timePattern) { iTimePattern = timePattern; }

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof TimePref)) return false;
		if (getUniqueId() == null || ((TimePref)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((TimePref)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "TimePref["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "TimePref[" +
			"\n	Note: " + getNote() +
			"\n	Owner: " + getOwner() +
			"\n	PrefLevel: " + getPrefLevel() +
			"\n	Preference: " + getPreference() +
			"\n	TimePattern: " + getTimePattern() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
