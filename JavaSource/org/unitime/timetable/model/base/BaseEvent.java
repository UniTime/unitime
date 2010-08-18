/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.model.base;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.EventContact;
import org.unitime.timetable.model.EventNote;
import org.unitime.timetable.model.Meeting;
import org.unitime.timetable.model.SponsoringOrganization;

public abstract class BaseEvent implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iEventName;
	private Integer iMinCapacity;
	private Integer iMaxCapacity;
	private String iEmail;

	private EventContact iMainContact;
	private SponsoringOrganization iSponsoringOrganization;
	private Set<EventContact> iAdditionalContacts;
	private Set<EventNote> iNotes;
	private Set<Meeting> iMeetings;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_EVENT_NAME = "eventName";
	public static String PROP_MIN_CAPACITY = "minCapacity";
	public static String PROP_MAX_CAPACITY = "maxCapacity";
	public static String PROP_EMAIL = "email";

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
			"\n	MainContact: " + getMainContact() +
			"\n	MaxCapacity: " + getMaxCapacity() +
			"\n	MinCapacity: " + getMinCapacity() +
			"\n	SponsoringOrganization: " + getSponsoringOrganization() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
