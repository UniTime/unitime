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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.struts2.components.Submit;
import org.apache.struts2.util.ValueStack;
import org.apache.struts2.views.annotations.StrutsTag;
import org.unitime.timetable.action.UniTimeAction;

/**
 * @author Tomas Muller
 */
@StrutsTag(name = "button", tldTagClass = "org.unitime.timetable.tags.ButtonTag", description = "GWT-like button")
public class Button extends Submit {

	public Button(final ValueStack stack, final HttpServletRequest request, final HttpServletResponse response) {
        super(stack, request, response);
    }
	
	public void evaluateParams() {
		if (name == null)
			name = "op";
        if (value != null && value.startsWith("%")) {
        	String name = (String)findValue(value);
        	String access = UniTimeAction.guessAccessKey(name);
        	if (access != null) {
        		accesskey = access.toString();
        		value = UniTimeAction.stripAccessKey(name);
        		title = this.value + " (Alt + " + this.accesskey.toUpperCase() + ")";
        	}
        }
        super.evaluateParams();
	}
}
