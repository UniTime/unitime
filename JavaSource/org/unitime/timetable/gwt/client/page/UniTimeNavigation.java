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
package org.unitime.timetable.gwt.client.page;

import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponse;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.RootPanel;

public class UniTimeNavigation {
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	protected static CourseMessages MSG = GWT.create(CourseMessages.class);
	private static UniTimeNavigation sInstance;
	
	private RootPanel iRootPanel = null;
	private P iNavigation = null;
	
	public static UniTimeNavigation getInstance() {
		if (sInstance == null)
			sInstance = new UniTimeNavigation();
		return sInstance;
	}

	public void insert(final RootPanel panel) {
		iRootPanel = panel;
	}
	
	public void refresh() {
		if (iRootPanel == null) return;
		RPC.execute(new NavigationRequest(), new AsyncCallback<GwtRpcResponseList<NavigationItem>>() {
			@Override
			public void onFailure(Throwable e) {
			}

			@Override
			public void onSuccess(GwtRpcResponseList<NavigationItem> items) {
				if (iNavigation == null) {
					iNavigation = new P("unitime-Navigation");
					iRootPanel.clear();
					iRootPanel.getElement().setInnerText(null);
					iRootPanel.add(iNavigation);
				} else {
					iNavigation.clear();
				}
				if (items.size() > 1) {
					final NavigationItem backItem = items.get(items.size() - 2);
					Button back = new Button(MSG.navigationBackButton(), new ClickHandler() {
						@Override
						public void onClick(ClickEvent evt) {
							ToolBox.open(GWT.getHostPageBaseURL() + backItem.getUrl());
						}
					});
					back.addStyleName("btn");
					back.setTitle(MSG.navigationBackTitle(back.getText()));
					iNavigation.add(back);
					boolean first = true;
					for (final NavigationItem item: items) {
						Anchor a = new Anchor(item.getTitle(), item.getUrl());
						a.addStyleName("item");
						if (first) {
							a.getElement().getStyle().setPaddingLeft(10, Unit.PX);
							first = false;
						} else {
							P next = new P(DOM.createSpan()); next.setHTML(" &rarr; ");
							iNavigation.add(next);
						}
						iNavigation.add(a);
					}
				}
			}
		});
	}
	
	public static class NavigationRequest implements GwtRpcRequest<GwtRpcResponseList<NavigationItem>> {
	}
	
	public static class NavigationItem implements GwtRpcResponse {
		String iUrl; String iTitle;
		public NavigationItem() {}
		public NavigationItem(String url, String title) {
			iUrl = url; iTitle = title;
		}
		public String getUrl() { return iUrl; }
		public void setUrl(String url) { iUrl = url; }
		public String getTitle() { return iTitle; }
		public void setTitle(String title) { iTitle = title; }
		
		@Override
		public String toString() { return getTitle() + " (" + getUrl() + ")"; }
		@Override
		public int hashCode() { return getUrl().hashCode(); }
		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof NavigationItem)) return false;
			return getUrl().equals(((NavigationItem)o).getUrl());
		}
	}
}
