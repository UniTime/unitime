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
 * This is an object that contains data related to the COURSE_REQUEST_OPTION table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="COURSE_REQUEST_OPTION"
 */

public abstract class BaseCourseRequestOption  implements Serializable {

	public static String REF = "CourseRequestOption";
	public static String PROP_OPTION_TYPE = "optionType";
	public static String PROP_VALUE = "value";


	// constructors
	public BaseCourseRequestOption () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseCourseRequestOption (java.lang.Long uniqueId) {
		this.setUniqueId(uniqueId);
		initialize();
	}

	/**
	 * Constructor for required fields
	 */
	public BaseCourseRequestOption (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.CourseRequest courseRequest,
		java.lang.Integer optionType,
		org.unitime.commons.hibernate.blob.XmlBlobType value) {

		this.setUniqueId(uniqueId);
		this.setCourseRequest(courseRequest);
		this.setOptionType(optionType);
		this.setValue(value);
		initialize();
	}

	protected void initialize () {}



	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private java.lang.Long uniqueId;

	// fields
	private java.lang.Integer optionType;
	private org.unitime.commons.hibernate.blob.XmlBlobType value;

	// many to one
	private org.unitime.timetable.model.CourseRequest courseRequest;



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
	 * Return the value associated with the column: OPTION_TYPE
	 */
	public java.lang.Integer getOptionType () {
		return optionType;
	}

	/**
	 * Set the value related to the column: OPTION_TYPE
	 * @param optionType the OPTION_TYPE value
	 */
	public void setOptionType (java.lang.Integer optionType) {
		this.optionType = optionType;
	}



	/**
	 * Return the value associated with the column: VALUE
	 */
	public org.unitime.commons.hibernate.blob.XmlBlobType getValue () {
		return value;
	}

	/**
	 * Set the value related to the column: VALUE
	 * @param value the VALUE value
	 */
	public void setValue (org.unitime.commons.hibernate.blob.XmlBlobType value) {
		this.value = value;
	}



	/**
	 * Return the value associated with the column: COURSE_REQUEST_ID
	 */
	public org.unitime.timetable.model.CourseRequest getCourseRequest () {
		return courseRequest;
	}

	/**
	 * Set the value related to the column: COURSE_REQUEST_ID
	 * @param courseRequest the COURSE_REQUEST_ID value
	 */
	public void setCourseRequest (org.unitime.timetable.model.CourseRequest courseRequest) {
		this.courseRequest = courseRequest;
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.CourseRequestOption)) return false;
		else {
			org.unitime.timetable.model.CourseRequestOption courseRequestOption = (org.unitime.timetable.model.CourseRequestOption) obj;
			if (null == this.getUniqueId() || null == courseRequestOption.getUniqueId()) return false;
			else return (this.getUniqueId().equals(courseRequestOption.getUniqueId()));
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