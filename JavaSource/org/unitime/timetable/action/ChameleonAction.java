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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.util.MessageResources;
import org.unitime.commons.Debug;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.form.ChameleonForm;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.ManagerRole;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.LookupTables;


/** 
 * MyEclipse Struts
 * Creation date: 10-23-2006
 * 
 * XDoclet definition:
 * @struts:action path="/chameleon" name="chameleonForm" input="/admin/chameleon.jsp" scope="request"
 */
public class ChameleonAction extends Action {

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
		if(!Web.isLoggedIn( httpSession ) 
		        || ( !Web.isAdmin(httpSession) 
		        	 && ( httpSession.getAttribute("hdnAdminAlias")==null || !httpSession.getAttribute("hdnAdminAlias").toString().equals("1")  ))) {
            throw new Exception ("Access Denied.");
        }
		
        MessageResources rsc = getResources(request);
        User user = Web.getUser(request.getSession());        
        ChameleonForm frm = (ChameleonForm) form;
	    ActionMessages errors = null;

        String op = (request.getParameter("op")==null) 
					? (frm.getOp()==null || frm.getOp().length()==0)
					        ? (request.getAttribute("op")==null)
					                ? null
					                : request.getAttribute("op").toString()
					        : frm.getOp()
					: request.getParameter("op");		        
	        
		if(op==null || op.trim().length()==0)
		    op = rsc.getMessage("op.view");
		
		frm.setOp(op);
        
		// First Access - display blank form
        if ( op.equals(rsc.getMessage("op.view")) ) {
            LookupTables.setupTimetableManagers(request);
        }        
		
        // Change User
        if ( op.equals(rsc.getMessage("button.changeUser")) ) {
            try {
                doSwitch(request, frm, user);
                return mapping.findForward("reload");
			}
			catch(Exception e) {
				Debug.error(e);
	            errors.add("exception", 
	                    new ActionMessage("errors.generic", e.getMessage()) );
	            saveErrors(request, errors);
	            LookupTables.setupTimetableManagers(request);
				return mapping.findForward("displayForm");
			}
        }        
		
        return mapping.findForward("displayForm");
    }

    /**
     * Reads in new user attributes and reloads Timetabling for the new user
     * @param request
     * @param frm
     * @param u
     */
    private void doSwitch(
            HttpServletRequest request, 
            ChameleonForm frm, 
            User u ) throws Exception {
        
		TimetableManager tm = TimetableManager.findByExternalId(frm.getPuid());
		if (tm == null)
            throw new Exception ("User is not a Timetable Manager");

        String puid = frm.getPuid();
        while (puid.startsWith("0")) puid = puid.substring(1);
		u.setId(puid);
		u.setName(tm.getName() + " (A)");
		u.setAdmin(false);
		u.setRole("");
		u.setOtherAttributes(new Properties());
		u.setAttribute(Constants.TMTBL_MGR_ID_ATTR_NAME, tm.getUniqueId().toString());

		Vector roles = new Vector();
		if (tm.getManagerRoles() != null){
			Iterator it2 = tm.getManagerRoles().iterator();
			while (it2.hasNext()){
			    ManagerRole mr = (ManagerRole) it2.next();
			    Roles r = mr.getRole();
			    String role1 = r.getReference();
				roles.addElement(role1);
			    if(role1.equals(Roles.ADMIN_ROLE))
			        u.setAdmin(true);
			}
		}
		u.setRoles(roles);

		HashSet depts = new HashSet();
		Set dp = tm.getDepartments();
		for (Iterator i = dp.iterator(); i.hasNext(); ) {
		    Department d = (Department) i.next();
		    depts.add(d.getDeptCode());
		}
		u.setDepartments(new Vector(depts));

		// Set Session Variables
		HttpSession session = request.getSession();
		session.setAttribute("loggedOn", "true");
		session.setAttribute("hdnCallingScreen", "main.jsp");
		session.setAttribute("hdnAdminAlias", "1");
		Constants.resetSessionAttributes(session);
		
		Web.setUser(session, u);		
    }
}