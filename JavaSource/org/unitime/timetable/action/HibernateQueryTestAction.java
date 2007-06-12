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

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.hibernate.Query;
import org.hibernate.Session;
import org.unitime.commons.Debug;
import org.unitime.commons.web.Web;
import org.unitime.timetable.form.HibernateQueryTestForm;
import org.unitime.timetable.model.dao._RootDAO;


/** 
 * MyEclipse Struts
 * Creation date: 12-16-2005
 * 
 * XDoclet definition:
 * @struts:action path="/hibernateQueryTest" name="hibernateQueryTestForm" input="/form/hibernateQueryTest.jsp" scope="request"
 */
public class HibernateQueryTestAction extends Action {

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
		if(!Web.isLoggedIn( httpSession ) || !Web.isAdmin(httpSession)) {
            throw new Exception ("Access Denied.");
        }

		String op = request.getParameter("op");
		if(op==null || !op.equals("Submit")) 
		    return mapping.findForward("displayQueryForm");
		
        HibernateQueryTestForm frm = (HibernateQueryTestForm) form;
        ActionMessages errors =  frm.validate(mapping, request);
        
        if(errors.size()==0) {
            try {
		        String query = frm.getQuery();	        
		        _RootDAO rdao = new _RootDAO();
		        Session hibSession = rdao.getSession();	        
		        Query q = hibSession.createQuery(query);
		        List l = q.list();
		        frm.setListSize("" + l.size());
            }
            catch (Exception e) {
                errors.add("query", 
                        	new ActionMessage("errors.generic", e.getMessage()));
                Debug.error(e);
            }
        }

        saveErrors(request, errors);        
        return mapping.findForward("displayQueryForm");
        
    }

}