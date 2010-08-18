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
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.CourseOfferingReservation;
import org.unitime.timetable.model.DemandOfferingType;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.SubjectArea;

public abstract class BaseCourseOffering implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Boolean iIsControl;
	private String iPermId;
	private Integer iProjectedDemand;
	private Integer iNbrExpectedStudents;
	private Integer iDemand;
	private Integer iEnrollment;
	private String iSubjectAreaAbbv;
	private String iCourseNbr;
	private String iTitle;
	private String iScheduleBookNote;
	private String iExternalUniqueId;
	private Long iUniqueIdRolledForwardFrom;

	private SubjectArea iSubjectArea;
	private InstructionalOffering iInstructionalOffering;
	private CourseOffering iDemandOffering;
	private DemandOfferingType iDemandOfferingType;
	private Set<CourseOfferingReservation> iCourseReservations;
	private Set<AcadAreaReservation> iAcadAreaReservations;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_IS_CONTROL = "isControl";
	public static String PROP_PERM_ID = "permId";
	public static String PROP_PROJ_DEMAND = "projectedDemand";
	public static String PROP_NBR_EXPECTED_STDENTS = "nbrExpectedStudents";
	public static String PROP_LASTLIKE_DEMAND = "demand";
	public static String PROP_ENROLLMENT = "enrollment";
	public static String PROP_COURSE_NBR = "courseNbr";
	public static String PROP_TITLE = "title";
	public static String PROP_SCHEDULE_BOOK_NOTE = "scheduleBookNote";
	public static String PROP_EXTERNAL_UID = "externalUniqueId";
	public static String PROP_UID_ROLLED_FWD_FROM = "uniqueIdRolledForwardFrom";

	public BaseCourseOffering() {
		initialize();
	}

	public BaseCourseOffering(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public Boolean isIsControl() { return iIsControl; }
	public Boolean getIsControl() { return iIsControl; }
	public void setIsControl(Boolean isControl) { iIsControl = isControl; }

	public String getPermId() { return iPermId; }
	public void setPermId(String permId) { iPermId = permId; }

	public Integer getProjectedDemand() { return iProjectedDemand; }
	public void setProjectedDemand(Integer projectedDemand) { iProjectedDemand = projectedDemand; }

	public Integer getNbrExpectedStudents() { return iNbrExpectedStudents; }
	public void setNbrExpectedStudents(Integer nbrExpectedStudents) { iNbrExpectedStudents = nbrExpectedStudents; }

	public Integer getDemand() { return iDemand; }
	public void setDemand(Integer demand) { iDemand = demand; }

	public Integer getEnrollment() { return iEnrollment; }
	public void setEnrollment(Integer enrollment) { iEnrollment = enrollment; }

	public String getSubjectAreaAbbv() { return iSubjectAreaAbbv; }
	public void setSubjectAreaAbbv(String subjectAreaAbbv) { iSubjectAreaAbbv = subjectAreaAbbv; }

	public String getCourseNbr() { return iCourseNbr; }
	public void setCourseNbr(String courseNbr) { iCourseNbr = courseNbr; }

	public String getTitle() { return iTitle; }
	public void setTitle(String title) { iTitle = title; }

	public String getScheduleBookNote() { return iScheduleBookNote; }
	public void setScheduleBookNote(String scheduleBookNote) { iScheduleBookNote = scheduleBookNote; }

	public String getExternalUniqueId() { return iExternalUniqueId; }
	public void setExternalUniqueId(String externalUniqueId) { iExternalUniqueId = externalUniqueId; }

	public Long getUniqueIdRolledForwardFrom() { return iUniqueIdRolledForwardFrom; }
	public void setUniqueIdRolledForwardFrom(Long uniqueIdRolledForwardFrom) { iUniqueIdRolledForwardFrom = uniqueIdRolledForwardFrom; }

	public SubjectArea getSubjectArea() { return iSubjectArea; }
	public void setSubjectArea(SubjectArea subjectArea) { iSubjectArea = subjectArea; }

	public InstructionalOffering getInstructionalOffering() { return iInstructionalOffering; }
	public void setInstructionalOffering(InstructionalOffering instructionalOffering) { iInstructionalOffering = instructionalOffering; }

	public CourseOffering getDemandOffering() { return iDemandOffering; }
	public void setDemandOffering(CourseOffering demandOffering) { iDemandOffering = demandOffering; }

	public DemandOfferingType getDemandOfferingType() { return iDemandOfferingType; }
	public void setDemandOfferingType(DemandOfferingType demandOfferingType) { iDemandOfferingType = demandOfferingType; }

	public Set<CourseOfferingReservation> getCourseReservations() { return iCourseReservations; }
	public void setCourseReservations(Set<CourseOfferingReservation> courseReservations) { iCourseReservations = courseReservations; }
	public void addTocourseReservations(CourseOfferingReservation courseOfferingReservation) {
		if (iCourseReservations == null) iCourseReservations = new HashSet<CourseOfferingReservation>();
		iCourseReservations.add(courseOfferingReservation);
	}

	public Set<AcadAreaReservation> getAcadAreaReservations() { return iAcadAreaReservations; }
	public void setAcadAreaReservations(Set<AcadAreaReservation> acadAreaReservations) { iAcadAreaReservations = acadAreaReservations; }
	public void addToacadAreaReservations(AcadAreaReservation acadAreaReservation) {
		if (iAcadAreaReservations == null) iAcadAreaReservations = new HashSet<AcadAreaReservation>();
		iAcadAreaReservations.add(acadAreaReservation);
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof CourseOffering)) return false;
		if (getUniqueId() == null || ((CourseOffering)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((CourseOffering)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "CourseOffering["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "CourseOffering[" +
			"\n	CourseNbr: " + getCourseNbr() +
			"\n	Demand: " + getDemand() +
			"\n	DemandOffering: " + getDemandOffering() +
			"\n	DemandOfferingType: " + getDemandOfferingType() +
			"\n	Enrollment: " + getEnrollment() +
			"\n	ExternalUniqueId: " + getExternalUniqueId() +
			"\n	InstructionalOffering: " + getInstructionalOffering() +
			"\n	IsControl: " + getIsControl() +
			"\n	NbrExpectedStudents: " + getNbrExpectedStudents() +
			"\n	PermId: " + getPermId() +
			"\n	ProjectedDemand: " + getProjectedDemand() +
			"\n	ScheduleBookNote: " + getScheduleBookNote() +
			"\n	SubjectArea: " + getSubjectArea() +
			"\n	Title: " + getTitle() +
			"\n	UniqueId: " + getUniqueId() +
			"\n	UniqueIdRolledForwardFrom: " + getUniqueIdRolledForwardFrom() +
			"]";
	}
}
