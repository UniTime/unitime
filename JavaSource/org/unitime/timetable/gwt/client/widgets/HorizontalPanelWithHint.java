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

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomas Muller
 */
public class HorizontalPanelWithHint extends HorizontalPanel {
	private PopupPanel iHint = null;
	private Timer iShowHint, iHideHint = null;
	
	public HorizontalPanelWithHint(Widget hint) {
		super();
		iHint = new PopupPanel();
		iHint.setWidget(hint);
		iHint.setStyleName("unitime-PopupHint");
		sinkEvents(Event.ONMOUSEOVER);
		sinkEvents(Event.ONMOUSEOUT);
		sinkEvents(Event.ONMOUSEMOVE);
		iShowHint = new Timer() {
			@Override
			public void run() {
				iHint.show();
			}
		};
		iHideHint = new Timer() {
			@Override
			public void run() {
				iHint.hide();
			}
		};
	}
	
	public void hideHint() {
		if (iHint.isShowing())
			iHint.hide();
	}
	
	public void onBrowserEvent(Event event) {
		int x = 10 + event.getClientX() + getElement().getOwnerDocument().getScrollLeft();
		int y = 10 + event.getClientY() + getElement().getOwnerDocument().getScrollTop();
		
		switch (DOM.eventGetType(event)) {
		case Event.ONMOUSEMOVE:
			if (iHint.isShowing()) {
				iHint.setPopupPosition(x, y);
			} else {
				iShowHint.cancel();
				iHint.setPopupPosition(x, y);
				iShowHint.schedule(1000);
			}
			break;
		case Event.ONMOUSEOUT:
			iShowHint.cancel();
			if (iHint.isShowing())
				iHideHint.schedule(1000);
			break;
		}
	}		
}
