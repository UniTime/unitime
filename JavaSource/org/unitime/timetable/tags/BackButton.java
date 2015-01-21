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
