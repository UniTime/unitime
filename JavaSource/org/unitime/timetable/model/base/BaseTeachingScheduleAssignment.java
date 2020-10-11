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

import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Meeting;
import org.unitime.timetable.model.TeachingScheduleAssignment;
import org.unitime.timetable.model.TeachingScheduleDivision;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseTeachingScheduleAssignment implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Integer iFirstHour;
	private Integer iLastHour;
	private Integer iClassIndex;
	private Integer iGroupIndex;
	private String iNote;

	private TeachingScheduleDivision iDivision;
	private Meeting iMeeting;
	private Set<DepartmentalInstructor> iInstructors;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_FIRST_HOUR = "firstHour";
	public static String PROP_LAST_HOUR = "lastHour";
	public static String PROP_CLASS_IDX = "classIndex";
	public static String PROP_GROUP_IDX = "groupIndex";
	public static String PROP_NOTE = "note";

	public BaseTeachingScheduleAssignment() {
		initialize();
	}

	public BaseTeachingScheduleAssignment(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public Integer getFirstHour() { return iFirstHour; }
	public void setFirstHour(Integer firstHour) { iFirstHour = firstHour; }

	public Integer getLastHour() { return iLastHour; }
	public void setLastHour(Integer lastHour) { iLastHour = lastHour; }

	public Integer getClassIndex() { return iClassIndex; }
	public void setClassIndex(Integer classIndex) { iClassIndex = classIndex; }

	public Integer getGroupIndex() { return iGroupIndex; }
	public void setGroupIndex(Integer groupIndex) { iGroupIndex = groupIndex; }

	public String getNote() { return iNote; }
	public void setNote(String note) { iNote = note; }

	public TeachingScheduleDivision getDivision() { return iDivision; }
	public void setDivision(TeachingScheduleDivision division) { iDivision = division; }

	public Meeting getMeeting() { return iMeeting; }
	public void setMeeting(Meeting meeting) { iMeeting = meeting; }

	public Set<DepartmentalInstructor> getInstructors() { return iInstructors; }
	public void setInstructors(Set<DepartmentalInstructor> instructors) { iInstructors = instructors; }
	public void addToinstructors(DepartmentalInstructor departmentalInstructor) {
		if (iInstructors == null) iInstructors = new HashSet<DepartmentalInstructor>();
		iInstructors.add(departmentalInstructor);
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof TeachingScheduleAssignment)) return false;
		if (getUniqueId() == null || ((TeachingScheduleAssignment)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((TeachingScheduleAssignment)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "TeachingScheduleAssignment["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "TeachingScheduleAssignment[" +
			"\n	ClassIndex: " + getClassIndex() +
			"\n	Division: " + getDivision() +
			"\n	FirstHour: " + getFirstHour() +
			"\n	GroupIndex: " + getGroupIndex() +
			"\n	LastHour: " + getLastHour() +
			"\n	Meeting: " + getMeeting() +
			"\n	Note: " + getNote() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
