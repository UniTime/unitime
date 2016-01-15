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

import com.google.gwt.aria.client.CheckedValue;
import com.google.gwt.aria.client.Roles;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Image;

/**
 * @author Tomas Muller
 */
public class AriaToggleButton extends Image implements HasAriaLabel, HasValue<Boolean>, Focusable {
	private ImageResource iCheckedFace, iUncheckedFace;
	private boolean iValue = false;

	public AriaToggleButton(ImageResource checked, ImageResource unchecked) {
		iCheckedFace = checked; iUncheckedFace = unchecked;
		setResource(iUncheckedFace);
		Roles.getCheckboxRole().set(getElement());
		Roles.getCheckboxRole().setAriaCheckedState(getElement(), CheckedValue.FALSE);
		setTabIndex(0);
		sinkEvents(Event.ONKEYUP | Event.ONCLICK);
	}

	@Override
	public String getAriaLabel() {
		return getAltText();
	}

	@Override
	public void setAriaLabel(String text) {
		setAltText(text);
	}

	@Override
	public void setAltText(String altText) {
		super.setAltText(altText);
		if (getTitle() == null || getTitle().isEmpty())
			setTitle(altText);
	}

	@Override
	public void setValue(Boolean value) {
		setValue(value, true);
	}

	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Boolean> handler) {
		return addHandler(handler, ValueChangeEvent.getType());
	}

	@Override
	public int getTabIndex() {
		return getElement().getTabIndex();
	}

	@Override
	public void setAccessKey(char key) {
		setAccessKey(getElement(), key);
	}

	private native void setAccessKey(Element elem, char key) /*-{
		elem.accessKey = String.fromCharCode(key);
	}-*/;

	@Override
	public void setFocus(boolean focused) {
		if (focused)
			getElement().focus();
		else
			getElement().blur();
	}

	@Override
	public void setTabIndex(int index) {
		getElement().setTabIndex(index);
	}

	@Override
	public Boolean getValue() {
		return iValue;
	}

	@Override
	public void setValue(Boolean value, boolean fireEvents) {
		if (value == null) value = false;
		iValue = value;
		setResource(iValue ? iCheckedFace : iUncheckedFace);
		Roles.getCheckboxRole().setAriaCheckedState(getElement(), iValue ? CheckedValue.TRUE : CheckedValue.FALSE);
		if (fireEvents)
			ValueChangeEvent.fire(this, getValue());
	}

	protected void onClick() {
	    getElement().dispatchEvent(Document.get().createClickEvent(1, 0, 0, 0, 0, false, false, false, false));
	}

	@Override
	public void onBrowserEvent(Event event) {
		if (event.getTypeInt() == Event.ONKEYUP && event.getKeyCode() == KeyCodes.KEY_SPACE) {
			onClick();
		}
		if (event.getTypeInt() == Event.ONCLICK) {
			setValue(!getValue());
		}
		super.onBrowserEvent(event);
	}
}
