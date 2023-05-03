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
import jakarta.persistence.GeneratedValue;
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
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.unitime.commons.hibernate.id.UniqueIdGenerator;
import org.unitime.timetable.model.ClassDurationType;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalMethod;
import org.unitime.timetable.model.PitInstrOfferingConfig;
import org.unitime.timetable.model.PitInstructionalOffering;
import org.unitime.timetable.model.PitSchedulingSubpart;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BasePitInstrOfferingConfig implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Boolean iUnlimitedEnrollment;
	private String iName;
	private Long iUniqueIdRolledForwardFrom;

	private InstrOfferingConfig iInstrOfferingConfig;
	private PitInstructionalOffering iPitInstructionalOffering;
	private ClassDurationType iClassDurationType;
	private InstructionalMethod iInstructionalMethod;
	private Set<PitSchedulingSubpart> iSchedulingSubparts;

	public BasePitInstrOfferingConfig() {
	}

	public BasePitInstrOfferingConfig(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "pit_instr_offer_config_id", type = UniqueIdGenerator.class, parameters = {
		@Parameter(name = "sequence", value = "point_in_time_seq")
	})
	@GeneratedValue(generator = "pit_instr_offer_config_id")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "unlimited_enrollment", nullable = false)
	public Boolean isUnlimitedEnrollment() { return iUnlimitedEnrollment; }
	@Transient
	public Boolean getUnlimitedEnrollment() { return iUnlimitedEnrollment; }
	public void setUnlimitedEnrollment(Boolean unlimitedEnrollment) { iUnlimitedEnrollment = unlimitedEnrollment; }

	@Column(name = "name", nullable = true, length = 10)
	public String getName() { return iName; }
	public void setName(String name) { iName = name; }

	@Column(name = "uid_rolled_fwd_from", nullable = true, length = 20)
	public Long getUniqueIdRolledForwardFrom() { return iUniqueIdRolledForwardFrom; }
	public void setUniqueIdRolledForwardFrom(Long uniqueIdRolledForwardFrom) { iUniqueIdRolledForwardFrom = uniqueIdRolledForwardFrom; }

	@ManyToOne(optional = true)
	@JoinColumn(name = "instr_offering_config_id", nullable = true)
	public InstrOfferingConfig getInstrOfferingConfig() { return iInstrOfferingConfig; }
	public void setInstrOfferingConfig(InstrOfferingConfig instrOfferingConfig) { iInstrOfferingConfig = instrOfferingConfig; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "pit_instr_offr_id", nullable = false)
	public PitInstructionalOffering getPitInstructionalOffering() { return iPitInstructionalOffering; }
	public void setPitInstructionalOffering(PitInstructionalOffering pitInstructionalOffering) { iPitInstructionalOffering = pitInstructionalOffering; }

	@ManyToOne(optional = true)
	@JoinColumn(name = "duration_type_id", nullable = true)
	public ClassDurationType getClassDurationType() { return iClassDurationType; }
	public void setClassDurationType(ClassDurationType classDurationType) { iClassDurationType = classDurationType; }

	@ManyToOne(optional = true)
	@JoinColumn(name = "instr_method_id", nullable = true)
	public InstructionalMethod getInstructionalMethod() { return iInstructionalMethod; }
	public void setInstructionalMethod(InstructionalMethod instructionalMethod) { iInstructionalMethod = instructionalMethod; }

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "pitInstrOfferingConfig", cascade = {CascadeType.ALL}, orphanRemoval = true)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	public Set<PitSchedulingSubpart> getSchedulingSubparts() { return iSchedulingSubparts; }
	public void setSchedulingSubparts(Set<PitSchedulingSubpart> schedulingSubparts) { iSchedulingSubparts = schedulingSubparts; }
	public void addToSchedulingSubparts(PitSchedulingSubpart pitSchedulingSubpart) {
		if (iSchedulingSubparts == null) iSchedulingSubparts = new HashSet<PitSchedulingSubpart>();
		iSchedulingSubparts.add(pitSchedulingSubpart);
	}
	@Deprecated
	public void addToschedulingSubparts(PitSchedulingSubpart pitSchedulingSubpart) {
		addToSchedulingSubparts(pitSchedulingSubpart);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof PitInstrOfferingConfig)) return false;
		if (getUniqueId() == null || ((PitInstrOfferingConfig)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((PitInstrOfferingConfig)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "PitInstrOfferingConfig["+getUniqueId()+" "+getName()+"]";
	}

	public String toDebugString() {
		return "PitInstrOfferingConfig[" +
			"\n	ClassDurationType: " + getClassDurationType() +
			"\n	InstrOfferingConfig: " + getInstrOfferingConfig() +
			"\n	InstructionalMethod: " + getInstructionalMethod() +
			"\n	Name: " + getName() +
			"\n	PitInstructionalOffering: " + getPitInstructionalOffering() +
			"\n	UniqueId: " + getUniqueId() +
			"\n	UniqueIdRolledForwardFrom: " + getUniqueIdRolledForwardFrom() +
			"\n	UnlimitedEnrollment: " + getUnlimitedEnrollment() +
			"]";
	}
}
