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

import org.unitime.timetable.model.PositionType;
import org.unitime.timetable.model.Staff;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseStaff implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iExternalUniqueId;
	private String iFirstName;
	private String iMiddleName;
	private String iLastName;
	private String iDept;
	private String iEmail;
	private String iAcademicTitle;
	private String iCampus;

	private PositionType iPositionType;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_EXTERNAL_UID = "externalUniqueId";
	public static String PROP_FNAME = "firstName";
	public static String PROP_MNAME = "middleName";
	public static String PROP_LNAME = "lastName";
	public static String PROP_DEPT = "dept";
	public static String PROP_EMAIL = "email";
	public static String PROP_ACAD_TITLE = "academicTitle";
	public static String PROP_CAMPUS = "campus";

	public BaseStaff() {
		initialize();
	}

	public BaseStaff(Long uniqueId) {
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

	public String getDept() { return iDept; }
	public void setDept(String dept) { iDept = dept; }

	public String getEmail() { return iEmail; }
	public void setEmail(String email) { iEmail = email; }

	public String getAcademicTitle() { return iAcademicTitle; }
	public void setAcademicTitle(String academicTitle) { iAcademicTitle = academicTitle; }

	public String getCampus() { return iCampus; }
	public void setCampus(String campus) { iCampus = campus; }

	public PositionType getPositionType() { return iPositionType; }
	public void setPositionType(PositionType positionType) { iPositionType = positionType; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof Staff)) return false;
		if (getUniqueId() == null || ((Staff)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((Staff)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "Staff["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "Staff[" +
			"\n	AcademicTitle: " + getAcademicTitle() +
			"\n	Campus: " + getCampus() +
			"\n	Dept: " + getDept() +
			"\n	Email: " + getEmail() +
			"\n	ExternalUniqueId: " + getExternalUniqueId() +
			"\n	FirstName: " + getFirstName() +
			"\n	LastName: " + getLastName() +
			"\n	MiddleName: " + getMiddleName() +
			"\n	PositionType: " + getPositionType() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
