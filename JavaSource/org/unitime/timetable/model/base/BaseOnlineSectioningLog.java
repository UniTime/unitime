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
import java.util.Date;

import org.unitime.timetable.model.OnlineSectioningLog;
import org.unitime.timetable.model.Session;

/**
 * @author Tomas Muller
 */
public abstract class BaseOnlineSectioningLog implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Date iTimeStamp;
	private String iStudent;
	private String iOperation;
	private byte[] iAction;
	private Integer iResult;
	private String iUser;

	private Session iSession;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_TIME_STAMP = "timeStamp";
	public static String PROP_STUDENT = "student";
	public static String PROP_OPERATION = "operation";
	public static String PROP_ACTION = "action";
	public static String PROP_RESULT = "result";
	public static String PROP_USER_ID = "user";

	public BaseOnlineSectioningLog() {
		initialize();
	}

	public BaseOnlineSectioningLog(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public Date getTimeStamp() { return iTimeStamp; }
	public void setTimeStamp(Date timeStamp) { iTimeStamp = timeStamp; }

	public String getStudent() { return iStudent; }
	public void setStudent(String student) { iStudent = student; }

	public String getOperation() { return iOperation; }
	public void setOperation(String operation) { iOperation = operation; }

	public byte[] getAction() { return iAction; }
	public void setAction(byte[] action) { iAction = action; }

	public Integer getResult() { return iResult; }
	public void setResult(Integer result) { iResult = result; }

	public String getUser() { return iUser; }
	public void setUser(String user) { iUser = user; }

	public Session getSession() { return iSession; }
	public void setSession(Session session) { iSession = session; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof OnlineSectioningLog)) return false;
		if (getUniqueId() == null || ((OnlineSectioningLog)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((OnlineSectioningLog)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "OnlineSectioningLog["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "OnlineSectioningLog[" +
			"\n	Action: " + getAction() +
			"\n	Operation: " + getOperation() +
			"\n	Result: " + getResult() +
			"\n	Session: " + getSession() +
			"\n	Student: " + getStudent() +
			"\n	TimeStamp: " + getTimeStamp() +
			"\n	UniqueId: " + getUniqueId() +
			"\n	User: " + getUser() +
			"]";
	}
}
