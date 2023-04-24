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

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToMany;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
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
@MappedSuperclass
public abstract class BaseAssignment implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Integer iDays;
	private Integer iStartSlot;
	private String iClassName;

	private TimePattern iTimePattern;
	private DatePattern iDatePattern;
	private Solution iSolution;
	private Class_ iClazz;
	private Set<DepartmentalInstructor> iInstructors;
	private Set<Location> iRooms;
	private Set<AssignmentInfo> iAssignmentInfo;
	private Set<ConstraintInfo> iConstraintInfo;

	public BaseAssignment() {
	}

	public BaseAssignment(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "assignment_id", strategy = "org.unitime.commons.hibernate.id.UniqueIdGenerator", parameters = {
		@Parameter(name = "sequence", value = "assignment_seq")
	})
	@GeneratedValue(generator = "assignment_id")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "days", nullable = true, length = 4)
	public Integer getDays() { return iDays; }
	public void setDays(Integer days) { iDays = days; }

	@Column(name = "slot", nullable = true, length = 4)
	public Integer getStartSlot() { return iStartSlot; }
	public void setStartSlot(Integer startSlot) { iStartSlot = startSlot; }

	@Column(name = "class_name", nullable = true, length = 100)
	public String getClassName() { return iClassName; }
	public void setClassName(String className) { iClassName = className; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "time_pattern_id", nullable = false)
	public TimePattern getTimePattern() { return iTimePattern; }
	public void setTimePattern(TimePattern timePattern) { iTimePattern = timePattern; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "date_pattern_id", nullable = false)
	public DatePattern getDatePattern() { return iDatePattern; }
	public void setDatePattern(DatePattern datePattern) { iDatePattern = datePattern; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "solution_id", nullable = false)
	public Solution getSolution() { return iSolution; }
	public void setSolution(Solution solution) { iSolution = solution; }

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "class_id", nullable = false)
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, include = "non-lazy")
	public Class_ getClazz() { return iClazz; }
	public void setClazz(Class_ clazz) { iClazz = clazz; }

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "assigned_instructors",
		joinColumns = { @JoinColumn(name = "assignment_id") },
		inverseJoinColumns = { @JoinColumn(name = "instructor_id") })
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, include = "non-lazy")
	public Set<DepartmentalInstructor> getInstructors() { return iInstructors; }
	public void setInstructors(Set<DepartmentalInstructor> instructors) { iInstructors = instructors; }
	public void addToinstructors(DepartmentalInstructor departmentalInstructor) {
		if (iInstructors == null) iInstructors = new HashSet<DepartmentalInstructor>();
		iInstructors.add(departmentalInstructor);
	}

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "assigned_rooms",
		joinColumns = { @JoinColumn(name = "assignment_id") },
		inverseJoinColumns = { @JoinColumn(name = "room_id") })
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, include = "non-lazy")
	public Set<Location> getRooms() { return iRooms; }
	public void setRooms(Set<Location> rooms) { iRooms = rooms; }
	public void addTorooms(Location location) {
		if (iRooms == null) iRooms = new HashSet<Location>();
		iRooms.add(location);
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "assignment", cascade = {CascadeType.ALL})
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, include = "non-lazy")
	public Set<AssignmentInfo> getAssignmentInfo() { return iAssignmentInfo; }
	public void setAssignmentInfo(Set<AssignmentInfo> assignmentInfo) { iAssignmentInfo = assignmentInfo; }
	public void addToassignmentInfo(AssignmentInfo assignmentInfo) {
		if (iAssignmentInfo == null) iAssignmentInfo = new HashSet<AssignmentInfo>();
		iAssignmentInfo.add(assignmentInfo);
	}

	@ManyToMany(fetch = FetchType.LAZY, mappedBy = "assignments")
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, include = "non-lazy")
	@Cascade(value = org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
	public Set<ConstraintInfo> getConstraintInfo() { return iConstraintInfo; }
	public void setConstraintInfo(Set<ConstraintInfo> constraintInfo) { iConstraintInfo = constraintInfo; }
	public void addToconstraintInfo(ConstraintInfo constraintInfo) {
		if (iConstraintInfo == null) iConstraintInfo = new HashSet<ConstraintInfo>();
		iConstraintInfo.add(constraintInfo);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof Assignment)) return false;
		if (getUniqueId() == null || ((Assignment)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((Assignment)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "Assignment["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "Assignment[" +
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
