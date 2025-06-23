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

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

import java.io.Serializable;

import org.unitime.commons.annotations.UniqueIdGenerator;
import org.unitime.timetable.model.TimePatternDays;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseTimePatternDays implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Integer iDayCode;


	public BaseTimePatternDays() {
	}

	public BaseTimePatternDays(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@UniqueIdGenerator(sequence = "time_pattern_days_seq")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "day_code", nullable = true, length = 7)
	public Integer getDayCode() { return iDayCode; }
	public void setDayCode(Integer dayCode) { iDayCode = dayCode; }

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof TimePatternDays)) return false;
		if (getUniqueId() == null || ((TimePatternDays)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((TimePatternDays)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "TimePatternDays["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "TimePatternDays[" +
			"\n	DayCode: " + getDayCode() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
