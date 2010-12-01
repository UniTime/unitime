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
 * This is an object that contains data related to the COURSE_REQUEST table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="COURSE_REQUEST"
 */

public abstract class BaseCourseRequest  implements Serializable {

	public static String REF = "CourseRequest";
	public static String PROP_ORDER = "order";
	public static String PROP_ALLOW_OVERLAP = "allowOverlap";
	public static String PROP_CREDIT = "credit";


	// constructors
	public BaseCourseRequest () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseCourseRequest (java.lang.Long uniqueId) {
		this.setUniqueId(uniqueId);
		initialize();
	}

	/**
	 * Constructor for required fields
	 */
	public BaseCourseRequest (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.CourseDemand courseDemand,
		org.unitime.timetable.model.CourseOffering courseOffering,
		java.lang.Integer order,
		java.lang.Boolean allowOverlap,
		java.lang.Integer credit) {

		this.setUniqueId(uniqueId);
		this.setCourseDemand(courseDemand);
		this.setCourseOffering(courseOffering);
		this.setOrder(order);
		this.setAllowOverlap(allowOverlap);
		this.setCredit(credit);
		initialize();
	}

	protected void initialize () {}



	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private java.lang.Long uniqueId;

	// fields
	private java.lang.Integer order;
	private java.lang.Boolean allowOverlap;
	private java.lang.Integer credit;

	// many to one
	private org.unitime.timetable.model.CourseDemand courseDemand;
	private org.unitime.timetable.model.CourseOffering courseOffering;

	// collections
	private java.util.Set classEnrollments;
	private java.util.Set courseRequestOptions;
    private java.util.Set classWaitLists;



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
	 * Return the value associated with the column: ALLOW_OVERLAP
	 */
	public java.lang.Boolean isAllowOverlap () {
		return allowOverlap;
	}

	/**
	 * Set the value related to the column: ALLOW_OVERLAP
	 * @param allowOverlap the ALLOW_OVERLAP value
	 */
	public void setAllowOverlap (java.lang.Boolean allowOverlap) {
		this.allowOverlap = allowOverlap;
	}



	/**
	 * Return the value associated with the column: CREDIT
	 */
	public java.lang.Integer getCredit () {
		return credit;
	}

	/**
	 * Set the value related to the column: CREDIT
	 * @param credit the CREDIT value
	 */
	public void setCredit (java.lang.Integer credit) {
		this.credit = credit;
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



	/**
	 * Return the value associated with the column: classEnrollments
	 */
	public java.util.Set getClassEnrollments () {
		return classEnrollments;
	}

	/**
	 * Set the value related to the column: classEnrollments
	 * @param classEnrollments the classEnrollments value
	 */
	public void setClassEnrollments (java.util.Set classEnrollments) {
		this.classEnrollments = classEnrollments;
	}

	public void addToclassEnrollments (org.unitime.timetable.model.StudentClassEnrollment studentClassEnrollment) {
		if (null == getClassEnrollments()) setClassEnrollments(new java.util.HashSet());
		getClassEnrollments().add(studentClassEnrollment);
	}



	/**
	 * Return the value associated with the column: courseRequestOptions
	 */
	public java.util.Set getCourseRequestOptions () {
		return courseRequestOptions;
	}

	/**
	 * Set the value related to the column: courseRequestOptions
	 * @param courseRequestOptions the courseRequestOptions value
	 */
	public void setCourseRequestOptions (java.util.Set courseRequestOptions) {
		this.courseRequestOptions = courseRequestOptions;
	}

	public void addTocourseRequestOptions (org.unitime.timetable.model.CourseRequestOption courseRequestOption) {
		if (null == getCourseRequestOptions()) setCourseRequestOptions(new java.util.HashSet());
		getCourseRequestOptions().add(courseRequestOption);
	}


    /**
     * Return the value associated with the column: classWaitLists
     */
    public java.util.Set getClassWaitLists () {
        return classWaitLists;
    }

    /**
     * Set the value related to the column: classWaitLists
     * @param classWaitLists the classWaitLists value
     */
    public void setClassWaitLists (java.util.Set classWaitLists) {
        this.classWaitLists = classWaitLists;
    }

    public void addToclassWaitLists (org.unitime.timetable.model.ClassWaitList classWaitLists) {
        if (null == getClassWaitLists()) setClassWaitLists(new java.util.HashSet());
        getClassWaitLists().add(classWaitLists);
    }



	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.CourseRequest)) return false;
		else {
			org.unitime.timetable.model.CourseRequest courseRequest = (org.unitime.timetable.model.CourseRequest) obj;
			if (null == this.getUniqueId() || null == courseRequest.getUniqueId()) return false;
			else return (this.getUniqueId().equals(courseRequest.getUniqueId()));
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
