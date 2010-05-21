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
 * This is an object that contains data related to the DEPARTMENT table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="DEPARTMENT"
 */

public abstract class BaseDepartment extends org.unitime.timetable.model.PreferenceGroup  implements Serializable {

	public static String REF = "Department";
	public static String PROP_EXTERNAL_UNIQUE_ID = "externalUniqueId";
	public static String PROP_DEPT_CODE = "deptCode";
	public static String PROP_ABBREVIATION = "abbreviation";
	public static String PROP_NAME = "name";
	public static String PROP_ALLOW_REQ_TIME = "allowReqTime";
	public static String PROP_ALLOW_REQ_ROOM = "allowReqRoom";
	public static String PROP_ROOM_SHARING_COLOR = "roomSharingColor";
	public static String PROP_EXTERNAL_MANAGER = "externalManager";
	public static String PROP_EXTERNAL_MGR_LABEL = "externalMgrLabel";
	public static String PROP_EXTERNAL_MGR_ABBV = "externalMgrAbbv";
	public static String PROP_DISTRIBUTION_PREF_PRIORITY = "distributionPrefPriority";


	// constructors
	public BaseDepartment () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseDepartment (java.lang.Long uniqueId) {
		super(uniqueId);
	}



	private int hashCode = Integer.MIN_VALUE;


	// fields
	private java.lang.String externalUniqueId;
	private java.lang.String deptCode;
	private java.lang.String abbreviation;
	private java.lang.String name;
	private java.lang.Boolean allowReqTime;
	private java.lang.Boolean allowReqRoom;
	private java.lang.String roomSharingColor;
	private java.lang.Boolean externalManager;
	private java.lang.String externalMgrLabel;
	private java.lang.String externalMgrAbbv;
	private java.lang.Integer distributionPrefPriority;

	// many to one
	private org.unitime.timetable.model.Session session;
	private org.unitime.timetable.model.DepartmentStatusType statusType;
	private org.unitime.timetable.model.SolverGroup solverGroup;

	// collections
	private java.util.Set subjectAreas;
	private java.util.Set roomDepts;
	private java.util.Set datePatterns;
	private java.util.Set timePatterns;
	private java.util.Set timetableManagers;
	private java.util.Set instructors;






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
	 * Return the value associated with the column: DEPT_CODE
	 */
	public java.lang.String getDeptCode () {
		return deptCode;
	}

	/**
	 * Set the value related to the column: DEPT_CODE
	 * @param deptCode the DEPT_CODE value
	 */
	public void setDeptCode (java.lang.String deptCode) {
		this.deptCode = deptCode;
	}



	/**
	 * Return the value associated with the column: ABBREVIATION
	 */
	public java.lang.String getAbbreviation () {
		return abbreviation;
	}

	/**
	 * Set the value related to the column: ABBREVIATION
	 * @param abbreviation the ABBREVIATION value
	 */
	public void setAbbreviation (java.lang.String abbreviation) {
		this.abbreviation = abbreviation;
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
	 * Return the value associated with the column: ALLOW_REQ_TIME
	 */
	public java.lang.Boolean isAllowReqTime () {
		return allowReqTime;
	}

	/**
	 * Set the value related to the column: ALLOW_REQ_TIME
	 * @param allowReqTime the ALLOW_REQ_TIME value
	 */
	public void setAllowReqTime (java.lang.Boolean allowReqTime) {
		this.allowReqTime = allowReqTime;
	}



	/**
	 * Return the value associated with the column: ALLOW_REQ_ROOM
	 */
	public java.lang.Boolean isAllowReqRoom () {
		return allowReqRoom;
	}

	/**
	 * Set the value related to the column: ALLOW_REQ_ROOM
	 * @param allowReqRoom the ALLOW_REQ_ROOM value
	 */
	public void setAllowReqRoom (java.lang.Boolean allowReqRoom) {
		this.allowReqRoom = allowReqRoom;
	}



	/**
	 * Return the value associated with the column: RS_COLOR
	 */
	public java.lang.String getRoomSharingColor () {
		return roomSharingColor;
	}

	/**
	 * Set the value related to the column: RS_COLOR
	 * @param roomSharingColor the RS_COLOR value
	 */
	public void setRoomSharingColor (java.lang.String roomSharingColor) {
		this.roomSharingColor = roomSharingColor;
	}



	/**
	 * Return the value associated with the column: EXTERNAL_MANAGER
	 */
	public java.lang.Boolean isExternalManager () {
		return externalManager;
	}

	/**
	 * Set the value related to the column: EXTERNAL_MANAGER
	 * @param externalManager the EXTERNAL_MANAGER value
	 */
	public void setExternalManager (java.lang.Boolean externalManager) {
		this.externalManager = externalManager;
	}



	/**
	 * Return the value associated with the column: EXTERNAL_MGR_LABEL
	 */
	public java.lang.String getExternalMgrLabel () {
		return externalMgrLabel;
	}

	/**
	 * Set the value related to the column: EXTERNAL_MGR_LABEL
	 * @param externalMgrLabel the EXTERNAL_MGR_LABEL value
	 */
	public void setExternalMgrLabel (java.lang.String externalMgrLabel) {
		this.externalMgrLabel = externalMgrLabel;
	}



	/**
	 * Return the value associated with the column: EXTERNAL_MGR_ABBV
	 */
	public java.lang.String getExternalMgrAbbv () {
		return externalMgrAbbv;
	}

	/**
	 * Set the value related to the column: EXTERNAL_MGR_ABBV
	 * @param externalMgrAbbv the EXTERNAL_MGR_ABBV value
	 */
	public void setExternalMgrAbbv (java.lang.String externalMgrAbbv) {
		this.externalMgrAbbv = externalMgrAbbv;
	}



	/**
	 * Return the value associated with the column: DIST_PRIORITY
	 */
	public java.lang.Integer getDistributionPrefPriority () {
		return distributionPrefPriority;
	}

	/**
	 * Set the value related to the column: DIST_PRIORITY
	 * @param distributionPrefPriority the DIST_PRIORITY value
	 */
	public void setDistributionPrefPriority (java.lang.Integer distributionPrefPriority) {
		this.distributionPrefPriority = distributionPrefPriority;
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
	 * Return the value associated with the column: STATUS_TYPE
	 */
	public org.unitime.timetable.model.DepartmentStatusType getStatusType () {
		return statusType;
	}

	/**
	 * Set the value related to the column: STATUS_TYPE
	 * @param statusType the STATUS_TYPE value
	 */
	public void setStatusType (org.unitime.timetable.model.DepartmentStatusType statusType) {
		this.statusType = statusType;
	}



	/**
	 * Return the value associated with the column: SOLVER_GROUP_ID
	 */
	public org.unitime.timetable.model.SolverGroup getSolverGroup () {
		return solverGroup;
	}

	/**
	 * Set the value related to the column: SOLVER_GROUP_ID
	 * @param solverGroup the SOLVER_GROUP_ID value
	 */
	public void setSolverGroup (org.unitime.timetable.model.SolverGroup solverGroup) {
		this.solverGroup = solverGroup;
	}



	/**
	 * Return the value associated with the column: subjectAreas
	 */
	public java.util.Set getSubjectAreas () {
		return subjectAreas;
	}

	/**
	 * Set the value related to the column: subjectAreas
	 * @param subjectAreas the subjectAreas value
	 */
	public void setSubjectAreas (java.util.Set subjectAreas) {
		this.subjectAreas = subjectAreas;
	}

	public void addTosubjectAreas (org.unitime.timetable.model.SubjectArea subjectArea) {
		if (null == getSubjectAreas()) setSubjectAreas(new java.util.HashSet());
		getSubjectAreas().add(subjectArea);
	}



	/**
	 * Return the value associated with the column: roomDepts
	 */
	public java.util.Set getRoomDepts () {
		return roomDepts;
	}

	/**
	 * Set the value related to the column: roomDepts
	 * @param roomDepts the roomDepts value
	 */
	public void setRoomDepts (java.util.Set roomDepts) {
		this.roomDepts = roomDepts;
	}

	public void addToroomDepts (org.unitime.timetable.model.RoomDept roomDept) {
		if (null == getRoomDepts()) setRoomDepts(new java.util.HashSet());
		getRoomDepts().add(roomDept);
	}



	/**
	 * Return the value associated with the column: datePatterns
	 */
	public java.util.Set getDatePatterns () {
		return datePatterns;
	}

	/**
	 * Set the value related to the column: datePatterns
	 * @param datePatterns the datePatterns value
	 */
	public void setDatePatterns (java.util.Set datePatterns) {
		this.datePatterns = datePatterns;
	}



	/**
	 * Return the value associated with the column: timePatterns
	 */
	public java.util.Set getTimePatterns () {
		return timePatterns;
	}

	/**
	 * Set the value related to the column: timePatterns
	 * @param timePatterns the timePatterns value
	 */
	public void setTimePatterns (java.util.Set timePatterns) {
		this.timePatterns = timePatterns;
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
	 * Return the value associated with the column: instructors
	 */
	public java.util.Set getInstructors () {
		return instructors;
	}

	/**
	 * Set the value related to the column: instructors
	 * @param instructors the instructors value
	 */
	public void setInstructors (java.util.Set instructors) {
		this.instructors = instructors;
	}

	public void addToinstructors (org.unitime.timetable.model.DepartmentalInstructor departmentalInstructor) {
		if (null == getInstructors()) setInstructors(new java.util.HashSet());
		getInstructors().add(departmentalInstructor);
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.Department)) return false;
		else {
			org.unitime.timetable.model.Department department = (org.unitime.timetable.model.Department) obj;
			if (null == this.getUniqueId() || null == department.getUniqueId()) return false;
			else return (this.getUniqueId().equals(department.getUniqueId()));
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
