/*
 * UniTime 3.0 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2007, UniTime.org, and individual contributors
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


/**
 * This is an object that contains data related to the DISTRIBUTION_TYPE table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="DISTRIBUTION_TYPE"
 */

public abstract class BaseDistributionType extends org.unitime.timetable.model.RefTableEntry  implements Serializable {

	public static String REF = "DistributionType";
	public static String PROP_SEQUENCING_REQUIRED = "sequencingRequired";
	public static String PROP_REQUIREMENT_ID = "requirementId";
	public static String PROP_ALLOWED_PREF = "allowedPref";
	public static String PROP_DESCR = "descr";
	public static String PROP_ABBREVIATION = "abbreviation";
	public static String PROP_INSTRUCTOR_PREF = "instructorPref";
	public static String PROP_EXAM_PREF = "examPref";


	// constructors
	public BaseDistributionType () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseDistributionType (Long uniqueId) {
		super(uniqueId);
	}

	/**
	 * Constructor for required fields
	 */
	public BaseDistributionType (
		Long uniqueId,
		java.lang.String reference) {

		super (
			uniqueId,
			reference);
	}



	private int hashCode = Integer.MIN_VALUE;


	// fields
	private boolean sequencingRequired;
	private java.lang.Integer requirementId;
	private java.lang.String allowedPref;
	private java.lang.String descr;
	private java.lang.String abbreviation;
	private java.lang.Boolean instructorPref;
	private java.lang.Boolean examPref;

	// collections
	private java.util.Set departments;






	/**
	 * Return the value associated with the column: SEQUENCING_REQUIRED
	 */
	public boolean isSequencingRequired () {
		return sequencingRequired;
	}

	/**
	 * Set the value related to the column: SEQUENCING_REQUIRED
	 * @param sequencingRequired the SEQUENCING_REQUIRED value
	 */
	public void setSequencingRequired (boolean sequencingRequired) {
		this.sequencingRequired = sequencingRequired;
	}



	/**
	 * Return the value associated with the column: REQ_ID
	 */
	public java.lang.Integer getRequirementId () {
		return requirementId;
	}

	/**
	 * Set the value related to the column: REQ_ID
	 * @param requirementId the REQ_ID value
	 */
	public void setRequirementId (java.lang.Integer requirementId) {
		this.requirementId = requirementId;
	}



	/**
	 * Return the value associated with the column: ALLOWED_PREF
	 */
	public java.lang.String getAllowedPref () {
		return allowedPref;
	}

	/**
	 * Set the value related to the column: ALLOWED_PREF
	 * @param allowedPref the ALLOWED_PREF value
	 */
	public void setAllowedPref (java.lang.String allowedPref) {
		this.allowedPref = allowedPref;
	}



	/**
	 * Return the value associated with the column: DESCRIPTION
	 */
	public java.lang.String getDescr () {
		return descr;
	}

	/**
	 * Set the value related to the column: DESCRIPTION
	 * @param descr the DESCRIPTION value
	 */
	public void setDescr (java.lang.String descr) {
		this.descr = descr;
	}



	/**
	 * Return the value associated with the column: ABBREVIATION
	 */
	public java.lang.String getAbbreviation () {
		return abbreviation;
	}

	/**
	 * Set the value related to the column: ABBREVIATION
	 * @param abbreviation the ABBREVIATION value
	 */
	public void setAbbreviation (java.lang.String abbreviation) {
		this.abbreviation = abbreviation;
	}



	/**
	 * Return the value associated with the column: INSTRUCTOR_PREF
	 */
	public java.lang.Boolean isInstructorPref () {
		return instructorPref;
	}

	/**
	 * Set the value related to the column: INSTRUCTOR_PREF
	 * @param instructorPref the INSTRUCTOR_PREF value
	 */
	public void setInstructorPref (java.lang.Boolean instructorPref) {
		this.instructorPref = instructorPref;
	}

    public java.lang.Boolean isExamPref () {
        return examPref;
    }

    public void setExamPref (java.lang.Boolean examPref) {
        this.examPref = examPref;
    }


	/**
	 * Return the value associated with the column: departments
	 */
	public java.util.Set getDepartments () {
		return departments;
	}

	/**
	 * Set the value related to the column: departments
	 * @param departments the departments value
	 */
	public void setDepartments (java.util.Set departments) {
		this.departments = departments;
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.DistributionType)) return false;
		else {
			org.unitime.timetable.model.DistributionType distributionType = (org.unitime.timetable.model.DistributionType) obj;
			if (null == this.getUniqueId() || null == distributionType.getUniqueId()) return false;
			else return (this.getUniqueId().equals(distributionType.getUniqueId()));
		}
	}

	public int hashCode () {
		if (Integer.MIN_VALUE == this.hashCode) {
			if (null == this.getUniqueId()) return super.hashCode();
			else {
				String hashStr = this.getClass().getName() + ":" + this.getUniqueId().hashCode();
				this.hashCode = hashStr.hashCode();
			}
		}
		return this.hashCode;
	}


	public String toString () {
		return super.toString();
	}


}