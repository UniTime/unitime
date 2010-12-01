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

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.BodyTagSupport;

/**
 * @author Tomas Muller
 */
public class SectionTitle  extends BodyTagSupport {
	
	public int doStartTag() throws JspException {
		return EVAL_BODY_BUFFERED;
	}
	
	public int doEndTag() throws JspException {
		if (getParent()!=null && getParent() instanceof SectionHeader) {
			((SectionHeader)getParent()).setTitle(getBodyContent().getString());
		} else {
			try {
				String body = (getBodyContent()==null?null:getBodyContent().getString());
				if (body==null || body.trim().length()==0) {
					pageContext.getOut().println("<DIV class='WelcomeRowHeadBlank'>&nbsp;</DIV>");
				} else {
					pageContext.getOut().println("<DIV class='WelcomeRowHead'>");
					pageContext.getOut().println(body);
					pageContext.getOut().println("</DIV>");
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw new JspTagException(e.getMessage());
			}
		}
		return EVAL_PAGE;
	}	

}
