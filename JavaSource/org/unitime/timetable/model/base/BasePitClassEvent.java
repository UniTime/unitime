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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToMany;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.unitime.commons.hibernate.id.UniqueIdGenerator;
import org.unitime.timetable.model.PitClass;
import org.unitime.timetable.model.PitClassEvent;
import org.unitime.timetable.model.PitClassMeeting;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BasePitClassEvent implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iEventName;

	private PitClass iPitClass;
	private Set<PitClassMeeting> iPitClassMeetings;

	public BasePitClassEvent() {
	}

	public BasePitClassEvent(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "pit_class_event_id", type = UniqueIdGenerator.class, parameters = {
		@Parameter(name = "sequence", value = "point_in_time_seq")
	})
	@GeneratedValue(generator = "pit_class_event_id")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "event_name", nullable = true, length = 100)
	public String getEventName() { return iEventName; }
	public void setEventName(String eventName) { iEventName = eventName; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "pit_class_id", nullable = false)
	public PitClass getPitClass() { return iPitClass; }
	public void setPitClass(PitClass pitClass) { iPitClass = pitClass; }

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "pitClassEvent", cascade = {CascadeType.ALL}, orphanRemoval = true)
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, includeLazy = false)
	public Set<PitClassMeeting> getPitClassMeetings() { return iPitClassMeetings; }
	public void setPitClassMeetings(Set<PitClassMeeting> pitClassMeetings) { iPitClassMeetings = pitClassMeetings; }
	public void addTopitClassMeetings(PitClassMeeting pitClassMeeting) {
		if (iPitClassMeetings == null) iPitClassMeetings = new HashSet<PitClassMeeting>();
		iPitClassMeetings.add(pitClassMeeting);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof PitClassEvent)) return false;
		if (getUniqueId() == null || ((PitClassEvent)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((PitClassEvent)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "PitClassEvent["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "PitClassEvent[" +
			"\n	EventName: " + getEventName() +
			"\n	PitClass: " + getPitClass() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
