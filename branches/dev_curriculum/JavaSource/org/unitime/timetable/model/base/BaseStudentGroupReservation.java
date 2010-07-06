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

import org.unitime.timetable.model.CharacteristicReservation;
import org.unitime.timetable.model.StudentGroup;
import org.unitime.timetable.model.StudentGroupReservation;

public abstract class BaseStudentGroupReservation extends CharacteristicReservation implements Serializable {
	private static final long serialVersionUID = 1L;

	private StudentGroup iStudentGroup;


	public BaseStudentGroupReservation() {
		initialize();
	}

	public BaseStudentGroupReservation(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public StudentGroup getStudentGroup() { return iStudentGroup; }
	public void setStudentGroup(StudentGroup studentGroup) { iStudentGroup = studentGroup; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof StudentGroupReservation)) return false;
		if (getUniqueId() == null || ((StudentGroupReservation)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((StudentGroupReservation)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "StudentGroupReservation["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "StudentGroupReservation[" +
			"\n	Owner: " + getOwner() +
			"\n	OwnerClassId: " + getOwnerClassId() +
			"\n	PriorEnrollment: " + getPriorEnrollment() +
			"\n	Priority: " + getPriority() +
			"\n	ProjectedEnrollment: " + getProjectedEnrollment() +
			"\n	Requested: " + getRequested() +
			"\n	ReservationType: " + getReservationType() +
			"\n	Reserved: " + getReserved() +
			"\n	StudentGroup: " + getStudentGroup() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
