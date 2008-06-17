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
package org.unitime.timetable.action;

import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.util.MessageResources;
import org.hibernate.Transaction;
import org.unitime.commons.Debug;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.form.InstructorEditForm;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Designator;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.webutil.BackTracker;


/** 
 * MyEclipse Struts
 * Creation date: 07-20-2006
 * 
 * XDoclet definition:
 * @struts.action path="/instructorInfoEdit" name="instructorEditForm" input="/user/instructorInfoEdit.jsp" scope="request"
 */
public class InstructorInfoEditAction extends InstructorAction {

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
		
		//Check permissions
		HttpSession httpSession = request.getSession();
		if (!Web.isLoggedIn(httpSession)) {
			throw new Exception("Access Denied.");
		}	
		
		super.execute(mapping, form, request, response);
		
		InstructorEditForm frm = (InstructorEditForm) form;
				
		User user = Web.getUser(httpSession);
		Long sessionId = (Long) user.getAttribute(Constants.SESSION_ID_ATTR_NAME);
        MessageResources rsc = getResources(request);
        ActionMessages errors = new ActionMessages();
        
        //Read parameters
        String instructorId = request.getParameter("instructorId");
        String op = frm.getOp();
        
        //Check instructor exists
        if(instructorId==null || instructorId.trim()=="") 
            throw new Exception ("Instructor Info not supplied.");
        
        frm.setInstructorId(instructorId);
        
        // Cancel - Go back to Instructors Detail Screen
        if(op.equals(rsc.getMessage("button.returnToDetail")) 
                && instructorId!=null && instructorId.trim()!="") {
        	response.sendRedirect( response.encodeURL("instructorDetail.do?instructorId="+instructorId));
        }
        
        // Check ID
        if(op.equals(rsc.getMessage("button.checkPuId"))) {
            errors = frm.validate(mapping, request);
            if(errors.size()==0) {
                findMatchingInstructor(frm, request);
                if (frm.getMatchFound()==null || !frm.getMatchFound().booleanValue()) {
                    errors.add("lookup", 
                            	new ActionMessage("errors.generic", "No matching records found"));
                }
            }
            
        	saveErrors(request, errors);
           	return mapping.findForward("showEdit");
        }
        
        //update - Update the instructor and go back to Instructor Detail Screen
        if((op.equals(rsc.getMessage("button.update")) || op.equals(rsc.getMessage("button.nextInstructor")) || op.equals(rsc.getMessage("button.previousInstructor")))
                && instructorId!=null && instructorId.trim()!="") {
            errors = frm.validate(mapping, request);
            if(errors.size()==0 && isDeptInstructorUnique(frm, request)) {
	        	doUpdate(frm, request);
	        	request.setAttribute("instructorId", frm.getInstructorId());
	            
	        	if (op.equals(rsc.getMessage("button.nextInstructor")))
	            	response.sendRedirect(response.encodeURL("instructorInfoEdit.do?instructorId="+frm.getNextId()));
	            
	            if (op.equals(rsc.getMessage("button.previousInstructor")))
	            	response.sendRedirect(response.encodeURL("instructorInfoEdit.do?instructorId="+frm.getPreviousId()));

	            return mapping.findForward("showDetail");
            } else {
                if (errors.size()==0) {
                    errors.add( "uniqueId", 
                        	new ActionMessage("errors.generic", "This Instructor Id already exists in your instructor list."));
            	}
            	saveErrors(request, errors);            	
            }
        }

        // Delete Instructor
        if(op.equals(rsc.getMessage("button.deleteInstructor"))) {
        	doDelete(request, frm);
        	return mapping.findForward("showList");
        }

        // search select
        if(op.equals(rsc.getMessage("button.selectInstructor")) ) {
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
        		"Instructor ("+ (frm.getName()==null?"null":frm.getName().trim()) +")",
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
	    
		org.hibernate.Session hibSession = idao.getSession();
		Transaction tx = null;
		
		try {	
			tx = hibSession.beginTransaction();
	        DepartmentalInstructor inst = idao.get(new Long(instructorId));
	        
            ChangeLog.addChange(
                    hibSession, 
                    request, 
                    inst, 
                    ChangeLog.Source.INSTRUCTOR_EDIT, 
                    ChangeLog.Operation.DELETE, 
                    null, 
                    inst.getDepartment());

            for (Iterator i=inst.getClasses().iterator();i.hasNext();) {
	        	ClassInstructor ci = (ClassInstructor)i.next();
	        	ci.getClassInstructing().getClassInstructors().remove(ci);
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
	        
	        for (Iterator i=inst.getDesignatorSubjectAreas().iterator();i.hasNext();) {
	            Designator d = (Designator) i.next();
	            SubjectArea sa = d.getSubjectArea();
	            sa.getDesignatorInstructors().remove(d);
	        	hibSession.saveOrUpdate(sa);
	        	hibSession.delete(d);
	        }
	        
	        Department d = null;
	        if (inst.getDepartment()!=null) {
	        	d = inst.getDepartment();
	        }
	        
            hibSession.delete(inst);
            
	        tx.commit();
			hibSession.refresh(d);
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
			if (puid != null && puid.length()>=8 && User.canIdentify()) {
			    User instructor = User.identify(puid);
			    if (instructor!=null)
			        frm.setCareerAcct(instructor.getLogin());
			    else 
			        frm.setCareerAcct("Not Found");
			}
		}
		
		if (inst.getNote() != null) {
			frm.setNote(inst.getNote().trim());
		}
        
        frm.setIgnoreDist(inst.isIgnoreToFar()==null?false:inst.isIgnoreToFar().booleanValue());
		
        try {
			User user = Web.getUser(request.getSession());
			DepartmentalInstructor previous = inst.getPreviousDepartmentalInstructor(request.getSession(),user,false,true);
			frm.setPreviousId(previous==null?null:previous.getUniqueId().toString());
			DepartmentalInstructor next = inst.getNextDepartmentalInstructor(request.getSession(),user,false,true);
			frm.setNextId(next==null?null:next.getUniqueId().toString());
		} catch (Exception e) {
			Debug.error(e);
		}
		
	}

}

