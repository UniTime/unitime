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
import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.impl.FocusImpl;

/**
 * @author Tomas Muller
 */
public class ClickableHint extends Label implements HasAriaLabel, Focusable {
	
	public ClickableHint(String text) {
		super(text);
		getElement().setTabIndex(0);
		setStyleName("unitime-Hint");
		sinkEvents(Event.KEYEVENTS);
		Roles.getLinkRole().set(getElement());
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

	@Override
	public int getTabIndex() {
		return getElement().getTabIndex();
	}

	@Override
	public void setTabIndex(int index) {
		getElement().setTabIndex(index);
	}

	@Override
	public void setAccessKey(char key) {
		FocusImpl.getFocusImplForWidget().setAccessKey(getElement(), key);
	}

	@Override
	public void setFocus(boolean focused) {
		if (focused)
			getElement().focus();
		else
			getElement().blur();
	}
	
	protected void onClick() {
	    getElement().dispatchEvent(Document.get().createClickEvent(1, 0, 0, 0, 0, false, false, false, false));
	}
	
	@Override
	public void onBrowserEvent(Event event) {
		super.onBrowserEvent(event);

	    if ((event.getTypeInt() & Event.KEYEVENTS) != 0) {
	    	int type = DOM.eventGetType(event);
	    	char keyCode = (char) event.getKeyCode();
	    	switch (type) {
	        case Event.ONKEYUP:
	        	if (keyCode == ' ' || keyCode == '\n' || keyCode == '\r')
	        		onClick();
	        	break;
	    	}
	    }
	}
}
