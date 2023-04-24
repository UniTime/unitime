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

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;

import java.io.Serializable;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.unitime.timetable.model.PositionType;
import org.unitime.timetable.model.Staff;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
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

	public BaseStaff() {
	}

	public BaseStaff(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "staff_id", strategy = "org.unitime.commons.hibernate.id.UniqueIdGenerator", parameters = {
		@Parameter(name = "sequence", value = "staff_seq")
	})
	@GeneratedValue(generator = "staff_id")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "external_uid", nullable = true, length = 40)
	public String getExternalUniqueId() { return iExternalUniqueId; }
	public void setExternalUniqueId(String externalUniqueId) { iExternalUniqueId = externalUniqueId; }

	@Column(name = "fname", nullable = true, length = 100)
	public String getFirstName() { return iFirstName; }
	public void setFirstName(String firstName) { iFirstName = firstName; }

	@Column(name = "mname", nullable = true, length = 100)
	public String getMiddleName() { return iMiddleName; }
	public void setMiddleName(String middleName) { iMiddleName = middleName; }

	@Column(name = "lname", nullable = false, length = 100)
	public String getLastName() { return iLastName; }
	public void setLastName(String lastName) { iLastName = lastName; }

	@Column(name = "dept", nullable = true, length = 50)
	public String getDept() { return iDept; }
	public void setDept(String dept) { iDept = dept; }

	@Column(name = "email", nullable = true, length = 200)
	public String getEmail() { return iEmail; }
	public void setEmail(String email) { iEmail = email; }

	@Column(name = "acad_title", nullable = true, length = 50)
	public String getAcademicTitle() { return iAcademicTitle; }
	public void setAcademicTitle(String academicTitle) { iAcademicTitle = academicTitle; }

	@Column(name = "campus", nullable = true, length = 20)
	public String getCampus() { return iCampus; }
	public void setCampus(String campus) { iCampus = campus; }

	@ManyToOne(optional = true)
	@JoinColumn(name = "pos_type", nullable = true)
	public PositionType getPositionType() { return iPositionType; }
	public void setPositionType(PositionType positionType) { iPositionType = positionType; }

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof Staff)) return false;
		if (getUniqueId() == null || ((Staff)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((Staff)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
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
