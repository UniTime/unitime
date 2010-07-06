/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.model.base;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.unitime.timetable.model.AcadAreaReservation;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOfferingReservation;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.IndividualReservation;
import org.unitime.timetable.model.PosReservation;
import org.unitime.timetable.model.PreferenceGroup;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.StudentGroupReservation;

public abstract class BaseClass_ extends PreferenceGroup implements Serializable {
	private static final long serialVersionUID = 1L;

	private Integer iExpectedCapacity;
	private String iNotes;
	private Integer iNbrRooms;
	private Integer iSectionNumberCache;
	private Boolean iDisplayInstructor;
	private String iSchedulePrintNote;
	private String iClassSuffix;
	private Boolean iDisplayInScheduleBook;
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
	private Set<CourseOfferingReservation> iCourseReservations;
	private Set<IndividualReservation> iIndividualReservations;
	private Set<StudentGroupReservation> iStudentGroupReservations;
	private Set<AcadAreaReservation> iAcadAreaReservations;
	private Set<PosReservation> iPosReservations;

	public static String PROP_EXPECTED_CAPACITY = "expectedCapacity";
	public static String PROP_NOTES = "notes";
	public static String PROP_NBR_ROOMS = "nbrRooms";
	public static String PROP_SECTION_NUMBER = "sectionNumberCache";
	public static String PROP_DISPLAY_INSTRUCTOR = "displayInstructor";
	public static String PROP_SCHED_PRINT_NOTE = "schedulePrintNote";
	public static String PROP_CLASS_SUFFIX = "classSuffix";
	public static String PROP_DISPLAY_IN_SCHED_BOOK = "displayInScheduleBook";
	public static String PROP_MAX_EXPECTED_CAPACITY = "maxExpectedCapacity";
	public static String PROP_ROOM_RATIO = "roomRatio";
	public static String PROP_UID_ROLLED_FWD_FROM = "uniqueIdRolledForwardFrom";
	public static String PROP_EXTERNAL_UID = "externalUniqueId";
	public static String PROP_ENROLLMENT = "enrollment";

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

	public Boolean isDisplayInScheduleBook() { return iDisplayInScheduleBook; }
	public Boolean getDisplayInScheduleBook() { return iDisplayInScheduleBook; }
	public void setDisplayInScheduleBook(Boolean displayInScheduleBook) { iDisplayInScheduleBook = displayInScheduleBook; }

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
		if (iChildClasses == null) iChildClasses = new HashSet();
		iChildClasses.add(class_);
	}

	public Set<ClassInstructor> getClassInstructors() { return iClassInstructors; }
	public void setClassInstructors(Set<ClassInstructor> classInstructors) { iClassInstructors = classInstructors; }
	public void addToclassInstructors(ClassInstructor classInstructor) {
		if (iClassInstructors == null) iClassInstructors = new HashSet();
		iClassInstructors.add(classInstructor);
	}

	public Set<Assignment> getAssignments() { return iAssignments; }
	public void setAssignments(Set<Assignment> assignments) { iAssignments = assignments; }
	public void addToassignments(Assignment assignment) {
		if (iAssignments == null) iAssignments = new HashSet();
		iAssignments.add(assignment);
	}

	public Set<StudentClassEnrollment> getStudentEnrollments() { return iStudentEnrollments; }
	public void setStudentEnrollments(Set<StudentClassEnrollment> studentEnrollments) { iStudentEnrollments = studentEnrollments; }
	public void addTostudentEnrollments(StudentClassEnrollment studentClassEnrollment) {
		if (iStudentEnrollments == null) iStudentEnrollments = new HashSet();
		iStudentEnrollments.add(studentClassEnrollment);
	}

	public Set<CourseOfferingReservation> getCourseReservations() { return iCourseReservations; }
	public void setCourseReservations(Set<CourseOfferingReservation> courseReservations) { iCourseReservations = courseReservations; }
	public void addTocourseReservations(CourseOfferingReservation courseOfferingReservation) {
		if (iCourseReservations == null) iCourseReservations = new HashSet();
		iCourseReservations.add(courseOfferingReservation);
	}

	public Set<IndividualReservation> getIndividualReservations() { return iIndividualReservations; }
	public void setIndividualReservations(Set<IndividualReservation> individualReservations) { iIndividualReservations = individualReservations; }
	public void addToindividualReservations(IndividualReservation individualReservation) {
		if (iIndividualReservations == null) iIndividualReservations = new HashSet();
		iIndividualReservations.add(individualReservation);
	}

	public Set<StudentGroupReservation> getStudentGroupReservations() { return iStudentGroupReservations; }
	public void setStudentGroupReservations(Set<StudentGroupReservation> studentGroupReservations) { iStudentGroupReservations = studentGroupReservations; }
	public void addTostudentGroupReservations(StudentGroupReservation studentGroupReservation) {
		if (iStudentGroupReservations == null) iStudentGroupReservations = new HashSet();
		iStudentGroupReservations.add(studentGroupReservation);
	}

	public Set<AcadAreaReservation> getAcadAreaReservations() { return iAcadAreaReservations; }
	public void setAcadAreaReservations(Set<AcadAreaReservation> acadAreaReservations) { iAcadAreaReservations = acadAreaReservations; }
	public void addToacadAreaReservations(AcadAreaReservation acadAreaReservation) {
		if (iAcadAreaReservations == null) iAcadAreaReservations = new HashSet();
		iAcadAreaReservations.add(acadAreaReservation);
	}

	public Set<PosReservation> getPosReservations() { return iPosReservations; }
	public void setPosReservations(Set<PosReservation> posReservations) { iPosReservations = posReservations; }
	public void addToposReservations(PosReservation posReservation) {
		if (iPosReservations == null) iPosReservations = new HashSet();
		iPosReservations.add(posReservation);
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
			"\n	DisplayInScheduleBook: " + getDisplayInScheduleBook() +
			"\n	DisplayInstructor: " + getDisplayInstructor() +
			"\n	Enrollment: " + getEnrollment() +
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
