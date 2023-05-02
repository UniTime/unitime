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

import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.MappedSuperclass;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.unitime.timetable.model.AcademicArea;
import org.unitime.timetable.model.AcademicClassification;
import org.unitime.timetable.model.CurriculumReservation;
import org.unitime.timetable.model.PosMajor;
import org.unitime.timetable.model.PosMajorConcentration;
import org.unitime.timetable.model.PosMinor;
import org.unitime.timetable.model.Reservation;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseCurriculumReservation extends Reservation implements Serializable {
	private static final long serialVersionUID = 1L;

	private Set<AcademicArea> iAreas;
	private Set<PosMajor> iMajors;
	private Set<PosMajorConcentration> iConcentrations;
	private Set<AcademicClassification> iClassifications;
	private Set<PosMinor> iMinors;

	public BaseCurriculumReservation() {
	}

	public BaseCurriculumReservation(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "reservation_acad_area",
		joinColumns = { @JoinColumn(name = "reservation_id") },
		inverseJoinColumns = { @JoinColumn(name = "area_id") })
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, includeLazy = false)
	public Set<AcademicArea> getAreas() { return iAreas; }
	public void setAreas(Set<AcademicArea> areas) { iAreas = areas; }
	public void addToAreas(AcademicArea academicArea) {
		if (iAreas == null) iAreas = new HashSet<AcademicArea>();
		iAreas.add(academicArea);
	}
	@Deprecated
	public void addToareas(AcademicArea academicArea) {
		addToAreas(academicArea);
	}

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "reservation_major",
		joinColumns = { @JoinColumn(name = "reservation_id") },
		inverseJoinColumns = { @JoinColumn(name = "major_id") })
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, includeLazy = false)
	public Set<PosMajor> getMajors() { return iMajors; }
	public void setMajors(Set<PosMajor> majors) { iMajors = majors; }
	public void addToMajors(PosMajor posMajor) {
		if (iMajors == null) iMajors = new HashSet<PosMajor>();
		iMajors.add(posMajor);
	}
	@Deprecated
	public void addTomajors(PosMajor posMajor) {
		addToMajors(posMajor);
	}

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "reservation_major_conc",
		joinColumns = { @JoinColumn(name = "reservation_id") },
		inverseJoinColumns = { @JoinColumn(name = "concentration_id") })
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, includeLazy = false)
	public Set<PosMajorConcentration> getConcentrations() { return iConcentrations; }
	public void setConcentrations(Set<PosMajorConcentration> concentrations) { iConcentrations = concentrations; }
	public void addToConcentrations(PosMajorConcentration posMajorConcentration) {
		if (iConcentrations == null) iConcentrations = new HashSet<PosMajorConcentration>();
		iConcentrations.add(posMajorConcentration);
	}
	@Deprecated
	public void addToconcentrations(PosMajorConcentration posMajorConcentration) {
		addToConcentrations(posMajorConcentration);
	}

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "reservation_clasf",
		joinColumns = { @JoinColumn(name = "reservation_id") },
		inverseJoinColumns = { @JoinColumn(name = "acad_clasf_id") })
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, includeLazy = false)
	public Set<AcademicClassification> getClassifications() { return iClassifications; }
	public void setClassifications(Set<AcademicClassification> classifications) { iClassifications = classifications; }
	public void addToClassifications(AcademicClassification academicClassification) {
		if (iClassifications == null) iClassifications = new HashSet<AcademicClassification>();
		iClassifications.add(academicClassification);
	}
	@Deprecated
	public void addToclassifications(AcademicClassification academicClassification) {
		addToClassifications(academicClassification);
	}

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "reservation_minor",
		joinColumns = { @JoinColumn(name = "reservation_id") },
		inverseJoinColumns = { @JoinColumn(name = "minor_id") })
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, includeLazy = false)
	public Set<PosMinor> getMinors() { return iMinors; }
	public void setMinors(Set<PosMinor> minors) { iMinors = minors; }
	public void addToMinors(PosMinor posMinor) {
		if (iMinors == null) iMinors = new HashSet<PosMinor>();
		iMinors.add(posMinor);
	}
	@Deprecated
	public void addTominors(PosMinor posMinor) {
		addToMinors(posMinor);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof CurriculumReservation)) return false;
		if (getUniqueId() == null || ((CurriculumReservation)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((CurriculumReservation)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
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
