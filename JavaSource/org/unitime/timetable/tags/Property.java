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
