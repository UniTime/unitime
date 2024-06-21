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
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToMany;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.unitime.commons.annotations.UniqueIdGenerator;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Solution;
import org.unitime.timetable.model.SolverGroup;
import org.unitime.timetable.model.TimetableManager;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseSolverGroup implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iName;
	private String iAbbv;

	private Session iSession;
	private Set<TimetableManager> iTimetableManagers;
	private Set<Department> iDepartments;
	private Set<Solution> iSolutions;

	public BaseSolverGroup() {
	}

	public BaseSolverGroup(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@UniqueIdGenerator(sequence = "solver_group_seq")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "name", nullable = false, length = 50)
	public String getName() { return iName; }
	public void setName(String name) { iName = name; }

	@Column(name = "abbv", nullable = false, length = 50)
	public String getAbbv() { return iAbbv; }
	public void setAbbv(String abbv) { iAbbv = abbv; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "session_id", nullable = false)
	public Session getSession() { return iSession; }
	public void setSession(Session session) { iSession = session; }

	@ManyToMany
	@JoinTable(name = "solver_gr_to_tt_mgr",
		joinColumns = { @JoinColumn(name = "solver_group_id") },
		inverseJoinColumns = { @JoinColumn(name = "timetable_mgr_id") })
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	public Set<TimetableManager> getTimetableManagers() { return iTimetableManagers; }
	public void setTimetableManagers(Set<TimetableManager> timetableManagers) { iTimetableManagers = timetableManagers; }
	public void addToTimetableManagers(TimetableManager timetableManager) {
		if (iTimetableManagers == null) iTimetableManagers = new HashSet<TimetableManager>();
		iTimetableManagers.add(timetableManager);
	}
	@Deprecated
	public void addTotimetableManagers(TimetableManager timetableManager) {
		addToTimetableManagers(timetableManager);
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "solverGroup")
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	public Set<Department> getDepartments() { return iDepartments; }
	public void setDepartments(Set<Department> departments) { iDepartments = departments; }
	public void addToDepartments(Department department) {
		if (iDepartments == null) iDepartments = new HashSet<Department>();
		iDepartments.add(department);
	}
	@Deprecated
	public void addTodepartments(Department department) {
		addToDepartments(department);
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "owner", cascade = {CascadeType.ALL}, orphanRemoval = true)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	public Set<Solution> getSolutions() { return iSolutions; }
	public void setSolutions(Set<Solution> solutions) { iSolutions = solutions; }
	public void addToSolutions(Solution solution) {
		if (iSolutions == null) iSolutions = new HashSet<Solution>();
		iSolutions.add(solution);
	}
	@Deprecated
	public void addTosolutions(Solution solution) {
		addToSolutions(solution);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof SolverGroup)) return false;
		if (getUniqueId() == null || ((SolverGroup)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((SolverGroup)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "SolverGroup["+getUniqueId()+" "+getName()+"]";
	}

	public String toDebugString() {
		return "SolverGroup[" +
			"\n	Abbv: " + getAbbv() +
			"\n	Name: " + getName() +
			"\n	Session: " + getSession() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
