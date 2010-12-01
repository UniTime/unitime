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
 * This is an object that contains data related to the DEPT_STATUS_TYPE table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="DEPT_STATUS_TYPE"
 */

public abstract class BaseDepartmentStatusType extends org.unitime.timetable.model.RefTableEntry  implements Serializable {

	public static String REF = "DepartmentStatusType";
	public static String PROP_STATUS = "status";
	public static String PROP_APPLY = "apply";
	public static String PROP_ORD = "ord";


	// constructors
	public BaseDepartmentStatusType () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseDepartmentStatusType (Long uniqueId) {
		super(uniqueId);
	}

	/**
	 * Constructor for required fields
	 */
	public BaseDepartmentStatusType (
		Long uniqueId,
		java.lang.String reference) {

		super (
			uniqueId,
			reference);
	}



	private int hashCode = Integer.MIN_VALUE;


	// fields
	private java.lang.Integer status;
	private java.lang.Integer apply;
	private java.lang.Integer ord;






	/**
	 * Return the value associated with the column: STATUS
	 */
	public java.lang.Integer getStatus () {
		return status;
	}

	/**
	 * Set the value related to the column: STATUS
	 * @param status the STATUS value
	 */
	public void setStatus (java.lang.Integer status) {
		this.status = status;
	}



	/**
	 * Return the value associated with the column: APPLY
	 */
	public java.lang.Integer getApply () {
		return apply;
	}

	/**
	 * Set the value related to the column: APPLY
	 * @param apply the APPLY value
	 */
	public void setApply (java.lang.Integer apply) {
		this.apply = apply;
	}



	/**
	 * Return the value associated with the column: ORD
	 */
	public java.lang.Integer getOrd () {
		return ord;
	}

	/**
	 * Set the value related to the column: ORD
	 * @param ord the ORD value
	 */
	public void setOrd (java.lang.Integer ord) {
		this.ord = ord;
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.DepartmentStatusType)) return false;
		else {
			org.unitime.timetable.model.DepartmentStatusType departmentStatusType = (org.unitime.timetable.model.DepartmentStatusType) obj;
			if (null == this.getUniqueId() || null == departmentStatusType.getUniqueId()) return false;
			else return (this.getUniqueId().equals(departmentStatusType.getUniqueId()));
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
