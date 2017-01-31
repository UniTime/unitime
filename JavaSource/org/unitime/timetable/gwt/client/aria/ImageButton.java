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
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.HasAllFocusHandlers;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.Image;

/**
 * @author Tomas Muller
 */
public class ImageButton extends Image implements HasEnabled, Focusable, HasAriaLabel, HasAllFocusHandlers {
	private ImageResource iUp = null, iDown = null, iOver = null, iDisabled = null;
	private boolean iEnabled = true, iFocusing = false;
	
	public ImageButton(ImageResource faceUp, ImageResource faceDown, ImageResource faceOver, ImageResource faceDisabled) {
		iUp = faceUp; iDown = faceDown; iOver = faceOver; iDisabled = faceDisabled;
		setResource(iUp);
		setTabIndex(0);
		setStyleName("unitime-ImageButton");
		Roles.getButtonRole().set(getElement());
		sinkEvents(Event.KEYEVENTS);
		addMouseOverHandler(new MouseOverHandler() {
			@Override
			public void onMouseOver(MouseOverEvent event) {
				if (isEnabled() && iOver != null)
					setResource(iOver);
			}
		});
		addMouseOutHandler(new MouseOutHandler() {
			@Override
			public void onMouseOut(MouseOutEvent event) {
				if (isEnabled())
					setResource(iUp);
			}
		});
		addMouseDownHandler(new MouseDownHandler() {
			@Override
			public void onMouseDown(MouseDownEvent event) {
				if (isEnabled() && iDown != null)
					setResource(iDown);
			}
		});
		addMouseUpHandler(new MouseUpHandler() {
			@Override
			public void onMouseUp(MouseUpEvent event) {
				if (isEnabled())
					setResource(iUp);
			}
		});
		addStyleName("unitime-ImageButton-focus");
		addFocusHandler(new FocusHandler() {
			@Override
			public void onFocus(FocusEvent event) {
				if (isEnabled() && iOver != null)
					setResource(iOver);
			}
		});
		addBlurHandler(new BlurHandler() {
			@Override
			public void onBlur(BlurEvent event) {
				if (isEnabled())
					setResource(iUp);
			}
		});
	}
	
	public ImageButton(ImageResource faceUp, ImageResource faceDown, ImageResource faceOver) {
		this(faceUp, faceDown, faceOver, null);
	}
	
	public ImageButton(ImageResource faceUp, ImageResource faceOver) {
		this(faceUp, null, faceOver, null);
	}

	@Override
	public boolean isEnabled() {
		return iEnabled;
	}

	@Override
	public void setEnabled(boolean enabled) {
		if (iEnabled != enabled) {
			iEnabled = enabled;
			setResource(enabled ? iUp : iDisabled != null ? iDisabled : iUp);
			if (enabled) {
				removeStyleName("unitime-ImageButton-disabled");
			} else {
				addStyleName("unitime-ImageButton-disabled");
			}
			Roles.getButtonRole().setAriaDisabledState(getElement(), !enabled);
		}
	}
	
	public void setFace(ImageResource face) {
		setResource(face);
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
	
	protected void onClick() {
	    getElement().dispatchEvent(Document.get().createClickEvent(1, 0, 0, 0, 0, false, false, false, false));
	}
	
	@Override
	public void onBrowserEvent(Event event) {
		if (!isEnabled()) return;

		super.onBrowserEvent(event);

	    if ((event.getTypeInt() & Event.KEYEVENTS) != 0) {
	    	int type = DOM.eventGetType(event);
	    	char keyCode = (char) event.getKeyCode();
	    	switch (type) {
	    	case Event.ONKEYDOWN:
	    		if (keyCode == ' ' || keyCode == '\n' || keyCode == '\r') {
	    			if (iDown != null) setResource(iDown);
	    		}
	    		break;
	        case Event.ONKEYUP:
	        	if (keyCode == ' ' || keyCode == '\n' || keyCode == '\r') {
	        		setResource(iUp);
	        		onClick();
	        	}
	        	break;
	    	}
	    }
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
	public HandlerRegistration addFocusHandler(FocusHandler handler) {
		return addDomHandler(handler, FocusEvent.getType());
	}

	@Override
	public HandlerRegistration addBlurHandler(BlurHandler handler) {
		return addDomHandler(handler, BlurEvent.getType());
	}
}