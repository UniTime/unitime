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

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_MIN_PER_WK = "minutesPerWk";
	public static String PROP_STUDENT_ALLOW_OVERLAP = "studentAllowOverlap";
	public static String PROP_SUBPART_SUFFIX = "schedulingSubpartSuffixCache";
	public static String PROP_CREDIT = "credit";
	public static String PROP_UID_ROLLED_FWD_FROM = "uniqueIdRolledForwardFrom";

	public BasePitSchedulingSubpart() {
		initialize();
	}

	public BasePitSchedulingSubpart(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public Integer getMinutesPerWk() { return iMinutesPerWk; }
	public void setMinutesPerWk(Integer minutesPerWk) { iMinutesPerWk = minutesPerWk; }

	public Boolean isStudentAllowOverlap() { return iStudentAllowOverlap; }
	public Boolean getStudentAllowOverlap() { return iStudentAllowOverlap; }
	public void setStudentAllowOverlap(Boolean studentAllowOverlap) { iStudentAllowOverlap = studentAllowOverlap; }

	public String getSchedulingSubpartSuffixCache() { return iSchedulingSubpartSuffixCache; }
	public void setSchedulingSubpartSuffixCache(String schedulingSubpartSuffixCache) { iSchedulingSubpartSuffixCache = schedulingSubpartSuffixCache; }

	public Float getCredit() { return iCredit; }
	public void setCredit(Float credit) { iCredit = credit; }

	public String getCourseName() { return iCourseName; }
	public void setCourseName(String courseName) { iCourseName = courseName; }

	public Long getUniqueIdRolledForwardFrom() { return iUniqueIdRolledForwardFrom; }
	public void setUniqueIdRolledForwardFrom(Long uniqueIdRolledForwardFrom) { iUniqueIdRolledForwardFrom = uniqueIdRolledForwardFrom; }

	public CourseCreditType getCreditType() { return iCreditType; }
	public void setCreditType(CourseCreditType creditType) { iCreditType = creditType; }

	public CourseCreditUnitType getCreditUnitType() { return iCreditUnitType; }
	public void setCreditUnitType(CourseCreditUnitType creditUnitType) { iCreditUnitType = creditUnitType; }

	public ItypeDesc getItype() { return iItype; }
	public void setItype(ItypeDesc itype) { iItype = itype; }

	public SchedulingSubpart getSchedulingSubpart() { return iSchedulingSubpart; }
	public void setSchedulingSubpart(SchedulingSubpart schedulingSubpart) { iSchedulingSubpart = schedulingSubpart; }

	public PitSchedulingSubpart getPitParentSubpart() { return iPitParentSubpart; }
	public void setPitParentSubpart(PitSchedulingSubpart pitParentSubpart) { iPitParentSubpart = pitParentSubpart; }

	public PitInstrOfferingConfig getPitInstrOfferingConfig() { return iPitInstrOfferingConfig; }
	public void setPitInstrOfferingConfig(PitInstrOfferingConfig pitInstrOfferingConfig) { iPitInstrOfferingConfig = pitInstrOfferingConfig; }

	public Set<PitSchedulingSubpart> getPitChildSubparts() { return iPitChildSubparts; }
	public void setPitChildSubparts(Set<PitSchedulingSubpart> pitChildSubparts) { iPitChildSubparts = pitChildSubparts; }
	public void addTopitChildSubparts(PitSchedulingSubpart pitSchedulingSubpart) {
		if (iPitChildSubparts == null) iPitChildSubparts = new HashSet<PitSchedulingSubpart>();
		iPitChildSubparts.add(pitSchedulingSubpart);
	}

	public Set<PitClass> getPitClasses() { return iPitClasses; }
	public void setPitClasses(Set<PitClass> pitClasses) { iPitClasses = pitClasses; }
	public void addTopitClasses(PitClass pitClass) {
		if (iPitClasses == null) iPitClasses = new HashSet<PitClass>();
		iPitClasses.add(pitClass);
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof PitSchedulingSubpart)) return false;
		if (getUniqueId() == null || ((PitSchedulingSubpart)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((PitSchedulingSubpart)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

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
