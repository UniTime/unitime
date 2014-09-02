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
package org.unitime.timetable.gwt.mobile.client.widgets;

import java.util.Date;

import org.unitime.timetable.gwt.client.GwtHint;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.UniTimeFrameDialogDisplay;

import com.google.gwt.dom.client.FrameElement;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * @author Tomas Muller
 */
public class MobileUniTimeFrameDialogImpl implements UniTimeFrameDialogDisplay {
	private PopupPanel iPopup;
	private Frame iFrame;
	private Timer iCheckLoadingWidgetIsShowing;
	private String iText;
	private int iScrollTop = 0, iScrollLeft = 0;

	public MobileUniTimeFrameDialogImpl() {
        iPopup = new PopupPanel(true, true);
        iPopup.addStyleName("unitime-MobileFrameDialog");
        iPopup.setGlassEnabled(true);
        
		iFrame = new Frame();
		iFrame.setStyleName("frame");
		iPopup.add(iFrame);
		hookFrameLoaded((FrameElement)iFrame.getElement().cast());
		
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
		
		iFrame.addLoadHandler(new LoadHandler() {
			@Override
			public void onLoad(LoadEvent event) {
				LoadingWidget.getInstance().hide();
			}
		});
		
		History.addValueChangeHandler(new ValueChangeHandler<String>() {
			public void onValueChange(ValueChangeEvent<String> event) {
				if (!event.getValue().equals(getText()))
					hideDialog();
			}
		});
		
		iPopup.addCloseHandler(new CloseHandler<PopupPanel>() {
			@Override
			public void onClose(CloseEvent<PopupPanel> event) {
				Window.scrollTo(iScrollLeft, iScrollTop);
				RootPanel.getBodyElement().getStyle().setOverflow(Overflow.AUTO);
			}
		});
	}

	@Override
	public void openDialog(String title, String source, String width, String height) {
		if (isShowing()) hideDialog();
		GwtHint.hideHint();
		
		iScrollLeft = Window.getScrollLeft(); iScrollTop = Window.getScrollTop();
		Window.scrollTo(0, 0);

		LoadingWidget.getInstance().show("Loading " + title + " ...");
		setText(title);
		String hash = null;
		int hashIdx = source.lastIndexOf('#');
		if (hashIdx >= 0) {
			hash = source.substring(hashIdx);
			source = source.substring(0, hashIdx);
		}
		iFrame.setUrl(source + (source.indexOf('?') >= 0 ? "&" : "?") + "noCacheTS=" + new Date().getTime() + (hash == null ? "" : hash));
		iCheckLoadingWidgetIsShowing.schedule(30000);
		
		History.newItem(title, false);
		iPopup.setPopupPosition(0, 0);
		iPopup.show();
		RootPanel.getBodyElement().getStyle().setOverflow(Overflow.HIDDEN);
	}

	@Override
	public void hideDialog() {
		if (iPopup.isShowing()) iPopup.hide();
	}

	@Override
	public boolean isShowing() {
		return iPopup.isShowing();
	}

	@Override
	public String getText() {
		return iText;
	}

	@Override
	public void setText(String text) {
		iText = text;
	}
	
	public static void notifyFrameLoaded() {
		LoadingWidget.getInstance().hide();
	}

	public static native void hookFrameLoaded(FrameElement element) /*-{
		element.onload = function() {
			@org.unitime.timetable.gwt.mobile.client.widgets.MobileUniTimeFrameDialogImpl::notifyFrameLoaded()();
		}
		if (element.addEventListener) {
			element.addEventListener("load", function() {
				@org.unitime.timetable.gwt.mobile.client.widgets.MobileUniTimeFrameDialogImpl::notifyFrameLoaded()();
			}, false);
		} else if (element.attachEvent) {
			element.attachEvent("onload", function() {
				@org.unitime.timetable.gwt.mobile.client.widgets.MobileUniTimeFrameDialogImpl::notifyFrameLoaded()();
			});
		}
	}-*/;
}
