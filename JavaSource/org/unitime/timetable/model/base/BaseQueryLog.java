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

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

import java.io.Serializable;
import java.util.Date;

import org.unitime.commons.annotations.UniqueIdGenerator;
import org.unitime.timetable.model.QueryLog;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
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


	public BaseQueryLog() {
	}

	public BaseQueryLog(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@UniqueIdGenerator(sequence = "pref_group_seq")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "time_stamp", nullable = false)
	public Date getTimeStamp() { return iTimeStamp; }
	public void setTimeStamp(Date timeStamp) { iTimeStamp = timeStamp; }

	@Column(name = "time_spent", nullable = false)
	public Long getTimeSpent() { return iTimeSpent; }
	public void setTimeSpent(Long timeSpent) { iTimeSpent = timeSpent; }

	@Column(name = "uri", nullable = false, length = 255)
	public String getUri() { return iUri; }
	public void setUri(String uri) { iUri = uri; }

	@Column(name = "type", nullable = false)
	public Integer getType() { return iType; }
	public void setType(Integer type) { iType = type; }

	@Column(name = "session_id", nullable = true, length = 32)
	public String getSessionId() { return iSessionId; }
	public void setSessionId(String sessionId) { iSessionId = sessionId; }

	@Column(name = "userid", nullable = true, length = 40)
	public String getUid() { return iUid; }
	public void setUid(String uid) { iUid = uid; }

	@Column(name = "query", nullable = true)
	public String getQuery() { return iQuery; }
	public void setQuery(String query) { iQuery = query; }

	@Column(name = "exception", nullable = true)
	public String getException() { return iException; }
	public void setException(String exception) { iException = exception; }

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof QueryLog)) return false;
		if (getUniqueId() == null || ((QueryLog)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((QueryLog)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
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
