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
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseCreditUnitConfig;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.ItypeDesc;
import org.unitime.timetable.model.PreferenceGroup;
import org.unitime.timetable.model.SchedulingSubpart;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseSchedulingSubpart extends PreferenceGroup implements Serializable {
	private static final long serialVersionUID = 1L;

	private Integer iMinutesPerWk;
	private Boolean iAutoSpreadInTime;
	private Boolean iStudentAllowOverlap;
	private String iSchedulingSubpartSuffixCache;
	private String iCourseName;
	private Integer iLimit;
	private Long iUniqueIdRolledForwardFrom;

	private ItypeDesc iItype;
	private SchedulingSubpart iParentSubpart;
	private InstrOfferingConfig iInstrOfferingConfig;
	private DatePattern iDatePattern;
	private Set<SchedulingSubpart> iChildSubparts;
	private Set<Class_> iClasses;
	private Set<CourseCreditUnitConfig> iCreditConfigs;

	public BaseSchedulingSubpart() {
	}

	public BaseSchedulingSubpart(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Column(name = "min_per_wk", nullable = false, length = 4)
	public Integer getMinutesPerWk() { return iMinutesPerWk; }
	public void setMinutesPerWk(Integer minutesPerWk) { iMinutesPerWk = minutesPerWk; }

	@Column(name = "auto_time_spread", nullable = false)
	public Boolean isAutoSpreadInTime() { return iAutoSpreadInTime; }
	@Transient
	public Boolean getAutoSpreadInTime() { return iAutoSpreadInTime; }
	public void setAutoSpreadInTime(Boolean autoSpreadInTime) { iAutoSpreadInTime = autoSpreadInTime; }

	@Column(name = "student_allow_overlap", nullable = false)
	public Boolean isStudentAllowOverlap() { return iStudentAllowOverlap; }
	@Transient
	public Boolean getStudentAllowOverlap() { return iStudentAllowOverlap; }
	public void setStudentAllowOverlap(Boolean studentAllowOverlap) { iStudentAllowOverlap = studentAllowOverlap; }

	@Column(name = "subpart_suffix", nullable = true, length = 5)
	public String getSchedulingSubpartSuffixCache() { return iSchedulingSubpartSuffixCache; }
	public void setSchedulingSubpartSuffixCache(String schedulingSubpartSuffixCache) { iSchedulingSubpartSuffixCache = schedulingSubpartSuffixCache; }

	@Formula("(select concat( concat( sa.subject_area_abbreviation , ' ') , co.course_nbr) from %SCHEMA%.scheduling_subpart s, %SCHEMA%.instr_offering_config c, %SCHEMA%.instructional_offering io, %SCHEMA%.course_offering co, %SCHEMA%.subject_area sa where s.uniqueid=uniqueid and s.config_id=c.uniqueid and c.instr_offr_id=io.uniqueid and co.is_control = %TRUE% and co.instr_offr_id=io.uniqueid and co.subject_area_id=sa.uniqueid)")
	public String getCourseName() { return iCourseName; }
	public void setCourseName(String courseName) { iCourseName = courseName; }

	@Formula(" ( select sum(crs.expected_capacity) from %SCHEMA%.class_ crs where crs.subpart_id = uniqueid ) ")
	public Integer getLimit() { return iLimit; }
	public void setLimit(Integer limit) { iLimit = limit; }

	@Column(name = "uid_rolled_fwd_from", nullable = true, length = 20)
	public Long getUniqueIdRolledForwardFrom() { return iUniqueIdRolledForwardFrom; }
	public void setUniqueIdRolledForwardFrom(Long uniqueIdRolledForwardFrom) { iUniqueIdRolledForwardFrom = uniqueIdRolledForwardFrom; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "itype", nullable = false)
	public ItypeDesc getItype() { return iItype; }
	public void setItype(ItypeDesc itype) { iItype = itype; }

	@ManyToOne(optional = true)
	@JoinColumn(name = "parent", nullable = true)
	public SchedulingSubpart getParentSubpart() { return iParentSubpart; }
	public void setParentSubpart(SchedulingSubpart parentSubpart) { iParentSubpart = parentSubpart; }

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "config_id", nullable = false)
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, includeLazy = false)
	public InstrOfferingConfig getInstrOfferingConfig() { return iInstrOfferingConfig; }
	public void setInstrOfferingConfig(InstrOfferingConfig instrOfferingConfig) { iInstrOfferingConfig = instrOfferingConfig; }

	@ManyToOne(optional = true)
	@JoinColumn(name = "date_pattern_id", nullable = true)
	public DatePattern getDatePattern() { return iDatePattern; }
	public void setDatePattern(DatePattern datePattern) { iDatePattern = datePattern; }

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "parentSubpart", cascade = {CascadeType.ALL}, orphanRemoval = true)
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, includeLazy = false)
	public Set<SchedulingSubpart> getChildSubparts() { return iChildSubparts; }
	public void setChildSubparts(Set<SchedulingSubpart> childSubparts) { iChildSubparts = childSubparts; }
	public void addToChildSubparts(SchedulingSubpart schedulingSubpart) {
		if (iChildSubparts == null) iChildSubparts = new HashSet<SchedulingSubpart>();
		iChildSubparts.add(schedulingSubpart);
	}
	@Deprecated
	public void addTochildSubparts(SchedulingSubpart schedulingSubpart) {
		addToChildSubparts(schedulingSubpart);
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "schedulingSubpart", cascade = {CascadeType.ALL}, orphanRemoval = true)
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, includeLazy = false)
	public Set<Class_> getClasses() { return iClasses; }
	public void setClasses(Set<Class_> classes) { iClasses = classes; }
	public void addToClasses(Class_ class_) {
		if (iClasses == null) iClasses = new HashSet<Class_>();
		iClasses.add(class_);
	}
	@Deprecated
	public void addToclasses(Class_ class_) {
		addToClasses(class_);
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "subpartOwner", cascade = {CascadeType.ALL}, orphanRemoval = true)
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, includeLazy = false)
	public Set<CourseCreditUnitConfig> getCreditConfigs() { return iCreditConfigs; }
	public void setCreditConfigs(Set<CourseCreditUnitConfig> creditConfigs) { iCreditConfigs = creditConfigs; }
	public void addToCreditConfigs(CourseCreditUnitConfig courseCreditUnitConfig) {
		if (iCreditConfigs == null) iCreditConfigs = new HashSet<CourseCreditUnitConfig>();
		iCreditConfigs.add(courseCreditUnitConfig);
	}
	@Deprecated
	public void addTocreditConfigs(CourseCreditUnitConfig courseCreditUnitConfig) {
		addToCreditConfigs(courseCreditUnitConfig);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof SchedulingSubpart)) return false;
		if (getUniqueId() == null || ((SchedulingSubpart)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((SchedulingSubpart)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "SchedulingSubpart["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "SchedulingSubpart[" +
			"\n	AutoSpreadInTime: " + getAutoSpreadInTime() +
			"\n	DatePattern: " + getDatePattern() +
			"\n	InstrOfferingConfig: " + getInstrOfferingConfig() +
			"\n	Itype: " + getItype() +
			"\n	MinutesPerWk: " + getMinutesPerWk() +
			"\n	ParentSubpart: " + getParentSubpart() +
			"\n	SchedulingSubpartSuffixCache: " + getSchedulingSubpartSuffixCache() +
			"\n	StudentAllowOverlap: " + getStudentAllowOverlap() +
			"\n	UniqueId: " + getUniqueId() +
			"\n	UniqueIdRolledForwardFrom: " + getUniqueIdRolledForwardFrom() +
			"]";
	}
}
