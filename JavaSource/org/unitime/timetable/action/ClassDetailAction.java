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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.cpsolver.coursett.model.RoomLocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.commons.Debug;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.form.ClassEditForm;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.StudentAccomodation;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.comparators.InstructorComparator;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.SolverProxy;
import org.unitime.timetable.solver.TimetableDatabaseLoader;
import org.unitime.timetable.solver.service.SolverService;
import org.unitime.timetable.util.LookupTables;
import org.unitime.timetable.webutil.BackTracker;
import org.unitime.timetable.webutil.DistributionPrefsTableBuilder;


/**
 * MyEclipse Struts
 * Creation date: 03-29-2006
 *
 * XDoclet definition:
 * @struts.action path="/classDetail" name="classEditForm" attribute="ClassEditForm" input="/user/classEdit.jsp" scope="request"
 *
 * @author Tomas Muller, Zuzana Mullerova, Stephanie Schluttenhofer
 */
@Service("/classDetail")
public class ClassDetailAction extends PreferencesAction {

	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	
	@Autowired SessionContext sessionContext;
	
	@Autowired SolverService<SolverProxy> courseTimetablingSolverService;
	
    // --------------------------------------------------------- Class Constants

    /** Anchor names **/
    public final String HASH_INSTR_PREF = "InstructorPref";

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

	        super.execute(mapping, form, request, response);

	        ClassEditForm frm = (ClassEditForm) form;
	        ActionMessages errors = new ActionMessages();

	        // Read parameters
	        String classId =  request.getParameter("cid")==null
								? request.getAttribute("cid") !=null
								        ? request.getAttribute("cid").toString()
								        : null
								: request.getParameter("cid");
	        String op = frm.getOp();

	        // Read class id from form
	        if(op.equals(MSG.actionEditClass())
	        		|| op.equals(MSG.actionAddDistributionPreference())
	                // || op.equals(rsc.getMessage("button.backToInstrOffrDet")) for deletion
	                || op.equals(MSG.actionNextClass())
	                || op.equals(MSG.actionPreviousClass())
	                ) {
	            classId = frm.getClassId().toString();
	        } else {
	        	frm.reset(mapping, request);
	        }

	        Debug.debug("op: " + op);
	        Debug.debug("class: " + classId);

	        // Check class exists
	        if(classId==null || classId.trim().length()==0)
	            throw new Exception ("Class Info not supplied.");

	        sessionContext.checkPermission(classId, "Class_", Right.ClassDetail);
	        
	        boolean timeVertical = CommonValues.VerticalGrid.eq(sessionContext.getUser().getProperty(UserProperty.GridOrientation));

	        // If class id is not null - load class info
	        Class_DAO cdao = new Class_DAO();
	        Class_ c = cdao.get(new Long(classId));

	        // Edit Preference - Redirect to prefs edit screen
	        if(op.equals(MSG.actionEditClass())
	                && classId!=null && classId.trim()!="") {
	        	response.sendRedirect( response.encodeURL("classEdit.do?cid=" + c.getUniqueId().toString() + "&sec=" + c.getSectionNumberString() ));
	        	return null;
	        }

            if (op.equals(MSG.actionNextClass())) {
            	response.sendRedirect(response.encodeURL("classDetail.do?cid="+frm.getNextId()));
            	return null;
            }

            if (op.equals(MSG.actionPreviousClass())) {
            	response.sendRedirect(response.encodeURL("classDetail.do?cid="+frm.getPreviousId()));
            	return null;
            }

			// Add Distribution Preference - Redirect to dist prefs screen
		    if(op.equals(MSG.actionAddDistributionPreference())) {
		        SchedulingSubpart ss = c.getSchedulingSubpart();
		        CourseOffering cco = ss.getInstrOfferingConfig().getControllingCourseOffering();
		        request.setAttribute("subjectAreaId", cco.getSubjectArea().getUniqueId().toString());
		        request.setAttribute("schedSubpartId", ss.getUniqueId().toString());
		        request.setAttribute("courseOffrId", cco.getUniqueId().toString());
		        request.setAttribute("classId", c.getUniqueId().toString());
	            return mapping.findForward("addDistributionPrefs");
		    }

	        // Load form attributes that are constant
	        doLoad(request, frm, c, op);

	        // Initialize Preferences for initial load
	        frm.setAvailableTimePatterns(TimePattern.findApplicable(sessionContext.getUser(),c.getSchedulingSubpart().getMinutesPerWk().intValue(),true,c.getManagingDept()));
			Set timePatterns = null;
        	initPrefs(frm, c, null, false);
		    timePatterns = c.effectiveTimePatterns();

		    // Display distribution Prefs
	        DistributionPrefsTableBuilder tbl = new DistributionPrefsTableBuilder();
	        String html = tbl.getDistPrefsTableForClass(request, sessionContext, c);
	        if (html!=null)
	        	request.setAttribute(DistributionPref.DIST_PREF_REQUEST_ATTR, html);

			// Process Preferences Action
			processPrefAction(request, frm, errors);

	        // Generate Time Pattern Grids
			super.generateTimePatternGrids(request, frm, c, timePatterns, "init", timeVertical, false, null);

			// Instructors
	        setupInstructors(request, frm, c);
	        
	        // date Patterns
	        setupDatePatterns(request, frm, c);

	        LookupTables.setupDatePatterns(request, sessionContext.getUser(), "Default", c.getSchedulingSubpart().effectiveDatePattern(), c.getManagingDept(), c.effectiveDatePattern());

	        LookupTables.setupRooms(request, c);		 // Room Prefs
	        LookupTables.setupBldgs(request, c);		 // Building Prefs
	        LookupTables.setupRoomFeatures(request, c); // Preference Levels
	        LookupTables.setupRoomGroups(request, c);   // Room Groups

	        BackTracker.markForBack(
	        		request,
	        		"classDetail.do?cid="+frm.getClassId(),
	        		MSG.backClass(frm.getClassName()),
	        		true, false);
	        
	        return mapping.findForward("displayClass");

	    	} catch (Exception e) {
	    		Debug.error(e);
	    		throw e;
	    	}
	    }

	    /**
	     * Loads class info into the form
	     * @param request
	     * @param frm
	     * @param c
	     * @param classId
	     */
	    private void doLoad(
	    		HttpServletRequest request,
	            ClassEditForm frm,
	            Class_ c,
	            String op) {

	        String parentClassName = "-";
	        Long parentClassId = null;
	        if (c.getParentClass()!=null) {
	            parentClassName = c.getParentClass().toString();
	            if (sessionContext.hasPermission(c.getParentClass(), Right.ClassDetail))
	            	parentClassId = c.getParentClass().getUniqueId();
	        }

	        CourseOffering cco = c.getSchedulingSubpart().getControllingCourseOffering();

		    // Set Session Variables
	        InstructionalOfferingSearchAction.setLastInstructionalOffering(sessionContext, c.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering());

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
	        if (sessionContext.hasPermission(c.getSchedulingSubpart(), Right.SchedulingSubpartDetail))
	        	frm.setSubpart(c.getSchedulingSubpart().getUniqueId());
	        else
	        	frm.setSubpart(null);
	        frm.setCourseName(cco.getInstructionalOffering().getCourseName());
	        frm.setCourseTitle(cco.getTitle());
	        frm.setIsCrosslisted(new Boolean(cco.getInstructionalOffering().getCourseOfferings().size()>1));
	        frm.setAccommodation(StudentAccomodation.toHtml(StudentAccomodation.getAccommodations(c)));

	        // Load from class
		    frm.setExpectedCapacity(c.getExpectedCapacity());
		    frm.setEnrollment(c.getEnrollment());
	        frm.setDatePattern(c.getDatePattern()==null?new Long(-1):c.getDatePattern().getUniqueId());
		    frm.setNbrRooms(c.getNbrRooms());
		    if (c.getNotes()==null)
		    	frm.setNotes("");
		    else
		    	frm.setNotes(c.getNotes().replaceAll("\n","<BR>"));
		    frm.setManagingDept(c.getManagingDept().getUniqueId());
		    frm.setManagingDeptLabel(c.getManagingDept().getManagingDeptLabel());
		    frm.setSchedulePrintNote(c.getSchedulePrintNote());
		    frm.setClassSuffix(c.getDivSecNumber());
		    frm.setMaxExpectedCapacity(c.getMaxExpectedCapacity());
		    frm.setRoomRatio(c.getRoomRatio());
		    frm.setEnabledForStudentScheduling(c.isEnabledForStudentScheduling());
		    frm.setDisplayInstructor(c.isDisplayInstructor());
	        frm.setMinRoomLimit(c.getMinRoomLimit());

	        Class_ next = c.getNextClass(sessionContext, Right.ClassDetail);
	        frm.setNextId(next==null?null:next.getUniqueId().toString());
	        Class_ previous = c.getPreviousClass(sessionContext, Right.ClassDetail);
	        frm.setPreviousId(previous==null?null:previous.getUniqueId().toString());

		    List instructors = new ArrayList(c.getClassInstructors());
		    InstructorComparator ic = new InstructorComparator();
		    ic.setCompareBy(ic.COMPARE_BY_LEAD);
		    Collections.sort(instructors, ic);

		    for(Iterator iter = instructors.iterator(); iter.hasNext(); ) {
		    	ClassInstructor classInstr = (ClassInstructor) iter.next();
		        frm.addToInstructors(classInstr);
		    }

	        if (c.getNbrRooms().intValue()>0) {
	        	List<RoomLocation> roomLocations = TimetableDatabaseLoader.computeRoomLocations(c);
	        	StringBuffer rooms = new StringBuffer();
	        	if (roomLocations.isEmpty()) {
	        		request.setAttribute(Location.AVAILABLE_LOCATIONS_ATTR,
	        				"<font color='red'><b>No rooms are available.</b></font>");
	        	} else {
	        		int idx = 0;
	        		for (RoomLocation rl: roomLocations) {
	        			if (idx>0) rooms.append(", ");
	    				if (idx==4)
	    					rooms.append("<span id='room_dots' onMouseOver=\"this.style.cursor='hand';this.style.cursor='pointer';\" style='display:inline'><a onClick=\"document.getElementById('room_dots').style.display='none';document.getElementById('room_rest').style.display='inline';\">...</a></span><span id='room_rest' style='display:none'>");
	        			rooms.append(
	        					"<span style='color:"+PreferenceLevel.int2color(rl.getPreference())+";' " +
	        					"onmouseover=\"showGwtRoomHint(this, '" + rl.getId() + "', '" + PreferenceLevel.int2string(rl.getPreference()) + "');\" onmouseout=\"hideGwtRoomHint();\">"+
	        					rl.getName()+
	        					"</span>");
	        			idx++;
	        		}
	        		if (idx>=4) rooms.append("</span>");
		        	if (roomLocations.size()<c.getNbrRooms().intValue()) {
		        		request.setAttribute(Location.AVAILABLE_LOCATIONS_ATTR,
		        				"<font color='red'><b>Not enough rooms are available:</b></font> "+rooms);
		        	} else {
		        		request.setAttribute(Location.AVAILABLE_LOCATIONS_ATTR,
		        				roomLocations.size()+" ("+rooms+")");
		        	}
	        	}
	        }
	    }

	    /**
	     * Set up instructor lists
	     * @param request
	     * @param frm
	     * @param errors
	     */
	    protected void setupInstructors(
	            HttpServletRequest request,
	            ClassEditForm frm,
	            Class_ c ) throws Exception {

	        List instructors = frm.getInstructors();
	        if(instructors.size()==0)
	            return;

	        // Get dept instructor list
	        LookupTables.setupInstructors(request, sessionContext, c.getDepartmentForSubjectArea().getUniqueId());
	        Vector deptInstrList = (Vector) request.getAttribute(DepartmentalInstructor.INSTR_LIST_ATTR_NAME);

	        // For each instructor set the instructor list
	        for (int i=0; i<instructors.size(); i++) {
    	        request.setAttribute(DepartmentalInstructor.INSTR_LIST_ATTR_NAME + i, deptInstrList);
	        }
	    }
	        	    
	    private void setupDatePatterns(HttpServletRequest request, ClassEditForm frm, Class_ c) throws Exception {	    	  
	    	DatePattern selectedDatePattern = c.effectiveDatePattern();			
			if (selectedDatePattern != null) {
				List<DatePattern> children = selectedDatePattern.findChildren();
				for (DatePattern dp: children) {
					if (!frm.getDatePatternPrefs().contains(
							dp.getUniqueId().toString())) {
						frm.addToDatePatternPrefs(dp.getUniqueId()
								.toString(), PreferenceLevel.PREF_LEVEL_NEUTRAL);
					}
				}
				frm.sortDatePatternPrefs(frm.getDatePatternPrefs(), frm.getDatePatternPrefLevels(), children);
			}			
		}

}

