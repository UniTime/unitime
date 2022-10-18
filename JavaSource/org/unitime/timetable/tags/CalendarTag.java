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
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.components.Component;
import org.apache.struts2.views.jsp.ui.TextFieldTag;

import com.opensymphony.xwork2.util.ValueStack;

public class CalendarTag extends TextFieldTag {
	private static final long serialVersionUID = -5915871382762128064L;
	
	private String format = null;
	private String outerStyle = null;

	public void setFormat(String format) { this.format = format; }
	public String getFormat() { return format; }
	
	public void setOuterStyle(String outerStyle) { this.outerStyle = outerStyle; }
	public String getOuterStyle() { return outerStyle; }

	@Override
    public Component getBean(final ValueStack stack, final HttpServletRequest req, final HttpServletResponse res) {
        return new Calendar(stack, req, res);
    }
	
	protected void populateParams() {
        super.populateParams();
        Calendar calendar = ((Calendar) component);
        calendar.setFormat(format);
        calendar.setOuterStyle(outerStyle);
	}
}
