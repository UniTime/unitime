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
package org.unitime.commons.web;


import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import javax.servlet.http.HttpSession;

import org.unitime.commons.User;


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
    public static boolean isLoggedIn(HttpSession session) {
        return (session.getAttribute(Web.USER_ATTR_NAME) != null
                && session.getAttribute(Web.USER_ATTR_NAME) instanceof User);
    }

    /** Get logged-in user for the given session. */
    public static User getUser(HttpSession session) {
        if (!isLoggedIn(session)) {
            return null;
        }
        return (User) session.getAttribute(Web.USER_ATTR_NAME);
    }

    /** Set logged-in user object for the given session. */
    public static void setUser(HttpSession session, User user) {
        session.setAttribute(Web.USER_ATTR_NAME, user);
    }

    /** Is the logged-in user administrator? */
    public static boolean isAdmin( HttpSession session ) {
    	
        User user = getUser(session);
        return (user == null ? false : user.isAdmin());
    }
    
    /**
     * Checks to see if the logged-in user has one of a set of roles
     * @param session
     * @param roles String[]
     */
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
