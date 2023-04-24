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

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

import java.io.Serializable;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.SectioningInfo;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseSectioningInfo implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Double iNbrExpectedStudents;
	private Double iNbrHoldingStudents;

	private Class_ iClazz;

	public BaseSectioningInfo() {
	}

	public BaseSectioningInfo(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "sectioning_info_id", strategy = "org.unitime.commons.hibernate.id.UniqueIdGenerator", parameters = {
		@Parameter(name = "sequence", value = "pref_group_seq")
	})
	@GeneratedValue(generator = "sectioning_info_id")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "nbr_exp_students", nullable = false)
	public Double getNbrExpectedStudents() { return iNbrExpectedStudents; }
	public void setNbrExpectedStudents(Double nbrExpectedStudents) { iNbrExpectedStudents = nbrExpectedStudents; }

	@Column(name = "nbr_hold_students", nullable = false)
	public Double getNbrHoldingStudents() { return iNbrHoldingStudents; }
	public void setNbrHoldingStudents(Double nbrHoldingStudents) { iNbrHoldingStudents = nbrHoldingStudents; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "class_id", nullable = false)
	public Class_ getClazz() { return iClazz; }
	public void setClazz(Class_ clazz) { iClazz = clazz; }

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof SectioningInfo)) return false;
		if (getUniqueId() == null || ((SectioningInfo)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((SectioningInfo)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "SectioningInfo["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "SectioningInfo[" +
			"\n	Clazz: " + getClazz() +
			"\n	NbrExpectedStudents: " + getNbrExpectedStudents() +
			"\n	NbrHoldingStudents: " + getNbrHoldingStudents() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
