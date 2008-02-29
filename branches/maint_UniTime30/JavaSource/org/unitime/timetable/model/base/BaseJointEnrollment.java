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
 * This is an object that contains data related to the JENRL table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="JENRL"
 */

public abstract class BaseJointEnrollment  implements Serializable {

	public static String REF = "JointEnrollment";
	public static String PROP_JENRL = "jenrl";


	// constructors
	public BaseJointEnrollment () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseJointEnrollment (java.lang.Long uniqueId) {
		this.setUniqueId(uniqueId);
		initialize();
	}

	/**
	 * Constructor for required fields
	 */
	public BaseJointEnrollment (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.Solution solution,
		org.unitime.timetable.model.Class_ class1,
		org.unitime.timetable.model.Class_ class2,
		java.lang.Double jenrl) {

		this.setUniqueId(uniqueId);
		this.setSolution(solution);
		this.setClass1(class1);
		this.setClass2(class2);
		this.setJenrl(jenrl);
		initialize();
	}

	protected void initialize () {}



	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private java.lang.Long uniqueId;

	// fields
	private java.lang.Double jenrl;

	// many to one
	private org.unitime.timetable.model.Solution solution;
	private org.unitime.timetable.model.Class_ class1;
	private org.unitime.timetable.model.Class_ class2;



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
	 * Return the value associated with the column: JENRL
	 */
	public java.lang.Double getJenrl () {
		return jenrl;
	}

	/**
	 * Set the value related to the column: JENRL
	 * @param jenrl the JENRL value
	 */
	public void setJenrl (java.lang.Double jenrl) {
		this.jenrl = jenrl;
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
	 * Return the value associated with the column: CLASS1_ID
	 */
	public org.unitime.timetable.model.Class_ getClass1 () {
		return class1;
	}

	/**
	 * Set the value related to the column: CLASS1_ID
	 * @param class1 the CLASS1_ID value
	 */
	public void setClass1 (org.unitime.timetable.model.Class_ class1) {
		this.class1 = class1;
	}



	/**
	 * Return the value associated with the column: CLASS2_ID
	 */
	public org.unitime.timetable.model.Class_ getClass2 () {
		return class2;
	}

	/**
	 * Set the value related to the column: CLASS2_ID
	 * @param class2 the CLASS2_ID value
	 */
	public void setClass2 (org.unitime.timetable.model.Class_ class2) {
		this.class2 = class2;
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.JointEnrollment)) return false;
		else {
			org.unitime.timetable.model.JointEnrollment jointEnrollment = (org.unitime.timetable.model.JointEnrollment) obj;
			if (null == this.getUniqueId() || null == jointEnrollment.getUniqueId()) return false;
			else return (this.getUniqueId().equals(jointEnrollment.getUniqueId()));
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