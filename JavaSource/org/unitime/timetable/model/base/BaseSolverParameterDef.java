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
 * This is an object that contains data related to the SOLVER_PARAMETER_DEF table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="SOLVER_PARAMETER_DEF"
 */

public abstract class BaseSolverParameterDef  implements Serializable {

	public static String REF = "SolverParameterDef";
	public static String PROP_NAME = "name";
	public static String PROP_DEFAULT = "default";
	public static String PROP_DESCRIPTION = "description";
	public static String PROP_TYPE = "type";
	public static String PROP_ORDER = "order";
	public static String PROP_VISIBLE = "visible";


	// constructors
	public BaseSolverParameterDef () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseSolverParameterDef (java.lang.Long uniqueId) {
		this.setUniqueId(uniqueId);
		initialize();
	}

	/**
	 * Constructor for required fields
	 */
	public BaseSolverParameterDef (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.SolverParameterGroup group) {

		this.setUniqueId(uniqueId);
		this.setGroup(group);
		initialize();
	}

	protected void initialize () {}



	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private java.lang.Long uniqueId;

	// fields
	private java.lang.String name;
	private java.lang.String m_default;
	private java.lang.String description;
	private java.lang.String type;
	private java.lang.Integer order;
	private java.lang.Boolean visible;

	// many to one
	private org.unitime.timetable.model.SolverParameterGroup group;



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
	 * Return the value associated with the column: DEFAULT_VALUE
	 */
	public java.lang.String getDefault () {
		return m_default;
	}

	/**
	 * Set the value related to the column: DEFAULT_VALUE
	 * @param m_default the DEFAULT_VALUE value
	 */
	public void setDefault (java.lang.String m_default) {
		this.m_default = m_default;
	}



	/**
	 * Return the value associated with the column: DESCRIPTION
	 */
	public java.lang.String getDescription () {
		return description;
	}

	/**
	 * Set the value related to the column: DESCRIPTION
	 * @param description the DESCRIPTION value
	 */
	public void setDescription (java.lang.String description) {
		this.description = description;
	}



	/**
	 * Return the value associated with the column: TYPE
	 */
	public java.lang.String getType () {
		return type;
	}

	/**
	 * Set the value related to the column: TYPE
	 * @param type the TYPE value
	 */
	public void setType (java.lang.String type) {
		this.type = type;
	}



	/**
	 * Return the value associated with the column: ORD
	 */
	public java.lang.Integer getOrder () {
		return order;
	}

	/**
	 * Set the value related to the column: ORD
	 * @param order the ORD value
	 */
	public void setOrder (java.lang.Integer order) {
		this.order = order;
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
	 * Return the value associated with the column: SOLVER_PARAM_GROUP_ID
	 */
	public org.unitime.timetable.model.SolverParameterGroup getGroup () {
		return group;
	}

	/**
	 * Set the value related to the column: SOLVER_PARAM_GROUP_ID
	 * @param group the SOLVER_PARAM_GROUP_ID value
	 */
	public void setGroup (org.unitime.timetable.model.SolverParameterGroup group) {
		this.group = group;
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.SolverParameterDef)) return false;
		else {
			org.unitime.timetable.model.SolverParameterDef solverParameterDef = (org.unitime.timetable.model.SolverParameterDef) obj;
			if (null == this.getUniqueId() || null == solverParameterDef.getUniqueId()) return false;
			else return (this.getUniqueId().equals(solverParameterDef.getUniqueId()));
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