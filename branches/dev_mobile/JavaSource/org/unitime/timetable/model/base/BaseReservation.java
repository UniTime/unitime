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
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.Reservation;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseReservation implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Date iExpirationDate;
	private Integer iLimit;

	private InstructionalOffering iInstructionalOffering;
	private Set<InstrOfferingConfig> iConfigurations;
	private Set<Class_> iClasses;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_EXPIRATION_DATE = "expirationDate";
	public static String PROP_RESERVATION_LIMIT = "limit";

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

	public Date getExpirationDate() { return iExpirationDate; }
	public void setExpirationDate(Date expirationDate) { iExpirationDate = expirationDate; }

	public Integer getLimit() { return iLimit; }
	public void setLimit(Integer limit) { iLimit = limit; }

	public InstructionalOffering getInstructionalOffering() { return iInstructionalOffering; }
	public void setInstructionalOffering(InstructionalOffering instructionalOffering) { iInstructionalOffering = instructionalOffering; }

	public Set<InstrOfferingConfig> getConfigurations() { return iConfigurations; }
	public void setConfigurations(Set<InstrOfferingConfig> configurations) { iConfigurations = configurations; }
	public void addToconfigurations(InstrOfferingConfig instrOfferingConfig) {
		if (iConfigurations == null) iConfigurations = new HashSet<InstrOfferingConfig>();
		iConfigurations.add(instrOfferingConfig);
	}

	public Set<Class_> getClasses() { return iClasses; }
	public void setClasses(Set<Class_> classes) { iClasses = classes; }
	public void addToclasses(Class_ class_) {
		if (iClasses == null) iClasses = new HashSet<Class_>();
		iClasses.add(class_);
	}

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
			"\n	ExpirationDate: " + getExpirationDate() +
			"\n	InstructionalOffering: " + getInstructionalOffering() +
			"\n	Limit: " + getLimit() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
