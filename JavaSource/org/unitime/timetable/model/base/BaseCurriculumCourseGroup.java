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
