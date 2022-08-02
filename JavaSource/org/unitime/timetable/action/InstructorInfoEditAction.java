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

import java.util.HashSet;
import java.util.Iterator;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.tiles.annotation.TilesDefinition;
import org.apache.struts2.tiles.annotation.TilesPutAttribute;
import org.hibernate.Transaction;
import org.unitime.commons.Debug;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.interfaces.ExternalClassEditAction;
import org.unitime.timetable.interfaces.ExternalUidLookup.UserInfo;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.webutil.BackTracker;


/** 
 * @author Tomas Muller, Stephanie Schluttenhofer, Zuzana Mullerova
 */
@Action(value = "instructorInfoEdit", results = {
		@Result(name = "showEdit", type = "tiles", location = "instructorInfoEdit.tiles"),
		@Result(name = "showDetail", type = "redirect", location = "/instructorDetail.action",
			params = { "instructorId", "${form.instructorId}" }
		),
		@Result(name = "showList", type = "redirect", location = "/instructorSearch.action")
	})
@TilesDefinition(name = "instructorInfoEdit.tiles", extend = "baseLayout", putAttributes =  {
		@TilesPutAttribute(name = "title", value = "Edit Instructor"),
		@TilesPutAttribute(name = "body", value = "/user/instructorInfoEdit.jsp"),
		@TilesPutAttribute(name = "showNavigation", value = "true"),
		@TilesPutAttribute(name = "checkRole", value = "false")
	})

public class InstructorInfoEditAction extends InstructorAction {
	private static final long serialVersionUID = -4279427823049903910L;

	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	
	public String execute() throws Exception {
		super.execute();
		
        //Read parameters
        String instructorId = request.getParameter("instructorId");
        if (instructorId == null) instructorId = form.getInstructorId();
        
        //Check instructor exists
        if(instructorId==null || instructorId.isEmpty()) 
            throw new Exception (MSG.exceptionInstructorInfoNotSupplied());
        
		sessionContext.checkPermission(instructorId, "DepartmentalInstructor", Right.InstructorEdit);
        
        form.setInstructorId(instructorId);
        
        // Cancel - Go back to Instructors Detail Screen
        if (MSG.actionBackToDetail().equals(op) 
                && instructorId!=null && !instructorId.isEmpty()) {
        	response.sendRedirect( response.encodeURL("instructorDetail.action?instructorId="+instructorId));
        	return null;
        }
        
        // Check ID
        if (MSG.actionLookupInstructor().equals(op)) {
            form.validate(this);
            if (!hasFieldErrors()) {
                findMatchingInstructor();
                if (form.getMatchFound()==null || !form.getMatchFound().booleanValue()) {
                	addFieldError("lookup", MSG.errorNoMatchingRecordsFound());
                }
            }
           	return "showEdit";
        }
        
        //update - Update the instructor and go back to Instructor Detail Screen
        if ((MSG.actionUpdateInstructor().equals(op) 
        		|| MSG.actionNextInstructor().equals(op) 
        		|| MSG.actionPreviousInstructor().equals(op))
                && instructorId!=null && !instructorId.isEmpty()) {
        	form.validate(this);
            if(!hasFieldErrors() && isDeptInstructorUnique()) {
	        	doUpdate();
	            
	        	if (MSG.actionNextInstructor().equals(op)) {
	            	response.sendRedirect(response.encodeURL("instructorInfoEdit.action?instructorId="+form.getNextId()));
	            	return null;
	        	}
	            
	            if (MSG.actionPreviousInstructor().equals(op)) {
	            	response.sendRedirect(response.encodeURL("instructorInfoEdit.action?instructorId="+form.getPreviousId()));
	            	return null;
	            }
	            return "showDetail";
            } else {
            	if (!hasFieldErrors()) {
                    addFieldError("uniqueId", MSG.errorInstructorIdAlreadyExistsInList());
            	}
            }
        }

        // Delete Instructor
        if (MSG.actionDeleteInstructor().equals(op)) {
        	doDelete();
        	return "showList";
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
        	return "showEdit";
        }
        
        //Load form 
        doLoad();
                
        BackTracker.markForBack(
        		request,
        		"instructorDetail.action?instructorId="+form.getInstructorId(),
        		MSG.backInstructor(form.getName()==null?"null":form.getName().trim()),
        		true, false);

        return "showEdit";
	}

	/**
	 * Deletes instructor
	 */
	private void doDelete() throws Exception {

	    String instructorId = form.getInstructorId();
	    DepartmentalInstructorDAO idao = new DepartmentalInstructorDAO();
	    
		sessionContext.checkPermission(instructorId, "DepartmentalInstructor", Right.InstructorDelete);

	    
		org.hibernate.Session hibSession = idao.getSession();
		Transaction tx = null;
		
		try {	
			tx = hibSession.beginTransaction();
	        DepartmentalInstructor inst = idao.get(Long.valueOf(instructorId));
	        
            ChangeLog.addChange(
                    hibSession, 
                    sessionContext, 
                    inst, 
                    ChangeLog.Source.INSTRUCTOR_EDIT, 
                    ChangeLog.Operation.DELETE, 
                    null, 
                    inst.getDepartment());

            HashSet<Class_> updatedClasses = new HashSet<Class_>();
            for (Iterator i=inst.getClasses().iterator();i.hasNext();) {
	        	ClassInstructor ci = (ClassInstructor)i.next();
	        	Class_ c = ci.getClassInstructing();
	        	updatedClasses.add(c);
	        	c.getClassInstructors().remove(ci);
	        	hibSession.saveOrUpdate(ci);
	        	hibSession.delete(ci);
	        }           
            
            for (Iterator i=inst.getExams().iterator();i.hasNext();) {
                Exam exam = (Exam)i.next();
                exam.getInstructors().remove(inst);
                hibSession.saveOrUpdate(exam);
            }
	        
	        for (Iterator i=inst.getAssignments().iterator();i.hasNext();) {
	        	Assignment a = (Assignment)i.next();
	        	a.getInstructors().remove(inst);
	        	hibSession.saveOrUpdate(a);
	        }
	        
	        Department d = null;
	        if (inst.getDepartment()!=null) {
	        	d = inst.getDepartment();
	        }
	        d.getInstructors().remove(inst);
	        
            hibSession.delete(inst);
            
	        tx.commit();
			
            String className = ApplicationProperty.ExternalActionClassEdit.value();
        	if (className != null && className.trim().length() > 0){
            	ExternalClassEditAction editAction = (ExternalClassEditAction) (Class.forName(className).getDeclaredConstructor().newInstance());
            	for(Class_ c : updatedClasses){
            		editAction.performExternalClassEditAction(c, hibSession);
            	}
        	}

			
	        hibSession.clear();
			
		} catch (Exception e) {
            Debug.error(e);
            try {
	            if(tx!=null && tx.isActive())
	                tx.rollback();
            }
            catch (Exception e1) { }
            throw e;
        }
	}
	
	/**
	 * Loads the non-editable instructor info into the form
	 * @param request
	 * @param form
	 */
	private void doLoad() {

	    String instructorId = form.getInstructorId();
	    DepartmentalInstructorDAO idao = new DepartmentalInstructorDAO();
        DepartmentalInstructor inst = idao.get(Long.valueOf(instructorId));      
	    
        // populate form
		form.setInstructorId(instructorId);
		
		form.setName(Constants.toInitialCase(inst.getFirstName(), "-".toCharArray())+ " " 
    			+ ((inst.getMiddleName() == null) ?"": Constants.toInitialCase(inst.getMiddleName(), "-".toCharArray()) )+ " " 
    			+ Constants.toInitialCase(inst.getLastName(), "-".toCharArray()));

		if (inst.getFirstName() != null) {
			form.setFname(inst.getFirstName().trim());
		}
		
		if (inst.getMiddleName() != null) {
			form.setMname(inst.getMiddleName().trim());
		}
		
		form.setLname(inst.getLastName().trim());
		form.setTitle(inst.getAcademicTitle());
		
	    String puid = inst.getExternalUniqueId();
		if (puid != null) {
			form.setPuId(puid);
		}
		
		form.setEmail(inst.getEmail());
		
		form.setDeptName(inst.getDepartment().getName().trim());
		
		if (inst.getPositionType() != null) {
			form.setPosType(inst.getPositionType().getUniqueId().toString());
		}
		
		if (inst.getCareerAcct() != null && inst.getCareerAcct().length()>0) {
			form.setCareerAcct(inst.getCareerAcct().trim());
		}
		else {
			if (puid != null && !puid.isEmpty() && DepartmentalInstructor.canLookupInstructor()) {
				try {
					UserInfo user = DepartmentalInstructor.lookupInstructor(puid);
					if (user != null && user.getUserName() != null)
						form.setCareerAcct(user.getUserName());
					else
						form.setCareerAcct("");
				} catch (Exception e) {}
			}
		}
		
		if (inst.getNote() != null) {
			form.setNote(inst.getNote().trim());
		}
        
        form.setIgnoreDist(inst.isIgnoreToFar()==null?false:inst.isIgnoreToFar().booleanValue());
                
        try {
			DepartmentalInstructor previous = inst.getPreviousDepartmentalInstructor(sessionContext, Right.InstructorEdit);
			form.setPreviousId(previous==null?null:previous.getUniqueId().toString());
			DepartmentalInstructor next = inst.getNextDepartmentalInstructor(sessionContext, Right.InstructorEdit);
			form.setNextId(next==null?null:next.getUniqueId().toString());
		} catch (Exception e) {
			Debug.error(e);
		}
		
	}

}

