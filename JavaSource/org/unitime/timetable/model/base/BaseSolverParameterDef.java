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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;

import java.io.Serializable;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.unitime.commons.hibernate.id.UniqueIdGenerator;
import org.unitime.timetable.model.SolverParameterDef;
import org.unitime.timetable.model.SolverParameterGroup;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseSolverParameterDef implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iName;
	private String iDefault;
	private String iDescription;
	private String iType;
	private Integer iOrder;
	private Boolean iVisible;

	private SolverParameterGroup iGroup;

	public BaseSolverParameterDef() {
	}

	public BaseSolverParameterDef(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "solver_parameter_def_id", type = UniqueIdGenerator.class, parameters = {
		@Parameter(name = "sequence", value = "solver_parameter_def_seq")
	})
	@GeneratedValue(generator = "solver_parameter_def_id")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "name", nullable = true, length = 100)
	public String getName() { return iName; }
	public void setName(String name) { iName = name; }

	@Column(name = "default_value", nullable = true, length = 2048)
	public String getDefault() { return iDefault; }
	public void setDefault(String defaultValue) { iDefault = defaultValue; }

	@Column(name = "description", nullable = true, length = 1000)
	public String getDescription() { return iDescription; }
	public void setDescription(String description) { iDescription = description; }

	@Column(name = "type", nullable = true, length = 1000)
	public String getType() { return iType; }
	public void setType(String type) { iType = type; }

	@Column(name = "ord", nullable = true, length = 4)
	public Integer getOrder() { return iOrder; }
	public void setOrder(Integer order) { iOrder = order; }

	@Column(name = "visible", nullable = true)
	public Boolean isVisible() { return iVisible; }
	@Transient
	public Boolean getVisible() { return iVisible; }
	public void setVisible(Boolean visible) { iVisible = visible; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "solver_param_group_id", nullable = false)
	public SolverParameterGroup getGroup() { return iGroup; }
	public void setGroup(SolverParameterGroup group) { iGroup = group; }

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof SolverParameterDef)) return false;
		if (getUniqueId() == null || ((SolverParameterDef)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((SolverParameterDef)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "SolverParameterDef["+getUniqueId()+" "+getName()+"]";
	}

	public String toDebugString() {
		return "SolverParameterDef[" +
			"\n	Default: " + getDefault() +
			"\n	Description: " + getDescription() +
			"\n	Group: " + getGroup() +
			"\n	Name: " + getName() +
			"\n	Order: " + getOrder() +
			"\n	Type: " + getType() +
			"\n	UniqueId: " + getUniqueId() +
			"\n	Visible: " + getVisible() +
			"]";
	}
}
