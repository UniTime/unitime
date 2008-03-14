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
 * This is an object that contains data related to the EVENT_NOTE table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="EVENT_NOTE"
 */

public abstract class BaseEventNote  implements Serializable {

	public static String REF = "EventNote";
	public static String PROP_TEXT_NOTE = "textNote";


	// constructors
	public BaseEventNote () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseEventNote (java.lang.Long uniqueId) {
		this.setUniqueId(uniqueId);
		initialize();
	}

	/**
	 * Constructor for required fields
	 */
	public BaseEventNote (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.Event event) {

		this.setUniqueId(uniqueId);
		this.setEvent(event);
		initialize();
	}

	protected void initialize () {}



	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private java.lang.Long uniqueId;

	// fields
	private java.lang.String textNote;

	// many to one
	private org.unitime.timetable.model.Event event;
	private org.unitime.timetable.model.StandardEventNote standardNote;



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
	 * Return the value associated with the column: TEXT_NOTE
	 */
	public java.lang.String getTextNote () {
		return textNote;
	}

	/**
	 * Set the value related to the column: TEXT_NOTE
	 * @param textNote the TEXT_NOTE value
	 */
	public void setTextNote (java.lang.String textNote) {
		this.textNote = textNote;
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



	/**
	 * Return the value associated with the column: NOTE_ID
	 */
	public org.unitime.timetable.model.StandardEventNote getStandardNote () {
		return standardNote;
	}

	/**
	 * Set the value related to the column: NOTE_ID
	 * @param standardNote the NOTE_ID value
	 */
	public void setStandardNote (org.unitime.timetable.model.StandardEventNote standardNote) {
		this.standardNote = standardNote;
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.EventNote)) return false;
		else {
			org.unitime.timetable.model.EventNote eventNote = (org.unitime.timetable.model.EventNote) obj;
			if (null == this.getUniqueId() || null == eventNote.getUniqueId()) return false;
			else return (this.getUniqueId().equals(eventNote.getUniqueId()));
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