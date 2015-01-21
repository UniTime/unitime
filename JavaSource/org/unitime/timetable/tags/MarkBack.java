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
import javax.servlet.jsp.tagext.TagSupport;

import org.unitime.timetable.webutil.BackTracker;


/**
 * @author Tomas Muller
 */
public class MarkBack extends TagSupport {
	private static final long serialVersionUID = 6247051046382946227L;
	boolean iBack = true;
	boolean iClear = false;
	String iUri = null;
	String iTitle = null;
	
	public boolean getBack() { return iBack; }
	public void setBack(boolean back) { iBack = back; }
	public boolean getClear() { return iClear; }
	public void setClear(boolean clear) { iClear = clear; }
	public String getUri() { return iUri; }
	public void setUri(String uri) { iUri = uri; }
	public String getTitle() { return iTitle; }
	public void setTitle(String title) { iTitle = title; }
	
	public int doStartTag() throws JspException {
		BackTracker.markForBack((HttpServletRequest)pageContext.getRequest(), getUri(), getTitle(), getBack(), getClear());
		return SKIP_BODY;
	}
	
	public int doEndTag() {
		return EVAL_PAGE;
	}
	
}
