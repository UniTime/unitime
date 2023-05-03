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
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.unitime.commons.hibernate.id.UniqueIdGenerator;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.EventContact;
import org.unitime.timetable.model.Meeting;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseMeeting implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Date iMeetingDate;
	private Integer iStartPeriod;
	private Integer iStartOffset;
	private Integer iStopPeriod;
	private Integer iStopOffset;
	private Long iLocationPermanentId;
	private Boolean iClassCanOverride;
	private Integer iApprovalStatus;
	private Date iApprovalDate;

	private Event iEvent;
	private Set<EventContact> iMeetingContacts;

	public BaseMeeting() {
	}

	public BaseMeeting(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "meeting_id", type = UniqueIdGenerator.class, parameters = {
		@Parameter(name = "sequence", value = "pref_group_seq")
	})
	@GeneratedValue(generator = "meeting_id")
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

	@Column(name = "class_can_override", nullable = false)
	public Boolean isClassCanOverride() { return iClassCanOverride; }
	@Transient
	public Boolean getClassCanOverride() { return iClassCanOverride; }
	public void setClassCanOverride(Boolean classCanOverride) { iClassCanOverride = classCanOverride; }

	@Column(name = "approval_status", nullable = false)
	public Integer getApprovalStatus() { return iApprovalStatus; }
	public void setApprovalStatus(Integer approvalStatus) { iApprovalStatus = approvalStatus; }

	@Column(name = "approval_date", nullable = true)
	public Date getApprovalDate() { return iApprovalDate; }
	public void setApprovalDate(Date approvalDate) { iApprovalDate = approvalDate; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "event_id", nullable = false)
	public Event getEvent() { return iEvent; }
	public void setEvent(Event event) { iEvent = event; }

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "meeting_contact",
		joinColumns = { @JoinColumn(name = "meeting_id") },
		inverseJoinColumns = { @JoinColumn(name = "contact_id") })
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	public Set<EventContact> getMeetingContacts() { return iMeetingContacts; }
	public void setMeetingContacts(Set<EventContact> meetingContacts) { iMeetingContacts = meetingContacts; }
	public void addToMeetingContacts(EventContact eventContact) {
		if (iMeetingContacts == null) iMeetingContacts = new HashSet<EventContact>();
		iMeetingContacts.add(eventContact);
	}
	@Deprecated
	public void addTomeetingContacts(EventContact eventContact) {
		addToMeetingContacts(eventContact);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof Meeting)) return false;
		if (getUniqueId() == null || ((Meeting)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((Meeting)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "Meeting["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "Meeting[" +
			"\n	ApprovalDate: " + getApprovalDate() +
			"\n	ApprovalStatus: " + getApprovalStatus() +
			"\n	ClassCanOverride: " + getClassCanOverride() +
			"\n	Event: " + getEvent() +
			"\n	LocationPermanentId: " + getLocationPermanentId() +
			"\n	MeetingDate: " + getMeetingDate() +
			"\n	StartOffset: " + getStartOffset() +
			"\n	StartPeriod: " + getStartPeriod() +
			"\n	StopOffset: " + getStopOffset() +
			"\n	StopPeriod: " + getStopPeriod() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
