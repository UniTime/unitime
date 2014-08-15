/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2014, UniTime LLC, and individual contributors
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

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.util.Constants;

/**
 * @author Tomas Muller
 */
public class PageFile extends BodyTagSupport {
	private static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	private static final long serialVersionUID = -1l;

	public int doStartTag() throws JspException {
        return EVAL_BODY_BUFFERED;
    }

    public int doEndTag() throws JspException {
    	try {
        	String url = (String)pageContext.getRequest().getAttribute(Constants.REQUEST_OPEN_URL);
        	if (url != null) {
        		pageContext.getOut().println("<script language='JavaScript'>window.open('" + url + "');</script>");
        		pageContext.getOut().println("<div class='unitime-PageMessage' onMouseOver='this.style.backgroundColor='#BBCDD0';' onMouseOut='this.style.backgroundColor='#DFE7F2';'>");
        		pageContext.getOut().println("<a class='noFancyLinks' href='" + url + "' target='_blank'>");
        		pageContext.getOut().println(MESSAGES.pageBlockedPopup());
        		pageContext.getOut().println("</a></div>");
        	}
    	} catch (IOException e) {}
		return EVAL_PAGE;
    }

}