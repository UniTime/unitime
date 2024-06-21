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
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;

import java.io.Serializable;

import org.unitime.commons.annotations.UniqueIdGenerator;
import org.unitime.timetable.model.PitClass;
import org.unitime.timetable.model.PitClassInstructor;
import org.unitime.timetable.model.PitDepartmentalInstructor;
import org.unitime.timetable.model.TeachingResponsibility;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BasePitClassInstructor implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Integer iPercentShare;
	private Integer iNormalizedPercentShare;
	private Boolean iLead;

	private PitClass iPitClassInstructing;
	private PitDepartmentalInstructor iPitDepartmentalInstructor;
	private TeachingResponsibility iResponsibility;

	public BasePitClassInstructor() {
	}

	public BasePitClassInstructor(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@UniqueIdGenerator(sequence = "point_in_time_seq")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "percent_share", nullable = false, length = 3)
	public Integer getPercentShare() { return iPercentShare; }
	public void setPercentShare(Integer percentShare) { iPercentShare = percentShare; }

	@Column(name = "normalized_pct_share", nullable = false, length = 3)
	public Integer getNormalizedPercentShare() { return iNormalizedPercentShare; }
	public void setNormalizedPercentShare(Integer normalizedPercentShare) { iNormalizedPercentShare = normalizedPercentShare; }

	@Column(name = "is_lead", nullable = false)
	public Boolean isLead() { return iLead; }
	@Transient
	public Boolean getLead() { return iLead; }
	public void setLead(Boolean lead) { iLead = lead; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "pit_class_id", nullable = false)
	public PitClass getPitClassInstructing() { return iPitClassInstructing; }
	public void setPitClassInstructing(PitClass pitClassInstructing) { iPitClassInstructing = pitClassInstructing; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "pit_dept_instr_id", nullable = false)
	public PitDepartmentalInstructor getPitDepartmentalInstructor() { return iPitDepartmentalInstructor; }
	public void setPitDepartmentalInstructor(PitDepartmentalInstructor pitDepartmentalInstructor) { iPitDepartmentalInstructor = pitDepartmentalInstructor; }

	@ManyToOne(optional = true)
	@JoinColumn(name = "responsibility_id", nullable = true)
	public TeachingResponsibility getResponsibility() { return iResponsibility; }
	public void setResponsibility(TeachingResponsibility responsibility) { iResponsibility = responsibility; }

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof PitClassInstructor)) return false;
		if (getUniqueId() == null || ((PitClassInstructor)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((PitClassInstructor)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "PitClassInstructor["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "PitClassInstructor[" +
			"\n	Lead: " + getLead() +
			"\n	NormalizedPercentShare: " + getNormalizedPercentShare() +
			"\n	PercentShare: " + getPercentShare() +
			"\n	PitClassInstructing: " + getPitClassInstructing() +
			"\n	PitDepartmentalInstructor: " + getPitDepartmentalInstructor() +
			"\n	Responsibility: " + getResponsibility() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
