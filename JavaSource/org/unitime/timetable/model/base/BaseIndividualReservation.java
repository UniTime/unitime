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
import java.util.Date;

import org.unitime.timetable.model.IndividualReservation;
import org.unitime.timetable.model.Reservation;

public abstract class BaseIndividualReservation extends Reservation implements Serializable {
	private static final long serialVersionUID = 1L;

	private String iExternalUniqueId;
	private Boolean iOverLimit;
	private Date iExpirationDate;


	public static String PROP_EXTERNAL_UID = "externalUniqueId";
	public static String PROP_OVER_LIMIT = "overLimit";
	public static String PROP_EXPIRATION_DATE = "expirationDate";

	public BaseIndividualReservation() {
		initialize();
	}

	public BaseIndividualReservation(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public String getExternalUniqueId() { return iExternalUniqueId; }
	public void setExternalUniqueId(String externalUniqueId) { iExternalUniqueId = externalUniqueId; }

	public Boolean isOverLimit() { return iOverLimit; }
	public Boolean getOverLimit() { return iOverLimit; }
	public void setOverLimit(Boolean overLimit) { iOverLimit = overLimit; }

	public Date getExpirationDate() { return iExpirationDate; }
	public void setExpirationDate(Date expirationDate) { iExpirationDate = expirationDate; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof IndividualReservation)) return false;
		if (getUniqueId() == null || ((IndividualReservation)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((IndividualReservation)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "IndividualReservation["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "IndividualReservation[" +
			"\n	ExpirationDate: " + getExpirationDate() +
			"\n	ExternalUniqueId: " + getExternalUniqueId() +
			"\n	OverLimit: " + getOverLimit() +
			"\n	Owner: " + getOwner() +
			"\n	OwnerClassId: " + getOwnerClassId() +
			"\n	Priority: " + getPriority() +
			"\n	ReservationType: " + getReservationType() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
