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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;

import java.io.Serializable;

import org.unitime.commons.annotations.UniqueIdGenerator;
import org.unitime.timetable.model.FreeTime;
import org.unitime.timetable.model.Session;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseFreeTime implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iName;
	private Integer iDayCode;
	private Integer iStartSlot;
	private Integer iLength;
	private Integer iCategory;

	private Session iSession;

	public BaseFreeTime() {
	}

	public BaseFreeTime(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@UniqueIdGenerator(sequence = "pref_group_seq")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "name", nullable = false, length = 50)
	public String getName() { return iName; }
	public void setName(String name) { iName = name; }

	@Column(name = "day_code", nullable = false, length = 10)
	public Integer getDayCode() { return iDayCode; }
	public void setDayCode(Integer dayCode) { iDayCode = dayCode; }

	@Column(name = "start_slot", nullable = false, length = 10)
	public Integer getStartSlot() { return iStartSlot; }
	public void setStartSlot(Integer startSlot) { iStartSlot = startSlot; }

	@Column(name = "length", nullable = false, length = 10)
	public Integer getLength() { return iLength; }
	public void setLength(Integer length) { iLength = length; }

	@Column(name = "category", nullable = false, length = 10)
	public Integer getCategory() { return iCategory; }
	public void setCategory(Integer category) { iCategory = category; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "session_id", nullable = false)
	public Session getSession() { return iSession; }
	public void setSession(Session session) { iSession = session; }

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof FreeTime)) return false;
		if (getUniqueId() == null || ((FreeTime)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((FreeTime)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "FreeTime["+getUniqueId()+" "+getName()+"]";
	}

	public String toDebugString() {
		return "FreeTime[" +
			"\n	Category: " + getCategory() +
			"\n	DayCode: " + getDayCode() +
			"\n	Length: " + getLength() +
			"\n	Name: " + getName() +
			"\n	Session: " + getSession() +
			"\n	StartSlot: " + getStartSlot() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
