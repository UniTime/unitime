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
import org.unitime.timetable.model.AcademicArea;
import org.unitime.timetable.model.AcademicClassification;
import org.unitime.timetable.model.Campus;
import org.unitime.timetable.model.Degree;
import org.unitime.timetable.model.PosMajor;
import org.unitime.timetable.model.PosMajorConcentration;
import org.unitime.timetable.model.Program;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentAreaClassificationMajor;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseStudentAreaClassificationMajor implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Double iWeight;

	private Student iStudent;
	private AcademicArea iAcademicArea;
	private AcademicClassification iAcademicClassification;
	private PosMajor iMajor;
	private PosMajorConcentration iConcentration;
	private Degree iDegree;
	private Program iProgram;
	private Campus iCampus;

	public BaseStudentAreaClassificationMajor() {
	}

	public BaseStudentAreaClassificationMajor(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "student_area_clasf_major_id", strategy = "org.unitime.commons.hibernate.id.UniqueIdGenerator", parameters = {
		@Parameter(name = "sequence", value = "pref_group_seq")
	})
	@GeneratedValue(generator = "student_area_clasf_major_id")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "weight", nullable = true)
	public Double getWeight() { return iWeight; }
	public void setWeight(Double weight) { iWeight = weight; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "student_id", nullable = false)
	public Student getStudent() { return iStudent; }
	public void setStudent(Student student) { iStudent = student; }

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

	@ManyToOne(optional = true)
	@JoinColumn(name = "degree_id", nullable = true)
	public Degree getDegree() { return iDegree; }
	public void setDegree(Degree degree) { iDegree = degree; }

	@ManyToOne(optional = true)
	@JoinColumn(name = "program_id", nullable = true)
	public Program getProgram() { return iProgram; }
	public void setProgram(Program program) { iProgram = program; }

	@ManyToOne(optional = true)
	@JoinColumn(name = "campus_id", nullable = true)
	public Campus getCampus() { return iCampus; }
	public void setCampus(Campus campus) { iCampus = campus; }

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof StudentAreaClassificationMajor)) return false;
		if (getUniqueId() == null || ((StudentAreaClassificationMajor)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((StudentAreaClassificationMajor)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "StudentAreaClassificationMajor["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "StudentAreaClassificationMajor[" +
			"\n	AcademicArea: " + getAcademicArea() +
			"\n	AcademicClassification: " + getAcademicClassification() +
			"\n	Campus: " + getCampus() +
			"\n	Concentration: " + getConcentration() +
			"\n	Degree: " + getDegree() +
			"\n	Major: " + getMajor() +
			"\n	Program: " + getProgram() +
			"\n	Student: " + getStudent() +
			"\n	UniqueId: " + getUniqueId() +
			"\n	Weight: " + getWeight() +
			"]";
	}
}
