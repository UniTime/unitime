/*
 * UniTime 3.3 (University Timetabling Application)
 * Copyright (C) 2011, UniTime LLC, and individual contributors
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
package org.unitime.localization.impl;

import java.lang.reflect.Method;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.BodyTagSupport;

/**
 * @author Tomas Muller
 */
public class MessageTag extends BodyTagSupport {
	private static final long serialVersionUID = 1625753634874912327L;

	private String iName = null;
	public String getName() { return iName; }
	public void setName(String name) { iName = name; }

	private String iId = BundleTag.DEFAULT_ID;
	public String getId() { return iId; }
	public void setId(String id) { iId = id; }

	@Override
    public int doStartTag() {
        return EVAL_BODY_BUFFERED;
    }
    
	@Override
    public int doEndTag() throws JspException {
        try {
        	Object messages = pageContext.getAttribute(getId());
        	if (messages != null) {
                String body = (getBodyContent() == null ? null : getBodyContent().getString());
        		Method method = messages.getClass().getMethod(getName(), body == null ? new Class[]{} : new Class[] { String.class });
        		pageContext.getOut().print(method.invoke(messages, body == null ? new Object[] {} : new Object[] { body }));
        	}
        } catch (Exception e) {
            throw new JspTagException("Unable to localize message '" + getName() + "': " + e.getMessage(), e);
        }
        return EVAL_PAGE;
    }  
}
