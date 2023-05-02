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
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.unitime.commons.hibernate.id.UniqueIdGenerator;
import org.unitime.timetable.model.CourseCreditType;
import org.unitime.timetable.model.CourseCreditUnitType;
import org.unitime.timetable.model.ItypeDesc;
import org.unitime.timetable.model.PitClass;
import org.unitime.timetable.model.PitInstrOfferingConfig;
import org.unitime.timetable.model.PitSchedulingSubpart;
import org.unitime.timetable.model.SchedulingSubpart;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BasePitSchedulingSubpart implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Integer iMinutesPerWk;
	private Boolean iStudentAllowOverlap;
	private String iSchedulingSubpartSuffixCache;
	private Float iCredit;
	private String iCourseName;
	private Long iUniqueIdRolledForwardFrom;

	private CourseCreditType iCreditType;
	private CourseCreditUnitType iCreditUnitType;
	private ItypeDesc iItype;
	private SchedulingSubpart iSchedulingSubpart;
	private PitSchedulingSubpart iPitParentSubpart;
	private PitInstrOfferingConfig iPitInstrOfferingConfig;
	private Set<PitSchedulingSubpart> iPitChildSubparts;
	private Set<PitClass> iPitClasses;

	public BasePitSchedulingSubpart() {
	}

	public BasePitSchedulingSubpart(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "pit_sched_subpart_id", type = UniqueIdGenerator.class, parameters = {
		@Parameter(name = "sequence", value = "point_in_time_seq")
	})
	@GeneratedValue(generator = "pit_sched_subpart_id")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "min_per_wk", nullable = false, length = 4)
	public Integer getMinutesPerWk() { return iMinutesPerWk; }
	public void setMinutesPerWk(Integer minutesPerWk) { iMinutesPerWk = minutesPerWk; }

	@Column(name = "student_allow_overlap", nullable = false)
	public Boolean isStudentAllowOverlap() { return iStudentAllowOverlap; }
	@Transient
	public Boolean getStudentAllowOverlap() { return iStudentAllowOverlap; }
	public void setStudentAllowOverlap(Boolean studentAllowOverlap) { iStudentAllowOverlap = studentAllowOverlap; }

	@Column(name = "subpart_suffix", nullable = true, length = 5)
	public String getSchedulingSubpartSuffixCache() { return iSchedulingSubpartSuffixCache; }
	public void setSchedulingSubpartSuffixCache(String schedulingSubpartSuffixCache) { iSchedulingSubpartSuffixCache = schedulingSubpartSuffixCache; }

	@Column(name = "credit", nullable = true)
	public Float getCredit() { return iCredit; }
	public void setCredit(Float credit) { iCredit = credit; }

	@Formula("(select concat( concat( sa.subject_area_abbreviation , ' ') , co.course_nbr) from %SCHEMA%.pit_sched_subpart s, %SCHEMA%.pit_instr_offer_config c, %SCHEMA%.pit_instr_offering io, %SCHEMA%.pit_course_offering co, %SCHEMA%.subject_area sa where s.uniqueid=uniqueid and s.pit_config_id=c.uniqueid and c.pit_instr_offr_id=io.uniqueid and co.is_control = %TRUE% and co.pit_instr_offr_id=io.uniqueid and co.subject_area_id=sa.uniqueid)")
	public String getCourseName() { return iCourseName; }
	public void setCourseName(String courseName) { iCourseName = courseName; }

	@Column(name = "uid_rolled_fwd_from", nullable = true, length = 20)
	public Long getUniqueIdRolledForwardFrom() { return iUniqueIdRolledForwardFrom; }
	public void setUniqueIdRolledForwardFrom(Long uniqueIdRolledForwardFrom) { iUniqueIdRolledForwardFrom = uniqueIdRolledForwardFrom; }

	@ManyToOne(optional = true)
	@JoinColumn(name = "credit_type", nullable = true)
	public CourseCreditType getCreditType() { return iCreditType; }
	public void setCreditType(CourseCreditType creditType) { iCreditType = creditType; }

	@ManyToOne(optional = true)
	@JoinColumn(name = "credit_unit_type", nullable = true)
	public CourseCreditUnitType getCreditUnitType() { return iCreditUnitType; }
	public void setCreditUnitType(CourseCreditUnitType creditUnitType) { iCreditUnitType = creditUnitType; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "itype", nullable = false)
	public ItypeDesc getItype() { return iItype; }
	public void setItype(ItypeDesc itype) { iItype = itype; }

	@ManyToOne(optional = true)
	@JoinColumn(name = "scheduling_subpart_id", nullable = true)
	public SchedulingSubpart getSchedulingSubpart() { return iSchedulingSubpart; }
	public void setSchedulingSubpart(SchedulingSubpart schedulingSubpart) { iSchedulingSubpart = schedulingSubpart; }

	@ManyToOne(optional = true)
	@JoinColumn(name = "pit_parent_id", nullable = true)
	public PitSchedulingSubpart getPitParentSubpart() { return iPitParentSubpart; }
	public void setPitParentSubpart(PitSchedulingSubpart pitParentSubpart) { iPitParentSubpart = pitParentSubpart; }

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "pit_config_id", nullable = false)
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, includeLazy = false)
	public PitInstrOfferingConfig getPitInstrOfferingConfig() { return iPitInstrOfferingConfig; }
	public void setPitInstrOfferingConfig(PitInstrOfferingConfig pitInstrOfferingConfig) { iPitInstrOfferingConfig = pitInstrOfferingConfig; }

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "pitParentSubpart", cascade = {CascadeType.ALL}, orphanRemoval = true)
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, includeLazy = false)
	public Set<PitSchedulingSubpart> getPitChildSubparts() { return iPitChildSubparts; }
	public void setPitChildSubparts(Set<PitSchedulingSubpart> pitChildSubparts) { iPitChildSubparts = pitChildSubparts; }
	public void addToPitChildSubparts(PitSchedulingSubpart pitSchedulingSubpart) {
		if (iPitChildSubparts == null) iPitChildSubparts = new HashSet<PitSchedulingSubpart>();
		iPitChildSubparts.add(pitSchedulingSubpart);
	}
	@Deprecated
	public void addTopitChildSubparts(PitSchedulingSubpart pitSchedulingSubpart) {
		addToPitChildSubparts(pitSchedulingSubpart);
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "pitSchedulingSubpart", cascade = {CascadeType.ALL}, orphanRemoval = true)
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, includeLazy = false)
	public Set<PitClass> getPitClasses() { return iPitClasses; }
	public void setPitClasses(Set<PitClass> pitClasses) { iPitClasses = pitClasses; }
	public void addToPitClasses(PitClass pitClass) {
		if (iPitClasses == null) iPitClasses = new HashSet<PitClass>();
		iPitClasses.add(pitClass);
	}
	@Deprecated
	public void addTopitClasses(PitClass pitClass) {
		addToPitClasses(pitClass);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof PitSchedulingSubpart)) return false;
		if (getUniqueId() == null || ((PitSchedulingSubpart)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((PitSchedulingSubpart)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "PitSchedulingSubpart["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "PitSchedulingSubpart[" +
			"\n	Credit: " + getCredit() +
			"\n	CreditType: " + getCreditType() +
			"\n	CreditUnitType: " + getCreditUnitType() +
			"\n	Itype: " + getItype() +
			"\n	MinutesPerWk: " + getMinutesPerWk() +
			"\n	PitInstrOfferingConfig: " + getPitInstrOfferingConfig() +
			"\n	PitParentSubpart: " + getPitParentSubpart() +
			"\n	SchedulingSubpart: " + getSchedulingSubpart() +
			"\n	SchedulingSubpartSuffixCache: " + getSchedulingSubpartSuffixCache() +
			"\n	StudentAllowOverlap: " + getStudentAllowOverlap() +
			"\n	UniqueId: " + getUniqueId() +
			"\n	UniqueIdRolledForwardFrom: " + getUniqueIdRolledForwardFrom() +
			"]";
	}
}
