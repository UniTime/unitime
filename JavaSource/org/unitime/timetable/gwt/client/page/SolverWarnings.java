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

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponse;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtMessages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.RootPanel;

public class SolverWarnings extends P {
	protected static final GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	protected static final GwtMessages MSG = GWT.create(GwtMessages.class);
	
	public SolverWarnings(final RootPanel panel, Type type) {
		RPC.execute(new SolverWarningsRequest(type), new AsyncCallback<SolverWarningsResponse>() {
			@Override
			public void onSuccess(final SolverWarningsResponse response) {
				if (response == null || !response.hasMessage()) return;
				final P p = new P(response.isWarning() ? "unitime-PageWarn" : "unitime-PageMessage");
				if (response.hasUrl()) {
					p.addMouseOverHandler(new MouseOverHandler() {
						@Override
						public void onMouseOver(MouseOverEvent e) {
							p.getElement().getStyle().setBackgroundColor("#BBCDD0");
						}
					});
					p.addMouseOutHandler(new MouseOutHandler() {
						@Override
						public void onMouseOut(MouseOutEvent e) {
							p.getElement().getStyle().clearBackgroundColor();
						}
					});
					p.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent c) {
							ToolBox.open(GWT.getHostPageBaseURL() + response.getUrl());
						}
					});
					p.getElement().getStyle().setCursor(Cursor.POINTER);
				}
				p.setHTML(response.getMessage());
				panel.add(p);
			}
			
			@Override
			public void onFailure(Throwable e) {
				UniTimeNotifications.error(MSG.failedToLoadPage(e.getMessage()), e);
			}
		});
	}
	

	public static class SolverWarningsRequest implements GwtRpcRequest<SolverWarningsResponse> {
		private Type iType;
		
		public SolverWarningsRequest() {}
		public SolverWarningsRequest(Type type) { iType = type; }
		
		public Type getType() { return iType; }
		public void setType(Type type) { iType = type; }
	}
	
	public static class SolverWarningsResponse implements GwtRpcResponse {
		private String iMessage;
		private boolean iWarning = false;
		private String iUrl;
		
		public SolverWarningsResponse() {}
		public SolverWarningsResponse(String url, String message) {
			iUrl = url;
			iMessage = message;
		}
		public SolverWarningsResponse(String message) {
			iMessage = message;
		}
		
		public void setMessage(String message) { iMessage = message; }
		public String getMessage() { return iMessage; }
		public boolean hasMessage() { return iMessage != null && !iMessage.isEmpty(); }

		public void setUrl(String url) { iUrl = url; }
		public String getUrl() { return iUrl; }
		public boolean hasUrl() { return iUrl != null && !iUrl.isEmpty(); }

		public boolean isWarning() { return iWarning; }
		public SolverWarningsResponse setWarning(boolean warning) { iWarning = warning; return this; }
	}
	
	public static enum Type implements IsSerializable {
		solver,
		assignments,
		exam
	}
}
