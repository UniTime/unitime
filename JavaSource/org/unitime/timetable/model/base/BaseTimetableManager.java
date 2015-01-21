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

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.ManagerRole;
import org.unitime.timetable.model.ManagerSettings;
import org.unitime.timetable.model.SolverGroup;
import org.unitime.timetable.model.TimetableManager;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
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

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_EXTERNAL_UID = "externalUniqueId";
	public static String PROP_FIRST_NAME = "firstName";
	public static String PROP_MIDDLE_NAME = "middleName";
	public static String PROP_LAST_NAME = "lastName";
	public static String PROP_ACAD_TITLE = "academicTitle";
	public static String PROP_EMAIL_ADDRESS = "emailAddress";

	public BaseTimetableManager() {
		initialize();
	}

	public BaseTimetableManager(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public String getExternalUniqueId() { return iExternalUniqueId; }
	public void setExternalUniqueId(String externalUniqueId) { iExternalUniqueId = externalUniqueId; }

	public String getFirstName() { return iFirstName; }
	public void setFirstName(String firstName) { iFirstName = firstName; }

	public String getMiddleName() { return iMiddleName; }
	public void setMiddleName(String middleName) { iMiddleName = middleName; }

	public String getLastName() { return iLastName; }
	public void setLastName(String lastName) { iLastName = lastName; }

	public String getAcademicTitle() { return iAcademicTitle; }
	public void setAcademicTitle(String academicTitle) { iAcademicTitle = academicTitle; }

	public String getEmailAddress() { return iEmailAddress; }
	public void setEmailAddress(String emailAddress) { iEmailAddress = emailAddress; }

	public Set<ManagerSettings> getSettings() { return iSettings; }
	public void setSettings(Set<ManagerSettings> settings) { iSettings = settings; }
	public void addTosettings(ManagerSettings managerSettings) {
		if (iSettings == null) iSettings = new HashSet<ManagerSettings>();
		iSettings.add(managerSettings);
	}

	public Set<Department> getDepartments() { return iDepartments; }
	public void setDepartments(Set<Department> departments) { iDepartments = departments; }
	public void addTodepartments(Department department) {
		if (iDepartments == null) iDepartments = new HashSet<Department>();
		iDepartments.add(department);
	}

	public Set<ManagerRole> getManagerRoles() { return iManagerRoles; }
	public void setManagerRoles(Set<ManagerRole> managerRoles) { iManagerRoles = managerRoles; }
	public void addTomanagerRoles(ManagerRole managerRole) {
		if (iManagerRoles == null) iManagerRoles = new HashSet<ManagerRole>();
		iManagerRoles.add(managerRole);
	}

	public Set<SolverGroup> getSolverGroups() { return iSolverGroups; }
	public void setSolverGroups(Set<SolverGroup> solverGroups) { iSolverGroups = solverGroups; }
	public void addTosolverGroups(SolverGroup solverGroup) {
		if (iSolverGroups == null) iSolverGroups = new HashSet<SolverGroup>();
		iSolverGroups.add(solverGroup);
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof TimetableManager)) return false;
		if (getUniqueId() == null || ((TimetableManager)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((TimetableManager)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

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
