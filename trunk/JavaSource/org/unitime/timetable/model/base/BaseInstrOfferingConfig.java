/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2010 - 2013, UniTime LLC, and individual contributors
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

import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.SchedulingSubpart;

/**
 * @author Tomas Muller
 */
public abstract class BaseInstrOfferingConfig implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Integer iLimit;
	private Boolean iUnlimitedEnrollment;
	private String iName;
	private Long iUniqueIdRolledForwardFrom;

	private InstructionalOffering iInstructionalOffering;
	private Set<SchedulingSubpart> iSchedulingSubparts;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_CONFIG_LIMIT = "limit";
	public static String PROP_UNLIMITED_ENROLLMENT = "unlimitedEnrollment";
	public static String PROP_NAME = "name";
	public static String PROP_UID_ROLLED_FWD_FROM = "uniqueIdRolledForwardFrom";

	public BaseInstrOfferingConfig() {
		initialize();
	}

	public BaseInstrOfferingConfig(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public Integer getLimit() { return iLimit; }
	public void setLimit(Integer limit) { iLimit = limit; }

	public Boolean isUnlimitedEnrollment() { return iUnlimitedEnrollment; }
	public Boolean getUnlimitedEnrollment() { return iUnlimitedEnrollment; }
	public void setUnlimitedEnrollment(Boolean unlimitedEnrollment) { iUnlimitedEnrollment = unlimitedEnrollment; }

	public String getName() { return iName; }
	public void setName(String name) { iName = name; }

	public Long getUniqueIdRolledForwardFrom() { return iUniqueIdRolledForwardFrom; }
	public void setUniqueIdRolledForwardFrom(Long uniqueIdRolledForwardFrom) { iUniqueIdRolledForwardFrom = uniqueIdRolledForwardFrom; }

	public InstructionalOffering getInstructionalOffering() { return iInstructionalOffering; }
	public void setInstructionalOffering(InstructionalOffering instructionalOffering) { iInstructionalOffering = instructionalOffering; }

	public Set<SchedulingSubpart> getSchedulingSubparts() { return iSchedulingSubparts; }
	public void setSchedulingSubparts(Set<SchedulingSubpart> schedulingSubparts) { iSchedulingSubparts = schedulingSubparts; }
	public void addToschedulingSubparts(SchedulingSubpart schedulingSubpart) {
		if (iSchedulingSubparts == null) iSchedulingSubparts = new HashSet<SchedulingSubpart>();
		iSchedulingSubparts.add(schedulingSubpart);
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof InstrOfferingConfig)) return false;
		if (getUniqueId() == null || ((InstrOfferingConfig)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((InstrOfferingConfig)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "InstrOfferingConfig["+getUniqueId()+" "+getName()+"]";
	}

	public String toDebugString() {
		return "InstrOfferingConfig[" +
			"\n	InstructionalOffering: " + getInstructionalOffering() +
			"\n	Limit: " + getLimit() +
			"\n	Name: " + getName() +
			"\n	UniqueId: " + getUniqueId() +
			"\n	UniqueIdRolledForwardFrom: " + getUniqueIdRolledForwardFrom() +
			"\n	UnlimitedEnrollment: " + getUnlimitedEnrollment() +
			"]";
	}
}
