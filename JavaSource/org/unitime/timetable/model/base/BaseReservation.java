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

import org.unitime.timetable.model.Reservation;
import org.unitime.timetable.model.ReservationType;

public abstract class BaseReservation implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iOwnerClassId;
	private Long iOwner;
	private Integer iPriority;

	private ReservationType iReservationType;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_OWNER_CLASS_ID = "ownerClassId";
	public static String PROP_OWNER = "owner";
	public static String PROP_PRIORITY = "priority";

	public BaseReservation() {
		initialize();
	}

	public BaseReservation(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public String getOwnerClassId() { return iOwnerClassId; }
	public void setOwnerClassId(String ownerClassId) { iOwnerClassId = ownerClassId; }

	public Long getOwner() { return iOwner; }
	public void setOwner(Long owner) { iOwner = owner; }

	public Integer getPriority() { return iPriority; }
	public void setPriority(Integer priority) { iPriority = priority; }

	public ReservationType getReservationType() { return iReservationType; }
	public void setReservationType(ReservationType reservationType) { iReservationType = reservationType; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof Reservation)) return false;
		if (getUniqueId() == null || ((Reservation)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((Reservation)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "Reservation["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "Reservation[" +
			"\n	Owner: " + getOwner() +
			"\n	OwnerClassId: " + getOwnerClassId() +
			"\n	Priority: " + getPriority() +
			"\n	ReservationType: " + getReservationType() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
