/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
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

import org.unitime.timetable.model.UserData;

public abstract class BaseUserData implements Serializable {
	private static final long serialVersionUID = 1L;

	private String iExternalUniqueId;
	private String iName;
	private String iValue;


	public static String PROP_VALUE = "value";

	public BaseUserData() {
		initialize();
	}

	protected void initialize() {}

	public String getExternalUniqueId() { return iExternalUniqueId; }
	public void setExternalUniqueId(String externalUniqueId) { iExternalUniqueId = externalUniqueId; }

	public String getName() { return iName; }
	public void setName(String name) { iName = name; }

	public String getValue() { return iValue; }
	public void setValue(String value) { iValue = value; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof UserData)) return false;
		UserData userData = (UserData)o;
		if (getExternalUniqueId() == null || userData.getExternalUniqueId() == null || !getExternalUniqueId().equals(userData.getExternalUniqueId())) return false;
		if (getName() == null || userData.getName() == null || !getName().equals(userData.getName())) return false;
		return true;
	}

	public int hashCode() {
		if (getExternalUniqueId() == null || getName() == null) return super.hashCode();
		return getExternalUniqueId().hashCode() ^ getName().hashCode();
	}

	public String toString() {
		return "UserData[" + getExternalUniqueId() + ", " + getName() + "]";
	}

	public String toDebugString() {
		return "UserData[" +
			"\n	ExternalUniqueId: " + getExternalUniqueId() +
			"\n	Name: " + getName() +
			"\n	Value: " + getValue() +
			"]";
	}
}
