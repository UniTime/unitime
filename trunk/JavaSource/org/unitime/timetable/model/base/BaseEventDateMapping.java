/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2010 - 2013, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.model.base;

import java.io.Serializable;

import org.unitime.timetable.model.EventDateMapping;
import org.unitime.timetable.model.Session;

/**
 * @author Tomas Muller
 */
public abstract class BaseEventDateMapping implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Integer iClassDateOffset;
	private Integer iEventDateOffset;
	private String iNote;

	private Session iSession;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_CLASS_DATE = "classDateOffset";
	public static String PROP_EVENT_DATE = "eventDateOffset";
	public static String PROP_NOTE = "note";

	public BaseEventDateMapping() {
		initialize();
	}

	public BaseEventDateMapping(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public Integer getClassDateOffset() { return iClassDateOffset; }
	public void setClassDateOffset(Integer classDateOffset) { iClassDateOffset = classDateOffset; }

	public Integer getEventDateOffset() { return iEventDateOffset; }
	public void setEventDateOffset(Integer eventDateOffset) { iEventDateOffset = eventDateOffset; }

	public String getNote() { return iNote; }
	public void setNote(String note) { iNote = note; }

	public Session getSession() { return iSession; }
	public void setSession(Session session) { iSession = session; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof EventDateMapping)) return false;
		if (getUniqueId() == null || ((EventDateMapping)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((EventDateMapping)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "EventDateMapping["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "EventDateMapping[" +
			"\n	ClassDateOffset: " + getClassDateOffset() +
			"\n	EventDateOffset: " + getEventDateOffset() +
			"\n	Note: " + getNote() +
			"\n	Session: " + getSession() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
