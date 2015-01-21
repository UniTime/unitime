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
