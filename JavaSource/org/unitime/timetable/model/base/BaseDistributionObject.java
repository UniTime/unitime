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

import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;

import java.io.Serializable;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.unitime.commons.hibernate.id.UniqueIdGenerator;
import org.unitime.timetable.model.DistributionObject;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.PreferenceGroup;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseDistributionObject implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Integer iSequenceNumber;

	private DistributionPref iDistributionPref;
	private PreferenceGroup iPrefGroup;

	public BaseDistributionObject() {
	}

	public BaseDistributionObject(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "distribution_object_id", type = UniqueIdGenerator.class, parameters = {
		@Parameter(name = "sequence", value = "dist_obj_seq")
	})
	@GeneratedValue(generator = "distribution_object_id")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "sequence_number", nullable = true, length = 2)
	public Integer getSequenceNumber() { return iSequenceNumber; }
	public void setSequenceNumber(Integer sequenceNumber) { iSequenceNumber = sequenceNumber; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "dist_pref_id", nullable = false)
	public DistributionPref getDistributionPref() { return iDistributionPref; }
	public void setDistributionPref(DistributionPref distributionPref) { iDistributionPref = distributionPref; }

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "pref_group_id", nullable = false)
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, includeLazy = false)
	public PreferenceGroup getPrefGroup() { return iPrefGroup; }
	public void setPrefGroup(PreferenceGroup prefGroup) { iPrefGroup = prefGroup; }

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof DistributionObject)) return false;
		if (getUniqueId() == null || ((DistributionObject)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((DistributionObject)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
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
