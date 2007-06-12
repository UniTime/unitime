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
 * This is an object that contains data related to the TIMETABLE_MANAGER table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="TIMETABLE_MANAGER"
 */

public abstract class BaseTimetableManager  implements Serializable {

	public static String REF = "TimetableManager";
	public static String PROP_EXTERNAL_UNIQUE_ID = "externalUniqueId";
	public static String PROP_FIRST_NAME = "firstName";
	public static String PROP_MIDDLE_NAME = "middleName";
	public static String PROP_LAST_NAME = "lastName";
	public static String PROP_EMAIL_ADDRESS = "emailAddress";


	// constructors
	public BaseTimetableManager () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseTimetableManager (java.lang.Long uniqueId) {
		this.setUniqueId(uniqueId);
		initialize();
	}

	/**
	 * Constructor for required fields
	 */
	public BaseTimetableManager (
		java.lang.Long uniqueId,
		java.lang.String firstName,
		java.lang.String lastName) {

		this.setUniqueId(uniqueId);
		this.setFirstName(firstName);
		this.setLastName(lastName);
		initialize();
	}

	protected void initialize () {}



	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private java.lang.Long uniqueId;

	// fields
	private java.lang.String externalUniqueId;
	private java.lang.String firstName;
	private java.lang.String middleName;
	private java.lang.String lastName;
	private java.lang.String emailAddress;

	// collections
	private java.util.Set settings;
	private java.util.Set departments;
	private java.util.Set managerRoles;
	private java.util.Set solverGroups;



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
	 * Return the value associated with the column: EXTERNAL_UID
	 */
	public java.lang.String getExternalUniqueId () {
		return externalUniqueId;
	}

	/**
	 * Set the value related to the column: EXTERNAL_UID
	 * @param externalUniqueId the EXTERNAL_UID value
	 */
	public void setExternalUniqueId (java.lang.String externalUniqueId) {
		this.externalUniqueId = externalUniqueId;
	}



	/**
	 * Return the value associated with the column: FIRST_NAME
	 */
	public java.lang.String getFirstName () {
		return firstName;
	}

	/**
	 * Set the value related to the column: FIRST_NAME
	 * @param firstName the FIRST_NAME value
	 */
	public void setFirstName (java.lang.String firstName) {
		this.firstName = firstName;
	}



	/**
	 * Return the value associated with the column: MIDDLE_NAME
	 */
	public java.lang.String getMiddleName () {
		return middleName;
	}

	/**
	 * Set the value related to the column: MIDDLE_NAME
	 * @param middleName the MIDDLE_NAME value
	 */
	public void setMiddleName (java.lang.String middleName) {
		this.middleName = middleName;
	}



	/**
	 * Return the value associated with the column: LAST_NAME
	 */
	public java.lang.String getLastName () {
		return lastName;
	}

	/**
	 * Set the value related to the column: LAST_NAME
	 * @param lastName the LAST_NAME value
	 */
	public void setLastName (java.lang.String lastName) {
		this.lastName = lastName;
	}



	/**
	 * Return the value associated with the column: EMAIL_ADDRESS
	 */
	public java.lang.String getEmailAddress () {
		return emailAddress;
	}

	/**
	 * Set the value related to the column: EMAIL_ADDRESS
	 * @param emailAddress the EMAIL_ADDRESS value
	 */
	public void setEmailAddress (java.lang.String emailAddress) {
		this.emailAddress = emailAddress;
	}



	/**
	 * Return the value associated with the column: settings
	 */
	public java.util.Set getSettings () {
		return settings;
	}

	/**
	 * Set the value related to the column: settings
	 * @param settings the settings value
	 */
	public void setSettings (java.util.Set settings) {
		this.settings = settings;
	}

	public void addTosettings (org.unitime.timetable.model.ManagerSettings managerSettings) {
		if (null == getSettings()) setSettings(new java.util.HashSet());
		getSettings().add(managerSettings);
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
	 * Return the value associated with the column: managerRoles
	 */
	public java.util.Set getManagerRoles () {
		return managerRoles;
	}

	/**
	 * Set the value related to the column: managerRoles
	 * @param managerRoles the managerRoles value
	 */
	public void setManagerRoles (java.util.Set managerRoles) {
		this.managerRoles = managerRoles;
	}

	public void addTomanagerRoles (org.unitime.timetable.model.ManagerRole managerRole) {
		if (null == getManagerRoles()) setManagerRoles(new java.util.HashSet());
		getManagerRoles().add(managerRole);
	}



	/**
	 * Return the value associated with the column: solverGroups
	 */
	public java.util.Set getSolverGroups () {
		return solverGroups;
	}

	/**
	 * Set the value related to the column: solverGroups
	 * @param solverGroups the solverGroups value
	 */
	public void setSolverGroups (java.util.Set solverGroups) {
		this.solverGroups = solverGroups;
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.TimetableManager)) return false;
		else {
			org.unitime.timetable.model.TimetableManager timetableManager = (org.unitime.timetable.model.TimetableManager) obj;
			if (null == this.getUniqueId() || null == timetableManager.getUniqueId()) return false;
			else return (this.getUniqueId().equals(timetableManager.getUniqueId()));
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