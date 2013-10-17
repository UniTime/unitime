/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2008 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.tags;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.security.UserContext;

/**
 * @author Tomas Muller
 */
public class Property  extends BodyTagSupport {
	private static final long serialVersionUID = -7140085371832958782L;
	private String iName;
    public String getName() { return iName; }
    public void setName(String name) { iName = name; }
	
	private boolean iUser = false;
	public boolean isUser() { return iUser; }
	public void setUser(boolean user) { iUser = user; }
	
	private UserContext getUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof UserContext)
			return (UserContext)authentication.getPrincipal();
		return null;
	}
	
	protected String getProperty(String defaultValue) {
		if (isUser() && getUser() != null) {
			return getUser().getProperty(getName(), ApplicationProperties.getProperty(getName(), defaultValue));
		} else {
			return ApplicationProperties.getProperty(getName(), defaultValue);
		}
	}

	public int doStartTag() throws JspException {
		return EVAL_BODY_BUFFERED;
	}
	
	public int doEndTag() throws JspException {
        try {
            String body = (getBodyContent()==null?null:getBodyContent().getString());
            String value = ApplicationProperties.getProperty(iName, body);
            if (value!=null)
                pageContext.getOut().println(value);
        } catch (Exception e) {
            e.printStackTrace();
            throw new JspTagException(e.getMessage());
        }
		return EVAL_PAGE;
	}	

}
