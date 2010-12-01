/* 
 * UniTime 3.1 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2008, UniTime LLC
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
 * This is an object that contains data related to the MEETING table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="MEETING"
 */

public abstract class BaseMeeting  implements Serializable {

	public static String REF = "Meeting";
	public static String PROP_MEETING_DATE = "meetingDate";
	public static String PROP_START_PERIOD = "startPeriod";
	public static String PROP_START_OFFSET = "startOffset";
	public static String PROP_STOP_PERIOD = "stopPeriod";
	public static String PROP_STOP_OFFSET = "stopOffset";
	public static String PROP_LOCATION_PERMANENT_ID = "locationPermanentId";
	public static String PROP_CLASS_CAN_OVERRIDE = "classCanOverride";
	public static String PROP_APPROVED_DATE = "approvedDate";


	// constructors
	public BaseMeeting () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseMeeting (java.lang.Long uniqueId) {
		this.setUniqueId(uniqueId);
		initialize();
	}

	/**
	 * Constructor for required fields
	 */
	public BaseMeeting (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.Event event,
		java.util.Date meetingDate,
		java.lang.Integer startPeriod,
		java.lang.Integer stopPeriod,
		java.lang.Boolean classCanOverride) {

		this.setUniqueId(uniqueId);
		this.setEvent(event);
		this.setMeetingDate(meetingDate);
		this.setStartPeriod(startPeriod);
		this.setStopPeriod(stopPeriod);
		this.setClassCanOverride(classCanOverride);
		initialize();
	}

	protected void initialize () {}



	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private java.lang.Long uniqueId;

	// fields
	private java.util.Date meetingDate;
	private java.lang.Integer startPeriod;
	private java.lang.Integer startOffset;
	private java.lang.Integer stopPeriod;
	private java.lang.Integer stopOffset;
	private java.lang.Long locationPermanentId;
	private java.lang.Boolean classCanOverride;
	private java.util.Date approvedDate;

	// many to one
	private org.unitime.timetable.model.Event event;



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
	 * Return the value associated with the column: MEETING_DATE
	 */
	public java.util.Date getMeetingDate () {
		return meetingDate;
	}

	/**
	 * Set the value related to the column: MEETING_DATE
	 * @param meetingDate the MEETING_DATE value
	 */
	public void setMeetingDate (java.util.Date meetingDate) {
		this.meetingDate = meetingDate;
	}



	/**
	 * Return the value associated with the column: START_PERIOD
	 */
	public java.lang.Integer getStartPeriod () {
		return startPeriod;
	}

	/**
	 * Set the value related to the column: START_PERIOD
	 * @param startPeriod the START_PERIOD value
	 */
	public void setStartPeriod (java.lang.Integer startPeriod) {
		this.startPeriod = startPeriod;
	}



	/**
	 * Return the value associated with the column: START_OFFSET
	 */
	public java.lang.Integer getStartOffset () {
		return startOffset;
	}

	/**
	 * Set the value related to the column: START_OFFSET
	 * @param startOffset the START_OFFSET value
	 */
	public void setStartOffset (java.lang.Integer startOffset) {
		this.startOffset = startOffset;
	}



	/**
	 * Return the value associated with the column: STOP_PERIOD
	 */
	public java.lang.Integer getStopPeriod () {
		return stopPeriod;
	}

	/**
	 * Set the value related to the column: STOP_PERIOD
	 * @param stopPeriod the STOP_PERIOD value
	 */
	public void setStopPeriod (java.lang.Integer stopPeriod) {
		this.stopPeriod = stopPeriod;
	}



	/**
	 * Return the value associated with the column: STOP_OFFSET
	 */
	public java.lang.Integer getStopOffset () {
		return stopOffset;
	}

	/**
	 * Set the value related to the column: STOP_OFFSET
	 * @param stopOffset the STOP_OFFSET value
	 */
	public void setStopOffset (java.lang.Integer stopOffset) {
		this.stopOffset = stopOffset;
	}



	/**
	 * Return the value associated with the column: location_perm_id
	 */
	public java.lang.Long getLocationPermanentId () {
		return locationPermanentId;
	}

	/**
	 * Set the value related to the column: location_perm_id
	 * @param locationPermanentId the location_perm_id value
	 */
	public void setLocationPermanentId (java.lang.Long locationPermanentId) {
		this.locationPermanentId = locationPermanentId;
	}



	/**
	 * Return the value associated with the column: CLASS_CAN_OVERRIDE
	 */
	public java.lang.Boolean isClassCanOverride () {
		return classCanOverride;
	}

	/**
	 * Set the value related to the column: CLASS_CAN_OVERRIDE
	 * @param classCanOverride the CLASS_CAN_OVERRIDE value
	 */
	public void setClassCanOverride (java.lang.Boolean classCanOverride) {
		this.classCanOverride = classCanOverride;
	}



	/**
	 * Return the value associated with the column: APPROVED_DATE
	 */
	public java.util.Date getApprovedDate () {
		return approvedDate;
	}

	/**
	 * Set the value related to the column: APPROVED_DATE
	 * @param approvedDate the APPROVED_DATE value
	 */
	public void setApprovedDate (java.util.Date approvedDate) {
		this.approvedDate = approvedDate;
	}



	/**
	 * Return the value associated with the column: EVENT_ID
	 */
	public org.unitime.timetable.model.Event getEvent () {
		return event;
	}

	/**
	 * Set the value related to the column: EVENT_ID
	 * @param event the EVENT_ID value
	 */
	public void setEvent (org.unitime.timetable.model.Event event) {
		this.event = event;
	}




	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.Meeting)) return false;
		else {
			org.unitime.timetable.model.Meeting meeting = (org.unitime.timetable.model.Meeting) obj;
			if (null == this.getUniqueId() || null == meeting.getUniqueId()) return false;
			else return (this.getUniqueId().equals(meeting.getUniqueId()));
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
