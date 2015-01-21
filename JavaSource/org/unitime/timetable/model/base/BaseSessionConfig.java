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

import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SessionConfig;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseSessionConfig implements Serializable {
	private static final long serialVersionUID = 1L;

	private Session iSession;
	private String iKey;
	private String iValue;
	private String iDescription;


	public static String PROP_VALUE = "value";
	public static String PROP_DESCRIPTION = "description";

	public BaseSessionConfig() {
		initialize();
	}

	protected void initialize() {}

	public Session getSession() { return iSession; }
	public void setSession(Session session) { iSession = session; }

	public String getKey() { return iKey; }
	public void setKey(String key) { iKey = key; }

	public String getValue() { return iValue; }
	public void setValue(String value) { iValue = value; }

	public String getDescription() { return iDescription; }
	public void setDescription(String description) { iDescription = description; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof SessionConfig)) return false;
		SessionConfig sessionConfig = (SessionConfig)o;
		if (getSession() == null || sessionConfig.getSession() == null || !getSession().equals(sessionConfig.getSession())) return false;
		if (getKey() == null || sessionConfig.getKey() == null || !getKey().equals(sessionConfig.getKey())) return false;
		return true;
	}

	public int hashCode() {
		if (getSession() == null || getKey() == null) return super.hashCode();
		return getSession().hashCode() ^ getKey().hashCode();
	}

	public String toString() {
		return "SessionConfig[" + getSession() + ", " + getKey() + "]";
	}

	public String toDebugString() {
		return "SessionConfig[" +
			"\n	Description: " + getDescription() +
			"\n	Key: " + getKey() +
			"\n	Session: " + getSession() +
			"\n	Value: " + getValue() +
			"]";
	}
}
