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
 * This is an object that contains data related to the LASTLIKE_COURSE_DEMAND table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="LASTLIKE_COURSE_DEMAND"
 */

public abstract class BaseLastLikeCourseDemand  implements Serializable {

	public static String REF = "LastLikeCourseDemand";
	public static String PROP_COURSE_NBR = "courseNbr";
	public static String PROP_PRIORITY = "priority";
	public static String PROP_COURSE_PERM_ID = "coursePermId";


	// constructors
	public BaseLastLikeCourseDemand () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseLastLikeCourseDemand (java.lang.Long uniqueId) {
		this.setUniqueId(uniqueId);
		initialize();
	}

	/**
	 * Constructor for required fields
	 */
	public BaseLastLikeCourseDemand (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.Student student,
		org.unitime.timetable.model.SubjectArea subjectArea,
		java.lang.String courseNbr,
		java.lang.Integer priority) {

		this.setUniqueId(uniqueId);
		this.setStudent(student);
		this.setSubjectArea(subjectArea);
		this.setCourseNbr(courseNbr);
		this.setPriority(priority);
		initialize();
	}

	protected void initialize () {}



	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private java.lang.Long uniqueId;

	// fields
	private java.lang.String courseNbr;
	private java.lang.Integer priority;
	private java.lang.String coursePermId;

	// many to one
	private org.unitime.timetable.model.Student student;
	private org.unitime.timetable.model.SubjectArea subjectArea;



	/**
	 * Return the unique identifier of this class
     * @hibernate.id
     *  generator-class="sequence"
     *  column="UNIQUEID"
     */
	public java.lang.Long getUniqueId () {
		return uniqueId;
	}

	/**
	 * Set the unique identifier of this class
	 * @param uniqueId the new ID
	 */
	public void setUniqueId (java.lang.Long uniqueId) {
		this.uniqueId = uniqueId;
		this.hashCode = Integer.MIN_VALUE;
	}




	/**
	 * Return the value associated with the column: COURSE_NBR
	 */
	public java.lang.String getCourseNbr () {
		return courseNbr;
	}

	/**
	 * Set the value related to the column: COURSE_NBR
	 * @param courseNbr the COURSE_NBR value
	 */
	public void setCourseNbr (java.lang.String courseNbr) {
		this.courseNbr = courseNbr;
	}



	/**
	 * Return the value associated with the column: PRIORITY
	 */
	public java.lang.Integer getPriority () {
		return priority;
	}

	/**
	 * Set the value related to the column: PRIORITY
	 * @param priority the PRIORITY value
	 */
	public void setPriority (java.lang.Integer priority) {
		this.priority = priority;
	}



	/**
	 * Return the value associated with the column: COURSE_PERM_ID
	 */
	public java.lang.String getCoursePermId () {
		return coursePermId;
	}

	/**
	 * Set the value related to the column: COURSE_PERM_ID
	 * @param coursePermId the COURSE_PERM_ID value
	 */
	public void setCoursePermId (java.lang.String coursePermId) {
		this.coursePermId = coursePermId;
	}



	/**
	 * Return the value associated with the column: STUDENT_ID
	 */
	public org.unitime.timetable.model.Student getStudent () {
		return student;
	}

	/**
	 * Set the value related to the column: STUDENT_ID
	 * @param student the STUDENT_ID value
	 */
	public void setStudent (org.unitime.timetable.model.Student student) {
		this.student = student;
	}



	/**
	 * Return the value associated with the column: SUBJECT_AREA_ID
	 */
	public org.unitime.timetable.model.SubjectArea getSubjectArea () {
		return subjectArea;
	}

	/**
	 * Set the value related to the column: SUBJECT_AREA_ID
	 * @param subjectArea the SUBJECT_AREA_ID value
	 */
	public void setSubjectArea (org.unitime.timetable.model.SubjectArea subjectArea) {
		this.subjectArea = subjectArea;
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.LastLikeCourseDemand)) return false;
		else {
			org.unitime.timetable.model.LastLikeCourseDemand lastLikeCourseDemand = (org.unitime.timetable.model.LastLikeCourseDemand) obj;
			if (null == this.getUniqueId() || null == lastLikeCourseDemand.getUniqueId()) return false;
			else return (this.getUniqueId().equals(lastLikeCourseDemand.getUniqueId()));
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