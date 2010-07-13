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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.sf.cpsolver.coursett.model.RoomLocation;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.util.MessageResources;
import org.unitime.commons.Debug;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.form.ClassEditForm;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.Reservation;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.TimePref;
import org.unitime.timetable.model.comparators.InstructorComparator;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.solver.TimetableDatabaseLoader;
import org.unitime.timetable.solver.WebSolver;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.LookupTables;
import org.unitime.timetable.webutil.BackTracker;
import org.unitime.timetable.webutil.DistributionPrefsTableBuilder;
import org.unitime.timetable.webutil.RequiredTimeTable;
import org.unitime.timetable.webutil.ReservationsTableBuilder;


/**
 * MyEclipse Struts
 * Creation date: 03-29-2006
 *
 * XDoclet definition:
 * @struts.action path="/classDetail" name="classEditForm" attribute="ClassEditForm" input="/user/classEdit.jsp" scope="request"
 */
public class ClassDetailAction extends PreferencesAction {

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

	        HttpSession httpSession = request.getSession();
	        ClassEditForm frm = (ClassEditForm) form;
	        MessageResources rsc = getResources(request);
	        ActionMessages errors = new ActionMessages();

	        // Read parameters
	        String classId =  request.getParameter("cid")==null
								? request.getAttribute("cid") !=null
								        ? request.getAttribute("cid").toString()
								        : null
								: request.getParameter("cid");
	        String op = frm.getOp();
	        boolean timeVertical = RequiredTimeTable.getTimeGridVertical(Web.getUser(httpSession));

	        // Read class id from form
	        if(op.equals(rsc.getMessage("button.editPrefsClass"))
	        		|| op.equals(rsc.getMessage("button.addDistPref"))
	                || op.equals(rsc.getMessage("button.backToInstrOffrDet"))
	                || op.equals(rsc.getMessage("button.nextClass"))
	                || op.equals(rsc.getMessage("button.previousClass"))
	                || op.equals(rsc.getMessage("button.addReservation"))
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

	        // backToInstrOffr - Go back to Instructional Offering Screen
	        if(op.equals(rsc.getMessage("button.backToInstrOffrDet"))
	                && classId!=null && classId.trim().length()!=0 ) {

	            Class_DAO cdao = new Class_DAO();
	            SchedulingSubpart ss = cdao.get(new Long(classId)).getSchedulingSubpart();

	            response.sendRedirect( response.encodeURL("instructionalOfferingDetail.do?op=view&io="+ss.getInstrOfferingConfig().getInstructionalOffering().getUniqueId()));
	        }

	        // If class id is not null - load class info
	        Class_DAO cdao = new Class_DAO();
	        Class_ c = cdao.get(new Long(classId));

	        // Edit Preference - Redirect to prefs edit screen
	        if(op.equals(rsc.getMessage("button.editPrefsClass"))
	                && classId!=null && classId.trim()!="") {
	        	response.sendRedirect( response.encodeURL("classEdit.do?cid=" + c.getUniqueId().toString() + "&sec=" + c.getSectionNumberString() ));
	        }

            if (op.equals(rsc.getMessage("button.nextClass"))) {
            	response.sendRedirect(response.encodeURL("classDetail.do?cid="+frm.getNextId()));
            	return null;
            }

            if (op.equals(rsc.getMessage("button.previousClass"))) {
            	response.sendRedirect(response.encodeURL("classDetail.do?cid="+frm.getPreviousId()));
            	return null;
            }

			// Add Distribution Preference - Redirect to dist prefs screen
		    if(op.equals(rsc.getMessage("button.addDistPref"))) {
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

	        User user = Web.getUser(httpSession);
	        
	        frm.setDisplayInfo(
	        		c.getManagingDept()!=null &&
	        		c.getManagingDept().getSolverGroup()!=null &&
	        		c.getManagingDept().getSolverGroup().getCommittedSolution()!=null &&  // HAS A COMMITED SOLUTION
	        		c.isEditableBy(user) && // CLASS IS EDITABLE
	        		WebSolver.getSolver(httpSession)==null && // NOT LOADED INTO THE SOLVER
	        		c.effectiveDatePattern()!=null && //HAS DATE PATTERN
	        		!c.effectivePreferences(TimePref.class).isEmpty() && //HAS TIME PATTERN
	        		user.isAdmin() //TODO: remove this once the info box allows to touch only classes editable by the user
	        		);

	        // Initialize Preferences for initial load
	        frm.setAvailableTimePatterns(TimePattern.findApplicable(request,c.getSchedulingSubpart().getMinutesPerWk().intValue(),true,c.getManagingDept()));
			Set timePatterns = null;
        	initPrefs(user, frm, c, null, false);
		    timePatterns = c.effectiveTimePatterns();

	        // Dist Prefs are not editable by Sched Dpty Asst
	        String currentRole = user.getCurrentRole();
	        boolean editable = true;

		    // Display distribution Prefs
	        DistributionPrefsTableBuilder tbl = new DistributionPrefsTableBuilder();
	        String html = tbl.getDistPrefsTableForClass(request, c, true);
	        if (html!=null)
	        	request.setAttribute(DistributionPref.DIST_PREF_REQUEST_ATTR, html);

			// Process Preferences Action
			processPrefAction(request, frm, errors);

	        // Generate Time Pattern Grids
			super.generateTimePatternGrids(request, frm, c, timePatterns, "init", timeVertical, false, null);

			// Instructors
	        setupInstructors(request, frm, c);

	        LookupTables.setupDatePatterns(request, "Default", c.getSchedulingSubpart().effectiveDatePattern(), c.getManagingDept(), c.effectiveDatePattern());

	        LookupTables.setupRooms(request, c);		 // Room Prefs
	        LookupTables.setupBldgs(request, c);		 // Building Prefs
	        LookupTables.setupRoomFeatures(request, c); // Preference Levels
	        LookupTables.setupRoomGroups(request, c);   // Room Groups

	        BackTracker.markForBack(
	        		request,
	        		"classDetail.do?cid="+frm.getClassId(),
	        		"Class ("+frm.getClassName()+")",
	        		true, false);

			// Add Reservation
			if(op.equals(rsc.getMessage("button.addReservation"))) {
			    //TODO Reservations Bypass - to be removed later
			    request.setAttribute("ownerId", frm.getClassId());
			    request.setAttribute("ownerName", frm.getClassName());
			    request.setAttribute("ownerType", Constants.RESV_OWNER_CLASS);
			    request.setAttribute("ownerTypeLabel", Constants.RESV_OWNER_CLASS_LBL);
			    InstrOfferingConfig ioc = c.getSchedulingSubpart().getInstrOfferingConfig();
                request.setAttribute("ioLimit", 
                		ioc.getInstructionalOffering().getLimit()!=null 
	                		? ioc.getInstructionalOffering().getLimit().toString()
	                		: null);
                request.setAttribute("unlimited", ioc.isUnlimitedEnrollment());
			    return mapping.findForward("displayCourseReservation");
			    // End Bypass

		        //TODO Reservations - functionality to be made visible later
			    /*
			    request.setAttribute("ownerId", frm.getClassId().toString());
			    request.setAttribute("ownerClassId", Constants.RESV_OWNER_CLASS);
			    return mapping.findForward("addReservation");
			    */
			}

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

	        HttpSession httpSession = request.getSession();
	    	User user = Web.getUser(httpSession);

	        String parentClassName = "-";
	        Long parentClassId = null;
	        if(c.getParentClass()!=null) {
	            parentClassName = c.getParentClass().toString();
	            if (c.getParentClass().isViewableBy(user))
	            	parentClassId = c.getParentClass().getUniqueId();
	        }

	        CourseOffering cco = c.getSchedulingSubpart().getControllingCourseOffering();

		    // Set Session Variables
	        httpSession.setAttribute(Constants.SUBJ_AREA_ID_ATTR_NAME, cco.getSubjectArea().getUniqueId().toString());
	        if (httpSession.getAttribute(Constants.CRS_NBR_ATTR_NAME)!=null
	                && httpSession.getAttribute(Constants.CRS_NBR_ATTR_NAME).toString().length()>0)
	            httpSession.setAttribute(Constants.CRS_NBR_ATTR_NAME, cco.getCourseNbr());

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
	        if (c.getSchedulingSubpart().isViewableBy(user))
	        	frm.setSubpart(c.getSchedulingSubpart().getUniqueId());
	        else
	        	frm.setSubpart(null);
	        frm.setCourseName(cco.getInstructionalOffering().getCourseName());
		    //TODO Reservations Bypass - to be removed later
	        frm.setIsCrosslisted(new Boolean(cco.getInstructionalOffering().getCourseOfferings().size()>1));
	        // End Bypass

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
		    frm.setDisplayInScheduleBook(c.isDisplayInScheduleBook());
		    frm.setDisplayInstructor(c.isDisplayInstructor());
	        frm.setMinRoomLimit(c.getMinRoomLimit());

	        Class_ next = c.getNextClass(request.getSession(), Web.getUser(request.getSession()), false, true);
	        frm.setNextId(next==null?null:next.getUniqueId().toString());
	        Class_ previous = c.getPreviousClass(request.getSession(), Web.getUser(request.getSession()), false, true);
	        frm.setPreviousId(previous==null?null:previous.getUniqueId().toString());

		    List instructors = new ArrayList(c.getClassInstructors());
		    InstructorComparator ic = new InstructorComparator();
		    ic.setCompareBy(ic.COMPARE_BY_LEAD);
		    Collections.sort(instructors, ic);

		    for(Iterator iter = instructors.iterator(); iter.hasNext(); ) {
		    	ClassInstructor classInstr = (ClassInstructor) iter.next();
		        frm.addToInstructors(classInstr);
		    }

	        ReservationsTableBuilder resvTbl = new ReservationsTableBuilder();
	        String resvHtml = resvTbl.htmlTableForReservations(c.effectiveReservations(true, true, true, true, true), true, c.isEditableBy(user), c.getSchedulingSubpart().getInstrOfferingConfig().getControllingCourseOffering().isLimitedEditableBy(user));
	        if (resvHtml!=null)
	        	request.setAttribute(Reservation.RESV_REQUEST_ATTR, resvHtml);

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
	        					"<span title='"+PreferenceLevel.int2string(rl.getPreference())+" "+rl.getName()+" ("+rl.getRoomSize()+" seats)'>"+
	        					"<font color='"+PreferenceLevel.int2color(rl.getPreference())+"'>"+
	        					rl.getName()+
	        					"</font></span>");
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
	        LookupTables.setupInstructors(request, c.getDepartmentForSubjectArea().getUniqueId());
	        Vector deptInstrList = (Vector) request.getAttribute(DepartmentalInstructor.INSTR_LIST_ATTR_NAME);

	        // For each instructor set the instructor list
	        for (int i=0; i<instructors.size(); i++) {
    	        request.setAttribute(DepartmentalInstructor.INSTR_LIST_ATTR_NAME + i, deptInstrList);
	        }
	    }
}

