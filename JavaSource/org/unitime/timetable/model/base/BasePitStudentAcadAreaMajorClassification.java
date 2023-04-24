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

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

import java.io.Serializable;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.unitime.timetable.model.AcademicArea;
import org.unitime.timetable.model.AcademicClassification;
import org.unitime.timetable.model.PitStudent;
import org.unitime.timetable.model.PitStudentAcadAreaMajorClassification;
import org.unitime.timetable.model.PosMajor;
import org.unitime.timetable.model.PosMajorConcentration;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BasePitStudentAcadAreaMajorClassification implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Double iWeight;

	private PitStudent iPitStudent;
	private AcademicArea iAcademicArea;
	private AcademicClassification iAcademicClassification;
	private PosMajor iMajor;
	private PosMajorConcentration iConcentration;

	public BasePitStudentAcadAreaMajorClassification() {
	}

	public BasePitStudentAcadAreaMajorClassification(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "pit_stu_aa_major_clasf_id", strategy = "org.unitime.commons.hibernate.id.UniqueIdGenerator", parameters = {
		@Parameter(name = "sequence", value = "point_in_time_seq")
	})
	@GeneratedValue(generator = "pit_stu_aa_major_clasf_id")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "weight", nullable = true)
	public Double getWeight() { return iWeight; }
	public void setWeight(Double weight) { iWeight = weight; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "pit_student_id", nullable = false)
	public PitStudent getPitStudent() { return iPitStudent; }
	public void setPitStudent(PitStudent pitStudent) { iPitStudent = pitStudent; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "acad_area_id", nullable = false)
	public AcademicArea getAcademicArea() { return iAcademicArea; }
	public void setAcademicArea(AcademicArea academicArea) { iAcademicArea = academicArea; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "acad_clasf_id", nullable = false)
	public AcademicClassification getAcademicClassification() { return iAcademicClassification; }
	public void setAcademicClassification(AcademicClassification academicClassification) { iAcademicClassification = academicClassification; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "major_id", nullable = false)
	public PosMajor getMajor() { return iMajor; }
	public void setMajor(PosMajor major) { iMajor = major; }

	@ManyToOne(optional = true)
	@JoinColumn(name = "concentration_id", nullable = true)
	public PosMajorConcentration getConcentration() { return iConcentration; }
	public void setConcentration(PosMajorConcentration concentration) { iConcentration = concentration; }

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof PitStudentAcadAreaMajorClassification)) return false;
		if (getUniqueId() == null || ((PitStudentAcadAreaMajorClassification)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((PitStudentAcadAreaMajorClassification)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "PitStudentAcadAreaMajorClassification["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "PitStudentAcadAreaMajorClassification[" +
			"\n	AcademicArea: " + getAcademicArea() +
			"\n	AcademicClassification: " + getAcademicClassification() +
			"\n	Concentration: " + getConcentration() +
			"\n	Major: " + getMajor() +
			"\n	PitStudent: " + getPitStudent() +
			"\n	UniqueId: " + getUniqueId() +
			"\n	Weight: " + getWeight() +
			"]";
	}
}
