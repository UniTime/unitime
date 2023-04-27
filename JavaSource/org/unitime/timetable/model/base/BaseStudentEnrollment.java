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
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;

import java.io.Serializable;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.unitime.commons.hibernate.id.UniqueIdGenerator;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.Solution;
import org.unitime.timetable.model.StudentEnrollment;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseStudentEnrollment implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Long iStudentId;

	private Solution iSolution;
	private Class_ iClazz;

	public BaseStudentEnrollment() {
	}

	public BaseStudentEnrollment(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "student_enrl_id", type = UniqueIdGenerator.class, parameters = {
		@Parameter(name = "sequence", value = "student_enrl_seq")
	})
	@GeneratedValue(generator = "student_enrl_id")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "student_id", nullable = false, length = 20)
	public Long getStudentId() { return iStudentId; }
	public void setStudentId(Long studentId) { iStudentId = studentId; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "solution_id", nullable = false)
	public Solution getSolution() { return iSolution; }
	public void setSolution(Solution solution) { iSolution = solution; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "class_id", nullable = false)
	public Class_ getClazz() { return iClazz; }
	public void setClazz(Class_ clazz) { iClazz = clazz; }

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof StudentEnrollment)) return false;
		if (getUniqueId() == null || ((StudentEnrollment)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((StudentEnrollment)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "StudentEnrollment["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "StudentEnrollment[" +
			"\n	Clazz: " + getClazz() +
			"\n	Solution: " + getSolution() +
			"\n	StudentId: " + getStudentId() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
