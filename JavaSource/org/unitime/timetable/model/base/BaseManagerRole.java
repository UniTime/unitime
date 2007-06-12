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
 * This is an object that contains data related to the TMTBL_MGR_TO_ROLES table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="TMTBL_MGR_TO_ROLES"
 */

public abstract class BaseManagerRole  implements Serializable {

	public static String REF = "ManagerRole";
	public static String PROP_PRIMARY = "primary";


	// constructors
	public BaseManagerRole () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseManagerRole (java.lang.Long uniqueId) {
		this.setUniqueId(uniqueId);
		initialize();
	}

	/**
	 * Constructor for required fields
	 */
	public BaseManagerRole (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.Roles role,
		org.unitime.timetable.model.TimetableManager timetableManager) {

		this.setUniqueId(uniqueId);
		this.setRole(role);
		this.setTimetableManager(timetableManager);
		initialize();
	}

	protected void initialize () {}



	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private java.lang.Long uniqueId;

	// fields
	private java.lang.Boolean primary;

	// many to one
	private org.unitime.timetable.model.Roles role;
	private org.unitime.timetable.model.TimetableManager timetableManager;



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
	 * Return the value associated with the column: PRIMARY
	 */
	public java.lang.Boolean isPrimary () {
		return primary;
	}

	/**
	 * Set the value related to the column: PRIMARY
	 * @param primary the PRIMARY value
	 */
	public void setPrimary (java.lang.Boolean primary) {
		this.primary = primary;
	}



	/**
	 * Return the value associated with the column: ROLE_ID
	 */
	public org.unitime.timetable.model.Roles getRole () {
		return role;
	}

	/**
	 * Set the value related to the column: ROLE_ID
	 * @param role the ROLE_ID value
	 */
	public void setRole (org.unitime.timetable.model.Roles role) {
		this.role = role;
	}



	/**
	 * Return the value associated with the column: MANAGER_ID
	 */
	public org.unitime.timetable.model.TimetableManager getTimetableManager () {
		return timetableManager;
	}

	/**
	 * Set the value related to the column: MANAGER_ID
	 * @param timetableManager the MANAGER_ID value
	 */
	public void setTimetableManager (org.unitime.timetable.model.TimetableManager timetableManager) {
		this.timetableManager = timetableManager;
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.ManagerRole)) return false;
		else {
			org.unitime.timetable.model.ManagerRole managerRole = (org.unitime.timetable.model.ManagerRole) obj;
			if (null == this.getUniqueId() || null == managerRole.getUniqueId()) return false;
			else return (this.getUniqueId().equals(managerRole.getUniqueId()));
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