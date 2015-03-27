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

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.commons.Debug;
import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.form.DistributionPrefsForm;
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
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.LookupTables;
import org.unitime.timetable.webutil.BackTracker;
import org.unitime.timetable.webutil.DistributionPrefsTableBuilder;


/**
 * MyEclipse Struts
 * Creation date: 03-24-2006
 *
 * XDoclet definition:
 * @struts.action path="/schedulingSubpartDetail" name="schedulingSubpartEditForm" input="schedulingSubpartDetailTile" scope="request" validate="true"
 * @struts.action-forward name="instructionalOfferingSearch" path="/instructionalOfferingSearch.do"
 * @struts.action-forward name="displaySchedulingSubpart" path="schedulingSubpartDetailTile"
 * @struts.action-forward name="addDistributionPrefs" path="/distributionPrefs.do"
 *
 * @author Tomas Muller, Zuzana Mullerova
 */
@Service("/schedulingSubpartDetail")
public class SchedulingSubpartDetailAction extends PreferencesAction {
	
	@Autowired SessionContext sessionContext;

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

	        SchedulingSubpartEditForm frm = (SchedulingSubpartEditForm) form;
	        ActionMessages errors = new ActionMessages();

	        // Read parameters
	        String subpartId = request.getParameter("ssuid")==null
									? request.getAttribute("ssuid") !=null
									        ? request.getAttribute("ssuid").toString()
									        : null
									: request.getParameter("ssuid");

	        String op = frm.getOp();
	        
	        // Check op exists
	        if(op==null)
	            throw new Exception (MSG.errorNullOperationNotSupported());

	        // Read subpart id from form
	        if(op.equals(MSG.actionEditSubpart())
	        		|| op.equals(MSG.actionAddDistributionPreference())
	                // || op.equals(rsc.getMessage("button.backToInstrOffrDet")) for deletion
	                || op.equals(MSG.actionNextSubpart())
	                || op.equals(MSG.actionPreviousSubpart())
	                || op.equals(MSG.actionClearClassPreferencesOnSubpart())) {
	            subpartId = frm.getSchedulingSubpartId();
	        } else {
	        	frm.reset(mapping, request);
	        }

	        Debug.debug("op: " + op);
	        Debug.debug("subpart: " + subpartId);

	        // Check subpart exists
	        if(subpartId==null || subpartId.trim()=="")
	            throw new Exception (MSG.errorSubpartInfoNotSupplied());
	        
	        sessionContext.checkPermission(subpartId, "SchedulingSubpart", Right.SchedulingSubpartDetail);

	        boolean timeVertical = CommonValues.VerticalGrid.eq(sessionContext.getUser().getProperty(UserProperty.GridOrientation));

	        // If subpart id is not null - load subpart info
	        SchedulingSubpartDAO sdao = new SchedulingSubpartDAO();
	        SchedulingSubpart ss = sdao.get(new Long(subpartId));

	        // Edit Preference - Redirect to prefs edit screen
	        if(op.equals(MSG.actionEditSubpart())
	                && subpartId!=null && subpartId.trim()!="") {
	        	response.sendRedirect( response.encodeURL("schedulingSubpartEdit.do?ssuid="+ss.getUniqueId().toString()) );
	        	return null;
	        }

			// Add Distribution Preference - Redirect to dist prefs screen
		    if(op.equals(MSG.actionAddDistributionPreference())) {

            	sessionContext.checkPermission(ss, Right.DistributionPreferenceSubpart);

            	CourseOffering cco = ss.getInstrOfferingConfig().getControllingCourseOffering();
		        request.setAttribute("subjectAreaId", cco.getSubjectArea().getUniqueId().toString());
		        request.setAttribute("schedSubpartId", subpartId);
		        request.setAttribute("courseOffrId", cco.getUniqueId().toString());
		        request.setAttribute("classId", DistributionPrefsForm.ALL_CLASSES_SELECT);
	            return mapping.findForward("addDistributionPrefs");
		    }

            if (op.equals(MSG.actionNextSubpart())) {
            	response.sendRedirect(response.encodeURL("schedulingSubpartDetail.do?ssuid="+frm.getNextId()));
            	return null;
            }

            if (op.equals(MSG.actionClearClassPreferencesOnSubpart()) && "y".equals(request.getParameter("confirm"))) {

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

            if (op.equals(MSG.actionPreviousSubpart())) {
            	response.sendRedirect(response.encodeURL("schedulingSubpartDetail.do?ssuid="+frm.getPreviousId()));
            	return null;
            }

	        // Load form attributes that are constant
	        doLoad(request, frm, ss, subpartId);

	        // Initialize Preferences for initial load
			Set timePatterns = null;
			frm.setAvailableTimePatterns(TimePattern.findApplicable(
	        		sessionContext.getUser(),
	        		ss.getMinutesPerWk(),
	        		ss.effectiveDatePattern(),
	        		ss.getInstrOfferingConfig().getDurationModel(),
	        		true,
	        		ss.getManagingDept()));
			initPrefs(frm, ss, null, false);
	        timePatterns = ss.getTimePatterns();

		    // Display distribution Prefs
	        DistributionPrefsTableBuilder tbl = new DistributionPrefsTableBuilder();
	        String html = tbl.getDistPrefsTableForSchedulingSubpart(request, sessionContext, ss);
	        if (html!=null)
	        	request.setAttribute(DistributionPref.DIST_PREF_REQUEST_ATTR, html);

			// Process Preferences Action
			processPrefAction(request, frm, errors);
			setupDatePatterns(request, frm, ss);

	        // Generate Time Pattern Grids
			super.generateTimePatternGrids(request, frm, ss,
					ss.getMinutesPerWk(),
	        		ss.getInstrOfferingConfig().getDurationModel(),
	        		ss.effectiveDatePattern(),
	        		timePatterns, "init", timeVertical, false, null);

			LookupTables.setupDatePatterns(request, sessionContext.getUser(), "Default", ss.getSession().getDefaultDatePatternNotNull(), ss.getManagingDept(), ss.effectiveDatePattern());

	        LookupTables.setupRooms(request, ss);		 // Room Prefs
	        LookupTables.setupBldgs(request, ss);		 // Building Prefs
	        LookupTables.setupRoomFeatures(request, ss); // Preference Levels
	        LookupTables.setupRoomGroups(request, ss);   // Room Groups

	        BackTracker.markForBack(request,
	        		"schedulingSubpartDetail.do?ssuid="+frm.getSchedulingSubpartId(),
	        		MSG.backSubpart(ss.getSchedulingSubpartLabel()),
	        		true, false);

	        return mapping.findForward("displaySchedulingSubpart");

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

		    // Set Session Variables
	        InstructionalOfferingSearchAction.setLastInstructionalOffering(sessionContext, ss.getInstrOfferingConfig().getInstructionalOffering());

	        // populate form
	        InstrOfferingConfig ioc = ss.getInstrOfferingConfig();
	        InstructionalOffering io = ioc.getInstructionalOffering();
	        frm.setInstrOfferingId(io.getUniqueId().toString());
	        frm.setSchedulingSubpartId(subpartId);
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
	        if (frm.getCreditText() == null || frm.getCreditText().length() == 0){
		        if (ss.getCredit() != null){
		        	CourseCreditUnitConfig credit = ss.getCredit();
		        	frm.setCreditText(credit.creditText());
		        }
	        }
	    	if (ss.getParentSubpart() != null && ss.getItype().equals(ss.getParentSubpart().getItype())){
	    		frm.setSameItypeAsParent(new Boolean(true));
	    	} else {
	    		frm.setSameItypeAsParent(new Boolean(false));
	    	}

	        SchedulingSubpart next = ss.getNextSchedulingSubpart(sessionContext, Right.SchedulingSubpartDetail);
	        frm.setNextId(next==null?null:next.getUniqueId().toString());
	        SchedulingSubpart previous = ss.getPreviousSchedulingSubpart(sessionContext, Right.SchedulingSubpartDetail);
	        frm.setPreviousId(previous==null?null:previous.getUniqueId().toString());


	        // Set Parent Subpart
	        String parentSubpart = "";
	        SchedulingSubpart parentSS = ss.getParentSubpart();
	        frm.setParentSubpartId(parentSS == null || !sessionContext.hasPermission(parentSS, Right.SchedulingSubpartDetail) ? null : parentSS.getUniqueId().toString());
	        frm.setParentSubpartLabel(parentSS==null?null:parentSS.getSchedulingSubpartLabel());
	        while(parentSS!=null) {
	            parentSubpart = parentSS.getItype().getAbbv() + " - " + parentSubpart;
	            parentSS = parentSS.getParentSubpart();
	        }
	        frm.setParentSubpart(parentSubpart);

	        frm.setManagingDeptName(ss.getManagingDept()==null?null:ss.getManagingDept().getManagingDeptLabel());
	    }
	    
	    private void setupDatePatterns(HttpServletRequest request, SchedulingSubpartEditForm frm, SchedulingSubpart ss) throws Exception {
	    	DatePattern selectedDatePattern = ss.effectiveDatePattern();			
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

