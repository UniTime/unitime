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
import java.util.Set;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.tiles.annotation.TilesDefinition;
import org.apache.struts2.tiles.annotation.TilesPutAttribute;
import org.unitime.commons.Debug;
import org.unitime.commons.web.WebTable;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.form.InstructorEditForm;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.InstructorCoursePref;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.TimePref;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.LookupTables;
import org.unitime.timetable.webutil.BackTracker;


/** 
 * @author Tomas Muller, Zuzana Mullerova
 */
@Action(value = "instructorPrefEdit", results = {
		@Result(name = "showEdit", type = "tiles", location = "instructorPrefEdit.tiles"),
		@Result(name = "showDetail", type = "redirect", location = "/instructorDetail.action",
				params = { "instructorId", "${form.instructorId}"}),
		@Result(name = "showList", type = "redirect", location = "/instructorSearch.action")
	})
@TilesDefinition(name = "instructorPrefEdit.tiles", extend = "baseLayout", putAttributes =  {
		@TilesPutAttribute(name = "title", value = "Instructor Preferences"),
		@TilesPutAttribute(name = "body", value = "/user/instructorPrefsEdit.jsp"),
		@TilesPutAttribute(name = "showNavigation", value = "true"),
		@TilesPutAttribute(name = "checkRole", value = "false")
	})
public class InstructorPrefEditAction extends PreferencesAction2<InstructorEditForm> {
	private static final long serialVersionUID = 5756382132156450413L;
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	protected String instructorId = null;
	protected String op2 = null;

	public String getInstructorId() { return instructorId; }
	public void setInstructorId(String instructorId) { this.instructorId = instructorId; }
	public String getOp2() { return op2; }
	public void setOp2(String op2) { this.op2 = op2; }

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
        if (op == null || op.trim().isEmpty()) {
        	op = "init";
        }
        
        // Check op exists
        if (op==null || op.trim().isEmpty()) 
            throw new Exception (MSG.exceptionNullOperationNotSupported());
        
        //Check instructor exists
        if (instructorId==null || instructorId.trim().isEmpty()) 
            throw new Exception (MSG.exceptionInstructorInfoNotSupplied());
        
        sessionContext.checkPermission(instructorId, "DepartmentalInstructor", Right.InstructorPreferences);
        
        boolean timeVertical = CommonValues.VerticalGrid.eq(sessionContext.getUser().getProperty(UserProperty.GridOrientation));
        
        // Set screen name
        form.setScreenName("instructorPref");
        
        // If subpart id is not null - load subpart info
        DepartmentalInstructorDAO idao = DepartmentalInstructorDAO.getInstance();
        DepartmentalInstructor inst = idao.get(Long.valueOf(instructorId));
        LookupTables.setupInstructorDistribTypes(request, sessionContext, inst);
        
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
        
        // Clear all preferences
        if (MSG.actionClearInstructorPreferences().equals(op)) {
        	doClear(inst.getPreferences(), Preference.Type.TIME, Preference.Type.ROOM, Preference.Type.ROOM_FEATURE, Preference.Type.ROOM_GROUP, Preference.Type.BUILDING, Preference.Type.DISTRIBUTION);
            idao.update(inst);
            op = "init";            	
            
            ChangeLog.addChange(
                    null, 
                    sessionContext,
                    inst, 
                    ChangeLog.Source.INSTRUCTOR_PREF_EDIT, 
                    ChangeLog.Operation.CLEAR_PREF, 
                    null, 
                    inst.getDepartment());
            
            return "showDetail";
        }
        
        // Reset form for initial load
        if ("init".equals(op)) { 
            form.reset();
        }
        
        // Load form attributes that are constant
        doLoad(inst, instructorId);
        
        // Update Preferences for InstructorDept
        if(MSG.actionUpdatePreferences().equals(op) 
        		|| MSG.actionNextInstructor().equals(op) 
        		|| MSG.actionPreviousInstructor().equals(op)) {	

        	// Validate input prefs
            form.validate(this);
            
            // No errors - Add to instructorDept and update
            if (!hasFieldErrors()) {
                Set s = inst.getPreferences();
         
                // Clear all old prefs (excluding course preferences)
                for (Iterator i = s.iterator(); i.hasNext(); ) {
                	Preference p = (Preference)i.next();
                	if (p instanceof InstructorCoursePref) continue;
                	i.remove();
                }                
                
        		super.doUpdate(inst, s, timeVertical,
        				Preference.Type.TIME, Preference.Type.ROOM, Preference.Type.ROOM_FEATURE, Preference.Type.ROOM_GROUP, Preference.Type.BUILDING, Preference.Type.DISTRIBUTION);
        		
        		if ("Enabled".equalsIgnoreCase(ApplicationProperty.InstructorUnavailbeDays.value()) || "Preferences".equalsIgnoreCase(ApplicationProperty.InstructorUnavailbeDays.value()))
        			inst.setUnavailablePatternAndOffset(request);
                
                ChangeLog.addChange(
                        null, 
                        sessionContext,
                        inst, 
                        ChangeLog.Source.INSTRUCTOR_PREF_EDIT, 
                        ChangeLog.Operation.UPDATE, 
                        null, 
                        inst.getDepartment());

        		idao.saveOrUpdate(inst);
                
	        	if (MSG.actionNextInstructor().equals(op)) {
	            	response.sendRedirect(response.encodeURL("instructorPrefEdit.action?instructorId="+form.getNextId()));
	            	return null;
	        	}
	            
	            if (MSG.actionPreviousInstructor().equals(op)) {
	            	response.sendRedirect(response.encodeURL("instructorPrefEdit.action?instructorId="+form.getPreviousId()));
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
    		// Generate Time Pattern Grids
        	timePatterns.add(new TimePattern(Long.valueOf(-1)));
    		for (Preference pref: inst.getPreferences()) {
    			if (pref instanceof TimePref) {
    				form.setAvailability(((TimePref)pref).getPreference());
    				break;
    			}
    		}
        }
        
        //load class assignments
		if (!inst.getClasses().isEmpty()) {
		    WebTable classTable = new WebTable( 3,
			   	null,
		    	new String[] {"class", "Type" , "Limit"},
			    new String[] {"left", "left","left"},
			    null );
					
		    //Get class assignment information
			for (Iterator iterInst = inst.getClasses().iterator(); iterInst.hasNext();) {
				ClassInstructor ci = (ClassInstructor) iterInst.next();
				Class_ c = ci.getClassInstructing();
				classTable.addLine(
					null,
					new String[] {
						c.getClassLabel(),
						c.getItypeDesc(),
						c.getExpectedCapacity().toString(),
					},
					null,null);
			}
			String tblData = classTable.printTable();
			request.setAttribute("classTable", tblData);
		}
		
		// Process Preferences Action
		processPrefAction();
		
        LookupTables.setupRooms(request, inst);		 // Room Prefs
        LookupTables.setupBldgs(request, inst);		 // Building Prefs
        LookupTables.setupRoomFeatures(request, inst); // Preference Levels
        LookupTables.setupRoomGroups(request, inst);   // Room Groups
	
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
		
		if ("Enabled".equalsIgnoreCase(ApplicationProperty.InstructorUnavailbeDays.value()) || "Preferences".equalsIgnoreCase(ApplicationProperty.InstructorUnavailbeDays.value()))
			request.setAttribute("unavailableDaysPattern", inst.getUnavailablePatternHtml(true));

		form.setName(
				inst.getName(UserProperty.NameFormat.get(sessionContext.getUser())) + 
    			(inst.getPositionType() == null ? "" : " (" + inst.getPositionType().getLabel() + ")"));
		
		if (inst.getExternalUniqueId() != null) {
			form.setPuId(inst.getExternalUniqueId());
		}
				
		form.setDeptName(inst.getDepartment().getName().trim());
		
		if (inst.getPositionType() != null) {
			form.setPosType(inst.getPositionType().getUniqueId().toString());
		}
		
		if (inst.getCareerAcct() != null) {
			form.setCareerAcct(inst.getCareerAcct().trim());
		}
		
		form.setEmail(inst.getEmail());
		
		if (inst.getNote() != null) {
			form.setNote(inst.getNote().trim());
		}
		
		try {
			DepartmentalInstructor previous = inst.getPreviousDepartmentalInstructor(sessionContext, Right.InstructorPreferences);
			form.setPreviousId(previous==null?null:previous.getUniqueId().toString());
			DepartmentalInstructor next = inst.getNextDepartmentalInstructor(sessionContext, Right.InstructorPreferences);
			form.setNextId(next==null?null:next.getUniqueId().toString());
		} catch (Exception e) {
			Debug.error(e);
		}
	}

}

