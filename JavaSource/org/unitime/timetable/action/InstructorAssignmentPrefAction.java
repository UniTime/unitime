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

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.tiles.annotation.TilesDefinition;
import org.apache.struts2.tiles.annotation.TilesPutAttribute;
import org.hibernate.Transaction;
import org.springframework.stereotype.Service;
import org.unitime.commons.Debug;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.ApplicationProperty;
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
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.util.LookupTables;
import org.unitime.timetable.webutil.BackTracker;

/**
 * @author Tomas Muller
 */
@Service("/instructorAssignmentPref")
@Action(value = "instructorAssignmentPref", results = {
		@Result(name = "showEdit", type = "tiles", location = "instructorAssignmentPref.tiles"),
		@Result(name = "showDetail", type = "redirect", location = "/instructorDetail.action",
				params = { "instructorId", "${form.instructorId}", "showPrefs", "true"}),
		@Result(name = "showList", type = "redirect", location = "/instructorSearch.action")
	})
@TilesDefinition(name = "instructorAssignmentPref.tiles", extend = "baseLayout", putAttributes =  {
		@TilesPutAttribute(name = "title", value = "Instructor Assignment Preferences"),
		@TilesPutAttribute(name = "body", value = "/user/instructorAssignmentPref.jsp"),
		@TilesPutAttribute(name = "showNavigation", value = "true"),
		@TilesPutAttribute(name = "checkRole", value = "false")
	})

public class InstructorAssignmentPrefAction extends PreferencesAction2<InstructorEditForm> {
	private static final long serialVersionUID = 8795827984312568961L;
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);

	protected String instructorId = null;
	protected String op2 = null;
	protected String reloadCause = null;

	public String getInstructorId() { return instructorId; }
	public void setInstructorId(String instructorId) { this.instructorId = instructorId; }
	public String getOp2() { return op2; }
	public void setOp2(String op2) { this.op2 = op2; }
	public String getReloadCause() { return reloadCause; }
	public void setReloadCause(String reloadCause) { this.reloadCause = reloadCause; }

	public String execute() throws Exception {
		if (form == null) form = new InstructorEditForm();

		super.execute();

		//Read parameters
        if (instructorId == null && request.getAttribute("instructorId") != null)
        	instructorId = (String)request.getAttribute("instructorId");
        if (instructorId == null) instructorId = form.getInstructorId();

        if (op == null) op = form.getOp();
        if (op2 != null && !op2.isEmpty()) op = op2;
        
        // Determine if initial load
        if (op == null || op.trim().isEmpty() || ("Reload".equals(op) && (reloadCause == null || reloadCause.trim().isEmpty()))) {
        	op = "init";
        }
        
        // Check op exists
        if (op==null || op.trim().isEmpty()) 
            throw new Exception (MSG.exceptionNullOperationNotSupported());
        
        //Check instructor exists
        if (instructorId==null || instructorId.trim().isEmpty()) 
            throw new Exception (MSG.exceptionInstructorInfoNotSupplied());
		

        // Set screen name
        form.setScreenName("instructorPref");
        
        // If subpart id is not null - load subpart info
        DepartmentalInstructorDAO idao = new DepartmentalInstructorDAO();
        DepartmentalInstructor inst = idao.get(Long.valueOf(instructorId));
        LookupTables.setupInstructorDistribTypes(request, sessionContext, inst);
        
        // Check permissions
        sessionContext.checkPermission(inst.getDepartment(), Right.InstructorAssignmentPreferences);
        
        // Cancel - Go back to Instructors Detail Screen
        if (MSG.actionBackToDetail().equals(op)) {
        	if (BackTracker.hasBack(request, 1)) {
                BackTracker.doBack(request, response);
                return null;
            }
        	if (instructorId != null && !instructorId.trim().isEmpty()) {
                return "showDetail";
            } else {
            	return "showList";
            }
        }

        // Reset form for initial load
        if ("init".equals(op)) { 
            form.reset();

            // Load form attributes
            doLoad(inst, instructorId);
        }
        
        // Update Preferences for InstructorDept
        if (MSG.actionUpdatePreferences().equals(op) || MSG.actionNextInstructor().equals(op) || MSG.actionPreviousInstructor().equals(op)) {	
        	// Validate input prefs
            form.validate(this);
            
            // No errors - Add to instructorDept and update
            if (!hasFieldErrors()) {
            	doUpdate();

            	if (MSG.actionNextInstructor().equals(op)) {
	            	response.sendRedirect(response.encodeURL("instructorAssignmentPref.action?instructorId="+form.getNextId()));
	            	return null;
	        	}
	            
	            if (MSG.actionPreviousInstructor().equals(op)) {
	            	response.sendRedirect(response.encodeURL("instructorAssignmentPref.action?instructorId="+form.getPreviousId()));
	            	return null;
	            }

	            return "showDetail";
            }
        }
        
        // Initialize Preferences for initial load 
        Set timePatterns = new HashSet();
        form.setAvailableTimePatterns(null);
        if ("init".equals(op)) {
        	initPrefs(inst, null, true);
        	timePatterns.add(new TimePattern(Long.valueOf(-1)));
        }
        
		// Process Preferences Action
		processPrefAction();
		
		// Generate Time Pattern Grids
		for (Preference pref: inst.getPreferences()) {
			if (pref instanceof TimePref) {
				form.setAvailability(((TimePref)pref).getPreference());
				break;
			}
		}

        LookupTables.setupCourses(request, inst); // Courses
        LookupTables.setupInstructorAttributeTypes(request, inst);
        LookupTables.setupInstructorAttributes(request, inst);
	
        BackTracker.markForBack(
        		request,
        		"instructorDetail.action?instructorId="+form.getInstructorId(),
        		MSG.backInstructor(form.getName()==null?"null":form.getName().trim()),
        		true, false);

        return "showEdit";
	}

	
	/**
	 * Loads the non-editable instructor info into the form
	 */
	private void doLoad(DepartmentalInstructor inst, String instructorId) {
        // populate form
		form.setInstructorId(instructorId);
		
		if ("Enabled".equalsIgnoreCase(ApplicationProperty.InstructorUnavailbeDays.value()) || "Assignments".equalsIgnoreCase(ApplicationProperty.InstructorUnavailbeDays.value()))
			request.setAttribute("unavailableDaysPattern", inst.getUnavailablePatternHtml(true));

		form.setName(
				inst.getName(UserProperty.NameFormat.get(sessionContext.getUser())) + 
    			(inst.getPositionType() == null ? "" : " (" + inst.getPositionType().getLabel() + ")"));
		
        form.setMaxLoad(inst.getMaxLoad() == null ? null : Formats.getNumberFormat("0.##").format(inst.getMaxLoad()));
        form.setTeachingPreference(inst.getTeachingPreference() == null ? PreferenceLevel.sProhibited : inst.getTeachingPreference().getPrefProlog());
        form.clearAttributes();
        for (InstructorAttribute attribute: inst.getAttributes())
        	form.setAttribute(attribute.getUniqueId(), true);
		
		try {
			DepartmentalInstructor previous = inst.getPreviousDepartmentalInstructor(sessionContext, Right.InstructorAssignmentPreferences);
			form.setPreviousId(previous==null?null:previous.getUniqueId().toString());
			DepartmentalInstructor next = inst.getNextDepartmentalInstructor(sessionContext, Right.InstructorAssignmentPreferences);
			form.setNextId(next==null?null:next.getUniqueId().toString());
		} catch (Exception e) {
			Debug.error(e);
		}
	}
	
	protected void doUpdate() throws Exception {
	    
		DepartmentalInstructorDAO idao = new DepartmentalInstructorDAO();
		org.hibernate.Session hibSession = idao.getSession();
		Transaction tx = null;
		
		try {	
			tx = hibSession.beginTransaction();
			
			DepartmentalInstructor inst = idao.get(Long.valueOf(form.getInstructorId()), hibSession);

			if (form.getMaxLoad() != null && !form.getMaxLoad().isEmpty()) {
				try {
					inst.setMaxLoad(Formats.getNumberFormat("0.##").parse(form.getMaxLoad()).floatValue());
				} catch (ParseException e) {}
			} else {
				inst.setMaxLoad(null);
			}
			
			if (form.getTeachingPreference() != null && !form.getTeachingPreference().isEmpty() && !PreferenceLevel.sProhibited.equals(form.getTeachingPreference())) {
				inst.setTeachingPreference(PreferenceLevel.getPreferenceLevel(form.getTeachingPreference()));
			} else {
				inst.setTeachingPreference(null);
			}
            
			for (InstructorAttribute attribute: inst.getDepartment().getAvailableAttributes()) {
				if (form.getAttribute(attribute.getUniqueId())) {
					if (!inst.getAttributes().contains(attribute))
						inst.getAttributes().add(attribute);
				} else {
					if (inst.getAttributes().contains(attribute))
						inst.getAttributes().remove(attribute);
				}
			}
			
    		super.doUpdate(inst, inst.getPreferences(), false, Preference.Type.TIME, Preference.Type.DISTRIBUTION, Preference.Type.COURSE);
    		
    		if ("Enabled".equalsIgnoreCase(ApplicationProperty.InstructorUnavailbeDays.value()) || "Assignments".equalsIgnoreCase(ApplicationProperty.InstructorUnavailbeDays.value()))
    			inst.setUnavailablePatternAndOffset(request);
			
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