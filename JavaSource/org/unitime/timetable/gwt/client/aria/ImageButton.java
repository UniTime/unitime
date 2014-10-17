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
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.Image;

/**
 * @author Tomas Muller
 */
public class ImageButton extends Image implements HasEnabled, Focusable, HasAriaLabel {
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
}