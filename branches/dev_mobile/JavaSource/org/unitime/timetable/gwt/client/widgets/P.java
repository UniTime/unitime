/*
 * UniTime 3.4 - 3.5 (University Timetabling Application)
 * Copyright (C) 2012 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.gwt.client.widgets;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.HasAllMouseHandlers;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.HasHTML;

/**
 * @author Tomas Muller
 */
public class P extends AbsolutePanel implements HasAllMouseHandlers, HasHTML {

	public P(String... styles) {
		addStyleNames(styles);
		sinkAllMouseEvents();
	}
	
	private void sinkAllMouseEvents() {
		sinkEvents(Event.ONMOUSEDOWN);
		sinkEvents(Event.ONMOUSEUP);
		sinkEvents(Event.ONMOUSEMOVE);
		sinkEvents(Event.ONMOUSEOVER);
		sinkEvents(Event.ONMOUSEOUT);
		sinkEvents(Event.ONMOUSEWHEEL);
	}
	
	public void addStyleNames(String... styles) {
		for (String style: styles)
			if (style != null && !style.isEmpty())
				addStyleName(style);
	}
	
	public void removeStyleNames(String... styles) {
		for (String style: styles)
			if (style != null && !style.isEmpty())
				removeStyleName(style);
	}
	
	@Override
	public void onBrowserEvent(Event event) {
		switch (DOM.eventGetType(event)) {
	    case Event.ONMOUSEDOWN:
	    	MouseDownEvent.fireNativeEvent(event, this);
	    	break;
	    case Event.ONMOUSEUP:
	    	MouseUpEvent.fireNativeEvent(event, this);
	    	break;
	    case Event.ONMOUSEMOVE:
	    	MouseMoveEvent.fireNativeEvent(event, this);
	    	break;
	    case Event.ONMOUSEOVER:
	    	MouseOverEvent.fireNativeEvent(event, this);
	    	break;
	    case Event.ONMOUSEOUT:
	    	MouseOutEvent.fireNativeEvent(event, this);
	    	break;
	    case Event.ONMOUSEWHEEL:
	    	MouseWheelEvent.fireNativeEvent(event, this);
	    	break;
		}
	}
	
	@Override
	public HandlerRegistration addMouseDownHandler(MouseDownHandler handler) {
		return addHandler(handler, MouseDownEvent.getType());
	}

	@Override
	public HandlerRegistration addMouseUpHandler(MouseUpHandler handler) {
		return addHandler(handler, MouseUpEvent.getType());
	}

	@Override
	public HandlerRegistration addMouseOutHandler(MouseOutHandler handler) {
		return addHandler(handler, MouseOutEvent.getType());
	}

	@Override
	public HandlerRegistration addMouseOverHandler(MouseOverHandler handler) {
		return addHandler(handler, MouseOverEvent.getType());
	}

	@Override
	public HandlerRegistration addMouseMoveHandler(MouseMoveHandler handler) {
		return addHandler(handler, MouseMoveEvent.getType());
	}

	@Override
	public HandlerRegistration addMouseWheelHandler( MouseWheelHandler handler) {
		return addHandler(handler, MouseWheelEvent.getType());
	}

	@Override
	public void setText(String text) {
		getElement().setInnerText(text == null ? "" : text);
	}

	@Override
	public String getText() {
		return getElement().getInnerText();
	}

	@Override
	public String getHTML() {
		return getElement().getInnerHTML();
	}

	@Override
	public void setHTML(String html) {
		getElement().setInnerHTML(html == null ? "" : html);
	}
	
	public void setWidth(double width) {
		getElement().getStyle().setWidth(width, Unit.PX);
	}
	
	public void setHeight(double height) {
		getElement().getStyle().setHeight(height, Unit.PX);
	}
	
	public void setSize(double width, double height) {
		getElement().getStyle().setWidth(width, Unit.PX);
		getElement().getStyle().setHeight(height, Unit.PX);
	}
	
	@Override
	@Deprecated
	public void setSize(String width, String height) {
		super.setSize(width, height);
	}
	
	@Override
	@Deprecated
	public void setWidth(String width) {
		super.setWidth(width);
	}

	@Override
	@Deprecated
	public void setHeight(String width) {
		super.setHeight(width);
	}
}