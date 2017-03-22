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
import org.unitime.timetable.model.PosMinor;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentAreaClassificationMinor;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseStudentAreaClassificationMinor implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;

	private Student iStudent;
	private AcademicArea iAcademicArea;
	private AcademicClassification iAcademicClassification;
	private PosMinor iMinor;

	public static String PROP_UNIQUEID = "uniqueId";

	public BaseStudentAreaClassificationMinor() {
		initialize();
	}

	public BaseStudentAreaClassificationMinor(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public Student getStudent() { return iStudent; }
	public void setStudent(Student student) { iStudent = student; }

	public AcademicArea getAcademicArea() { return iAcademicArea; }
	public void setAcademicArea(AcademicArea academicArea) { iAcademicArea = academicArea; }

	public AcademicClassification getAcademicClassification() { return iAcademicClassification; }
	public void setAcademicClassification(AcademicClassification academicClassification) { iAcademicClassification = academicClassification; }

	public PosMinor getMinor() { return iMinor; }
	public void setMinor(PosMinor minor) { iMinor = minor; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof StudentAreaClassificationMinor)) return false;
		if (getUniqueId() == null || ((StudentAreaClassificationMinor)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((StudentAreaClassificationMinor)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "StudentAreaClassificationMinor["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "StudentAreaClassificationMinor[" +
			"\n	AcademicArea: " + getAcademicArea() +
			"\n	AcademicClassification: " + getAcademicClassification() +
			"\n	Minor: " + getMinor() +
			"\n	Student: " + getStudent() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
