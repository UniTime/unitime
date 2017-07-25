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

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_EVENT_NAME = "eventName";
	public static String PROP_MIN_CAPACITY = "minCapacity";
	public static String PROP_MAX_CAPACITY = "maxCapacity";
	public static String PROP_EMAIL = "email";
	public static String PROP_EXPIRATION_DATE = "expirationDate";

	public BaseEvent() {
		initialize();
	}

	public BaseEvent(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public String getEventName() { return iEventName; }
	public void setEventName(String eventName) { iEventName = eventName; }

	public Integer getMinCapacity() { return iMinCapacity; }
	public void setMinCapacity(Integer minCapacity) { iMinCapacity = minCapacity; }

	public Integer getMaxCapacity() { return iMaxCapacity; }
	public void setMaxCapacity(Integer maxCapacity) { iMaxCapacity = maxCapacity; }

	public String getEmail() { return iEmail; }
	public void setEmail(String email) { iEmail = email; }

	public Date getExpirationDate() { return iExpirationDate; }
	public void setExpirationDate(Date expirationDate) { iExpirationDate = expirationDate; }

	public Integer getExamStatus() { return iExamStatus; }
	public void setExamStatus(Integer examStatus) { iExamStatus = examStatus; }

	public Long getDepartmentId() { return iDepartmentId; }
	public void setDepartmentId(Long departmentId) { iDepartmentId = departmentId; }

	public EventContact getMainContact() { return iMainContact; }
	public void setMainContact(EventContact mainContact) { iMainContact = mainContact; }

	public SponsoringOrganization getSponsoringOrganization() { return iSponsoringOrganization; }
	public void setSponsoringOrganization(SponsoringOrganization sponsoringOrganization) { iSponsoringOrganization = sponsoringOrganization; }

	public Set<EventContact> getAdditionalContacts() { return iAdditionalContacts; }
	public void setAdditionalContacts(Set<EventContact> additionalContacts) { iAdditionalContacts = additionalContacts; }
	public void addToadditionalContacts(EventContact eventContact) {
		if (iAdditionalContacts == null) iAdditionalContacts = new HashSet<EventContact>();
		iAdditionalContacts.add(eventContact);
	}

	public Set<EventNote> getNotes() { return iNotes; }
	public void setNotes(Set<EventNote> notes) { iNotes = notes; }
	public void addTonotes(EventNote eventNote) {
		if (iNotes == null) iNotes = new HashSet<EventNote>();
		iNotes.add(eventNote);
	}

	public Set<Meeting> getMeetings() { return iMeetings; }
	public void setMeetings(Set<Meeting> meetings) { iMeetings = meetings; }
	public void addTomeetings(Meeting meeting) {
		if (iMeetings == null) iMeetings = new HashSet<Meeting>();
		iMeetings.add(meeting);
	}

	public Set<EventServiceProvider> getRequestedServices() { return iRequestedServices; }
	public void setRequestedServices(Set<EventServiceProvider> requestedServices) { iRequestedServices = requestedServices; }
	public void addTorequestedServices(EventServiceProvider eventServiceProvider) {
		if (iRequestedServices == null) iRequestedServices = new HashSet<EventServiceProvider>();
		iRequestedServices.add(eventServiceProvider);
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof Event)) return false;
		if (getUniqueId() == null || ((Event)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((Event)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

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
