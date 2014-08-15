/*
 * UniTime 3.4 - 3.5 (University Timetabling Application)
 * Copyright (C) 2013, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
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
