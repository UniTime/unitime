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
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToMany;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.unitime.commons.hibernate.id.UniqueIdGenerator;
import org.unitime.timetable.model.ManagerSettings;
import org.unitime.timetable.model.Settings;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseSettings implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iKey;
	private String iDefaultValue;
	private String iAllowedValues;
	private String iDescription;

	private Set<ManagerSettings> iManagerSettings;

	public BaseSettings() {
	}

	public BaseSettings(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "settings_id", type = UniqueIdGenerator.class, parameters = {
		@Parameter(name = "sequence", value = "settings_seq")
	})
	@GeneratedValue(generator = "settings_id")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "name", nullable = false, length = 30)
	public String getKey() { return iKey; }
	public void setKey(String key) { iKey = key; }

	@Column(name = "default_value", nullable = false, length = 100)
	public String getDefaultValue() { return iDefaultValue; }
	public void setDefaultValue(String defaultValue) { iDefaultValue = defaultValue; }

	@Column(name = "allowed_values", nullable = false, length = 500)
	public String getAllowedValues() { return iAllowedValues; }
	public void setAllowedValues(String allowedValues) { iAllowedValues = allowedValues; }

	@Column(name = "description", nullable = false, length = 100)
	public String getDescription() { return iDescription; }
	public void setDescription(String description) { iDescription = description; }

	@OneToMany
	@JoinColumn(name = "uniqueid", nullable = true)
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, includeLazy = false)
	public Set<ManagerSettings> getManagerSettings() { return iManagerSettings; }
	public void setManagerSettings(Set<ManagerSettings> managerSettings) { iManagerSettings = managerSettings; }
	public void addTomanagerSettings(ManagerSettings managerSettings) {
		if (iManagerSettings == null) iManagerSettings = new HashSet<ManagerSettings>();
		iManagerSettings.add(managerSettings);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof Settings)) return false;
		if (getUniqueId() == null || ((Settings)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((Settings)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "Settings["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "Settings[" +
			"\n	AllowedValues: " + getAllowedValues() +
			"\n	DefaultValue: " + getDefaultValue() +
			"\n	Description: " + getDescription() +
			"\n	Key: " + getKey() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
