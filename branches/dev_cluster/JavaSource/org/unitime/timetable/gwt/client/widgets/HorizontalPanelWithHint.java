/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
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
