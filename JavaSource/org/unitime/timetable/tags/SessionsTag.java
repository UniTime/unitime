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
package org.unitime.timetable.tags;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.TagSupport;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.unitime.timetable.gwt.shared.PageAccessException;
import org.unitime.timetable.listeners.SessionListener;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.Formats;


/**
 * Counts active sessions
 * @author Heston Fernandes, Tomas Muller
 */
public class SessionsTag extends TagSupport {

	private static final long serialVersionUID = 1332135385302161770L;
	
	private UserContext getUser(SecurityContext context) {
		if (context == null) return null;
		Authentication authentication = context.getAuthentication();
		if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof UserContext)
			return (UserContext)authentication.getPrincipal();
		return null;
	}
	
	private UserContext getUser() {
		return getUser(SecurityContextHolder.getContext());
	}

	/**
	 * Default method to handle start of tag.
	 */
	public int doStartTag() throws JspException {
		
        // Check Access
	    UserContext user = getUser();
        if (user == null || user.getCurrentAuthority() == null || !user.getCurrentAuthority().hasRight(Right.IsAdmin))
        	throw new PageAccessException("Access Denied.");
        
		StringBuffer html = new StringBuffer("");
		Formats.Format<Date> sdf = Formats.getDateFormat(Formats.Pattern.DATE_TIME_STAMP);
		
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
			    	session.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
			        UserContext u = getUser((SecurityContext)session.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY));
			        String userDetail = "Cannot be determined";
			        if (u != null && u.getUsername() != null) 
			            userDetail = u.getUsername() + (u.getCurrentAuthority() == null ? "" : " ("+ u.getCurrentAuthority() + ")");
			            
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
