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
 * This is an object that contains data related to the PREFERENCE_LEVEL table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="PREFERENCE_LEVEL"
 */

public abstract class BasePreferenceLevel  implements Serializable {

	public static String REF = "PreferenceLevel";
	public static String PROP_PREF_ID = "prefId";
	public static String PROP_PREF_PROLOG = "prefProlog";
	public static String PROP_PREF_NAME = "prefName";


	// constructors
	public BasePreferenceLevel () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BasePreferenceLevel (java.lang.Long uniqueId) {
		this.setUniqueId(uniqueId);
		initialize();
	}

	/**
	 * Constructor for required fields
	 */
	public BasePreferenceLevel (
		java.lang.Long uniqueId,
		java.lang.Integer prefId) {

		this.setUniqueId(uniqueId);
		this.setPrefId(prefId);
		initialize();
	}

	protected void initialize () {}



	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private java.lang.Long uniqueId;

	// fields
	private java.lang.Integer prefId;
	private java.lang.String prefProlog;
	private java.lang.String prefName;



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
	 * Return the value associated with the column: PREF_ID
	 */
	public java.lang.Integer getPrefId () {
		return prefId;
	}

	/**
	 * Set the value related to the column: PREF_ID
	 * @param prefId the PREF_ID value
	 */
	public void setPrefId (java.lang.Integer prefId) {
		this.prefId = prefId;
	}



	/**
	 * Return the value associated with the column: PREF_PROLOG
	 */
	public java.lang.String getPrefProlog () {
		return prefProlog;
	}

	/**
	 * Set the value related to the column: PREF_PROLOG
	 * @param prefProlog the PREF_PROLOG value
	 */
	public void setPrefProlog (java.lang.String prefProlog) {
		this.prefProlog = prefProlog;
	}



	/**
	 * Return the value associated with the column: PREF_NAME
	 */
	public java.lang.String getPrefName () {
		return prefName;
	}

	/**
	 * Set the value related to the column: PREF_NAME
	 * @param prefName the PREF_NAME value
	 */
	public void setPrefName (java.lang.String prefName) {
		this.prefName = prefName;
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.PreferenceLevel)) return false;
		else {
			org.unitime.timetable.model.PreferenceLevel preferenceLevel = (org.unitime.timetable.model.PreferenceLevel) obj;
			if (null == this.getUniqueId() || null == preferenceLevel.getUniqueId()) return false;
			else return (this.getUniqueId().equals(preferenceLevel.getUniqueId()));
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
