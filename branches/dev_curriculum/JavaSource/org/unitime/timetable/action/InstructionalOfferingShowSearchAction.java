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

import java.util.Collection;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.hibernate.HibernateException;
import org.unitime.commons.Debug;
import org.unitime.commons.web.Web;
import org.unitime.timetable.form.InstructionalOfferingListForm;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.webutil.BackTracker;


/**
 * @author Stephanie Schluttenhofer
 */

public class InstructionalOfferingShowSearchAction extends Action {

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

	    HttpSession httpSession = request.getSession();
        if(!Web.isLoggedIn( httpSession )) {
            throw new Exception ("Access Denied.");
        }
        
        BackTracker.markForBack(request, null, null, false, true); //clear back list
        
        httpSession.setAttribute("callingPage", "instructionalOfferingShowSearch");
        InstructionalOfferingListForm frm = (InstructionalOfferingListForm) form;
        
        // Check if subject area / course number saved to session
	    Object sa = httpSession.getAttribute(Constants.SUBJ_AREA_ID_ATTR_NAME);
	    Object cn = httpSession.getAttribute(Constants.CRS_NBR_ATTR_NAME);
	    String subjectAreaId = "";
	    String courseNbr = "";
	    
	    if ( (sa==null || sa.toString().trim().length()==0) 
	            && (cn==null || cn.toString().trim().length()==0) ) {
		    // use session variables from class search  
		    sa = httpSession.getAttribute(Constants.CRS_LST_SUBJ_AREA_IDS_ATTR_NAME);
		    cn = httpSession.getAttribute(Constants.CRS_LST_CRS_NBR_ATTR_NAME);
		    
		    // Use first subject area
		    if (sa!=null) {
		       String saStr = sa.toString();
		       if (saStr.indexOf(",")>0) {
		           sa = saStr.substring(0, saStr.indexOf(","));
		       }
		    }
	    }
	    
	    
	    InstructionalOfferingSearchAction.setupInstrOffrListSpecificFormFilters(httpSession, frm);
	    /*
	    if (request.getParameter("subjectAreaId") != null){
	    	frm.setDivSec(request.getParameter("divSec")==null?Boolean.FALSE:new Boolean(request.getParameter("divSec")));
	    	frm.setDemand(request.getParameter("demand")==null?Boolean.FALSE:new Boolean(request.getParameter("demand")));
	    	frm.setProjectedDemand(request.getParameter("projectedDemand")==null?Boolean.FALSE:new Boolean(request.getParameter("projectedDemand")));
	    	frm.setMinPerWk(request.getParameter("minPerWk")==null?Boolean.FALSE:new Boolean(request.getParameter("minPerWk")));
	    	frm.setLimit(request.getParameter("limit")==null?Boolean.FALSE:new Boolean(request.getParameter("limit")));
	    	frm.setRoomLimit(request.getParameter("roomLimit")==null?Boolean.FALSE:new Boolean(request.getParameter("roomLimit")));
	    	frm.setManager(request.getParameter("manager")==null?Boolean.FALSE:new Boolean(request.getParameter("manager")));
	    	frm.setDatePattern(request.getParameter("datePattern")==null?Boolean.FALSE:new Boolean(request.getParameter("datePattern")));
	    	frm.setTimePattern(request.getParameter("timePattern")==null?Boolean.FALSE:new Boolean(request.getParameter("timePattern")));
	    	frm.setPreferences(request.getParameter("preferences")==null?Boolean.FALSE:new Boolean(request.getParameter("preferences")));
	    	frm.setInstructor(request.getParameter("instructor")==null?Boolean.FALSE:new Boolean(request.getParameter("instructor")));
	    	frm.setTimetable(request.getParameter("timetable")==null?Boolean.FALSE:new Boolean(request.getParameter("timetable")));
	    	frm.setCredit(request.getParameter("credit")==null?Boolean.FALSE:new Boolean(request.getParameter("credit")));
	    	frm.setSchedulePrintNote(request.getParameter("schedulePrintNote")==null?Boolean.FALSE:new Boolean(request.getParameter("schedulePrintNote")));
	    }
	    */
	    // Subject Area is saved to the session - Perform automatic search
	    if(sa!=null) {
	        subjectAreaId = sa.toString();
	        
	        try {
	            
		        if(cn!=null && cn.toString().trim().length()>0)
		            courseNbr = cn.toString();
		        
		        Debug.debug("Subject Area: " + subjectAreaId);
		        Debug.debug("Course Number: " + courseNbr);
		        
		        frm.setSubjectAreaId(subjectAreaId);
		        frm.setCourseNbr(courseNbr);
		        
		        if(doSearch(request, frm)) {
					BackTracker.markForBack(
							request, 
							"instructionalOfferingSearch.do?doit=Search&loadInstrFilter=1&subjectAreaId="+frm.getSubjectAreaId()+"&courseNbr="+frm.getCourseNbr(), 
							"Instructional Offerings ("+
								(frm.getSubjectAreaAbbv()==null?((new SubjectAreaDAO()).get(new Long(frm.getSubjectAreaId()))).getSubjectAreaAbbreviation():frm.getSubjectAreaAbbv())+
								(frm.getCourseNbr()==null || frm.getCourseNbr().length()==0?"":" "+frm.getCourseNbr())+
								")", 
							true, true);
		            return mapping.findForward("showInstructionalOfferingList");
		        	
		        }
	        }
	        catch (NumberFormatException nfe) {
	            Debug.error("Subject Area Id session attribute is corrupted. Resetting ... ");
	            httpSession.setAttribute(Constants.SUBJ_AREA_ID_ATTR_NAME, null);
	            httpSession.setAttribute(Constants.CRS_NBR_ATTR_NAME, null);
	        }
	    }
	    
	    // No session attribute found - Load subject areas
	    else {	        
	        frm.setCollections(request, null);
	        
	        // Check if only 1 subject area exists
	        Set s = (Set) frm.getSubjectAreas();
	        if(s.size()==1) {
	            Debug.debug("Exactly 1 subject area found ... ");
	            frm.setSubjectAreaId(((SubjectArea) s.iterator().next()).getUniqueId().toString());
		        if(doSearch(request, frm)) {
					BackTracker.markForBack(
							request, 
							"instructionalOfferingSearch.do?doit=Search&loadInstrFilter=1&subjectAreaId="+frm.getSubjectAreaId()+"&courseNbr="+frm.getCourseNbr(), 
							"Instructional Offerings ("+
								(frm.getSubjectAreaAbbv()==null?((new SubjectAreaDAO()).get(new Long(frm.getSubjectAreaId()))).getSubjectAreaAbbreviation():frm.getSubjectAreaAbbv())+
								(frm.getCourseNbr()==null || frm.getCourseNbr().length()==0?"":" "+frm.getCourseNbr())+
								")", 
							true, true);
		            return mapping.findForward("showInstructionalOfferingList");
		        }
	        }
	    }
	    
        return mapping.findForward("showInstructionalOfferingSearch");
	}

	/**
	 * Perform search based on form values of subject area and course number
	 * If search produces results - generate html and store the html as a request attribute
	 * @param request
	 * @param frm
	 * @return true if search returned results, false otherwise
	 * @throws Exception
	 */
	private boolean doSearch(
	        HttpServletRequest request,
	        InstructionalOfferingListForm frm) throws Exception {
	    
	    
	    frm.setCollections(request, InstructionalOfferingSearchAction.getInstructionalOfferings(request, frm));
		Collection instrOfferings = frm.getInstructionalOfferings();
        
		// Search return results - Generate html
		if (!instrOfferings.isEmpty()) {
		    return true;
		}
		
		return false;
	}
	
}
