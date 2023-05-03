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
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.unitime.commons.hibernate.id.UniqueIdGenerator;
import org.unitime.timetable.model.AcademicArea;
import org.unitime.timetable.model.PosMajor;
import org.unitime.timetable.model.PosMinor;
import org.unitime.timetable.model.Session;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseAcademicArea implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iExternalUniqueId;
	private String iAcademicAreaAbbreviation;
	private String iTitle;

	private Session iSession;
	private Set<PosMajor> iPosMajors;
	private Set<PosMinor> iPosMinors;

	public BaseAcademicArea() {
	}

	public BaseAcademicArea(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "academic_area_id", type = UniqueIdGenerator.class, parameters = {
		@Parameter(name = "sequence", value = "academic_area_seq")
	})
	@GeneratedValue(generator = "academic_area_id")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "external_uid", nullable = true, length = 40)
	public String getExternalUniqueId() { return iExternalUniqueId; }
	public void setExternalUniqueId(String externalUniqueId) { iExternalUniqueId = externalUniqueId; }

	@Column(name = "academic_area_abbreviation", nullable = false, length = 40)
	public String getAcademicAreaAbbreviation() { return iAcademicAreaAbbreviation; }
	public void setAcademicAreaAbbreviation(String academicAreaAbbreviation) { iAcademicAreaAbbreviation = academicAreaAbbreviation; }

	@Column(name = "long_title", nullable = false, length = 100)
	public String getTitle() { return iTitle; }
	public void setTitle(String title) { iTitle = title; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "session_id", nullable = false)
	public Session getSession() { return iSession; }
	public void setSession(Session session) { iSession = session; }

	@ManyToMany(fetch = FetchType.LAZY, mappedBy = "academicAreas")
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	public Set<PosMajor> getPosMajors() { return iPosMajors; }
	public void setPosMajors(Set<PosMajor> posMajors) { iPosMajors = posMajors; }
	public void addToPosMajors(PosMajor posMajor) {
		if (iPosMajors == null) iPosMajors = new HashSet<PosMajor>();
		iPosMajors.add(posMajor);
	}
	@Deprecated
	public void addToposMajors(PosMajor posMajor) {
		addToPosMajors(posMajor);
	}

	@ManyToMany(fetch = FetchType.LAZY, mappedBy = "academicAreas")
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	public Set<PosMinor> getPosMinors() { return iPosMinors; }
	public void setPosMinors(Set<PosMinor> posMinors) { iPosMinors = posMinors; }
	public void addToPosMinors(PosMinor posMinor) {
		if (iPosMinors == null) iPosMinors = new HashSet<PosMinor>();
		iPosMinors.add(posMinor);
	}
	@Deprecated
	public void addToposMinors(PosMinor posMinor) {
		addToPosMinors(posMinor);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof AcademicArea)) return false;
		if (getUniqueId() == null || ((AcademicArea)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((AcademicArea)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "AcademicArea["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "AcademicArea[" +
			"\n	AcademicAreaAbbreviation: " + getAcademicAreaAbbreviation() +
			"\n	ExternalUniqueId: " + getExternalUniqueId() +
			"\n	Session: " + getSession() +
			"\n	Title: " + getTitle() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
