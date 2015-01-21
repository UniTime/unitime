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

import com.google.gwt.aria.client.Id;
import com.google.gwt.aria.client.Roles;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.DialogBox;

/**
 * @author Tomas Muller
 */
public class AriaDialogBox extends DialogBox {
	
	public AriaDialogBox(boolean autoHide, boolean modal, Caption caption) {
		super(autoHide, modal, caption);
    	Roles.getDialogRole().set(getElement());
    	caption.asWidget().getElement().setId(DOM.createUniqueId());
    	Roles.getDialogRole().setAriaLabelledbyProperty(getElement(), Id.of(caption.asWidget().getElement()));			
	}
	
	public AriaDialogBox(boolean autoHide, boolean modal) {
		this(autoHide, modal, new CaptionImpl());
	}
	
	public AriaDialogBox(boolean autoHide) {
		this(autoHide, true, new CaptionImpl());
	}
	
	public AriaDialogBox() {
		this(false, true, new CaptionImpl());
	}
}