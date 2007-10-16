/*
 * UniTime 3.0 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2007, UniTime.org, and individual contributors
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
import org.unitime.timetable.form.DistributionPrefsForm;
import org.unitime.timetable.form.SchedulingSubpartEditForm;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseCreditUnitConfig;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.SchedulingSubpartDAO;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.LookupTables;
import org.unitime.timetable.webutil.BackTracker;
import org.unitime.timetable.webutil.DistributionPrefsTableBuilder;
import org.unitime.timetable.webutil.RequiredTimeTable;


/**
 * MyEclipse Struts
 * Creation date: 03-24-2006
 *
 * XDoclet definition:
 * @struts.action path="/schedulingSubpartDetail" name="schedulingSubpartEditForm" input="schedulingSubpartDetailTile" scope="request" validate="true"
 * @struts.action-forward name="instructionalOfferingSearch" path="/instructionalOfferingSearch.do"
 * @struts.action-forward name="displaySchedulingSubpart" path="schedulingSubpartDetailTile"
 * @struts.action-forward name="addDistributionPrefs" path="/distributionPrefs.do"
 */
public class SchedulingSubpartDetailAction extends PreferencesAction {

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
	        SchedulingSubpartEditForm frm = (SchedulingSubpartEditForm) form;
	        MessageResources rsc = getResources(request);
	        ActionMessages errors = new ActionMessages();

	        // Read parameters
	        String subpartId = request.getParameter("ssuid")==null
									? request.getAttribute("ssuid") !=null
									        ? request.getAttribute("ssuid").toString()
									        : null
									: request.getParameter("ssuid");

	        String op = frm.getOp();
	        boolean timeVertical = RequiredTimeTable.getTimeGridVertical(Web.getUser(httpSession));

	        // Read subpart id from form
	        if(op.equals(rsc.getMessage("button.editPrefsSubpart"))
	        		|| op.equals(rsc.getMessage("button.addDistPref"))
	                || op.equals(rsc.getMessage("button.backToInstrOffrDet"))
	                || op.equals(rsc.getMessage("button.nextSchedulingSubpart"))
	                || op.equals(rsc.getMessage("button.previousSchedulingSubpart"))
	                || op.equals(rsc.getMessage("button.clearAllClassPrefs"))) {
	            subpartId = frm.getSchedulingSubpartId();
	        } else {
	        	frm.reset(mapping, request);
	        }

	        // Check op exists
	        if(op==null)
	            throw new Exception ("Null Operation not supported.");

	        Debug.debug("op: " + op);
	        Debug.debug("subpart: " + subpartId);

	        // Check subpart exists
	        if(subpartId==null || subpartId.trim()=="")
	            throw new Exception ("Subpart Info not supplied.");

	        // If subpart id is not null - load subpart info
	        SchedulingSubpartDAO sdao = new SchedulingSubpartDAO();
	        SchedulingSubpart ss = sdao.get(new Long(subpartId));

	        // Cancel - Go back to Instructional Offering Screen
	        if(op.equals(rsc.getMessage("button.backToInstrOffrDet"))
	                && subpartId!=null && subpartId.trim()!="") {

	            /*doCancel(request, subpartId);
	            return mapping.findForward("instructionalOfferingSearch");
	            */

	        	response.sendRedirect( response.encodeURL("instructionalOfferingDetail.do?op=view&io="+ss.getInstrOfferingConfig().getInstructionalOffering().getUniqueId()));
	        }

	        // Edit Preference - Redirect to prefs edit screen
	        if(op.equals(rsc.getMessage("button.editPrefsSubpart"))
	                && subpartId!=null && subpartId.trim()!="") {
	        	response.sendRedirect( response.encodeURL("schedulingSubpartEdit.do?ssuid="+ss.getUniqueId().toString()) );
	        }

			// Add Distribution Preference - Redirect to dist prefs screen
		    if(op.equals(rsc.getMessage("button.addDistPref"))) {
		        CourseOffering cco = ss.getInstrOfferingConfig().getControllingCourseOffering();
		        request.setAttribute("subjectAreaId", cco.getSubjectArea().getUniqueId().toString());
		        request.setAttribute("schedSubpartId", subpartId);
		        request.setAttribute("courseOffrId", cco.getUniqueId().toString());
		        request.setAttribute("classId", DistributionPrefsForm.ALL_CLASSES_SELECT);
	            return mapping.findForward("addDistributionPrefs");
		    }

            if (op.equals(rsc.getMessage("button.nextSchedulingSubpart"))) {
            	response.sendRedirect(response.encodeURL("schedulingSubpartDetail.do?ssuid="+frm.getNextId()));
            	return null;
            }

            if (op.equals(rsc.getMessage("button.clearAllClassPrefs")) && "y".equals(request.getParameter("confirm"))) {
            	Class_DAO cdao = new Class_DAO();
            	for (Iterator i=ss.getClasses().iterator();i.hasNext();) {
            		Class_ c = (Class_)i.next();
            		c.getPreferences().clear();
            		cdao.saveOrUpdate(c);
            	}

                ChangeLog.addChange(
                        null,
                        request,
                        ss,
                        ChangeLog.Source.SCHEDULING_SUBPART_EDIT,
                        ChangeLog.Operation.CLEAR_ALL_PREF,
                        ss.getInstrOfferingConfig().getInstructionalOffering().getControllingCourseOffering().getSubjectArea(),
                        ss.getManagingDept());
            }

            if (op.equals(rsc.getMessage("button.previousSchedulingSubpart"))) {
            	response.sendRedirect(response.encodeURL("schedulingSubpartDetail.do?ssuid="+frm.getPreviousId()));
            	return null;
            }

	        // Load form attributes that are constant
	        doLoad(request, frm, ss, subpartId);

	        User user = Web.getUser(httpSession);

	        // Initialize Preferences for initial load
			Set timePatterns = null;
			frm.setAvailableTimePatterns(TimePattern.findApplicable(request,ss.getMinutesPerWk().intValue(),false,ss.getManagingDept()));
			initPrefs(user, frm, ss, null, false);
	        timePatterns = ss.getTimePatterns();

	        // Dist Prefs are not editable by Sched Dpty Asst
	        String currentRole = user.getCurrentRole();
	        boolean editable = true;
	       // if(currentRole.equals(Roles.SCHED_DEPUTY_ASST_ROLE))
	       //     editable = false;

		    // Display distribution Prefs
	        DistributionPrefsTableBuilder tbl = new DistributionPrefsTableBuilder();
	        String html = tbl.getDistPrefsTableForSchedulingSubpart(request, ss, true);
	        if (html!=null)
	        	request.setAttribute(DistributionPref.DIST_PREF_REQUEST_ATTR, html);

			// Process Preferences Action
			processPrefAction(request, frm, errors);

	        // Generate Time Pattern Grids
			super.generateTimePatternGrids(request, frm, ss, timePatterns, "init", timeVertical, false, null);

			LookupTables.setupDatePatterns(request, "Default", ss.getSession().getDefaultDatePatternNotNull(), ss.getManagingDept(), ss.effectiveDatePattern());

	        LookupTables.setupRooms(request, ss);		 // Room Prefs
	        LookupTables.setupBldgs(request, ss);		 // Building Prefs
	        LookupTables.setupRoomFeatures(request, ss); // Preference Levels
	        LookupTables.setupRoomGroups(request, ss);   // Room Groups

	        BackTracker.markForBack(request,
	        		"schedulingSubpartDetail.do?ssuid="+frm.getSchedulingSubpartId(),
	        		"Scheduling Subpart ("+ss.getSchedulingSubpartLabel()+")",
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

	        HttpSession httpSession = request.getSession();
	    	User user = Web.getUser(httpSession);

	        CourseOffering co = ss.getInstrOfferingConfig().getInstructionalOffering().getControllingCourseOffering();

		    // Set Session Variables
	        httpSession.setAttribute(Constants.SUBJ_AREA_ID_ATTR_NAME, co.getSubjectArea().getUniqueId().toString());
	        if (httpSession.getAttribute(Constants.CRS_NBR_ATTR_NAME)!=null
	                && httpSession.getAttribute(Constants.CRS_NBR_ATTR_NAME).toString().length()>0)
	            httpSession.setAttribute(Constants.CRS_NBR_ATTR_NAME, co.getCourseNbr());

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
	        frm.setSubjectArea(co.getSubjectAreaAbbv());
	        frm.setSubjectAreaId(co.getSubjectArea().getUniqueId().toString());
	        frm.setCourseNbr(co.getCourseNbr());
	        frm.setAutoSpreadInTime(ss.isAutoSpreadInTime());
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

	        SchedulingSubpart next = ss.getNextSchedulingSubpart(request.getSession(), Web.getUser(request.getSession()), false, true);
	        frm.setNextId(next==null?null:next.getUniqueId().toString());
	        SchedulingSubpart previous = ss.getPreviousSchedulingSubpart(request.getSession(), Web.getUser(request.getSession()), false, true);
	        frm.setPreviousId(previous==null?null:previous.getUniqueId().toString());


	        // Set Parent Subpart
	        String parentSubpart = "";
	        SchedulingSubpart parentSS = ss.getParentSubpart();
	        frm.setParentSubpartId(parentSS==null || !parentSS.isViewableBy(user)?null:parentSS.getUniqueId().toString());
	        frm.setParentSubpartLabel(parentSS==null?null:parentSS.getSchedulingSubpartLabel());
	        while(parentSS!=null) {
	            parentSubpart = parentSS.getItype().getAbbv() + " - " + parentSubpart;
	            parentSS = parentSS.getParentSubpart();
	        }
	        frm.setParentSubpart(parentSubpart);

	        frm.setManagingDeptName(ss.getManagingDept()==null?null:ss.getManagingDept().getManagingDeptLabel());
	    }

}

