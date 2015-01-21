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

import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.AssignmentInfo;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.ConstraintInfo;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Solution;
import org.unitime.timetable.model.TimePattern;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseAssignment implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Integer iDays;
	private Integer iStartSlot;
	private Long iClassId;
	private String iClassName;

	private TimePattern iTimePattern;
	private DatePattern iDatePattern;
	private Solution iSolution;
	private Class_ iClazz;
	private Set<DepartmentalInstructor> iInstructors;
	private Set<Location> iRooms;
	private Set<AssignmentInfo> iAssignmentInfo;
	private Set<ConstraintInfo> iConstraintInfo;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_DAYS = "days";
	public static String PROP_SLOT = "startSlot";
	public static String PROP_CLASS_ID = "classId";
	public static String PROP_CLASS_NAME = "className";

	public BaseAssignment() {
		initialize();
	}

	public BaseAssignment(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public Integer getDays() { return iDays; }
	public void setDays(Integer days) { iDays = days; }

	public Integer getStartSlot() { return iStartSlot; }
	public void setStartSlot(Integer startSlot) { iStartSlot = startSlot; }

	public Long getClassId() { return iClassId; }
	public void setClassId(Long classId) { iClassId = classId; }

	public String getClassName() { return iClassName; }
	public void setClassName(String className) { iClassName = className; }

	public TimePattern getTimePattern() { return iTimePattern; }
	public void setTimePattern(TimePattern timePattern) { iTimePattern = timePattern; }

	public DatePattern getDatePattern() { return iDatePattern; }
	public void setDatePattern(DatePattern datePattern) { iDatePattern = datePattern; }

	public Solution getSolution() { return iSolution; }
	public void setSolution(Solution solution) { iSolution = solution; }

	public Class_ getClazz() { return iClazz; }
	public void setClazz(Class_ clazz) { iClazz = clazz; }

	public Set<DepartmentalInstructor> getInstructors() { return iInstructors; }
	public void setInstructors(Set<DepartmentalInstructor> instructors) { iInstructors = instructors; }
	public void addToinstructors(DepartmentalInstructor departmentalInstructor) {
		if (iInstructors == null) iInstructors = new HashSet<DepartmentalInstructor>();
		iInstructors.add(departmentalInstructor);
	}

	public Set<Location> getRooms() { return iRooms; }
	public void setRooms(Set<Location> rooms) { iRooms = rooms; }
	public void addTorooms(Location location) {
		if (iRooms == null) iRooms = new HashSet<Location>();
		iRooms.add(location);
	}

	public Set<AssignmentInfo> getAssignmentInfo() { return iAssignmentInfo; }
	public void setAssignmentInfo(Set<AssignmentInfo> assignmentInfo) { iAssignmentInfo = assignmentInfo; }
	public void addToassignmentInfo(AssignmentInfo assignmentInfo) {
		if (iAssignmentInfo == null) iAssignmentInfo = new HashSet<AssignmentInfo>();
		iAssignmentInfo.add(assignmentInfo);
	}

	public Set<ConstraintInfo> getConstraintInfo() { return iConstraintInfo; }
	public void setConstraintInfo(Set<ConstraintInfo> constraintInfo) { iConstraintInfo = constraintInfo; }
	public void addToconstraintInfo(ConstraintInfo constraintInfo) {
		if (iConstraintInfo == null) iConstraintInfo = new HashSet<ConstraintInfo>();
		iConstraintInfo.add(constraintInfo);
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof Assignment)) return false;
		if (getUniqueId() == null || ((Assignment)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((Assignment)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "Assignment["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "Assignment[" +
			"\n	ClassId: " + getClassId() +
			"\n	ClassName: " + getClassName() +
			"\n	Clazz: " + getClazz() +
			"\n	DatePattern: " + getDatePattern() +
			"\n	Days: " + getDays() +
			"\n	Solution: " + getSolution() +
			"\n	StartSlot: " + getStartSlot() +
			"\n	TimePattern: " + getTimePattern() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
