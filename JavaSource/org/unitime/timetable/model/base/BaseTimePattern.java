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
import java.util.HashSet;
import java.util.Set;

import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.TimePatternDays;
import org.unitime.timetable.model.TimePatternTime;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseTimePattern implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iName;
	private Integer iMinPerMtg;
	private Integer iSlotsPerMtg;
	private Integer iNrMeetings;
	private Integer iBreakTime;
	private Integer iType;
	private Boolean iVisible;

	private Session iSession;
	private Set<TimePatternTime> iTimes;
	private Set<TimePatternDays> iDays;
	private Set<Department> iDepartments;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_NAME = "name";
	public static String PROP_MINS_PMT = "minPerMtg";
	public static String PROP_SLOTS_PMT = "slotsPerMtg";
	public static String PROP_NR_MTGS = "nrMeetings";
	public static String PROP_BREAK_TIME = "breakTime";
	public static String PROP_TYPE = "type";
	public static String PROP_VISIBLE = "visible";

	public BaseTimePattern() {
		initialize();
	}

	public BaseTimePattern(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public String getName() { return iName; }
	public void setName(String name) { iName = name; }

	public Integer getMinPerMtg() { return iMinPerMtg; }
	public void setMinPerMtg(Integer minPerMtg) { iMinPerMtg = minPerMtg; }

	public Integer getSlotsPerMtg() { return iSlotsPerMtg; }
	public void setSlotsPerMtg(Integer slotsPerMtg) { iSlotsPerMtg = slotsPerMtg; }

	public Integer getNrMeetings() { return iNrMeetings; }
	public void setNrMeetings(Integer nrMeetings) { iNrMeetings = nrMeetings; }

	public Integer getBreakTime() { return iBreakTime; }
	public void setBreakTime(Integer breakTime) { iBreakTime = breakTime; }

	public Integer getType() { return iType; }
	public void setType(Integer type) { iType = type; }

	public Boolean isVisible() { return iVisible; }
	public Boolean getVisible() { return iVisible; }
	public void setVisible(Boolean visible) { iVisible = visible; }

	public Session getSession() { return iSession; }
	public void setSession(Session session) { iSession = session; }

	public Set<TimePatternTime> getTimes() { return iTimes; }
	public void setTimes(Set<TimePatternTime> times) { iTimes = times; }
	public void addTotimes(TimePatternTime timePatternTime) {
		if (iTimes == null) iTimes = new HashSet<TimePatternTime>();
		iTimes.add(timePatternTime);
	}

	public Set<TimePatternDays> getDays() { return iDays; }
	public void setDays(Set<TimePatternDays> days) { iDays = days; }
	public void addTodays(TimePatternDays timePatternDays) {
		if (iDays == null) iDays = new HashSet<TimePatternDays>();
		iDays.add(timePatternDays);
	}

	public Set<Department> getDepartments() { return iDepartments; }
	public void setDepartments(Set<Department> departments) { iDepartments = departments; }
	public void addTodepartments(Department department) {
		if (iDepartments == null) iDepartments = new HashSet<Department>();
		iDepartments.add(department);
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof TimePattern)) return false;
		if (getUniqueId() == null || ((TimePattern)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((TimePattern)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "TimePattern["+getUniqueId()+" "+getName()+"]";
	}

	public String toDebugString() {
		return "TimePattern[" +
			"\n	BreakTime: " + getBreakTime() +
			"\n	MinPerMtg: " + getMinPerMtg() +
			"\n	Name: " + getName() +
			"\n	NrMeetings: " + getNrMeetings() +
			"\n	Session: " + getSession() +
			"\n	SlotsPerMtg: " + getSlotsPerMtg() +
			"\n	Type: " + getType() +
			"\n	UniqueId: " + getUniqueId() +
			"\n	Visible: " + getVisible() +
			"]";
	}
}
