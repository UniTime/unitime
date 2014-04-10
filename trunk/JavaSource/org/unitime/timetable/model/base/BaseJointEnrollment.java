/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2010 - 2014, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
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
