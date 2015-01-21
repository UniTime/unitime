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
package org.unitime.timetable.gwt.mobile.client.page;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications.NotificationType;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.HasStyleName;

import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomas Muller
 */
public class MobileNotifications implements UniTimeNotifications.Display {
	protected List<Notification> iNotifications = new ArrayList<Notification>();
	protected P iPanel = new P("unitime-MobileNotifications");
	
	public MobileNotifications() {
		RootPanel.get().add(iPanel);
		iPanel.addStyleName("unitime-MobileNotifications");
		iPanel.getElement().getStyle().setPosition(Position.FIXED);
	}
	
	@Override
	public void addNotification(String html, NotificationType type) {
		switch (type) {
		case ERROR:
			addNotification(new Notification(html, "error"));
			break;
		case WARN:
			addNotification(new Notification(html, "warn"));
			break;
		case INFO:
			addNotification(new Notification(html, "info"));
			break;
		}
	}
	
	protected void populate(String style) {
		iPanel.clear();
		if (!iNotifications.isEmpty()) {
			P panel = new P("container", style);
			for (Notification n: iNotifications) {
				panel.add(n.asWidget());
			}
			iPanel.add(panel);
		}
	}
	
	protected void addNotification(final Notification notification) {
		for (Iterator<Notification> i = iNotifications.iterator(); i.hasNext(); ) {
			Notification n = i.next();
			if (n.getText().equals(notification.getText())) {
				i.remove();
			}
		}
		iNotifications.add(notification);
		populate("slideup");
		
		Timer timer = new Timer() {
			@Override
			public void run() {
				iNotifications.remove(notification);
				populate(null);
			}
		};
		timer.schedule(10000);
	}

	public class Notification implements IsWidget, HasText, HasStyleName {
		String iText, iStyle;
		Notification(String text, String style) {
			iText = text; iStyle = style;
		}
		
		@Override
		public String getStyleName() { return iStyle; }

		@Override
		public String getText() { return iText; }

		@Override
		public void setText(String text) { iText = text; }
		
		@Override
		public Widget asWidget() {
			final P p = new P("notification", getStyleName());
			p.setHTML(getText());
			p.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					iNotifications.remove(Notification.this);
					populate(null);
				}
			});
			return p;
		}

	}
}