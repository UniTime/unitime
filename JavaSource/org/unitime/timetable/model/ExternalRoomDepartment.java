/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
*/
package org.unitime.timetable.model;

import java.util.Iterator;
import java.util.Set;

import org.unitime.timetable.model.base.BaseExternalRoomDepartment;



/**
 * @author Stephanie Schluttenhofer, Tomas Muller
 */
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
