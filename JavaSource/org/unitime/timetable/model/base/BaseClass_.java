/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2010 - 2013, UniTime LLC, and individual contributors
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
import java.util.HashSet;
import java.util.Set;

import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.PreferenceGroup;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.StudentClassEnrollment;

/**
 * @author Tomas Muller
 */
public abstract class BaseClass_ extends PreferenceGroup implements Serializable {
	private static final long serialVersionUID = 1L;

	private Integer iExpectedCapacity;
	private String iNotes;
	private Integer iNbrRooms;
	private Integer iSectionNumberCache;
	private Boolean iDisplayInstructor;
	private String iSchedulePrintNote;
	private String iClassSuffix;
	private Boolean iEnabledForStudentScheduling;
	private Integer iMaxExpectedCapacity;
	private Float iRoomRatio;
	private Long iUniqueIdRolledForwardFrom;
	private String iExternalUniqueId;
	private Integer iEnrollment;

	private Department iControllingDept;
	private Department iManagingDept;
	private SchedulingSubpart iSchedulingSubpart;
	private Class_ iParentClass;
	private DatePattern iDatePattern;
	private Assignment iCommittedAssignment;
	private Set<Class_> iChildClasses;
	private Set<ClassInstructor> iClassInstructors;
	private Set<Assignment> iAssignments;
	private Set<StudentClassEnrollment> iStudentEnrollments;

	public static String PROP_EXPECTED_CAPACITY = "expectedCapacity";
	public static String PROP_NOTES = "notes";
	public static String PROP_NBR_ROOMS = "nbrRooms";
	public static String PROP_SECTION_NUMBER = "sectionNumberCache";
	public static String PROP_DISPLAY_INSTRUCTOR = "displayInstructor";
	public static String PROP_SCHED_PRINT_NOTE = "schedulePrintNote";
	public static String PROP_CLASS_SUFFIX = "classSuffix";
	public static String PROP_DISPLAY_IN_SCHED_BOOK = "enabledForStudentScheduling";
	public static String PROP_MAX_EXPECTED_CAPACITY = "maxExpectedCapacity";
	public static String PROP_ROOM_RATIO = "roomRatio";
	public static String PROP_UID_ROLLED_FWD_FROM = "uniqueIdRolledForwardFrom";
	public static String PROP_EXTERNAL_UID = "externalUniqueId";

	public BaseClass_() {
		initialize();
	}

	public BaseClass_(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Integer getExpectedCapacity() { return iExpectedCapacity; }
	public void setExpectedCapacity(Integer expectedCapacity) { iExpectedCapacity = expectedCapacity; }

	public String getNotes() { return iNotes; }
	public void setNotes(String notes) { iNotes = notes; }

	public Integer getNbrRooms() { return iNbrRooms; }
	public void setNbrRooms(Integer nbrRooms) { iNbrRooms = nbrRooms; }

	public Integer getSectionNumberCache() { return iSectionNumberCache; }
	public void setSectionNumberCache(Integer sectionNumberCache) { iSectionNumberCache = sectionNumberCache; }

	public Boolean isDisplayInstructor() { return iDisplayInstructor; }
	public Boolean getDisplayInstructor() { return iDisplayInstructor; }
	public void setDisplayInstructor(Boolean displayInstructor) { iDisplayInstructor = displayInstructor; }

	public String getSchedulePrintNote() { return iSchedulePrintNote; }
	public void setSchedulePrintNote(String schedulePrintNote) { iSchedulePrintNote = schedulePrintNote; }

	public String getClassSuffix() { return iClassSuffix; }
	public void setClassSuffix(String classSuffix) { iClassSuffix = classSuffix; }

	public Boolean isEnabledForStudentScheduling() { return iEnabledForStudentScheduling; }
	public Boolean getEnabledForStudentScheduling() { return iEnabledForStudentScheduling; }
	public void setEnabledForStudentScheduling(Boolean enabledForStudentScheduling) { iEnabledForStudentScheduling = enabledForStudentScheduling; }

	public Integer getMaxExpectedCapacity() { return iMaxExpectedCapacity; }
	public void setMaxExpectedCapacity(Integer maxExpectedCapacity) { iMaxExpectedCapacity = maxExpectedCapacity; }

	public Float getRoomRatio() { return iRoomRatio; }
	public void setRoomRatio(Float roomRatio) { iRoomRatio = roomRatio; }

	public Long getUniqueIdRolledForwardFrom() { return iUniqueIdRolledForwardFrom; }
	public void setUniqueIdRolledForwardFrom(Long uniqueIdRolledForwardFrom) { iUniqueIdRolledForwardFrom = uniqueIdRolledForwardFrom; }

	public String getExternalUniqueId() { return iExternalUniqueId; }
	public void setExternalUniqueId(String externalUniqueId) { iExternalUniqueId = externalUniqueId; }

	public Integer getEnrollment() { return iEnrollment; }
	public void setEnrollment(Integer enrollment) { iEnrollment = enrollment; }

	public Department getControllingDept() { return iControllingDept; }
	public void setControllingDept(Department controllingDept) { iControllingDept = controllingDept; }

	public Department getManagingDept() { return iManagingDept; }
	public void setManagingDept(Department managingDept) { iManagingDept = managingDept; }

	public SchedulingSubpart getSchedulingSubpart() { return iSchedulingSubpart; }
	public void setSchedulingSubpart(SchedulingSubpart schedulingSubpart) { iSchedulingSubpart = schedulingSubpart; }

	public Class_ getParentClass() { return iParentClass; }
	public void setParentClass(Class_ parentClass) { iParentClass = parentClass; }

	public DatePattern getDatePattern() { return iDatePattern; }
	public void setDatePattern(DatePattern datePattern) { iDatePattern = datePattern; }

	public Assignment getCommittedAssignment() { return iCommittedAssignment; }
	public void setCommittedAssignment(Assignment committedAssignment) { iCommittedAssignment = committedAssignment; }

	public Set<Class_> getChildClasses() { return iChildClasses; }
	public void setChildClasses(Set<Class_> childClasses) { iChildClasses = childClasses; }
	public void addTochildClasses(Class_ class_) {
		if (iChildClasses == null) iChildClasses = new HashSet<Class_>();
		iChildClasses.add(class_);
	}

	public Set<ClassInstructor> getClassInstructors() { return iClassInstructors; }
	public void setClassInstructors(Set<ClassInstructor> classInstructors) { iClassInstructors = classInstructors; }
	public void addToclassInstructors(ClassInstructor classInstructor) {
		if (iClassInstructors == null) iClassInstructors = new HashSet<ClassInstructor>();
		iClassInstructors.add(classInstructor);
	}

	public Set<Assignment> getAssignments() { return iAssignments; }
	public void setAssignments(Set<Assignment> assignments) { iAssignments = assignments; }
	public void addToassignments(Assignment assignment) {
		if (iAssignments == null) iAssignments = new HashSet<Assignment>();
		iAssignments.add(assignment);
	}

	public Set<StudentClassEnrollment> getStudentEnrollments() { return iStudentEnrollments; }
	public void setStudentEnrollments(Set<StudentClassEnrollment> studentEnrollments) { iStudentEnrollments = studentEnrollments; }
	public void addTostudentEnrollments(StudentClassEnrollment studentClassEnrollment) {
		if (iStudentEnrollments == null) iStudentEnrollments = new HashSet<StudentClassEnrollment>();
		iStudentEnrollments.add(studentClassEnrollment);
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof Class_)) return false;
		if (getUniqueId() == null || ((Class_)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((Class_)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "Class_["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "Class_[" +
			"\n	ClassSuffix: " + getClassSuffix() +
			"\n	DatePattern: " + getDatePattern() +
			"\n	DisplayInstructor: " + getDisplayInstructor() +
			"\n	EnabledForStudentScheduling: " + getEnabledForStudentScheduling() +
			"\n	ExpectedCapacity: " + getExpectedCapacity() +
			"\n	ExternalUniqueId: " + getExternalUniqueId() +
			"\n	ManagingDept: " + getManagingDept() +
			"\n	MaxExpectedCapacity: " + getMaxExpectedCapacity() +
			"\n	NbrRooms: " + getNbrRooms() +
			"\n	Notes: " + getNotes() +
			"\n	ParentClass: " + getParentClass() +
			"\n	RoomRatio: " + getRoomRatio() +
			"\n	SchedulePrintNote: " + getSchedulePrintNote() +
			"\n	SchedulingSubpart: " + getSchedulingSubpart() +
			"\n	SectionNumberCache: " + getSectionNumberCache() +
			"\n	UniqueId: " + getUniqueId() +
			"\n	UniqueIdRolledForwardFrom: " + getUniqueIdRolledForwardFrom() +
			"]";
	}
}
