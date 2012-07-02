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
package org.unitime.commons.web;


import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import javax.servlet.http.HttpSession;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.unitime.commons.User;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.security.UserAuthority;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.UserQualifier;
import org.unitime.timetable.security.authority.RoleAuthority;
import org.unitime.timetable.util.Constants;


/**
 * This class provides several simple methods directly used from JSP files 
 * (project version, data versions, login/logout, table orderings, column filters). 
 * It also provides all needed initialization.
 *
 * @author Tomas Muller
 */
public class Web {

    /** double format */
    private static DecimalFormat sDoubleFormat = new DecimalFormat("0.0",
            new DecimalFormatSymbols(Locale.US));

    /** Represents the attribute name of the User object stored in HttpSession */
    public static String USER_ATTR_NAME = "User";
	    
    // Session dependent methods
	      
    /** Is someone logged
     * @return true, if a user of the given session is logged in.
     */
    @Deprecated
    public static boolean isLoggedIn(HttpSession session) {
    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    	return auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof UserContext;
    }

    /** Get logged-in user for the given session. */
    @Deprecated
    public static User getUser(HttpSession session) {
    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    	if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof UserContext)) return null;
    	UserContext user = (UserContext)auth.getPrincipal();
    	User legacy = new User();
    	legacy.setId(user.getExternalUserId());
    	legacy.setName(user.getName());
    	legacy.setLogin(user.getUsername());
    	Vector<String> roles = new Vector<String>();
    	for (UserAuthority role: user.getAuthorities()) {
    		if (role instanceof RoleAuthority)
    			roles.add(role.getRole());
    	}
    	UserAuthority authority = user.getCurrentAuthority();
    	if (authority != null) {
    		if (authority instanceof RoleAuthority) {
    			legacy.setRole(authority.getRole());
    		}
        	if (authority.getAcademicSession() != null) {
        		legacy.setAttribute(Constants.SESSION_ID_ATTR_NAME, authority.getAcademicSession().getQualifierId());
        		legacy.setAttribute(Constants.ACAD_YRTERM_LABEL_ATTR_NAME, authority.getAcademicSession().getQualifierLabel());
        		legacy.setAttribute(Constants.ACAD_YRTERM_ATTR_NAME, authority.getAcademicSession().getQualifierReference());
        		List<? extends UserQualifier> managers = authority.getQualifiers("TimetableManager");
        		if (!managers.isEmpty())
        			legacy.setAttribute(Constants.TMTBL_MGR_ID_ATTR_NAME, managers.get(0).getQualifierId().toString());
        	}
    	}
    	if (Roles.ADMIN_ROLE.equals(legacy.getRole())) legacy.setAdmin(true);
    	session.setAttribute(Web.USER_ATTR_NAME, legacy);
    	return legacy;
    }

    /** Set logged-in user object for the given session. */
    @Deprecated
    public static void setUser(HttpSession session, User user) {
        session.setAttribute(Web.USER_ATTR_NAME, user);
    }

    /** Is the logged-in user administrator? */
    @Deprecated
    public static boolean isAdmin( HttpSession session ) {
    	
        User user = getUser(session);
        return (user == null ? false : user.isAdmin());
    }
    
    /**
     * Checks to see if the logged-in user has one of a set of roles
     * @param session
     * @param roles String[]
     */
    @Deprecated
    public static boolean hasRole(HttpSession session, String[] roles) {
    	
    	boolean result = false;
    	
    	if(roles != null && roles.length > 0) {
    		User user = getUser(session);
    		if(user != null) {
	    		for(int i = 0; i < roles.length; i++) {
	    			if(user.hasRole(roles[i])) {
	    				result = true;
	    				break;
	    			}
				}
    		}
    	}
    	
    	return result;
    }
    

    // Number Formatting Methods
    
    public static String format(double x) {
        return sDoubleFormat.format(x);
    }


    // HTML Formatting Methods
	
    /** <META> expire to be added to HTML header */
    public static String metaExpireNow() {
        return "<META http-equiv=\"Expires\" content=\"Mon, 1 Jan 1970 01:00:00 GMT\">";
    }   
    
}
