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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;

import java.io.Serializable;

import org.unitime.commons.annotations.UniqueIdGenerator;
import org.unitime.timetable.model.SolverInfo;
import org.unitime.timetable.model.SolverInfoDef;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseSolverInfo implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private byte[] iData;
	private String iOpt;

	private SolverInfoDef iDefinition;

	public BaseSolverInfo() {
	}

	public BaseSolverInfo(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@UniqueIdGenerator(sequence = "solver_info_seq")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "value", nullable = false)
	public byte[] getData() { return iData; }
	public void setData(byte[] data) { iData = data; }

	@Column(name = "opt", nullable = true, length = 250)
	public String getOpt() { return iOpt; }
	public void setOpt(String opt) { iOpt = opt; }

	@ManyToOne(optional = true)
	@JoinColumn(name = "solver_info_def_id", nullable = true)
	public SolverInfoDef getDefinition() { return iDefinition; }
	public void setDefinition(SolverInfoDef definition) { iDefinition = definition; }

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof SolverInfo)) return false;
		if (getUniqueId() == null || ((SolverInfo)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((SolverInfo)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "SolverInfo["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "SolverInfo[" +
			"\n	Data: " + getData() +
			"\n	Definition: " + getDefinition() +
			"\n	Opt: " + getOpt() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
