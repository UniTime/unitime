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
import org.unitime.timetable.gwt.services.CurriculaService;
import org.unitime.timetable.gwt.services.CurriculaServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
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

public class PageLabel extends Composite {
	public static final GwtResources RESOURCES =  GWT.create(GwtResources.class);

	private final CurriculaServiceAsync iService = GWT.create(CurriculaService.class);

	private HorizontalPanel iPanel;
	
	private Label iName;
	private Image iHelp;
	
	private boolean iEnabled = false;
	private String iWikiUrl = null;

	public PageLabel() {
		iPanel = new HorizontalPanel();
		
        iName = new Label();
        iName.setStyleName("unitime-Title");
		iHelp = new Image(RESOURCES.help());
		iHelp.setVisible(false);
		
		iPanel.add(iName);
		iPanel.add(iHelp);
		iPanel.setCellVerticalAlignment(iHelp, HasVerticalAlignment.ALIGN_TOP);
				
		initWidget(iPanel);
		
		iService.getAppliationProperty(new String[] {"tmtbl.wiki.help", "tmtbl.wiki.url"}, new AsyncCallback<String[]>() {
			
			@Override
			public void onSuccess(String[] result) {
				iEnabled = "true".equals(result[0]);
				iWikiUrl = result[1];
				iHelp.setVisible(iEnabled && iWikiUrl != null && !iName.getText().isEmpty());
			}
			
			@Override
			public void onFailure(Throwable caught) {
				iEnabled = false;
			}
		});
		
		final DialogBox dialog = new MyDialogBox();
		dialog.setAutoHideEnabled(true);
		dialog.setModal(true);
		final Frame frame = new Frame();
		frame.getElement().getStyle().setBorderWidth(0, Unit.PX);
		dialog.setGlassEnabled(true);
		dialog.setAnimationEnabled(true);
		dialog.setWidget(frame);
		
		iHelp.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				dialog.setText(iName.getText() + " Help");
				frame.setUrl(iWikiUrl + iName.getText().trim().replace(' ', '_'));
				frame.setSize(String.valueOf(Window.getClientWidth() * 3 / 4), String.valueOf(Window.getClientHeight() * 3 / 4));
				dialog.center();
			}
		});
		iHelp.addMouseOverHandler(new MouseOverHandler() {
			@Override
			public void onMouseOver(MouseOverEvent event) {
				iHelp.getElement().getStyle().setCursor(Cursor.POINTER);	
			}
		});
		
	}
	
	public void setPageName(String pageName) {
		iName.setText(pageName);
		iHelp.setTitle(pageName + " Help");
		iHelp.setVisible(iEnabled && iWikiUrl != null);
	}
	
	private class MyDialogBox extends DialogBox {
		private MyDialogBox() { super(); }
		protected void onPreviewNativeEvent(NativePreviewEvent event) {
			super.onPreviewNativeEvent(event);
			if (DOM.eventGetKeyCode((Event) event.getNativeEvent()) == KeyCodes.KEY_ESCAPE)
				MyDialogBox.this.hide();
		}
	}
}
