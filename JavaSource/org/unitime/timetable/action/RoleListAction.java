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
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
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
import org.hibernate.criterion.Restrictions;
import org.unitime.commons.Debug;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.form.RoleListForm;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.ManagerRole;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.TimetableManager;
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
        ManagerRole mr = null;
        for(Iterator it = tm.getManagerRoles().iterator();it.hasNext();){
        	mr = (ManagerRole) it.next();
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
            	        if (!elem.equalsIgnoreCase(Constants.APP_ACL_ADMIN)
            	                && elem!=null && elem.length()==4) {
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
        
		// User possesses at least one role
		if(roles.size()>=1) {

		    // Lookup acad sessions for the roles
	        String defaultRole = setUpRoles(request, user);

	        // If only one role exists - redirect to main menu
	        Vector reqRoles = (Vector) request.getAttribute(Roles.USER_ROLES_ATTR_NAME);
	        if(reqRoles.size()==1) {
	            String primaryRole = (String) reqRoles.elementAt(0);
	            setPrimaryRole(webSession, user, primaryRole);
	            if(issetUserAcadSession(user)) {
	                Constants.resetSessionAttributes(webSession);
    	            roleListForm.setPrimaryRole(user.getRole(), user.getAttribute(Constants.ACAD_YRTERM_ATTR_NAME));
    	            return(mapping.findForward("success"));
	            }
	            else {
    	            return(mapping.findForward("getDefaultAcadSession"));
	            }
	        }

	        // Check form submission
            String action = roleListForm.getAction();
            if(action==null || !action.trim().equals("selectRole")) {

    	        // If default role found - redirect to main menu
    	        String listAll = request.getParameter("list");
    	        if( (listAll==null || !listAll.equals("Y"))
    	                && (defaultRole!=null && defaultRole.trim().length()>0) ) {
    	            setPrimaryRole(webSession, user, defaultRole);
    	            if(issetUserAcadSession(user)) {
    	                Constants.resetSessionAttributes(webSession);
	    	            roleListForm.setPrimaryRole(user.getRole(), user.getAttribute(Constants.ACAD_YRTERM_ATTR_NAME));
	    	            return(mapping.findForward("success"));
    	            }
    	            else {
	    	            return(mapping.findForward("getDefaultAcadSession"));
    	            }
    	        }

    	        // Else prompt for selection
	            roleListForm.setPrimaryRole(user.getRole(), user.getAttribute(Constants.ACAD_YRTERM_ATTR_NAME));
                return(mapping.findForward("getUserSelectedRole"));
            }

            // Form submitted
            else {
                // Validate input - user has selected a role
                ActionMessages errors = roleListForm.validate( mapping, request );

                // Validation error - redirect back to input jsp
                if ( errors != null && !errors.isEmpty() ) {
                    saveErrors(request, errors);
                    return (new ActionForward(mapping.getInput()));
                }

                // Validation success - save to user object
                String primaryRole = roleListForm.getPrimaryRole();
                setPrimaryRole(webSession, user, primaryRole);
	            if(issetUserAcadSession(user)) {
	                Constants.resetSessionAttributes(webSession);
	                return(mapping.findForward("success"));
	            }
	            else {
		            return(mapping.findForward("getDefaultAcadSession"));
	            }
           }
		}

		// No roles found - Generate error message
		ActionMessages errors = new ActionMessages();
		errors.add("primaryRole", new ActionMessage("errors.lookup.primaryRole.notFound"));
		return(mapping.findForward("fail"));
    }

    /**
     * Looks up roles for the current user and saves it in the User object
     * @param request HttpServletRequest object
     * @param user User object
     */
    private String setUpRoles (HttpServletRequest request, User user) {

        String defaultRole = "";
 	    Vector roleSessions = (Vector) user.getAttribute(Roles.USER_ROLES_ATTR_NAME);

		try {
		    roleSessions = new Vector();

		    TimetableManager tm = TimetableManager.getManager(user);
		    ManagerRole mr = null;
		    Iterator iterUserRoles = tm.getManagerRoles().iterator();
		    List sList = Session.getAllSessions();
		    
			while (iterUserRoles.hasNext()) {
			    mr = (ManagerRole) iterUserRoles.next();	
			    
			    String roleRef = mr.getRole().getReference();
			    
			    if(roleRef != null && roleRef.length() > 0) {			        
			        if (mr.getRole().getReference().equals(Roles.ADMIN_ROLE)){
			        // Loop through all sessions and add role to each session
				        Iterator iterS = sList.iterator();
				        while(iterS.hasNext()) {
				        	Session  sessn = (Session)iterS.next();
				            String roleRef1 = sessn.getAcademicYear() 
				            					+ " " + sessn.getAcademicTerm() 
				            					+ "-" + roleRef; 
					        roleSessions.addElement(roleRef1);
				        }
			        } else {
			        	Iterator iterS = tm.sessionsCanManage().iterator();
				        while(iterS.hasNext()) {
				        	Session  sessn = (Session)iterS.next();
				            String roleRef1 = sessn.getAcademicYear() 
				            					+ " " + sessn.getAcademicTerm() 
				            					+ "-" + roleRef; 
					        roleSessions.addElement(roleRef1);
				        }
			        }
			    }
			    else
			        roleSessions.addElement(roleRef);

			    if(mr.isPrimary().booleanValue())
			        defaultRole = roleRef;
			}

			// Add to request scope
			request.setAttribute(Roles.USER_ROLES_ATTR_NAME, roleSessions);

			// Save User Attribute to cache roles
			user.setAttribute(Roles.USER_ROLES_ATTR_NAME, roleSessions);
			Web.setUser(request.getSession(), user);
		}
		catch (Exception ex) {
		    Debug.error(ex);
		}
		finally {
			//if (hibSession!=null && hibSession.isOpen()) hibSession.close();
		}

		return defaultRole;
    }

    /**
     * Parse the role token to set the role and academic year
     * @param webSession Http Session object of the user
     * @param user User object
     * @param roleToken Role Token (of the form {acadYearTerm}-{role})
     */
    private void setPrimaryRole(HttpSession webSession, User user, String roleToken)
    		throws Exception {

        user.setAttribute(Constants.TMTBL_MGR_ID_ATTR_NAME, "");
        user.setAttribute(Constants.SESSION_ID_ATTR_NAME, "");
        user.setAttribute(Constants.ACAD_YRTERM_ATTR_NAME, "");
        user.setAttribute(Constants.ACAD_YRTERM_LABEL_ATTR_NAME, "");

        if(roleToken.equals(Roles.ADMIN_ROLE)) {
            // Load default Session
            setUserRoleAcadSession(user, null, roleToken);
        }
        else {
            user.setAdmin(false);

            int indx = roleToken.indexOf("-");
            if(indx<0)
            	setUserRoleAcadSession(user,null,roleToken);
            else {
	            String acadYearTerm = roleToken.substring(0, indx);
	            String currentRole = roleToken.substring(indx+1);
	
	            user.setRole(currentRole);
	            user.setAttribute(Constants.ACAD_YRTERM_ATTR_NAME, acadYearTerm);
	
	            // Load acad session for the Acad Year Term
	            org.hibernate.Session hibSession = null;
	
	    		try {
	    			TimetableManager tm = TimetableManager.getManager(user);
	    			if(tm == null){
	    				throw new Exception("Timetable Manager Record could not be loaded for - " + user.getLogin());
	    			}
	
	    		    // Get Session Id
	    			int indx2 = acadYearTerm.indexOf(' ');
	    			String acadYear = acadYearTerm.substring(0, indx2).trim();
	    			String acadTerm = acadYearTerm.substring(indx2).trim();
	    			
	    			hibSession = new SessionDAO().getSession();
	    			List sessionList = hibSession.createCriteria(Session.class)
	    										.add(Restrictions.eq("academicYear", acadYear))
	    										.add(Restrictions.eq("academicTerm", acadTerm))
	    										.list();
	    			Iterator iterSessions = sessionList.iterator();
	    			
	    			if (iterSessions.hasNext()) {
	    	            setUserRoleAcadSession(user, (Session) iterSessions.next(), currentRole);
	    			}
	    			else {
	    			    throw new Exception("Academic Session could not be loaded for - " + acadYearTerm);
	    			}
			        user.setAttribute(Constants.TMTBL_MGR_ID_ATTR_NAME, tm.getUniqueId().toString());
	   			
	    		}
	    		catch (Exception ex) {
	    		    Debug.error(ex);
	    		    throw (ex);
	    		}
            }
        }

        Debug.debug("Current Role: " + user.getRole());
		Debug.debug("Acad Session Id: " + user.getAttribute(Constants.SESSION_ID_ATTR_NAME));
		Debug.debug("Acad Year Term: " + user.getAttribute(Constants.ACAD_YRTERM_ATTR_NAME));
		Debug.debug("Acad Year Term Label: " + user.getAttribute(Constants.ACAD_YRTERM_LABEL_ATTR_NAME));
		Debug.debug("Timetable Manager Id: " + user.getAttribute(Constants.TMTBL_MGR_ID_ATTR_NAME));

        Web.setUser(webSession, user);
    }
    
    /**
     * Sets the user attributes for Session Id and Acad Year Term
     * If Acad Session object is null then default Session is loaded
     * @param user User object
     * @param acadSession Academic Session Object
     * @return true is academic session is set, false otherwise
     */
    private boolean setUserRoleAcadSession(
            User user, 
            Session acadSession, 
            String roleToken) throws Exception {
        
        // Set admin
        if(roleToken.equals(Roles.ADMIN_ROLE)) 
            user.setAdmin(true);
        else
            user.setAdmin(false);
		TimetableManager tm = TimetableManager.getManager(user);
		if(tm == null){
			throw new Exception("Timetable Manager Record could not be loaded for - " + user.getLogin());
		}
		user.setAttribute(Constants.TMTBL_MGR_ID_ATTR_NAME, tm.getUniqueId().toString());

		// Set Role
        user.setRole(roleToken);

        // Get Default Acad Session
        if(acadSession==null)
            acadSession = Session.defaultSession();
        
        // Default not found - return to prompt user to select one
        if(acadSession==null)
            return false;
        
        // Set Acad Session details
	    user.setAttribute(Constants.SESSION_ID_ATTR_NAME, acadSession.getSessionId());
	    user.setAttribute(Constants.ACAD_YRTERM_ATTR_NAME, acadSession.getAcademicYearTerm());
	    user.setAttribute(Constants.ACAD_YRTERM_LABEL_ATTR_NAME, acadSession.getLabel());
	    
        Debug.debug("Current Role: " + user.getRole());
		Debug.debug("Acad Session Id: " + user.getAttribute(Constants.SESSION_ID_ATTR_NAME));
		Debug.debug("Acad Year Term: " + user.getAttribute(Constants.ACAD_YRTERM_ATTR_NAME));
		Debug.debug("Acad Year Term Label: " + user.getAttribute(Constants.ACAD_YRTERM_LABEL_ATTR_NAME));
		Debug.debug("Timetable Manager Id: " + user.getAttribute(Constants.TMTBL_MGR_ID_ATTR_NAME));

	    return true;
    }

    /**
     * Check if user acad session is set
     * @param user User object
     * @return true is acad session is set, false otherwise
     */
    private boolean issetUserAcadSession(User user) {
        
        Object s = user.getAttribute(Constants.SESSION_ID_ATTR_NAME);
        if(s==null || s.toString().trim().length()==0)
            return false;
        
        return true;
    }
}