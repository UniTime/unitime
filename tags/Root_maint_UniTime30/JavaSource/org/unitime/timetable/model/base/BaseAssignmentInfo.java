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
import org.dom4j.Document;

/**
 * This is an object that contains data related to the SOLVER_INFO table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="SOLVER_INFO"
 */

public abstract class BaseAssignmentInfo extends org.unitime.timetable.model.SolverInfo  implements Serializable {

	public static String REF = "AssignmentInfo";


	// constructors
	public BaseAssignmentInfo () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseAssignmentInfo (java.lang.Long uniqueId) {
		super(uniqueId);
	}

	/**
	 * Constructor for required fields
	 */
	public BaseAssignmentInfo (
		java.lang.Long uniqueId,
		Document value) {

		super (
			uniqueId,
			value);
	}



	private int hashCode = Integer.MIN_VALUE;


	// many to one
	private org.unitime.timetable.model.Assignment assignment;






	/**
	 * Return the value associated with the column: ASSIGNMENT_ID
	 */
	public org.unitime.timetable.model.Assignment getAssignment () {
		return assignment;
	}

	/**
	 * Set the value related to the column: ASSIGNMENT_ID
	 * @param assignment the ASSIGNMENT_ID value
	 */
	public void setAssignment (org.unitime.timetable.model.Assignment assignment) {
		this.assignment = assignment;
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.AssignmentInfo)) return false;
		else {
			org.unitime.timetable.model.AssignmentInfo assignmentInfo = (org.unitime.timetable.model.AssignmentInfo) obj;
			if (null == this.getUniqueId() || null == assignmentInfo.getUniqueId()) return false;
			else return (this.getUniqueId().equals(assignmentInfo.getUniqueId()));
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