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

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.util.MessageResources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.form.ClassInstructorAssignmentForm;
import org.unitime.timetable.interfaces.ExternalInstrOfferingConfigAssignInstructorsAction;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.StudentSectioningQueue;
import org.unitime.timetable.model.comparators.ClassComparator;
import org.unitime.timetable.model.comparators.ClassCourseComparator;
import org.unitime.timetable.model.comparators.DepartmentalInstructorComparator;
import org.unitime.timetable.model.comparators.SchedulingSubpartComparator;
import org.unitime.timetable.model.dao.InstrOfferingConfigDAO;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.permissions.Permission;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.WebSolver;

/**
 * @author Tomas Muller, Zuzana Mullerova, Stephanie Schluttenhofer
 */
@Service("/classInstructorAssignment")
public class ClassInstructorAssignmentAction extends Action {

	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	
	@Autowired SessionContext sessionContext;
	
	@Autowired Permission<InstructionalOffering> permissionOfferingLockNeeded;
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

        MessageResources rsc = getResources(request);
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
            throw new Exception (MSG.exceptionOperationNotInterpreted() + op);

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
        
        sessionContext.checkPermission(ioc, Right.AssignInstructors);

        ArrayList instructors = new ArrayList(ioc.getDepartment().getInstructors());
	    Collections.sort(instructors, new DepartmentalInstructorComparator());
        request.setAttribute(DepartmentalInstructor.INSTR_LIST_ATTR_NAME, instructors);

        // First access to screen
        if(op.equalsIgnoreCase(MSG.actionAssignInstructors())) {
            doLoad(request, frm, instrOffrConfigId, ioc);
        }
        
		if(op.equals(MSG.actionUpdateClassInstructorsAssignment()) ||
        		op.equals(MSG.actionNextIO()) ||
        		op.equals(MSG.actionPreviousIO()) ||
        		op.equals(MSG.actionUnassignAllInstructorsFromConfig())) {

            if (op.equals(MSG.actionUnassignAllInstructorsFromConfig())) {
            	frm.unassignAllInstructors();
            }

        	// Validate input prefs
            ActionMessages errors = frm.validate(mapping, request);

            // No errors - Update class
            if(errors.size()==0) {

            	try {
            		frm.updateClasses();

                    InstrOfferingConfig cfg = new InstrOfferingConfigDAO().get(frm.getInstrOffrConfigId());

                    org.hibernate.Session hibSession = InstructionalOfferingDAO.getInstance().getSession();
                    ChangeLog.addChange(
                    		hibSession,
                            sessionContext,
                            cfg,
                            ChangeLog.Source.CLASS_INSTR_ASSIGN,
                            ChangeLog.Operation.UPDATE,
                            cfg.getInstructionalOffering().getControllingCourseOffering().getSubjectArea(),
                            null);
                    
                	if (permissionOfferingLockNeeded.check(sessionContext.getUser(), cfg.getInstructionalOffering())) {
                		StudentSectioningQueue.offeringChanged(hibSession, sessionContext.getUser(), cfg.getInstructionalOffering().getSessionId(), cfg.getInstructionalOffering().getUniqueId());
                	}
                	
                	hibSession.flush();

                	String className = ApplicationProperty.ExternalActionInstrOfferingConfigAssignInstructors.value();
                	if (className != null && className.trim().length() > 0){
        	        	ExternalInstrOfferingConfigAssignInstructorsAction assignAction = (ExternalInstrOfferingConfigAssignInstructorsAction) (Class.forName(className).newInstance());
        	       		assignAction.performExternalInstrOfferingConfigAssignInstructorsAction(ioc, InstrOfferingConfigDAO.getInstance().getSession());
                	}

    	            if (op.equals(MSG.actionNextIO())) {
    	            	response.sendRedirect(response.encodeURL("classInstructorAssignment.do?uid="+frm.getNextId()+"&op="+URLEncoder.encode(MSG.actionAssignInstructors(), "UTF-8")));
    	            	return null;
    	            }

    	            if (op.equals(MSG.actionPreviousIO())) {
    	            	response.sendRedirect(response.encodeURL("classInstructorAssignment.do?uid="+frm.getPreviousId()+"&op="+URLEncoder.encode(MSG.actionAssignInstructors(), "UTF-8")));
    	            	return null;
    	            }

                    ActionRedirect redirect = new ActionRedirect(mapping.findForward("instructionalOfferingDetail"));
                    redirect.addParameter("io", frm.getInstrOfferingId());
                    redirect.addParameter("op", "view");
                    return redirect;
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
            InstrOfferingConfig ioc) throws Exception {

        // Check uniqueid
        if(instrOffrConfigId==null || instrOffrConfigId.trim().length()==0)
            throw new Exception (MSG.exceptionMissingIOConfig());

        // Load details
        InstructionalOffering io = ioc.getInstructionalOffering();

        // Load form properties
        frm.setInstrOffrConfigId(ioc.getUniqueId());
        frm.setInstrOffrConfigLimit(ioc.getLimit());
        frm.setInstrOfferingId(io.getUniqueId());

        frm.setDisplayExternalId(ApplicationProperty.ClassSetupShowExternalIds.isTrue());

        String name = io.getCourseNameWithTitle();
        if (io.hasMultipleConfigurations()) {
        	name += " [" + ioc.getName() +"]";
        }
        frm.setInstrOfferingName(name);

        if (ioc.getSchedulingSubparts() == null || ioc.getSchedulingSubparts().size() == 0)
        	throw new Exception(MSG.exceptionIOConfigUndefined());

        InstrOfferingConfig config = ioc.getNextInstrOfferingConfig(sessionContext);
        if(config != null) {
        	frm.setNextId(config.getUniqueId().toString());
        } else {
        	frm.setNextId(null);
        }

        config = ioc.getPreviousInstrOfferingConfig(sessionContext);
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
    			throw new Exception(MSG.exceptionInitialIOSetupIncomplete());
    		if (ss.getParentSubpart() == null){
        		loadClasses(frm, ss.getClasses(), new String());
        	}
        }
    }

    private void loadClasses(ClassInstructorAssignmentForm frm, Set classes, String indent){
    	if (classes != null && classes.size() > 0){
    		ArrayList classesList = new ArrayList(classes);

    		if (CommonValues.Yes.eq(UserProperty.ClassesKeepSort.get(sessionContext.getUser()))) {
        		Collections.sort(classesList,
        			new ClassCourseComparator(
        					sessionContext.getUser().getProperty("InstructionalOfferingList.sortBy",ClassCourseComparator.getName(ClassCourseComparator.SortBy.NAME)),
        					frm.getProxy(),
        					false
        			)
        		);
        	} else {
        		Collections.sort(classesList, new ClassComparator(ClassComparator.COMPARE_BY_ITYPE) );
        	}

	    	Class_ cls = null;
	    	for(Iterator it = classesList.iterator(); it.hasNext();){
	    		cls = (Class_) it.next();
	    		frm.addToClasses(cls, !sessionContext.hasPermission(cls, Right.AssignInstructorsClass), indent);
	    		loadClasses(frm, cls.getChildClasses(), indent + "&nbsp;&nbsp;&nbsp;&nbsp;");
	    	}
    	}
    }
}
