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

import org.unitime.timetable.model.OnlineSectioningLog;
import org.unitime.timetable.model.Session;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
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
