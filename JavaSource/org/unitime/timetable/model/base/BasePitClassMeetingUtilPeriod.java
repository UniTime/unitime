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

import org.unitime.timetable.model.PitClassMeeting;
import org.unitime.timetable.model.PitClassMeetingUtilPeriod;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BasePitClassMeetingUtilPeriod implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Integer iTimeSlot;

	private PitClassMeeting iPitClassMeeting;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_TIME_SLOT = "timeSlot";

	public BasePitClassMeetingUtilPeriod() {
		initialize();
	}

	public BasePitClassMeetingUtilPeriod(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public Integer getTimeSlot() { return iTimeSlot; }
	public void setTimeSlot(Integer timeSlot) { iTimeSlot = timeSlot; }

	public PitClassMeeting getPitClassMeeting() { return iPitClassMeeting; }
	public void setPitClassMeeting(PitClassMeeting pitClassMeeting) { iPitClassMeeting = pitClassMeeting; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof PitClassMeetingUtilPeriod)) return false;
		if (getUniqueId() == null || ((PitClassMeetingUtilPeriod)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((PitClassMeetingUtilPeriod)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "PitClassMeetingUtilPeriod["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "PitClassMeetingUtilPeriod[" +
			"\n	PitClassMeeting: " + getPitClassMeeting() +
			"\n	TimeSlot: " + getTimeSlot() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
