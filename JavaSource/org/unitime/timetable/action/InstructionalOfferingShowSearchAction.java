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

import java.util.Collection;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.hibernate.HibernateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.commons.Debug;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.form.InstructionalOfferingListForm;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.service.ClassAssignmentService;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.webutil.BackTracker;


/**
 * @author Stephanie Schluttenhofer
 */
@Service("/instructionalOfferingShowSearch")
public class InstructionalOfferingShowSearchAction extends Action {
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);

	@Autowired SessionContext sessionContext;
	
	@Autowired ClassAssignmentService classAssignmentService;

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

    	if (!sessionContext.hasPermission(Right.InstructionalOfferings))
    		throw new Exception(MSG.errorAccessDenied());
        
        BackTracker.markForBack(request, null, null, false, true); //clear back list
        
        sessionContext.setAttribute("callingPage", "instructionalOfferingShowSearch");
        InstructionalOfferingListForm frm = (InstructionalOfferingListForm) form;
        
        // Check if subject area / course number saved to session
	    Object sa = sessionContext.getAttribute(SessionAttribute.OfferingsSubjectArea);
	    Object cn = sessionContext.getAttribute(SessionAttribute.OfferingsCourseNumber);
	    String subjectAreaId = "";
	    String courseNbr = "";
	    
	    if (Constants.ALL_OPTION_VALUE.equals(sa))
	    	sa = null;
	    
	    if ((sa == null || sa.toString().trim().isEmpty()) && (cn == null || cn.toString().trim().isEmpty())) {
		    // use session variables from class search  
		    sa = sessionContext.getAttribute(SessionAttribute.ClassesSubjectAreas);
		    cn = sessionContext.getAttribute(SessionAttribute.ClassesCourseNumber);
		    
		    // Use first subject area
		    if (sa != null) {
		       String saStr = sa.toString();
		       if (saStr.indexOf(",") > 0)
		           sa = saStr.substring(0, saStr.indexOf(","));
		    }
	    }
	    
	    InstructionalOfferingSearchAction.setupInstrOffrListSpecificFormFilters(sessionContext, frm);
	    
	    if (!sessionContext.hasPermission(Right.Examinations))
	    	frm.setExams(null);

	    // Subject Area is saved to the session - Perform automatic search
	    if (sa != null) {
	        subjectAreaId = sa.toString();
	        
	        try {
	            
		        if (cn != null && !cn.toString().isEmpty())
		            courseNbr = cn.toString();
		        
		        Debug.debug("Subject Area: " + subjectAreaId);
		        Debug.debug("Course Number: " + courseNbr);
		        
		        frm.setSubjectAreaId(Long.valueOf(subjectAreaId));
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
	            sessionContext.removeAttribute(SessionAttribute.OfferingsSubjectArea);
	            sessionContext.removeAttribute(SessionAttribute.OfferingsCourseNumber);
	        }
	    }
	    
	    // No session attribute found - Load subject areas
	    else {
	    	frm.setSubjectAreas(SubjectArea.getUserSubjectAreas(sessionContext.getUser()));
	    	frm.setInstructionalOfferings(null);
	        
	        // Check if only 1 subject area exists
	        Set s = (Set) frm.getSubjectAreas();
	        if (s.size() == 1) {
	            Debug.debug("Exactly 1 subject area found ... ");
	            frm.setSubjectAreaId(((SubjectArea) s.iterator().next()).getUniqueId());
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
	    
	    
		Collection instrOfferings = InstructionalOfferingSearchAction.getInstructionalOfferings(sessionContext.getUser().getCurrentAcademicSessionId(), classAssignmentService.getAssignment(), frm);
		
	    frm.setSubjectAreas(SubjectArea.getUserSubjectAreas(sessionContext.getUser()));
	    frm.setInstructionalOfferings(instrOfferings);
        
		// Search return results - Generate html
		return !instrOfferings.isEmpty();
	}
	
}
