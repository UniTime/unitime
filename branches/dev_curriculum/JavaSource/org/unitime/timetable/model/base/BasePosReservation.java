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
import org.unitime.timetable.model.PosMajor;
import org.unitime.timetable.model.PosReservation;

public abstract class BasePosReservation extends AcadAreaPosReservation implements Serializable {
	private static final long serialVersionUID = 1L;

	private PosMajor iPosMajor;


	public BasePosReservation() {
		initialize();
	}

	public BasePosReservation(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public PosMajor getPosMajor() { return iPosMajor; }
	public void setPosMajor(PosMajor posMajor) { iPosMajor = posMajor; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof PosReservation)) return false;
		if (getUniqueId() == null || ((PosReservation)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((PosReservation)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "PosReservation["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "PosReservation[" +
			"\n	AcademicClassification: " + getAcademicClassification() +
			"\n	Owner: " + getOwner() +
			"\n	OwnerClassId: " + getOwnerClassId() +
			"\n	PosMajor: " + getPosMajor() +
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
