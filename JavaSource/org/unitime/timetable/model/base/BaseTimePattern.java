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

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.unitime.commons.hibernate.id.UniqueIdGenerator;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.TimePatternDays;
import org.unitime.timetable.model.TimePatternTime;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
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

	public BaseTimePattern() {
	}

	public BaseTimePattern(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "time_pattern_id", type = UniqueIdGenerator.class, parameters = {
		@Parameter(name = "sequence", value = "time_pattern_seq")
	})
	@GeneratedValue(generator = "time_pattern_id")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "name", nullable = true, length = 50)
	public String getName() { return iName; }
	public void setName(String name) { iName = name; }

	@Column(name = "mins_pmt", nullable = true, length = 3)
	public Integer getMinPerMtg() { return iMinPerMtg; }
	public void setMinPerMtg(Integer minPerMtg) { iMinPerMtg = minPerMtg; }

	@Column(name = "slots_pmt", nullable = true, length = 3)
	public Integer getSlotsPerMtg() { return iSlotsPerMtg; }
	public void setSlotsPerMtg(Integer slotsPerMtg) { iSlotsPerMtg = slotsPerMtg; }

	@Column(name = "nr_mtgs", nullable = true, length = 3)
	public Integer getNrMeetings() { return iNrMeetings; }
	public void setNrMeetings(Integer nrMeetings) { iNrMeetings = nrMeetings; }

	@Column(name = "break_time", nullable = true, length = 3)
	public Integer getBreakTime() { return iBreakTime; }
	public void setBreakTime(Integer breakTime) { iBreakTime = breakTime; }

	@Column(name = "type", nullable = true, length = 2)
	public Integer getType() { return iType; }
	public void setType(Integer type) { iType = type; }

	@Column(name = "visible", nullable = true)
	public Boolean isVisible() { return iVisible; }
	@Transient
	public Boolean getVisible() { return iVisible; }
	public void setVisible(Boolean visible) { iVisible = visible; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "session_id", nullable = false)
	public Session getSession() { return iSession; }
	public void setSession(Session session) { iSession = session; }

	@OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
	@JoinColumn(name = "time_pattern_id", nullable = true)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	public Set<TimePatternTime> getTimes() { return iTimes; }
	public void setTimes(Set<TimePatternTime> times) { iTimes = times; }
	public void addToTimes(TimePatternTime timePatternTime) {
		if (iTimes == null) iTimes = new HashSet<TimePatternTime>();
		iTimes.add(timePatternTime);
	}
	@Deprecated
	public void addTotimes(TimePatternTime timePatternTime) {
		addToTimes(timePatternTime);
	}

	@OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
	@JoinColumn(name = "time_pattern_id", nullable = true)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	public Set<TimePatternDays> getDays() { return iDays; }
	public void setDays(Set<TimePatternDays> days) { iDays = days; }
	public void addToDays(TimePatternDays timePatternDays) {
		if (iDays == null) iDays = new HashSet<TimePatternDays>();
		iDays.add(timePatternDays);
	}
	@Deprecated
	public void addTodays(TimePatternDays timePatternDays) {
		addToDays(timePatternDays);
	}

	@ManyToMany(mappedBy = "timePatterns")
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	public Set<Department> getDepartments() { return iDepartments; }
	public void setDepartments(Set<Department> departments) { iDepartments = departments; }
	public void addToDepartments(Department department) {
		if (iDepartments == null) iDepartments = new HashSet<Department>();
		iDepartments.add(department);
	}
	@Deprecated
	public void addTodepartments(Department department) {
		addToDepartments(department);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof TimePattern)) return false;
		if (getUniqueId() == null || ((TimePattern)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((TimePattern)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
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
