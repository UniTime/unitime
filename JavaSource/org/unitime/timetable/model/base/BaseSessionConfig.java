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

import javax.persistence.Column;
import javax.persistence.IdClass;
import javax.persistence.MappedSuperclass;

import org.unitime.timetable.model.SessionConfig;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
@IdClass(SessionConfigId.class)
public abstract class BaseSessionConfig extends SessionConfigId {
	private static final long serialVersionUID = 1L;

	private String iValue;
	private String iDescription;



	@Column(name = "value", nullable = true, length = 4000)
	public String getValue() { return iValue; }
	public void setValue(String value) { iValue = value; }

	@Column(name = "description", nullable = true, length = 500)
	public String getDescription() { return iDescription; }
	public void setDescription(String description) { iDescription = description; }

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof SessionConfig)) return false;
		SessionConfig sessionConfig = (SessionConfig)o;
		if (getSession() == null || sessionConfig.getSession() == null || !getSession().equals(sessionConfig.getSession())) return false;
		if (getKey() == null || sessionConfig.getKey() == null || !getKey().equals(sessionConfig.getKey())) return false;
		return true;
	}

	@Override
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
