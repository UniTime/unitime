/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.gwt.widgets;

import org.unitime.timetable.gwt.resources.GwtResources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.RootPanel;

public class LoadingWidget extends Composite {
	public static final GwtResources RESOURCES =  GWT.create(GwtResources.class);
	
	private static LoadingWidget sInstance = null;

	private AbsolutePanel iPanel = null;
	private Image iImage = null;
	private int iCount = 0;
	private Timer iTimer = null;
	private HTML iWarning;
	private HTML iMessage = null;
	
	public LoadingWidget() {
		iPanel = new AbsolutePanel();
		iPanel.setStyleName("unitime-LoadingPanel");
		iImage = new Image(RESOURCES.loading());
		iImage.setStyleName("unitime-LoadingIcon");
		initWidget(iPanel);
		Window.addWindowScrollHandler(new Window.ScrollHandler() {
			@Override
			public void onWindowScroll(Window.ScrollEvent event) {
				if (iCount > 0) {
					DOM.setStyleAttribute(iPanel.getElement(), "left", String.valueOf(event.getScrollLeft()));
					DOM.setStyleAttribute(iPanel.getElement(), "top", String.valueOf(event.getScrollTop()));
					DOM.setStyleAttribute(iImage.getElement(), "left", String.valueOf(event.getScrollLeft() + Window.getClientWidth() / 2));
					DOM.setStyleAttribute(iImage.getElement(), "top", String.valueOf(event.getScrollTop() + Window.getClientHeight() / 2));
					DOM.setStyleAttribute(iWarning.getElement(), "left", String.valueOf(event.getScrollLeft() + Window.getClientWidth() / 2 - 200));
					DOM.setStyleAttribute(iWarning.getElement(), "top", String.valueOf(event.getScrollTop() + 5 * Window.getClientHeight() / 12));
					DOM.setStyleAttribute(iMessage.getElement(), "left", String.valueOf(event.getScrollLeft() + Window.getClientWidth() / 2 - 200));
					DOM.setStyleAttribute(iMessage.getElement(), "top", String.valueOf(event.getScrollTop() + Window.getClientHeight() / 3));
				}
			}
		});
		iWarning = new HTML("Oooops, the looding is taking too much time... Something probably went wrong. You may need to reload this page.", true);
		iWarning.setWidth("400px");
		iWarning.setStyleName("unitime-PopupWarning");
		iMessage = new HTML("", true);
		iMessage.setWidth("400px");
		iMessage.setStyleName("unitime-PopupMessage");
		iTimer = new Timer() {
			@Override
			public void run() {
				RootPanel.get().add(iWarning, Window.getScrollLeft() + Window.getClientWidth() / 2 - 200, Window.getScrollTop() + 5 * Window.getClientHeight() / 12);
			}
		};
	}
	
	public void show() {
		show(null);
	}
	
	public void show(String message) {
		show(message, 30000);
	}

	public void show(String message, int warningDelayInMillis) {
		if (iCount == 0) {
			RootPanel.get().add(this, Window.getScrollLeft(), Window.getScrollTop());
			RootPanel.get().add(iImage, Window.getScrollLeft() + Window.getClientWidth() / 2, Window.getScrollTop() + Window.getClientHeight() / 2);
			iTimer.schedule(warningDelayInMillis);
		}
		if (message != null) {
			boolean showing = (iCount > 0 && !iMessage.getText().isEmpty());
			iMessage.setHTML(message);
			iMessage.setStyleName("unitime-PopupMessage");
			if (!showing && !iMessage.getText().isEmpty()) {
				RootPanel.get().add(iMessage, Window.getScrollLeft() + Window.getClientWidth() / 2 - 200, Window.getScrollTop() + Window.getClientHeight() / 3);
			} else if (showing && iMessage.getText().isEmpty()) {
				RootPanel.get().remove(iMessage);
			}
		}
		iCount ++;
	}
	
	public void setMessage(String message) {
		iMessage.setHTML(message);
		iMessage.setStyleName("unitime-PopupMessage");
	}
	
	public void fail(String message) {
		fail(message, 5000);
	}
	
	public void fail(String message, int hideDelayInMillis) {
		iTimer.cancel();
		iMessage.setHTML(message);
		iMessage.setStyleName("unitime-PopupWarning");
		Timer t = new Timer() {
			@Override
			public void run() {
				hide();
			}
		};
		t.schedule(hideDelayInMillis);
	}
	
	public void hide() {
		if (iCount > 0) iCount --;
		if (iCount == 0) {
			RootPanel.get().remove(iImage);
			RootPanel.get().remove(this);
			iTimer.cancel();
			RootPanel.get().remove(iWarning);
			RootPanel.get().remove(iMessage);
			iMessage.setHTML("");
		}
	}
	
	public boolean isShowing() {
		return iCount > 0;
	}

	public static LoadingWidget getInstance() {
		if (sInstance == null) sInstance = new LoadingWidget();
		return sInstance;
	}
}
