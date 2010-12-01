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
package org.unitime.timetable.tags;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.TagSupport;

import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.listeners.SessionListener;


/**
 * Counts active sessions
 * @author Heston Fernandes
 */
public class SessionsTag extends TagSupport {

	/**
	 * Default method to handle start of tag.
	 */
	public int doStartTag() throws JspException {
		
        // Check Access
	    HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        if (!Web.isLoggedIn( request.getSession() ) && !Web.isAdmin( request.getSession() )) {
            throw new JspException ("Access Denied.");
        }
        
		StringBuffer html = new StringBuffer("");
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
		
		try {
			
			html.append("<TABLE border='0' cellspacing='1' cellpadding='2' width='100%'>"); 
			
			html.append("<TR>"); 
			html.append("<TD align='center'>User</TD>"); 
			html.append("<TD align='center'>Created</TD>"); 
			html.append("<TD align='center'>Last Access</TD>"); 
			html.append("</TR>"); 

			HashMap s = SessionListener.getSessions();
			Set keys = s.keySet();
			Iterator i = keys.iterator();
			
			while (i.hasNext()) {
			    String sessionId = i.next().toString();
			    HttpSession session = (HttpSession) s.get(sessionId);
			    
			    if (session!=null) {
			        User user = (User)(session.getAttribute("User"));
			        String userDetail = "Cannot be determined";
			        if (user!=null && user.getLogin()!=null) 
			            userDetail = user.getName() + " ("+ user.getCurrentRole() + ")";
			            
					html.append("<TR>"); 
					html.append("<TD align='left'>" + userDetail + "</TD>"); 
					html.append("<TD align='left'>" + sdf.format(new Date(session.getCreationTime())) + "</TD>"); 
					html.append("<TD align='left'>" + sdf.format(new Date(session.getLastAccessedTime())) + "</TD>"); 
					html.append("</TR>"); 
			    }
			}
			
			html.append("</TABLE>"); 
			
			pageContext.getOut().print(html.toString());			
		} 
		catch (Exception ex) {
			throw new JspTagException("SessionsTag: " + ex.getMessage());
		}

		return SKIP_BODY;		
	}	
	
	/**
	 * Default method to handle end of tag
	 */
	public int doEndTag() {
		return EVAL_PAGE;
	}

}
