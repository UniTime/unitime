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
import java.util.HashSet;
import java.util.Set;

import org.unitime.timetable.model.AcademicArea;
import org.unitime.timetable.model.Curriculum;
import org.unitime.timetable.model.CurriculumClassification;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.PosMajor;

public abstract class BaseCurriculum implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iAbbv;
	private String iName;

	private AcademicArea iAcademicArea;
	private Department iDepartment;
	private Set<PosMajor> iMajors;
	private Set<CurriculumClassification> iClassifications;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_ABBV = "abbv";
	public static String PROP_NAME = "name";

	public BaseCurriculum() {
		initialize();
	}

	public BaseCurriculum(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public String getAbbv() { return iAbbv; }
	public void setAbbv(String abbv) { iAbbv = abbv; }

	public String getName() { return iName; }
	public void setName(String name) { iName = name; }

	public AcademicArea getAcademicArea() { return iAcademicArea; }
	public void setAcademicArea(AcademicArea academicArea) { iAcademicArea = academicArea; }

	public Department getDepartment() { return iDepartment; }
	public void setDepartment(Department department) { iDepartment = department; }

	public Set<PosMajor> getMajors() { return iMajors; }
	public void setMajors(Set<PosMajor> majors) { iMajors = majors; }
	public void addTomajors(PosMajor posMajor) {
		if (iMajors == null) iMajors = new HashSet<PosMajor>();
		iMajors.add(posMajor);
	}

	public Set<CurriculumClassification> getClassifications() { return iClassifications; }
	public void setClassifications(Set<CurriculumClassification> classifications) { iClassifications = classifications; }
	public void addToclassifications(CurriculumClassification curriculumClassification) {
		if (iClassifications == null) iClassifications = new HashSet<CurriculumClassification>();
		iClassifications.add(curriculumClassification);
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof Curriculum)) return false;
		if (getUniqueId() == null || ((Curriculum)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((Curriculum)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "Curriculum["+getUniqueId()+" "+getName()+"]";
	}

	public String toDebugString() {
		return "Curriculum[" +
			"\n	Abbv: " + getAbbv() +
			"\n	AcademicArea: " + getAcademicArea() +
			"\n	Department: " + getDepartment() +
			"\n	Name: " + getName() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
