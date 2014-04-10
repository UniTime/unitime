/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2010 - 2014, UniTime LLC, and individual contributors
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

import org.unitime.timetable.model.CourseCatalog;
import org.unitime.timetable.model.CourseSubpartCredit;
import org.unitime.timetable.model.Session;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseCourseCatalog implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iExternalUniqueId;
	private String iSubject;
	private String iCourseNumber;
	private String iTitle;
	private String iPermanentId;
	private String iApprovalType;
	private String iPreviousSubject;
	private String iPreviousCourseNumber;
	private String iCreditType;
	private String iCreditUnitType;
	private String iCreditFormat;
	private Float iFixedMinimumCredit;
	private Float iMaximumCredit;
	private Boolean iFractionalCreditAllowed;

	private Session iSession;
	private Set<CourseSubpartCredit> iSubparts;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_EXTERNAL_UID = "externalUniqueId";
	public static String PROP_SUBJECT = "subject";
	public static String PROP_COURSE_NBR = "courseNumber";
	public static String PROP_TITLE = "title";
	public static String PROP_PERM_ID = "permanentId";
	public static String PROP_APPROVAL_TYPE = "approvalType";
	public static String PROP_PREV_SUBJECT = "previousSubject";
	public static String PROP_PREV_CRS_NBR = "previousCourseNumber";
	public static String PROP_CREDIT_TYPE = "creditType";
	public static String PROP_CREDIT_UNIT_TYPE = "creditUnitType";
	public static String PROP_CREDIT_FORMAT = "creditFormat";
	public static String PROP_FIXED_MIN_CREDIT = "fixedMinimumCredit";
	public static String PROP_MAX_CREDIT = "maximumCredit";
	public static String PROP_FRAC_CREDIT_ALLOWED = "fractionalCreditAllowed";

	public BaseCourseCatalog() {
		initialize();
	}

	public BaseCourseCatalog(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public String getExternalUniqueId() { return iExternalUniqueId; }
	public void setExternalUniqueId(String externalUniqueId) { iExternalUniqueId = externalUniqueId; }

	public String getSubject() { return iSubject; }
	public void setSubject(String subject) { iSubject = subject; }

	public String getCourseNumber() { return iCourseNumber; }
	public void setCourseNumber(String courseNumber) { iCourseNumber = courseNumber; }

	public String getTitle() { return iTitle; }
	public void setTitle(String title) { iTitle = title; }

	public String getPermanentId() { return iPermanentId; }
	public void setPermanentId(String permanentId) { iPermanentId = permanentId; }

	public String getApprovalType() { return iApprovalType; }
	public void setApprovalType(String approvalType) { iApprovalType = approvalType; }

	public String getPreviousSubject() { return iPreviousSubject; }
	public void setPreviousSubject(String previousSubject) { iPreviousSubject = previousSubject; }

	public String getPreviousCourseNumber() { return iPreviousCourseNumber; }
	public void setPreviousCourseNumber(String previousCourseNumber) { iPreviousCourseNumber = previousCourseNumber; }

	public String getCreditType() { return iCreditType; }
	public void setCreditType(String creditType) { iCreditType = creditType; }

	public String getCreditUnitType() { return iCreditUnitType; }
	public void setCreditUnitType(String creditUnitType) { iCreditUnitType = creditUnitType; }

	public String getCreditFormat() { return iCreditFormat; }
	public void setCreditFormat(String creditFormat) { iCreditFormat = creditFormat; }

	public Float getFixedMinimumCredit() { return iFixedMinimumCredit; }
	public void setFixedMinimumCredit(Float fixedMinimumCredit) { iFixedMinimumCredit = fixedMinimumCredit; }

	public Float getMaximumCredit() { return iMaximumCredit; }
	public void setMaximumCredit(Float maximumCredit) { iMaximumCredit = maximumCredit; }

	public Boolean isFractionalCreditAllowed() { return iFractionalCreditAllowed; }
	public Boolean getFractionalCreditAllowed() { return iFractionalCreditAllowed; }
	public void setFractionalCreditAllowed(Boolean fractionalCreditAllowed) { iFractionalCreditAllowed = fractionalCreditAllowed; }

	public Session getSession() { return iSession; }
	public void setSession(Session session) { iSession = session; }

	public Set<CourseSubpartCredit> getSubparts() { return iSubparts; }
	public void setSubparts(Set<CourseSubpartCredit> subparts) { iSubparts = subparts; }
	public void addTosubparts(CourseSubpartCredit courseSubpartCredit) {
		if (iSubparts == null) iSubparts = new HashSet<CourseSubpartCredit>();
		iSubparts.add(courseSubpartCredit);
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof CourseCatalog)) return false;
		if (getUniqueId() == null || ((CourseCatalog)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((CourseCatalog)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "CourseCatalog["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "CourseCatalog[" +
			"\n	ApprovalType: " + getApprovalType() +
			"\n	CourseNumber: " + getCourseNumber() +
			"\n	CreditFormat: " + getCreditFormat() +
			"\n	CreditType: " + getCreditType() +
			"\n	CreditUnitType: " + getCreditUnitType() +
			"\n	ExternalUniqueId: " + getExternalUniqueId() +
			"\n	FixedMinimumCredit: " + getFixedMinimumCredit() +
			"\n	FractionalCreditAllowed: " + getFractionalCreditAllowed() +
			"\n	MaximumCredit: " + getMaximumCredit() +
			"\n	PermanentId: " + getPermanentId() +
			"\n	PreviousCourseNumber: " + getPreviousCourseNumber() +
			"\n	PreviousSubject: " + getPreviousSubject() +
			"\n	Session: " + getSession() +
			"\n	Subject: " + getSubject() +
			"\n	Title: " + getTitle() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
