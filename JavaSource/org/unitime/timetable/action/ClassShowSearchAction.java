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

import java.net.URLEncoder;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.hibernate.HibernateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.commons.Debug;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.form.ClassListForm;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.WebSolver;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.webutil.BackTracker;


/**
 * @author Stephanie Schluttenhofer, Tomas Muller
 */
@Service("/classShowSearch")
public class ClassShowSearchAction extends Action {
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	
	@Autowired SessionContext sessionContext;
	
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

        BackTracker.markForBack(request, null, null, false, true);
        
    	sessionContext.checkPermission(Right.Classes);
        
    	sessionContext.setAttribute(SessionAttribute.CallingPage, "classShowSearch");
    	
	    Object sas = sessionContext.getAttribute(SessionAttribute.ClassesSubjectAreas);
	    Object cn = sessionContext.getAttribute(SessionAttribute.ClassesCourseNumber);
	    String subjectAreaIds = "";
	    String courseNbr = "";
	    
	    if ( (sas==null || sas.toString().trim().isEmpty()) && (cn==null || cn.toString().trim().isEmpty()) ) {
		    // use session variables from io search  
	        sas = sessionContext.getAttribute(SessionAttribute.OfferingsSubjectArea);
	        cn = sessionContext.getAttribute(SessionAttribute.OfferingsCourseNumber);	        
	    }
	    
	    request.setAttribute(Department.EXTERNAL_DEPT_ATTR_NAME, Department.findAllExternal(sessionContext.getUser().getCurrentAcademicSessionId()));
        
		ClassListForm classListForm = (ClassListForm) form;
		ClassSearchAction.setupGeneralFormFilters(sessionContext, classListForm);
		ClassSearchAction.setupClassListSpecificFormFilters(sessionContext, classListForm);

    	if (!sessionContext.hasPermission(Right.Examinations))
    		classListForm.setExams(null);

    	classListForm.setSubjectAreas(SubjectArea.getUserSubjectAreas(sessionContext.getUser()));
    	
		if (sas == null && classListForm.getSubjectAreas().size() == 1)
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
		        
		        Integer maxSubjectsToSearch = ApplicationProperty.MaxSubjectsToSearchAutomatically.intValue();
		        if (maxSubjectsToSearch != null && maxSubjectsToSearch >= 0 && classListForm.getSubjectAreaIds().length > maxSubjectsToSearch) {
		        	return mapping.findForward("showClassSearch");
		        }
		        
				StringBuffer ids = new StringBuffer();
				StringBuffer names = new StringBuffer();
				StringBuffer subjIds = new StringBuffer();
				classListForm.setClasses(ClassSearchAction.getClasses(classListForm, WebSolver.getClassAssignmentProxy(request.getSession())));
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
							"classSearch.do?doit=Search&loadFilter=1&"+ids+"&courseNbr="+URLEncoder.encode(classListForm.getCourseNbr(), "utf-8"), 
							"Classes ("+names+
								(classListForm.getCourseNbr()==null || classListForm.getCourseNbr().length()==0?"":" "+classListForm.getCourseNbr())+
								")", 
							true, true);
				    return mapping.findForward("showClassList");
				}
	        }
	        catch (NumberFormatException nfe) {
	            Debug.error("Subject Area Ids session attribute is corrupted. Resetting ... ");
	            sessionContext.removeAttribute(SessionAttribute.ClassesSubjectAreas);
	            sessionContext.removeAttribute(SessionAttribute.ClassesCourseNumber);
	        }
	    }

		return mapping.findForward("showClassSearch");
	}
}
