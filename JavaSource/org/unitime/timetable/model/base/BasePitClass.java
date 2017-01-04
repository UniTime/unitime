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

import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.PitClass;
import org.unitime.timetable.model.PitClassEvent;
import org.unitime.timetable.model.PitClassInstructor;
import org.unitime.timetable.model.PitSchedulingSubpart;
import org.unitime.timetable.model.PitStudentClassEnrollment;
import org.unitime.timetable.model.TimePattern;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BasePitClass implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Integer iLimit;
	private Integer iNbrRooms;
	private Boolean iEnabledForStudentScheduling;
	private Integer iSectionNumber;
	private String iClassSuffix;
	private Long iUniqueIdRolledForwardFrom;
	private String iExternalUniqueId;
	private Integer iEnrollment;

	private Department iControllingDept;
	private Class_ iClazz;
	private Department iManagingDept;
	private PitSchedulingSubpart iPitSchedulingSubpart;
	private PitClass iPitParentClass;
	private DatePattern iDatePattern;
	private TimePattern iTimePattern;
	private Set<PitClass> iPitChildClasses;
	private Set<PitClassInstructor> iPitClassInstructors;
	private Set<PitStudentClassEnrollment> iStudentEnrollments;
	private Set<PitClassEvent> iPitClassEvents;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_CLASS_LIMIT = "limit";
	public static String PROP_NBR_ROOMS = "nbrRooms";
	public static String PROP_ENABLED_FOR_STU_SCHED = "enabledForStudentScheduling";
	public static String PROP_SECTION_NUMBER = "sectionNumber";
	public static String PROP_CLASS_SUFFIX = "classSuffix";
	public static String PROP_UID_ROLLED_FWD_FROM = "uniqueIdRolledForwardFrom";
	public static String PROP_EXTERNAL_UID = "externalUniqueId";

	public BasePitClass() {
		initialize();
	}

	public BasePitClass(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public Integer getLimit() { return iLimit; }
	public void setLimit(Integer limit) { iLimit = limit; }

	public Integer getNbrRooms() { return iNbrRooms; }
	public void setNbrRooms(Integer nbrRooms) { iNbrRooms = nbrRooms; }

	public Boolean isEnabledForStudentScheduling() { return iEnabledForStudentScheduling; }
	public Boolean getEnabledForStudentScheduling() { return iEnabledForStudentScheduling; }
	public void setEnabledForStudentScheduling(Boolean enabledForStudentScheduling) { iEnabledForStudentScheduling = enabledForStudentScheduling; }

	public Integer getSectionNumber() { return iSectionNumber; }
	public void setSectionNumber(Integer sectionNumber) { iSectionNumber = sectionNumber; }

	public String getClassSuffix() { return iClassSuffix; }
	public void setClassSuffix(String classSuffix) { iClassSuffix = classSuffix; }

	public Long getUniqueIdRolledForwardFrom() { return iUniqueIdRolledForwardFrom; }
	public void setUniqueIdRolledForwardFrom(Long uniqueIdRolledForwardFrom) { iUniqueIdRolledForwardFrom = uniqueIdRolledForwardFrom; }

	public String getExternalUniqueId() { return iExternalUniqueId; }
	public void setExternalUniqueId(String externalUniqueId) { iExternalUniqueId = externalUniqueId; }

	public Integer getEnrollment() { return iEnrollment; }
	public void setEnrollment(Integer enrollment) { iEnrollment = enrollment; }

	public Department getControllingDept() { return iControllingDept; }
	public void setControllingDept(Department controllingDept) { iControllingDept = controllingDept; }

	public Class_ getClazz() { return iClazz; }
	public void setClazz(Class_ clazz) { iClazz = clazz; }

	public Department getManagingDept() { return iManagingDept; }
	public void setManagingDept(Department managingDept) { iManagingDept = managingDept; }

	public PitSchedulingSubpart getPitSchedulingSubpart() { return iPitSchedulingSubpart; }
	public void setPitSchedulingSubpart(PitSchedulingSubpart pitSchedulingSubpart) { iPitSchedulingSubpart = pitSchedulingSubpart; }

	public PitClass getPitParentClass() { return iPitParentClass; }
	public void setPitParentClass(PitClass pitParentClass) { iPitParentClass = pitParentClass; }

	public DatePattern getDatePattern() { return iDatePattern; }
	public void setDatePattern(DatePattern datePattern) { iDatePattern = datePattern; }

	public TimePattern getTimePattern() { return iTimePattern; }
	public void setTimePattern(TimePattern timePattern) { iTimePattern = timePattern; }

	public Set<PitClass> getPitChildClasses() { return iPitChildClasses; }
	public void setPitChildClasses(Set<PitClass> pitChildClasses) { iPitChildClasses = pitChildClasses; }
	public void addTopitChildClasses(PitClass pitClass) {
		if (iPitChildClasses == null) iPitChildClasses = new HashSet<PitClass>();
		iPitChildClasses.add(pitClass);
	}

	public Set<PitClassInstructor> getPitClassInstructors() { return iPitClassInstructors; }
	public void setPitClassInstructors(Set<PitClassInstructor> pitClassInstructors) { iPitClassInstructors = pitClassInstructors; }
	public void addTopitClassInstructors(PitClassInstructor pitClassInstructor) {
		if (iPitClassInstructors == null) iPitClassInstructors = new HashSet<PitClassInstructor>();
		iPitClassInstructors.add(pitClassInstructor);
	}

	public Set<PitStudentClassEnrollment> getStudentEnrollments() { return iStudentEnrollments; }
	public void setStudentEnrollments(Set<PitStudentClassEnrollment> studentEnrollments) { iStudentEnrollments = studentEnrollments; }
	public void addTostudentEnrollments(PitStudentClassEnrollment pitStudentClassEnrollment) {
		if (iStudentEnrollments == null) iStudentEnrollments = new HashSet<PitStudentClassEnrollment>();
		iStudentEnrollments.add(pitStudentClassEnrollment);
	}

	public Set<PitClassEvent> getPitClassEvents() { return iPitClassEvents; }
	public void setPitClassEvents(Set<PitClassEvent> pitClassEvents) { iPitClassEvents = pitClassEvents; }
	public void addTopitClassEvents(PitClassEvent pitClassEvent) {
		if (iPitClassEvents == null) iPitClassEvents = new HashSet<PitClassEvent>();
		iPitClassEvents.add(pitClassEvent);
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof PitClass)) return false;
		if (getUniqueId() == null || ((PitClass)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((PitClass)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "PitClass["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "PitClass[" +
			"\n	ClassSuffix: " + getClassSuffix() +
			"\n	Clazz: " + getClazz() +
			"\n	DatePattern: " + getDatePattern() +
			"\n	EnabledForStudentScheduling: " + getEnabledForStudentScheduling() +
			"\n	ExternalUniqueId: " + getExternalUniqueId() +
			"\n	Limit: " + getLimit() +
			"\n	ManagingDept: " + getManagingDept() +
			"\n	NbrRooms: " + getNbrRooms() +
			"\n	PitParentClass: " + getPitParentClass() +
			"\n	PitSchedulingSubpart: " + getPitSchedulingSubpart() +
			"\n	SectionNumber: " + getSectionNumber() +
			"\n	TimePattern: " + getTimePattern() +
			"\n	UniqueId: " + getUniqueId() +
			"\n	UniqueIdRolledForwardFrom: " + getUniqueIdRolledForwardFrom() +
			"]";
	}
}
