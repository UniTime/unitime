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
 * This is an object that contains data related to the CHANGE_LOG table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="CHANGE_LOG"
 */

public abstract class BaseChangeLog  implements Serializable {

	public static String REF = "ChangeLog";
	public static String PROP_TIME_STAMP = "timeStamp";
	public static String PROP_OBJECT_TYPE = "objectType";
	public static String PROP_OBJECT_TITLE = "objectTitle";
	public static String PROP_OBJECT_UNIQUE_ID = "objectUniqueId";
	public static String PROP_SOURCE_STRING = "sourceString";
	public static String PROP_OPERATION_STRING = "operationString";
	public static String PROP_DETAIL = "detail";


	// constructors
	public BaseChangeLog () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseChangeLog (java.lang.Long uniqueId) {
		this.setUniqueId(uniqueId);
		initialize();
	}

	/**
	 * Constructor for required fields
	 */
	public BaseChangeLog (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.Session session,
		org.unitime.timetable.model.TimetableManager manager,
		java.util.Date timeStamp,
		java.lang.String objectType,
		java.lang.String objectTitle,
		java.lang.Long objectUniqueId,
		java.lang.String sourceString,
		java.lang.String operationString) {

		this.setUniqueId(uniqueId);
		this.setSession(session);
		this.setManager(manager);
		this.setTimeStamp(timeStamp);
		this.setObjectType(objectType);
		this.setObjectTitle(objectTitle);
		this.setObjectUniqueId(objectUniqueId);
		this.setSourceString(sourceString);
		this.setOperationString(operationString);
		initialize();
	}

	protected void initialize () {}



	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private java.lang.Long uniqueId;

	// fields
	private java.util.Date timeStamp;
	private java.lang.String objectType;
	private java.lang.String objectTitle;
	private java.lang.Long objectUniqueId;
	private java.lang.String sourceString;
	private java.lang.String operationString;
	private org.unitime.commons.hibernate.blob.XmlBlobType detail;

	// many to one
	private org.unitime.timetable.model.Session session;
	private org.unitime.timetable.model.TimetableManager manager;
	private org.unitime.timetable.model.SubjectArea subjectArea;
	private org.unitime.timetable.model.Department department;



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
	 * Return the value associated with the column: TIME_STAMP
	 */
	public java.util.Date getTimeStamp () {
		return timeStamp;
	}

	/**
	 * Set the value related to the column: TIME_STAMP
	 * @param timeStamp the TIME_STAMP value
	 */
	public void setTimeStamp (java.util.Date timeStamp) {
		this.timeStamp = timeStamp;
	}



	/**
	 * Return the value associated with the column: OBJ_TYPE
	 */
	public java.lang.String getObjectType () {
		return objectType;
	}

	/**
	 * Set the value related to the column: OBJ_TYPE
	 * @param objectType the OBJ_TYPE value
	 */
	public void setObjectType (java.lang.String objectType) {
		this.objectType = objectType;
	}



	/**
	 * Return the value associated with the column: OBJ_TITLE
	 */
	public java.lang.String getObjectTitle () {
		return objectTitle;
	}

	/**
	 * Set the value related to the column: OBJ_TITLE
	 * @param objectTitle the OBJ_TITLE value
	 */
	public void setObjectTitle (java.lang.String objectTitle) {
		this.objectTitle = objectTitle;
	}



	/**
	 * Return the value associated with the column: OBJ_UID
	 */
	public java.lang.Long getObjectUniqueId () {
		return objectUniqueId;
	}

	/**
	 * Set the value related to the column: OBJ_UID
	 * @param objectUniqueId the OBJ_UID value
	 */
	public void setObjectUniqueId (java.lang.Long objectUniqueId) {
		this.objectUniqueId = objectUniqueId;
	}



	/**
	 * Return the value associated with the column: SOURCE
	 */
	public java.lang.String getSourceString () {
		return sourceString;
	}

	/**
	 * Set the value related to the column: SOURCE
	 * @param sourceString the SOURCE value
	 */
	public void setSourceString (java.lang.String sourceString) {
		this.sourceString = sourceString;
	}



	/**
	 * Return the value associated with the column: OPERATION
	 */
	public java.lang.String getOperationString () {
		return operationString;
	}

	/**
	 * Set the value related to the column: OPERATION
	 * @param operationString the OPERATION value
	 */
	public void setOperationString (java.lang.String operationString) {
		this.operationString = operationString;
	}



	/**
	 * Return the value associated with the column: DETAIL
	 */
	public org.unitime.commons.hibernate.blob.XmlBlobType getDetail () {
		return detail;
	}

	/**
	 * Set the value related to the column: DETAIL
	 * @param detail the DETAIL value
	 */
	public void setDetail (org.unitime.commons.hibernate.blob.XmlBlobType detail) {
		this.detail = detail;
	}



	/**
	 * Return the value associated with the column: SESSION_ID
	 */
	public org.unitime.timetable.model.Session getSession () {
		return session;
	}

	/**
	 * Set the value related to the column: SESSION_ID
	 * @param session the SESSION_ID value
	 */
	public void setSession (org.unitime.timetable.model.Session session) {
		this.session = session;
	}



	/**
	 * Return the value associated with the column: MANAGER_ID
	 */
	public org.unitime.timetable.model.TimetableManager getManager () {
		return manager;
	}

	/**
	 * Set the value related to the column: MANAGER_ID
	 * @param manager the MANAGER_ID value
	 */
	public void setManager (org.unitime.timetable.model.TimetableManager manager) {
		this.manager = manager;
	}



	/**
	 * Return the value associated with the column: SUBJ_AREA_ID
	 */
	public org.unitime.timetable.model.SubjectArea getSubjectArea () {
		return subjectArea;
	}

	/**
	 * Set the value related to the column: SUBJ_AREA_ID
	 * @param subjectArea the SUBJ_AREA_ID value
	 */
	public void setSubjectArea (org.unitime.timetable.model.SubjectArea subjectArea) {
		this.subjectArea = subjectArea;
	}



	/**
	 * Return the value associated with the column: DEPARTMENT_ID
	 */
	public org.unitime.timetable.model.Department getDepartment () {
		return department;
	}

	/**
	 * Set the value related to the column: DEPARTMENT_ID
	 * @param department the DEPARTMENT_ID value
	 */
	public void setDepartment (org.unitime.timetable.model.Department department) {
		this.department = department;
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.ChangeLog)) return false;
		else {
			org.unitime.timetable.model.ChangeLog changeLog = (org.unitime.timetable.model.ChangeLog) obj;
			if (null == this.getUniqueId() || null == changeLog.getUniqueId()) return false;
			else return (this.getUniqueId().equals(changeLog.getUniqueId()));
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
