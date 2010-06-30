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
package org.unitime.timetable.gwt.client;

import java.util.List;

import org.unitime.timetable.gwt.services.MenuService;
import org.unitime.timetable.gwt.services.MenuServiceAsync;
import org.unitime.timetable.gwt.shared.MenuInterface;
import org.unitime.timetable.gwt.shared.ToolBox;
import org.unitime.timetable.gwt.widgets.LoadingWidget;
import org.unitime.timetable.gwt.widgets.PageLabel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.MenuItemSeparator;
import com.google.gwt.user.client.ui.RootPanel;

public class UniTimeMenuBar extends Composite {
	private final MenuServiceAsync iService = GWT.create(MenuService.class);
	
	private MenuBar iMenu;

	public UniTimeMenuBar() {
		
		iMenu = new MenuBar();
		iMenu.setVisible(false);
		iMenu.addStyleName("unitime-NoPrint");
		
		initWidget(iMenu);
		
		iService.getMenu(new AsyncCallback<List<MenuInterface>>() {
			@Override
			public void onSuccess(List<MenuInterface> result) {
				initMenu(iMenu, result, 0);
				iMenu.setVisible(true);
			}
			@Override
			public void onFailure(Throwable caught) {
			}
		});
	}
	
	private void initMenu(MenuBar menu, List<MenuInterface> items, int level) {
		MenuItemSeparator lastSeparator = null;
		for (final MenuInterface item: items) {
			if (item.isSeparator()) {
				lastSeparator = new MenuItemSeparator();
				menu.addSeparator(lastSeparator);
			} else if (item.hasSubMenus()) {
				MenuBar m = new MenuBar(true);
				initMenu(m, item.getSubMenus(), level + 1);
				menu.addItem(new MenuItem(item.getName().replace(" ", "&nbsp;"), true, m));
			} else {
				menu.addItem(new MenuItem(item.getName().replace(" ", "&nbsp;"), true, new Command() {
					@Override
					public void execute() {
						if (item.getPage() != null) {
							if (item.isGWT()) 
								//openPageAsync(item.getPage());
								openUrl(item.getName(), "gwt.html?page=" + item.getPage(), item.getTarget());
							else {
								openUrl(item.getName(), item.getPage(), item.getTarget());
							}
						}
					}
				}));
			}
		}
		if (level == 0 && lastSeparator != null) {
			lastSeparator.setStyleName("unitime-BlankSeparator");
			lastSeparator.setWidth("100%");
		}
	}
	
	private void openUrl(String name, String url, String target) {
		/*
		if (url.indexOf('?') >= 0)
			url += "&gwt.codesvr=127.0.0.1:9997";
		else
			url += "?gwt.codesvr=127.0.0.1:9997";
		*/
		if (target == null)
			LoadingWidget.getInstance().show();
		if ("dialog".equals(target)) {
			final DialogBox dialog = new MyDialogBox();
			dialog.setAutoHideEnabled(true);
			dialog.setModal(true);
			final Frame frame = new Frame();
			frame.getElement().getStyle().setBorderWidth(0, Unit.PX);
			dialog.setGlassEnabled(true);
			dialog.setAnimationEnabled(true);
			dialog.setWidget(frame);
			dialog.setText(name);
			frame.setUrl(url);
			frame.setSize(String.valueOf(Window.getClientWidth() * 3 / 4), String.valueOf(Window.getClientHeight() * 3 / 4));
			dialog.center();
		} else {
			ToolBox.open(GWT.getHostPageBaseURL() + url);
		}
	}
	
	private void openPageAsync(final String page) {
		LoadingWidget.getInstance().show();
		if (RootPanel.get("UniTimeGWT:Content") == null || RootPanel.get("UniTimeGWT:TitlePanel") == null) {
			ToolBox.open(GWT.getHostPageBaseURL() + "gwt.html?page=" + page);
			return;
		}
		RootPanel.get("UniTimeGWT:Content").clear();
		RootPanel.get("UniTimeGWT:Content").getElement().setInnerHTML(null);
		RootPanel.get("UniTimeGWT:TitlePanel").clear();
		RootPanel.get("UniTimeGWT:TitlePanel").getElement().setInnerHTML(null);
		GWT.runAsync(new RunAsyncCallback() {
			public void onSuccess() {
				openPage(page);
				LoadingWidget.getInstance().hide();
			}
			public void onFailure(Throwable reason) {
				Label error = new Label("Failed to load the page (" + reason.getMessage() + ")");
				error.setStyleName("unitime-ErrorMessage");
				RootPanel.get("UniTimeGWT:Content").add(error);
				LoadingWidget.getInstance().hide();
			}
		});
	}
	
	private void openPage(String page) {
		try {
			for (Pages p: Pages.values()) {
				if (p.name().equals(page)) {
					LoadingWidget.getInstance().setMessage("Loading " + p.title() + " ...");
					RootPanel title = RootPanel.get("UniTimeGWT:Title");
					if (title != null) {
						title.clear();
						PageLabel label = new PageLabel(); label.setPageName(p.title());
						title.add(label);
					}
					RootPanel.get("UniTimeGWT:Content").add(p.widget());
					return;
				}
			}
			Label error = new Label("Failed to load the page (" + (page == null ? "page not provided" : "page " + page + " not registered" ) + ")");
			error.setStyleName("unitime-ErrorMessage");
			RootPanel.get("UniTimeGWT:Content").add(error);
		} catch (Exception e) {
			Label error = new Label("Failed to load the page (" + e.getMessage() + ")");
			error.setStyleName("unitime-ErrorMessage");
			RootPanel.get("UniTimeGWT:Content").add(error);
		}
	}
	
	public void insert(final RootPanel panel) {
		panel.add(this);
		panel.setVisible(true);
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
