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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.EventContact;
import org.unitime.timetable.model.EventNote;
import org.unitime.timetable.model.EventServiceProvider;
import org.unitime.timetable.model.Meeting;
import org.unitime.timetable.model.SponsoringOrganization;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseEvent implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iEventName;
	private Integer iMinCapacity;
	private Integer iMaxCapacity;
	private String iEmail;
	private Date iExpirationDate;
	private Integer iExamStatus;
	private Long iDepartmentId;

	private EventContact iMainContact;
	private SponsoringOrganization iSponsoringOrganization;
	private Set<EventContact> iAdditionalContacts;
	private Set<EventNote> iNotes;
	private Set<Meeting> iMeetings;
	private Set<EventServiceProvider> iRequestedServices;

	public BaseEvent() {
	}

	public BaseEvent(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "event_id", strategy = "org.unitime.commons.hibernate.id.UniqueIdGenerator", parameters = {
		@Parameter(name = "sequence", value = "pref_group_seq")
	})
	@GeneratedValue(generator = "event_id")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "event_name", nullable = true, length = 100)
	public String getEventName() { return iEventName; }
	public void setEventName(String eventName) { iEventName = eventName; }

	@Column(name = "min_capacity", nullable = true, length = 10)
	public Integer getMinCapacity() { return iMinCapacity; }
	public void setMinCapacity(Integer minCapacity) { iMinCapacity = minCapacity; }

	@Column(name = "max_capacity", nullable = true, length = 10)
	public Integer getMaxCapacity() { return iMaxCapacity; }
	public void setMaxCapacity(Integer maxCapacity) { iMaxCapacity = maxCapacity; }

	@Column(name = "email", nullable = true, length = 1000)
	public String getEmail() { return iEmail; }
	public void setEmail(String email) { iEmail = email; }

	@Column(name = "expiration_date", nullable = true)
	public Date getExpirationDate() { return iExpirationDate; }
	public void setExpirationDate(Date expirationDate) { iExpirationDate = expirationDate; }

	@Formula(" ( select t.status from %SCHEMA%.exam x, %SCHEMA%.exam_status s, %SCHEMA%.dept_status_type t where x.uniqueid = exam_id and s.session_id = x.session_id and s.type_id = x.exam_type_id and s.status_id = t.uniqueid) ")
	public Integer getExamStatus() { return iExamStatus; }
	public void setExamStatus(Integer examStatus) { iExamStatus = examStatus; }

	@Formula(" ( select sa.department_uniqueid from %SCHEMA%.class_ c, %SCHEMA%.scheduling_subpart ss, %SCHEMA%.instr_offering_config ioc, %SCHEMA%.instructional_offering io, %SCHEMA%.course_offering co, %SCHEMA%.subject_area sa where c.uniqueid = class_id and ss.uniqueid = c.subpart_id and ioc.uniqueid = ss.config_id and io.uniqueid = ioc.instr_offr_id and co.instr_offr_id = io.uniqueid and co.is_control = %TRUE% and sa.uniqueid = co.subject_area_id ) ")
	public Long getDepartmentId() { return iDepartmentId; }
	public void setDepartmentId(Long departmentId) { iDepartmentId = departmentId; }

	@ManyToOne(optional = true)
	@JoinColumn(name = "main_contact_id", nullable = true)
	public EventContact getMainContact() { return iMainContact; }
	public void setMainContact(EventContact mainContact) { iMainContact = mainContact; }

	@ManyToOne(optional = true)
	@JoinColumn(name = "sponsor_org_id", nullable = true)
	public SponsoringOrganization getSponsoringOrganization() { return iSponsoringOrganization; }
	public void setSponsoringOrganization(SponsoringOrganization sponsoringOrganization) { iSponsoringOrganization = sponsoringOrganization; }

	@ManyToMany
	@JoinTable(name = "event_join_event_contact",
		joinColumns = { @JoinColumn(name = "event_id") },
		inverseJoinColumns = { @JoinColumn(name = "event_contact_id") })
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, include = "non-lazy")
	public Set<EventContact> getAdditionalContacts() { return iAdditionalContacts; }
	public void setAdditionalContacts(Set<EventContact> additionalContacts) { iAdditionalContacts = additionalContacts; }
	public void addToadditionalContacts(EventContact eventContact) {
		if (iAdditionalContacts == null) iAdditionalContacts = new HashSet<EventContact>();
		iAdditionalContacts.add(eventContact);
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "event", cascade = {CascadeType.ALL}, orphanRemoval = true)
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, include = "non-lazy")
	public Set<EventNote> getNotes() { return iNotes; }
	public void setNotes(Set<EventNote> notes) { iNotes = notes; }
	public void addTonotes(EventNote eventNote) {
		if (iNotes == null) iNotes = new HashSet<EventNote>();
		iNotes.add(eventNote);
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "event", cascade = {CascadeType.ALL}, orphanRemoval = true)
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, include = "non-lazy")
	public Set<Meeting> getMeetings() { return iMeetings; }
	public void setMeetings(Set<Meeting> meetings) { iMeetings = meetings; }
	public void addTomeetings(Meeting meeting) {
		if (iMeetings == null) iMeetings = new HashSet<Meeting>();
		iMeetings.add(meeting);
	}

	@ManyToMany
	@JoinTable(name = "event_service_provider",
		joinColumns = { @JoinColumn(name = "event_id") },
		inverseJoinColumns = { @JoinColumn(name = "provider_id") })
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, include = "non-lazy")
	public Set<EventServiceProvider> getRequestedServices() { return iRequestedServices; }
	public void setRequestedServices(Set<EventServiceProvider> requestedServices) { iRequestedServices = requestedServices; }
	public void addTorequestedServices(EventServiceProvider eventServiceProvider) {
		if (iRequestedServices == null) iRequestedServices = new HashSet<EventServiceProvider>();
		iRequestedServices.add(eventServiceProvider);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof Event)) return false;
		if (getUniqueId() == null || ((Event)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((Event)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "Event["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "Event[" +
			"\n	Email: " + getEmail() +
			"\n	EventName: " + getEventName() +
			"\n	ExpirationDate: " + getExpirationDate() +
			"\n	MainContact: " + getMainContact() +
			"\n	MaxCapacity: " + getMaxCapacity() +
			"\n	MinCapacity: " + getMinCapacity() +
			"\n	SponsoringOrganization: " + getSponsoringOrganization() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
