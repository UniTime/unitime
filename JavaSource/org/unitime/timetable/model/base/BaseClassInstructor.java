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

import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.DepartmentalInstructor;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
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
