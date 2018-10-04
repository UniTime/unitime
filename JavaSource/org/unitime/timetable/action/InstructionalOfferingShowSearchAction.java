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
import org.unitime.timetable.defaults.ApplicationProperty;
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
 * @author Stephanie Schluttenhofer, Tomas Muller
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

    	sessionContext.checkPermission(Right.InstructionalOfferings);
        
        BackTracker.markForBack(request, null, null, false, true); //clear back list
        
        sessionContext.setAttribute(SessionAttribute.CallingPage, "instructionalOfferingShowSearch");
        InstructionalOfferingListForm frm = (InstructionalOfferingListForm) form;
        
        // Check if subject area / course number saved to session
	    Object sa = sessionContext.getAttribute(SessionAttribute.OfferingsSubjectArea);
	    Object cn = sessionContext.getAttribute(SessionAttribute.OfferingsCourseNumber);
	    
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

	    Integer maxSubjectsToSearch = ApplicationProperty.MaxSubjectsToSearchAutomatically.intValue();
	    // Subject Area is saved to the session - Perform automatic search
	    if (sa != null) {
	        try {
	            
			    StringBuffer ids = new StringBuffer();
				StringBuffer names = new StringBuffer();
				StringBuffer subjIds = new StringBuffer();
				for (String id: sa.toString().split(",")) {
					if ("-1".equals(id)) continue;
					if (names.length() > 0) {
						names.append(","); 
						subjIds.append(",");
					}
					ids.append("&subjectAreaIds="+id);
					subjIds.append(id);
					names.append(((new SubjectAreaDAO()).get(new Long(id))).getSubjectAreaAbbreviation());
				}
				

				String courseNbr = "";
				if (cn != null && !cn.toString().isEmpty())
		            courseNbr = cn.toString();
		        
		        Debug.debug("Subject Areas: " + subjIds);
		        Debug.debug("Course Number: " + courseNbr);
		        
		        frm.setSubjectAreaIds(sa.toString().split(","));
		        frm.setCourseNbr(courseNbr);
		        
		        if (maxSubjectsToSearch != null && maxSubjectsToSearch >= 0 && frm.getSubjectAreaIds().length > maxSubjectsToSearch) {
		        	frm.setSubjectAreas(SubjectArea.getUserSubjectAreas(sessionContext.getUser()));
			    	frm.setInstructionalOfferings(null);
			    	return mapping.findForward("showInstructionalOfferingSearch");
		        }
		        
		        if(doSearch(request, frm)) {
					BackTracker.markForBack(
							request, 
							"instructionalOfferingSearch.do?doit=Search&loadInstrFilter=1" + ids + "&courseNbr="+frm.getCourseNbr(), 
							"Instructional Offerings (" + names + (frm.getCourseNbr()==null || frm.getCourseNbr().length()==0?"":" "+frm.getCourseNbr()) + ")", 
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
	        if (s.size() == 1 && (maxSubjectsToSearch == null || maxSubjectsToSearch != 0)) {
	            Debug.debug("Exactly 1 subject area found ... ");
	            frm.setSubjectAreaIds(new String[] {((SubjectArea) s.iterator().next()).getUniqueId().toString()});
		        if(doSearch(request, frm)) {
					BackTracker.markForBack(
							request, 
							"instructionalOfferingSearch.do?doit=Search&loadInstrFilter=1&subjectAreaIds="+frm.getSubjectAreaIds()[0]+"&courseNbr="+frm.getCourseNbr(), 
							"Instructional Offerings ("+
								(frm.getSubjectAreaAbbv()==null?((new SubjectAreaDAO()).get(new Long(frm.getSubjectAreaIds()[0]))).getSubjectAreaAbbreviation():frm.getSubjectAreaAbbv())+
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
	    
	    
	    frm.setSubjectAreas(SubjectArea.getUserSubjectAreas(sessionContext.getUser()));
	    frm.setInstructionalOfferings(InstructionalOfferingSearchAction.getInstructionalOfferings(sessionContext.getUser().getCurrentAcademicSessionId(), classAssignmentService.getAssignment(), frm));
        
		// Search return results - Generate html
		return !frm.getInstructionalOfferings().isEmpty();
	}
	
}
