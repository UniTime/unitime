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
 * This is an object that contains data related to the EXACT_TIME_MINS table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="EXACT_TIME_MINS"
 */

public abstract class BaseExactTimeMins  implements Serializable {

	public static String REF = "ExactTimeMins";
	public static String PROP_MINS_PER_MTG_MIN = "minsPerMtgMin";
	public static String PROP_MINS_PER_MTG_MAX = "minsPerMtgMax";
	public static String PROP_NR_SLOTS = "nrSlots";
	public static String PROP_BREAK_TIME = "breakTime";


	// constructors
	public BaseExactTimeMins () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseExactTimeMins (java.lang.Long uniqueId) {
		this.setUniqueId(uniqueId);
		initialize();
	}

	/**
	 * Constructor for required fields
	 */
	public BaseExactTimeMins (
		java.lang.Long uniqueId,
		java.lang.Integer minsPerMtgMin,
		java.lang.Integer minsPerMtgMax,
		java.lang.Integer nrSlots,
		java.lang.Integer breakTime) {

		this.setUniqueId(uniqueId);
		this.setMinsPerMtgMin(minsPerMtgMin);
		this.setMinsPerMtgMax(minsPerMtgMax);
		this.setNrSlots(nrSlots);
		this.setBreakTime(breakTime);
		initialize();
	}

	protected void initialize () {}



	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private java.lang.Long uniqueId;

	// fields
	private java.lang.Integer minsPerMtgMin;
	private java.lang.Integer minsPerMtgMax;
	private java.lang.Integer nrSlots;
	private java.lang.Integer breakTime;



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
	 * Return the value associated with the column: MINS_MIN
	 */
	public java.lang.Integer getMinsPerMtgMin () {
		return minsPerMtgMin;
	}

	/**
	 * Set the value related to the column: MINS_MIN
	 * @param minsPerMtgMin the MINS_MIN value
	 */
	public void setMinsPerMtgMin (java.lang.Integer minsPerMtgMin) {
		this.minsPerMtgMin = minsPerMtgMin;
	}



	/**
	 * Return the value associated with the column: MINS_MAX
	 */
	public java.lang.Integer getMinsPerMtgMax () {
		return minsPerMtgMax;
	}

	/**
	 * Set the value related to the column: MINS_MAX
	 * @param minsPerMtgMax the MINS_MAX value
	 */
	public void setMinsPerMtgMax (java.lang.Integer minsPerMtgMax) {
		this.minsPerMtgMax = minsPerMtgMax;
	}



	/**
	 * Return the value associated with the column: NR_SLOTS
	 */
	public java.lang.Integer getNrSlots () {
		return nrSlots;
	}

	/**
	 * Set the value related to the column: NR_SLOTS
	 * @param nrSlots the NR_SLOTS value
	 */
	public void setNrSlots (java.lang.Integer nrSlots) {
		this.nrSlots = nrSlots;
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





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.ExactTimeMins)) return false;
		else {
			org.unitime.timetable.model.ExactTimeMins exactTimeMins = (org.unitime.timetable.model.ExactTimeMins) obj;
			if (null == this.getUniqueId() || null == exactTimeMins.getUniqueId()) return false;
			else return (this.getUniqueId().equals(exactTimeMins.getUniqueId()));
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
