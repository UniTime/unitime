/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.action;

import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.util.MessageResources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.commons.Debug;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.form.SchedulingSubpartEditForm;
import org.unitime.timetable.interfaces.ExternalSchedulingSubpartEditAction;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.CourseCreditUnitConfig;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.DatePatternPref;
import org.unitime.timetable.model.FixedCreditUnitConfig;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.ItypeDesc;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.VariableFixedCreditUnitConfig;
import org.unitime.timetable.model.VariableRangeCreditUnitConfig;
import org.unitime.timetable.model.dao.DatePatternDAO;
import org.unitime.timetable.model.dao.ItypeDescDAO;
import org.unitime.timetable.model.dao.SchedulingSubpartDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.LookupTables;
import org.unitime.timetable.webutil.BackTracker;


/**
 * MyEclipse Struts
 * Creation date: 07-26-2005
 *
 * XDoclet definition:
 * @struts:action path="/schedulingSubpartEdit" name="schedulingSubpartEditForm" input="/user/schedulingSubpartEdit.jsp" scope="request"
 */
@Service("/schedulingSubpartEdit")
public class SchedulingSubpartEditAction extends PreferencesAction {

	@Autowired SessionContext sessionContext;
	
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

        SchedulingSubpartEditForm frm = (SchedulingSubpartEditForm) form;
        MessageResources rsc = getResources(request);
        ActionMessages errors = new ActionMessages();

        // Read parameters
        String subpartId = request.getParameter("ssuid")==null
        					? request.getAttribute("ssuid") !=null
        					        ? request.getAttribute("ssuid").toString()
        					        : null
        					: request.getParameter("ssuid");

        String reloadCause = request.getParameter("reloadCause");
        String op = frm.getOp();
        if (request.getParameter("op2")!=null && request.getParameter("op2").length()>0)
        	op = request.getParameter("op2");

        // Read subpart id from form
        if(		//op.equals(rsc.getMessage("button.reload"))
        		//	|| 
        	op.equals(MSG.actionAddTimePreference())
                || op.equals(MSG.actionAddRoomPreference())
                || op.equals(MSG.actionAddBuildingPreference())
                || op.equals(MSG.actionAddRoomFeaturePreference())
                || op.equals(MSG.actionAddDistributionPreference())
                || op.equals(MSG.actionAddRoomGroupPreference())
                || op.equals(MSG.actionUpdatePreferences())
                || op.equals(MSG.actionAddDatePatternPreference())
               // || op.equals(rsc.getMessage("button.cancel"))
                || op.equals(MSG.actionClearSubpartPreferences())
                || op.equals(MSG.actionRemoveBuildingPreference())
        		|| op.equals(MSG.actionRemoveDistributionPreference())
        		|| op.equals(MSG.actionRemoveRoomFeaturePreference())
        		|| op.equals(MSG.actionRemoveRoomGroupPreference())
        		|| op.equals(MSG.actionRemoveRoomPreference())
        		|| op.equals(MSG.actionRemoveTimePattern())
                || op.equals(MSG.actionBackToDetail())
               // || op.equals(rsc.getMessage("button.addClass_"))
                || op.equals(MSG.actionNextSubpart())
                || op.equals(MSG.actionPreviousSubpart())
                 || op.equals("updateDatePattern")) {
            subpartId = frm.getSchedulingSubpartId();
        }

        // Determine if initial load
        if(op==null || op.trim().length()==0
                || ( op.equals(rsc.getMessage("button.reload"))
                	 && (reloadCause==null || reloadCause.trim().length()==0) )) {
            op = "init";
        }

        // Check op exists
        if(op==null || op.trim()=="")
            throw new Exception (MSG.errorNullOperationNotSupported());
        
        sessionContext.checkPermission(subpartId, "SchedulingSubpart", Right.SchedulingSubpartEdit);

        boolean timeVertical = CommonValues.VerticalGrid.eq(sessionContext.getUser().getProperty(UserProperty.GridOrientation));

        Debug.debug("op: " + op);
        Debug.debug("subpart: " + subpartId);
        Debug.debug("reload cause: " + reloadCause);

        // Check subpart exists
        if(subpartId==null || subpartId.trim()=="")
            throw new Exception (MSG.errorSubpartInfoNotSupplied());

        // If subpart id is not null - load subpart info
        SchedulingSubpartDAO sdao = new SchedulingSubpartDAO();
        SchedulingSubpart ss = sdao.get(new Long(subpartId));

        // Cancel - Go back to Instructional Offering Screen
        if(op.equals(MSG.actionBackToDetail())
                && subpartId!=null && subpartId.trim()!="") {

            ActionRedirect redirect = new ActionRedirect(mapping.findForward("displaySubpartDetail"));
            redirect.addParameter("ssuid", subpartId);
            return redirect;
        }

        // Clear all preferences
        if(op.equals(MSG.actionClearSubpartPreferences())) {

        	sessionContext.checkPermission(ss, Right.SchedulingSubpartEditClearPreferences);

            Set s = ss.getPreferences();
            s.clear();
            ss.setPreferences(s);
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
        if(op.equals("init")) {
            frm.reset(mapping, request);
            frm.setAutoSpreadInTime(ss.isAutoSpreadInTime());
            frm.setStudentAllowOverlap(ss.isStudentAllowOverlap());
        }

        // Load form attributes that are constant
        doLoad(request, frm, ss, subpartId);

        if (op.equals("init")) {
        	frm.setDatePattern(ss.getDatePattern()==null?new Long(-1):ss.getDatePattern().getUniqueId());
        }

        // Update Preferences for Subpart
        if(op.equals(MSG.actionUpdatePreferences()) || op.equals(MSG.actionNextSubpart()) || op.equals(MSG.actionPreviousSubpart())) {
            // Validate input prefs
            errors = frm.validate(mapping, request);

            // No errors - Add to subpart and update
            if(errors.size()==0) {
                this.doUpdate(request, frm, ss, sdao, timeVertical);

	            if (op.equals(MSG.actionNextSubpart())) {
	            	response.sendRedirect(response.encodeURL("schedulingSubpartEdit.do?ssuid="+frm.getNextId()));
	            	return null;
	            }

	            if (op.equals(MSG.actionPreviousSubpart())) {
	            	response.sendRedirect(response.encodeURL("schedulingSubpartEdit.do?ssuid="+frm.getPreviousId()));
	            	return null;
	            }

	            ActionRedirect redirect = new ActionRedirect(mapping.findForward("displaySubpartDetail"));
	            redirect.addParameter("ssuid", subpartId);
	            return redirect;
            }
            else {
                saveErrors(request, errors);
            }
        }

        // Initialize Preferences for initial load
		Set timePatterns = null;
		frm.setAvailableTimePatterns(TimePattern.findApplicable(sessionContext.getUser(),ss.getMinutesPerWk().intValue(),false,ss.getManagingDept()));
        if(op.equals("init")) {
        	initPrefs(frm, ss, null, true);
        	timePatterns = ss.getTimePatterns();
        	
        	DatePattern selectedDatePattern = ss.effectiveDatePattern();
			if (selectedDatePattern != null) {
				for (DatePattern dp: selectedDatePattern.findChildren()) {					
					if (!frm.getDatePatternPrefs().contains(
							dp.getUniqueId().toString())) {
						frm.addToDatePatternPrefs(dp.getUniqueId()
								.toString(), PreferenceLevel.PREF_LEVEL_NEUTRAL);
					}
				}
			}
        }
        
        if (op.equals("updateDatePattern")) {        	
			initPrefs(frm, ss, null, true);
			timePatterns = ss.getTimePatterns();
			frm.getDatePatternPrefs().clear();
        	frm.getDatePatternPrefLevels().clear();
			DatePattern selectedDatePattern = (frm.getDatePattern() < 0 ? ss.effectiveDatePattern() : DatePatternDAO.getInstance().get(frm.getDatePattern()));
			if (selectedDatePattern != null) {
				for (DatePattern dp: selectedDatePattern.findChildren()) {
					boolean found = false;
					for (DatePatternPref dpp: (Set<DatePatternPref>)ss.getPreferences(DatePatternPref.class)) {
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

		// Process Preferences Action
		processPrefAction(request, frm, errors);

        // Generate Time Pattern Grids
		super.generateTimePatternGrids(request, frm, ss, timePatterns, op, timeVertical, true, null);
		setupChildren(frm, request, ss); // Date patterns allowed in the DDL for Date pattern preferences
		LookupTables.setupDatePatterns(request, sessionContext.getUser(), "Default", ss.getSession().getDefaultDatePatternNotNull(), ss.getManagingDept(), ss.effectiveDatePattern());

        LookupTables.setupRooms(request, ss);		 // Room Prefs
        LookupTables.setupBldgs(request, ss);		 // Building Prefs
        LookupTables.setupRoomFeatures(request, ss); // Preference Levels
        LookupTables.setupRoomGroups(request, ss);   // Room Groups
        LookupTables.setupCourseCreditFormats(request); // Course Credit Formats
        LookupTables.setupCourseCreditTypes(request); //Course Credit Types
        LookupTables.setupCourseCreditUnitTypes(request); //Course Credit Unit Types

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

    /**
     * Loads the non-editable scheduling subpart info into the form
     * @param request
     * @param frm
     * @param ss
     * @param subpartId
     */
    private void doLoad(
            HttpServletRequest request,
            SchedulingSubpartEditForm frm,
            SchedulingSubpart ss,
            String subpartId ) {

        CourseOffering co = ss.getInstrOfferingConfig().getInstructionalOffering().getControllingCourseOffering();

        // populate form
        InstrOfferingConfig ioc = ss.getInstrOfferingConfig();
        InstructionalOffering io = ioc.getInstructionalOffering();
        frm.setInstrOfferingId(io.getUniqueId().toString());
        frm.setSchedulingSubpartId(subpartId);
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

    	if (ss.getParentSubpart() != null && ss.getItype().equals(ss.getParentSubpart().getItype())){
    		frm.setSameItypeAsParent(new Boolean(true));
    	} else {
    		frm.setSameItypeAsParent(new Boolean(false));
    	}

        if (frm.getCreditFormat() == null){
	        if (ss.getCredit() != null){
	        	CourseCreditUnitConfig credit = ss.getCredit();
	        	frm.setCreditText(credit.creditText());
	        	frm.setCreditFormat(credit.getCreditFormat());
	        	frm.setCreditType(credit.getCreditType().getUniqueId());
	        	frm.setCreditUnitType(credit.getCreditUnitType().getUniqueId());
	        	if (credit instanceof FixedCreditUnitConfig){
	        		frm.setUnits(((FixedCreditUnitConfig) credit).getFixedUnits());
	        	} else if (credit instanceof VariableFixedCreditUnitConfig){
	        		frm.setUnits(((VariableFixedCreditUnitConfig) credit).getMinUnits());
	        		frm.setMaxUnits(((VariableFixedCreditUnitConfig) credit).getMaxUnits());
	        		if (credit instanceof VariableRangeCreditUnitConfig){
	        			frm.setFractionalIncrementsAllowed(((VariableRangeCreditUnitConfig) credit).isFractionalIncrementsAllowed());
	        		}
	        	}
	        }
        }

        SchedulingSubpart next = ss.getNextSchedulingSubpart(sessionContext, Right.SchedulingSubpartEdit);
        frm.setNextId(next==null?null:next.getUniqueId().toString());
        SchedulingSubpart previous = ss.getPreviousSchedulingSubpart(sessionContext, Right.SchedulingSubpartEdit);
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
    }
    /**
     * Loads the non-editable scheduling subpart info into the form
     * @param request
     * @param frm
     * @param ss
     * @param subpartId
     * @throws Exception
     */
    private void doUpdate(
            HttpServletRequest request,
            SchedulingSubpartEditForm frm,
            SchedulingSubpart ss,
            SchedulingSubpartDAO sdao,
            boolean timeVertical) throws Exception {

        Set s = ss.getPreferences();

        // Clear all old prefs
        s.clear();

        super.doUpdate(request, frm, ss, s, timeVertical);

        ss.setAutoSpreadInTime(frm.getAutoSpreadInTime());
        ss.setStudentAllowOverlap(frm.getStudentAllowOverlap());

        if (frm.getDatePattern()==null || frm.getDatePattern().intValue()<0)
        	ss.setDatePattern(null);
        else
        	ss.setDatePattern(new DatePatternDAO().get(frm.getDatePattern()));
        
        if (frm.getInstructionalType() == null || frm.getInstructionalType().length() == 0){
        	// do nothing
        } else {
        	ItypeDesc newItype = new ItypeDescDAO().get(new Integer(frm.getInstructionalType()));
        	if (newItype != null){
        		ss.setItype(newItype);
        	}
        }

        if (frm.getCreditFormat() == null || frm.getCreditFormat().length() == 0 || frm.getCreditFormat().equals(Constants.BLANK_OPTION_VALUE)){
        	CourseCreditUnitConfig origConfig = ss.getCredit();
        	if (origConfig != null){
				ss.setCredit(null);
				sdao.getSession().delete(origConfig);
        	}
        } else {
         	if(ss.getCredit() != null){
        		CourseCreditUnitConfig ccuc = ss.getCredit();
        		if (ccuc.getCreditFormat().equals(frm.getCreditFormat())){
        			boolean changed = false;
        			if (!ccuc.getCreditType().getUniqueId().equals(frm.getCreditType())){
        				changed = true;
        			}
        			if (!ccuc.getCreditUnitType().getUniqueId().equals(frm.getCreditUnitType())){
        				changed = true;
        			}
        			if (ccuc instanceof FixedCreditUnitConfig) {
						FixedCreditUnitConfig fcuc = (FixedCreditUnitConfig) ccuc;
						if (!fcuc.getFixedUnits().equals(frm.getUnits())){
							changed = true;
						}
					} else if (ccuc instanceof VariableFixedCreditUnitConfig) {
						VariableFixedCreditUnitConfig vfcuc = (VariableFixedCreditUnitConfig) ccuc;
						if (!vfcuc.getMinUnits().equals(frm.getUnits())){
							changed = true;
						}
						if (!vfcuc.getMaxUnits().equals(frm.getMaxUnits())){
							changed = true;
						}
						if (vfcuc instanceof VariableRangeCreditUnitConfig) {
							VariableRangeCreditUnitConfig vrcuc = (VariableRangeCreditUnitConfig) vfcuc;
							if (!vrcuc.isFractionalIncrementsAllowed().equals(frm.getFractionalIncrementsAllowed())){
								changed = true;
							}
						}
					}
        			if (changed){
        				CourseCreditUnitConfig origConfig = ss.getCredit();
            			ss.setCredit(null);
            			sdao.getSession().delete(origConfig);
            			ss.setCredit(CourseCreditUnitConfig.createCreditUnitConfigOfFormat(frm.getCreditFormat(), frm.getCreditType(), frm.getCreditUnitType(), frm.getUnits(), frm.getMaxUnits(), frm.getFractionalIncrementsAllowed(), new Boolean(false)));
            			ss.getCredit().setOwner(ss);
        			}
        		} else {
        			CourseCreditUnitConfig origConfig = ss.getCredit();
        			ss.setCredit(null);
        			sdao.getSession().delete(origConfig);
        			ss.setCredit(CourseCreditUnitConfig.createCreditUnitConfigOfFormat(frm.getCreditFormat(), frm.getCreditType(), frm.getCreditUnitType(), frm.getUnits(), frm.getMaxUnits(), frm.getFractionalIncrementsAllowed(), new Boolean(false)));
        			ss.getCredit().setOwner(ss);
        		}
        	} else {
    			ss.setCredit(CourseCreditUnitConfig.createCreditUnitConfigOfFormat(frm.getCreditFormat(), frm.getCreditType(), frm.getCreditUnitType(), frm.getUnits(), frm.getMaxUnits(), frm.getFractionalIncrementsAllowed(), new Boolean(false)));
    			ss.getCredit().setOwner(ss);
        	}
        }

        if (ss.getCredit() != null){
        	sdao.getSession().saveOrUpdate(ss.getCredit());
        }
        sdao.update(ss);
 
        String className = ApplicationProperties.getProperty("tmtbl.external.sched_subpart.edit_action.class");
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
    
    protected void setupChildren(SchedulingSubpartEditForm frm, HttpServletRequest request, SchedulingSubpart ss) {
		DatePattern selectedDatePattern = (frm.getDatePattern() < 0 ? ss.effectiveDatePattern() : DatePatternDAO.getInstance().get(frm.getDatePattern()));
		try {
			if (selectedDatePattern != null) {
				List<DatePattern> v = selectedDatePattern.findChildren();
				request.setAttribute(DatePattern.DATE_PATTERN_CHILDREN_LIST_ATTR, v);	
				frm.sortDatePatternPrefs(frm.getDatePatternPrefs(), frm.getDatePatternPrefLevels(), v);
			}
		} catch (Exception e) {e.printStackTrace();}
		
	}

}
