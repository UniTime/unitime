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

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.unitime.localization.messages.Messages;
import org.unitime.localization.impl.Localization;

/**
 * @author Tomas Muller
 */
public class BundleTag extends BodyTagSupport {
	private static final long serialVersionUID = 8594907730878329848L;
	public static final String DEFAULT_ID = "MSG";
	
	private String iName = null;
	public String getName() { return iName; }
	public void setName(String name) { iName = name; }
	
	private String iId = DEFAULT_ID;
	public String getId() { return iId; }
	public void setId(String id) { iId = id; }

	@Override
	public int doStartTag() throws JspTagException {
		try {
			pageContext.setAttribute(getId(), Localization.create(
					(Class<? extends Messages>)Class.forName(Localization.ROOT + getName())));
		} catch (Exception e) {
            throw new JspTagException("Unable to find messages '" + getName() + "': " + e.getMessage(), e);
		}
		return EVAL_BODY_INCLUDE;
	}
	
	@Override
	public int doEndTag() {
		return EVAL_PAGE;
	}
	
	@Override
	public int doAfterBody() {
		return SKIP_BODY;
	}

}
