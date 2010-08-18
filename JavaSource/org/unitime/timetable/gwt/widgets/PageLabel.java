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
import org.unitime.timetable.gwt.services.MenuService;
import org.unitime.timetable.gwt.services.MenuServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.FrameElement;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;

public class PageLabel extends Composite {
	public static final GwtResources RESOURCES =  GWT.create(GwtResources.class);

	private final MenuServiceAsync iService = GWT.create(MenuService.class);

	private HorizontalPanel iPanel;
	
	private Label iName;
	private Image iHelp;
	
	private String iUrl = null;
	private Timer iTimer = null;
	
	public PageLabel() {
		iPanel = new HorizontalPanel();
		
        iName = new Label();
        iName.setStyleName("unitime-Title");
		iHelp = new Image(RESOURCES.help());
		iHelp.setVisible(false);
		iHelp.getElement().getStyle().setCursor(Cursor.POINTER);
		
		iPanel.add(iName);
		iPanel.add(iHelp);
		iPanel.setCellVerticalAlignment(iHelp, HasVerticalAlignment.ALIGN_TOP);
				
		initWidget(iPanel);
		
		final DialogBox dialog = new MyDialogBox();
		dialog.setAutoHideEnabled(true);
		dialog.setModal(true);
		final Frame frame = new MyFrame();
		frame.getElement().getStyle().setBorderWidth(0, Unit.PX);
		dialog.setGlassEnabled(true);
		dialog.setAnimationEnabled(true);
		dialog.setWidget(frame);
		
		iTimer = new Timer() {
			@Override
			public void run() {
				if (LoadingWidget.getInstance().isShowing())
					LoadingWidget.getInstance().fail(iName.getText() + " Help does not seem to load, " +
							"please check <a href='" + iUrl + "' style='white-space: nowrap;'>" + iUrl + "</a> for yourself.");
			}
		};

		dialog.addCloseHandler(new CloseHandler<PopupPanel>() {
			@Override
			public void onClose(CloseEvent<PopupPanel> event) {
				if (LoadingWidget.getInstance().isShowing())
					LoadingWidget.getInstance().hide();
			}
		});
		
		iHelp.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (iUrl == null) return;
				dialog.setText(iName.getText() + " Help");
				frame.setUrl(iUrl);
				frame.setSize(String.valueOf(Window.getClientWidth() * 3 / 4), String.valueOf(Window.getClientHeight() * 3 / 4));
				dialog.center();
				iTimer.schedule(30000);
			}
		});
		
	}
	
	public void insert(final RootPanel panel) {
		setPageName(panel.getElement().getInnerText());
		panel.getElement().setInnerText(null);
		panel.add(this);
		panel.setVisible(true);
	}
	
	public void setPageName(String title) {
		iName.setText(title);
		iHelp.setTitle(title + " Help");
		iHelp.setVisible(false);
		iService.getHelpPage(title, new AsyncCallback<String>() {
			@Override
			public void onFailure(Throwable caught) {
				iHelp.setVisible(false);
				iUrl = null;
			}
			@Override
			public void onSuccess(String result) {
				iHelp.setVisible(true);
				iUrl = result;
			}
		});		
	}

	private class MyDialogBox extends DialogBox {
		private MyDialogBox() { super(); }
		protected void onPreviewNativeEvent(NativePreviewEvent event) {
			super.onPreviewNativeEvent(event);
			if (DOM.eventGetKeyCode((Event) event.getNativeEvent()) == KeyCodes.KEY_ESCAPE)
				MyDialogBox.this.hide();
		}
	}
	
	public static void notifyFrameLoaded() {
		LoadingWidget.getInstance().hide();
	}
	
	public class MyFrame extends Frame {
		public MyFrame() {
			super();
			hookFremaLoaded((FrameElement)getElement().cast());
		}
		
		public void onLoad() {
			super.onLoad();
			LoadingWidget.getInstance().show("Loading " + iName.getText() + " Help ...");
		}
	}
	
	public native void hookFremaLoaded(FrameElement element) /*-{
		element.onload = function() {
			@org.unitime.timetable.gwt.widgets.PageLabel::notifyFrameLoaded()();
		}
	}-*/;
}
