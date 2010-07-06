/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.model.base;

import java.io.Serializable;

import org.unitime.timetable.model.Roles;

public abstract class BaseRoles implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iRoleId;
	private String iReference;
	private String iAbbv;


	public static String PROP_ROLE_ID = "roleId";
	public static String PROP_REFERENCE = "reference";
	public static String PROP_ABBV = "abbv";

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
			"\n	Reference: " + getReference() +
			"\n	RoleId: " + getRoleId() +
			"]";
	}
}
