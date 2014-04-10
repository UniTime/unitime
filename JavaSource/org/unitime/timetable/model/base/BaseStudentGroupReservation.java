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

import org.unitime.timetable.model.Reservation;
import org.unitime.timetable.model.StudentGroup;
import org.unitime.timetable.model.StudentGroupReservation;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseStudentGroupReservation extends Reservation implements Serializable {
	private static final long serialVersionUID = 1L;

	private StudentGroup iGroup;


	public BaseStudentGroupReservation() {
		initialize();
	}

	public BaseStudentGroupReservation(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public StudentGroup getGroup() { return iGroup; }
	public void setGroup(StudentGroup group) { iGroup = group; }

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
			"\n	ExpirationDate: " + getExpirationDate() +
			"\n	Group: " + getGroup() +
			"\n	InstructionalOffering: " + getInstructionalOffering() +
			"\n	Limit: " + getLimit() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
