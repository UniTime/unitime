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
import java.util.HashSet;
import java.util.Set;

import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.CurriculumClassification;
import org.unitime.timetable.model.CurriculumCourse;
import org.unitime.timetable.model.CurriculumCourseGroup;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseCurriculumCourse implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Float iPercShare;
	private Integer iOrd;

	private CurriculumClassification iClassification;
	private CourseOffering iCourse;
	private Set<CurriculumCourseGroup> iGroups;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_PR_SHARE = "percShare";
	public static String PROP_ORD = "ord";

	public BaseCurriculumCourse() {
		initialize();
	}

	public BaseCurriculumCourse(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public Float getPercShare() { return iPercShare; }
	public void setPercShare(Float percShare) { iPercShare = percShare; }

	public Integer getOrd() { return iOrd; }
	public void setOrd(Integer ord) { iOrd = ord; }

	public CurriculumClassification getClassification() { return iClassification; }
	public void setClassification(CurriculumClassification classification) { iClassification = classification; }

	public CourseOffering getCourse() { return iCourse; }
	public void setCourse(CourseOffering course) { iCourse = course; }

	public Set<CurriculumCourseGroup> getGroups() { return iGroups; }
	public void setGroups(Set<CurriculumCourseGroup> groups) { iGroups = groups; }
	public void addTogroups(CurriculumCourseGroup curriculumCourseGroup) {
		if (iGroups == null) iGroups = new HashSet<CurriculumCourseGroup>();
		iGroups.add(curriculumCourseGroup);
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof CurriculumCourse)) return false;
		if (getUniqueId() == null || ((CurriculumCourse)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((CurriculumCourse)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "CurriculumCourse["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "CurriculumCourse[" +
			"\n	Classification: " + getClassification() +
			"\n	Course: " + getCourse() +
			"\n	Ord: " + getOrd() +
			"\n	PercShare: " + getPercShare() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
