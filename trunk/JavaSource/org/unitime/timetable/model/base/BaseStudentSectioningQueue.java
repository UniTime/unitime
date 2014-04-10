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
import java.util.Date;

import org.dom4j.Document;
import org.unitime.timetable.model.StudentSectioningQueue;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseStudentSectioningQueue implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Long iSessionId;
	private Date iTimeStamp;
	private Integer iType;
	private Document iMessage;


	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_SESSION_ID = "sessionId";
	public static String PROP_TIME_STAMP = "timeStamp";
	public static String PROP_TYPE = "type";
	public static String PROP_MESSAGE = "message";

	public BaseStudentSectioningQueue() {
		initialize();
	}

	public BaseStudentSectioningQueue(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public Long getSessionId() { return iSessionId; }
	public void setSessionId(Long sessionId) { iSessionId = sessionId; }

	public Date getTimeStamp() { return iTimeStamp; }
	public void setTimeStamp(Date timeStamp) { iTimeStamp = timeStamp; }

	public Integer getType() { return iType; }
	public void setType(Integer type) { iType = type; }

	public Document getMessage() { return iMessage; }
	public void setMessage(Document message) { iMessage = message; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof StudentSectioningQueue)) return false;
		if (getUniqueId() == null || ((StudentSectioningQueue)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((StudentSectioningQueue)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "StudentSectioningQueue["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "StudentSectioningQueue[" +
			"\n	Message: " + getMessage() +
			"\n	SessionId: " + getSessionId() +
			"\n	TimeStamp: " + getTimeStamp() +
			"\n	Type: " + getType() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
