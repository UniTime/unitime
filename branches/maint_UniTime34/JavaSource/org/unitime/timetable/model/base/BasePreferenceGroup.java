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
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.PreferenceGroup;

public abstract class BasePreferenceGroup implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;

	private Set<Preference> iPreferences;
	private Set<DistributionObject> iDistributionObjects;

	public static String PROP_UNIQUEID = "uniqueId";

	public BasePreferenceGroup() {
		initialize();
	}

	public BasePreferenceGroup(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public Set<Preference> getPreferences() { return iPreferences; }
	public void setPreferences(Set<Preference> preferences) { iPreferences = preferences; }
	public void addTopreferences(Preference preference) {
		if (iPreferences == null) iPreferences = new HashSet<Preference>();
		iPreferences.add(preference);
	}

	public Set<DistributionObject> getDistributionObjects() { return iDistributionObjects; }
	public void setDistributionObjects(Set<DistributionObject> distributionObjects) { iDistributionObjects = distributionObjects; }
	public void addTodistributionObjects(DistributionObject distributionObject) {
		if (iDistributionObjects == null) iDistributionObjects = new HashSet<DistributionObject>();
		iDistributionObjects.add(distributionObject);
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof PreferenceGroup)) return false;
		if (getUniqueId() == null || ((PreferenceGroup)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((PreferenceGroup)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "PreferenceGroup["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "PreferenceGroup[" +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
