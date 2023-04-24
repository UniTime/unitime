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

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToMany;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
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
@MappedSuperclass
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

	public BasePitDepartmentalInstructor() {
	}

	public BasePitDepartmentalInstructor(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "pit_dept_instructor_id", strategy = "org.unitime.commons.hibernate.id.UniqueIdGenerator", parameters = {
		@Parameter(name = "sequence", value = "point_in_time_seq")
	})
	@GeneratedValue(generator = "pit_dept_instructor_id")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "external_uid", nullable = true, length = 40)
	public String getExternalUniqueId() { return iExternalUniqueId; }
	public void setExternalUniqueId(String externalUniqueId) { iExternalUniqueId = externalUniqueId; }

	@Column(name = "career_acct", nullable = true, length = 20)
	public String getCareerAcct() { return iCareerAcct; }
	public void setCareerAcct(String careerAcct) { iCareerAcct = careerAcct; }

	@Column(name = "fname", nullable = true, length = 100)
	public String getFirstName() { return iFirstName; }
	public void setFirstName(String firstName) { iFirstName = firstName; }

	@Column(name = "mname", nullable = true, length = 100)
	public String getMiddleName() { return iMiddleName; }
	public void setMiddleName(String middleName) { iMiddleName = middleName; }

	@Column(name = "lname", nullable = false, length = 100)
	public String getLastName() { return iLastName; }
	public void setLastName(String lastName) { iLastName = lastName; }

	@Column(name = "email", nullable = true, length = 200)
	public String getEmail() { return iEmail; }
	public void setEmail(String email) { iEmail = email; }

	@ManyToOne(optional = true)
	@JoinColumn(name = "pos_code_type", nullable = true)
	public PositionType getPositionType() { return iPositionType; }
	public void setPositionType(PositionType positionType) { iPositionType = positionType; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "department_id", nullable = false)
	public Department getDepartment() { return iDepartment; }
	public void setDepartment(Department department) { iDepartment = department; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "point_in_time_data_id", nullable = false)
	public PointInTimeData getPointInTimeData() { return iPointInTimeData; }
	public void setPointInTimeData(PointInTimeData pointInTimeData) { iPointInTimeData = pointInTimeData; }

	@ManyToOne(optional = true)
	@JoinColumn(name = "dept_instructor_id", nullable = true)
	public DepartmentalInstructor getDepartmentalInstructor() { return iDepartmentalInstructor; }
	public void setDepartmentalInstructor(DepartmentalInstructor departmentalInstructor) { iDepartmentalInstructor = departmentalInstructor; }

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "pitDepartmentalInstructor", cascade = {CascadeType.ALL})
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, include = "non-lazy")
	public Set<PitClassInstructor> getPitClassesInstructing() { return iPitClassesInstructing; }
	public void setPitClassesInstructing(Set<PitClassInstructor> pitClassesInstructing) { iPitClassesInstructing = pitClassesInstructing; }
	public void addTopitClassesInstructing(PitClassInstructor pitClassInstructor) {
		if (iPitClassesInstructing == null) iPitClassesInstructing = new HashSet<PitClassInstructor>();
		iPitClassesInstructing.add(pitClassInstructor);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof PitDepartmentalInstructor)) return false;
		if (getUniqueId() == null || ((PitDepartmentalInstructor)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((PitDepartmentalInstructor)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
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
