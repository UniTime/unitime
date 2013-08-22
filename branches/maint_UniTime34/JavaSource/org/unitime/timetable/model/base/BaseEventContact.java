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

import org.unitime.timetable.model.EventContact;

public abstract class BaseEventContact implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iExternalUniqueId;
	private String iEmailAddress;
	private String iPhone;
	private String iFirstName;
	private String iMiddleName;
	private String iLastName;


	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_EXTERNAL_ID = "externalUniqueId";
	public static String PROP_EMAIL = "emailAddress";
	public static String PROP_PHONE = "phone";
	public static String PROP_FIRSTNAME = "firstName";
	public static String PROP_MIDDLENAME = "middleName";
	public static String PROP_LASTNAME = "lastName";

	public BaseEventContact() {
		initialize();
	}

	public BaseEventContact(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public String getExternalUniqueId() { return iExternalUniqueId; }
	public void setExternalUniqueId(String externalUniqueId) { iExternalUniqueId = externalUniqueId; }

	public String getEmailAddress() { return iEmailAddress; }
	public void setEmailAddress(String emailAddress) { iEmailAddress = emailAddress; }

	public String getPhone() { return iPhone; }
	public void setPhone(String phone) { iPhone = phone; }

	public String getFirstName() { return iFirstName; }
	public void setFirstName(String firstName) { iFirstName = firstName; }

	public String getMiddleName() { return iMiddleName; }
	public void setMiddleName(String middleName) { iMiddleName = middleName; }

	public String getLastName() { return iLastName; }
	public void setLastName(String lastName) { iLastName = lastName; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof EventContact)) return false;
		if (getUniqueId() == null || ((EventContact)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((EventContact)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "EventContact["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "EventContact[" +
			"\n	EmailAddress: " + getEmailAddress() +
			"\n	ExternalUniqueId: " + getExternalUniqueId() +
			"\n	FirstName: " + getFirstName() +
			"\n	LastName: " + getLastName() +
			"\n	MiddleName: " + getMiddleName() +
			"\n	Phone: " + getPhone() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
