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
 * This is an object that contains data related to the TIME_PATTERN table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="TIME_PATTERN"
 */

public abstract class BaseTimePattern  implements Serializable {

	public static String REF = "TimePattern";
	public static String PROP_NAME = "name";
	public static String PROP_MIN_PER_MTG = "minPerMtg";
	public static String PROP_SLOTS_PER_MTG = "slotsPerMtg";
	public static String PROP_NR_MEETINGS = "nrMeetings";
	public static String PROP_BREAK_TIME = "breakTime";
	public static String PROP_TYPE = "type";
	public static String PROP_VISIBLE = "visible";


	// constructors
	public BaseTimePattern () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseTimePattern (java.lang.Long uniqueId) {
		this.setUniqueId(uniqueId);
		initialize();
	}

	/**
	 * Constructor for required fields
	 */
	public BaseTimePattern (
		java.lang.Long uniqueId,
        org.unitime.timetable.model.Session session) {

		this.setUniqueId(uniqueId);
		this.setSession(session);
		initialize();
	}

	protected void initialize () {}



	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private java.lang.Long uniqueId;

	// fields
	private java.lang.String name;
	private java.lang.Integer minPerMtg;
	private java.lang.Integer slotsPerMtg;
	private java.lang.Integer nrMeetings;
	private java.lang.Integer breakTime;
	private java.lang.Integer type;
	private java.lang.Boolean visible;

    // many to one
    private org.unitime.timetable.model.Session session;

    // collections
	private java.util.Set times;
	private java.util.Set days;
	private java.util.Set departments;



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
	 * Return the value associated with the column: NAME
	 */
	public java.lang.String getName () {
		return name;
	}

	/**
	 * Set the value related to the column: NAME
	 * @param name the NAME value
	 */
	public void setName (java.lang.String name) {
		this.name = name;
	}



    /**
     * Return the value associated with the column: SESSION_ID
     */
    public org.unitime.timetable.model.Session getSession () {
        return session;
    }

    /**
     * Set the value related to the column: SESSION_ID
     * @param session the SESSION_ID value
     */
    public void setSession (org.unitime.timetable.model.Session session) {
        this.session = session;
    }

	/**
	 * Return the value associated with the column: MINS_PMT
	 */
	public java.lang.Integer getMinPerMtg () {
		return minPerMtg;
	}

	/**
	 * Set the value related to the column: MINS_PMT
	 * @param minPerMtg the MINS_PMT value
	 */
	public void setMinPerMtg (java.lang.Integer minPerMtg) {
		this.minPerMtg = minPerMtg;
	}



	/**
	 * Return the value associated with the column: SLOTS_PMT
	 */
	public java.lang.Integer getSlotsPerMtg () {
		return slotsPerMtg;
	}

	/**
	 * Set the value related to the column: SLOTS_PMT
	 * @param slotsPerMtg the SLOTS_PMT value
	 */
	public void setSlotsPerMtg (java.lang.Integer slotsPerMtg) {
		this.slotsPerMtg = slotsPerMtg;
	}



	/**
	 * Return the value associated with the column: NR_MTGS
	 */
	public java.lang.Integer getNrMeetings () {
		return nrMeetings;
	}

	/**
	 * Set the value related to the column: NR_MTGS
	 * @param nrMeetings the NR_MTGS value
	 */
	public void setNrMeetings (java.lang.Integer nrMeetings) {
		this.nrMeetings = nrMeetings;
	}



	/**
	 * Return the value associated with the column: BREAK_TIME
	 */
	public java.lang.Integer getBreakTime () {
		return breakTime;
	}

	/**
	 * Set the value related to the column: BREAK_TIME
	 * @param breakTime the BREAK_TIME value
	 */
	public void setBreakTime (java.lang.Integer breakTime) {
		this.breakTime = breakTime;
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
	 * Return the value associated with the column: VISIBLE
	 */
	public java.lang.Boolean isVisible () {
		return visible;
	}

	/**
	 * Set the value related to the column: VISIBLE
	 * @param visible the VISIBLE value
	 */
	public void setVisible (java.lang.Boolean visible) {
		this.visible = visible;
	}



	/**
	 * Return the value associated with the column: times
	 */
	public java.util.Set getTimes () {
		return times;
	}

	/**
	 * Set the value related to the column: times
	 * @param times the times value
	 */
	public void setTimes (java.util.Set times) {
		this.times = times;
	}

	public void addTotimes (org.unitime.timetable.model.TimePatternTime timePatternTime) {
		if (null == getTimes()) setTimes(new java.util.HashSet());
		getTimes().add(timePatternTime);
	}



	/**
	 * Return the value associated with the column: days
	 */
	public java.util.Set getDays () {
		return days;
	}

	/**
	 * Set the value related to the column: days
	 * @param days the days value
	 */
	public void setDays (java.util.Set days) {
		this.days = days;
	}

	public void addTodays (org.unitime.timetable.model.TimePatternDays timePatternDays) {
		if (null == getDays()) setDays(new java.util.HashSet());
		getDays().add(timePatternDays);
	}



	/**
	 * Return the value associated with the column: departments
	 */
	public java.util.Set getDepartments () {
		return departments;
	}

	/**
	 * Set the value related to the column: departments
	 * @param departments the departments value
	 */
	public void setDepartments (java.util.Set departments) {
		this.departments = departments;
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.TimePattern)) return false;
		else {
			org.unitime.timetable.model.TimePattern timePattern = (org.unitime.timetable.model.TimePattern) obj;
			if (null == this.getUniqueId() || null == timePattern.getUniqueId()) return false;
			else return (this.getUniqueId().equals(timePattern.getUniqueId()));
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
