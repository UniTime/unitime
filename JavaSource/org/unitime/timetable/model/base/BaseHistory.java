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
