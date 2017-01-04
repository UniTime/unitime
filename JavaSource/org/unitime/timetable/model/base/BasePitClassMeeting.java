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
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.unitime.timetable.model.PitClassEvent;
import org.unitime.timetable.model.PitClassMeeting;
import org.unitime.timetable.model.PitClassMeetingUtilPeriod;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
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

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_MEETING_DATE = "meetingDate";
	public static String PROP_START_PERIOD = "startPeriod";
	public static String PROP_START_OFFSET = "startOffset";
	public static String PROP_STOP_PERIOD = "stopPeriod";
	public static String PROP_STOP_OFFSET = "stopOffset";
	public static String PROP_LOCATION_PERM_ID = "locationPermanentId";
	public static String PROP_TIME_PATTERN_MIN_PER_MTG = "timePatternMinPerMtg";
	public static String PROP_CALCULATED_MIN_PER_MTG = "calculatedMinPerMtg";

	public BasePitClassMeeting() {
		initialize();
	}

	public BasePitClassMeeting(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public Date getMeetingDate() { return iMeetingDate; }
	public void setMeetingDate(Date meetingDate) { iMeetingDate = meetingDate; }

	public Integer getStartPeriod() { return iStartPeriod; }
	public void setStartPeriod(Integer startPeriod) { iStartPeriod = startPeriod; }

	public Integer getStartOffset() { return iStartOffset; }
	public void setStartOffset(Integer startOffset) { iStartOffset = startOffset; }

	public Integer getStopPeriod() { return iStopPeriod; }
	public void setStopPeriod(Integer stopPeriod) { iStopPeriod = stopPeriod; }

	public Integer getStopOffset() { return iStopOffset; }
	public void setStopOffset(Integer stopOffset) { iStopOffset = stopOffset; }

	public Long getLocationPermanentId() { return iLocationPermanentId; }
	public void setLocationPermanentId(Long locationPermanentId) { iLocationPermanentId = locationPermanentId; }

	public Integer getTimePatternMinPerMtg() { return iTimePatternMinPerMtg; }
	public void setTimePatternMinPerMtg(Integer timePatternMinPerMtg) { iTimePatternMinPerMtg = timePatternMinPerMtg; }

	public Integer getCalculatedMinPerMtg() { return iCalculatedMinPerMtg; }
	public void setCalculatedMinPerMtg(Integer calculatedMinPerMtg) { iCalculatedMinPerMtg = calculatedMinPerMtg; }

	public PitClassEvent getPitClassEvent() { return iPitClassEvent; }
	public void setPitClassEvent(PitClassEvent pitClassEvent) { iPitClassEvent = pitClassEvent; }

	public Set<PitClassMeetingUtilPeriod> getPitClassMeetingUtilPeriods() { return iPitClassMeetingUtilPeriods; }
	public void setPitClassMeetingUtilPeriods(Set<PitClassMeetingUtilPeriod> pitClassMeetingUtilPeriods) { iPitClassMeetingUtilPeriods = pitClassMeetingUtilPeriods; }
	public void addTopitClassMeetingUtilPeriods(PitClassMeetingUtilPeriod pitClassMeetingUtilPeriod) {
		if (iPitClassMeetingUtilPeriods == null) iPitClassMeetingUtilPeriods = new HashSet<PitClassMeetingUtilPeriod>();
		iPitClassMeetingUtilPeriods.add(pitClassMeetingUtilPeriod);
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof PitClassMeeting)) return false;
		if (getUniqueId() == null || ((PitClassMeeting)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((PitClassMeeting)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

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
