/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.model.base;

import java.io.Serializable;

import org.unitime.timetable.model.AcadAreaPosReservation;
import org.unitime.timetable.model.AcademicClassification;
import org.unitime.timetable.model.CharacteristicReservation;

public abstract class BaseAcadAreaPosReservation extends CharacteristicReservation implements Serializable {
	private static final long serialVersionUID = 1L;

	private AcademicClassification iAcademicClassification;


	public BaseAcadAreaPosReservation() {
		initialize();
	}

	public BaseAcadAreaPosReservation(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public AcademicClassification getAcademicClassification() { return iAcademicClassification; }
	public void setAcademicClassification(AcademicClassification academicClassification) { iAcademicClassification = academicClassification; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof AcadAreaPosReservation)) return false;
		if (getUniqueId() == null || ((AcadAreaPosReservation)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((AcadAreaPosReservation)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "AcadAreaPosReservation["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "AcadAreaPosReservation[" +
			"\n	AcademicClassification: " + getAcademicClassification() +
			"\n	Owner: " + getOwner() +
			"\n	OwnerClassId: " + getOwnerClassId() +
			"\n	PriorEnrollment: " + getPriorEnrollment() +
			"\n	Priority: " + getPriority() +
			"\n	ProjectedEnrollment: " + getProjectedEnrollment() +
			"\n	Requested: " + getRequested() +
			"\n	ReservationType: " + getReservationType() +
			"\n	Reserved: " + getReserved() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
