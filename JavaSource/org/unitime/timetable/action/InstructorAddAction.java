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

import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.form.InstructorEditForm;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.LookupTables;


/** 
 * MyEclipse Struts
 * Creation date: 07-20-2006
 * 
 * XDoclet definition:
 * @struts.action path="/addNewInstructor" name="instructorEditForm" input="/user/addNewInstructor.jsp" scope="request"
 *
 * @author Tomas Muller, Zuzana Mullerova
 */
@Service("/instructorAdd")
public class InstructorAddAction extends InstructorAction {
	
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	
	@Autowired SessionContext sessionContext;

	// --------------------------------------------------------- Instance Variables

	// --------------------------------------------------------- Methods

	/** 
	 * Method execute
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return ActionForward
	 */
	public ActionForward execute(
		ActionMapping mapping,
		ActionForm form,
		HttpServletRequest request,
		HttpServletResponse response) throws Exception {
		
		super.execute(mapping, form, request, response);
		
		InstructorEditForm frm = (InstructorEditForm) form;
		frm.setMatchFound(null);
		ActionMessages errors = new ActionMessages();
		String op = frm.getOp();
		
        // Cancel adding an instructor - Go back to Instructors screen
        if(op.equals(MSG.actionBackToInstructors())) {
        	response.sendRedirect( response.encodeURL("instructorList.do"));
        	return null;
        }
        
		//get department
		if (sessionContext.getAttribute(SessionAttribute.DepartmentId) != null) {
			String deptId = (String) sessionContext.getAttribute(SessionAttribute.DepartmentId);
			Department d = new DepartmentDAO().get(new Long(deptId));
			frm.setDeptName(d.getName().trim());
			frm.setDeptCode(d.getDeptCode());
	        LookupTables.setupInstructorAttributes(request, d);
		}
		
		Vector<PreferenceLevel> prefs = new Vector<PreferenceLevel>();
    	for (PreferenceLevel pref: PreferenceLevel.getPreferenceLevelList()) {
    		if (!pref.getPrefProlog().equalsIgnoreCase(PreferenceLevel.sRequired))
    			prefs.addElement(pref);
    	}
    	request.setAttribute(PreferenceLevel.PREF_LEVEL_ATTR_NAME, prefs);
		
		sessionContext.checkPermission(frm.getDeptCode(), "Department", Right.InstructorAdd);
				
        //update - Update the instructor and go back to Instructor List Screen
        if(op.equals(MSG.actionSaveInstructor()) ) {
            errors = frm.validate(mapping, request);
            if(errors.size()==0 && isDeptInstructorUnique(frm, request)) {
	        	doUpdate(frm, request);
	        	response.sendRedirect( response.encodeURL("instructorList.do"));
	        	return null;
            } else {
                if (errors.size()==0) {
                    errors.add( "uniqueId", 
                        	new ActionMessage("errors.generic", MSG.errorInstructorIdAlreadyExistsInList()));
            	}
            	saveErrors(request, errors);
            	return mapping.findForward("showAdd");
            }
        }
		
        // lookup 
        if(op.equals(MSG.actionLookupInstructor()) ) {
            errors = frm.validate(mapping, request);
            if(errors.size()==0) {
                findMatchingInstructor(frm, request);
                if (frm.getMatchFound()==null || !frm.getMatchFound().booleanValue()) {
                    errors.add("lookup", 
                            	new ActionMessage("errors.generic", MSG.errorNoMatchingRecordsFound()));
                }
            }
            
        	saveErrors(request, errors);
        	return mapping.findForward("showAdd");
        }
        
        // search select
        if(op.equals(MSG.actionSelectInstructor()) ) {
            String select = frm.getSearchSelect();            
            if (select!=null && select.trim().length()>0) {
	            if (select.equalsIgnoreCase("i2a2")) {
	                fillI2A2Info(frm, request);
	            }
	            else {
	                fillStaffInfo(frm, request);
	            }
            }
            else {
                errors.add("lookup", 
                    	new ActionMessage("errors.generic", MSG.errorNoInstructorSelectedFromList()));
            	saveErrors(request, errors);
            }
        	return mapping.findForward("showAdd");
        }
        
		return mapping.findForward("showAdd");
	}
	
}

