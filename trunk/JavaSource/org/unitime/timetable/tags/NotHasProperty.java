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

import javax.servlet.jsp.tagext.TagSupport;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.security.UserContext;


/**
 * @author Tomas Muller
 */
public class NotHasProperty extends TagSupport {
	private static final long serialVersionUID = -2188052467367119211L;
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
	
	protected String getProperty() {
		if (isUser() && getUser() != null) {
			return getUser().getProperty(getName(), ApplicationProperties.getProperty(getName()));
		} else {
			return ApplicationProperties.getProperty(getName());
		}
	}
	
	public int doStartTag() {
        try {
            String value = getProperty();
            if (value==null || value.length()==0) return EVAL_BODY_INCLUDE;
        } catch (Exception e) {}
        return SKIP_BODY;
	}
	
	public int doEndTag() {
		return EVAL_PAGE;
	}
}
