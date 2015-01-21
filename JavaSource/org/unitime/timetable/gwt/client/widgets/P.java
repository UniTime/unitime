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
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasAllMouseHandlers;
import com.google.gwt.event.dom.client.HasClickHandlers;
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
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.HasHTML;

/**
 * @author Tomas Muller
 */
public class P extends AbsolutePanel implements HasAllMouseHandlers, HasHTML, HasClickHandlers {

	public P(Element element, String... styles) {
		super(element);
		addStyleNames(styles);
		sinkAllMouseEvents();
	}
	
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
		sinkEvents(Event.ONCLICK);
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

	@Override
	public HandlerRegistration addClickHandler(ClickHandler handler) {
		return addHandler(handler, ClickEvent.getType());
	}
}