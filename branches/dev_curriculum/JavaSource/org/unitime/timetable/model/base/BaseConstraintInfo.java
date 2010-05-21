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
import org.dom4j.Document;

/**
 * This is an object that contains data related to the SOLVER_INFO table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="SOLVER_INFO"
 */

public abstract class BaseConstraintInfo extends org.unitime.timetable.model.SolverInfo  implements Serializable {

	public static String REF = "ConstraintInfo";


	// constructors
	public BaseConstraintInfo () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseConstraintInfo (java.lang.Long uniqueId) {
		super(uniqueId);
	}

	/**
	 * Constructor for required fields
	 */
	public BaseConstraintInfo (
		java.lang.Long uniqueId,
		Document value) {

		super (
			uniqueId,
			value);
	}



	private int hashCode = Integer.MIN_VALUE;


	// collections
	private java.util.Set assignments;






	/**
	 * Return the value associated with the column: assignments
	 */
	public java.util.Set getAssignments () {
		return assignments;
	}

	/**
	 * Set the value related to the column: assignments
	 * @param assignments the assignments value
	 */
	public void setAssignments (java.util.Set assignments) {
		this.assignments = assignments;
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.ConstraintInfo)) return false;
		else {
			org.unitime.timetable.model.ConstraintInfo constraintInfo = (org.unitime.timetable.model.ConstraintInfo) obj;
			if (null == this.getUniqueId() || null == constraintInfo.getUniqueId()) return false;
			else return (this.getUniqueId().equals(constraintInfo.getUniqueId()));
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
