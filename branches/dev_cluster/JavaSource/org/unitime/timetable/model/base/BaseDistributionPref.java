/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
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

import org.unitime.timetable.model.DistributionObject;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.DistributionType;
import org.unitime.timetable.model.Preference;

public abstract class BaseDistributionPref extends Preference implements Serializable {
	private static final long serialVersionUID = 1L;

	private Integer iGrouping;
	private Long iUniqueIdRolledForwardFrom;

	private DistributionType iDistributionType;
	private Set<DistributionObject> iDistributionObjects;

	public static String PROP_GROUPING = "grouping";
	public static String PROP_UID_ROLLED_FWD_FROM = "uniqueIdRolledForwardFrom";

	public BaseDistributionPref() {
		initialize();
	}

	public BaseDistributionPref(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Integer getGrouping() { return iGrouping; }
	public void setGrouping(Integer grouping) { iGrouping = grouping; }

	public Long getUniqueIdRolledForwardFrom() { return iUniqueIdRolledForwardFrom; }
	public void setUniqueIdRolledForwardFrom(Long uniqueIdRolledForwardFrom) { iUniqueIdRolledForwardFrom = uniqueIdRolledForwardFrom; }

	public DistributionType getDistributionType() { return iDistributionType; }
	public void setDistributionType(DistributionType distributionType) { iDistributionType = distributionType; }

	public Set<DistributionObject> getDistributionObjects() { return iDistributionObjects; }
	public void setDistributionObjects(Set<DistributionObject> distributionObjects) { iDistributionObjects = distributionObjects; }
	public void addTodistributionObjects(DistributionObject distributionObject) {
		if (iDistributionObjects == null) iDistributionObjects = new HashSet<DistributionObject>();
		iDistributionObjects.add(distributionObject);
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof DistributionPref)) return false;
		if (getUniqueId() == null || ((DistributionPref)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((DistributionPref)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "DistributionPref["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "DistributionPref[" +
			"\n	DistributionType: " + getDistributionType() +
			"\n	Grouping: " + getGrouping() +
			"\n	Owner: " + getOwner() +
			"\n	PrefLevel: " + getPrefLevel() +
			"\n	UniqueId: " + getUniqueId() +
			"\n	UniqueIdRolledForwardFrom: " + getUniqueIdRolledForwardFrom() +
			"]";
	}
}
