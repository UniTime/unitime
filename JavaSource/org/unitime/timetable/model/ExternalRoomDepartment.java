/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.model;

import java.util.Iterator;
import java.util.Set;

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
	
	public static boolean isControllingExternalDept(ExternalRoomDepartment externalRoomDept, Set deptList){
		String asgn = "assigned";
		String sched = "scheduling";
		if (externalRoomDept == null || deptList == null || deptList.isEmpty()){
			return(false);
		}
		if (deptList.size() == 1) {
			if (deptList.contains(externalRoomDept)){
				return(true);
			} else {
				return(false);
			}
		} else {
			boolean isControl = true;
			ExternalRoomDepartment erd = null;
			for (Iterator erdIt = deptList.iterator(); (erdIt.hasNext() && isControl);){
				erd = (ExternalRoomDepartment) erdIt.next();
				if (erd != null && !erd.equals(externalRoomDept)){
					if (!erd.getDepartmentCode().equals(externalRoomDept.getDepartmentCode())){
						if (externalRoomDept.getAssignmentType().equals(asgn)){
							if (erd.getAssignmentType().equals(asgn) && erd.getPercent().compareTo(externalRoomDept.getPercent()) >= 0){
								isControl = false;
							} else if (erd.getAssignmentType().equals(sched)){
								isControl = false;
							}
						} else if (externalRoomDept.getAssignmentType().equals(sched)){
							if (erd.getAssignmentType().equals(sched) && erd.getPercent().compareTo(externalRoomDept.getPercent()) >= 0){
								isControl = false;
							}
						}
					}
				}
			}
			return(isControl);
		}
	}
}
