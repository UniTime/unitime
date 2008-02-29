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
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.unitime.commons.Debug;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.commons.web.WebTable;
import org.unitime.timetable.form.RoleListForm;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.ManagerRole;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.ManagerRoleDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.util.Constants;


/**
 * MyEclipse Struts
 * Creation date: 03-17-2005
 *
 * XDoclet definition:
 * @struts:action path="/selectPrimaryRole" name="roleListForm" input="/selectPrimaryRole.jsp" scope="request" validate="true"
 * @struts:action-forward name="success" path="/main.jsp" contextRelative="true"
 * @struts:action-forward name="fail" path="/selectPrimaryRole.jsp" contextRelative="true"
 */
public class RoleListAction extends Action {

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

        HttpSession webSession = request.getSession();
        User user = Web.getUser(webSession);
        RoleListForm roleListForm = (RoleListForm) form;

        // Check user is logged in
        if(user==null)
            return(mapping.findForward("loginRequired"));

        // Get manager object
        TimetableManager tm = TimetableManager.getManager(user);        
        if(tm==null)
            return(mapping.findForward("loginRequired"));
 
        // Check App Access Level
    	String appAccessLevel = (String) webSession.getAttribute(Constants.CFG_APP_ACCESS_LEVEL);
    	if (appAccessLevel==null || appAccessLevel.trim().length()==0) {
            return(mapping.findForward("loginRequired"));
    	}
        
    	// All authorized users can access the application
        boolean aclSet = false;
    	Set departments = tm.getDepartments();

    	if (appAccessLevel.equalsIgnoreCase(Constants.APP_ACL_ALL)) {
    	    webSession.setAttribute(Constants.SESSION_APP_ACCESS_LEVEL, "true");
    	    aclSet = true;
    	}        	

    	// Get roles
        Vector roles = new Vector();
        for(Iterator it = tm.getManagerRoles().iterator();it.hasNext();){
            ManagerRole mr = (ManagerRole) it.next();
        	roles.add(mr.getRole().getReference());
        	
        	if (!appAccessLevel.equalsIgnoreCase(Constants.APP_ACL_ALL)) {
        	    
        	    // If user possesses admin role (may not be default) - allow user
            	if (mr.getRole().getReference().equals(Roles.ADMIN_ROLE)) {
            	    webSession.setAttribute(Constants.SESSION_APP_ACCESS_LEVEL, "true");
            	    aclSet = true;
            	}
            	
            	if (!aclSet) {
                	StringTokenizer strTok = new StringTokenizer(appAccessLevel, ":");
            	    outerLoop: while (strTok.hasMoreTokens()) {
            	        String elem = strTok.nextToken();
            	        // Check department access
            	        if (!elem.equalsIgnoreCase(Constants.APP_ACL_ADMIN) && elem!=null) {
            	            for (Iterator deptIter=departments.iterator(); deptIter.hasNext(); ) {
            	                Department dept = (Department) deptIter.next();
            	                if (elem.trim().equalsIgnoreCase(dept.getDeptCode())) {
            	            	    webSession.setAttribute(Constants.SESSION_APP_ACCESS_LEVEL, "true");
            	                    aclSet = true;
            	                    break outerLoop;
            	                }
            	            }
            	        }
            	    }
            	}
        	}        	
        }
        
    	if (!aclSet) {
    	    webSession.setAttribute(Constants.SESSION_APP_ACCESS_LEVEL, "false");
    	}
        
        // Lookup acad sessions for the roles
        ManagerRole defaultRole = setUpRoles(request, user);
        
        /*
        if (defaultRole!=null) {
            Set sessions = (Roles.ADMIN_ROLE.equals(defaultRole.getRole().getReference())?Session.getAllSessions():defaultRole.getTimetableManager().sessionsCanManage());
            //If only one role exists - redirect to main menu  
            if (sessions.size()==1 && setPrimaryRole(webSession, user, defaultRole, (Session)sessions.iterator().next()))
                return mapping.findForward("success");
        }
        */

        // Form submitted
        if (roleListForm.getRoleId()!=null && roleListForm.getSessionId()!=null) {
            ManagerRole role = new ManagerRoleDAO().get(roleListForm.getRoleId());
            Session session = new SessionDAO().get(roleListForm.getSessionId());
            if (setPrimaryRole(webSession, user, role, session))
                return mapping.findForward("success");
        }
        
        // Role/session list not requested -- try assign default role/session first 
        if (!"Y".equals(request.getParameter("list"))) {
            if (setPrimaryRole(webSession, user, defaultRole, null))
                return mapping.findForward("success");
        }
        
        if (tm.getManagerRoles().size()>1)
            return(mapping.findForward("getUserSelectedRole"));
        else
            return(mapping.findForward("getDefaultAcadSession"));
    }

    /**
     * Looks up roles for the current user and saves it in the User object
     * @param request HttpServletRequest object
     * @param user User object
     */
    private ManagerRole setUpRoles (HttpServletRequest request, User user) throws Exception {
        
        ManagerRole defaultRole = null;
 	    Vector roleSessions = (Vector) user.getAttribute(Roles.USER_ROLES_ATTR_NAME);
 	    
 	    roleSessions = new Vector();
 	   
 	    TimetableManager tm = TimetableManager.getManager(user);
 	    Set sList = new TreeSet(Session.getAllSessions());
 	    
        WebTable table = new WebTable(4,"Select "+(tm.getManagerRoles().size()>1?"User Role &amp; ":"")+"Academic Session",
                new String[] { "User Role", "Academic Session", "Academic Initiative", "Academic Session Status" },
                new String[] { "left", "left", "left", "left"},
                null);
        
        Object currentSessionId = user.getAttribute(Constants.SESSION_ID_ATTR_NAME);

        int nrLines = 0;
        
        for (Iterator i=tm.getManagerRoles().iterator(); i.hasNext();) {
 	        ManagerRole mr = (ManagerRole)i.next();
 	        
            if (mr.isPrimary().booleanValue()) defaultRole = mr;

            boolean currentRole = mr.getRole().getReference().equals(user.getCurrentRole());
 	        
            Set sessions = Session.availableSessions(mr);
            
            for (Iterator j=sessions.iterator();j.hasNext();) {
                Session  session = (Session)j.next();
 	               
                String onClick = 
                    "onClick=\"roleListForm.roleId.value="+mr.getUniqueId()+";roleListForm.sessionId.value="+session.getUniqueId()+";roleListForm.submit();\"";
                
                String bgColor = 
                    (currentRole && session.getUniqueId().equals(currentSessionId)?"rgb(168,187,225)":null);
                
                table.addLine(
                        onClick,
                        new String[] {
                            mr.getRole().getAbbv(),
                            session.getAcademicYear()+" "+session.getAcademicTerm(),
                            session.getAcademicInitiative(),
                            (session.getStatusType()==null?"":session.getStatusType().getLabel())}, null)
               .setBgColor(bgColor);
                   
               nrLines++;
            } 	               
 	    }
        
        if (tm.getManagerRoles().isEmpty())
            table.addLine(new String[] {"<i><font color='red'>No user role associated with timetabling manager "+tm.getName()+".</font></i>",null,null,null}, null);
        else if (nrLines==0)
            table.addLine(new String[] {"<i><font color='red'>No academic session associated with timetabling manager "+tm.getName()+".</font></i>",null,null,null}, null);
 	    
 	    if (defaultRole==null && tm.getManagerRoles().size()==1)
 	       defaultRole = (ManagerRole)tm.getManagerRoles().iterator().next();
 	    
 	    request.setAttribute(Roles.USER_ROLES_ATTR_NAME, table.printTable());
 	    
 	    Web.setUser(request.getSession(), user);
 	    
		return defaultRole;
    }

    /**
     * Parse the role token to set the role and academic year
     * @param webSession Http Session object of the user
     * @param user User object
     * @param role Manager role
     * @param session Academic session (default session will be taken if null)
     * @return true if primary role was set
     */
    private boolean setPrimaryRole(HttpSession webSession, User user, ManagerRole role, Session session) throws Exception {
        
        if (role==null) return false;
        if (session==null)
            session = Session.defaultSession(role);
        
        if (session==null) return false;
        
        TimetableManager tm = TimetableManager.getManager(user);
        if(tm == null)
            throw new Exception("Timetable manager could not be loaded for user "+user.getLogin()+".");
        
        if (!tm.getManagerRoles().contains(role)) 
            throw new Exception("Timetable manager "+tm.getName()+" does not have requested role "+role.getRole().getReference()+".");
        
        if (!Session.availableSessions(role).contains(session))
            throw new Exception("Timetable manager "+tm.getName()+" cannot manage requested academic session "+session.getAcademicYear()+" "+session.getAcademicTerm()+" "+session.getAcademicInitiative()+".");
        
        Constants.resetSessionAttributes(webSession);

        user.setAdmin(Roles.ADMIN_ROLE.equals(role.getRole().getReference()));
        user.setRole(role.getRole().getReference());
        user.setAttribute(Constants.SESSION_ID_ATTR_NAME, session.getUniqueId());
        user.setAttribute(Constants.ACAD_YRTERM_ATTR_NAME, session.getAcademicYearTerm());
        user.setAttribute(Constants.TMTBL_MGR_ID_ATTR_NAME, tm.getUniqueId().toString());
        user.setAttribute(Constants.ACAD_YRTERM_LABEL_ATTR_NAME, session.getLabel());

        Debug.debug("Current Role: " + user.getRole());
		Debug.debug("Acad Session Id: " + user.getAttribute(Constants.SESSION_ID_ATTR_NAME));
		Debug.debug("Acad Year Term: " + user.getAttribute(Constants.ACAD_YRTERM_ATTR_NAME));
		Debug.debug("Acad Year Term Label: " + user.getAttribute(Constants.ACAD_YRTERM_LABEL_ATTR_NAME));
		Debug.debug("Timetable Manager Id: " + user.getAttribute(Constants.TMTBL_MGR_ID_ATTR_NAME));

        Web.setUser(webSession, user);
        
        return true;
    }
}