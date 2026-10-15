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

import java.util.HashMap;
import java.util.Map;

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

public class SolverWarnings extends P {
	protected static final GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	protected static final GwtMessages MSG = GWT.create(GwtMessages.class);
	private Type iType;
	private String iUrl;
	private static Map<Type, SolverWarnings> sInstances = null;
	
	private SolverWarnings(Type type) {
		iType = type;
		addMouseOverHandler(new MouseOverHandler() {
			@Override
			public void onMouseOver(MouseOverEvent e) {
				if (iUrl != null && !iUrl.isEmpty())
					getElement().getStyle().setBackgroundColor("#BBCDD0");
			}
		});
		addMouseOutHandler(new MouseOutHandler() {
			@Override
			public void onMouseOut(MouseOutEvent e) {
				if (iUrl != null && !iUrl.isEmpty())
					getElement().getStyle().clearBackgroundColor();
			}
		});
		addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent c) {
				if (iUrl != null && !iUrl.isEmpty())
					ToolBox.open(GWT.getHostPageBaseURL() + iUrl);
			}
		});
		refreshWarnings();
	}
	
	public static SolverWarnings createInstance(Type type) {
		if (sInstances == null)
			 sInstances = new HashMap<Type, SolverWarnings>();
		SolverWarnings instance = new SolverWarnings(type);
		sInstances.put(type, instance);
		return instance;
	}
	
	public static void refresh(Type type) {
		SolverWarnings instance = (sInstances == null ? null : sInstances.get(type));
		if (instance != null)
			instance.refreshWarnings();
	}
	
	public static void refresh() {
		if (sInstances != null)
			for (SolverWarnings instance: sInstances.values())
				instance.refreshWarnings();
	}
	
	protected void setUrl(String url) {
		iUrl = url;
		if (iUrl != null && !iUrl.isEmpty())
			getElement().getStyle().setCursor(Cursor.POINTER);
		else
			getElement().getStyle().clearCursor();
	}
	
	protected void setMessage(String message) {
		setHTML(message == null ? "" : message);
		setVisible(message != null && !message.isEmpty());
	}
	
	private void refreshWarnings() {
		RPC.execute(new SolverWarningsRequest(iType), new AsyncCallback<SolverWarningsResponse>() {
			@Override
			public void onSuccess(final SolverWarningsResponse response) {
				if (response == null || !response.hasMessage()) {
					setMessage(null); setUrl(null);
					return;
				}
				setStyleName(response.isWarning() ? "unitime-PageWarn" : "unitime-PageMessage");
				setUrl(response.getUrl());
				setMessage(response.getMessage());
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
		exam,
		locks
	}
}
