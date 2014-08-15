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


import com.google.gwt.aria.client.LiveValue;
import com.google.gwt.aria.client.Roles;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HasHTML;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomas Muller
 */
public class AriaStatus extends Widget implements HasHTML, HasText {
	private static AriaStatus sStatus = null;
	
	public AriaStatus(boolean assertive) {
		this(DOM.createSpan(), assertive);
	}
	
	protected AriaStatus(Element element, boolean assertive) {
		setElement(element);
		setStyleName("unitime-AriaStatus");
		Roles.getStatusRole().set(getElement());
		Roles.getStatusRole().setAriaLiveProperty(getElement(), assertive ? LiveValue.ASSERTIVE : LiveValue.POLITE);
		Roles.getStatusRole().setAriaAtomicProperty(getElement(), true);
	}
	
	public AriaStatus() {
		this(false);
	}
	
	public static AriaStatus getInstance() {
		if (sStatus == null) {
			RootPanel statusPanel = RootPanel.get("UniTimeGWT:AriaStatus");
			if (statusPanel != null && "1".equals(Window.Location.getParameter("aria"))) {
				sStatus = new AriaStatus(statusPanel.getElement(), false);
				sStatus.setStyleName("unitime-VisibleAriaStatus");
			} else {
				sStatus  = new AriaStatus(false);
				RootPanel.get().add(sStatus);
			}
			
			RootPanel.get().addDomHandler(new KeyUpHandler() {
				@Override
				public void onKeyUp(KeyUpEvent event) {
    				if (event.getNativeEvent().getKeyCode() == 191 && (event.isControlKeyDown() || event.isAltKeyDown())) {
    					sStatus.setHTML(sStatus.getHTML());
    				}
    			}
    		}, KeyUpEvent.getType());
		}
		return sStatus;
	}
	
	@Override
	public String getText() {
		return getElement().getInnerText();
	}

	@Override
	public void setText(String text) {
		getElement().setInnerText(text);
	}

	@Override
	public String getHTML() {
		return getElement().getInnerHTML();
	}

	@Override
	public void setHTML(String html) {
		getElement().setInnerHTML(html);
	}
}