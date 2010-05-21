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
 * This is an object that contains data related to the COURSE_DEMAND table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="COURSE_DEMAND"
 */

public abstract class BaseCourseDemand  implements Serializable {

	public static String REF = "CourseDemand";
	public static String PROP_PRIORITY = "priority";
	public static String PROP_WAITLIST = "waitlist";
	public static String PROP_ALTERNATIVE = "alternative";
	public static String PROP_TIMESTAMP = "timestamp";


	// constructors
	public BaseCourseDemand () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseCourseDemand (java.lang.Long uniqueId) {
		this.setUniqueId(uniqueId);
		initialize();
	}

	/**
	 * Constructor for required fields
	 */
	public BaseCourseDemand (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.Student student,
		java.lang.Integer priority,
		java.lang.Boolean waitlist,
		java.lang.Boolean alternative,
		java.util.Date timestamp) {

		this.setUniqueId(uniqueId);
		this.setStudent(student);
		this.setPriority(priority);
		this.setWaitlist(waitlist);
		this.setAlternative(alternative);
		this.setTimestamp(timestamp);
		initialize();
	}

	protected void initialize () {}



	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private java.lang.Long uniqueId;

	// fields
	private java.lang.Integer priority;
	private java.lang.Boolean waitlist;
	private java.lang.Boolean alternative;
	private java.util.Date timestamp;

	// many to one
	private org.unitime.timetable.model.Student student;
	private org.unitime.timetable.model.FreeTime freeTime;

	// collections
	private java.util.Set courseRequests;
    private java.util.Set enrollmentMessages;



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
	 * Return the value associated with the column: WAITLIST
	 */
	public java.lang.Boolean isWaitlist () {
		return waitlist;
	}

	/**
	 * Set the value related to the column: WAITLIST
	 * @param waitlist the WAITLIST value
	 */
	public void setWaitlist (java.lang.Boolean waitlist) {
		this.waitlist = waitlist;
	}



	/**
	 * Return the value associated with the column: IS_ALTERNATIVE
	 */
	public java.lang.Boolean isAlternative () {
		return alternative;
	}

	/**
	 * Set the value related to the column: IS_ALTERNATIVE
	 * @param alternative the IS_ALTERNATIVE value
	 */
	public void setAlternative (java.lang.Boolean alternative) {
		this.alternative = alternative;
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
	 * Return the value associated with the column: FREE_TIME_ID
	 */
	public org.unitime.timetable.model.FreeTime getFreeTime () {
		return freeTime;
	}

	/**
	 * Set the value related to the column: FREE_TIME_ID
	 * @param freeTime the FREE_TIME_ID value
	 */
	public void setFreeTime (org.unitime.timetable.model.FreeTime freeTime) {
		this.freeTime = freeTime;
	}



	/**
	 * Return the value associated with the column: courseRequests
	 */
	public java.util.Set getCourseRequests () {
		return courseRequests;
	}

	/**
	 * Set the value related to the column: courseRequests
	 * @param courseRequests the courseRequests value
	 */
	public void setCourseRequests (java.util.Set courseRequests) {
		this.courseRequests = courseRequests;
	}

	public void addTocourseRequests (org.unitime.timetable.model.CourseRequest courseRequest) {
		if (null == getCourseRequests()) setCourseRequests(new java.util.HashSet());
		getCourseRequests().add(courseRequest);
	}


    /**
     * Return the value associated with the column: enrollmentMessages
     */
    public java.util.Set getEnrollmentMessages () {
        return enrollmentMessages;
    }

    /**
     * Set the value related to the column: enrollmentMessages
     * @param enrollmentMessages the enrollmentMessages value
     */
    public void setEnrollmentMessages (java.util.Set enrollmentMessages) {
        this.enrollmentMessages = enrollmentMessages;
    }

    public void addToenrollmentMessages (org.unitime.timetable.model.StudentEnrollmentMessage studentEnrollmentMessage) {
        if (null == getEnrollmentMessages()) setEnrollmentMessages(new java.util.HashSet());
        getEnrollmentMessages().add(studentEnrollmentMessage);
    }




	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.CourseDemand)) return false;
		else {
			org.unitime.timetable.model.CourseDemand courseDemand = (org.unitime.timetable.model.CourseDemand) obj;
			if (null == this.getUniqueId() || null == courseDemand.getUniqueId()) return false;
			else return (this.getUniqueId().equals(courseDemand.getUniqueId()));
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
