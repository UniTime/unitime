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
package org.unitime.timetable.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.util.MessageResources;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.form.ClassInstructorAssignmentForm;
import org.unitime.timetable.form.ClassListForm;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.Settings;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.UserData;
import org.unitime.timetable.model.comparators.ClassComparator;
import org.unitime.timetable.model.comparators.DepartmentalInstructorComparator;
import org.unitime.timetable.model.comparators.SchedulingSubpartComparator;
import org.unitime.timetable.model.dao.InstrOfferingConfigDAO;
import org.unitime.timetable.solver.WebSolver;
import org.unitime.timetable.util.Constants;


public class ClassInstructorAssignmentAction extends Action {
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

        if(!Web.isLoggedIn( request.getSession() )) {
            throw new Exception ("Access Denied.");
        }

        MessageResources rsc = getResources(request);
        User user = Web.getUser(request.getSession());
        TimetableManager tm = TimetableManager.getManager(user);
        ClassInstructorAssignmentForm frm = (ClassInstructorAssignmentForm) form;

        // Get operation
        String op = (request.getParameter("op")==null)
						? (frm.getOp()==null || frm.getOp().length()==0)
						        ? (request.getAttribute("op")==null)
						                ? null
						                : request.getAttribute("op").toString()
						        : frm.getOp()
						: request.getParameter("op");

        if(op==null)
            op = request.getParameter("hdnOp");

        if(op==null || op.trim().length()==0)
            throw new Exception ("Operation could not be interpreted: " + op);

        // Instructional Offering Config Id
        String instrOffrConfigId = "";

        // Set the operation
        frm.setOp(op);

        // Set the proxy so we can get the class time and room
        frm.setProxy(WebSolver.getClassAssignmentProxy(request.getSession()));

    	instrOffrConfigId = (request.getParameter("uid")==null)
				? (request.getAttribute("uid")==null)
				        ? frm.getInstrOffrConfigId()!=null
				        		? frm.getInstrOffrConfigId().toString()
				        		: null
				        : request.getAttribute("uid").toString()
				: request.getParameter("uid");

        InstrOfferingConfigDAO iocDao = new InstrOfferingConfigDAO();
        InstrOfferingConfig ioc = iocDao.get(Long.valueOf(instrOffrConfigId));
        frm.setInstrOffrConfigId(Long.valueOf(instrOffrConfigId));

        ArrayList instructors = new ArrayList(ioc.getDepartment().getInstructors());
	    Collections.sort(instructors, new DepartmentalInstructorComparator());
        request.setAttribute(DepartmentalInstructor.INSTR_LIST_ATTR_NAME, instructors);

        // First access to screen
        if(op.equalsIgnoreCase(rsc.getMessage("button.classInstructorAssignment"))) {
            doLoad(request, frm, instrOffrConfigId, user, ioc);
        }

        if(op.equals(rsc.getMessage("button.classInstrUpdate")) ||
        		op.equals(rsc.getMessage("button.nextInstructionalOffering")) ||
        		op.equals(rsc.getMessage("button.previousInstructionalOffering")) ||
        		op.equals(rsc.getMessage("button.unassignAll"))) {

            if (op.equals(rsc.getMessage("button.unassignAll"))) {
            	frm.unassignAllInstructors();
            }

        	// Validate input prefs
            ActionMessages errors = frm.validate(mapping, request);

            // No errors - Update class
            if(errors.size()==0) {

            	try {
            		frm.updateClasses();

                    InstrOfferingConfig cfg = new InstrOfferingConfigDAO().get(frm.getInstrOffrConfigId());

                    ChangeLog.addChange(
                            null,
                            request,
                            cfg,
                            ChangeLog.Source.CLASS_INSTR_ASSIGN,
                            ChangeLog.Operation.UPDATE,
                            cfg.getInstructionalOffering().getControllingCourseOffering().getSubjectArea(),
                            null);


                    request.setAttribute("io", frm.getInstrOfferingId());

    	            if (op.equals(rsc.getMessage("button.nextInstructionalOffering"))) {
    	            	response.sendRedirect(response.encodeURL("classInstructorAssignment.do?uid="+frm.getNextId())+"&op="+rsc.getMessage("button.classInstructorAssignment"));
    	            	return null;
    	            }

    	            if (op.equals(rsc.getMessage("button.previousInstructionalOffering"))) {
    	            	response.sendRedirect(response.encodeURL("classInstructorAssignment.do?uid="+frm.getPreviousId())+"&op="+rsc.getMessage("button.classInstructorAssignment"));
    	            	return null;
    	            }

    	            return mapping.findForward("instructionalOfferingDetail");
            	} catch (Exception e) {
            		throw e;
            	}
            }
            else {
                saveErrors(request, errors);
            }
        }

        if (op.equals(rsc.getMessage("button.delete"))) {
        	frm.deleteInstructor();
        }

        if (op.equals(rsc.getMessage("button.addInstructor"))) {
        	frm.addInstructor();
        }
        
        return mapping.findForward("classInstructorAssignment");
    }

	/**
     * Loads the form with the classes that are part of the instructional offering config
     * @param frm Form object
     * @param instrCoffrConfigId Instructional Offering Config Id
     * @param user User object
     */
    private void doLoad(
    		HttpServletRequest request,
    		ClassInstructorAssignmentForm frm,
            String instrOffrConfigId,
            User user,
            InstrOfferingConfig ioc) throws Exception {

    	HttpSession session = request.getSession();

        // Check uniqueid
        if(instrOffrConfigId==null || instrOffrConfigId.trim().length()==0)
            throw new Exception ("Missing Instructional Offering Config.");

        // Load details
        InstructionalOffering io = ioc.getInstructionalOffering();

        // Load form properties
        frm.setInstrOffrConfigId(ioc.getUniqueId());
        frm.setInstrOffrConfigLimit(ioc.getLimit());
        frm.setInstrOfferingId(io.getUniqueId());

        String name = io.getCourseNameWithTitle();
        if (io.hasMultipleConfigurations()) {
        	name += " [" + ioc.getName() +"]";
        }
        frm.setInstrOfferingName(name);

        if (ioc.getSchedulingSubparts() == null || ioc.getSchedulingSubparts().size() == 0)
        	throw new Exception("Instructional Offering Config has not been defined.");

        InstrOfferingConfig config = ioc.getNextInstrOfferingConfig(session, user, false, true);
        if(config != null) {
        	frm.setNextId(config.getUniqueId().toString());
        } else {
        	frm.setNextId(null);
        }

        config = ioc.getPreviousInstrOfferingConfig(session, user, false, true);
        if(config != null) {
            frm.setPreviousId(config.getUniqueId().toString());
        } else {
            frm.setPreviousId(null);
        }

        ArrayList subpartList = new ArrayList(ioc.getSchedulingSubparts());
        Collections.sort(subpartList, new SchedulingSubpartComparator());

        for(Iterator it = subpartList.iterator(); it.hasNext();){
        	SchedulingSubpart ss = (SchedulingSubpart) it.next();
    		if (ss.getClasses() == null || ss.getClasses().size() == 0)
    			throw new Exception("Initial setup of Instructional Offering Config has not been completed.");
    		if (ss.getParentSubpart() == null){
        		loadClasses(frm, user, ss.getClasses(), new Boolean(true), new String());
        	}
        }
    }

    private void loadClasses(ClassInstructorAssignmentForm frm, User user, Set classes, Boolean isReadOnly, String indent){
    	if (classes != null && classes.size() > 0){
    		ArrayList classesList = new ArrayList(classes);

        	if ("yes".equals(Settings.getSettingValue(user, Constants.SETTINGS_KEEP_SORT))) {
        		Collections.sort(classesList,
        			new ClassComparator(
        					UserData.getProperty(user.getId(),"InstructionalOfferingList.sortBy",ClassListForm.sSortByName),
        					frm.getProxy(),
        					false
        			)
        		);
        	} else {
        		Collections.sort(classesList, new ClassComparator(ClassComparator.COMPARE_BY_ITYPE) );
        	}

	    	Boolean readOnlyClass = new Boolean(false);
	    	Class_ cls = null;
	    	for(Iterator it = classesList.iterator(); it.hasNext();){
	    		cls = (Class_) it.next();
	    		if (!isReadOnly.booleanValue()){
	    			readOnlyClass = new Boolean(isReadOnly.booleanValue());
	    		} else {
	    			readOnlyClass = new Boolean(!cls.isLimitedEditable(user));
	    		}
	    		frm.addToClasses(cls, readOnlyClass, indent);
	    		loadClasses(frm, user, cls.getChildClasses(), new Boolean(true), indent + "&nbsp;&nbsp;&nbsp;&nbsp;");
	    	}
    	}
    }

    private void doUpdate(
    		HttpServletRequest request,
    		ClassInstructorAssignmentForm frm,
    		User user) {
	}

}
