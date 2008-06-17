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
import org.unitime.timetable.form.ClassEditForm;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.comparators.InstructorComparator;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.DatePatternDAO;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.util.LookupTables;
import org.unitime.timetable.webutil.BackTracker;
import org.unitime.timetable.webutil.RequiredTimeTable;


/**
 * MyEclipse Struts
 * Creation date: 12-08-2005
 *
 * XDoclet definition:
 * @struts:action path="/classEdit" name="classEditForm" input="/user/classEdit.jsp" scope="request" validate="true"
 */
public class ClassEditAction extends PreferencesAction {

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

        String reloadCause = request.getParameter("reloadCause");
        String deleteType = request.getParameter("deleteType");
        String op = frm.getOp();
        if (request.getParameter("op2")!=null && request.getParameter("op2").length()>0)
        	op = request.getParameter("op2");
        boolean timeVertical = RequiredTimeTable.getTimeGridVertical(Web.getUser(httpSession));

        // Read class id from form
        if(//op.equals(rsc.getMessage("button.reload"))||
                op.equals(rsc.getMessage("button.addTimePattern"))
                || op.equals(rsc.getMessage("button.addRoomPref"))
                || op.equals(rsc.getMessage("button.addBldgPref"))
                || op.equals(rsc.getMessage("button.addRoomFeaturePref"))
                || op.equals(rsc.getMessage("button.addDistPref"))
                || op.equals(rsc.getMessage("button.addInstructor"))
                || op.equals(rsc.getMessage("button.update"))
                || op.equals(rsc.getMessage("button.cancel"))
                || op.equals(rsc.getMessage("button.clearClassPrefs"))
                || op.equals(rsc.getMessage("button.delete"))
                || op.equals(rsc.getMessage("button.changeOwner"))
                || op.equals(rsc.getMessage("button.addRoomGroupPref"))
                || op.equals(rsc.getMessage("button.returnToDetail"))
                || op.equals(rsc.getMessage("button.nextClass"))
                || op.equals(rsc.getMessage("button.previousClass"))
                || op.equals("updatePref")) {
            classId = frm.getClassId().toString();
        }

        // Determine if initial load
        if(op==null || op.trim().length()==0
                || (request.getAttribute("cs")!=null
                        && request.getAttribute("cs").toString().equals("classOwnerChange") )
           ) {
            op = "init";
        }

        // Check op exists
        if(op==null || op.trim()=="")
            throw new Exception ("Null Operation not supported.");

        Debug.debug("op: " + op);
        Debug.debug("class: " + classId);
        Debug.debug("reload cause: " + reloadCause);

        // Check class exists
        if(classId==null || classId.trim().length()==0)
            throw new Exception ("Class Info not supplied.");

        // Change Owner - Go back to Change Owner Screen
        if(op.equals(rsc.getMessage("button.changeOwner"))
                && classId!=null && classId.trim().length()!=0) {

            request.setAttribute("classId", classId);
            return mapping.findForward("changeClassOwner");
        }

        // Cancel - Go back to Class Detail Screen
        if(op.equals(rsc.getMessage("button.returnToDetail"))
                && classId!=null && classId.trim().length()!=0 ) {
            request.setAttribute("cid", classId);
            return mapping.findForward("displayClassDetail");
        }

        // If class id is not null - load class info
        Class_DAO cdao = new Class_DAO();
        Class_ c = cdao.get(new Long(classId));

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

	    // Add Instructor
	    if(op.equals(rsc.getMessage("button.addInstructor")))
            addInstructor(request, frm, errors);

	    // Delete Instructor
        if(op.equals(rsc.getMessage("button.delete"))
                && request.getParameter("deleteType")!=null
                && request.getParameter("deleteType").equals("instructor"))
            deleteInstructor(request, frm);

        // Restore all inherited preferences
        if(op.equals(rsc.getMessage("button.clearClassPrefs"))) {

            Set s = c.getPreferences();
            s.clear();
            c.setPreferences(s);
            cdao.update(c);
            op = "init";

            ChangeLog.addChange(
                    null,
                    request,
                    c,
                    ChangeLog.Source.CLASS_EDIT,
                    ChangeLog.Operation.CLEAR_PREF,
                    c.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getControllingCourseOffering().getSubjectArea(),
                    c.getManagingDept());

            request.setAttribute("cid", classId);
            return mapping.findForward("displayClassDetail");
        }

        // Reset form for initial load
        if(op.equals("init")) {
            frm.reset(mapping, request);
        }

        // Load form attributes that are constant
        doLoad(request, frm, c, op);

        // Update Preferences for Class
        if(op.equals(rsc.getMessage("button.update")) || op.equals(rsc.getMessage("button.nextClass")) || op.equals(rsc.getMessage("button.previousClass"))) {
            // Validate input prefs
            errors = frm.validate(mapping, request);

            // No errors - Add to class and update
            if(errors.size()==0) {

            	org.hibernate.Session hibSession = cdao.getSession();
            	Transaction tx = hibSession.beginTransaction();

            	try {
                    // Clear all old prefs
                    Set s = c.getPreferences();
                    s.clear();

                    // Save class data
                    doUpdate(request, frm, c, hibSession);

                    // Save Prefs
                    super.doUpdate(request, frm, c, s, timeVertical);

                    hibSession.saveOrUpdate(c);

    	            tx.commit();

    	            request.setAttribute("cid", classId);

    	            if (op.equals(rsc.getMessage("button.nextClass")))
    	            	response.sendRedirect(response.encodeURL("classEdit.do?cid="+frm.getNextId()));

    	            if (op.equals(rsc.getMessage("button.previousClass")))
    	            	response.sendRedirect(response.encodeURL("classEdit.do?cid="+frm.getPreviousId()));

    	            return mapping.findForward("displayClassDetail");

            	} catch (Exception e) {
            		tx.rollback(); throw e;
            	}
            }
            else {
                saveErrors(request, errors);
            }
        }

        Vector leadInstructors = new Vector();
        if (op.equals("updatePref")) {
        	try {
                List instrLead = frm.getInstrLead();
                List instructors = frm.getInstructors();

                for(int i=0; i<instructors.size(); i++) {
                    String instrId = instructors.get(i).toString();
                    if (Preference.BLANK_PREF_VALUE.equals(instrId)) continue;
                    boolean lead = "on".equals(instrLead.get(i));
                    if (lead) leadInstructors.add((new DepartmentalInstructorDAO()).get(new Long(instrId)));
                }
        		op="init";
        	} catch (NumberFormatException e) {}
        }

        User user = Web.getUser(httpSession);

        // Initialize Preferences for initial load
        frm.setAvailableTimePatterns(TimePattern.findApplicable(request,c.getSchedulingSubpart().getMinutesPerWk().intValue(),true,c.getManagingDept()));
		Set timePatterns = null;
        if(op.equals("init")) {
        	initPrefs(user, frm, c, leadInstructors, true);
		    timePatterns = c.effectiveTimePatterns();
        }

		// Process Preferences Action
		processPrefAction(request, frm, errors);

        // Generate Time Pattern Grids
		super.generateTimePatternGrids(request, frm, c, timePatterns, op, timeVertical, true, leadInstructors);

		// Instructors
        setupInstructors(request, frm, c);

        LookupTables.setupDatePatterns(request, "Default", c.getSchedulingSubpart().effectiveDatePattern(), c.getManagingDept(), c.effectiveDatePattern());

        LookupTables.setupRooms(request, c);		 // Rooms
        LookupTables.setupBldgs(request, c);		 // Buildings
        LookupTables.setupRoomFeatures(request, c); // Room Features
        LookupTables.setupRoomGroups(request, c);   // Room Groups

        frm.setAllowHardPrefs(c.canUseHardRoomPreferences(user));

        BackTracker.markForBack(
        		request,
        		"classDetail.do?cid="+frm.getClassId(),
        		"Class ("+frm.getClassName()+")",
        		true, false);

        return mapping.findForward("editClass");

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

        Department managingDept = c.getManagingDept();
        String parentClassName = "-";
        Long parentClassId = null;
        if(c.getParentClass()!=null) {
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
        frm.setManagingDept(managingDept.getUniqueId());
        frm.setManagingDeptLabel(managingDept.getManagingDeptLabel());
        frm.setUnlimitedEnroll(c.getSchedulingSubpart().getInstrOfferingConfig().isUnlimitedEnrollment());

        Class_ next = c.getNextClass(request.getSession(), Web.getUser(request.getSession()), true, false);
        frm.setNextId(next==null?null:next.getUniqueId().toString());
        Class_ previous = c.getPreviousClass(request.getSession(), Web.getUser(request.getSession()), true, false);
        frm.setPreviousId(previous==null?null:previous.getUniqueId().toString());
        frm.setMinRoomLimit(c.getMinRoomLimit());

        // Load from class only for initial load or reload
        if(op.equals("init")) {
	        frm.setExpectedCapacity(c.getExpectedCapacity());
	        frm.setDatePattern(c.getDatePattern()==null?new Long(-1):c.getDatePattern().getUniqueId());
	        frm.setNbrRooms(c.getNbrRooms());
	        frm.setNotes(c.getNotes());
	        frm.setManagingDept(c.getManagingDept().getUniqueId());
		    frm.setSchedulePrintNote(c.getSchedulePrintNote());
		    frm.setClassSuffix(c.getDivSecNumber());
		    frm.setMaxExpectedCapacity(c.getMaxExpectedCapacity());
		    frm.setRoomRatio(c.getRoomRatio());
		    frm.setDisplayInScheduleBook(c.isDisplayInScheduleBook());
		    frm.setDisplayInstructor(c.isDisplayInstructor());

		    List instructors = new ArrayList(c.getClassInstructors());
		    InstructorComparator ic = new InstructorComparator();
		    ic.setCompareBy(ic.COMPARE_BY_LEAD);
		    Collections.sort(instructors, ic);

	        for(Iterator iter = instructors.iterator(); iter.hasNext(); ) {
	            ClassInstructor classInstr = (ClassInstructor) iter.next();
	            frm.addToInstructors(classInstr);
	        }

	        if (instructors.isEmpty())
	        	frm.addToInstructors(null);
        }
    }

    /**
     * Update class data
     * @param request
     * @param frm
     */
    private void doUpdate(
            HttpServletRequest request,
            ClassEditForm frm,
            Class_ c,
            org.hibernate.Session hibSession ) throws Exception {

        c.setExpectedCapacity(frm.getExpectedCapacity());
        if (frm.getDatePattern()==null || frm.getDatePattern().intValue()<0)
        	c.setDatePattern(null);
        else
        	c.setDatePattern(new DatePatternDAO().get(frm.getDatePattern()));
        c.setNbrRooms(frm.getNbrRooms());
        c.setNotes(frm.getNotes());
	    c.setSchedulePrintNote(frm.getSchedulePrintNote());
	    //c.setClassSuffix(frm.getClassSuffix());
	    c.setMaxExpectedCapacity(frm.getMaxExpectedCapacity());
	    c.setRoomRatio(frm.getRoomRatio());

	    Boolean disb = frm.getDisplayInScheduleBook();
	    c.setDisplayInScheduleBook(disb==null ? new Boolean(false) : disb);

	    Boolean di = frm.getDisplayInstructor();
	    c.setDisplayInstructor(di==null ? new Boolean(false) : di);

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

        ChangeLog.addChange(
                hibSession,
                request,
                c,
                ChangeLog.Source.CLASS_EDIT,
                ChangeLog.Operation.UPDATE,
                c.getSchedulingSubpart().getInstrOfferingConfig().getControllingCourseOffering().getSubjectArea(),
                c.getManagingDept());
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

    /**
     * Add an instructor to the list (UI)
     * @param request
     * @param frm
     * @param errors
     */
    protected void addInstructor(
            HttpServletRequest request,
            ClassEditForm frm,
            ActionMessages errors ) {

        if(request.getParameter("instrListTypeAction")!=null
                && request.getParameter("instrListTypeAction").toString().length()>0)
            return;

        List lst = frm.getInstructors();
        if(frm.checkPrefs(lst)) {
            frm.addToInstructors(null);
            request.setAttribute(HASH_ATTR, HASH_INSTR_PREF);
        }
        else {
            errors.add("instrPrefs",
                       new ActionMessage(
                               "errors.generic",
                               "Invalid instructor preference: Check for duplicate / blank selection. ") );
            saveErrors(request, errors);
        }
    }

    /**
     * Deletes an instructor from the list (UI)
     * @param request
     * @param frm
     */
    protected void deleteInstructor(
            HttpServletRequest request,
            ClassEditForm frm) {

        int deleteId = -1;

        try {
            deleteId = Integer.parseInt(request.getParameter("deleteId"));
        }
        catch(Exception e) {
            deleteId = -1;
        }

        if(deleteId>=0) {
            frm.removeInstructor(deleteId);
            request.setAttribute(HASH_ATTR, HASH_INSTR_PREF);
        }
    }
}
