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
package org.unitime.timetable.model;

import org.unitime.timetable.model.base.BaseExternalRoomDepartment;



public class ExternalRoomDepartment extends BaseExternalRoomDepartment {
	private static final long serialVersionUID = 1L;

	public static String ASSIGNMENT_TYPE_ASSIGNED = "assigned";
	public static String ASSIGNMENT_TYPE_SCHEDULING = "scheduling";
	
/*[CONSTRUCTOR MARKER BEGIN]*/
	public ExternalRoomDepartment () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public ExternalRoomDepartment (java.lang.Long uniqueId) {
		super(uniqueId);
	}

	/**
	 * Constructor for required fields
	 */
	public ExternalRoomDepartment (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.ExternalRoom room,
		java.lang.String departmentCode,
		java.lang.Integer percent,
		java.lang.String assignmentType) {

		super (
			uniqueId,
			room,
			departmentCode,
			percent,
			assignmentType);
	}

/*[CONSTRUCTOR MARKER END]*/

	/**
	 * Check for assigned department
	 * @return true if assigned department
	 */
	public boolean isAssigned() {
		
		return checkAssignmentType(ASSIGNMENT_TYPE_ASSIGNED);
	}
	
	/**
	 * Check for scheduling department
	 * @return true if scheduling department
	 */
	public boolean isScheduling() {
		
		return checkAssignmentType(ASSIGNMENT_TYPE_SCHEDULING);
	}
	
	/**
	 * Check assignment type
	 * @param assignType
	 * @return true if match
	 */
	private boolean checkAssignmentType(String assignmentType) {
		
		boolean result = false;
		if(this.getAssignmentType().equals(assignmentType)) {
			result = true;
		}
		
		return result;
	}
}