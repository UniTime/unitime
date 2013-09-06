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

import org.unitime.timetable.model.DistributionObject;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.PreferenceGroup;

public abstract class BaseDistributionObject implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Integer iSequenceNumber;

	private DistributionPref iDistributionPref;
	private PreferenceGroup iPrefGroup;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_SEQUENCE_NUMBER = "sequenceNumber";

	public BaseDistributionObject() {
		initialize();
	}

	public BaseDistributionObject(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public Integer getSequenceNumber() { return iSequenceNumber; }
	public void setSequenceNumber(Integer sequenceNumber) { iSequenceNumber = sequenceNumber; }

	public DistributionPref getDistributionPref() { return iDistributionPref; }
	public void setDistributionPref(DistributionPref distributionPref) { iDistributionPref = distributionPref; }

	public PreferenceGroup getPrefGroup() { return iPrefGroup; }
	public void setPrefGroup(PreferenceGroup prefGroup) { iPrefGroup = prefGroup; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof DistributionObject)) return false;
		if (getUniqueId() == null || ((DistributionObject)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((DistributionObject)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "DistributionObject["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "DistributionObject[" +
			"\n	DistributionPref: " + getDistributionPref() +
			"\n	PrefGroup: " + getPrefGroup() +
			"\n	SequenceNumber: " + getSequenceNumber() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
