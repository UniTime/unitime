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
import java.util.HashSet;
import java.util.Set;

import org.unitime.timetable.model.DistributionObject;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.DistributionType;
import org.unitime.timetable.model.Preference;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseDistributionPref extends Preference implements Serializable {
	private static final long serialVersionUID = 1L;

	private Integer iGrouping;
	private Long iUniqueIdRolledForwardFrom;

	private DistributionType iDistributionType;
	private Set<DistributionObject> iDistributionObjects;

	public static String PROP_DIST_GROUPING = "grouping";
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
