/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC, and individual contributors
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
public class BackButton extends BodyTagSupport {
	private static final long serialVersionUID = 8565058511853635478L;
	String iTitle = "Return to %%";
	String iName = "Back";
	String iAccessKey = null;
	String iStyle = null;
	String iClass = null;
	String iType = null;
	String iId = null;
	int iBack = 2;
	
	public String getTitle() { return iTitle; }
	public void setTitle(String title) { iTitle = title; }
	public String getName() { return iName; }
	public void setName(String name) { iName = name; }
	public String getAccesskey() { return iAccessKey; }
	public void setAccesskey(String accessKey) { iAccessKey = accessKey; }
	public String getStyle() { return iStyle; }
	public void setStyle(String style) { iStyle = style; }
	public String getStyleClass() { return iClass; }
	public void setStyleClass(String styleClass) { iClass = styleClass; }
	public int getBack() { return iBack; }
	public void setBack(int back) { iBack = back; }
	public String getType() { return iType; }
	public void setType(String type) { iType = type; }
	public String getId() { return iId; }
	public void setId(String id) { iId = id; }
	
	public int doStartTag() throws JspException {
		return EVAL_BODY_BUFFERED;
	}
	
	public int doEndTag() throws JspException {
		try {
			String id = (getBodyContent()==null?null:getBodyContent().getString());
			pageContext.getOut().print(
					BackTracker.getBackButton((HttpServletRequest)pageContext.getRequest(),getBack(),getName(),getTitle(),getAccesskey(),getStyle(),getStyleClass(),getType(),(id==null || id.trim().length()==0?null:id.trim()))
				);
		} catch (Exception e) {
			throw new JspTagException(e.getMessage());
		}
		return EVAL_PAGE;
	}
}
