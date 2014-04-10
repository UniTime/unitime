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

import org.unitime.timetable.model.QueryLog;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseQueryLog implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Date iTimeStamp;
	private Long iTimeSpent;
	private String iUri;
	private Integer iType;
	private String iSessionId;
	private String iUid;
	private String iQuery;
	private String iException;


	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_TIME_STAMP = "timeStamp";
	public static String PROP_TIME_SPENT = "timeSpent";
	public static String PROP_URI = "uri";
	public static String PROP_TYPE = "type";
	public static String PROP_SESSION_ID = "sessionId";
	public static String PROP_USERID = "uid";
	public static String PROP_QUERY = "query";
	public static String PROP_EXCEPTION = "exception";

	public BaseQueryLog() {
		initialize();
	}

	public BaseQueryLog(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public Date getTimeStamp() { return iTimeStamp; }
	public void setTimeStamp(Date timeStamp) { iTimeStamp = timeStamp; }

	public Long getTimeSpent() { return iTimeSpent; }
	public void setTimeSpent(Long timeSpent) { iTimeSpent = timeSpent; }

	public String getUri() { return iUri; }
	public void setUri(String uri) { iUri = uri; }

	public Integer getType() { return iType; }
	public void setType(Integer type) { iType = type; }

	public String getSessionId() { return iSessionId; }
	public void setSessionId(String sessionId) { iSessionId = sessionId; }

	public String getUid() { return iUid; }
	public void setUid(String uid) { iUid = uid; }

	public String getQuery() { return iQuery; }
	public void setQuery(String query) { iQuery = query; }

	public String getException() { return iException; }
	public void setException(String exception) { iException = exception; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof QueryLog)) return false;
		if (getUniqueId() == null || ((QueryLog)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((QueryLog)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "QueryLog["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "QueryLog[" +
			"\n	Exception: " + getException() +
			"\n	Query: " + getQuery() +
			"\n	SessionId: " + getSessionId() +
			"\n	TimeSpent: " + getTimeSpent() +
			"\n	TimeStamp: " + getTimeStamp() +
			"\n	Type: " + getType() +
			"\n	Uid: " + getUid() +
			"\n	UniqueId: " + getUniqueId() +
			"\n	Uri: " + getUri() +
			"]";
	}
}
