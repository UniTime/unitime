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

import java.util.Iterator;

import javax.servlet.jsp.JspException;

import org.apache.struts.Globals;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.taglib.TagUtils;
import org.apache.struts.taglib.html.TextTag;

/**
 * @author Tomas Muller
 */
public class CourseNumberSuggestBox extends TextTag {
	private static final long serialVersionUID = 1L;
	private String iOuterStyle = null;
	private String iConfiguration = null;
	
	public String getOuterStyle() { return iOuterStyle; }
	public void setOuterStyle(String outerStyle) { iOuterStyle = outerStyle; }
	
	public String getConfiguration() { return iConfiguration; }
	public void setConfiguration(String configuration) { iConfiguration = configuration; }
	
	public int doStartTag() throws JspException {
        ActionMessages errors = null;

        try {
            errors = TagUtils.getInstance().getActionMessages(pageContext, Globals.ERROR_KEY);
        } catch (JspException e) {
            TagUtils.getInstance().saveException(pageContext, e);
            throw e;
        }
        
        String hint = null;
        if (errors != null && !errors.isEmpty()) {
        	 String message = null;
             Iterator reports = (getProperty() == null ? errors.get() : errors.get(getProperty()));
             while (reports.hasNext()) {
            	 ActionMessage report = (ActionMessage) reports.next();
            	 if (report.isResource()) {
            		 message = TagUtils.getInstance().message(pageContext, null, Globals.LOCALE_KEY, report.getKey(), report.getValues());
            	 } else {
            		 message = report.getKey();
            	 }
            	 if (message != null && !message.isEmpty()) {
            		 hint = (hint == null ? "" : hint + "<br>") + message;
            	 }
             }
        }
        
		// setStyleClass("unitime-DateSelectionBox");
		String onchange = getOnchange(); setOnchange(null);
		TagUtils.getInstance().write(pageContext, "<span name='UniTimeGWT:CourseNumberSuggestBox' configuration=\"" + getConfiguration() + "\"" +
				(hint == null ? "" : " error=\"" + hint + "\"") +
				(onchange == null ? "" : " onchange=\"" + onchange + "\"") +
				(getOuterStyle() == null ? "" : " style=\"" + getOuterStyle() + "\"" ) +
				">");
		super.doStartTag();
		TagUtils.getInstance().write(pageContext, "</span>");
		return EVAL_BODY_BUFFERED;
	}
}