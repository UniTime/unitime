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
package org.unitime.timetable.gwt.client.widgets;

import java.util.Date;

import org.unitime.timetable.gwt.client.GwtHint;
import org.unitime.timetable.gwt.client.aria.AriaStatus;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.resources.GwtAriaMessages;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.BodyElement;
import com.google.gwt.dom.client.FrameElement;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * @author Tomas Muller
 */
public class UniTimeFrameDialogImpl extends UniTimeDialogBox implements UniTimeFrameDialogDisplay {
	private static GwtAriaMessages ARIA = GWT.create(GwtAriaMessages.class);
	private Frame iFrame = null;
	private Timer iCheckLoadingWidgetIsShowing = null;
	
	public UniTimeFrameDialogImpl() {
		super(true, true);
		
		setEscapeToHide(true);
		iFrame = new Frame();
		iFrame.getElement().getStyle().setBorderWidth(0, Unit.PX);
		hookFrameLoaded((FrameElement)iFrame.getElement().cast());
		setWidget(iFrame);
		
		iCheckLoadingWidgetIsShowing = new Timer() {
			@Override
			public void run() {
				if (LoadingWidget.getInstance().isShowing()) {
					LoadingWidget.getInstance().hide();
					UniTimeNotifications.error(getText() + " does not seem to load, " +
							"please check <a href='" + iFrame.getUrl() + "' style='white-space: nowrap;'>" + iFrame.getUrl() + "</a> for yourself.");
				}
			}
		};
		
		addCloseHandler(new CloseHandler<PopupPanel>() {
			@Override
			public void onClose(CloseEvent<PopupPanel> event) {
				if (LoadingWidget.getInstance().isShowing())
					LoadingWidget.getInstance().hide();
				RootPanel.getBodyElement().getStyle().setOverflow(Overflow.AUTO);
			}
		});
		
		iFrame.addLoadHandler(new LoadHandler() {
			@Override
			public void onLoad(LoadEvent event) {
				LoadingWidget.getInstance().hide();
				try {
					FrameElement frame = (FrameElement)iFrame.getElement().cast();
					BodyElement body = frame.getContentDocument().getBody();
					if (body.getScrollWidth() > body.getClientWidth()) {
						iFrame.setWidth(Math.min(frame.getClientWidth() + body.getScrollWidth() - body.getClientWidth(), Window.getClientWidth() * 95 / 100) + "px");
						setPopupPosition(
								Math.max(Window.getScrollLeft() + (Window.getClientWidth() - getOffsetWidth()) / 2, 0),
								Math.max(Window.getScrollTop() + (Window.getClientHeight() - getOffsetHeight()) / 2, 0));
					}
				} catch (Exception e) {}
			}
		});
	}
	
	public void setFrameSize(String width, String height) {
		try {
			iFrame.getElement().getStyle().setWidth(Double.parseDouble(width), Unit.PX);
		} catch (NumberFormatException e) {
			iFrame.setWidth(width);
		}
		try {
			iFrame.getElement().getStyle().setHeight(Double.parseDouble(height), Unit.PX);
		} catch (NumberFormatException e) {
			iFrame.setHeight(height);
		}
	}
	
	public void center() {
		super.center();
		iCheckLoadingWidgetIsShowing.schedule(30000);
		RootPanel.getBodyElement().getStyle().setOverflow(Overflow.HIDDEN);
    	AriaStatus.getInstance().setText(ARIA.dialogOpened(getText()));
	}
	
	@Override
	public void openDialog(String title, String source, String width, String height) {
		if (isShowing()) hide();
		GwtHint.hideHint();
		
		LoadingWidget.getInstance().show("Loading " + title + " ...");
		setText(title);
		String hash = null;
		int hashIdx = source.lastIndexOf('#');
		if (hashIdx >= 0) {
			hash = source.substring(hashIdx);
			source = source.substring(0, hashIdx);
		}
		iFrame.setUrl(source + (source.indexOf('?') >= 0 ? "&" : "?") + "noCacheTS=" + new Date().getTime() + (hash == null ? "" : hash));
		String w = (width == null || width.isEmpty() ? String.valueOf(Window.getClientWidth() * 3 / 4) : width);
		String h = (height == null || height.isEmpty() ? String.valueOf(Window.getClientHeight() * 3 / 4) : height);
		if (w.endsWith("%")) w = String.valueOf(Integer.parseInt(w.substring(0, w.length() - 1)) * Window.getClientWidth() / 100);
		if (h.endsWith("%")) h = String.valueOf(Integer.parseInt(h.substring(0, h.length() - 1)) * Window.getClientHeight() / 100);
		setFrameSize(w, h);
		center();
	}
	
	@Override
	public void hideDialog() {
		hide();
	}

	public static void notifyFrameLoaded() {
		LoadingWidget.getInstance().hide();
	}

	public static native void hookFrameLoaded(FrameElement element) /*-{
		element.onload = function() {
			@org.unitime.timetable.gwt.client.widgets.UniTimeFrameDialogImpl::notifyFrameLoaded()();
		}
		if (element.addEventListener) {
			element.addEventListener("load", function() {
				@org.unitime.timetable.gwt.client.widgets.UniTimeFrameDialogImpl::notifyFrameLoaded()();
			}, false);
		} else if (element.attachEvent) {
			element.attachEvent("onload", function() {
				@org.unitime.timetable.gwt.client.widgets.UniTimeFrameDialogImpl::notifyFrameLoaded()();
			});
		}
	}-*/;
}
