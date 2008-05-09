/* 
 * UniTime 3.1 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2008, UniTime.org
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
 * This is an object that contains data related to the RELATED_COURSE_INFO table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="RELATED_COURSE_INFO"
 */

public abstract class BaseRelatedCourseInfo  implements Serializable {

	public static String REF = "RelatedCourseInfo";
	public static String PROP_OWNER_ID = "ownerId";
	public static String PROP_OWNER_TYPE = "ownerType";


	// constructors
	public BaseRelatedCourseInfo () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseRelatedCourseInfo (java.lang.Long uniqueId) {
		this.setUniqueId(uniqueId);
		initialize();
	}

	/**
	 * Constructor for required fields
	 */
	public BaseRelatedCourseInfo (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.CourseEvent event,
		org.unitime.timetable.model.CourseOffering course,
		java.lang.Long ownerId,
		java.lang.Integer ownerType) {

		this.setUniqueId(uniqueId);
		this.setEvent(event);
		this.setCourse(course);
		this.setOwnerId(ownerId);
		this.setOwnerType(ownerType);
		initialize();
	}

	protected void initialize () {}



	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private java.lang.Long uniqueId;

	// fields
	private java.lang.Long ownerId;
	private java.lang.Integer ownerType;

	// many to one
	private org.unitime.timetable.model.CourseEvent event;
	private org.unitime.timetable.model.CourseOffering course;



	/**
	 * Return the unique identifier of this class
     * @hibernate.id
     *  generator-class="org.unitime.commons.hibernate.id.UniqueIdGenerator"
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
	 * Return the value associated with the column: OWNER_ID
	 */
	public java.lang.Long getOwnerId () {
		return ownerId;
	}

	/**
	 * Set the value related to the column: OWNER_ID
	 * @param ownerId the OWNER_ID value
	 */
	public void setOwnerId (java.lang.Long ownerId) {
		this.ownerId = ownerId;
	}



	/**
	 * Return the value associated with the column: OWNER_TYPE
	 */
	public java.lang.Integer getOwnerType () {
		return ownerType;
	}

	/**
	 * Set the value related to the column: OWNER_TYPE
	 * @param ownerType the OWNER_TYPE value
	 */
	public void setOwnerType (java.lang.Integer ownerType) {
		this.ownerType = ownerType;
	}



	/**
	 * Return the value associated with the column: EVENT_ID
	 */
	public org.unitime.timetable.model.CourseEvent getEvent () {
		return event;
	}

	/**
	 * Set the value related to the column: EVENT_ID
	 * @param event the EVENT_ID value
	 */
	public void setEvent (org.unitime.timetable.model.CourseEvent event) {
		this.event = event;
	}



	/**
	 * Return the value associated with the column: COURSE_ID
	 */
	public org.unitime.timetable.model.CourseOffering getCourse () {
		return course;
	}

	/**
	 * Set the value related to the column: COURSE_ID
	 * @param course the COURSE_ID value
	 */
	public void setCourse (org.unitime.timetable.model.CourseOffering course) {
		this.course = course;
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.RelatedCourseInfo)) return false;
		else {
			org.unitime.timetable.model.RelatedCourseInfo relatedCourseInfo = (org.unitime.timetable.model.RelatedCourseInfo) obj;
			if (null == this.getUniqueId() || null == relatedCourseInfo.getUniqueId()) return false;
			else return (this.getUniqueId().equals(relatedCourseInfo.getUniqueId()));
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