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
package org.unitime.timetable.gwt.client.widgets;

import org.unitime.timetable.gwt.resources.GwtAriaMessages;
import org.unitime.timetable.gwt.resources.GwtResources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.impl.FocusImpl;

/**
 * @author Tomas Muller
 */
public class OpenCloseSectionImage extends Image implements HasValueChangeHandlers<Boolean>, HasValue<Boolean>, Focusable {
	public static final GwtResources RESOURCES =  GWT.create(GwtResources.class);
	public static final GwtAriaMessages ARIA = GWT.create(GwtAriaMessages.class);
	
	private boolean iValue = true;

	public OpenCloseSectionImage(boolean opened) {
		super(RESOURCES.treeOpen());
		setAltText(ARIA.iconSectionOpened());
		getElement().getStyle().setCursor(Cursor.POINTER);
		addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				setValue(!getValue(), true);
			}
		});
		getElement().setTabIndex(0);
		sinkEvents(Event.KEYEVENTS);
	}
		
	@Override
	public HandlerRegistration addValueChangeHandler(final ValueChangeHandler<Boolean> handler) {
		return addHandler(handler, ValueChangeEvent.getType());
	}

	@Override
	public Boolean getValue() {
		return iValue;
	}

	@Override
	public void setValue(Boolean value) {
		setValue(value, false);
	}
	
	@Override
	public void setValue(Boolean value, boolean fireEvents) {
		if (value == null) return;
		iValue = value;
		setResource(iValue ? RESOURCES.treeOpen() : RESOURCES.treeClosed());
		setAltText(iValue ? ARIA.iconSectionOpened() : ARIA.iconSectionClosed());
		if (fireEvents)
			ValueChangeEvent.fire(this, iValue);
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
