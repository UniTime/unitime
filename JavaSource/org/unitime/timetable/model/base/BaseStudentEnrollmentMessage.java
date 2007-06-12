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
 * This is an object that contains data related to the STUDENT_ENRL_MSG table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="STUDENT_ENRL_MSG"
 */

public abstract class BaseStudentEnrollmentMessage  implements Serializable {

	public static String REF = "StudentEnrollmentMessage";
	public static String PROP_MESSAGE = "message";
	public static String PROP_LEVEL = "level";
	public static String PROP_TYPE = "type";
	public static String PROP_TIMESTAMP = "timestamp";
    public static String PROP_ORD = "order";


	// constructors
	public BaseStudentEnrollmentMessage () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseStudentEnrollmentMessage (java.lang.Long uniqueId) {
		this.setUniqueId(uniqueId);
		initialize();
	}

	/**
	 * Constructor for required fields
	 */
	public BaseStudentEnrollmentMessage (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.CourseDemand courseDemand,
		java.lang.String message,
		java.lang.Integer level,
		java.lang.Integer type,
        java.util.Date timestamp,
        java.lang.Integer order) {

		this.setUniqueId(uniqueId);
		this.setCourseDemand(courseDemand);
		this.setMessage(message);
		this.setLevel(level);
		this.setType(type);
		this.setTimestamp(timestamp);
        this.setOrder(order);
		initialize();
	}

	protected void initialize () {}



	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private java.lang.Long uniqueId;

	// fields
	private java.lang.String message;
	private java.lang.Integer level;
	private java.lang.Integer type;
	private java.util.Date timestamp;
    private java.lang.Integer order;

	// many to one
	private org.unitime.timetable.model.CourseDemand courseDemand;



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
	 * Return the value associated with the column: MESSAGE
	 */
	public java.lang.String getMessage () {
		return message;
	}

	/**
	 * Set the value related to the column: MESSAGE
	 * @param message the MESSAGE value
	 */
	public void setMessage (java.lang.String message) {
		this.message = message;
	}



	/**
	 * Return the value associated with the column: MSG_LEVEL
	 */
	public java.lang.Integer getLevel () {
		return level;
	}

	/**
	 * Set the value related to the column: MSG_LEVEL
	 * @param level the MSG_LEVEL value
	 */
	public void setLevel (java.lang.Integer level) {
		this.level = level;
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
     * Return the value associated with the column: ORD
     */
    public java.lang.Integer getOrder () {
        return order;
    }

    /**
     * Set the value related to the column: ORD
     * @param order the ORD value
     */
    public void setOrder (java.lang.Integer order) {
        this.order = order;
    }


	/**
	 * Return the value associated with the column: COURSE_DEMAND_ID
	 */
	public org.unitime.timetable.model.CourseDemand getCourseDemand () {
		return courseDemand;
	}

	/**
	 * Set the value related to the column: COURSE_DEMAND_ID
	 * @param courseDemand the COURSE_DEMAND_ID value
	 */
	public void setCourseDemand (org.unitime.timetable.model.CourseDemand courseDemand) {
		this.courseDemand = courseDemand;
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.StudentEnrollmentMessage)) return false;
		else {
			org.unitime.timetable.model.StudentEnrollmentMessage studentEnrollmentMessage = (org.unitime.timetable.model.StudentEnrollmentMessage) obj;
			if (null == this.getUniqueId() || null == studentEnrollmentMessage.getUniqueId()) return false;
			else return (this.getUniqueId().equals(studentEnrollmentMessage.getUniqueId()));
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