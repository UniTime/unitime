/*
 * UniTime 3.4 - 3.5 (University Timetabling Application)
 * Copyright (C) 2012 - 2013, UniTime LLC, and individual contributors
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

import javax.servlet.ServletRequest;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.struts.Globals;
import org.unitime.commons.Debug;
import org.unitime.timetable.ApplicationProperties;

/**
 * @author Tomas Muller
 */
public class PageWarning extends TagSupport {
	private static final long serialVersionUID = 1L;
	
	private String iPrefix;
	private String iStyle;
	
	public String getPrefix() { return iPrefix; }
	public void setPrefix(String prefix) { iPrefix = prefix; }
	
	public String getStyle() { return iStyle; }
	public void setStyle(String styleName) { iStyle = styleName; }
	
	
	public String getPageWarning(ServletRequest request) {
		String page = request.getParameter("page");
		if (page != null) {
			String warning = ApplicationProperties.getProperty(getPrefix() + page);
			if (warning != null && !warning.isEmpty()) return warning;
		}
		String action = (String)request.getAttribute(Globals.ORIGINAL_URI_KEY);
		if (action != null && action.endsWith(".do")) {
			String warning = ApplicationProperties.getProperty(getPrefix() + action.substring(action.lastIndexOf('/') + 1, action.length() - 3));
			if (warning != null && !warning.isEmpty()) return warning;
		}
		return null;
	}
	
	public int doStartTag() {
		try {
			String warning = getPageWarning(pageContext.getRequest());
			if (warning != null) {
				pageContext.getOut().println("<table width='100%' border='0' cellpadding='3' cellspacing='0'><tr>");
				pageContext.getOut().println("<td class=\"" + getStyle() + "\" style='padding-left:10px;'>");
				pageContext.getOut().println(warning);
				pageContext.getOut().println("</td></tr></table>");
			}
			return SKIP_BODY;
		} catch (Exception e) {
			Debug.error(e);
			return SKIP_BODY;
		}
	}
	
	public int doEndTag() {
		return EVAL_PAGE;
	}

}
