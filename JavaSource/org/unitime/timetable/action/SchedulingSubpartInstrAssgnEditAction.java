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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.action.ActionRedirect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.commons.Debug;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.form.SchedulingSubpartEditForm;
import org.unitime.timetable.interfaces.ExternalSchedulingSubpartEditAction;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.CourseCreditUnitConfig;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.dao.SchedulingSubpartDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.util.LookupTables;
import org.unitime.timetable.webutil.BackTracker;


/**
 * @author Tomas Muller
 */
@Service("/schedulingSubpartInstrAssgnEdit")
public class SchedulingSubpartInstrAssgnEditAction extends PreferencesAction {

	@Autowired SessionContext sessionContext;
	
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
    	try {
    		super.execute(mapping, form, request, response);
    		
    		SchedulingSubpartEditForm frm = (SchedulingSubpartEditForm) form;
    		ActionMessages errors = new ActionMessages();
    		
    		// Read parameters
    		String subpartId = (request.getParameter("ssuid") == null ? request.getAttribute("ssuid") != null ? request.getAttribute("ssuid").toString() : null : request.getParameter("ssuid"));
    		String op = frm.getOp();
    		if (request.getParameter("op2") != null && !request.getParameter("op2").isEmpty())
    			op = request.getParameter("op2");

    		// Read subpart id from form
    		if (op.equals(MSG.actionUpdatePreferences())
                    || op.equals(MSG.actionAddAttributePreference())
                    || op.equals(MSG.actionAddInstructorPreference())
                    || op.equals(MSG.actionClearSubpartPreferences())
            		|| op.equals(MSG.actionRemoveAttributePreference())
            		|| op.equals(MSG.actionRemoveInstructorPreference())
                    || op.equals(MSG.actionBackToDetail())
                    || op.equals(MSG.actionNextClass())
                    || op.equals(MSG.actionPreviousClass())
                    || op.equals("updateInstructorAssignment")) {
    			subpartId = frm.getSchedulingSubpartId();
    		}

            // Determine if initial load
    		if (op == null || op.trim().isEmpty()) op = "init";

            // Check subpart exists
            if (subpartId==null || subpartId.trim().isEmpty()) {
    			if (BackTracker.doBack(request, response))
    				return null;
    			else
    				throw new Exception (MSG.errorSubpartInfoNotSupplied());
            }
            
            // If subpart id is not null - load subpart info
            SchedulingSubpartDAO sdao = new SchedulingSubpartDAO();
            SchedulingSubpart ss = sdao.get(new Long(subpartId));

            sessionContext.checkPermission(ss.getControllingDept(), Right.InstructorAssignmentPreferences);
            
    		// Cancel - Go back to Class Detail Screen
    		if (op.equals(MSG.actionBackToDetail())) {
                ActionRedirect redirect = new ActionRedirect(mapping.findForward("displaySubpartDetail"));
                redirect.addParameter("ssuid", subpartId);
    			return redirect;
    		}
    		
    		// Clear all preferences
    		if (op.equals(MSG.actionClearSubpartPreferences())) {
    			sessionContext.checkPermission(ss.getControllingDept(), Right.InstructorClearAssignmentPreferences);
    			doClear(ss.getPreferences(), Preference.Type.ATTRIBUTE, Preference.Type.INSTRUCTOR);
    			sdao.update(ss);

                ChangeLog.addChange(
                        null,
                        sessionContext,
                        ss,
                        ChangeLog.Source.SCHEDULING_SUBPART_EDIT,
                        ChangeLog.Operation.CLEAR_PREF,
                        ss.getInstrOfferingConfig().getInstructionalOffering().getControllingCourseOffering().getSubjectArea(),
                        ss.getManagingDept());

                ActionRedirect redirect = new ActionRedirect(mapping.findForward("displaySubpartDetail"));
                redirect.addParameter("ssuid", subpartId);
                return redirect;
            }
    		
    		// Reset form for initial load
    		if (op.equals("init")) {
                frm.reset(mapping, request);
            }
    		
    		// Load form attributes that are constant
    		doLoad(request, frm, ss, op);
    		
    		// Update Preferences for Subpart
    		if(op.equals(MSG.actionUpdatePreferences()) || op.equals(MSG.actionNextSubpart()) || op.equals(MSG.actionPreviousSubpart())) {
                // Validate input prefs
                errors = frm.validate(mapping, request);

                // No errors - Add to subpart and update
                if(errors.isEmpty()) {
                    doUpdate(request, frm, ss, sdao);

    	            if (op.equals(MSG.actionNextSubpart())) {
    	            	response.sendRedirect(response.encodeURL("schedulingSubpartInstrAssgnEdit.do?ssuid="+frm.getNextId()));
    	            	return null;
    	            }

    	            if (op.equals(MSG.actionPreviousSubpart())) {
    	            	response.sendRedirect(response.encodeURL("schedulingSubpartInstrAssgnEdit.do?ssuid="+frm.getPreviousId()));
    	            	return null;
    	            }

    	            ActionRedirect redirect = new ActionRedirect(mapping.findForward("displaySubpartDetail"));
    	            redirect.addParameter("ssuid", subpartId);
    	            return redirect;
                } else {
                    saveErrors(request, errors);
                }
    		}

            // Initialize Preferences for initial load
            if (op.equals("init")) {
            	initPrefs(frm, ss, null, true);
            }
            
            if (op.equals("updateInstructorAssignment")) {
            	initPrefs(frm, ss, null, true);
            }
            
            // Process Preferences Action
            processPrefAction(request, frm, errors);
            
            LookupTables.setupInstructorAttributes(request, ss);   // Instructor Attributes
            LookupTables.setupInstructors(request, sessionContext, ss.getInstrOfferingConfig().getInstructionalOffering().getControllingCourseOffering().getSubjectArea().getDepartment().getUniqueId());
            
            frm.setAllowHardPrefs(sessionContext.hasPermission(ss, Right.CanUseHardRoomPrefs));
            
            BackTracker.markForBack(request,
        		"schedulingSubpartDetail.do?ssuid="+frm.getSchedulingSubpartId(),
        		MSG.backSubpart(ss.getSchedulingSubpartLabel()),
        		true, false);
            
            return mapping.findForward("editSchedulingSubpart");
    	} catch (Exception e) {
    		Debug.error(e);
    		throw e;
    	}
    }


    private void doLoad(HttpServletRequest request, SchedulingSubpartEditForm frm, SchedulingSubpart ss, String op) {

        CourseOffering co = ss.getInstrOfferingConfig().getInstructionalOffering().getControllingCourseOffering();

        // populate form
        InstrOfferingConfig ioc = ss.getInstrOfferingConfig();
        InstructionalOffering io = ioc.getInstructionalOffering();
        frm.setInstrOfferingId(io.getUniqueId().toString());
        frm.setSchedulingSubpartId(ss.getUniqueId().toString());
        if(frm.getInstructionalType() == null)
        	frm.setInstructionalType(ss.getItype().getItype().toString());
        String label = ss.getItype().getAbbv();
        if (io.hasMultipleConfigurations())
        	label += " [" + ioc.getName() + "]";
        frm.setInstructionalTypeLabel(label);
        frm.setUnlimitedEnroll(ioc.isUnlimitedEnrollment());
        frm.setItypeBasic(ss.getItype()==null || ss.getItype().getBasic()==1);
        if (!frm.getItypeBasic())
            LookupTables.setupItypes(request, false);
        frm.setSubjectArea(co.getSubjectAreaAbbv());
        frm.setSubjectAreaId(co.getSubjectArea().getUniqueId().toString());
        frm.setCourseNbr(co.getCourseNbr());
        frm.setCourseTitle(co.getTitle());
        frm.setAutoSpreadInTime(ss.isAutoSpreadInTime());
        frm.setStudentAllowOverlap(ss.isStudentAllowOverlap());
        frm.setDatePattern(ss.getDatePattern()==null?new Long(-1):ss.getDatePattern().getUniqueId());

    	if (ss.getParentSubpart() != null && ss.getItype().equals(ss.getParentSubpart().getItype())){
    		frm.setSameItypeAsParent(new Boolean(true));
    	} else {
    		frm.setSameItypeAsParent(new Boolean(false));
    	}

    	if (ss.getCredit() != null) {
        	CourseCreditUnitConfig credit = ss.getCredit();
        	frm.setCreditText(credit.creditText());
        }
    	if (ss.getParentSubpart() != null && ss.getItype().equals(ss.getParentSubpart().getItype())){
    		frm.setSameItypeAsParent(new Boolean(true));
    	} else {
    		frm.setSameItypeAsParent(new Boolean(false));
    	}

        SchedulingSubpart next = ss.getNextSchedulingSubpart(sessionContext, Right.InstructorAssignmentPreferences);
        frm.setNextId(next==null?null:next.getUniqueId().toString());
        SchedulingSubpart previous = ss.getPreviousSchedulingSubpart(sessionContext, Right.InstructorAssignmentPreferences);
        frm.setPreviousId(previous==null?null:previous.getUniqueId().toString());

        // Set Parent Subpart
        String parentSubpart = "";
        SchedulingSubpart parentSS = ss.getParentSubpart();
        frm.setParentSubpartId(parentSS==null?null:parentSS.getUniqueId().toString());
        frm.setParentSubpartLabel(parentSS==null?null:parentSS.getSchedulingSubpartLabel());

        while(parentSS!=null) {
            parentSubpart = parentSS.getItype().getAbbv() + " - " + parentSubpart;
            parentSS = parentSS.getParentSubpart();
        }
        frm.setParentSubpart(parentSubpart);

        frm.setManagingDeptName(ss.getManagingDept()==null?null:ss.getManagingDept().getManagingDeptLabel());
        frm.setControllingDept(ss.getControllingDept().getUniqueId());
        
        if ("init".equals(op)) {
            frm.setInstructorAssignment(ss.isInstructorAssignmentNeeded());
	        frm.setTeachingLoad(ss.getTeachingLoad() == null ? "" : Formats.getNumberFormat("0.##").format(ss.getTeachingLoad()));
	        frm.setNbrInstructors(ss.isInstructorAssignmentNeeded() ? ss.getNbrInstructors().intValue() : 1);
        }
    }

    private void doUpdate(HttpServletRequest request, SchedulingSubpartEditForm frm, SchedulingSubpart ss, SchedulingSubpartDAO sdao) throws Exception {
    	doClear(ss.getPreferences(), Preference.Type.ATTRIBUTE, Preference.Type.INSTRUCTOR);
    	
    	doUpdate(request, frm, ss, ss.getPreferences(), false, Preference.Type.ATTRIBUTE, Preference.Type.INSTRUCTOR);

        try {
        	if (frm.getInstructorAssignment() && frm.getTeachingLoad() != null)
        		ss.setTeachingLoad(Formats.getNumberFormat("0.##").parse(frm.getTeachingLoad()).floatValue());
        	else
        		ss.setTeachingLoad(null);
        } catch (ParseException e) {
        	ss.setTeachingLoad(null);
        }
        ss.setNbrInstructors(frm.getInstructorAssignment() ? frm.getNbrInstructors() : 0);

        sdao.update(ss);
 
        String className = ApplicationProperty.ExternalActionSchedulingSubpartEdit.value();
    	if (className != null && className.trim().length() > 0){
        	ExternalSchedulingSubpartEditAction editAction = (ExternalSchedulingSubpartEditAction) (Class.forName(className).newInstance());
       		editAction.performExternalSchedulingSubpartEditAction(ss, sdao.getSession());
    	}

        ChangeLog.addChange(
                null,
                sessionContext,
                ss,
                ChangeLog.Source.SCHEDULING_SUBPART_EDIT,
                ChangeLog.Operation.UPDATE,
                ss.getInstrOfferingConfig().getControllingCourseOffering().getSubjectArea(),
                ss.getManagingDept());
    }
}
