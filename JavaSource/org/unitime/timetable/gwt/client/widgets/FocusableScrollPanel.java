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

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.HasBlurHandlers;
import com.google.gwt.event.dom.client.HasFocusHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.impl.FocusImpl;

public class FocusableScrollPanel extends ScrollPanel implements Focusable, HasFocusHandlers, HasBlurHandlers {
	public FocusableScrollPanel() {
		super();
		setTabIndex(0);
	}
	
	public FocusableScrollPanel(Widget child) {
		super(child);
		setTabIndex(0);
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
	
	public boolean isFocused() {
		return hasFocus(getElement());
	}

	@Override
	public HandlerRegistration addFocusHandler(FocusHandler handler) {
		return addHandler(handler, FocusEvent.getType());
	}
	
	@Override
	public HandlerRegistration addBlurHandler(BlurHandler handler) {
		return addHandler(handler, BlurEvent.getType());
	}
	
	protected native boolean hasFocus(Element element) /*-{
	   return element.ownerDocument.activeElement == element;
	}-*/; 
}
