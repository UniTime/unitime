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

import org.unitime.timetable.model.AcademicArea;
import org.unitime.timetable.model.AcademicClassification;
import org.unitime.timetable.model.CurriculumReservation;
import org.unitime.timetable.model.PosMajor;
import org.unitime.timetable.model.PosMinor;
import org.unitime.timetable.model.Reservation;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseCurriculumReservation extends Reservation implements Serializable {
	private static final long serialVersionUID = 1L;

	private Set<AcademicArea> iAreas;
	private Set<PosMajor> iMajors;
	private Set<AcademicClassification> iClassifications;
	private Set<PosMinor> iMinors;


	public BaseCurriculumReservation() {
		initialize();
	}

	public BaseCurriculumReservation(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Set<AcademicArea> getAreas() { return iAreas; }
	public void setAreas(Set<AcademicArea> areas) { iAreas = areas; }
	public void addToareas(AcademicArea academicArea) {
		if (iAreas == null) iAreas = new HashSet<AcademicArea>();
		iAreas.add(academicArea);
	}

	public Set<PosMajor> getMajors() { return iMajors; }
	public void setMajors(Set<PosMajor> majors) { iMajors = majors; }
	public void addTomajors(PosMajor posMajor) {
		if (iMajors == null) iMajors = new HashSet<PosMajor>();
		iMajors.add(posMajor);
	}

	public Set<AcademicClassification> getClassifications() { return iClassifications; }
	public void setClassifications(Set<AcademicClassification> classifications) { iClassifications = classifications; }
	public void addToclassifications(AcademicClassification academicClassification) {
		if (iClassifications == null) iClassifications = new HashSet<AcademicClassification>();
		iClassifications.add(academicClassification);
	}

	public Set<PosMinor> getMinors() { return iMinors; }
	public void setMinors(Set<PosMinor> minors) { iMinors = minors; }
	public void addTominors(PosMinor posMinor) {
		if (iMinors == null) iMinors = new HashSet<PosMinor>();
		iMinors.add(posMinor);
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof CurriculumReservation)) return false;
		if (getUniqueId() == null || ((CurriculumReservation)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((CurriculumReservation)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "CurriculumReservation["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "CurriculumReservation[" +
			"\n	ExpirationDate: " + getExpirationDate() +
			"\n	Inclusive: " + getInclusive() +
			"\n	InstructionalOffering: " + getInstructionalOffering() +
			"\n	Limit: " + getLimit() +
			"\n	StartDate: " + getStartDate() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
