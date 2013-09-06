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

import org.unitime.timetable.model.AcademicArea;
import org.unitime.timetable.model.AcademicClassification;
import org.unitime.timetable.model.CurriculumProjectionRule;
import org.unitime.timetable.model.PosMajor;

public abstract class BaseCurriculumProjectionRule implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Float iProjection;

	private AcademicArea iAcademicArea;
	private PosMajor iMajor;
	private AcademicClassification iAcademicClassification;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_PROJECTION = "projection";

	public BaseCurriculumProjectionRule() {
		initialize();
	}

	public BaseCurriculumProjectionRule(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public Float getProjection() { return iProjection; }
	public void setProjection(Float projection) { iProjection = projection; }

	public AcademicArea getAcademicArea() { return iAcademicArea; }
	public void setAcademicArea(AcademicArea academicArea) { iAcademicArea = academicArea; }

	public PosMajor getMajor() { return iMajor; }
	public void setMajor(PosMajor major) { iMajor = major; }

	public AcademicClassification getAcademicClassification() { return iAcademicClassification; }
	public void setAcademicClassification(AcademicClassification academicClassification) { iAcademicClassification = academicClassification; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof CurriculumProjectionRule)) return false;
		if (getUniqueId() == null || ((CurriculumProjectionRule)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((CurriculumProjectionRule)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "CurriculumProjectionRule["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "CurriculumProjectionRule[" +
			"\n	AcademicArea: " + getAcademicArea() +
			"\n	AcademicClassification: " + getAcademicClassification() +
			"\n	Major: " + getMajor() +
			"\n	Projection: " + getProjection() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
