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
package org.unitime.localization.impl;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.unitime.localization.messages.Messages;
import org.unitime.localization.impl.Localization;

/**
 * @author Tomas Muller
 */
public class BundleTag extends BodyTagSupport {
	private static final long serialVersionUID = 8594907730878329848L;
	public static final String DEFAULT_ID = "MSG";
	
	private String iName = null;
	public String getName() { return iName; }
	public void setName(String name) { iName = name; }
	
	private String iId = DEFAULT_ID;
	public String getId() { return iId; }
	public void setId(String id) { iId = id; }

	@Override
	public int doStartTag() throws JspTagException {
		try {
			try {
				pageContext.setAttribute(getId(), Localization.create((Class<? extends Messages>)Class.forName(Localization.ROOT + getName())));
			} catch (ClassNotFoundException f) {
				pageContext.setAttribute(getId(), Localization.create((Class<? extends Messages>)Class.forName(getName())));
			}
		} catch (Exception e) {
            throw new JspTagException("Unable to find messages '" + getName() + "': " + e.getMessage(), e);
		}
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
