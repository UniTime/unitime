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
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToMany;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.unitime.commons.annotations.UniqueIdGenerator;
import org.unitime.timetable.model.PitClassEvent;
import org.unitime.timetable.model.PitClassMeeting;
import org.unitime.timetable.model.PitClassMeetingUtilPeriod;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BasePitClassMeeting implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Date iMeetingDate;
	private Integer iStartPeriod;
	private Integer iStartOffset;
	private Integer iStopPeriod;
	private Integer iStopOffset;
	private Long iLocationPermanentId;
	private Integer iTimePatternMinPerMtg;
	private Integer iCalculatedMinPerMtg;

	private PitClassEvent iPitClassEvent;
	private Set<PitClassMeetingUtilPeriod> iPitClassMeetingUtilPeriods;

	public BasePitClassMeeting() {
	}

	public BasePitClassMeeting(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@UniqueIdGenerator(sequence = "point_in_time_seq")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "meeting_date", nullable = false)
	public Date getMeetingDate() { return iMeetingDate; }
	public void setMeetingDate(Date meetingDate) { iMeetingDate = meetingDate; }

	@Column(name = "start_period", nullable = false, length = 10)
	public Integer getStartPeriod() { return iStartPeriod; }
	public void setStartPeriod(Integer startPeriod) { iStartPeriod = startPeriod; }

	@Column(name = "start_offset", nullable = true, length = 10)
	public Integer getStartOffset() { return iStartOffset; }
	public void setStartOffset(Integer startOffset) { iStartOffset = startOffset; }

	@Column(name = "stop_period", nullable = false, length = 10)
	public Integer getStopPeriod() { return iStopPeriod; }
	public void setStopPeriod(Integer stopPeriod) { iStopPeriod = stopPeriod; }

	@Column(name = "stop_offset", nullable = true, length = 10)
	public Integer getStopOffset() { return iStopOffset; }
	public void setStopOffset(Integer stopOffset) { iStopOffset = stopOffset; }

	@Column(name = "location_perm_id", nullable = true, length = 20)
	public Long getLocationPermanentId() { return iLocationPermanentId; }
	public void setLocationPermanentId(Long locationPermanentId) { iLocationPermanentId = locationPermanentId; }

	@Column(name = "time_pattern_min_per_mtg", nullable = false, length = 10)
	public Integer getTimePatternMinPerMtg() { return iTimePatternMinPerMtg; }
	public void setTimePatternMinPerMtg(Integer timePatternMinPerMtg) { iTimePatternMinPerMtg = timePatternMinPerMtg; }

	@Column(name = "calculated_min_per_mtg", nullable = false, length = 10)
	public Integer getCalculatedMinPerMtg() { return iCalculatedMinPerMtg; }
	public void setCalculatedMinPerMtg(Integer calculatedMinPerMtg) { iCalculatedMinPerMtg = calculatedMinPerMtg; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "pit_class_event_id", nullable = false)
	public PitClassEvent getPitClassEvent() { return iPitClassEvent; }
	public void setPitClassEvent(PitClassEvent pitClassEvent) { iPitClassEvent = pitClassEvent; }

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "pitClassMeeting", cascade = {CascadeType.ALL}, orphanRemoval = true)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	public Set<PitClassMeetingUtilPeriod> getPitClassMeetingUtilPeriods() { return iPitClassMeetingUtilPeriods; }
	public void setPitClassMeetingUtilPeriods(Set<PitClassMeetingUtilPeriod> pitClassMeetingUtilPeriods) { iPitClassMeetingUtilPeriods = pitClassMeetingUtilPeriods; }
	public void addToPitClassMeetingUtilPeriods(PitClassMeetingUtilPeriod pitClassMeetingUtilPeriod) {
		if (iPitClassMeetingUtilPeriods == null) iPitClassMeetingUtilPeriods = new HashSet<PitClassMeetingUtilPeriod>();
		iPitClassMeetingUtilPeriods.add(pitClassMeetingUtilPeriod);
	}
	@Deprecated
	public void addTopitClassMeetingUtilPeriods(PitClassMeetingUtilPeriod pitClassMeetingUtilPeriod) {
		addToPitClassMeetingUtilPeriods(pitClassMeetingUtilPeriod);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof PitClassMeeting)) return false;
		if (getUniqueId() == null || ((PitClassMeeting)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((PitClassMeeting)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "PitClassMeeting["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "PitClassMeeting[" +
			"\n	CalculatedMinPerMtg: " + getCalculatedMinPerMtg() +
			"\n	LocationPermanentId: " + getLocationPermanentId() +
			"\n	MeetingDate: " + getMeetingDate() +
			"\n	PitClassEvent: " + getPitClassEvent() +
			"\n	StartOffset: " + getStartOffset() +
			"\n	StartPeriod: " + getStartPeriod() +
			"\n	StopOffset: " + getStopOffset() +
			"\n	StopPeriod: " + getStopPeriod() +
			"\n	TimePatternMinPerMtg: " + getTimePatternMinPerMtg() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
