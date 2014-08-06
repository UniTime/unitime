/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2010 - 2014, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
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
import org.unitime.timetable.model.Reservation;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseCurriculumReservation extends Reservation implements Serializable {
	private static final long serialVersionUID = 1L;

	private AcademicArea iArea;
	private Set<PosMajor> iMajors;
	private Set<AcademicClassification> iClassifications;


	public BaseCurriculumReservation() {
		initialize();
	}

	public BaseCurriculumReservation(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public AcademicArea getArea() { return iArea; }
	public void setArea(AcademicArea area) { iArea = area; }

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
			"\n	Area: " + getArea() +
			"\n	ExpirationDate: " + getExpirationDate() +
			"\n	InstructionalOffering: " + getInstructionalOffering() +
			"\n	Limit: " + getLimit() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
