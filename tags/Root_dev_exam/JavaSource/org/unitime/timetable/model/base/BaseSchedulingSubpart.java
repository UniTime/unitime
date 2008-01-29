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
 * This is an object that contains data related to the SCHEDULING_SUBPART table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="SCHEDULING_SUBPART"
 */

public abstract class BaseSchedulingSubpart extends org.unitime.timetable.model.PreferenceGroup  implements Serializable {

	public static String REF = "SchedulingSubpart";
	public static String PROP_MINUTES_PER_WK = "minutesPerWk";
	public static String PROP_AUTO_SPREAD_IN_TIME = "autoSpreadInTime";
	public static String PROP_STUDENT_ALLOW_OVERLAP = "studentAllowOverlap";
	public static String PROP_SCHEDULING_SUBPART_SUFFIX_CACHE = "schedulingSubpartSuffixCache";
	public static String PROP_COURSE_NAME = "courseName";
	public static String PROP_LIMIT = "limit";
	public static String PROP_UNIQUE_ID_ROLLED_FORWARD_FROM = "uniqueIdRolledForwardFrom";


	// constructors
	public BaseSchedulingSubpart () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseSchedulingSubpart (java.lang.Long uniqueId) {
		super(uniqueId);
	}



	private int hashCode = Integer.MIN_VALUE;


	// fields
	private java.lang.Integer minutesPerWk;
	private java.lang.Boolean autoSpreadInTime;
	private java.lang.Boolean studentAllowOverlap;
	private java.lang.String schedulingSubpartSuffixCache;
	private java.lang.String courseName;
	private java.lang.Integer limit;
	private java.lang.Long uniqueIdRolledForwardFrom;

	// many to one
	private org.unitime.timetable.model.Session session;
	private org.unitime.timetable.model.ItypeDesc itype;
	private org.unitime.timetable.model.SchedulingSubpart parentSubpart;
	private org.unitime.timetable.model.InstrOfferingConfig instrOfferingConfig;
	private org.unitime.timetable.model.DatePattern datePattern;

	// collections
	private java.util.Set childSubparts;
	private java.util.Set classes;
	private java.util.Set creditConfigs;






	/**
	 * Return the value associated with the column: MIN_PER_WK
	 */
	public java.lang.Integer getMinutesPerWk () {
		return minutesPerWk;
	}

	/**
	 * Set the value related to the column: MIN_PER_WK
	 * @param minutesPerWk the MIN_PER_WK value
	 */
	public void setMinutesPerWk (java.lang.Integer minutesPerWk) {
		this.minutesPerWk = minutesPerWk;
	}



	/**
	 * Return the value associated with the column: AUTO_TIME_SPREAD
	 */
	public java.lang.Boolean isAutoSpreadInTime () {
		return autoSpreadInTime;
	}

	/**
	 * Set the value related to the column: AUTO_TIME_SPREAD
	 * @param autoSpreadInTime the AUTO_TIME_SPREAD value
	 */
	public void setAutoSpreadInTime (java.lang.Boolean autoSpreadInTime) {
		this.autoSpreadInTime = autoSpreadInTime;
	}



	/**
	 * Return the value associated with the column: STUDENT_ALLOW_OVERLAP
	 */
	public java.lang.Boolean isStudentAllowOverlap () {
		return studentAllowOverlap;
	}

	/**
	 * Set the value related to the column: STUDENT_ALLOW_OVERLAP
	 * @param studentAllowOverlap the STUDENT_ALLOW_OVERLAP value
	 */
	public void setStudentAllowOverlap (java.lang.Boolean studentAllowOverlap) {
		this.studentAllowOverlap = studentAllowOverlap;
	}



	/**
	 * Return the value associated with the column: SUBPART_SUFFIX
	 */
	public java.lang.String getSchedulingSubpartSuffixCache () {
		return schedulingSubpartSuffixCache;
	}

	/**
	 * Set the value related to the column: SUBPART_SUFFIX
	 * @param schedulingSubpartSuffixCache the SUBPART_SUFFIX value
	 */
	public void setSchedulingSubpartSuffixCache (java.lang.String schedulingSubpartSuffixCache) {
		this.schedulingSubpartSuffixCache = schedulingSubpartSuffixCache;
	}



	/**
	 * Return the value associated with the column: courseName
	 */
	public java.lang.String getCourseName () {
		return courseName;
	}

	/**
	 * Set the value related to the column: courseName
	 * @param courseName the courseName value
	 */
	public void setCourseName (java.lang.String courseName) {
		this.courseName = courseName;
	}



	/**
	 * Return the value associated with the column: limit
	 */
	public java.lang.Integer getLimit () {
		return limit;
	}

	/**
	 * Set the value related to the column: limit
	 * @param limit the limit value
	 */
	public void setLimit (java.lang.Integer limit) {
		this.limit = limit;
	}



	/**
	 * Return the value associated with the column: UID_ROLLED_FWD_FROM
	 */
	public java.lang.Long getUniqueIdRolledForwardFrom () {
		return uniqueIdRolledForwardFrom;
	}

	/**
	 * Set the value related to the column: UID_ROLLED_FWD_FROM
	 * @param uniqueIdRolledForwardFrom the UID_ROLLED_FWD_FROM value
	 */
	public void setUniqueIdRolledForwardFrom (java.lang.Long uniqueIdRolledForwardFrom) {
		this.uniqueIdRolledForwardFrom = uniqueIdRolledForwardFrom;
	}



	/**
	 * Return the value associated with the column: session
	 */
	public org.unitime.timetable.model.Session getSession () {
		return session;
	}

	/**
	 * Set the value related to the column: session
	 * @param session the session value
	 */
	public void setSession (org.unitime.timetable.model.Session session) {
		this.session = session;
	}



	/**
	 * Return the value associated with the column: itype
	 */
	public org.unitime.timetable.model.ItypeDesc getItype () {
		return itype;
	}

	/**
	 * Set the value related to the column: itype
	 * @param itype the itype value
	 */
	public void setItype (org.unitime.timetable.model.ItypeDesc itype) {
		this.itype = itype;
	}



	/**
	 * Return the value associated with the column: PARENT
	 */
	public org.unitime.timetable.model.SchedulingSubpart getParentSubpart () {
		return parentSubpart;
	}

	/**
	 * Set the value related to the column: PARENT
	 * @param parentSubpart the PARENT value
	 */
	public void setParentSubpart (org.unitime.timetable.model.SchedulingSubpart parentSubpart) {
		this.parentSubpart = parentSubpart;
	}



	/**
	 * Return the value associated with the column: CONFIG_ID
	 */
	public org.unitime.timetable.model.InstrOfferingConfig getInstrOfferingConfig () {
		return instrOfferingConfig;
	}

	/**
	 * Set the value related to the column: CONFIG_ID
	 * @param instrOfferingConfig the CONFIG_ID value
	 */
	public void setInstrOfferingConfig (org.unitime.timetable.model.InstrOfferingConfig instrOfferingConfig) {
		this.instrOfferingConfig = instrOfferingConfig;
	}



	/**
	 * Return the value associated with the column: DATE_PATTERN_ID
	 */
	public org.unitime.timetable.model.DatePattern getDatePattern () {
		return datePattern;
	}

	/**
	 * Set the value related to the column: DATE_PATTERN_ID
	 * @param datePattern the DATE_PATTERN_ID value
	 */
	public void setDatePattern (org.unitime.timetable.model.DatePattern datePattern) {
		this.datePattern = datePattern;
	}



	/**
	 * Return the value associated with the column: childSubparts
	 */
	public java.util.Set getChildSubparts () {
		return childSubparts;
	}

	/**
	 * Set the value related to the column: childSubparts
	 * @param childSubparts the childSubparts value
	 */
	public void setChildSubparts (java.util.Set childSubparts) {
		this.childSubparts = childSubparts;
	}

	public void addTochildSubparts (org.unitime.timetable.model.SchedulingSubpart schedulingSubpart) {
		if (null == getChildSubparts()) setChildSubparts(new java.util.HashSet());
		getChildSubparts().add(schedulingSubpart);
	}



	/**
	 * Return the value associated with the column: classes
	 */
	public java.util.Set getClasses () {
		return classes;
	}

	/**
	 * Set the value related to the column: classes
	 * @param classes the classes value
	 */
	public void setClasses (java.util.Set classes) {
		this.classes = classes;
	}

	public void addToclasses (org.unitime.timetable.model.Class_ class_) {
		if (null == getClasses()) setClasses(new java.util.HashSet());
		getClasses().add(class_);
	}



	/**
	 * Return the value associated with the column: creditConfigs
	 */
	public java.util.Set getCreditConfigs () {
		return creditConfigs;
	}

	/**
	 * Set the value related to the column: creditConfigs
	 * @param creditConfigs the creditConfigs value
	 */
	public void setCreditConfigs (java.util.Set creditConfigs) {
		this.creditConfigs = creditConfigs;
	}

	public void addTocreditConfigs (org.unitime.timetable.model.CourseCreditUnitConfig courseCreditUnitConfig) {
		if (null == getCreditConfigs()) setCreditConfigs(new java.util.HashSet());
		getCreditConfigs().add(courseCreditUnitConfig);
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.SchedulingSubpart)) return false;
		else {
			org.unitime.timetable.model.SchedulingSubpart schedulingSubpart = (org.unitime.timetable.model.SchedulingSubpart) obj;
			if (null == this.getUniqueId() || null == schedulingSubpart.getUniqueId()) return false;
			else return (this.getUniqueId().equals(schedulingSubpart.getUniqueId()));
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