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