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

import org.unitime.timetable.model.Roles;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseRoles implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iRoleId;
	private String iReference;
	private String iAbbv;
	private Boolean iManager;
	private Boolean iEnabled;
	private Boolean iInstructor;

	private Set<String> iRights;

	public static String PROP_ROLE_ID = "roleId";
	public static String PROP_REFERENCE = "reference";
	public static String PROP_ABBV = "abbv";
	public static String PROP_MANAGER = "manager";
	public static String PROP_ENABLED = "enabled";
	public static String PROP_INSTRUCTOR = "instructor";

	public BaseRoles() {
		initialize();
	}

	public BaseRoles(Long roleId) {
		setRoleId(roleId);
		initialize();
	}

	protected void initialize() {}

	public Long getRoleId() { return iRoleId; }
	public void setRoleId(Long roleId) { iRoleId = roleId; }

	public String getReference() { return iReference; }
	public void setReference(String reference) { iReference = reference; }

	public String getAbbv() { return iAbbv; }
	public void setAbbv(String abbv) { iAbbv = abbv; }

	public Boolean isManager() { return iManager; }
	public Boolean getManager() { return iManager; }
	public void setManager(Boolean manager) { iManager = manager; }

	public Boolean isEnabled() { return iEnabled; }
	public Boolean getEnabled() { return iEnabled; }
	public void setEnabled(Boolean enabled) { iEnabled = enabled; }

	public Boolean isInstructor() { return iInstructor; }
	public Boolean getInstructor() { return iInstructor; }
	public void setInstructor(Boolean instructor) { iInstructor = instructor; }

	public Set<String> getRights() { return iRights; }
	public void setRights(Set<String> rights) { iRights = rights; }
	public void addTorights(String string) {
		if (iRights == null) iRights = new HashSet<String>();
		iRights.add(string);
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof Roles)) return false;
		if (getRoleId() == null || ((Roles)o).getRoleId() == null) return false;
		return getRoleId().equals(((Roles)o).getRoleId());
	}

	public int hashCode() {
		if (getRoleId() == null) return super.hashCode();
		return getRoleId().hashCode();
	}

	public String toString() {
		return "Roles["+getRoleId()+"]";
	}

	public String toDebugString() {
		return "Roles[" +
			"\n	Abbv: " + getAbbv() +
			"\n	Enabled: " + getEnabled() +
			"\n	Instructor: " + getInstructor() +
			"\n	Manager: " + getManager() +
			"\n	Reference: " + getReference() +
			"\n	RoleId: " + getRoleId() +
			"]";
	}
}
