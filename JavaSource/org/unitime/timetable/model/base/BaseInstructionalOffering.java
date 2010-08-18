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
import org.unitime.timetable.model.CourseCreditUnitConfig;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.CourseOfferingReservation;
import org.unitime.timetable.model.IndividualReservation;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.OfferingConsentType;
import org.unitime.timetable.model.PosReservation;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.StudentGroupReservation;

public abstract class BaseInstructionalOffering implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Integer iInstrOfferingPermId;
	private Boolean iNotOffered;
	private Integer iDemand;
	private Integer iEnrollment;
	private Integer iCtrlCourseId;
	private Integer iLimit;
	private Boolean iDesignatorRequired;
	private Long iUniqueIdRolledForwardFrom;
	private String iExternalUniqueId;

	private Session iSession;
	private OfferingConsentType iConsentType;
	private Set<CourseOffering> iCourseOfferings;
	private Set<InstrOfferingConfig> iInstrOfferingConfigs;
	private Set<CourseOfferingReservation> iCourseReservations;
	private Set<IndividualReservation> iIndividualReservations;
	private Set<StudentGroupReservation> iStudentGroupReservations;
	private Set<AcadAreaReservation> iAcadAreaReservations;
	private Set<PosReservation> iPosReservations;
	private Set<CourseCreditUnitConfig> iCreditConfigs;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_INSTR_OFFERING_PERM_ID = "instrOfferingPermId";
	public static String PROP_NOT_OFFERED = "notOffered";
	public static String PROP_DESIGNATOR_REQUIRED = "designatorRequired";
	public static String PROP_UID_ROLLED_FWD_FROM = "uniqueIdRolledForwardFrom";
	public static String PROP_EXTERNAL_UID = "externalUniqueId";

	public BaseInstructionalOffering() {
		initialize();
	}

	public BaseInstructionalOffering(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public Integer getInstrOfferingPermId() { return iInstrOfferingPermId; }
	public void setInstrOfferingPermId(Integer instrOfferingPermId) { iInstrOfferingPermId = instrOfferingPermId; }

	public Boolean isNotOffered() { return iNotOffered; }
	public Boolean getNotOffered() { return iNotOffered; }
	public void setNotOffered(Boolean notOffered) { iNotOffered = notOffered; }

	public Integer getDemand() { return iDemand; }
	public void setDemand(Integer demand) { iDemand = demand; }

	public Integer getEnrollment() { return iEnrollment; }
	public void setEnrollment(Integer enrollment) { iEnrollment = enrollment; }

	public Integer getCtrlCourseId() { return iCtrlCourseId; }
	public void setCtrlCourseId(Integer ctrlCourseId) { iCtrlCourseId = ctrlCourseId; }

	public Integer getLimit() { return iLimit; }
	public void setLimit(Integer limit) { iLimit = limit; }

	public Boolean isDesignatorRequired() { return iDesignatorRequired; }
	public Boolean getDesignatorRequired() { return iDesignatorRequired; }
	public void setDesignatorRequired(Boolean designatorRequired) { iDesignatorRequired = designatorRequired; }

	public Long getUniqueIdRolledForwardFrom() { return iUniqueIdRolledForwardFrom; }
	public void setUniqueIdRolledForwardFrom(Long uniqueIdRolledForwardFrom) { iUniqueIdRolledForwardFrom = uniqueIdRolledForwardFrom; }

	public String getExternalUniqueId() { return iExternalUniqueId; }
	public void setExternalUniqueId(String externalUniqueId) { iExternalUniqueId = externalUniqueId; }

	public Session getSession() { return iSession; }
	public void setSession(Session session) { iSession = session; }

	public OfferingConsentType getConsentType() { return iConsentType; }
	public void setConsentType(OfferingConsentType consentType) { iConsentType = consentType; }

	public Set<CourseOffering> getCourseOfferings() { return iCourseOfferings; }
	public void setCourseOfferings(Set<CourseOffering> courseOfferings) { iCourseOfferings = courseOfferings; }
	public void addTocourseOfferings(CourseOffering courseOffering) {
		if (iCourseOfferings == null) iCourseOfferings = new HashSet<CourseOffering>();
		iCourseOfferings.add(courseOffering);
	}

	public Set<InstrOfferingConfig> getInstrOfferingConfigs() { return iInstrOfferingConfigs; }
	public void setInstrOfferingConfigs(Set<InstrOfferingConfig> instrOfferingConfigs) { iInstrOfferingConfigs = instrOfferingConfigs; }
	public void addToinstrOfferingConfigs(InstrOfferingConfig instrOfferingConfig) {
		if (iInstrOfferingConfigs == null) iInstrOfferingConfigs = new HashSet<InstrOfferingConfig>();
		iInstrOfferingConfigs.add(instrOfferingConfig);
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

	public Set<CourseCreditUnitConfig> getCreditConfigs() { return iCreditConfigs; }
	public void setCreditConfigs(Set<CourseCreditUnitConfig> creditConfigs) { iCreditConfigs = creditConfigs; }
	public void addTocreditConfigs(CourseCreditUnitConfig courseCreditUnitConfig) {
		if (iCreditConfigs == null) iCreditConfigs = new HashSet<CourseCreditUnitConfig>();
		iCreditConfigs.add(courseCreditUnitConfig);
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof InstructionalOffering)) return false;
		if (getUniqueId() == null || ((InstructionalOffering)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((InstructionalOffering)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "InstructionalOffering["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "InstructionalOffering[" +
			"\n	ConsentType: " + getConsentType() +
			"\n	DesignatorRequired: " + getDesignatorRequired() +
			"\n	ExternalUniqueId: " + getExternalUniqueId() +
			"\n	InstrOfferingPermId: " + getInstrOfferingPermId() +
			"\n	NotOffered: " + getNotOffered() +
			"\n	Session: " + getSession() +
			"\n	UniqueId: " + getUniqueId() +
			"\n	UniqueIdRolledForwardFrom: " + getUniqueIdRolledForwardFrom() +
			"]";
	}
}
