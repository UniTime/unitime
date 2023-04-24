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

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToMany;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.unitime.timetable.model.Script;
import org.unitime.timetable.model.ScriptParameter;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseScript implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iName;
	private String iDescription;
	private String iEngine;
	private String iPermission;
	private String iScript;

	private Set<ScriptParameter> iParameters;

	public BaseScript() {
	}

	public BaseScript(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "script_id", strategy = "org.unitime.commons.hibernate.id.UniqueIdGenerator", parameters = {
		@Parameter(name = "sequence", value = "pref_group_seq")
	})
	@GeneratedValue(generator = "script_id")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "name", nullable = false, length = 128)
	public String getName() { return iName; }
	public void setName(String name) { iName = name; }

	@Column(name = "description", nullable = true, length = 1024)
	public String getDescription() { return iDescription; }
	public void setDescription(String description) { iDescription = description; }

	@Column(name = "engine", nullable = false, length = 32)
	public String getEngine() { return iEngine; }
	public void setEngine(String engine) { iEngine = engine; }

	@Column(name = "permission", nullable = true, length = 128)
	public String getPermission() { return iPermission; }
	public void setPermission(String permission) { iPermission = permission; }

	@Column(name = "script", nullable = false)
	public String getScript() { return iScript; }
	public void setScript(String script) { iScript = script; }

	@OneToMany(mappedBy = "script", cascade = {CascadeType.ALL})
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, include = "non-lazy")
	public Set<ScriptParameter> getParameters() { return iParameters; }
	public void setParameters(Set<ScriptParameter> parameters) { iParameters = parameters; }
	public void addToparameters(ScriptParameter scriptParameter) {
		if (iParameters == null) iParameters = new HashSet<ScriptParameter>();
		iParameters.add(scriptParameter);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof Script)) return false;
		if (getUniqueId() == null || ((Script)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((Script)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "Script["+getUniqueId()+" "+getName()+"]";
	}

	public String toDebugString() {
		return "Script[" +
			"\n	Description: " + getDescription() +
			"\n	Engine: " + getEngine() +
			"\n	Name: " + getName() +
			"\n	Permission: " + getPermission() +
			"\n	Script: " + getScript() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
