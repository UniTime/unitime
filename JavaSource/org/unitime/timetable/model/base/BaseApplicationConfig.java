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

import org.unitime.timetable.model.ApplicationConfig;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseApplicationConfig implements Serializable {
	private static final long serialVersionUID = 1L;

	private String iKey;
	private String iValue;
	private String iDescription;


	public BaseApplicationConfig() {
	}

	public BaseApplicationConfig(String key) {
		setKey(key);
	}


	@Id
	@Column(name="name")
	public String getKey() { return iKey; }
	public void setKey(String key) { iKey = key; }

	@Column(name = "value", nullable = true, length = 4000)
	public String getValue() { return iValue; }
	public void setValue(String value) { iValue = value; }

	@Column(name = "description", nullable = true, length = 500)
	public String getDescription() { return iDescription; }
	public void setDescription(String description) { iDescription = description; }

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof ApplicationConfig)) return false;
		if (getKey() == null || ((ApplicationConfig)o).getKey() == null) return false;
		return getKey().equals(((ApplicationConfig)o).getKey());
	}

	@Override
	public int hashCode() {
		if (getKey() == null) return super.hashCode();
		return getKey().hashCode();
	}

	@Override
	public String toString() {
		return "ApplicationConfig["+getKey()+"]";
	}

	public String toDebugString() {
		return "ApplicationConfig[" +
			"\n	Description: " + getDescription() +
			"\n	Key: " + getKey() +
			"\n	Value: " + getValue() +
			"]";
	}
}
