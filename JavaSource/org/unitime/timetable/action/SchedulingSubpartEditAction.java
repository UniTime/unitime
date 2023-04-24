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
import java.util.List;
import java.util.Set;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.tiles.annotation.TilesDefinition;
import org.apache.struts2.tiles.annotation.TilesPutAttribute;
import org.unitime.commons.Debug;
import org.unitime.timetable.defaults.ApplicationProperty;
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
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.VariableFixedCreditUnitConfig;
import org.unitime.timetable.model.VariableRangeCreditUnitConfig;
import org.unitime.timetable.model.dao.DatePatternDAO;
import org.unitime.timetable.model.dao.ItypeDescDAO;
import org.unitime.timetable.model.dao.SchedulingSubpartDAO;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.ComboBoxLookup;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.LookupTables;
import org.unitime.timetable.webutil.BackTracker;


/**
 * @author Tomas Muller, Zuzana Mullerova, Stephanie Schluttenhofer
 */
@Action(value = "schedulingSubpartEdit", results = {
		@Result(name = "editSchedulingSubpart", type = "tiles", location = "schedulingSubpartEdit.tiles"),
		@Result(name = "instructionalOfferingSearch", type = "redirect", location = "/instructionalOfferingSearch.action"),
		@Result(name = "addDistributionPrefs", type = "redirect", location = "/distributionPrefs.action", 
			params = { "subpartId", "${form.schedulingSubpartId}", "op", "${op}"}
		),
		@Result(name = "displaySubpartDetail", type = "redirect", location = "/schedulingSubpartDetail.action",
			params = { "ssuid", "${form.schedulingSubpartId}"}
		)
	})
@TilesDefinition(name = "schedulingSubpartEdit.tiles", extend = "baseLayout", putAttributes =  {
		@TilesPutAttribute(name = "title", value = "Edit Scheduling Subpart"),
		@TilesPutAttribute(name = "body", value = "/user/schedulingSubpartEdit.jsp"),
		@TilesPutAttribute(name = "showNavigation", value = "true")
	})
public class SchedulingSubpartEditAction extends PreferencesAction2<SchedulingSubpartEditForm> {
	private static final long serialVersionUID = -271792073108532899L;

	protected String subpartId = null;
	protected String op2 = null;

	public String getSsuid() { return subpartId; }
	public void setSsuid(String subpartId) { this.subpartId = subpartId; }
	public String getOp2() { return op2; }
	public void setOp2(String op2) { this.op2 = op2; }	
	
    public String execute() throws Exception {
    	if (form == null) {
    		form = new SchedulingSubpartEditForm();
    		form.reset();
    	}

		super.execute();

		if (subpartId == null && request.getAttribute("ssuid") != null)
			subpartId = (String)request.getAttribute("ssuid");

		if (op == null) op = form.getOp();
        if (op2 != null && !op2.isEmpty()) op = op2;

        // Read subpart id from form
        if (MSG.actionAddTimePreference().equals(op)
                || MSG.actionAddRoomPreference().equals(op)
                || MSG.actionAddBuildingPreference().equals(op)
                || MSG.actionAddRoomFeaturePreference().equals(op)
                || MSG.actionAddDistributionPreference().equals(op)
                || MSG.actionAddRoomGroupPreference().equals(op)
                || MSG.actionUpdatePreferences().equals(op)
                || MSG.actionAddDatePatternPreference().equals(op)
                || MSG.actionAddAttributePreference().equals(op)
                || MSG.actionAddInstructorPreference().equals(op)
                || MSG.actionClearSubpartPreferences().equals(op)
                || MSG.actionRemoveBuildingPreference().equals(op)
        		|| MSG.actionRemoveDistributionPreference().equals(op)
        		|| MSG.actionRemoveRoomFeaturePreference().equals(op)
        		|| MSG.actionRemoveRoomGroupPreference().equals(op)
        		|| MSG.actionRemoveRoomPreference().equals(op)
        		|| MSG.actionRemoveTimePattern().equals(op)
        		|| MSG.actionRemoveAttributePreference().equals(op)
        		|| MSG.actionRemoveInstructorPreference().equals(op)
                || MSG.actionBackToDetail().equals(op)
                || MSG.actionNextSubpart().equals(op)
                || MSG.actionPreviousSubpart().equals(op)
                || "updateDatePattern".equals(op)) {
            subpartId = form.getSchedulingSubpartId();
        }

        // Determine if initial load
        if (op==null || op.trim().isEmpty()) {
            op = "init";
        }

        sessionContext.checkPermission(subpartId, "SchedulingSubpart", Right.SchedulingSubpartEdit);

        boolean timeVertical = CommonValues.VerticalGrid.eq(sessionContext.getUser().getProperty(UserProperty.GridOrientation));

        Debug.debug("op: " + op);
        Debug.debug("subpart: " + subpartId);

        // Check subpart exists
        if (subpartId==null || subpartId.trim().isEmpty())
            throw new Exception (MSG.errorSubpartInfoNotSupplied());

        // If subpart id is not null - load subpart info
        SchedulingSubpartDAO sdao = SchedulingSubpartDAO.getInstance();
        SchedulingSubpart ss = sdao.get(Long.valueOf(subpartId));

        // Cancel - Go back to Instructional Offering Screen
        if (MSG.actionBackToDetail().equals(op)) {
            return "displaySubpartDetail";
        }

        // Clear all preferences
        if (MSG.actionClearSubpartPreferences().equals(op)) {

        	sessionContext.checkPermission(ss, Right.SchedulingSubpartEditClearPreferences);

            Set s = ss.getPreferences();
            super.doClear(s, Preference.Type.TIME, Preference.Type.ROOM, Preference.Type.ROOM_FEATURE, Preference.Type.ROOM_GROUP, Preference.Type.BUILDING, Preference.Type.DATE);
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

            return "displaySubpartDetail";
        }

        // Reset form for initial load
        if ("init".equals(op)) {
            form.reset();
            form.setAutoSpreadInTime(ss.isAutoSpreadInTime());
            form.setStudentAllowOverlap(ss.isStudentAllowOverlap());
        }

        // Load form attributes that are constant
        doLoad(ss, subpartId);

        if ("init".equals(op)) {
        	form.setDatePattern(ss.getDatePattern()==null?Long.valueOf(-1):ss.getDatePattern().getUniqueId());
        }

        // Update Preferences for Subpart
        if (MSG.actionUpdatePreferences().equals(op) || MSG.actionNextSubpart().equals(op) || MSG.actionPreviousSubpart().equals(op)) {
            // Validate input prefs
            form.validate(this);

            // No errors - Add to subpart and update
            if (!hasFieldErrors()) {
                this.doUpdate(ss, sdao, timeVertical);

	            if (MSG.actionNextSubpart().equals(op)) {
	            	response.sendRedirect(response.encodeURL("schedulingSubpartEdit.action?ssuid="+form.getNextId()));
	            	return null;
	            }

	            if (MSG.actionPreviousSubpart().equals(op)) {
	            	response.sendRedirect(response.encodeURL("schedulingSubpartEdit.action?ssuid="+form.getPreviousId()));
	            	return null;
	            }

	            return "displaySubpartDetail";
            }
        }

        // Initialize Preferences for initial load
		Set timePatterns = null;
		form.setAvailableTimePatterns(TimePattern.findApplicable(
        		sessionContext.getUser(),
        		ss.getMinutesPerWk(),
        		 (form.getDatePattern() < 0 ? (ss.canInheritParentPreferences() ? ss.getParentSubpart().effectiveDatePattern() : ss.getSession().getDefaultDatePatternNotNull()) : DatePatternDAO.getInstance().get(form.getDatePattern())),
        		ss.getInstrOfferingConfig().getDurationModel(),
        		false,
        		ss.getManagingDept()));

		if ("init".equals(op)) {
        	initPrefs(ss, null, true);
        	timePatterns = ss.getTimePatterns();
        	
        	DatePattern selectedDatePattern = ss.effectiveDatePattern();
			if (selectedDatePattern != null) {
				for (DatePattern dp: selectedDatePattern.findChildren()) {					
					if (!form.getDatePatternPrefs().contains(
							dp.getUniqueId().toString())) {
						form.addToDatePatternPrefs(dp.getUniqueId()
								.toString(), PreferenceLevel.PREF_LEVEL_NEUTRAL);
					}
				}
			}
        }
        
        if ("updateDatePattern".equals(op)) {        	
			initPrefs(ss, null, true);
			timePatterns = ss.getTimePatterns();
			form.getDatePatternPrefs().clear();
        	form.getDatePatternPrefLevels().clear();
			DatePattern selectedDatePattern = (form.getDatePattern() < 0 ? (ss.canInheritParentPreferences() ? ss.getParentSubpart().effectiveDatePattern() : ss.getSession().getDefaultDatePatternNotNull()) : DatePatternDAO.getInstance().get(form.getDatePattern()));
			if (selectedDatePattern != null) {
				for (DatePattern dp: selectedDatePattern.findChildren()) {
					boolean found = false;
					for (DatePatternPref dpp: (Set<DatePatternPref>)ss.getPreferences(DatePatternPref.class)) {
						if (dp.equals(dpp.getDatePattern())) {
							form.addToDatePatternPrefs(dp.getUniqueId().toString(), dpp.getPrefLevel().getUniqueId().toString());
							found = true;
						}
					}
					if (!found)
						form.addToDatePatternPrefs(dp.getUniqueId().toString(), PreferenceLevel.PREF_LEVEL_NEUTRAL);
				}
			}
		}
        
		// Process Preferences Action
		processPrefAction();

        // Generate Time Pattern Grids
		super.generateTimePatternGrids(ss,
				ss.getMinutesPerWk(),
        		ss.getInstrOfferingConfig().getDurationModel(),
        		(form.getDatePattern() < 0 ? (ss.canInheritParentPreferences() ? ss.getParentSubpart().effectiveDatePattern() : ss.getSession().getDefaultDatePatternNotNull()) : DatePatternDAO.getInstance().get(form.getDatePattern())),
				timePatterns, op, timeVertical, true, null);
		setupChildren(ss); // Date patterns allowed in the DDL for Date pattern preferences
		LookupTables.setupDatePatterns(request, sessionContext.getUser(), MSG.dropDefaultDatePattern(), (ss.canInheritParentPreferences() ? ss.getParentSubpart().effectiveDatePattern() : ss.getSession().getDefaultDatePatternNotNull()), ss.getManagingDept(), ss.effectiveDatePattern());

        LookupTables.setupRooms(request, ss);		 // Room Prefs
        LookupTables.setupBldgs(request, ss);		 // Building Prefs
        LookupTables.setupRoomFeatures(request, ss); // Preference Levels
        LookupTables.setupRoomGroups(request, ss);   // Room Groups
        LookupTables.setupInstructorAttributes(request, ss);   // Instructor Attributes
        LookupTables.setupCourseCreditFormats(request); // Course Credit Formats
        LookupTables.setupCourseCreditTypes(request); //Course Credit Types
        LookupTables.setupCourseCreditUnitTypes(request); //Course Credit Unit Types
        LookupTables.setupInstructors(request, sessionContext, ss.getInstrOfferingConfig().getInstructionalOffering().getControllingCourseOffering().getSubjectArea().getDepartment().getUniqueId());

        form.setAllowHardPrefs(sessionContext.hasPermission(ss, Right.CanUseHardRoomPrefs));

        BackTracker.markForBack(request,
        		"schedulingSubpartDetail.action?ssuid="+form.getSchedulingSubpartId(),
        		MSG.backSubpart(ss.getSchedulingSubpartLabel()),
        		true, false);

        return "editSchedulingSubpart";
    }

    /**
     * Loads the non-editable scheduling subpart info into the form
     */
    private void doLoad(SchedulingSubpart ss, String subpartId ) {

        CourseOffering co = ss.getInstrOfferingConfig().getInstructionalOffering().getControllingCourseOffering();

        // populate form
        InstrOfferingConfig ioc = ss.getInstrOfferingConfig();
        InstructionalOffering io = ioc.getInstructionalOffering();
        form.setInstrOfferingId(io.getUniqueId().toString());
        form.setSchedulingSubpartId(subpartId);
        if(form.getInstructionalType() == null)
        	form.setInstructionalType(ss.getItype().getItype().toString());
        String label = ss.getItype().getAbbv();
        if (io.hasMultipleConfigurations())
        	label += " [" + ioc.getName() + "]";
        form.setInstructionalTypeLabel(label);
        form.setUnlimitedEnroll(ioc.isUnlimitedEnrollment());
        form.setItypeBasic(ss.getItype()==null || ss.getItype().getBasic());
        List<ComboBoxLookup> itypes = new ArrayList<ComboBoxLookup>();
        for (ItypeDesc itype: ItypeDesc.findAll(form.getItypeBasic()))
        	itypes.add(new ComboBoxLookup(itype.getDesc(), itype.getItype().toString()));
        if (form.getItypeBasic())
        	itypes.add(new ComboBoxLookup(MSG.selectMoreOptions(), "more"));
        else
        	itypes.add(new ComboBoxLookup(MSG.selectLessOptions(), "less"));
        request.setAttribute("itypes", itypes);
        
        form.setSubjectArea(co.getSubjectAreaAbbv());
        form.setSubjectAreaId(co.getSubjectArea().getUniqueId().toString());
        form.setCourseNbr(co.getCourseNbr());
        form.setCourseTitle(co.getTitle());

    	if (ss.getParentSubpart() != null && ss.getItype().equals(ss.getParentSubpart().getItype())){
    		form.setSameItypeAsParent(Boolean.valueOf(true));
    	} else {
    		form.setSameItypeAsParent(Boolean.valueOf(false));
    	}

        if (form.getCreditFormat() == null){
	        if (ss.getCredit() != null){
	        	CourseCreditUnitConfig credit = ss.getCredit();
	        	form.setCreditText(credit.creditText());
	        	form.setCreditFormat(credit.getCreditFormat());
	        	form.setCreditType(credit.getCreditType().getUniqueId());
	        	form.setCreditUnitType(credit.getCreditUnitType().getUniqueId());
	        	if (credit instanceof FixedCreditUnitConfig){
	        		form.setUnits(((FixedCreditUnitConfig) credit).getFixedUnits());
	        	} else if (credit instanceof VariableFixedCreditUnitConfig){
	        		form.setUnits(((VariableFixedCreditUnitConfig) credit).getMinUnits());
	        		form.setMaxUnits(((VariableFixedCreditUnitConfig) credit).getMaxUnits());
	        		if (credit instanceof VariableRangeCreditUnitConfig){
	        			form.setFractionalIncrementsAllowed(((VariableRangeCreditUnitConfig) credit).isFractionalIncrementsAllowed());
	        		}
	        	}
	        }
        }

        SchedulingSubpart next = ss.getNextSchedulingSubpart(sessionContext, Right.SchedulingSubpartEdit);
        form.setNextId(next==null?null:next.getUniqueId().toString());
        SchedulingSubpart previous = ss.getPreviousSchedulingSubpart(sessionContext, Right.SchedulingSubpartEdit);
        form.setPreviousId(previous==null?null:previous.getUniqueId().toString());

        // Set Parent Subpart
        String parentSubpart = "";
        SchedulingSubpart parentSS = ss.getParentSubpart();
        form.setParentSubpartId(parentSS==null?null:parentSS.getUniqueId().toString());
        form.setParentSubpartLabel(parentSS==null?null:parentSS.getSchedulingSubpartLabel());

        while(parentSS!=null) {
            parentSubpart = parentSS.getItype().getAbbv() + " - " + parentSubpart;
            parentSS = parentSS.getParentSubpart();
        }
        form.setParentSubpart(parentSubpart);

        form.setManagingDeptName(ss.getManagingDept()==null?null:ss.getManagingDept().getManagingDeptLabel());
        form.setControllingDept(ss.getControllingDept().getUniqueId());
    	form.setDatePatternEditable(ApplicationProperty.WaitListCanChangeDatePattern.isTrue() || ss.getInstrOfferingConfig().getEnrollment() == 0 || !ss.getInstrOfferingConfig().getInstructionalOffering().effectiveReScheduleNow());
    }

    /**
     * Loads the non-editable scheduling subpart info into the form
     */
    private void doUpdate(SchedulingSubpart ss, SchedulingSubpartDAO sdao, boolean timeVertical) throws Exception {

        Set s = ss.getPreferences();

        // Clear all old prefs
        super.doClear(s, Preference.Type.TIME, Preference.Type.ROOM, Preference.Type.ROOM_FEATURE, Preference.Type.ROOM_GROUP, Preference.Type.BUILDING, Preference.Type.DATE);

        super.doUpdate(ss, s, timeVertical,
        		Preference.Type.TIME, Preference.Type.ROOM, Preference.Type.ROOM_FEATURE, Preference.Type.ROOM_GROUP, Preference.Type.BUILDING, Preference.Type.DATE);

        ss.setAutoSpreadInTime(form.getAutoSpreadInTime());
        ss.setStudentAllowOverlap(form.getStudentAllowOverlap());

        if (form.getDatePattern()==null || form.getDatePattern().intValue()<0)
        	ss.setDatePattern(null);
        else
        	ss.setDatePattern(DatePatternDAO.getInstance().get(form.getDatePattern()));
        
        if (form.getInstructionalType() == null || form.getInstructionalType().length() == 0){
        	// do nothing
        } else {
        	ItypeDesc newItype = ItypeDescDAO.getInstance().get(Integer.valueOf(form.getInstructionalType()));
        	if (newItype != null){
        		ss.setItype(newItype);
        	}
        }

        if (form.getCreditFormat() == null || form.getCreditFormat().length() == 0 || form.getCreditFormat().equals(Constants.BLANK_OPTION_VALUE)){
        	CourseCreditUnitConfig origConfig = ss.getCredit();
        	if (origConfig != null){
				ss.setCredit(null);
				sdao.getSession().delete(origConfig);
        	}
        } else {
         	if(ss.getCredit() != null){
        		CourseCreditUnitConfig ccuc = ss.getCredit();
        		if (ccuc.getCreditFormat().equals(form.getCreditFormat())){
        			boolean changed = false;
        			if (!ccuc.getCreditType().getUniqueId().equals(form.getCreditType())){
        				changed = true;
        			}
        			if (!ccuc.getCreditUnitType().getUniqueId().equals(form.getCreditUnitType())){
        				changed = true;
        			}
        			if (ccuc instanceof FixedCreditUnitConfig) {
						FixedCreditUnitConfig fcuc = (FixedCreditUnitConfig) ccuc;
						if (!fcuc.getFixedUnits().equals(form.getUnits())){
							changed = true;
						}
					} else if (ccuc instanceof VariableFixedCreditUnitConfig) {
						VariableFixedCreditUnitConfig vfcuc = (VariableFixedCreditUnitConfig) ccuc;
						if (!vfcuc.getMinUnits().equals(form.getUnits())){
							changed = true;
						}
						if (!vfcuc.getMaxUnits().equals(form.getMaxUnits())){
							changed = true;
						}
						if (vfcuc instanceof VariableRangeCreditUnitConfig) {
							VariableRangeCreditUnitConfig vrcuc = (VariableRangeCreditUnitConfig) vfcuc;
							if (!vrcuc.isFractionalIncrementsAllowed().equals(form.getFractionalIncrementsAllowed())){
								changed = true;
							}
						}
					}
        			if (changed){
        				CourseCreditUnitConfig origConfig = ss.getCredit();
            			ss.setCredit(null);
            			sdao.getSession().delete(origConfig);
            			ss.setCredit(CourseCreditUnitConfig.createCreditUnitConfigOfFormat(form.getCreditFormat(), form.getCreditType(), form.getCreditUnitType(), form.getUnits(), form.getMaxUnits(), form.getFractionalIncrementsAllowed(), Boolean.valueOf(false)));
            			ss.getCredit().setOwner(ss);
        			}
        		} else {
        			CourseCreditUnitConfig origConfig = ss.getCredit();
        			ss.setCredit(null);
        			sdao.getSession().delete(origConfig);
        			ss.setCredit(CourseCreditUnitConfig.createCreditUnitConfigOfFormat(form.getCreditFormat(), form.getCreditType(), form.getCreditUnitType(), form.getUnits(), form.getMaxUnits(), form.getFractionalIncrementsAllowed(), Boolean.valueOf(false)));
        			ss.getCredit().setOwner(ss);
        		}
        	} else {
    			ss.setCredit(CourseCreditUnitConfig.createCreditUnitConfigOfFormat(form.getCreditFormat(), form.getCreditType(), form.getCreditUnitType(), form.getUnits(), form.getMaxUnits(), form.getFractionalIncrementsAllowed(), Boolean.valueOf(false)));
    			ss.getCredit().setOwner(ss);
        	}
        }

        if (ss.getCredit() != null){
        	sdao.getSession().saveOrUpdate(ss.getCredit());
        }
        sdao.update(ss);
 
        String className = ApplicationProperty.ExternalActionSchedulingSubpartEdit.value();
    	if (className != null && className.trim().length() > 0){
        	ExternalSchedulingSubpartEditAction editAction = (ExternalSchedulingSubpartEditAction) (Class.forName(className).getDeclaredConstructor().newInstance());
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
    
    protected void setupChildren(SchedulingSubpart ss) {
		DatePattern selectedDatePattern = (form.getDatePattern() < 0 ? (ss.canInheritParentPreferences() ? ss.getParentSubpart().effectiveDatePattern() : ss.getSession().getDefaultDatePatternNotNull()) : DatePatternDAO.getInstance().get(form.getDatePattern()));
		try {
			if (selectedDatePattern != null) {
				List<DatePattern> v = selectedDatePattern.findChildren();
				request.setAttribute(DatePattern.DATE_PATTERN_CHILDREN_LIST_ATTR, v);	
				form.sortDatePatternPrefs(form.getDatePatternPrefs(), form.getDatePatternPrefLevels(), v);
			}
		} catch (Exception e) {e.printStackTrace();}
		
	}

}
