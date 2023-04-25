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
package org.unitime.timetable.model;


import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;


import org.unitime.timetable.model.base.BaseStudentAreaClassificationMajor;

@Entity
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, includeLazy = false)
@Table(name = "student_area_clasf_major")
public class StudentAreaClassificationMajor extends BaseStudentAreaClassificationMajor implements Comparable<StudentAreaClassificationMajor> {
	private static final long serialVersionUID = 1L;

	public StudentAreaClassificationMajor() {
		super();
	}

	@Override
	public int compareTo(StudentAreaClassificationMajor m) {
		int cmp = Double.compare(getWeight() == null ? 1.0 : getWeight().doubleValue(), m.getWeight() == null ? 1.0 : m.getWeight().doubleValue());
		if (cmp != 0) return - cmp;
		cmp = getAcademicArea().getAcademicAreaAbbreviation().compareTo(m.getAcademicArea().getAcademicAreaAbbreviation());
		if (cmp != 0) return cmp;
		cmp = getAcademicClassification().getCode().compareTo(m.getAcademicClassification().getCode());
		if (cmp != 0) return cmp;
		cmp = (getDegree() == null ? "" : getDegree().getReference()).compareTo(m.getDegree() == null ? "" : m.getDegree().getReference());
		if (cmp != 0) return cmp;
		cmp = (getProgram() == null ? "" : getProgram().getReference()).compareTo(m.getProgram() == null ? "" : m.getProgram().getReference());
		if (cmp != 0) return cmp;
		cmp = getMajor().getCode().compareTo(m.getMajor().getCode());
		if (cmp != 0) return cmp;
		cmp = (getConcentration() == null ? "" : getConcentration().getCode()).compareTo(m.getConcentration() == null ? "" : m.getConcentration().getCode());
		if (cmp != 0) return cmp;
		return (getUniqueId() == null ? Long.valueOf(-1) : getUniqueId()).compareTo(m.getUniqueId() == null ? -1 : m.getUniqueId());
	}

}
