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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.form.ClassEditForm;
import org.unitime.timetable.interfaces.ExternalClassEditAction;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.StudentAccomodation;
import org.unitime.timetable.model.comparators.InstructorComparator;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.ClassAssignmentProxy;
import org.unitime.timetable.solver.interactive.ClassAssignmentDetails;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.util.LookupTables;
import org.unitime.timetable.webutil.BackTracker;


/**
 * @author Tomas Muller
 */
@Service("/classInstrAssgnEdit")
public class ClassInstrAssgnEditAction extends PreferencesAction {
	
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);

	@Autowired SessionContext sessionContext;
	
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
    	try {
    		super.execute(mapping, form, request, response);
    		
    		ClassEditForm frm = (ClassEditForm) form;
    		ActionMessages errors = new ActionMessages();
    		
    		// Read parameters
    		String classId = (request.getParameter("cid") == null ? request.getAttribute("cid") != null ? request.getAttribute("cid").toString() : null : request.getParameter("cid"));
    		
    		String op = frm.getOp();
    		if (request.getParameter("op2") != null && !request.getParameter("op2").isEmpty())
    			op = request.getParameter("op2");
    		
    		// Read class id from form
    		if (op.equals(MSG.actionUpdatePreferences())
                || op.equals(MSG.actionAddAttributePreference())
                || op.equals(MSG.actionAddInstructorPreference())
                || op.equals(MSG.actionClearClassPreferences())
        		|| op.equals(MSG.actionRemoveAttributePreference())
        		|| op.equals(MSG.actionRemoveInstructorPreference())
                || op.equals(MSG.actionBackToDetail())
                || op.equals(MSG.actionNextClass())
                || op.equals(MSG.actionPreviousClass())
                || op.equals(MSG.actionAddInstructor())
                || op.equals(MSG.actionRemoveInstructor())
                || op.equals("updateInstructorAssignment")) {
    			classId = frm.getClassId().toString();
    		}
    		
    		// Determine if initial load
    		if (op == null || op.trim().isEmpty()) op = "init";
    		
    		// Check op exists
    		if (op == null || op.trim().isEmpty())
    			throw new Exception (MSG.errorNullOperationNotSupported());
    		
    		// Check class exists
    		if (classId == null || classId.trim().isEmpty()) {
    			if (BackTracker.doBack(request, response))
    				return null;
    			else
    				throw new Exception (MSG.errorClassInfoNotSupplied());
    		}
    		
            // If class id is not null - load class info
            Class_DAO cdao = new Class_DAO();
            Class_ c = cdao.get(new Long(classId));

    		sessionContext.checkPermission(c.getControllingDept(), Right.InstructorAssignmentPreferences);
    		
    		// Cancel - Go back to Class Detail Screen
    		if (op.equals(MSG.actionBackToDetail())) {
    			ActionRedirect redirect = new ActionRedirect(mapping.findForward("displayClassDetail"));
    			redirect.addParameter("cid", classId);
    			return redirect;
    		}
    		
    	    // Add Instructor
    	    if (op.equals(MSG.actionAddInstructor())) {
    	        List lst = frm.getInstructors();
    	        if (frm.checkPrefs(lst)) {
    	            frm.addToInstructors(null);
    	            request.setAttribute(HASH_ATTR, "Instructors");
    	        } else {
    	        	errors.add("instrPrefs", new ActionMessage("errors.generic", MSG.errorInvalidInstructors()));
    	        	saveErrors(request, errors);
    	        }
    	    }

    	    // Delete Instructor
            if (op.equals(MSG.actionRemoveInstructor()) && "instructor".equals(request.getParameter("deleteType"))) {
                try {
                	frm.removeInstructor(Integer.parseInt(request.getParameter("deleteId")));
                	request.setAttribute(HASH_ATTR, "Instructors");
                } catch (Exception e) {}
            }
            
    		// Restore all inherited preferences
    		if (op.equals(MSG.actionClearClassPreferences())) {
    			sessionContext.checkPermission(c.getControllingDept(), Right.InstructorAssignmentPreferences);
    			doClear(c.getPreferences(), Preference.Type.ATTRIBUTE, Preference.Type.INSTRUCTOR);
    			cdao.update(c);
    			op = "init";
    			
    			ChangeLog.addChange(
                    null,
                    sessionContext,
                    c,
                    ChangeLog.Source.CLASS_EDIT,
                    ChangeLog.Operation.CLEAR_PREF,
                    c.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getControllingCourseOffering().getSubjectArea(),
                    c.getManagingDept());
    			
    			ActionRedirect redirect = new ActionRedirect(mapping.findForward("displayClassDetail"));
    			redirect.addParameter("cid", classId);
    			return redirect;
    		}
    		
    		// Reset form for initial load
    		if (op.equals("init")) {
    			frm.reset(mapping, request);
    		}
    		
    		// Create assignment information
			if (sessionContext.hasPermission(Right.ClassAssignments)) {
				ClassAssignmentDetails ca = ClassAssignmentDetails.createClassAssignmentDetails(sessionContext, courseTimetablingSolverService.getSolver(), c.getUniqueId(), true);
				if (ca != null) {
					String assignmentTable = SuggestionsAction.getAssignmentTable(sessionContext, courseTimetablingSolverService.getSolver(), ca,false, null, true);
					if (assignmentTable!=null)
						request.setAttribute("Suggestions.assignmentInfo", assignmentTable);
				} else {
					ClassAssignmentProxy cap = classAssignmentService.getAssignment();
					if (cap != null) {
						Assignment assignment = cap.getAssignment(c);
						if (assignment!=null && assignment.getUniqueId()!=null) {
							ca = ClassAssignmentDetails.createClassAssignmentDetailsFromAssignment(sessionContext, assignment.getUniqueId(), true);
							if (ca!=null) {
								String assignmentTable = SuggestionsAction.getAssignmentTable(sessionContext, courseTimetablingSolverService.getSolver(), ca,false, null, true);
								if (assignmentTable!=null)
									request.setAttribute("Suggestions.assignmentInfo", assignmentTable);
							}
						}
					}
				}
			}
    		
    		// Load form attributes that are constant
    		doLoad(request, frm, c, op);
    		
    		// Update Preferences for Class
    		if (op.equals(MSG.actionUpdatePreferences()) || op.equals(MSG.actionNextClass()) || op.equals(MSG.actionPreviousClass())) {
    			// Validate input prefs
    			errors = frm.validate(mapping, request);
    			
    			// No errors - Add to class and update
    			if (errors.isEmpty()) {
    				org.hibernate.Session hibSession = cdao.getSession();
    				Transaction tx = hibSession.beginTransaction();
    				
    				try {
    					// Clear all old prefs
    					doClear(c.getPreferences(), Preference.Type.ATTRIBUTE, Preference.Type.INSTRUCTOR);

    					// Save class data
    					doUpdate(request, frm, c, hibSession);

    					// Save Prefs
    					doUpdate(request, frm, c, c.getPreferences(), false, Preference.Type.ATTRIBUTE, Preference.Type.INSTRUCTOR);
    					
    					hibSession.saveOrUpdate(c);
    					tx.commit();
    					
    					String className = ApplicationProperty.ExternalActionClassEdit.value();
    					if (className != null && className.trim().length() > 0){
    						ExternalClassEditAction editAction = (ExternalClassEditAction) (Class.forName(className).newInstance());
    						editAction.performExternalClassEditAction(c, hibSession);
    					}
    					if (op.equals(MSG.actionNextClass())) {
    						response.sendRedirect(response.encodeURL("classInstrAssgnEdit.do?cid="+frm.getNextId()));
    						return null;
    					}
    					if (op.equals(MSG.actionPreviousClass())) {
    						response.sendRedirect(response.encodeURL("classInstrAssgnEdit.do?cid="+frm.getPreviousId()));
    						return null;
    					}
    					
    					ActionRedirect redirect = new ActionRedirect(mapping.findForward("displayClassDetail"));
    					redirect.addParameter("cid", classId);
    					return redirect;
    				} catch (Exception e) {
    					if (tx != null && tx.isActive()) tx.rollback();
    					throw e;
    				}
    			} else {
        			saveErrors(request, errors);
    			}
    		}

            if (op.equals("updateInstructorAssignment")) {
            	initPrefs(frm, c, null, true);
            }
            
            // Initialize Preferences for initial load
            if (op.equals("init")) {
            	initPrefs(frm, c, null, true);
            }
            
            // Process Preferences Action
            processPrefAction(request, frm, errors);
            
            LookupTables.setupDatePatterns(request, sessionContext.getUser(), "Default", c.getSchedulingSubpart().effectiveDatePattern(), c.getManagingDept(), c.effectiveDatePattern());
            LookupTables.setupInstructors(request, sessionContext, c.getDepartmentForSubjectArea().getUniqueId()); // Instructors
            LookupTables.setupInstructorAttributes(request, c);   // Instructor Attributes
            
            frm.setAllowHardPrefs(sessionContext.hasPermission(c, Right.CanUseHardRoomPrefs));
            
            BackTracker.markForBack(
        		request,
        		"classDetail.do?cid="+frm.getClassId(),
        		MSG.backClass(frm.getClassName()),
        		true, false);
            
            return mapping.findForward("editClass");
    	} catch (Exception e) {
    		Debug.error(e);
    		throw e;
    	}
    }

    private void doLoad(HttpServletRequest request, ClassEditForm frm, Class_ c, String op) {
        Department managingDept = c.getManagingDept();
        String parentClassName = "-";
        Long parentClassId = null;
        if (c.getParentClass()!=null) {
            parentClassName = c.getParentClass().toString();
            parentClassId = c.getParentClass().getUniqueId();
        }

        CourseOffering cco = c.getSchedulingSubpart().getControllingCourseOffering();

        // populate form
        frm.setClassId(c.getUniqueId());
        frm.setSection(c.getSectionNumberString());
        frm.setClassName(c.getClassLabel());

        SchedulingSubpart ss = c.getSchedulingSubpart();
    	String itypeDesc = c.getItypeDesc();
    	if (ss.getInstrOfferingConfig().getInstructionalOffering().hasMultipleConfigurations())
    		itypeDesc += " [" + ss.getInstrOfferingConfig().getName() + "]";
        frm.setItypeDesc(itypeDesc);

        frm.setParentClassName(parentClassName);
        frm.setParentClassId(parentClassId);
        frm.setSubjectAreaId(cco.getSubjectArea().getUniqueId().toString());
        frm.setInstrOfferingId(cco.getInstructionalOffering().getUniqueId().toString());
        frm.setSubpart(c.getSchedulingSubpart().getUniqueId());
        frm.setCourseName(cco.getInstructionalOffering().getCourseName());
        frm.setCourseTitle(cco.getTitle());
        frm.setManagingDept(managingDept.getUniqueId());
        frm.setControllingDept(c.getControllingDept().getUniqueId());
        frm.setManagingDeptLabel(managingDept.getManagingDeptLabel());
        frm.setUnlimitedEnroll(c.getSchedulingSubpart().getInstrOfferingConfig().isUnlimitedEnrollment());
        frm.setAccommodation(StudentAccomodation.toHtml(StudentAccomodation.getAccommodations(c)));
        frm.setInstructorAssignmentDefault(c.getSchedulingSubpart().isInstructorAssignmentNeeded());
        frm.setTeachingLoadDefault(c.getSchedulingSubpart().getTeachingLoad() == null ? "" : Formats.getNumberFormat("0.##").format(c.getSchedulingSubpart().getTeachingLoad()));
        frm.setNbrInstructorsDefault(c.getSchedulingSubpart().isInstructorAssignmentNeeded() ? c.getSchedulingSubpart().getNbrInstructors() : 1);
        
        Class_ next = c.getNextClass(sessionContext, Right.InstructorAssignmentPreferences);
        frm.setNextId(next==null?null:next.getUniqueId().toString());
        Class_ previous = c.getPreviousClass(sessionContext, Right.InstructorAssignmentPreferences);
        frm.setPreviousId(previous==null?null:previous.getUniqueId().toString());
        frm.setMinRoomLimit(c.getMinRoomLimit());
        frm.setEnrollment(c.getEnrollment());

        frm.setExpectedCapacity(c.getExpectedCapacity());
        frm.setDatePattern(c.getDatePattern()==null?new Long(-1):c.getDatePattern().getUniqueId());
        frm.setNbrRooms(c.getNbrRooms());
        frm.setNotes(c.getNotes());
        frm.setManagingDept(c.getManagingDept().getUniqueId());
	    frm.setSchedulePrintNote(c.getSchedulePrintNote());
	    frm.setClassSuffix(c.getDivSecNumber());
	    frm.setMaxExpectedCapacity(c.getMaxExpectedCapacity());
	    frm.setRoomRatio(c.getRoomRatio());
	    frm.setEnabledForStudentScheduling(c.isEnabledForStudentScheduling());
	    
        // Load from class only for initial load or reload
        if ("init".equals(op)) {
	        frm.setInstructorAssignment(c.isInstructorAssignmentNeeded());
	        frm.setTeachingLoad(c.getTeachingLoad() == null ? "" : Formats.getNumberFormat("0.##").format(c.getTeachingLoad()));
	        frm.setNbrInstructors(c.isInstructorAssignmentNeeded() && c.getNbrInstructors() != null? c.getNbrInstructors().toString() : "");
	        
		    frm.setDisplayInstructor(c.isDisplayInstructor());
        }
        
        if ("init".equals(op) || !sessionContext.hasPermission(c, Right.AssignInstructorsClass)) {
		    List instructors = new ArrayList(c.getClassInstructors());
		    InstructorComparator ic = new InstructorComparator();
		    ic.setCompareBy(ic.COMPARE_BY_LEAD);
		    Collections.sort(instructors, ic);

	        for(Iterator iter = instructors.iterator(); iter.hasNext(); ) {
	            ClassInstructor classInstr = (ClassInstructor) iter.next();
	            frm.addToInstructors(classInstr);
	        }
        }
    }

    private void doUpdate(HttpServletRequest request, ClassEditForm frm, Class_ c, org.hibernate.Session hibSession) throws Exception {
	    if (frm.getInstructorAssignment() != null && frm.getInstructorAssignment().booleanValue()) {
	        try {
	        	if (frm.getInstructorAssignment() && frm.getTeachingLoad() != null)
	        		c.setTeachingLoad(Formats.getNumberFormat("0.##").parse(frm.getTeachingLoad()).floatValue());
	        	else
	        		c.setTeachingLoad(null);
	        } catch (ParseException e) {
	        	c.setTeachingLoad(null);
	        }
	        try {
	        	if (frm.getInstructorAssignment() && frm.getNbrInstructors() != null)
	        		c.setNbrInstructors(Integer.parseInt(frm.getNbrInstructors()));
	        	else
	        		c.setNbrInstructors(null);
	        } catch (NumberFormatException e) {
	        	c.setNbrInstructors(null);
	        }
	    } else {
	    	c.setTeachingLoad(null);
	    	c.setNbrInstructors(c.getSchedulingSubpart().getNbrInstructors() != null && c.getSchedulingSubpart().getNbrInstructors() > 0 ? new Integer(0) : null);
	    }
	    
	    Boolean di = frm.getDisplayInstructor();
	    c.setDisplayInstructor(di==null ? new Boolean(false) : di);
	    
	    if (sessionContext.hasPermission(c, Right.AssignInstructorsClass)) {
	        // Class all instructors
	        Set classInstrs = c.getClassInstructors();
	        for (Iterator iter=classInstrs.iterator(); iter.hasNext() ;) {
	            ClassInstructor ci = (ClassInstructor) iter.next();
	            DepartmentalInstructor instr = ci.getInstructor();
	            instr.getClasses().remove(ci);
	            hibSession.saveOrUpdate(instr);
	            hibSession.delete(ci);
	        }

	        classInstrs.clear();

	        // Get instructor data
	        List instrLead = frm.getInstrLead();
	        List instructors = frm.getInstructors();
	        List instrPctShare = frm.getInstrPctShare();

	        // Save instructor data to class
	        for(int i=0; i<instructors.size(); i++) {

	            String instrId = instructors.get(i).toString();
	            if (Preference.BLANK_PREF_VALUE.equals(instrId)) continue;
	            String pctShare = instrPctShare.get(i).toString();
	            boolean lead = "on".equals(instrLead.get(i));

	            DepartmentalInstructor deptInstr = new DepartmentalInstructorDAO().get(new Long(instrId));

	            ClassInstructor classInstr = new ClassInstructor();
	            classInstr.setClassInstructing(c);
	            classInstr.setInstructor(deptInstr);
	            classInstr.setLead(new Boolean(lead));
	            classInstr.setTentative(false);
	            try {
	            	classInstr.setPercentShare(new Integer(pctShare));
	            } catch (NumberFormatException e) {
	            	classInstr.setPercentShare(new Integer(0));
	            }

	            classInstrs.add(classInstr);

	            deptInstr.getClasses().add(classInstr);
	            hibSession.saveOrUpdate(deptInstr);
	        }

	        c.setClassInstructors(classInstrs);
	    }

        ChangeLog.addChange(
                hibSession,
                sessionContext,
                c,
                ChangeLog.Source.CLASS_EDIT,
                ChangeLog.Operation.UPDATE,
                c.getSchedulingSubpart().getInstrOfferingConfig().getControllingCourseOffering().getSubjectArea(),
                c.getManagingDept());
    }
}
