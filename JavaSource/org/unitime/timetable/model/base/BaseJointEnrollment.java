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

import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.JointEnrollment;
import org.unitime.timetable.model.Solution;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseJointEnrollment implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Double iJenrl;

	private Solution iSolution;
	private Class_ iClass1;
	private Class_ iClass2;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_JENRL = "jenrl";

	public BaseJointEnrollment() {
		initialize();
	}

	public BaseJointEnrollment(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public Double getJenrl() { return iJenrl; }
	public void setJenrl(Double jenrl) { iJenrl = jenrl; }

	public Solution getSolution() { return iSolution; }
	public void setSolution(Solution solution) { iSolution = solution; }

	public Class_ getClass1() { return iClass1; }
	public void setClass1(Class_ class1) { iClass1 = class1; }

	public Class_ getClass2() { return iClass2; }
	public void setClass2(Class_ class2) { iClass2 = class2; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof JointEnrollment)) return false;
		if (getUniqueId() == null || ((JointEnrollment)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((JointEnrollment)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

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
