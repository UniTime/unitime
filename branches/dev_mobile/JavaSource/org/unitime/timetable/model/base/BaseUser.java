/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2010 - 2014, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.model.base;

import java.io.Serializable;

import org.unitime.timetable.model.User;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseUser implements Serializable {
	private static final long serialVersionUID = 1L;

	private String iUsername;
	private String iPassword;
	private String iExternalUniqueId;


	public static String PROP_USERNAME = "username";
	public static String PROP_PASSWORD = "password";
	public static String PROP_EXTERNAL_UID = "externalUniqueId";

	public BaseUser() {
		initialize();
	}

	public BaseUser(String username) {
		setUsername(username);
		initialize();
	}

	protected void initialize() {}

	public String getUsername() { return iUsername; }
	public void setUsername(String username) { iUsername = username; }

	public String getPassword() { return iPassword; }
	public void setPassword(String password) { iPassword = password; }

	public String getExternalUniqueId() { return iExternalUniqueId; }
	public void setExternalUniqueId(String externalUniqueId) { iExternalUniqueId = externalUniqueId; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof User)) return false;
		if (getUsername() == null || ((User)o).getUsername() == null) return false;
		return getUsername().equals(((User)o).getUsername());
	}

	public int hashCode() {
		if (getUsername() == null) return super.hashCode();
		return getUsername().hashCode();
	}

	public String toString() {
		return "User["+getUsername()+"]";
	}

	public String toDebugString() {
		return "User[" +
			"\n	ExternalUniqueId: " + getExternalUniqueId() +
			"\n	Password: " + getPassword() +
			"\n	Username: " + getUsername() +
			"]";
	}
}
