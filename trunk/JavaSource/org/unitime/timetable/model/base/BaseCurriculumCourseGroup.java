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

import org.unitime.timetable.model.Curriculum;
import org.unitime.timetable.model.CurriculumCourseGroup;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseCurriculumCourseGroup implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iName;
	private String iColor;
	private Integer iType;

	private Curriculum iCurriculum;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_NAME = "name";
	public static String PROP_COLOR = "color";
	public static String PROP_TYPE = "type";

	public BaseCurriculumCourseGroup() {
		initialize();
	}

	public BaseCurriculumCourseGroup(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public String getName() { return iName; }
	public void setName(String name) { iName = name; }

	public String getColor() { return iColor; }
	public void setColor(String color) { iColor = color; }

	public Integer getType() { return iType; }
	public void setType(Integer type) { iType = type; }

	public Curriculum getCurriculum() { return iCurriculum; }
	public void setCurriculum(Curriculum curriculum) { iCurriculum = curriculum; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof CurriculumCourseGroup)) return false;
		if (getUniqueId() == null || ((CurriculumCourseGroup)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((CurriculumCourseGroup)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "CurriculumCourseGroup["+getUniqueId()+" "+getName()+"]";
	}

	public String toDebugString() {
		return "CurriculumCourseGroup[" +
			"\n	Color: " + getColor() +
			"\n	Curriculum: " + getCurriculum() +
			"\n	Name: " + getName() +
			"\n	Type: " + getType() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
