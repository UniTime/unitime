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
 * This is an object that contains data related to the ITYPE_DESC table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="ITYPE_DESC"
 */

public abstract class BaseItypeDesc  implements Serializable {

	public static String REF = "ItypeDesc";
	public static String PROP_ABBV = "abbv";
	public static String PROP_DESC = "desc";
	public static String PROP_SIS_REF = "sis_ref";
	public static String PROP_BASIC = "basic";
	public static String PROP_PARENT = "parent";
	public static String PROP_ORGANIZED = "organized";


	// constructors
	public BaseItypeDesc () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseItypeDesc (java.lang.Integer itype, java.lang.Boolean organized) {
		this.setItype(itype);
		this.setOrganized(organized);
		initialize();
	}

	protected void initialize () {}



	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private java.lang.Integer itype;

	// fields
	private java.lang.String abbv;
	private java.lang.String desc;
	private java.lang.String sis_ref;
	private java.lang.Integer basic;
	private java.lang.Boolean organized;
	private org.unitime.timetable.model.ItypeDesc parent;



	/**
	 * Return the unique identifier of this class
     * @hibernate.id
     *  column="ITYPE"
     */
	public java.lang.Integer getItype () {
		return itype;
	}

	/**
	 * Set the unique identifier of this class
	 * @param itype the new ID
	 */
	public void setItype (java.lang.Integer itype) {
		this.itype = itype;
		this.hashCode = Integer.MIN_VALUE;
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



	/**
	 * Return the value associated with the column: DESCRIPTION
	 */
	public java.lang.String getDesc () {
		return desc;
	}

	/**
	 * Set the value related to the column: DESCRIPTION
	 * @param desc the DESCRIPTION value
	 */
	public void setDesc (java.lang.String desc) {
		this.desc = desc;
	}



	/**
	 * Return the value associated with the column: SIS_REF
	 */
	public java.lang.String getSis_ref () {
		return sis_ref;
	}

	/**
	 * Set the value related to the column: SIS_REF
	 * @param sis_ref the SIS_REF value
	 */
	public void setSis_ref (java.lang.String sis_ref) {
		this.sis_ref = sis_ref;
	}



	/**
	 * Return the value associated with the column: BASIC
	 */
	public java.lang.Integer getBasic () {
		return basic;
	}

	/**
	 * Set the value related to the column: BASIC
	 * @param basic the BASIC value
	 */
	public void setBasic (java.lang.Integer basic) {
		this.basic = basic;
	}

	public org.unitime.timetable.model.ItypeDesc getParent() {
	    return parent;
	}
	
	public void setParent(org.unitime.timetable.model.ItypeDesc parent) {
	    this.parent = parent;
	}


	public java.lang.Boolean isOrganized() { return organized; }
	public void setOrganized(java.lang.Boolean organized) { this.organized = organized; }


	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.ItypeDesc)) return false;
		else {
			org.unitime.timetable.model.ItypeDesc itypeDesc = (org.unitime.timetable.model.ItypeDesc) obj;
			if (null == this.getItype() || null == itypeDesc.getItype()) return false;
			else return (this.getItype().equals(itypeDesc.getItype()));
		}
	}

	public int hashCode () {
		if (Integer.MIN_VALUE == this.hashCode) {
			if (null == this.getItype()) return super.hashCode();
			else {
				String hashStr = this.getClass().getName() + ":" + this.getItype().hashCode();
				this.hashCode = hashStr.hashCode();
			}
		}
		return this.hashCode;
	}


	public String toString () {
		return super.toString();
	}


}
