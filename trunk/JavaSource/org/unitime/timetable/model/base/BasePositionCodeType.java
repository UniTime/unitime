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
 * This is an object that contains data related to the POSITION_CODE_TO_TYPE table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="POSITION_CODE_TO_TYPE"
 */

public abstract class BasePositionCodeType  implements Serializable {

	public static String REF = "PositionCodeType";


	// constructors
	public BasePositionCodeType () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BasePositionCodeType (java.lang.String positionCode) {
		this.setPositionCode(positionCode);
		initialize();
	}

	protected void initialize () {}



	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private java.lang.String positionCode;

	// many to one
	private org.unitime.timetable.model.PositionType positionType;

	// collections
	private java.util.Set staff;



	/**
	 * Return the unique identifier of this class
     * @hibernate.id
     *  column="POSITION_CODE"
     */
	public java.lang.String getPositionCode () {
		return positionCode;
	}

	/**
	 * Set the unique identifier of this class
	 * @param positionCode the new ID
	 */
	public void setPositionCode (java.lang.String positionCode) {
		this.positionCode = positionCode;
		this.hashCode = Integer.MIN_VALUE;
	}




	/**
	 * Return the value associated with the column: POS_CODE_TYPE
	 */
	public org.unitime.timetable.model.PositionType getPositionType () {
		return positionType;
	}

	/**
	 * Set the value related to the column: POS_CODE_TYPE
	 * @param positionType the POS_CODE_TYPE value
	 */
	public void setPositionType (org.unitime.timetable.model.PositionType positionType) {
		this.positionType = positionType;
	}



	/**
	 * Return the value associated with the column: staff
	 */
	public java.util.Set getStaff () {
		return staff;
	}

	/**
	 * Set the value related to the column: staff
	 * @param staff the staff value
	 */
	public void setStaff (java.util.Set staff) {
		this.staff = staff;
	}

	public void addTostaff (org.unitime.timetable.model.Staff staff) {
		if (null == getStaff()) setStaff(new java.util.HashSet());
		getStaff().add(staff);
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.PositionCodeType)) return false;
		else {
			org.unitime.timetable.model.PositionCodeType positionCodeType = (org.unitime.timetable.model.PositionCodeType) obj;
			if (null == this.getPositionCode() || null == positionCodeType.getPositionCode()) return false;
			else return (this.getPositionCode().equals(positionCodeType.getPositionCode()));
		}
	}

	public int hashCode () {
		if (Integer.MIN_VALUE == this.hashCode) {
			if (null == this.getPositionCode()) return super.hashCode();
			else {
				String hashStr = this.getClass().getName() + ":" + this.getPositionCode().hashCode();
				this.hashCode = hashStr.hashCode();
			}
		}
		return this.hashCode;
	}


	public String toString () {
		return super.toString();
	}


}