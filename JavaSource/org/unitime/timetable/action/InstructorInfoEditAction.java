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
package org.unitime.timetable.action;

import java.util.HashSet;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.action.ActionRedirect;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.commons.Debug;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.form.InstructorEditForm;
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
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.webutil.BackTracker;


/** 
 * MyEclipse Struts
 * Creation date: 07-20-2006
 * 
 * XDoclet definition:
 * @struts.action path="/instructorInfoEdit" name="instructorEditForm" input="/user/instructorInfoEdit.jsp" scope="request"
 */
@Service("/instructorInfoEdit")
public class InstructorInfoEditAction extends InstructorAction {

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
				
		ActionMessages errors = new ActionMessages();
        
        //Read parameters
        String instructorId = request.getParameter("instructorId");
        String op = frm.getOp();
        
        //Check instructor exists
        if(instructorId==null || instructorId.trim()=="") 
            throw new Exception (MSG.exceptionInstructorInfoNotSupplied());
        
		sessionContext.checkPermission(instructorId, "DepartmentalInstructor", Right.InstructorEdit);
        
        frm.setInstructorId(instructorId);
        
        // Cancel - Go back to Instructors Detail Screen
        if(op.equals(MSG.actionBackToDetail()) 
                && instructorId!=null && instructorId.trim()!="") {
        	response.sendRedirect( response.encodeURL("instructorDetail.do?instructorId="+instructorId));
        }
        
        // Check ID
        if(op.equals(MSG.actionLookupInstructor())) {
            errors = frm.validate(mapping, request);
            if(errors.size()==0) {
                findMatchingInstructor(frm, request);
                if (frm.getMatchFound()==null || !frm.getMatchFound().booleanValue()) {
                    errors.add("lookup", 
                            	new ActionMessage("errors.generic", MSG.errorNoMatchingRecordsFound()));
                }
            }
            
        	saveErrors(request, errors);
           	return mapping.findForward("showEdit");
        }
        
        //update - Update the instructor and go back to Instructor Detail Screen
        if((op.equals(MSG.actionUpdateInstructor()) 
        		|| op.equals(MSG.actionNextInstructor()) 
        		|| op.equals(MSG.actionPreviousInstructor()))
                && instructorId!=null && instructorId.trim()!="") {
            errors = frm.validate(mapping, request);
            if(errors.size()==0 && isDeptInstructorUnique(frm, request)) {
	        	doUpdate(frm, request);
	            
	        	if (op.equals(MSG.actionNextInstructor()))
	            	response.sendRedirect(response.encodeURL("instructorInfoEdit.do?instructorId="+frm.getNextId()));
	            
	            if (op.equals(MSG.actionPreviousInstructor()))
	            	response.sendRedirect(response.encodeURL("instructorInfoEdit.do?instructorId="+frm.getPreviousId()));

	            ActionRedirect redirect = new ActionRedirect(mapping.findForward("showDetail"));
	            redirect.addParameter("instructorId", frm.getInstructorId());
	            return redirect;
            } else {
                if (errors.size()==0) {
                    errors.add( "uniqueId", 
                        	new ActionMessage("errors.generic", MSG.errorInstructorIdAlreadyExistsInList()));
            	}
            	saveErrors(request, errors);            	
            }
        }

        // Delete Instructor
        if(op.equals(MSG.actionDeleteInstructor())) {
        	doDelete(request, frm);
        	return mapping.findForward("showList");
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
        	return mapping.findForward("showEdit");
        }
        
        //Load form 
        doLoad(request, frm);
        
        BackTracker.markForBack(
        		request,
        		"instructorDetail.do?instructorId="+frm.getInstructorId(),
        		MSG.backInstructor(frm.getName()==null?"null":frm.getName().trim()),
        		true, false);

        return mapping.findForward("showEdit");
	}

	/**
	 * Deletes instructor
	 * @param request
	 * @param frm
	 */
	private void doDelete(HttpServletRequest request, InstructorEditForm frm) throws Exception {

	    String instructorId = frm.getInstructorId();
	    DepartmentalInstructorDAO idao = new DepartmentalInstructorDAO();
	    
		sessionContext.checkPermission(instructorId, "DepartmentalInstructor", Right.InstructorDelete);

	    
		org.hibernate.Session hibSession = idao.getSession();
		Transaction tx = null;
		
		try {	
			tx = hibSession.beginTransaction();
	        DepartmentalInstructor inst = idao.get(new Long(instructorId));
	        
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
			
            String className = ApplicationProperties.getProperty("tmtbl.external.class.edit_action.class");
        	if (className != null && className.trim().length() > 0){
            	ExternalClassEditAction editAction = (ExternalClassEditAction) (Class.forName(className).newInstance());
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
	 * @param frm
	 */
	private void doLoad(HttpServletRequest request, InstructorEditForm frm) {

	    String instructorId = frm.getInstructorId();
	    DepartmentalInstructorDAO idao = new DepartmentalInstructorDAO();
        DepartmentalInstructor inst = idao.get(new Long(instructorId));      
	    
        // populate form
		frm.setInstructorId(instructorId);
		
		frm.setName(Constants.toInitialCase(inst.getFirstName(), "-".toCharArray())+ " " 
    			+ ((inst.getMiddleName() == null) ?"": Constants.toInitialCase(inst.getMiddleName(), "-".toCharArray()) )+ " " 
    			+ Constants.toInitialCase(inst.getLastName(), "-".toCharArray()));

		if (inst.getFirstName() != null) {
			frm.setFname(inst.getFirstName().trim());
		}
		
		if (inst.getMiddleName() != null) {
			frm.setMname(inst.getMiddleName().trim());
		}		
		
		frm.setLname(inst.getLastName().trim());
		
	    String puid = inst.getExternalUniqueId();
		if (puid != null) {
			frm.setPuId(puid);
		}
		
		frm.setEmail(inst.getEmail());
		
		frm.setDeptName(inst.getDepartment().getName().trim());
		
		if (inst.getPositionType() != null) {
			frm.setPosType(inst.getPositionType().getUniqueId().toString());
		}
		
		if (inst.getCareerAcct() != null && inst.getCareerAcct().length()>0) {
			frm.setCareerAcct(inst.getCareerAcct().trim());
		}
		else {
			if (puid != null && !puid.isEmpty() && DepartmentalInstructor.canLookupInstructor()) {
				try {
					UserInfo user = DepartmentalInstructor.lookupInstructor(puid);
					if (user != null && user.getUserName() != null)
						frm.setCareerAcct(user.getUserName());
					else
						frm.setCareerAcct(MSG.valueInstructorAccountNameNotFound());
				} catch (Exception e) {}
			}
		}
		
		if (inst.getNote() != null) {
			frm.setNote(inst.getNote().trim());
		}
        
        frm.setIgnoreDist(inst.isIgnoreToFar()==null?false:inst.isIgnoreToFar().booleanValue());
        
        try {
			DepartmentalInstructor previous = inst.getPreviousDepartmentalInstructor(sessionContext, Right.InstructorEdit);
			frm.setPreviousId(previous==null?null:previous.getUniqueId().toString());
			DepartmentalInstructor next = inst.getNextDepartmentalInstructor(sessionContext, Right.InstructorEdit);
			frm.setNextId(next==null?null:next.getUniqueId().toString());
		} catch (Exception e) {
			Debug.error(e);
		}
		
	}

}

