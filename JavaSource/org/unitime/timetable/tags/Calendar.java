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

import java.util.Iterator;

import javax.servlet.jsp.JspException;

import org.apache.struts.Globals;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.taglib.TagUtils;
import org.apache.struts.taglib.html.TextTag;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.GwtConstants;

/**
 * @author Tomas Muller
 */
public class Calendar extends TextTag {
	private static final long serialVersionUID = 1L;
	private static GwtConstants CONSTANTS = Localization.create(GwtConstants.class);
	
	private String iFormat = CONSTANTS.dateEntryFormat();
	private String iOuterStyle = null;
	
	public String getFormat() { return iFormat; }
	public void setFormat(String format) { iFormat = format; }
	
	public String getOuterStyle() { return iOuterStyle; }
	public void setOuterStyle(String outerStyle) { iOuterStyle = outerStyle; }
	
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
        
		setStyleClass("unitime-DateSelectionBox");
		String onchange = getOnchange(); setOnchange(null);
		TagUtils.getInstance().write(pageContext, "<span name='UniTimeGWT:Calendar' format=\"" + getFormat() + "\"" +
				(hint == null ? "" : " error=\"" + hint + "\"") +
				(onchange == null ? "" : " onchange=\"" + onchange + "\"") +
				(getOuterStyle() == null ? "" : " style=\"" + getOuterStyle() + "\"" ) +
				">");
		super.doStartTag();
		TagUtils.getInstance().write(pageContext, "</span>");
		return EVAL_BODY_BUFFERED;
	}
}
