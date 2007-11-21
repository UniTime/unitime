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
 * This is an object that contains data related to the SOLVER_PARAMETER_GROUP table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="SOLVER_PARAMETER_GROUP"
 */

public abstract class BaseSolverParameterGroup  implements Serializable {

	public static String REF = "SolverParameterGroup";
	public static String PROP_NAME = "name";
	public static String PROP_DESCRIPTION = "description";
	public static String PROP_ORDER = "order";


	// constructors
	public BaseSolverParameterGroup () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseSolverParameterGroup (java.lang.Long uniqueId) {
		this.setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize () {}



	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private java.lang.Long uniqueId;

	// fields
	private java.lang.String name;
	private java.lang.String description;
	private java.lang.Integer order;

	// collections
	private java.util.Set parameters;



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
	 * Return the value associated with the column: parameters
	 */
	public java.util.Set getParameters () {
		return parameters;
	}

	/**
	 * Set the value related to the column: parameters
	 * @param parameters the parameters value
	 */
	public void setParameters (java.util.Set parameters) {
		this.parameters = parameters;
	}

	public void addToparameters (org.unitime.timetable.model.SolverParameterDef solverParameterDef) {
		if (null == getParameters()) setParameters(new java.util.HashSet());
		getParameters().add(solverParameterDef);
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.SolverParameterGroup)) return false;
		else {
			org.unitime.timetable.model.SolverParameterGroup solverParameterGroup = (org.unitime.timetable.model.SolverParameterGroup) obj;
			if (null == this.getUniqueId() || null == solverParameterGroup.getUniqueId()) return false;
			else return (this.getUniqueId().equals(solverParameterGroup.getUniqueId()));
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