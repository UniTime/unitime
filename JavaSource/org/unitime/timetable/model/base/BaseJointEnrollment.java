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
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;

import java.io.Serializable;

import org.unitime.commons.annotations.UniqueIdGenerator;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.JointEnrollment;
import org.unitime.timetable.model.Solution;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseJointEnrollment implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Double iJenrl;

	private Solution iSolution;
	private Class_ iClass1;
	private Class_ iClass2;

	public BaseJointEnrollment() {
	}

	public BaseJointEnrollment(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@UniqueIdGenerator(sequence = "jenrl_seq")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "jenrl", nullable = false)
	public Double getJenrl() { return iJenrl; }
	public void setJenrl(Double jenrl) { iJenrl = jenrl; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "solution_id", nullable = false)
	public Solution getSolution() { return iSolution; }
	public void setSolution(Solution solution) { iSolution = solution; }

	@ManyToOne(optional = false, cascade = {CascadeType.ALL})
	@JoinColumn(name = "class1_id", nullable = false)
	public Class_ getClass1() { return iClass1; }
	public void setClass1(Class_ class1) { iClass1 = class1; }

	@ManyToOne(optional = false, cascade = {CascadeType.ALL})
	@JoinColumn(name = "class2_id", nullable = false)
	public Class_ getClass2() { return iClass2; }
	public void setClass2(Class_ class2) { iClass2 = class2; }

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof JointEnrollment)) return false;
		if (getUniqueId() == null || ((JointEnrollment)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((JointEnrollment)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "JointEnrollment["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "JointEnrollment[" +
			"\n	Class1: " + getClass1() +
			"\n	Class2: " + getClass2() +
			"\n	Jenrl: " + getJenrl() +
			"\n	Solution: " + getSolution() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
