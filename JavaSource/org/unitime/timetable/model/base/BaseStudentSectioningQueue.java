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
	private String iData;


	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_SESSION_ID = "sessionId";
	public static String PROP_TIME_STAMP = "timeStamp";
	public static String PROP_TYPE = "type";
	public static String PROP_MESSAGE = "data";

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

	public String getData() { return iData; }
	public void setData(String data) { iData = data; }

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
			"\n	Data: " + getData() +
			"\n	SessionId: " + getSessionId() +
			"\n	TimeStamp: " + getTimeStamp() +
			"\n	Type: " + getType() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
