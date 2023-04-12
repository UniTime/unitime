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

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.unitime.timetable.model.PitClassMeeting;
import org.unitime.timetable.model.PitClassMeetingUtilPeriod;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BasePitClassMeetingUtilPeriod implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Integer iTimeSlot;

	private PitClassMeeting iPitClassMeeting;

	public BasePitClassMeetingUtilPeriod() {
	}

	public BasePitClassMeetingUtilPeriod(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "pit_class_mtg_util_period_id", strategy = "org.unitime.commons.hibernate.id.UniqueIdGenerator", parameters = {
		@Parameter(name = "sequence", value = "point_in_time_seq")
	})
	@GeneratedValue(generator = "pit_class_mtg_util_period_id")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "time_slot", nullable = false, length = 10)
	public Integer getTimeSlot() { return iTimeSlot; }
	public void setTimeSlot(Integer timeSlot) { iTimeSlot = timeSlot; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "pit_class_meeting_id", nullable = false)
	public PitClassMeeting getPitClassMeeting() { return iPitClassMeeting; }
	public void setPitClassMeeting(PitClassMeeting pitClassMeeting) { iPitClassMeeting = pitClassMeeting; }

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof PitClassMeetingUtilPeriod)) return false;
		if (getUniqueId() == null || ((PitClassMeetingUtilPeriod)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((PitClassMeetingUtilPeriod)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
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
