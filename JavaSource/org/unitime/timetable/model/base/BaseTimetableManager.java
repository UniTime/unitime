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
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
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
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.ManagerRole;
import org.unitime.timetable.model.ManagerSettings;
import org.unitime.timetable.model.SolverGroup;
import org.unitime.timetable.model.TimetableManager;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseTimetableManager implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iExternalUniqueId;
	private String iFirstName;
	private String iMiddleName;
	private String iLastName;
	private String iAcademicTitle;
	private String iEmailAddress;

	private Set<ManagerSettings> iSettings;
	private Set<Department> iDepartments;
	private Set<ManagerRole> iManagerRoles;
	private Set<SolverGroup> iSolverGroups;

	public BaseTimetableManager() {
	}

	public BaseTimetableManager(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "timetable_manager_id", type = UniqueIdGenerator.class, parameters = {
		@Parameter(name = "sequence", value = "timetable_mgr_seq")
	})
	@GeneratedValue(generator = "timetable_manager_id")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "external_uid", nullable = true, length = 40)
	public String getExternalUniqueId() { return iExternalUniqueId; }
	public void setExternalUniqueId(String externalUniqueId) { iExternalUniqueId = externalUniqueId; }

	@Column(name = "first_name", nullable = false, length = 100)
	public String getFirstName() { return iFirstName; }
	public void setFirstName(String firstName) { iFirstName = firstName; }

	@Column(name = "middle_name", nullable = true, length = 100)
	public String getMiddleName() { return iMiddleName; }
	public void setMiddleName(String middleName) { iMiddleName = middleName; }

	@Column(name = "last_name", nullable = false, length = 100)
	public String getLastName() { return iLastName; }
	public void setLastName(String lastName) { iLastName = lastName; }

	@Column(name = "acad_title", nullable = true, length = 50)
	public String getAcademicTitle() { return iAcademicTitle; }
	public void setAcademicTitle(String academicTitle) { iAcademicTitle = academicTitle; }

	@Column(name = "email_address", nullable = true, length = 200)
	public String getEmailAddress() { return iEmailAddress; }
	public void setEmailAddress(String emailAddress) { iEmailAddress = emailAddress; }

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "manager", cascade = {CascadeType.ALL}, orphanRemoval = true)
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, includeLazy = false)
	public Set<ManagerSettings> getSettings() { return iSettings; }
	public void setSettings(Set<ManagerSettings> settings) { iSettings = settings; }
	public void addToSettings(ManagerSettings managerSettings) {
		if (iSettings == null) iSettings = new HashSet<ManagerSettings>();
		iSettings.add(managerSettings);
	}
	@Deprecated
	public void addTosettings(ManagerSettings managerSettings) {
		addToSettings(managerSettings);
	}

	@ManyToMany(mappedBy = "timetableManagers")
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, includeLazy = false)
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

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "timetableManager", cascade = {CascadeType.ALL}, orphanRemoval = true)
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, includeLazy = false)
	public Set<ManagerRole> getManagerRoles() { return iManagerRoles; }
	public void setManagerRoles(Set<ManagerRole> managerRoles) { iManagerRoles = managerRoles; }
	public void addToManagerRoles(ManagerRole managerRole) {
		if (iManagerRoles == null) iManagerRoles = new HashSet<ManagerRole>();
		iManagerRoles.add(managerRole);
	}
	@Deprecated
	public void addTomanagerRoles(ManagerRole managerRole) {
		addToManagerRoles(managerRole);
	}

	@ManyToMany(mappedBy = "timetableManagers")
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, includeLazy = false)
	public Set<SolverGroup> getSolverGroups() { return iSolverGroups; }
	public void setSolverGroups(Set<SolverGroup> solverGroups) { iSolverGroups = solverGroups; }
	public void addToSolverGroups(SolverGroup solverGroup) {
		if (iSolverGroups == null) iSolverGroups = new HashSet<SolverGroup>();
		iSolverGroups.add(solverGroup);
	}
	@Deprecated
	public void addTosolverGroups(SolverGroup solverGroup) {
		addToSolverGroups(solverGroup);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof TimetableManager)) return false;
		if (getUniqueId() == null || ((TimetableManager)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((TimetableManager)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "TimetableManager["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "TimetableManager[" +
			"\n	AcademicTitle: " + getAcademicTitle() +
			"\n	EmailAddress: " + getEmailAddress() +
			"\n	ExternalUniqueId: " + getExternalUniqueId() +
			"\n	FirstName: " + getFirstName() +
			"\n	LastName: " + getLastName() +
			"\n	MiddleName: " + getMiddleName() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
