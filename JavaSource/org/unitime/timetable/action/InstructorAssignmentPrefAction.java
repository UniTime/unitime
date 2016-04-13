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
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.util.MessageResources;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.commons.Debug;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.form.InstructorEditForm;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.InstructorAttribute;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.TimePref;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.util.LookupTables;
import org.unitime.timetable.webutil.BackTracker;

/**
 * @author Tomas Muller
 */
@Service("/instructorAssignmentPref")
public class InstructorAssignmentPrefAction extends PreferencesAction {

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
		
    	try {
            // Set common lookup tables
            super.execute(mapping, form, request, response);
            
    		InstructorEditForm frm = (InstructorEditForm) form;       
            MessageResources rsc = getResources(request);
    		ActionMessages errors = new ActionMessages();
            
            // Read parameters
            String instructorId = request.getParameter("instructorId");
            String op = frm.getOp();            
            String reloadCause = request.getParameter("reloadCause");
            
            // Read subpart id from form
            if(op.equals(rsc.getMessage("button.reload"))
            		|| op.equals(MSG.actionAddTimePreference())
                    || op.equals(MSG.actionAddDistributionPreference()) 
                    || op.equals(MSG.actionAddCoursePreference())
                    || op.equals(MSG.actionUpdatePreferences()) 
                    || op.equals(MSG.actionBackToDetail())
                    || op.equals(MSG.actionNextInstructor())
                    || op.equals(MSG.actionPreviousInstructor())) {
            	instructorId = frm.getInstructorId();
            }
            
            // Determine if initial load
            if (op==null || op.trim().isEmpty()  || (op.equals(rsc.getMessage("button.reload")) && (reloadCause==null || reloadCause.trim().isEmpty()))) {     
                op = "init";
            }
            
            // Check op exists
            if (op==null || op.trim().isEmpty()) 
                throw new Exception(MSG.exceptionNullOperationNotSupported());
            
            //Check instructor exists
            if (instructorId==null || instructorId.isEmpty()) 
                throw new Exception (MSG.exceptionInstructorInfoNotSupplied());
            
            sessionContext.checkPermission(instructorId, "DepartmentalInstructor", Right.InstructorAssignmentPreferences);
            
            // Set screen name
            frm.setScreenName("instructorPref");
            
            // If subpart id is not null - load subpart info
            DepartmentalInstructorDAO idao = new DepartmentalInstructorDAO();
            DepartmentalInstructor inst = idao.get(new Long(instructorId));
            LookupTables.setupInstructorDistribTypes(request, sessionContext, inst);
            
            // Cancel - Go back to Instructors Detail Screen
            if (op.equals(MSG.actionBackToDetail())  && instructorId != null && !instructorId.trim().isEmpty()) {
	            ActionRedirect redirect = new ActionRedirect(mapping.findForward("showDetail"));
	            redirect.addParameter("instructorId", frm.getInstructorId());
	            return redirect;
            }
            
            if (op.equals("init")) { 
                // Reset form for initial load
                frm.reset(mapping, request);

                // Load form attributes
                doLoad(request, frm, inst, instructorId);
            }
            
            // Update Preferences for InstructorDept
            if (op.equals(MSG.actionUpdatePreferences()) || op.equals(MSG.actionNextInstructor()) || op.equals(MSG.actionPreviousInstructor())) {	

            	// Validate input prefs
                errors = frm.validate(mapping, request);
                
                // No errors - Add to instructorDept and update
                if (errors.isEmpty()) {
                	doUpdate(frm, request);

                	if (op.equals(MSG.actionNextInstructor())) {
    	            	response.sendRedirect(response.encodeURL("instructorAssignmentPref.do?instructorId="+frm.getNextId()));
    	            	return null;
    	        	}
    	            
    	            if (op.equals(MSG.actionPreviousInstructor())) {
    	            	response.sendRedirect(response.encodeURL("instructorAssignmentPref.do?instructorId="+frm.getPreviousId()));
    	            	return null;
    	            }
                    
    	            ActionRedirect redirect = new ActionRedirect(mapping.findForward("showDetail"));
    	            redirect.addParameter("instructorId", frm.getInstructorId());
    	            redirect.addParameter("showPrefs", "true");
    	            return redirect;
                } else {
                    saveErrors(request, errors);
                }
            }
            
	        // Initialize Preferences for initial load 
            Set timePatterns = new HashSet();
            frm.setAvailableTimePatterns(null);
            if(op.equals("init")) {
            	initPrefs(frm, inst, null, true);
            	timePatterns.add(new TimePattern(new Long(-1)));
            }
            
    		// Process Preferences Action
    		processPrefAction(request, frm, errors);
    		
    		// Generate Time Pattern Grids
    		for (Preference pref: inst.getPreferences()) {
				if (pref instanceof TimePref) {
					frm.setAvailability(((TimePref)pref).getPreference());
					break;
				}
			}

            LookupTables.setupCourses(request, inst); // Courses
            LookupTables.setupInstructorAttributeTypes(request, inst);
            LookupTables.setupInstructorAttributes(request, inst);
		
            BackTracker.markForBack(
            		request,
            		"instructorDetail.do?instructorId="+frm.getInstructorId(),
            		MSG.backInstructor(frm.getName()==null?"null":frm.getName().trim()),
            		true, false);

            return mapping.findForward("showEdit");
    	} catch (Exception e) {
    		Debug.error(e);
    		throw e;
    	}
	}

	
	/**
	 * Loads the non-editable instructor info into the form
	 * @param request
	 * @param frm
	 * @param inst
	 * @param instructorId
	 */
	private void doLoad(HttpServletRequest request, InstructorEditForm frm, DepartmentalInstructor inst, String instructorId) {
        // populate form
		frm.setInstructorId(instructorId);		

		frm.setName(
				inst.getName(UserProperty.NameFormat.get(sessionContext.getUser())) + 
    			(inst.getPositionType() == null ? "" : " (" + inst.getPositionType().getLabel() + ")"));
		
        frm.setMaxLoad(inst.getMaxLoad() == null ? null : Formats.getNumberFormat("0.##").format(inst.getMaxLoad()));
        frm.setTeachingPreference(inst.getTeachingPreference() == null ? PreferenceLevel.sProhibited : inst.getTeachingPreference().getPrefProlog());
        frm.clearAttributes();
        for (InstructorAttribute attribute: inst.getAttributes())
        	frm.setAttribute(attribute.getUniqueId(), true);
		
		try {
			DepartmentalInstructor previous = inst.getPreviousDepartmentalInstructor(sessionContext, Right.InstructorAssignmentPreferences);
			frm.setPreviousId(previous==null?null:previous.getUniqueId().toString());
			DepartmentalInstructor next = inst.getNextDepartmentalInstructor(sessionContext, Right.InstructorAssignmentPreferences);
			frm.setNextId(next==null?null:next.getUniqueId().toString());
		} catch (Exception e) {
			Debug.error(e);
		}
	}
	
	protected void doUpdate(InstructorEditForm frm, HttpServletRequest request) throws Exception {
	    
		DepartmentalInstructorDAO idao = new DepartmentalInstructorDAO();
		org.hibernate.Session hibSession = idao.getSession();
		Transaction tx = null;
		
		try {	
			tx = hibSession.beginTransaction();
			
			DepartmentalInstructor inst = idao.get(new Long(frm.getInstructorId()), hibSession);

			if (frm.getMaxLoad() != null && !frm.getMaxLoad().isEmpty()) {
				try {
					inst.setMaxLoad(Formats.getNumberFormat("0.##").parse(frm.getMaxLoad()).floatValue());
				} catch (ParseException e) {}
			} else {
				inst.setMaxLoad(null);
			}
			
			if (frm.getTeachingPreference() != null && !frm.getTeachingPreference().isEmpty() && !PreferenceLevel.sProhibited.equals(frm.getTeachingPreference())) {
				inst.setTeachingPreference(PreferenceLevel.getPreferenceLevel(frm.getTeachingPreference()));
			} else {
				inst.setTeachingPreference(null);
			}
            
			for (InstructorAttribute attribute: inst.getDepartment().getAvailableAttributes()) {
				if (frm.getAttribute(attribute.getUniqueId())) {
					if (!inst.getAttributes().contains(attribute))
						inst.getAttributes().add(attribute);
				} else {
					if (inst.getAttributes().contains(attribute))
						inst.getAttributes().remove(attribute);
				}
			}
			
    		super.doUpdate(request, frm, inst, inst.getPreferences(), false, Preference.Type.TIME, Preference.Type.DISTRIBUTION, Preference.Type.COURSE);
			
			hibSession.saveOrUpdate(inst);

            ChangeLog.addChange(
                    hibSession, 
                    sessionContext, 
                    inst, 
                    ChangeLog.Source.INSTRUCTOR_ASSIGNMENT_PREF_EDIT, 
                    ChangeLog.Operation.UPDATE, 
                    null, 
                    inst.getDepartment());

            tx.commit();			
		} catch (Exception e) {
            Debug.error(e);
            try {
	            if (tx != null && tx.isActive()) tx.rollback();
            } catch (Exception e1) {}
            throw e;
        }
		
	}

}