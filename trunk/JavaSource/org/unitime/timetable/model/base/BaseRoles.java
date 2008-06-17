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
 * This class represents the roles other than
 * Schedule Deputy. Examples of other roles are
 * LLR Manager, Lab Manager, Administrator.
 * @author Heston Fernandes
 */

public abstract class BaseRoles  implements Serializable {

	public static String REF = "Roles";
	public static String PROP_REFERENCE = "reference";
	public static String PROP_ABBV = "abbv";


	// constructors
	public BaseRoles () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseRoles (java.lang.Long roleId) {
		this.setRoleId(roleId);
		initialize();
	}

	/**
	 * Constructor for required fields
	 */
	public BaseRoles (
		java.lang.Long roleId,
		java.lang.String reference,
		java.lang.String abbv) {

		this.setRoleId(roleId);
		this.setReference(reference);
		this.setAbbv(abbv);
		initialize();
	}

	protected void initialize () {}



	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private java.lang.Long roleId;

	// fields
	private java.lang.String reference;
	private java.lang.String abbv;



	/**
	 * Return the unique identifier of this class
     * @hibernate.id
     *  generator-class="sequence"
     *  column="ROLE_ID"
     */
	public java.lang.Long getRoleId () {
		return roleId;
	}

	/**
	 * Set the unique identifier of this class
	 * @param roleId the new ID
	 */
	public void setRoleId (java.lang.Long roleId) {
		this.roleId = roleId;
		this.hashCode = Integer.MIN_VALUE;
	}




	/**
	 * Return the value associated with the column: REFERENCE
	 */
	public java.lang.String getReference () {
		return reference;
	}

	/**
	 * Set the value related to the column: REFERENCE
	 * @param reference the REFERENCE value
	 */
	public void setReference (java.lang.String reference) {
		this.reference = reference;
	}



	/**
	 * Return the value associated with the column: ABBV
	 */
	public java.lang.String getAbbv () {
		return abbv;
	}

	/**
	 * Set the value related to the column: ABBV
	 * @param abbv the ABBV value
	 */
	public void setAbbv (java.lang.String abbv) {
		this.abbv = abbv;
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.Roles)) return false;
		else {
			org.unitime.timetable.model.Roles roles = (org.unitime.timetable.model.Roles) obj;
			if (null == this.getRoleId() || null == roles.getRoleId()) return false;
			else return (this.getRoleId().equals(roles.getRoleId()));
		}
	}

	public int hashCode () {
		if (Integer.MIN_VALUE == this.hashCode) {
			if (null == this.getRoleId()) return super.hashCode();
			else {
				String hashStr = this.getClass().getName() + ":" + this.getRoleId().hashCode();
				this.hashCode = hashStr.hashCode();
			}
		}
		return this.hashCode;
	}


	public String toString () {
		return super.toString();
	}


}
