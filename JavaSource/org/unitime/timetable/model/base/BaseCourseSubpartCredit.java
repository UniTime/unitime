/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
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

import org.unitime.timetable.model.CourseCatalog;
import org.unitime.timetable.model.CourseSubpartCredit;

public abstract class BaseCourseSubpartCredit implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iSubpartId;
	private String iCreditType;
	private String iCreditUnitType;
	private String iCreditFormat;
	private Float iFixedMinimumCredit;
	private Float iMaximumCredit;
	private Boolean iFractionalCreditAllowed;

	private CourseCatalog iCourseCatalog;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_SUBPART_ID = "subpartId";
	public static String PROP_CREDIT_TYPE = "creditType";
	public static String PROP_CREDIT_UNIT_TYPE = "creditUnitType";
	public static String PROP_CREDIT_FORMAT = "creditFormat";
	public static String PROP_FIXED_MIN_CREDIT = "fixedMinimumCredit";
	public static String PROP_MAX_CREDIT = "maximumCredit";
	public static String PROP_FRAC_CREDIT_ALLOWED = "fractionalCreditAllowed";

	public BaseCourseSubpartCredit() {
		initialize();
	}

	public BaseCourseSubpartCredit(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public String getSubpartId() { return iSubpartId; }
	public void setSubpartId(String subpartId) { iSubpartId = subpartId; }

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

	public CourseCatalog getCourseCatalog() { return iCourseCatalog; }
	public void setCourseCatalog(CourseCatalog courseCatalog) { iCourseCatalog = courseCatalog; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof CourseSubpartCredit)) return false;
		if (getUniqueId() == null || ((CourseSubpartCredit)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((CourseSubpartCredit)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "CourseSubpartCredit["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "CourseSubpartCredit[" +
			"\n	CourseCatalog: " + getCourseCatalog() +
			"\n	CreditFormat: " + getCreditFormat() +
			"\n	CreditType: " + getCreditType() +
			"\n	CreditUnitType: " + getCreditUnitType() +
			"\n	FixedMinimumCredit: " + getFixedMinimumCredit() +
			"\n	FractionalCreditAllowed: " + getFractionalCreditAllowed() +
			"\n	MaximumCredit: " + getMaximumCredit() +
			"\n	SubpartId: " + getSubpartId() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
