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

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.EventNote;
import org.unitime.timetable.model.Meeting;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseEventNote implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Integer iNoteType;
	private String iTextNote;
	private Date iTimeStamp;
	private String iUser;
	private String iUserId;
	private String iMeetings;
	private byte[] iAttachedFile;
	private String iAttachedName;
	private String iAttachedContentType;

	private Event iEvent;
	private Set<Meeting> iAffectedMeetings;

	public BaseEventNote() {
	}

	public BaseEventNote(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "event_note_id", strategy = "org.unitime.commons.hibernate.id.UniqueIdGenerator", parameters = {
		@Parameter(name = "sequence", value = "pref_group_seq")
	})
	@GeneratedValue(generator = "event_note_id")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "note_type", nullable = false, length = 10)
	public Integer getNoteType() { return iNoteType; }
	public void setNoteType(Integer noteType) { iNoteType = noteType; }

	@Column(name = "text_note", nullable = true, length = 2000)
	public String getTextNote() { return iTextNote; }
	public void setTextNote(String textNote) { iTextNote = textNote; }

	@Column(name = "time_stamp", nullable = false)
	public Date getTimeStamp() { return iTimeStamp; }
	public void setTimeStamp(Date timeStamp) { iTimeStamp = timeStamp; }

	@Column(name = "uname", nullable = true, length = 100)
	public String getUser() { return iUser; }
	public void setUser(String user) { iUser = user; }

	@Column(name = "user_id", nullable = true, length = 40)
	public String getUserId() { return iUserId; }
	public void setUserId(String userId) { iUserId = userId; }

	@Column(name = "meetings", nullable = true)
	public String getMeetings() { return iMeetings; }
	public void setMeetings(String meetings) { iMeetings = meetings; }

	@Column(name = "attached_file", nullable = true)
	public byte[] getAttachedFile() { return iAttachedFile; }
	public void setAttachedFile(byte[] attachedFile) { iAttachedFile = attachedFile; }

	@Column(name = "attached_name", nullable = true, length = 260)
	public String getAttachedName() { return iAttachedName; }
	public void setAttachedName(String attachedName) { iAttachedName = attachedName; }

	@Column(name = "attached_content", nullable = true, length = 260)
	public String getAttachedContentType() { return iAttachedContentType; }
	public void setAttachedContentType(String attachedContentType) { iAttachedContentType = attachedContentType; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "event_id", nullable = false)
	public Event getEvent() { return iEvent; }
	public void setEvent(Event event) { iEvent = event; }

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "event_note_meeting",
		joinColumns = { @JoinColumn(name = "note_id") },
		inverseJoinColumns = { @JoinColumn(name = "meeting_id") })
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, include = "non-lazy")
	public Set<Meeting> getAffectedMeetings() { return iAffectedMeetings; }
	public void setAffectedMeetings(Set<Meeting> affectedMeetings) { iAffectedMeetings = affectedMeetings; }
	public void addToaffectedMeetings(Meeting meeting) {
		if (iAffectedMeetings == null) iAffectedMeetings = new HashSet<Meeting>();
		iAffectedMeetings.add(meeting);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof EventNote)) return false;
		if (getUniqueId() == null || ((EventNote)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((EventNote)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "EventNote["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "EventNote[" +
			"\n	AttachedContentType: " + getAttachedContentType() +
			"\n	AttachedFile: " + getAttachedFile() +
			"\n	AttachedName: " + getAttachedName() +
			"\n	Event: " + getEvent() +
			"\n	Meetings: " + getMeetings() +
			"\n	NoteType: " + getNoteType() +
			"\n	TextNote: " + getTextNote() +
			"\n	TimeStamp: " + getTimeStamp() +
			"\n	UniqueId: " + getUniqueId() +
			"\n	User: " + getUser() +
			"\n	UserId: " + getUserId() +
			"]";
	}
}
