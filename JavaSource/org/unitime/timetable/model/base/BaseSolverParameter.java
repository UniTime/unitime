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
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

import java.io.Serializable;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.unitime.timetable.model.SolverParameter;
import org.unitime.timetable.model.SolverParameterDef;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseSolverParameter implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iValue;

	private SolverParameterDef iDefinition;

	public BaseSolverParameter() {
	}

	public BaseSolverParameter(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "solver_parameter_id", strategy = "org.unitime.commons.hibernate.id.UniqueIdGenerator", parameters = {
		@Parameter(name = "sequence", value = "solver_parameter_seq")
	})
	@GeneratedValue(generator = "solver_parameter_id")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "value", nullable = true, length = 2048)
	public String getValue() { return iValue; }
	public void setValue(String value) { iValue = value; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "solver_param_def_id", nullable = false)
	public SolverParameterDef getDefinition() { return iDefinition; }
	public void setDefinition(SolverParameterDef definition) { iDefinition = definition; }

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof SolverParameter)) return false;
		if (getUniqueId() == null || ((SolverParameter)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((SolverParameter)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "SolverParameter["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "SolverParameter[" +
			"\n	Definition: " + getDefinition() +
			"\n	UniqueId: " + getUniqueId() +
			"\n	Value: " + getValue() +
			"]";
	}
}
