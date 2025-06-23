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
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToMany;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.unitime.commons.annotations.UniqueIdGenerator;
import org.unitime.timetable.model.SolverParameter;
import org.unitime.timetable.model.SolverPredefinedSetting;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseSolverPredefinedSetting implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iName;
	private String iDescription;
	private Integer iAppearance;

	private Set<SolverParameter> iParameters;

	public BaseSolverPredefinedSetting() {
	}

	public BaseSolverPredefinedSetting(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@UniqueIdGenerator(sequence = "solver_predef_setting_seq")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "name", nullable = true, length = 100)
	public String getName() { return iName; }
	public void setName(String name) { iName = name; }

	@Column(name = "description", nullable = true, length = 1000)
	public String getDescription() { return iDescription; }
	public void setDescription(String description) { iDescription = description; }

	@Column(name = "appearance", nullable = true, length = 2)
	public Integer getAppearance() { return iAppearance; }
	public void setAppearance(Integer appearance) { iAppearance = appearance; }

	@OneToMany(cascade = {CascadeType.ALL})
	@JoinColumn(name = "solver_predef_setting_id", nullable = true)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	public Set<SolverParameter> getParameters() { return iParameters; }
	public void setParameters(Set<SolverParameter> parameters) { iParameters = parameters; }
	public void addToParameters(SolverParameter solverParameter) {
		if (iParameters == null) iParameters = new HashSet<SolverParameter>();
		iParameters.add(solverParameter);
	}
	@Deprecated
	public void addToparameters(SolverParameter solverParameter) {
		addToParameters(solverParameter);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof SolverPredefinedSetting)) return false;
		if (getUniqueId() == null || ((SolverPredefinedSetting)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((SolverPredefinedSetting)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "SolverPredefinedSetting["+getUniqueId()+" "+getName()+"]";
	}

	public String toDebugString() {
		return "SolverPredefinedSetting[" +
			"\n	Appearance: " + getAppearance() +
			"\n	Description: " + getDescription() +
			"\n	Name: " + getName() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
