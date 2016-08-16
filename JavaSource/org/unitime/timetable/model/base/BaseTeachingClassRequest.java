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
import org.unitime.timetable.model.TeachingClassRequest;
import org.unitime.timetable.model.TeachingRequest;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseTeachingClassRequest implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Integer iPercentShare;
	private Boolean iLead;
	private Boolean iCanOverlap;
	private Boolean iAssignInstructor;
	private Boolean iCommon;

	private TeachingRequest iTeachingRequest;
	private Class_ iTeachingClass;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_PERCENT_SHARE = "percentShare";
	public static String PROP_IS_LEAD = "lead";
	public static String PROP_CAN_OVERLAP = "canOverlap";
	public static String PROP_ASSIGN_INSTRUCTOR = "assignInstructor";
	public static String PROP_COMMON = "common";

	public BaseTeachingClassRequest() {
		initialize();
	}

	public BaseTeachingClassRequest(Long uniqueId) {
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

	public Boolean isCanOverlap() { return iCanOverlap; }
	public Boolean getCanOverlap() { return iCanOverlap; }
	public void setCanOverlap(Boolean canOverlap) { iCanOverlap = canOverlap; }

	public Boolean isAssignInstructor() { return iAssignInstructor; }
	public Boolean getAssignInstructor() { return iAssignInstructor; }
	public void setAssignInstructor(Boolean assignInstructor) { iAssignInstructor = assignInstructor; }

	public Boolean isCommon() { return iCommon; }
	public Boolean getCommon() { return iCommon; }
	public void setCommon(Boolean common) { iCommon = common; }

	public TeachingRequest getTeachingRequest() { return iTeachingRequest; }
	public void setTeachingRequest(TeachingRequest teachingRequest) { iTeachingRequest = teachingRequest; }

	public Class_ getTeachingClass() { return iTeachingClass; }
	public void setTeachingClass(Class_ teachingClass) { iTeachingClass = teachingClass; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof TeachingClassRequest)) return false;
		if (getUniqueId() == null || ((TeachingClassRequest)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((TeachingClassRequest)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "TeachingClassRequest["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "TeachingClassRequest[" +
			"\n	AssignInstructor: " + getAssignInstructor() +
			"\n	CanOverlap: " + getCanOverlap() +
			"\n	Common: " + getCommon() +
			"\n	Lead: " + getLead() +
			"\n	PercentShare: " + getPercentShare() +
			"\n	TeachingClass: " + getTeachingClass() +
			"\n	TeachingRequest: " + getTeachingRequest() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
