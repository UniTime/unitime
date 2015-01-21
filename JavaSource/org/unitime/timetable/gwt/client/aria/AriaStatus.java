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