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
package org.unitime.timetable.action;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.tiles.annotation.TilesDefinition;
import org.apache.struts2.tiles.annotation.TilesPutAttribute;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.security.rights.Right;


/** 
 * @author Tomas Muller, Zuzana Mullerova
 */
@Action(value = "instructorAdd", results = {
		@Result(name = "showAdd", type = "tiles", location = "instructorAdd.tiles"),
	})
@TilesDefinition(name = "instructorAdd.tiles", extend = "baseLayout", putAttributes =  {
		@TilesPutAttribute(name = "title", value = "Add Instructor"),
		@TilesPutAttribute(name = "body", value = "/user/instructorAdd.jsp")
	})
public class InstructorAddAction extends InstructorAction {
	private static final long serialVersionUID = 3203081761214701242L;

	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);

	public String execute() throws Exception {
		super.execute();
		
		form.setMatchFound(null);
		
        // Cancel adding an instructor - Go back to Instructors screen
        if (MSG.actionBackToInstructors().equals(op)) {
        	response.sendRedirect( response.encodeURL("instructorSearch.action"));
        	return null;
        }
        
		//get department
		if (sessionContext.getAttribute(SessionAttribute.DepartmentId) != null) {
			String deptId = (String) sessionContext.getAttribute(SessionAttribute.DepartmentId);
			Department d = DepartmentDAO.getInstance().get(Long.valueOf(deptId));
			form.setDeptName(d.getName().trim());
			form.setDeptCode(d.getDeptCode());
		}
		
		sessionContext.checkPermission(form.getDeptCode(), "Department", Right.InstructorAdd);
				
        //update - Update the instructor and go back to Instructor List Screen
        if (MSG.actionSaveInstructor().equals(op)) {
            form.validate(this);
            if (!hasFieldErrors() && isDeptInstructorUnique()) {
	        	doUpdate();
	        	response.sendRedirect( response.encodeURL("instructorSearch.action"));
	        	return null;
            } else {
                if (!hasFieldErrors()) {
                    addFieldError( "uniqueId", MSG.errorInstructorIdAlreadyExistsInList());
            	}
            	return "showAdd";
            }
        }
		
        // lookup 
        if (MSG.actionLookupInstructor().equals(op)) {
            form.validate(this);
            if (!hasFieldErrors()) {
                findMatchingInstructor();
                if (form.getMatchFound()==null || !form.getMatchFound().booleanValue()) {
                	addFieldError("lookup", MSG.errorNoMatchingRecordsFound());
                }
            }
        	return "showAdd";
        }
        
        // search select
        if (MSG.actionSelectInstructor().equals(op)) {
            String select = form.getSearchSelect();            
            if (select!=null && select.trim().length()>0) {
	            if (select.equalsIgnoreCase("i2a2")) {
	                fillI2A2Info();
	            }
	            else {
	                fillStaffInfo();
	            }
            }
            else {
            	addFieldError("lookup", MSG.errorNoInstructorSelectedFromList());
            }
        	return "showAdd";
        }
        
		return "showAdd";
	}
	
}

