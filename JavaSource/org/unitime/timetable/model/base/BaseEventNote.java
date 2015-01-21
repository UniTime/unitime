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
import org.unitime.timetable.model.EventNote;
import org.unitime.timetable.model.Meeting;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
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

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_NOTE_TYPE = "noteType";
	public static String PROP_TEXT_NOTE = "textNote";
	public static String PROP_TIME_STAMP = "timeStamp";
	public static String PROP_UNAME = "user";
	public static String PROP_USER_ID = "userId";
	public static String PROP_MEETINGS = "meetings";
	public static String PROP_ATTACHED_FILE = "attachedFile";
	public static String PROP_ATTACHED_NAME = "attachedName";
	public static String PROP_ATTACHED_CONTENT = "attachedContentType";

	public BaseEventNote() {
		initialize();
	}

	public BaseEventNote(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public Integer getNoteType() { return iNoteType; }
	public void setNoteType(Integer noteType) { iNoteType = noteType; }

	public String getTextNote() { return iTextNote; }
	public void setTextNote(String textNote) { iTextNote = textNote; }

	public Date getTimeStamp() { return iTimeStamp; }
	public void setTimeStamp(Date timeStamp) { iTimeStamp = timeStamp; }

	public String getUser() { return iUser; }
	public void setUser(String user) { iUser = user; }

	public String getUserId() { return iUserId; }
	public void setUserId(String userId) { iUserId = userId; }

	public String getMeetings() { return iMeetings; }
	public void setMeetings(String meetings) { iMeetings = meetings; }

	public byte[] getAttachedFile() { return iAttachedFile; }
	public void setAttachedFile(byte[] attachedFile) { iAttachedFile = attachedFile; }

	public String getAttachedName() { return iAttachedName; }
	public void setAttachedName(String attachedName) { iAttachedName = attachedName; }

	public String getAttachedContentType() { return iAttachedContentType; }
	public void setAttachedContentType(String attachedContentType) { iAttachedContentType = attachedContentType; }

	public Event getEvent() { return iEvent; }
	public void setEvent(Event event) { iEvent = event; }

	public Set<Meeting> getAffectedMeetings() { return iAffectedMeetings; }
	public void setAffectedMeetings(Set<Meeting> affectedMeetings) { iAffectedMeetings = affectedMeetings; }
	public void addToaffectedMeetings(Meeting meeting) {
		if (iAffectedMeetings == null) iAffectedMeetings = new HashSet<Meeting>();
		iAffectedMeetings.add(meeting);
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof EventNote)) return false;
		if (getUniqueId() == null || ((EventNote)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((EventNote)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

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
