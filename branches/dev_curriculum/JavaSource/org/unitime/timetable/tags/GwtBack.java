/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.unitime.timetable.webutil.BackTracker;


/**
 * @author Tomas Muller
 */
public class GwtBack extends BodyTagSupport {
	private static final long serialVersionUID = 2328664801804591253L;
	int iBack = 2;
	
	public int getBack() { return iBack; }
	public void setBack(int back) { iBack = back; }
	
	public int doStartTag() throws JspException {
		return EVAL_BODY_BUFFERED;
	}
	
	public int doEndTag() throws JspException {
		try {
			pageContext.getOut().print(
					BackTracker.getGwtBack((HttpServletRequest)pageContext.getRequest(),getBack())
					);
		} catch (Exception e) {
			throw new JspTagException(e.getMessage());
		}
		return EVAL_PAGE;
	}
}
