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

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.unitime.commons.annotations.UniqueIdGenerator;
import org.unitime.timetable.model.ClassDurationType;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalMethod;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.SchedulingSubpart;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseInstrOfferingConfig implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Integer iLimit;
	private Boolean iUnlimitedEnrollment;
	private String iName;
	private Long iUniqueIdRolledForwardFrom;

	private InstructionalOffering iInstructionalOffering;
	private ClassDurationType iClassDurationType;
	private InstructionalMethod iInstructionalMethod;
	private Set<SchedulingSubpart> iSchedulingSubparts;

	public BaseInstrOfferingConfig() {
	}

	public BaseInstrOfferingConfig(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@UniqueIdGenerator(sequence = "instr_offr_config_seq")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "config_limit", nullable = false, length = 4)
	public Integer getLimit() { return iLimit; }
	public void setLimit(Integer limit) { iLimit = limit; }

	@Column(name = "unlimited_enrollment", nullable = false)
	public Boolean isUnlimitedEnrollment() { return iUnlimitedEnrollment; }
	@Transient
	public Boolean getUnlimitedEnrollment() { return iUnlimitedEnrollment; }
	public void setUnlimitedEnrollment(Boolean unlimitedEnrollment) { iUnlimitedEnrollment = unlimitedEnrollment; }

	@Column(name = "name", nullable = true, length = 20)
	public String getName() { return iName; }
	public void setName(String name) { iName = name; }

	@Column(name = "uid_rolled_fwd_from", nullable = true, length = 20)
	public Long getUniqueIdRolledForwardFrom() { return iUniqueIdRolledForwardFrom; }
	public void setUniqueIdRolledForwardFrom(Long uniqueIdRolledForwardFrom) { iUniqueIdRolledForwardFrom = uniqueIdRolledForwardFrom; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "instr_offr_id", nullable = false)
	public InstructionalOffering getInstructionalOffering() { return iInstructionalOffering; }
	public void setInstructionalOffering(InstructionalOffering instructionalOffering) { iInstructionalOffering = instructionalOffering; }

	@ManyToOne(optional = true)
	@JoinColumn(name = "duration_type_id", nullable = true)
	public ClassDurationType getClassDurationType() { return iClassDurationType; }
	public void setClassDurationType(ClassDurationType classDurationType) { iClassDurationType = classDurationType; }

	@ManyToOne(optional = true)
	@JoinColumn(name = "instr_method_id", nullable = true)
	public InstructionalMethod getInstructionalMethod() { return iInstructionalMethod; }
	public void setInstructionalMethod(InstructionalMethod instructionalMethod) { iInstructionalMethod = instructionalMethod; }

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "instrOfferingConfig", cascade = {CascadeType.ALL}, orphanRemoval = true)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	public Set<SchedulingSubpart> getSchedulingSubparts() { return iSchedulingSubparts; }
	public void setSchedulingSubparts(Set<SchedulingSubpart> schedulingSubparts) { iSchedulingSubparts = schedulingSubparts; }
	public void addToSchedulingSubparts(SchedulingSubpart schedulingSubpart) {
		if (iSchedulingSubparts == null) iSchedulingSubparts = new HashSet<SchedulingSubpart>();
		iSchedulingSubparts.add(schedulingSubpart);
	}
	@Deprecated
	public void addToschedulingSubparts(SchedulingSubpart schedulingSubpart) {
		addToSchedulingSubparts(schedulingSubpart);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof InstrOfferingConfig)) return false;
		if (getUniqueId() == null || ((InstrOfferingConfig)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((InstrOfferingConfig)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "InstrOfferingConfig["+getUniqueId()+" "+getName()+"]";
	}

	public String toDebugString() {
		return "InstrOfferingConfig[" +
			"\n	ClassDurationType: " + getClassDurationType() +
			"\n	InstructionalMethod: " + getInstructionalMethod() +
			"\n	InstructionalOffering: " + getInstructionalOffering() +
			"\n	Limit: " + getLimit() +
			"\n	Name: " + getName() +
			"\n	UniqueId: " + getUniqueId() +
			"\n	UniqueIdRolledForwardFrom: " + getUniqueIdRolledForwardFrom() +
			"\n	UnlimitedEnrollment: " + getUnlimitedEnrollment() +
			"]";
	}
}
