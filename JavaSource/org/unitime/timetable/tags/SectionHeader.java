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

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.BodyTagSupport;

/**
 * @author Tomas Muller
 */
public class SectionHeader extends BodyTagSupport {

	private static final long serialVersionUID = 7960410331965348148L;
	private String iTitle = null;

	public String getTitle() { return iTitle; }
	public void setTitle(String title) { iTitle = title; }
	
	public int doStartTag() throws JspException {
		return EVAL_BODY_BUFFERED;
	}
	
	public int doEndTag() throws JspException {
		try {
			pageContext.getOut().println("<div class='unitime-MainTableHeader'><div class='unitime-HeaderPanel' style='margin-bottom: 0px;'>");
			if (iTitle != null) {
				pageContext.getOut().println("<div class='left'><div class='title'>" + iTitle + "</div></div>");
			}
			String body = (getBodyContent() == null ? null : getBodyContent().getString());
			if (body != null) {
				pageContext.getOut().println("<div class='right unitime-NoPrint' style='line-height: 29px; vertical-align: bottom; font-size: small;'>");
				pageContext.getOut().println(body);
				pageContext.getOut().println("</div>");
			}
			pageContext.getOut().println("</div></div>");
			/*
			pageContext.getOut().println("<table class='BottomBorder' width='100%'><tr><td width='100%' nowrap>");
			pageContext.getOut().println("<DIV class='WelcomeRowHeadNoLine'>");
			if (iTitle!=null)
				pageContext.getOut().println(iTitle);
			pageContext.getOut().println("</DIV>");
			pageContext.getOut().println("</td><td style='padding-bottom: 3px' nowrap>");
			String body = (getBodyContent()==null?null:getBodyContent().getString());
			if (body!=null)
				pageContext.getOut().println(body);
			pageContext.getOut().println("</td></tr></table>");
			*/
		} catch (Exception e) {
			throw new JspTagException(e.getMessage());
		}
		return EVAL_PAGE;
	}
}
