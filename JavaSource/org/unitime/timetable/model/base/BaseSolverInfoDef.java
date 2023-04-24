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
import jakarta.persistence.MappedSuperclass;

import java.io.Serializable;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.unitime.timetable.model.SolverInfoDef;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseSolverInfoDef implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iName;
	private String iDescription;
	private String iImplementation;


	public BaseSolverInfoDef() {
	}

	public BaseSolverInfoDef(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "solver_info_def_id", strategy = "org.unitime.commons.hibernate.id.UniqueIdGenerator", parameters = {
		@Parameter(name = "sequence", value = "solver_info_def_seq")
	})
	@GeneratedValue(generator = "solver_info_def_id")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "name", nullable = true, length = 100)
	public String getName() { return iName; }
	public void setName(String name) { iName = name; }

	@Column(name = "description", nullable = true, length = 1000)
	public String getDescription() { return iDescription; }
	public void setDescription(String description) { iDescription = description; }

	@Column(name = "implementation", nullable = true, length = 250)
	public String getImplementation() { return iImplementation; }
	public void setImplementation(String implementation) { iImplementation = implementation; }

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof SolverInfoDef)) return false;
		if (getUniqueId() == null || ((SolverInfoDef)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((SolverInfoDef)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "SolverInfoDef["+getUniqueId()+" "+getName()+"]";
	}

	public String toDebugString() {
		return "SolverInfoDef[" +
			"\n	Description: " + getDescription() +
			"\n	Implementation: " + getImplementation() +
			"\n	Name: " + getName() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
