/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2010 - 2014, UniTime LLC, and individual contributors
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

import org.unitime.timetable.model.History;
import org.unitime.timetable.model.Session;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseHistory implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iOldValue;
	private String iNewValue;
	private Long iSessionId;

	private Session iSession;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_OLD_VALUE = "oldValue";
	public static String PROP_NEW_VALUE = "newValue";
	public static String PROP_SESSION_ID = "sessionId";

	public BaseHistory() {
		initialize();
	}

	public BaseHistory(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public String getOldValue() { return iOldValue; }
	public void setOldValue(String oldValue) { iOldValue = oldValue; }

	public String getNewValue() { return iNewValue; }
	public void setNewValue(String newValue) { iNewValue = newValue; }

	public Long getSessionId() { return iSessionId; }
	public void setSessionId(Long sessionId) { iSessionId = sessionId; }

	public Session getSession() { return iSession; }
	public void setSession(Session session) { iSession = session; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof History)) return false;
		if (getUniqueId() == null || ((History)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((History)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "History["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "History[" +
			"\n	NewValue: " + getNewValue() +
			"\n	OldValue: " + getOldValue() +
			"\n	Session: " + getSession() +
			"\n	SessionId: " + getSessionId() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
