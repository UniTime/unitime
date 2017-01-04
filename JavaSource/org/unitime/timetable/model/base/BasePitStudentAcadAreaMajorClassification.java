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

import org.unitime.timetable.model.AcademicArea;
import org.unitime.timetable.model.AcademicClassification;
import org.unitime.timetable.model.PitStudent;
import org.unitime.timetable.model.PitStudentAcadAreaMajorClassification;
import org.unitime.timetable.model.PosMajor;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BasePitStudentAcadAreaMajorClassification implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;

	private PitStudent iPitStudent;
	private AcademicArea iAcademicArea;
	private AcademicClassification iAcademicClassification;
	private PosMajor iMajor;

	public static String PROP_UNIQUEID = "uniqueId";

	public BasePitStudentAcadAreaMajorClassification() {
		initialize();
	}

	public BasePitStudentAcadAreaMajorClassification(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public PitStudent getPitStudent() { return iPitStudent; }
	public void setPitStudent(PitStudent pitStudent) { iPitStudent = pitStudent; }

	public AcademicArea getAcademicArea() { return iAcademicArea; }
	public void setAcademicArea(AcademicArea academicArea) { iAcademicArea = academicArea; }

	public AcademicClassification getAcademicClassification() { return iAcademicClassification; }
	public void setAcademicClassification(AcademicClassification academicClassification) { iAcademicClassification = academicClassification; }

	public PosMajor getMajor() { return iMajor; }
	public void setMajor(PosMajor major) { iMajor = major; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof PitStudentAcadAreaMajorClassification)) return false;
		if (getUniqueId() == null || ((PitStudentAcadAreaMajorClassification)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((PitStudentAcadAreaMajorClassification)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "PitStudentAcadAreaMajorClassification["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "PitStudentAcadAreaMajorClassification[" +
			"\n	AcademicArea: " + getAcademicArea() +
			"\n	AcademicClassification: " + getAcademicClassification() +
			"\n	Major: " + getMajor() +
			"\n	PitStudent: " + getPitStudent() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
