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

import org.unitime.timetable.model.FreeTime;
import org.unitime.timetable.model.Session;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseFreeTime implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iName;
	private Integer iDayCode;
	private Integer iStartSlot;
	private Integer iLength;
	private Integer iCategory;

	private Session iSession;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_NAME = "name";
	public static String PROP_DAY_CODE = "dayCode";
	public static String PROP_START_SLOT = "startSlot";
	public static String PROP_LENGTH = "length";
	public static String PROP_CATEGORY = "category";

	public BaseFreeTime() {
		initialize();
	}

	public BaseFreeTime(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public String getName() { return iName; }
	public void setName(String name) { iName = name; }

	public Integer getDayCode() { return iDayCode; }
	public void setDayCode(Integer dayCode) { iDayCode = dayCode; }

	public Integer getStartSlot() { return iStartSlot; }
	public void setStartSlot(Integer startSlot) { iStartSlot = startSlot; }

	public Integer getLength() { return iLength; }
	public void setLength(Integer length) { iLength = length; }

	public Integer getCategory() { return iCategory; }
	public void setCategory(Integer category) { iCategory = category; }

	public Session getSession() { return iSession; }
	public void setSession(Session session) { iSession = session; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof FreeTime)) return false;
		if (getUniqueId() == null || ((FreeTime)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((FreeTime)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

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
