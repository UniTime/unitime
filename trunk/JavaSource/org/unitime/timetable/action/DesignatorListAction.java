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

import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.util.MessageResources;
import org.unitime.commons.Debug;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.commons.web.WebTable;
import org.unitime.timetable.form.DesignatorListForm;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.webutil.BackTracker;
import org.unitime.timetable.webutil.DesignatorListBuilder;


/** 
 * MyEclipse Struts
 * Creation date: 07-26-2006
 * 
 * XDoclet definition:
 * @struts:action path="/designatorList" name="designatorListForm" input="/user/designatorList.jsp" scope="request"
 */
public class DesignatorListAction extends Action {

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

	    HttpSession httpSession = request.getSession();
        if(!Web.isLoggedIn( httpSession )) {
            throw new Exception ("Access Denied.");
        }
        
        MessageResources rsc = getResources(request);
        User user = Web.getUser(request.getSession());        
        DesignatorListForm frm = (DesignatorListForm) form;
	    ActionMessages errors = null;
        
        // Get operation
        String op = (request.getParameter("op")==null) 
						? (frm.getOp()==null || frm.getOp().length()==0)
						        ? (request.getAttribute("op")==null)
						                ? null
						                : request.getAttribute("op").toString()
						        : frm.getOp()
						: request.getParameter("op");		        
        			        
        if(op==null)
            op = request.getParameter("hdnOp");
        
        if(op==null || op.trim().length()==0)
            op = rsc.getMessage("op.view");

        // Set up Lists
        frm.setOp(op);
        frm.setSubjectAreas(TimetableManager.getSubjectAreas(user));
        
        BackTracker.markForBack(
        		request,
        		"designatorList.do?subjectAreaId=" + frm.getSubjectAreaId(),
        		"Designator List",
        		true, true);
	    
		// Check column ordering - default to name
        WebTable.setOrder(request.getSession(),"designatorList.ord",request.getParameter("order"),1);
		
        // First access to screen
        if (op.equals(rsc.getMessage("op.view"))
                || op.equals(rsc.getMessage("button.saveDesignator"))
                || op.equals(rsc.getMessage("button.updateDesignator"))
                || op.equals(rsc.getMessage("button.deleteDesignator")) ) {
            
	        Set s = (Set) frm.getSubjectAreas();
	        if(s.size()==1) {
	            Debug.debug("Exactly 1 subject area found ... ");
	            SubjectArea sa = (SubjectArea) s.iterator().next(); 
	            frm.setSubjectAreaId(sa.getUniqueId().toString());
	            frm.setEditable(sa.getDepartment().isEditableBy(user) || sa.getDepartment().isLimitedEditableBy(user));
	            String html = new DesignatorListBuilder().htmlTableForSubjectArea(request, frm.getSubjectAreaId(), WebTable.getOrder(request.getSession(),"designatorList.ord"));
	            request.setAttribute("designatorList", html);
	            httpSession.setAttribute(Constants.SUBJ_AREA_ID_ATTR_NAME, frm.getSubjectAreaId());
	        } else {
            	String subjectAreaId = (String) request.getAttribute("subjectAreaId");
            	if (subjectAreaId==null)
            	    subjectAreaId = request.getParameter("subjectAreaId");
            	if (subjectAreaId==null)
            	    subjectAreaId = (String) httpSession.getAttribute(Constants.SUBJ_AREA_ID_ATTR_NAME);
            	
            	if (subjectAreaId!=null && subjectAreaId.trim().length()>0) {
    	            frm.setSubjectAreaId(subjectAreaId);
    	            SubjectArea sa = (new SubjectAreaDAO()).get(Long.valueOf(subjectAreaId));
    	            frm.setEditable(sa==null?false:sa.getDepartment().isEditableBy(user) || sa.getDepartment().isLimitedEditableBy(user));
    	            String html = new DesignatorListBuilder().htmlTableForSubjectArea(request, frm.getSubjectAreaId(), WebTable.getOrder(request.getSession(),"designatorList.ord"));
    	            request.setAttribute("designatorList", html);
    	            httpSession.setAttribute(Constants.SUBJ_AREA_ID_ATTR_NAME, frm.getSubjectAreaId());
            	} else {
            		frm.setEditable(false);
            	}
            }
	        
            return mapping.findForward("displayDesignatorList");
        }
        
        // View Button Clicked
        if( op.equals(rsc.getMessage("button.exportPDF")) || op.equals(rsc.getMessage("button.displayDesignatorList")) ) {

		    errors = frm.validate(mapping, request);
		    
		    // Validation fails
		    if(errors.size()>0) {
			    saveErrors(request, errors);
			    return mapping.findForward("displayDesignatorList");
		    }
		    
		    if (op.equals(rsc.getMessage("button.exportPDF")))
		    	new DesignatorListBuilder().pdfTableForSubjectArea(request, frm.getSubjectAreaId(), WebTable.getOrder(request.getSession(),"designatorList.ord"));
            
        	if (frm.getSubjectAreaId()!=null && frm.getSubjectAreaId().trim().length()>0) {
	            SubjectArea sa = (new SubjectAreaDAO()).get(Long.valueOf(frm.getSubjectAreaId()));
	            frm.setEditable(sa==null?false:sa.getDepartment().isEditableBy(user) || sa.getDepartment().isLimitedEditableBy(user));
        	} else {
        		frm.setEditable(false);
        	}
            String html = new DesignatorListBuilder().htmlTableForSubjectArea(request, frm.getSubjectAreaId(), WebTable.getOrder(request.getSession(),"designatorList.ord"));
            request.setAttribute("designatorList", html);
            httpSession.setAttribute(Constants.SUBJ_AREA_ID_ATTR_NAME, frm.getSubjectAreaId());
            return mapping.findForward("displayDesignatorList");
        }            
        
        // Add new designator
        if( op.equals(rsc.getMessage("button.addDesignator")) ) {
		    errors = frm.validate(mapping, request);
		    
		    // Validation fails
		    if(errors.size()>0) {
			    saveErrors(request, errors);
			    return mapping.findForward("displayDesignatorList");
		    }
            
            request.setAttribute("subjectAreaId", frm.getSubjectAreaId());
            return mapping.findForward("addDesignator");
        }
        
        return mapping.findForward("displayDesignatorList");
    }

    
}
