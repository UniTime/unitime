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

import java.io.StringWriter;
import java.util.Iterator;
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
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseCreditUnitConfig;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.SchedulingSubpartDAO;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.LookupTables;
import org.unitime.timetable.webutil.BackTracker;
import org.unitime.timetable.webutil.DistributionPrefsTableBuilder;
import org.unitime.timetable.webutil.WebClassListTableBuilder;


/**
 * @author Tomas Muller, Zuzana Mullerova
 */
@Action(value = "schedulingSubpartDetail", results = {
		@Result(name = "displaySchedulingSubpart", type = "tiles", location = "schedulingSubpartDetail.tiles"),
		@Result(name = "instructionalOfferingSearch", type = "redirect", location = "/instructionalOfferingSearch.do"),
		@Result(name = "addDistributionPrefs", type = "redirect", location = "/distributionPrefs.do",
			params = { "subpartId", "${form.schedulingSubpartId}", "op", "${op}"}
		)
	})
@TilesDefinition(name = "schedulingSubpartDetail.tiles", extend = "baseLayout", putAttributes =  {
		@TilesPutAttribute(name = "title", value = "Scheduling Subpart Detail"),
		@TilesPutAttribute(name = "body", value = "/user/schedulingSubpartDetail.jsp"),
		@TilesPutAttribute(name = "showNavigation", value = "true"),
		@TilesPutAttribute(name = "showSolverWarnings", value = "assignment")
	})
public class SchedulingSubpartDetailAction extends PreferencesAction2<SchedulingSubpartEditForm> {
	private static final long serialVersionUID = 4649492548245480462L;
	
	protected String subpartId = null;
	protected String op2 = null;

	public String getSsuid() { return subpartId; }
	public void setSsuid(String subpartId) { this.subpartId = subpartId; }
	public String getOp2() { return op2; }
	public void setOp2(String op2) { this.op2 = op2; }	

	public String execute() throws Exception {
		if (form == null) form = new SchedulingSubpartEditForm();
		
		super.execute();
		
		if (subpartId == null && request.getAttribute("ssuid") != null)
			subpartId = (String)request.getAttribute("ssuid");

		if (op == null) op = form.getOp();
        if (op2 != null && !op2.isEmpty()) op = op2;

        // Set common lookup tables
        super.execute();

        // Read subpart id from form
        if (MSG.actionEditSubpart().equals(op)
        		|| MSG.actionAddDistributionPreference().equals(op)
                || MSG.actionNextSubpart().equals(op)
                || MSG.actionPreviousSubpart().equals(op)
                || MSG.actionClearClassPreferencesOnSubpart().equals(op)
                || MSG.actionEditSubpartInstructorAssignmentPreferences().equals(op)) {
            subpartId = form.getSchedulingSubpartId();
        } else {
        	form.reset();
        }

        Debug.debug("op: " + op);
        Debug.debug("subpart: " + subpartId);

        // Check subpart exists
        if (subpartId==null || subpartId.trim().isEmpty())
            throw new Exception (MSG.errorSubpartInfoNotSupplied());
        
        sessionContext.checkPermission(subpartId, "SchedulingSubpart", Right.SchedulingSubpartDetail);

        boolean timeVertical = CommonValues.VerticalGrid.eq(sessionContext.getUser().getProperty(UserProperty.GridOrientation));

        // If subpart id is not null - load subpart info
        SchedulingSubpartDAO sdao = new SchedulingSubpartDAO();
        SchedulingSubpart ss = sdao.get(Long.valueOf(subpartId));

        // Edit Preference - Redirect to prefs edit screen
        if (MSG.actionEditSubpart().equals(op)) {
        	response.sendRedirect( response.encodeURL("schedulingSubpartEdit.action?ssuid="+ss.getUniqueId().toString()) );
        	return null;
        }

        if (MSG.actionEditSubpartInstructorAssignmentPreferences().equals(op)) {
        	response.sendRedirect( response.encodeURL("schedulingSubpartInstrAssgnEdit.do?ssuid="+ss.getUniqueId().toString()) );
        	return null;
        }

		// Add Distribution Preference - Redirect to dist prefs screen
	    if (MSG.actionAddDistributionPreference().equals(op)) {
        	sessionContext.checkPermission(ss, Right.DistributionPreferenceSubpart);
            return "addDistributionPrefs";
	    }

        if (MSG.actionNextSubpart().equals(op)) {
        	response.sendRedirect(response.encodeURL("schedulingSubpartDetail.action?ssuid="+form.getNextId()));
        	return null;
        }

        if (MSG.actionClearClassPreferencesOnSubpart().equals(op)) {
        	sessionContext.checkPermission(ss, Right.SchedulingSubpartDetailClearClassPreferences);

        	Class_DAO cdao = new Class_DAO();
        	for (Iterator i=ss.getClasses().iterator();i.hasNext();) {
        		Class_ c = (Class_)i.next();
        		c.getPreferences().clear();
        		cdao.saveOrUpdate(c);
        	}

            ChangeLog.addChange(
                    null,
                    sessionContext,
                    ss,
                    ChangeLog.Source.SCHEDULING_SUBPART_EDIT,
                    ChangeLog.Operation.CLEAR_ALL_PREF,
                    ss.getInstrOfferingConfig().getInstructionalOffering().getControllingCourseOffering().getSubjectArea(),
                    ss.getManagingDept());
        }

        if (MSG.actionPreviousSubpart().equals(op)) {
        	response.sendRedirect(response.encodeURL("schedulingSubpartDetail.action?ssuid="+form.getPreviousId()));
        	return null;
        }

        // Load form attributes that are constant
        doLoad(ss, subpartId);

        // Initialize Preferences for initial load
		Set timePatterns = null;
		form.setAvailableTimePatterns(TimePattern.findApplicable(
        		sessionContext.getUser(),
        		ss.getMinutesPerWk(),
        		ss.effectiveDatePattern(),
        		ss.getInstrOfferingConfig().getDurationModel(),
        		false,
        		ss.getManagingDept()));
		initPrefs(ss, null, false);
        timePatterns = ss.getTimePatterns();

	    // Display distribution Prefs
        DistributionPrefsTableBuilder tbl = new DistributionPrefsTableBuilder();
        String html = tbl.getDistPrefsTableForSchedulingSubpart(request, sessionContext, ss);
        if (html!=null)
        	request.setAttribute(DistributionPref.DIST_PREF_REQUEST_ATTR, html);

		// Process Preferences Action
		processPrefAction();
		setupDatePatterns(ss);

        // Generate Time Pattern Grids
		super.generateTimePatternGrids(ss,
				ss.getMinutesPerWk(),
        		ss.getInstrOfferingConfig().getDurationModel(),
        		ss.effectiveDatePattern(),
        		timePatterns, "init", timeVertical, false, null);

		LookupTables.setupDatePatterns(request, sessionContext.getUser(), MSG.dropDefaultDatePattern(), ss.getSession().getDefaultDatePatternNotNull(), ss.getManagingDept(), ss.effectiveDatePattern());

        LookupTables.setupRooms(request, ss);		 // Room Prefs
        LookupTables.setupBldgs(request, ss);		 // Building Prefs
        LookupTables.setupRoomFeatures(request, ss); // Preference Levels
        LookupTables.setupRoomGroups(request, ss);   // Room Groups
        LookupTables.setupInstructorAttributes(request, ss);   // Instructor Attributes
        LookupTables.setupInstructors(request, sessionContext, ss.getInstrOfferingConfig().getInstructionalOffering().getControllingCourseOffering().getSubjectArea().getDepartment().getUniqueId());

        BackTracker.markForBack(request,
        		"schedulingSubpartDetail.action?ssuid="+form.getSchedulingSubpartId(),
        		MSG.backSubpart(ss.getSchedulingSubpartLabel()),
        		true, false);
        
        StringWriter out = new StringWriter();
        WebClassListTableBuilder subpartClsTableBuilder = new WebClassListTableBuilder();
		subpartClsTableBuilder.setDisplayDistributionPrefs(false);
		subpartClsTableBuilder.setDisplayConflicts(true);
		subpartClsTableBuilder.setDisplayDatePatternDifferentWarning(true);
		subpartClsTableBuilder.htmlTableForSubpartClasses(
									sessionContext,
									getClassAssignmentService().getAssignment(),
									getExaminationSolverService().getSolver(),
				    		        Long.valueOf(form.getSchedulingSubpartId()), 
				    		        out,
				    		        request.getParameter("backType"),
				    		        request.getParameter("backId"));
		out.flush(); out.close();
		request.setAttribute("classTable", out.toString());

        return "displaySchedulingSubpart";
	}

    /**
     * Loads the non-editable scheduling subpart info into the form
     */
    private void doLoad(SchedulingSubpart ss, String subpartId) {

        CourseOffering co = ss.getInstrOfferingConfig().getInstructionalOffering().getControllingCourseOffering();

	    // Set Session Variables
        InstructionalOfferingSearchAction.setLastInstructionalOffering(sessionContext, ss.getInstrOfferingConfig().getInstructionalOffering());

        // populate form
        InstrOfferingConfig ioc = ss.getInstrOfferingConfig();
        InstructionalOffering io = ioc.getInstructionalOffering();
        form.setInstrOfferingId(io.getUniqueId().toString());
        form.setSchedulingSubpartId(subpartId);
        form.setInstructionalType(ss.getItype().getItype().toString());
        String label = ss.getItype().getAbbv();
        if (io.hasMultipleConfigurations())
        	label += " [" + ioc.getName() + "]";
        form.setInstructionalTypeLabel(label);
        form.setUnlimitedEnroll(ioc.isUnlimitedEnrollment());
        form.setItypeBasic(ss.getItype()==null || ss.getItype().getBasic());
        if (!form.getItypeBasic())
            LookupTables.setupItypes(request, false);
        form.setSubjectArea(co.getSubjectAreaAbbv());
        form.setSubjectAreaId(co.getSubjectArea().getUniqueId().toString());
        form.setCourseNbr(co.getCourseNbr());
        form.setCourseTitle(co.getTitle());
        form.setAutoSpreadInTime(ss.isAutoSpreadInTime());
        form.setStudentAllowOverlap(ss.isStudentAllowOverlap());
        form.setDatePattern(ss.getDatePattern()==null?Long.valueOf(-1):ss.getDatePattern().getUniqueId());
        form.setDatePatternEditable(ApplicationProperty.WaitListCanChangeDatePattern.isTrue() || ioc.getEnrollment() == 0 || !io.effectiveWaitList());
        if (form.getCreditText() == null || form.getCreditText().length() == 0){
	        if (ss.getCredit() != null){
	        	CourseCreditUnitConfig credit = ss.getCredit();
	        	form.setCreditText(credit.creditText());
	        }
        }
    	if (ss.getParentSubpart() != null && ss.getItype().equals(ss.getParentSubpart().getItype())){
    		form.setSameItypeAsParent(true);
    	} else {
    		form.setSameItypeAsParent(false);
    	}

        SchedulingSubpart next = ss.getNextSchedulingSubpart(sessionContext, Right.SchedulingSubpartDetail);
        form.setNextId(next==null?null:next.getUniqueId().toString());
        SchedulingSubpart previous = ss.getPreviousSchedulingSubpart(sessionContext, Right.SchedulingSubpartDetail);
        form.setPreviousId(previous==null?null:previous.getUniqueId().toString());


        // Set Parent Subpart
        String parentSubpart = "";
        SchedulingSubpart parentSS = ss.getParentSubpart();
        form.setParentSubpartId(parentSS == null || !sessionContext.hasPermission(parentSS, Right.SchedulingSubpartDetail) ? null : parentSS.getUniqueId().toString());
        form.setParentSubpartLabel(parentSS==null?null:parentSS.getSchedulingSubpartLabel());
        while(parentSS!=null) {
            parentSubpart = parentSS.getItype().getAbbv() + " - " + parentSubpart;
            parentSS = parentSS.getParentSubpart();
        }
        form.setParentSubpart(parentSubpart);

        form.setManagingDeptName(ss.getManagingDept()==null?null:ss.getManagingDept().getManagingDeptLabel());
        form.setControllingDept(ss.getControllingDept().getUniqueId());
    }
    
    private void setupDatePatterns(SchedulingSubpart ss) throws Exception {
    	DatePattern selectedDatePattern = ss.effectiveDatePattern();			
		if (selectedDatePattern != null) {
			List<DatePattern> children = selectedDatePattern.findChildren();
			for (DatePattern dp: children) {					
				if (!form.getDatePatternPrefs().contains(
						dp.getUniqueId().toString())) {
					form.addToDatePatternPrefs(dp.getUniqueId()
							.toString(), PreferenceLevel.PREF_LEVEL_NEUTRAL);
				}
			}
			form.sortDatePatternPrefs(form.getDatePatternPrefs(), form.getDatePatternPrefLevels(), children);
		}
		
	}
}

