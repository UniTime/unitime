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

import org.unitime.timetable.model.EventContact;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseEventContact implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iExternalUniqueId;
	private String iEmailAddress;
	private String iPhone;
	private String iFirstName;
	private String iMiddleName;
	private String iLastName;
	private String iAcademicTitle;


	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_EXTERNAL_ID = "externalUniqueId";
	public static String PROP_EMAIL = "emailAddress";
	public static String PROP_PHONE = "phone";
	public static String PROP_FIRSTNAME = "firstName";
	public static String PROP_MIDDLENAME = "middleName";
	public static String PROP_LASTNAME = "lastName";
	public static String PROP_ACAD_TITLE = "academicTitle";

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

	public String getAcademicTitle() { return iAcademicTitle; }
	public void setAcademicTitle(String academicTitle) { iAcademicTitle = academicTitle; }

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
			"\n	AcademicTitle: " + getAcademicTitle() +
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
