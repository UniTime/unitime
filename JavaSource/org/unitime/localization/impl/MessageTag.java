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

import java.lang.reflect.Method;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.BodyTagSupport;

/**
 * @author Tomas Muller
 */
public class MessageTag extends BodyTagSupport {
	private static final long serialVersionUID = 1625753634874912327L;

	private String iName = null;
	public String getName() { return iName; }
	public void setName(String name) { iName = name; }

	private String iId = BundleTag.DEFAULT_ID;
	public String getId() { return iId; }
	public void setId(String id) { iId = id; }

	@Override
    public int doStartTag() {
        return EVAL_BODY_BUFFERED;
    }
    
	@Override
    public int doEndTag() throws JspException {
        try {
        	Object messages = pageContext.getAttribute(getId());
        	if (messages != null) {
                String body = (getBodyContent() == null ? null : getBodyContent().getString());
        		Method method = messages.getClass().getMethod(getName(), body == null ? new Class[]{} : new Class[] { String.class });
        		pageContext.getOut().print(method.invoke(messages, body == null ? new Object[] {} : new Object[] { body }));
        	}
        } catch (Exception e) {
            throw new JspTagException("Unable to localize message '" + getName() + "': " + e.getMessage(), e);
        }
        return EVAL_PAGE;
    }  
}
