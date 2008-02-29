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

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.hibernate.HibernateException;
import org.unitime.commons.Debug;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.form.ClassListForm;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.model.dao.TimetableManagerDAO;
import org.unitime.timetable.solver.WebSolver;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.LookupTables;
import org.unitime.timetable.webutil.BackTracker;


/**
 * @author Stephanie Schluttenhofer
 */

public class ClassShowSearchAction extends Action {

	/** 
	 * Method execute
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return ActionForward
	 * @throws HibernateException
	 */
	public ActionForward execute(
		ActionMapping mapping,
		ActionForm form,
		HttpServletRequest request,
		HttpServletResponse response) throws Exception {

        BackTracker.markForBack(request, null, null, false, true); //clear back list

        HttpSession httpSession = request.getSession();
        if(!Web.isLoggedIn( httpSession )) {
            throw new Exception ("Access Denied.");
        }
        
        
        httpSession.setAttribute("callingPage", "classShowSearch");
        // Check if subject area / course number saved to session
	    Object sas = httpSession.getAttribute(Constants.CRS_LST_SUBJ_AREA_IDS_ATTR_NAME);
	    Object cn = httpSession.getAttribute(Constants.CRS_LST_CRS_NBR_ATTR_NAME);
	    String subjectAreaIds = "";
	    String courseNbr = "";
	    
	    if ( (sas==null || sas.toString().trim().length()==0) 
	            && (cn==null || cn.toString().trim().length()==0) ) {
		    // use session variables from io search  
	        sas = httpSession.getAttribute(Constants.SUBJ_AREA_ID_ATTR_NAME);
	        cn = httpSession.getAttribute(Constants.CRS_NBR_ATTR_NAME);	        
	    }
	    
	    User user = Web.getUser(request.getSession());
	    LookupTables.setupExternalDepts(request, (Long)user.getAttribute(Constants.SESSION_ID_ATTR_NAME));
        
		ClassListForm classListForm = (ClassListForm) form;
		ClassSearchAction.setupBasicFormData(classListForm, user);
		ClassSearchAction.setupGeneralFormFilters(httpSession, classListForm);
		ClassSearchAction.setupClassListSpecificFormFilters(httpSession, classListForm);
		/*
		if (request.getParameter("sortBy") != null) {
			classListForm.setDivSec(request.getParameter("divSec")==null?Boolean.FALSE:new Boolean(request.getParameter("divSec")));
			classListForm.setLimit(request.getParameter("limit")==null?Boolean.FALSE:new Boolean(request.getParameter("limit")));
			classListForm.setRoomLimit(request.getParameter("roomLimit")==null?Boolean.FALSE:new Boolean(request.getParameter("roomLimit")));	
			classListForm.setManager(request.getParameter("manager")==null?Boolean.FALSE:new Boolean(request.getParameter("manager")));
			classListForm.setDatePattern(request.getParameter("datePattern")==null?Boolean.FALSE:new Boolean(request.getParameter("datePattern")));
			classListForm.setTimePattern(request.getParameter("timePattern")==null?Boolean.FALSE:new Boolean(request.getParameter("timePattern")));	
			classListForm.setInstructor(request.getParameter("instructor")==null?Boolean.FALSE:new Boolean(request.getParameter("instructor")));
			classListForm.setPreferences(request.getParameter("preferences")==null?Boolean.FALSE:new Boolean(request.getParameter("preferences")));	
			classListForm.setTimetable(request.getParameter("timetable")==null?Boolean.FALSE:new Boolean(request.getParameter("timetable")));
			classListForm.setSchedulePrintNote(request.getParameter("schedulePrintNote")==null?Boolean.FALSE:new Boolean(request.getParameter("schedulePrintNote")));
			classListForm.setSortBy((String)request.getParameter("sortBy"));
			classListForm.setFilterAssignedRoom((String)request.getParameter("filterAssignedRoom"));
			classListForm.setFilterInstructor((String)request.getParameter("filterInstructor"));
			classListForm.setFilterManager((String)request.getParameter("filterManager"));
			classListForm.setFilterIType((String)request.getParameter("filterIType"));
			classListForm.setFilterAssignedTimeMon(request.getParameter("filterAssignedTimeMon")==null?false:Boolean.getBoolean((String)request.getParameter("filterAssignedTimeMon")));
			classListForm.setFilterAssignedTimeTue(request.getParameter("filterAssignedTimeTue")==null?false:Boolean.getBoolean((String)request.getParameter("filterAssignedTimeTue")));
			classListForm.setFilterAssignedTimeWed(request.getParameter("filterAssignedTimeWed")==null?false:Boolean.getBoolean((String)request.getParameter("filterAssignedTimeWed")));
			classListForm.setFilterAssignedTimeThu(request.getParameter("filterAssignedTimeThu")==null?false:Boolean.getBoolean((String)request.getParameter("filterAssignedTimeThu")));
			classListForm.setFilterAssignedTimeFri(request.getParameter("filterAssignedTimeFri")==null?false:Boolean.getBoolean((String)request.getParameter("filterAssignedTimeFri")));
			classListForm.setFilterAssignedTimeSat(request.getParameter("filterAssignedTimeSat")==null?false:Boolean.getBoolean((String)request.getParameter("filterAssignedTimeSat")));
			classListForm.setFilterAssignedTimeSun(request.getParameter("filterAssignedTimeSun")==null?false:Boolean.getBoolean((String)request.getParameter("filterAssignedTimeSun")));
			classListForm.setFilterAssignedTimeHour((String)request.getParameter("filterAssignedTimeHour"));
			classListForm.setFilterAssignedTimeMin((String)request.getParameter("filterAssignedTimeMin"));
			classListForm.setFilterAssignedTimeAmPm((String)request.getParameter("filterAssignedTimeAmPm"));
			classListForm.setFilterAssignedTimeLength((String)request.getParameter("filterAssignedTimeLength"));
			classListForm.setSortByKeepSubparts(Boolean.getBoolean((String)request.getParameter("sortByKeepSubparts")));
		}
		*/
		
        String managerId = (String)user.getAttribute(Constants.TMTBL_MGR_ID_ATTR_NAME);
        TimetableManager manager = (new TimetableManagerDAO()).get(new Long(managerId));
		if (manager==null || !manager.canSeeTimetable(Session.getCurrentAcadSession(user), user))
			classListForm.setTimetable(null);
		
		classListForm.setCollections(request, null);
		if (sas==null && classListForm.getSubjectAreas().size()==1)
			sas = ((SubjectArea)classListForm.getSubjectAreas().iterator().next()).getUniqueId().toString();
			
        if (Constants.ALL_OPTION_VALUE.equals(sas)) sas=null;
			
	    // Subject Areas are saved to the session - Perform automatic search
	    if(sas!=null && sas.toString().trim().length() > 0) {
	        subjectAreaIds = sas.toString();
	        
	        try {
	            
		        if(cn!=null && cn.toString().trim().length()>0)
		            courseNbr = cn.toString();
		        
		        Debug.debug("Subject Areas: " + subjectAreaIds);
		        Debug.debug("Course Number: " + courseNbr);
		        
		        classListForm.setSubjectAreaIds(subjectAreaIds.split(","));
		        classListForm.setCourseNbr(courseNbr);
				StringBuffer ids = new StringBuffer();
				StringBuffer names = new StringBuffer();
				StringBuffer subjIds = new StringBuffer();
				classListForm.setCollections(request, ClassSearchAction.getClasses(classListForm, WebSolver.getClassAssignmentProxy(request.getSession())));
				Collection classes = classListForm.getClasses();
				if (classes.isEmpty()) {
					    ActionMessages errors = new ActionMessages();
					    errors.add("searchResult", new ActionMessage("errors.generic", "No records matching the search criteria were found."));
					    saveErrors(request, errors);
					    return mapping.findForward("showClassSearch");
				} else {
			        for (int i=0;i<classListForm.getSubjectAreaIds().length;i++) {
						if (i>0) {
							names.append(","); 
							subjIds.append(",");
							}
						ids.append("&subjectAreaIds="+classListForm.getSubjectAreaIds()[i]);
						subjIds.append(classListForm.getSubjectAreaIds()[i]);
						names.append(((new SubjectAreaDAO()).get(new Long(classListForm.getSubjectAreaIds()[i]))).getSubjectAreaAbbreviation());
					}
			        BackTracker.markForBack(
							request, 
							"classSearch.do?doit=Search&loadFilter=1&"+ids+"&courseNbr="+classListForm.getCourseNbr(), 
							"Classes ("+names+
								(classListForm.getCourseNbr()==null || classListForm.getCourseNbr().length()==0?"":" "+classListForm.getCourseNbr())+
								")", 
							true, true);
				    return mapping.findForward("showClassList");
				}
	        }
	        catch (NumberFormatException nfe) {
	            Debug.error("Subject Area Ids session attribute is corrupted. Resetting ... ");
	            httpSession.setAttribute(Constants.CRS_LST_SUBJ_AREA_IDS_ATTR_NAME, null);
	            httpSession.setAttribute(Constants.CRS_LST_CRS_NBR_ATTR_NAME, null);
	        }
	    }

		return mapping.findForward("showClassSearch");
	}
}
