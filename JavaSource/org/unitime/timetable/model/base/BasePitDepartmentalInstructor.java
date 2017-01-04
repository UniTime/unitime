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
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.PitClassInstructor;
import org.unitime.timetable.model.PitDepartmentalInstructor;
import org.unitime.timetable.model.PointInTimeData;
import org.unitime.timetable.model.PositionType;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BasePitDepartmentalInstructor implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iExternalUniqueId;
	private String iCareerAcct;
	private String iFirstName;
	private String iMiddleName;
	private String iLastName;
	private String iEmail;

	private PositionType iPositionType;
	private Department iDepartment;
	private PointInTimeData iPointInTimeData;
	private DepartmentalInstructor iDepartmentalInstructor;
	private Set<PitClassInstructor> iPitClassesInstructing;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_EXTERNAL_UID = "externalUniqueId";
	public static String PROP_CAREER_ACCT = "careerAcct";
	public static String PROP_FNAME = "firstName";
	public static String PROP_MNAME = "middleName";
	public static String PROP_LNAME = "lastName";
	public static String PROP_EMAIL = "email";

	public BasePitDepartmentalInstructor() {
		initialize();
	}

	public BasePitDepartmentalInstructor(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public String getExternalUniqueId() { return iExternalUniqueId; }
	public void setExternalUniqueId(String externalUniqueId) { iExternalUniqueId = externalUniqueId; }

	public String getCareerAcct() { return iCareerAcct; }
	public void setCareerAcct(String careerAcct) { iCareerAcct = careerAcct; }

	public String getFirstName() { return iFirstName; }
	public void setFirstName(String firstName) { iFirstName = firstName; }

	public String getMiddleName() { return iMiddleName; }
	public void setMiddleName(String middleName) { iMiddleName = middleName; }

	public String getLastName() { return iLastName; }
	public void setLastName(String lastName) { iLastName = lastName; }

	public String getEmail() { return iEmail; }
	public void setEmail(String email) { iEmail = email; }

	public PositionType getPositionType() { return iPositionType; }
	public void setPositionType(PositionType positionType) { iPositionType = positionType; }

	public Department getDepartment() { return iDepartment; }
	public void setDepartment(Department department) { iDepartment = department; }

	public PointInTimeData getPointInTimeData() { return iPointInTimeData; }
	public void setPointInTimeData(PointInTimeData pointInTimeData) { iPointInTimeData = pointInTimeData; }

	public DepartmentalInstructor getDepartmentalInstructor() { return iDepartmentalInstructor; }
	public void setDepartmentalInstructor(DepartmentalInstructor departmentalInstructor) { iDepartmentalInstructor = departmentalInstructor; }

	public Set<PitClassInstructor> getPitClassesInstructing() { return iPitClassesInstructing; }
	public void setPitClassesInstructing(Set<PitClassInstructor> pitClassesInstructing) { iPitClassesInstructing = pitClassesInstructing; }
	public void addTopitClassesInstructing(PitClassInstructor pitClassInstructor) {
		if (iPitClassesInstructing == null) iPitClassesInstructing = new HashSet<PitClassInstructor>();
		iPitClassesInstructing.add(pitClassInstructor);
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof PitDepartmentalInstructor)) return false;
		if (getUniqueId() == null || ((PitDepartmentalInstructor)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((PitDepartmentalInstructor)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "PitDepartmentalInstructor["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "PitDepartmentalInstructor[" +
			"\n	CareerAcct: " + getCareerAcct() +
			"\n	Department: " + getDepartment() +
			"\n	DepartmentalInstructor: " + getDepartmentalInstructor() +
			"\n	Email: " + getEmail() +
			"\n	ExternalUniqueId: " + getExternalUniqueId() +
			"\n	FirstName: " + getFirstName() +
			"\n	LastName: " + getLastName() +
			"\n	MiddleName: " + getMiddleName() +
			"\n	PointInTimeData: " + getPointInTimeData() +
			"\n	PositionType: " + getPositionType() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
