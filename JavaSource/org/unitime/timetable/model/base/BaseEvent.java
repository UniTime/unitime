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
 * This is an object that contains data related to the EVENT table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="EVENT"
 */

public abstract class BaseEvent  implements Serializable {

	public static String REF = "Event";
	public static String PROP_EVENT_NAME = "eventName";
	public static String PROP_MIN_CAPACITY = "minCapacity";
	public static String PROP_MAX_CAPACITY = "maxCapacity";


	// constructors
	public BaseEvent () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseEvent (java.lang.Long uniqueId) {
		this.setUniqueId(uniqueId);
		initialize();
	}

	/**
	 * Constructor for required fields
	 */
	public BaseEvent (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.EventType eventType,
		java.lang.Integer minCapacity,
		java.lang.Integer maxCapacity) {

		this.setUniqueId(uniqueId);
		this.setEventType(eventType);
		this.setMinCapacity(minCapacity);
		this.setMaxCapacity(maxCapacity);
		initialize();
	}

	protected void initialize () {}



	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private java.lang.Long uniqueId;

	// fields
	private java.lang.String eventName;
	private java.lang.Integer minCapacity;
	private java.lang.Integer maxCapacity;

	// many to one
	private org.unitime.timetable.model.EventType eventType;
	private org.unitime.timetable.model.EventContact mainContact;

	// collections
	private java.util.Set additionalContacts;
	private java.util.Set notes;
	private java.util.Set relatedCourses;
	private java.util.Set meetings;



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
	 * Return the value associated with the column: EVENT_NAME
	 */
	public java.lang.String getEventName () {
		return eventName;
	}

	/**
	 * Set the value related to the column: EVENT_NAME
	 * @param eventName the EVENT_NAME value
	 */
	public void setEventName (java.lang.String eventName) {
		this.eventName = eventName;
	}



	/**
	 * Return the value associated with the column: MIN_CAPACITY
	 */
	public java.lang.Integer getMinCapacity () {
		return minCapacity;
	}

	/**
	 * Set the value related to the column: MIN_CAPACITY
	 * @param minCapacity the MIN_CAPACITY value
	 */
	public void setMinCapacity (java.lang.Integer minCapacity) {
		this.minCapacity = minCapacity;
	}



	/**
	 * Return the value associated with the column: MAX_CAPACITY
	 */
	public java.lang.Integer getMaxCapacity () {
		return maxCapacity;
	}

	/**
	 * Set the value related to the column: MAX_CAPACITY
	 * @param maxCapacity the MAX_CAPACITY value
	 */
	public void setMaxCapacity (java.lang.Integer maxCapacity) {
		this.maxCapacity = maxCapacity;
	}



	/**
	 * Return the value associated with the column: EVENT_TYPE
	 */
	public org.unitime.timetable.model.EventType getEventType () {
		return eventType;
	}

	/**
	 * Set the value related to the column: EVENT_TYPE
	 * @param eventType the EVENT_TYPE value
	 */
	public void setEventType (org.unitime.timetable.model.EventType eventType) {
		this.eventType = eventType;
	}



	/**
	 * Return the value associated with the column: main_contact_id
	 */
	public org.unitime.timetable.model.EventContact getMainContact () {
		return mainContact;
	}

	/**
	 * Set the value related to the column: main_contact_id
	 * @param mainContact the main_contact_id value
	 */
	public void setMainContact (org.unitime.timetable.model.EventContact mainContact) {
		this.mainContact = mainContact;
	}



	/**
	 * Return the value associated with the column: additionalContacts
	 */
	public java.util.Set getAdditionalContacts () {
		return additionalContacts;
	}

	/**
	 * Set the value related to the column: additionalContacts
	 * @param additionalContacts the additionalContacts value
	 */
	public void setAdditionalContacts (java.util.Set additionalContacts) {
		this.additionalContacts = additionalContacts;
	}



	/**
	 * Return the value associated with the column: notes
	 */
	public java.util.Set getNotes () {
		return notes;
	}

	/**
	 * Set the value related to the column: notes
	 * @param notes the notes value
	 */
	public void setNotes (java.util.Set notes) {
		this.notes = notes;
	}

	public void addTonotes (org.unitime.timetable.model.EventNote eventNote) {
		if (null == getNotes()) setNotes(new java.util.HashSet());
		getNotes().add(eventNote);
	}



	/**
	 * Return the value associated with the column: relatedCourses
	 */
	public java.util.Set getRelatedCourses () {
		return relatedCourses;
	}

	/**
	 * Set the value related to the column: relatedCourses
	 * @param relatedCourses the relatedCourses value
	 */
	public void setRelatedCourses (java.util.Set relatedCourses) {
		this.relatedCourses = relatedCourses;
	}

	public void addTorelatedCourses (org.unitime.timetable.model.RelatedCourseInfo relatedCourseInfo) {
		if (null == getRelatedCourses()) setRelatedCourses(new java.util.HashSet());
		getRelatedCourses().add(relatedCourseInfo);
	}



	/**
	 * Return the value associated with the column: meetings
	 */
	public java.util.Set getMeetings () {
		return meetings;
	}

	/**
	 * Set the value related to the column: meetings
	 * @param meetings the meetings value
	 */
	public void setMeetings (java.util.Set meetings) {
		this.meetings = meetings;
	}

	public void addTomeetings (org.unitime.timetable.model.Meeting meeting) {
		if (null == getMeetings()) setMeetings(new java.util.HashSet());
		getMeetings().add(meeting);
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.Event)) return false;
		else {
			org.unitime.timetable.model.Event event = (org.unitime.timetable.model.Event) obj;
			if (null == this.getUniqueId() || null == event.getUniqueId()) return false;
			else return (this.getUniqueId().equals(event.getUniqueId()));
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