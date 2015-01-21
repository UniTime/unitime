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
package org.unitime.timetable.gwt.client.aria;

import com.google.gwt.aria.client.Roles;
import com.google.gwt.user.client.ui.Anchor;

/**
 * @author Tomas Muller
 */
public class AriaAnchor extends Anchor implements HasAriaLabel {
	
	public AriaAnchor(String text) {
		super(text, false, "#");
	}
	
	public AriaAnchor(String text, boolean asHtml) {
		super(text, asHtml, "#");
	}

	@Override
	public String getAriaLabel() {
		return Roles.getLinkRole().getAriaLabelProperty(getElement());
	}

	@Override
	public void setAriaLabel(String text) {
		if (text == null || text.isEmpty())
			Roles.getLinkRole().removeAriaLabelledbyProperty(getElement());
		else
			Roles.getLinkRole().setAriaLabelProperty(getElement(), text);
	}

}
