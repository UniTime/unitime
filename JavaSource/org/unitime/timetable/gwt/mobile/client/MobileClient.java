/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2014, UniTime LLC, and individual contributors
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
package org.unitime.timetable.gwt.mobile.client;

import java.util.logging.Logger;

import org.unitime.timetable.gwt.client.Client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.MetaElement;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.RootPanel;
import com.googlecode.mgwt.dom.client.event.orientation.OrientationChangeEvent;
import com.googlecode.mgwt.dom.client.event.orientation.OrientationChangeHandler;
import com.googlecode.mgwt.ui.client.MGWT;
import com.googlecode.mgwt.ui.client.MGWTSettings;

/**
 * @author Tomas Muller
 */
public class MobileClient extends Client {
	public static Logger sLogger = Logger.getLogger(MobileClient.class.getName());
	
	@Override
	public void onModuleLoadDeferred() {
	    MGWTSettings settings = new MGWTSettings();
	    settings.setViewPort(new Viewport());
	    settings.setFullscreen(true);
	    settings.setPreventScrolling(false);
	    settings.setIconUrl("images/unitime-phone.png");
	    settings.setFixIOS71BodyBug(true);
	    
		MGWT.applySettings(settings);
		
		MGWT.addOrientationChangeHandler(
				new OrientationChangeHandler() {
					@Override
					public void onOrientationChanged(OrientationChangeEvent event) {
						NodeList<Element> tags = Document.get().getElementsByTagName("meta");
						for (int i = 0; i < tags.getLength(); i++) {
							MetaElement meta = (MetaElement)tags.getItem(i);
							if (meta.getName().equals("viewport")) {
								meta.setContent(new Viewport().getContent());
							}
						}
					}
				}
			);
				
		
		super.onModuleLoadDeferred();
		
		// load components
		for (final MobileComponents c: MobileComponents.values()) {
			final RootPanel p = RootPanel.get(c.id());
			if (p != null) {
				Scheduler.get().scheduleDeferred(new ScheduledCommand() {
					@Override
					public void execute() {
						initComponentAsync(p, c);
					}
				});
			}
			if (p == null && c.isMultiple()) {
				NodeList<Element> x = getElementsByName(c.id());
				if (x != null && x.getLength() > 0)
					for (int i = 0; i < x.getLength(); i++) {
						Element e = x.getItem(i);
						e.setId(DOM.createUniqueId());
						final RootPanel q = RootPanel.get(e.getId());
						Scheduler.get().scheduleDeferred(new ScheduledCommand() {
							@Override
							public void execute() {
								initComponentAsync(q, c);
							}
						});
					}
			}
		}
	}
	
	public void initComponentAsync(final RootPanel panel, final MobileComponents comp) {
		GWT.runAsync(new RunAsyncCallback() {
			public void onSuccess() {
				comp.insert(panel);
			}
			public void onFailure(Throwable reason) {
			}
		});
	}
	
	public native static int getScreenWidth() /*-{
		if ($wnd.orientation == 90 || $wnd.orientation == -90)
			return ($wnd.screen.width > $wnd.screen.height ? $wnd.screen.width : $wnd.screen.height);
		else
			return ($wnd.screen.width < $wnd.screen.height ? $wnd.screen.width : $wnd.screen.height);
	}-*/;
	
	public static class Viewport extends MGWTSettings.ViewPort {
		@Override
	    public String getContent() {
			int sw = getScreenWidth();
			if (sw < 800) {
				return "initial-scale=" + (sw / 800.0) + ",minimum-scale=" + (sw / 3200.0) + ",maximum-scale=" + (sw / 200.0) + ",width=800,user-scalable=yes";
			} else {
				return "initial-scale=1.0,minimum-scale=0.25,maximum-scale=1.0,width=device-width,user-scalable=yes";
			}
		}
	}
}
