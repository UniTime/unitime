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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.util.MessageResources;
import org.unitime.commons.Debug;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.commons.web.WebTable;
import org.unitime.timetable.form.InstructorEditForm;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.LookupTables;
import org.unitime.timetable.webutil.BackTracker;
import org.unitime.timetable.webutil.RequiredTimeTable;


/** 
 * MyEclipse Struts
 * Creation date: 07-24-2006
 * 
 * XDoclet definition:
 * @struts.action path="/instructorPrefEdit" name="instructorEditForm" input="/user/instructorPrefsEdit.jsp" scope="request"
 * @struts.action-forward name="showEdit" path="instructorPrefsEditTile"
 */
public class InstructorPrefEditAction extends PreferencesAction {

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
            
            HttpSession httpSession = request.getSession();
    		InstructorEditForm frm = (InstructorEditForm) form;       
            MessageResources rsc = getResources(request);
            ActionMessages errors = new ActionMessages();
            
            // Read parameters
            String instructorId = request.getParameter("instructorId");
            String op = frm.getOp();            
            String reloadCause = request.getParameter("reloadCause");
            String deleteType = request.getParameter("deleteType");
            boolean timeVertical = RequiredTimeTable.getTimeGridVertical(Web.getUser(httpSession));
            
            // Read subpart id from form
            if(op.equals(rsc.getMessage("button.reload"))
            		|| op.equals(rsc.getMessage("button.addTimePattern"))
                    || op.equals(rsc.getMessage("button.addRoomPref"))
                    || op.equals(rsc.getMessage("button.addBldgPref"))
                    || op.equals(rsc.getMessage("button.addRoomFeaturePref"))
                    || op.equals(rsc.getMessage("button.addDistPref")) 
                    || op.equals(rsc.getMessage("button.addRoomGroupPref"))
                    || op.equals(rsc.getMessage("button.updatePrefs")) 
                    || op.equals(rsc.getMessage("button.cancel")) 
                    || op.equals(rsc.getMessage("button.clearInstrPrefs"))                 
                    || op.equals(rsc.getMessage("button.delete"))
                    || op.equals(rsc.getMessage("button.returnToDetail"))
                    || op.equals(rsc.getMessage("button.nextInstructor"))
                    || op.equals(rsc.getMessage("button.previousInstructor"))) {
            	instructorId = frm.getInstructorId();
            }
            
            // Determine if initial load
            if(op==null || op.trim().length()==0 
                    || ( op.equals(rsc.getMessage("button.reload")) 
                    	 && (reloadCause==null || reloadCause.trim().length()==0) )) {
                op = "init";
            }
            
            // Check op exists
            if(op==null || op.trim()=="") 
                throw new Exception ("Null Operation not supported.");
            
            //Check instructor exists
            if(instructorId==null || instructorId.trim()=="") 
                throw new Exception ("Instructor Info not supplied.");
            
            // Set screen name
            frm.setScreenName("instructorPref");
            
            // If subpart id is not null - load subpart info
            DepartmentalInstructorDAO idao = new DepartmentalInstructorDAO();
            DepartmentalInstructor inst = idao.get(new Long(instructorId)); 
            
            // Cancel - Go back to Instructors Detail Screen
            if(op.equals(rsc.getMessage("button.returnToDetail")) 
                    && instructorId!=null && instructorId.trim()!="") {
                request.setAttribute("instructorId", instructorId);
                request.setAttribute("fromChildScreen", "true");
                return mapping.findForward("showDetail");
            }
            
            // Clear all preferences
            if(op.equals(rsc.getMessage("button.clearInstrPrefs"))) { 
            	Set s = inst.getPreferences();
                s.clear();
                inst.setPreferences(s);            
                idao.update(inst);
                op = "init";            	
                request.setAttribute("instructorId", instructorId);
                request.setAttribute("fromChildScreen", "true");
                
                ChangeLog.addChange(
                        null, 
                        request,
                        inst, 
                        ChangeLog.Source.INSTRUCTOR_PREF_EDIT, 
                        ChangeLog.Operation.CLEAR_PREF, 
                        null, 
                        inst.getDepartment());
                
                return mapping.findForward("showDetail");
            }
            
            // Reset form for initial load
            if(op.equals("init")) { 
                frm.reset(mapping, request);
            }
            
            // Load form attributes that are constant
            doLoad(request, frm, inst, instructorId);
            
            // Update Preferences for InstructorDept
            if(op.equals(rsc.getMessage("button.update")) 
            		 || op.equals(rsc.getMessage("button.nextInstructor")) || op.equals(rsc.getMessage("button.previousInstructor"))
            		|| op.equals(rsc.getMessage("button.updatePrefs")) ) {	
                // Validate input prefs
                errors = frm.validate(mapping, request);
                
                // No errors - Add to instructorDept and update
                if(errors.size()==0) {
                    Set s = inst.getPreferences();
             
                    // Clear all old prefs
                    s.clear();                
                    
            		super.doUpdate(request, frm, inst, s, timeVertical);
                    
                    ChangeLog.addChange(
                            null, 
                            request,
                            inst, 
                            ChangeLog.Source.INSTRUCTOR_PREF_EDIT, 
                            ChangeLog.Operation.UPDATE, 
                            null, 
                            inst.getDepartment());

            		idao.saveOrUpdate(inst);
                    request.setAttribute("instructorId", instructorId);
                    request.setAttribute("fromChildScreen", "true");
                    request.setAttribute("showPrefs", "true");
                    
    	        	if (op.equals(rsc.getMessage("button.nextInstructor")))
    	            	response.sendRedirect(response.encodeURL("instructorPrefEdit.do?instructorId="+frm.getNextId()));
    	            
    	            if (op.equals(rsc.getMessage("button.previousInstructor")))
    	            	response.sendRedirect(response.encodeURL("instructorPrefEdit.do?instructorId="+frm.getPreviousId()));
                    
                    return mapping.findForward("showDetail");
                }
                else {
                    saveErrors(request, errors);
                }
            }
            
	        User user = Web.getUser(httpSession);

	        // Initialize Preferences for initial load 
            Set timePatterns = new HashSet();
            frm.setAvailableTimePatterns(null);
            if(op.equals("init")) {
            	initPrefs(user, frm, inst, null, true);
            	timePatterns.add(new TimePattern(new Long(-1)));
        		//timePatterns.addAll(TimePattern.findApplicable(request,30,false));
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
    		
    		//// Set display distribution to Not Applicable
    		/*
    		request.setAttribute(DistributionPref.DIST_PREF_REQUEST_ATTR, 
    				"<FONT color=696969>Distribution Preferences Not Applicable</FONT>");
    				*/
            
    		// Process Preferences Action
    		processPrefAction(request, frm, errors);
    		
    		// Generate Time Pattern Grids
    		super.generateTimePatternGrids(request, frm, inst, timePatterns, op, timeVertical, true, null);

            LookupTables.setupRooms(request, inst);		 // Room Prefs
            LookupTables.setupBldgs(request, inst);		 // Building Prefs
            LookupTables.setupRoomFeatures(request, inst); // Preference Levels
            LookupTables.setupRoomGroups(request, inst);   // Room Groups
		
            BackTracker.markForBack(
            		request,
            		"instructorDetail.do?instructorId="+frm.getInstructorId(),
            		"Instructor ("+ (frm.getName()==null?"null":frm.getName().trim()) +")",
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

		frm.setName(Constants.toInitialCase(inst.getFirstName(), "-".toCharArray())+ " " 
    			+ ((inst.getMiddleName() == null) ?"": Constants.toInitialCase(inst.getMiddleName(), "-".toCharArray()) )+ " " 
    			+ Constants.toInitialCase(inst.getLastName(), "-".toCharArray()));
		
		if (inst.getExternalUniqueId() != null) {
			frm.setPuId(inst.getExternalUniqueId());
		}
				
		frm.setDeptName(inst.getDepartment().getName().trim());
		
		if (inst.getPositionType() != null) {
			frm.setPosType(inst.getPositionType().getUniqueId().toString());
		}
		
		if (inst.getCareerAcct() != null) {
			frm.setCareerAcct(inst.getCareerAcct().trim());
		}
		
		frm.setEmail(inst.getEmail());
		
		if (inst.getNote() != null) {
			frm.setNote(inst.getNote().trim());
		}
		
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

