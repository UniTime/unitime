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
 * This is an object that contains data related to the SOLVER_GROUP table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="SOLVER_GROUP"
 */

public abstract class BaseSolverGroup  implements Serializable {

	public static String REF = "SolverGroup";
	public static String PROP_NAME = "name";
	public static String PROP_ABBV = "abbv";
	public static String PROP_SESSION = "session";

	// constructors
	public BaseSolverGroup () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseSolverGroup (Long uniqueId) {
		this.setUniqueId(uniqueId);
		initialize();
	}

	/**
	 * Constructor for required fields
	 */
	public BaseSolverGroup (
		Long uniqueId,
		java.lang.String name,
		java.lang.String abbv,
		org.unitime.timetable.model.Session session) {

		this.setUniqueId(uniqueId);
		this.setName(name);
		this.setAbbv(abbv);
		this.setSession(session);
		initialize();
	}

	protected void initialize () {}



	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private Long uniqueId;

	// fields
	private java.lang.String name;
	private java.lang.String abbv;
	private org.unitime.timetable.model.Session session;

	// collections
	private java.util.Set timetableManagers;
	private java.util.Set departments;
	private java.util.Set solutions;



	/**
	 * Return the unique identifier of this class
     * @hibernate.id
     *  generator-class="sequence"
     *  column="UNIQUEID"
     */
	public Long getUniqueId () {
		return uniqueId;
	}

	/**
	 * Set the unique identifier of this class
	 * @param uniqueId the new ID
	 */
	public void setUniqueId (Long uniqueId) {
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

	public java.lang.String getAbbv () {
		return abbv;
	}

	public void setAbbv (java.lang.String abbv) {
		this.abbv = abbv;
	}

	public org.unitime.timetable.model.Session getSession() {
		return session;
	}
	
	public void setSession(org.unitime.timetable.model.Session session) {
		this.session = session;
	}



	/**
	 * Return the value associated with the column: timetableManagers
	 */
	public java.util.Set getTimetableManagers () {
		return timetableManagers;
	}

	/**
	 * Set the value related to the column: timetableManagers
	 * @param timetableManagers the timetableManagers value
	 */
	public void setTimetableManagers (java.util.Set timetableManagers) {
		this.timetableManagers = timetableManagers;
	}



	/**
	 * Return the value associated with the column: departments
	 */
	public java.util.Set getDepartments () {
		return departments;
	}

	/**
	 * Set the value related to the column: departments
	 * @param departments the departments value
	 */
	public void setDepartments (java.util.Set departments) {
		this.departments = departments;
	}



	/**
	 * Return the value associated with the column: solutions
	 */
	public java.util.Set getSolutions () {
		return solutions;
	}

	/**
	 * Set the value related to the column: solutions
	 * @param solutions the solutions value
	 */
	public void setSolutions (java.util.Set solutions) {
		this.solutions = solutions;
	}

	public void addTosolutions (org.unitime.timetable.model.Solution solution) {
		if (null == getSolutions()) setSolutions(new java.util.HashSet());
		getSolutions().add(solution);
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.SolverGroup)) return false;
		else {
			org.unitime.timetable.model.SolverGroup solverGroup = (org.unitime.timetable.model.SolverGroup) obj;
			if (null == this.getUniqueId() || null == solverGroup.getUniqueId()) return false;
			else return (this.getUniqueId().equals(solverGroup.getUniqueId()));
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
