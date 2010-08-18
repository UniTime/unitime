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
import org.unitime.timetable.model.CourseOfferingReservation;
import org.unitime.timetable.model.IndividualReservation;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.PosReservation;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.StudentGroupReservation;

public abstract class BaseInstrOfferingConfig implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Integer iLimit;
	private Boolean iUnlimitedEnrollment;
	private String iName;
	private Long iUniqueIdRolledForwardFrom;

	private InstructionalOffering iInstructionalOffering;
	private Set<SchedulingSubpart> iSchedulingSubparts;
	private Set<CourseOfferingReservation> iCourseReservations;
	private Set<IndividualReservation> iIndividualReservations;
	private Set<StudentGroupReservation> iStudentGroupReservations;
	private Set<AcadAreaReservation> iAcadAreaReservations;
	private Set<PosReservation> iPosReservations;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_CONFIG_LIMIT = "limit";
	public static String PROP_UNLIMITED_ENROLLMENT = "unlimitedEnrollment";
	public static String PROP_NAME = "name";
	public static String PROP_UID_ROLLED_FWD_FROM = "uniqueIdRolledForwardFrom";

	public BaseInstrOfferingConfig() {
		initialize();
	}

	public BaseInstrOfferingConfig(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public Integer getLimit() { return iLimit; }
	public void setLimit(Integer limit) { iLimit = limit; }

	public Boolean isUnlimitedEnrollment() { return iUnlimitedEnrollment; }
	public Boolean getUnlimitedEnrollment() { return iUnlimitedEnrollment; }
	public void setUnlimitedEnrollment(Boolean unlimitedEnrollment) { iUnlimitedEnrollment = unlimitedEnrollment; }

	public String getName() { return iName; }
	public void setName(String name) { iName = name; }

	public Long getUniqueIdRolledForwardFrom() { return iUniqueIdRolledForwardFrom; }
	public void setUniqueIdRolledForwardFrom(Long uniqueIdRolledForwardFrom) { iUniqueIdRolledForwardFrom = uniqueIdRolledForwardFrom; }

	public InstructionalOffering getInstructionalOffering() { return iInstructionalOffering; }
	public void setInstructionalOffering(InstructionalOffering instructionalOffering) { iInstructionalOffering = instructionalOffering; }

	public Set<SchedulingSubpart> getSchedulingSubparts() { return iSchedulingSubparts; }
	public void setSchedulingSubparts(Set<SchedulingSubpart> schedulingSubparts) { iSchedulingSubparts = schedulingSubparts; }
	public void addToschedulingSubparts(SchedulingSubpart schedulingSubpart) {
		if (iSchedulingSubparts == null) iSchedulingSubparts = new HashSet<SchedulingSubpart>();
		iSchedulingSubparts.add(schedulingSubpart);
	}

	public Set<CourseOfferingReservation> getCourseReservations() { return iCourseReservations; }
	public void setCourseReservations(Set<CourseOfferingReservation> courseReservations) { iCourseReservations = courseReservations; }
	public void addTocourseReservations(CourseOfferingReservation courseOfferingReservation) {
		if (iCourseReservations == null) iCourseReservations = new HashSet<CourseOfferingReservation>();
		iCourseReservations.add(courseOfferingReservation);
	}

	public Set<IndividualReservation> getIndividualReservations() { return iIndividualReservations; }
	public void setIndividualReservations(Set<IndividualReservation> individualReservations) { iIndividualReservations = individualReservations; }
	public void addToindividualReservations(IndividualReservation individualReservation) {
		if (iIndividualReservations == null) iIndividualReservations = new HashSet<IndividualReservation>();
		iIndividualReservations.add(individualReservation);
	}

	public Set<StudentGroupReservation> getStudentGroupReservations() { return iStudentGroupReservations; }
	public void setStudentGroupReservations(Set<StudentGroupReservation> studentGroupReservations) { iStudentGroupReservations = studentGroupReservations; }
	public void addTostudentGroupReservations(StudentGroupReservation studentGroupReservation) {
		if (iStudentGroupReservations == null) iStudentGroupReservations = new HashSet<StudentGroupReservation>();
		iStudentGroupReservations.add(studentGroupReservation);
	}

	public Set<AcadAreaReservation> getAcadAreaReservations() { return iAcadAreaReservations; }
	public void setAcadAreaReservations(Set<AcadAreaReservation> acadAreaReservations) { iAcadAreaReservations = acadAreaReservations; }
	public void addToacadAreaReservations(AcadAreaReservation acadAreaReservation) {
		if (iAcadAreaReservations == null) iAcadAreaReservations = new HashSet<AcadAreaReservation>();
		iAcadAreaReservations.add(acadAreaReservation);
	}

	public Set<PosReservation> getPosReservations() { return iPosReservations; }
	public void setPosReservations(Set<PosReservation> posReservations) { iPosReservations = posReservations; }
	public void addToposReservations(PosReservation posReservation) {
		if (iPosReservations == null) iPosReservations = new HashSet<PosReservation>();
		iPosReservations.add(posReservation);
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof InstrOfferingConfig)) return false;
		if (getUniqueId() == null || ((InstrOfferingConfig)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((InstrOfferingConfig)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "InstrOfferingConfig["+getUniqueId()+" "+getName()+"]";
	}

	public String toDebugString() {
		return "InstrOfferingConfig[" +
			"\n	InstructionalOffering: " + getInstructionalOffering() +
			"\n	Limit: " + getLimit() +
			"\n	Name: " + getName() +
			"\n	UniqueId: " + getUniqueId() +
			"\n	UniqueIdRolledForwardFrom: " + getUniqueIdRolledForwardFrom() +
			"\n	UnlimitedEnrollment: " + getUnlimitedEnrollment() +
			"]";
	}
}
