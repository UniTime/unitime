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
import java.util.Date;

import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.EventNote;

public abstract class BaseEventNote implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Integer iNoteType;
	private String iTextNote;
	private Date iTimeStamp;
	private String iUser;
	private String iMeetings;

	private Event iEvent;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_NOTE_TYPE = "noteType";
	public static String PROP_TEXT_NOTE = "textNote";
	public static String PROP_TIME_STAMP = "timeStamp";
	public static String PROP_UNAME = "user";
	public static String PROP_MEETINGS = "meetings";

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

	public String getMeetings() { return iMeetings; }
	public void setMeetings(String meetings) { iMeetings = meetings; }

	public Event getEvent() { return iEvent; }
	public void setEvent(Event event) { iEvent = event; }

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
			"\n	Event: " + getEvent() +
			"\n	Meetings: " + getMeetings() +
			"\n	NoteType: " + getNoteType() +
			"\n	TextNote: " + getTextNote() +
			"\n	TimeStamp: " + getTimeStamp() +
			"\n	UniqueId: " + getUniqueId() +
			"\n	User: " + getUser() +
			"]";
	}
}
