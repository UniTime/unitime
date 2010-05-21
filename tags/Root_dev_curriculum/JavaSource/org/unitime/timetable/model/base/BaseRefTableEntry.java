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
 * This is an object that contains data related to the  table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table=""
 */

public abstract class BaseRefTableEntry  implements Serializable {

	public static String REF = "RefTableEntry";
	public static String PROP_REFERENCE = "reference";
	public static String PROP_LABEL = "label";


	// constructors
	public BaseRefTableEntry () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseRefTableEntry (Long uniqueId) {
		this.setUniqueId(uniqueId);
		initialize();
	}

	/**
	 * Constructor for required fields
	 */
	public BaseRefTableEntry (
		Long uniqueId,
		java.lang.String reference) {

		this.setUniqueId(uniqueId);
		this.setReference(reference);
		initialize();
	}

	protected void initialize () {}



	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private Long uniqueId;

	// fields
	private java.lang.String reference;
	private java.lang.String label;



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
	 * Return the value associated with the column: LABEL
	 */
	public java.lang.String getLabel () {
		return label;
	}

	/**
	 * Set the value related to the column: LABEL
	 * @param label the LABEL value
	 */
	public void setLabel (java.lang.String label) {
		this.label = label;
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.RefTableEntry)) return false;
		else {
			org.unitime.timetable.model.RefTableEntry refTableEntry = (org.unitime.timetable.model.RefTableEntry) obj;
			if (null == this.getUniqueId() || null == refTableEntry.getUniqueId()) return false;
			else return (this.getUniqueId().equals(refTableEntry.getUniqueId()));
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
