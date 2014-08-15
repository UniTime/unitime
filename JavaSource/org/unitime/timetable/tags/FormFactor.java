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

import javax.servlet.jsp.tagext.TagSupport;

import org.unitime.timetable.defaults.SessionAttribute;

/**
 * @author Tomas Muller
 */
public class FormFactor extends TagSupport {
	private static final long serialVersionUID = -1L;
	private String iValue;
	public String getValue() { return iValue; }
	public void setValue(String value) { iValue = value; }
	
	public int doStartTag() {
		String ff = (String)pageContext.getSession().getAttribute(SessionAttribute.FormFactor.key());
		if ("unknown".equals(getValue()) && ff == null) return EVAL_BODY_INCLUDE;
		if (ff == null) ff = "desktop";
		if (ff.equalsIgnoreCase(getValue())) return EVAL_BODY_INCLUDE;
		else if ("mobile".equalsIgnoreCase(getValue()) && (ff.equals("phone") || ff.equals("tablet"))) return EVAL_BODY_INCLUDE;
		else return SKIP_BODY;
	}
	
	public int doEndTag() {
		return EVAL_PAGE;
	}
}