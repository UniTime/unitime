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
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.util.MessageResources;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.commons.Debug;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.form.ClassEditForm;
import org.unitime.timetable.interfaces.ExternalClassEditAction;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.DatePatternPref;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.StudentAccomodation;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.comparators.InstructorComparator;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.DatePatternDAO;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.util.LookupTables;
import org.unitime.timetable.webutil.BackTracker;


/**
 * MyEclipse Struts
 * Creation date: 12-08-2005
 *
 * XDoclet definition:
 * @struts:action path="/classEdit" name="classEditForm" input="/user/classEdit.jsp" scope="request" validate="true"
 *
 * @author Tomas Muller, Zuzana Mullerova, Stephanie Schluttenhofer
 */
@Service("/classEdit")
public class ClassEditAction extends PreferencesAction {
	
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);

	@Autowired SessionContext sessionContext;
	
    // --------------------------------------------------------- Class Constants

    /** Anchor names **/
    public final String HASH_INSTRUCTORS = "Instructors";

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
        MessageResources rsc = getResources(request);
        ActionMessages errors = new ActionMessages();

        // Read parameters
        String classId =  request.getParameter("cid")==null
								? request.getAttribute("cid") !=null
								        ? request.getAttribute("cid").toString()
								        : null
								: request.getParameter("cid");

        String reloadCause = request.getParameter("reloadCause");
        String op = frm.getOp();
        if (request.getParameter("op2")!=null && request.getParameter("op2").length()>0)
        	op = request.getParameter("op2");

        // Read class id from form
        if(//op.equals(rsc.getMessage("button.reload"))||
                op.equals(MSG.actionAddTimePreference())
                || op.equals(MSG.actionAddRoomPreference())
                || op.equals(MSG.actionAddBuildingPreference())
                || op.equals(MSG.actionAddRoomFeaturePreference())
                || op.equals(MSG.actionAddDistributionPreference())
                || op.equals(MSG.actionAddInstructor())
                || op.equals(MSG.actionUpdatePreferences())
                || op.equals(MSG.actionAddDatePatternPreference())
                || op.equals(MSG.actionAddAttributePreference())
                || op.equals(MSG.actionAddInstructorPreference())
                || op.equals(rsc.getMessage("button.cancel"))
                || op.equals(MSG.actionClearClassPreferences())
                || op.equals(MSG.actionRemoveBuildingPreference())
        		|| op.equals(MSG.actionRemoveDistributionPreference())
        		|| op.equals(MSG.actionRemoveRoomFeaturePreference())
        		|| op.equals(MSG.actionRemoveRoomGroupPreference())
        		|| op.equals(MSG.actionRemoveRoomPreference())
        		|| op.equals(MSG.actionRemoveDatePatternPreference())
        		|| op.equals(MSG.actionRemoveTimePattern())
        		|| op.equals(MSG.actionRemoveInstructor())
        		|| op.equals(MSG.actionRemoveAttributePreference())
        		|| op.equals(MSG.actionRemoveInstructorPreference())
                || op.equals(rsc.getMessage("button.changeOwner"))
                || op.equals(MSG.actionAddRoomGroupPreference())
                || op.equals(MSG.actionBackToDetail())
                || op.equals(MSG.actionNextClass())
                || op.equals(MSG.actionPreviousClass())
                || op.equals("updateDatePattern")
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
            throw new Exception (MSG.errorNullOperationNotSupported());
        
        boolean timeVertical = CommonValues.VerticalGrid.eq(sessionContext.getUser().getProperty(UserProperty.GridOrientation));

        Debug.debug("op: " + op);
        Debug.debug("class: " + classId);
        Debug.debug("reload cause: " + reloadCause);

        // Check class exists
        if (classId==null || classId.trim().length()==0) {
           if (BackTracker.doBack(request, response))
        	   return null;
           else
        	   throw new Exception (MSG.errorClassInfoNotSupplied());
        }

        sessionContext.checkPermission(classId, "Class_", Right.ClassEdit);

        // Change Owner - Go back to Change Owner Screen
        if(op.equals(rsc.getMessage("button.changeOwner"))
                && classId!=null && classId.trim().length()!=0) {

            request.setAttribute("classId", classId);
            return mapping.findForward("changeClassOwner");
        }

        // Cancel - Go back to Class Detail Screen
        if(op.equals(MSG.actionBackToDetail()) && classId!=null && classId.trim().length()!=0 ) {
            ActionRedirect redirect = new ActionRedirect(mapping.findForward("displayClassDetail"));
            redirect.addParameter("cid", classId);
            return redirect;
        }

        // If class id is not null - load class info
        Class_DAO cdao = new Class_DAO();
        Class_ c = cdao.get(new Long(classId));

		// Add Distribution Preference - Redirect to dist prefs screen
	    if(op.equals(MSG.actionAddDistributionPreference())) {
        	sessionContext.checkPermission(c, Right.DistributionPreferenceClass);

	        SchedulingSubpart ss = c.getSchedulingSubpart();
	        CourseOffering cco = ss.getInstrOfferingConfig().getControllingCourseOffering();
	        request.setAttribute("subjectAreaId", cco.getSubjectArea().getUniqueId().toString());
	        request.setAttribute("schedSubpartId", ss.getUniqueId().toString());
	        request.setAttribute("courseOffrId", cco.getUniqueId().toString());
	        request.setAttribute("classId", c.getUniqueId().toString());
            return mapping.findForward("addDistributionPrefs");
	    }

	    // Add Instructor
	    if(op.equals(MSG.actionAddInstructor()))
            addInstructor(request, frm, errors);

	    // Delete Instructor
        if(op.equals(MSG.actionRemoveInstructor())
                && request.getParameter("deleteType")!=null
                && request.getParameter("deleteType").equals("instructor"))
            deleteInstructor(request, frm);

        // Restore all inherited preferences
        if(op.equals(MSG.actionClearClassPreferences())) {
        	sessionContext.checkPermission(c, Right.ClassEditClearPreferences);

            Set s = c.getPreferences();
            doClear(s, Preference.Type.TIME, Preference.Type.ROOM, Preference.Type.ROOM_FEATURE, Preference.Type.ROOM_GROUP, Preference.Type.BUILDING);
            c.setPreferences(s);
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
        if(op.equals("init")) {
            frm.reset(mapping, request);
        }

        // Load form attributes that are constant
        doLoad(request, frm, c, op);

        // Update Preferences for Class
        if(op.equals(MSG.actionUpdatePreferences()) || op.equals(MSG.actionNextClass()) || op.equals(MSG.actionPreviousClass())) {
            // Validate input prefs
            errors = frm.validate(mapping, request);

            // No errors - Add to class and update
            if(errors.size()==0) {

            	org.hibernate.Session hibSession = cdao.getSession();
            	Transaction tx = hibSession.beginTransaction();

            	try {
                    // Clear all old prefs
                    Set s = c.getPreferences();
                    doClear(s, Preference.Type.TIME, Preference.Type.ROOM, Preference.Type.ROOM_FEATURE, Preference.Type.ROOM_GROUP, Preference.Type.BUILDING);

                    // Save class data
                    doUpdate(request, frm, c, hibSession);

                    // Save Prefs
                    super.doUpdate(request, frm, c, s, timeVertical,
                    		Preference.Type.TIME, Preference.Type.ROOM, Preference.Type.ROOM_FEATURE, Preference.Type.ROOM_GROUP, Preference.Type.BUILDING);
                    
                    hibSession.saveOrUpdate(c);

    	            tx.commit();
    	            
                    String className = ApplicationProperty.ExternalActionClassEdit.value();
                	if (className != null && className.trim().length() > 0){
                    	ExternalClassEditAction editAction = (ExternalClassEditAction) (Class.forName(className).newInstance());
                   		editAction.performExternalClassEditAction(c, hibSession);
                	}

    	            if (op.equals(MSG.actionNextClass())) {
    	            	response.sendRedirect(response.encodeURL("classEdit.do?cid="+frm.getNextId()));
    	            	return null;
    	            }

    	            if (op.equals(MSG.actionPreviousClass())) {
    	            	response.sendRedirect(response.encodeURL("classEdit.do?cid="+frm.getPreviousId()));
    	            	return null;
    	            }

    	            ActionRedirect redirect = new ActionRedirect(mapping.findForward("displayClassDetail"));
    	            redirect.addParameter("cid", classId);
    	            return redirect;
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

        if (op.equals("updateDatePattern")) {        	
			initPrefs(frm, c, leadInstructors, true);
			frm.getDatePatternPrefs().clear();
        	frm.getDatePatternPrefLevels().clear();
			DatePattern selectedDatePattern = (frm.getDatePattern() < 0 ? c.getSchedulingSubpart().effectiveDatePattern() : DatePatternDAO.getInstance().get(frm.getDatePattern()));
			if (selectedDatePattern != null) {
				for (DatePattern dp: selectedDatePattern.findChildren()) {
					boolean found = false;
					for (DatePatternPref dpp: (Set<DatePatternPref>)c.getPreferences(DatePatternPref.class)) {
						if (dp.equals(dpp.getDatePattern())) {
							frm.addToDatePatternPrefs(dp.getUniqueId().toString(), dpp.getPrefLevel().getUniqueId().toString());
							found = true;
						}
					}
					if (!found)
						for (DatePatternPref dpp: (Set<DatePatternPref>)c.getSchedulingSubpart().getPreferences(DatePatternPref.class)) {
							if (dp.equals(dpp.getDatePattern())) {
								frm.addToDatePatternPrefs(dp.getUniqueId().toString(), dpp.getPrefLevel().getUniqueId().toString());
								found = true;
							}
						}
					if (!found)
						frm.addToDatePatternPrefs(dp.getUniqueId().toString(), PreferenceLevel.PREF_LEVEL_NEUTRAL);
				}
			}
		}
        
        // Initialize Preferences for initial load
        frm.setAvailableTimePatterns(TimePattern.findApplicable(
        		sessionContext.getUser(),
        		c.getSchedulingSubpart().getMinutesPerWk(),
        		(frm.getDatePattern() < 0 ? c.getSchedulingSubpart().effectiveDatePattern() : DatePatternDAO.getInstance().get(frm.getDatePattern())),
        		c.getSchedulingSubpart().getInstrOfferingConfig().getDurationModel(),
        		true,
        		c.getManagingDept()));
		Set timePatterns = null;
        if(op.equals("init")) {
        	initPrefs(frm, c, leadInstructors, true);
		    timePatterns = c.effectiveTimePatterns();
		    
		    DatePattern selectedDatePattern = c.effectiveDatePattern();
			
			if (selectedDatePattern != null) {
				for (DatePattern dp: selectedDatePattern.findChildren()) {					
					if (!frm.getDatePatternPrefs().contains(
							dp.getUniqueId().toString())) {
						frm.addToDatePatternPrefs(dp.getUniqueId().toString(), PreferenceLevel.PREF_LEVEL_NEUTRAL);
					}
				}
			}
		   
        }

		// Process Preferences Action
		processPrefAction(request, frm, errors);

        // Generate Time Pattern Grids
		super.generateTimePatternGrids(request, frm, c, 
				c.getSchedulingSubpart().getMinutesPerWk(),
        		c.getSchedulingSubpart().getInstrOfferingConfig().getDurationModel(),
        		(frm.getDatePattern() < 0 ? c.getSchedulingSubpart().effectiveDatePattern() : DatePatternDAO.getInstance().get(frm.getDatePattern())),
				timePatterns, op, timeVertical, true, leadInstructors);

		// Instructors
        setupInstructors(request, frm, c);
        setupChildren(frm, request, c); // Date patterns allowed in the DDL for Date pattern preferences
        LookupTables.setupDatePatterns(request, sessionContext.getUser(), "Default", c.getSchedulingSubpart().effectiveDatePattern(), c.getManagingDept(), c.effectiveDatePattern());

        LookupTables.setupRooms(request, c);		 // Rooms
        LookupTables.setupBldgs(request, c);		 // Buildings
        LookupTables.setupRoomFeatures(request, c); // Room Features
        LookupTables.setupRoomGroups(request, c);   // Room Groups

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
        frm.setCourseTitle(cco.getTitle());
        frm.setManagingDept(managingDept.getUniqueId());
        frm.setControllingDept(c.getControllingDept().getUniqueId());
        frm.setManagingDeptLabel(managingDept.getManagingDeptLabel());
        frm.setUnlimitedEnroll(c.getSchedulingSubpart().getInstrOfferingConfig().isUnlimitedEnrollment());
        frm.setAccommodation(StudentAccomodation.toHtml(StudentAccomodation.getAccommodations(c)));
        frm.setInstructorAssignmentDefault(c.getSchedulingSubpart().isInstructorAssignmentNeeded());
        frm.setTeachingLoadDefault(c.getSchedulingSubpart().getTeachingLoad() == null ? "" : Formats.getNumberFormat("0.##").format(c.getSchedulingSubpart().getTeachingLoad()));
        frm.setNbrInstructorsDefault(c.getSchedulingSubpart().isInstructorAssignmentNeeded() ? c.getSchedulingSubpart().getNbrInstructors() : 1);
        
        Class_ next = c.getNextClass(sessionContext, Right.ClassEdit);
        frm.setNextId(next==null?null:next.getUniqueId().toString());
        Class_ previous = c.getPreviousClass(sessionContext, Right.ClassEdit);
        frm.setPreviousId(previous==null?null:previous.getUniqueId().toString());
        frm.setMinRoomLimit(c.getMinRoomLimit());
        frm.setEnrollment(c.getEnrollment());
        frm.setInstructorAssignment(c.isInstructorAssignmentNeeded());
        frm.setTeachingLoad(c.effectiveTeachingLoad() == null ? "" : Formats.getNumberFormat("0.##").format(c.effectiveTeachingLoad()));
        frm.setNbrInstructors(c.isInstructorAssignmentNeeded() ? String.valueOf(c.effectiveNbrInstructors()) : "");

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
		    frm.setEnabledForStudentScheduling(c.isEnabledForStudentScheduling());
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
	    
	    Boolean disb = frm.getEnabledForStudentScheduling();
	    c.setEnabledForStudentScheduling(disb==null ? new Boolean(false) : disb);

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

        ChangeLog.addChange(
                hibSession,
                sessionContext,
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
        LookupTables.setupInstructors(request, sessionContext, c.getDepartmentForSubjectArea().getUniqueId());
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
            request.setAttribute(HASH_ATTR, HASH_INSTRUCTORS);
        }
        else {
            errors.add("instrPrefs",
                       new ActionMessage(
                               "errors.generic",
                               MSG.errorInvalidInstructors()) );
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
            request.setAttribute(HASH_ATTR, HASH_INSTRUCTORS);
        }
    }
    
    /**
     * This method is called to setup children of the selected date pattern of the class
     * or the selected date pattern of the scheduling subpart of the current class.
     * 
     * @param frm ClassEditForm
     * @param request HttpServletRequest
     * @param c Class_
     * @throws Exception 
     */
	protected void setupChildren(ClassEditForm frm, HttpServletRequest request, Class_ c) throws Exception {
		DatePattern selectedDatePattern = (frm.getDatePattern() < 0 ? c.getSchedulingSubpart().effectiveDatePattern() : DatePatternDAO.getInstance().get(frm.getDatePattern()));
		if (selectedDatePattern != null) {
			List<DatePattern> v =  selectedDatePattern.findChildren();
			request.setAttribute(DatePattern.DATE_PATTERN_CHILDREN_LIST_ATTR, v);	
			frm.sortDatePatternPrefs(frm.getDatePatternPrefs(), frm.getDatePatternPrefLevels(), v);
		}
	}
}
