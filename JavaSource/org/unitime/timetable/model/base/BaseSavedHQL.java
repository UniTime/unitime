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
import org.unitime.timetable.model.SavedHQL;
import org.unitime.timetable.model.SavedHQLParameter;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseSavedHQL implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iName;
	private String iDescription;
	private String iQuery;
	private Integer iType;

	private Set<SavedHQLParameter> iParameters;

	public BaseSavedHQL() {
	}

	public BaseSavedHQL(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "saved_hql_id", strategy = "org.unitime.commons.hibernate.id.UniqueIdGenerator", parameters = {
		@Parameter(name = "sequence", value = "pref_group_seq")
	})
	@GeneratedValue(generator = "saved_hql_id")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "name", nullable = false, length = 100)
	public String getName() { return iName; }
	public void setName(String name) { iName = name; }

	@Column(name = "description", nullable = true, length = 1000)
	public String getDescription() { return iDescription; }
	public void setDescription(String description) { iDescription = description; }

	@Column(name = "query", nullable = false)
	public String getQuery() { return iQuery; }
	public void setQuery(String query) { iQuery = query; }

	@Column(name = "type", nullable = false, length = 10)
	public Integer getType() { return iType; }
	public void setType(Integer type) { iType = type; }

	@OneToMany(mappedBy = "savedHQL", cascade = {CascadeType.ALL})
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, include = "non-lazy")
	public Set<SavedHQLParameter> getParameters() { return iParameters; }
	public void setParameters(Set<SavedHQLParameter> parameters) { iParameters = parameters; }
	public void addToparameters(SavedHQLParameter savedHQLParameter) {
		if (iParameters == null) iParameters = new HashSet<SavedHQLParameter>();
		iParameters.add(savedHQLParameter);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof SavedHQL)) return false;
		if (getUniqueId() == null || ((SavedHQL)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((SavedHQL)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "SavedHQL["+getUniqueId()+" "+getName()+"]";
	}

	public String toDebugString() {
		return "SavedHQL[" +
			"\n	Description: " + getDescription() +
			"\n	Name: " + getName() +
			"\n	Query: " + getQuery() +
			"\n	Type: " + getType() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
