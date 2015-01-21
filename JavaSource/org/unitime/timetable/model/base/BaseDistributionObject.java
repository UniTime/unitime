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

import org.unitime.timetable.model.DistributionObject;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.PreferenceGroup;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
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
