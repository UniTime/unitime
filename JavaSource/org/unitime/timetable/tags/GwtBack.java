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
