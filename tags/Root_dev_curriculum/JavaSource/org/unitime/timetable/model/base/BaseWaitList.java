/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime LLC, and individual contributors
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
 * This is an object that contains data related to the WAITLIST table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="WAITLIST"
 */

public abstract class BaseWaitList  implements Serializable {

	public static String REF = "WaitList";
	public static String PROP_TYPE = "type";
	public static String PROP_TIMESTAMP = "timestamp";


	// constructors
	public BaseWaitList () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseWaitList (java.lang.Long uniqueId) {
		this.setUniqueId(uniqueId);
		initialize();
	}

	/**
	 * Constructor for required fields
	 */
	public BaseWaitList (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.Student student,
		org.unitime.timetable.model.CourseOffering courseOffering,
		java.lang.Integer type,
		java.util.Date timestamp) {

		this.setUniqueId(uniqueId);
		this.setStudent(student);
		this.setCourseOffering(courseOffering);
		this.setType(type);
		this.setTimestamp(timestamp);
		initialize();
	}

	protected void initialize () {}



	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private java.lang.Long uniqueId;

	// fields
	private java.lang.Integer type;
	private java.util.Date timestamp;

	// many to one
	private org.unitime.timetable.model.Student student;
	private org.unitime.timetable.model.CourseOffering courseOffering;



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
	 * Return the value associated with the column: TYPE
	 */
	public java.lang.Integer getType () {
		return type;
	}

	/**
	 * Set the value related to the column: TYPE
	 * @param type the TYPE value
	 */
	public void setType (java.lang.Integer type) {
		this.type = type;
	}



	/**
	 * Return the value associated with the column: TIMESTAMP
	 */
	public java.util.Date getTimestamp () {
		return timestamp;
	}

	/**
	 * Set the value related to the column: TIMESTAMP
	 * @param timestamp the TIMESTAMP value
	 */
	public void setTimestamp (java.util.Date timestamp) {
		this.timestamp = timestamp;
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
	 * Return the value associated with the column: COURSE_OFFERING_ID
	 */
	public org.unitime.timetable.model.CourseOffering getCourseOffering () {
		return courseOffering;
	}

	/**
	 * Set the value related to the column: COURSE_OFFERING_ID
	 * @param courseOffering the COURSE_OFFERING_ID value
	 */
	public void setCourseOffering (org.unitime.timetable.model.CourseOffering courseOffering) {
		this.courseOffering = courseOffering;
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.WaitList)) return false;
		else {
			org.unitime.timetable.model.WaitList waitList = (org.unitime.timetable.model.WaitList) obj;
			if (null == this.getUniqueId() || null == waitList.getUniqueId()) return false;
			else return (this.getUniqueId().equals(waitList.getUniqueId()));
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
