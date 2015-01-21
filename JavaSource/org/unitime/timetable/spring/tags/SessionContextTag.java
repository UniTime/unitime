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
package org.unitime.timetable.spring.tags;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * @author Tomas Muller
 */
public class SessionContextTag extends BodyTagSupport {
	private static final long serialVersionUID = 8594907730878329848L;
	public static final String DEFAULT_ID = "MSG";
	
	@Override
	public int doStartTag() throws JspTagException {
		pageContext.setAttribute(
				"sessionContext",
				WebApplicationContextUtils.getWebApplicationContext(this.pageContext.getServletContext()).getBean("sessionContext")
				);
		return EVAL_BODY_INCLUDE;
	}
	
	@Override
	public int doEndTag() {
		return EVAL_PAGE;
	}
	
	@Override
	public int doAfterBody() {
		return SKIP_BODY;
	}

}
