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

import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.MappedSuperclass;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.ConstraintInfo;
import org.unitime.timetable.model.SolverInfo;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseConstraintInfo extends SolverInfo implements Serializable {
	private static final long serialVersionUID = 1L;

	private Set<Assignment> iAssignments;

	public BaseConstraintInfo() {
	}

	public BaseConstraintInfo(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@ManyToMany
	@JoinTable(name = "constraint_info",
		joinColumns = { @JoinColumn(name = "solver_info_id") },
		inverseJoinColumns = { @JoinColumn(name = "assignment_id") })
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, includeLazy = false)
	public Set<Assignment> getAssignments() { return iAssignments; }
	public void setAssignments(Set<Assignment> assignments) { iAssignments = assignments; }
	public void addToassignments(Assignment assignment) {
		if (iAssignments == null) iAssignments = new HashSet<Assignment>();
		iAssignments.add(assignment);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof ConstraintInfo)) return false;
		if (getUniqueId() == null || ((ConstraintInfo)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((ConstraintInfo)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "ConstraintInfo["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "ConstraintInfo[" +
			"\n	Data: " + getData() +
			"\n	Definition: " + getDefinition() +
			"\n	Opt: " + getOpt() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
