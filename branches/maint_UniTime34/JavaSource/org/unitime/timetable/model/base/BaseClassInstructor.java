/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
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

import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.DepartmentalInstructor;

public abstract class BaseClassInstructor implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Integer iPercentShare;
	private Boolean iLead;

	private Class_ iClassInstructing;
	private DepartmentalInstructor iInstructor;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_PERCENT_SHARE = "percentShare";
	public static String PROP_IS_LEAD = "lead";

	public BaseClassInstructor() {
		initialize();
	}

	public BaseClassInstructor(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public Integer getPercentShare() { return iPercentShare; }
	public void setPercentShare(Integer percentShare) { iPercentShare = percentShare; }

	public Boolean isLead() { return iLead; }
	public Boolean getLead() { return iLead; }
	public void setLead(Boolean lead) { iLead = lead; }

	public Class_ getClassInstructing() { return iClassInstructing; }
	public void setClassInstructing(Class_ classInstructing) { iClassInstructing = classInstructing; }

	public DepartmentalInstructor getInstructor() { return iInstructor; }
	public void setInstructor(DepartmentalInstructor instructor) { iInstructor = instructor; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof ClassInstructor)) return false;
		if (getUniqueId() == null || ((ClassInstructor)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((ClassInstructor)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "ClassInstructor["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "ClassInstructor[" +
			"\n	ClassInstructing: " + getClassInstructing() +
			"\n	Instructor: " + getInstructor() +
			"\n	Lead: " + getLead() +
			"\n	PercentShare: " + getPercentShare() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
