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
 * This is an object that contains data related to the STUDENT_ENRL table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="STUDENT_ENRL"
 */

public abstract class BaseStudentEnrollment  implements Serializable {

	public static String REF = "StudentEnrollment";
	public static String PROP_STUDENT_ID = "studentId";


	// constructors
	public BaseStudentEnrollment () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseStudentEnrollment (java.lang.Long uniqueId) {
		this.setUniqueId(uniqueId);
		initialize();
	}

	/**
	 * Constructor for required fields
	 */
	public BaseStudentEnrollment (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.Solution solution,
		org.unitime.timetable.model.Class_ clazz,
		java.lang.Long studentId) {

		this.setUniqueId(uniqueId);
		this.setSolution(solution);
		this.setClazz(clazz);
		this.setStudentId(studentId);
		initialize();
	}

	protected void initialize () {}



	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private java.lang.Long uniqueId;

	// fields
	private java.lang.Long studentId;

	// many to one
	private org.unitime.timetable.model.Solution solution;
	private org.unitime.timetable.model.Class_ clazz;



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
	 * Return the value associated with the column: STUDENT_ID
	 */
	public java.lang.Long getStudentId () {
		return studentId;
	}

	/**
	 * Set the value related to the column: STUDENT_ID
	 * @param studentId the STUDENT_ID value
	 */
	public void setStudentId (java.lang.Long studentId) {
		this.studentId = studentId;
	}



	/**
	 * Return the value associated with the column: SOLUTION_ID
	 */
	public org.unitime.timetable.model.Solution getSolution () {
		return solution;
	}

	/**
	 * Set the value related to the column: SOLUTION_ID
	 * @param solution the SOLUTION_ID value
	 */
	public void setSolution (org.unitime.timetable.model.Solution solution) {
		this.solution = solution;
	}



	/**
	 * Return the value associated with the column: CLASS_ID
	 */
	public org.unitime.timetable.model.Class_ getClazz () {
		return clazz;
	}

	/**
	 * Set the value related to the column: CLASS_ID
	 * @param clazz the CLASS_ID value
	 */
	public void setClazz (org.unitime.timetable.model.Class_ clazz) {
		this.clazz = clazz;
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.StudentEnrollment)) return false;
		else {
			org.unitime.timetable.model.StudentEnrollment studentEnrollment = (org.unitime.timetable.model.StudentEnrollment) obj;
			if (null == this.getUniqueId() || null == studentEnrollment.getUniqueId()) return false;
			else return (this.getUniqueId().equals(studentEnrollment.getUniqueId()));
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
