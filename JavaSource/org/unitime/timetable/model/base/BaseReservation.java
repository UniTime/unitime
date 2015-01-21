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
