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
package org.unitime.timetable.gwt.client;

import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponse;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomas Muller
 */
public class TimeHint {
	private static String sLastParameter = null;
	private static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	private static boolean sShowHint = false;
	
	public static Widget content(TimeHintResponse response) {
		SimplePanel panel = null;
		try {
		if (response.getContent().startsWith("$")) {
			HTML content = new HTML(ToolBox.eval(response.getContent()), false);
			panel = new SimplePanel(content);
		} else {
			panel = new SimplePanel();
			panel.getElement().setInnerHTML(response.getContent());
		}
		} catch (Exception e) {
			UniTimeNotifications.error("Failed to display: " + response.getContent(), e);
		}
		panel.setStyleName("unitime-TimeHint");
		return panel;
	}
	
	/** Never use from GWT code */
	public static void _showTimeHint(JavaScriptObject source, String parameter) {
		showHint((Element) source.cast(), parameter);
	}
	
	public static void showHint(final Element relativeObject, final String parameter) {
		sLastParameter = parameter;
		sShowHint = true;
		RPC.execute(new TimeHintRequest(parameter), new AsyncCallback<TimeHintResponse>() {
			@Override
			public void onFailure(Throwable caught) {
			}
			
			@Override
			public void onSuccess(TimeHintResponse result) {
				if (result != null && sLastParameter.equals(parameter) && sShowHint)
					GwtHint.showHint(relativeObject, content(result));
			}
		});
	}
	
	public static void hideHint() {
		sShowHint = false;
		GwtHint.hideHint();
	}
	
	public static native void createTriggers()/*-{
	$wnd.showGwtTimeHint = function(source, content) {
		@org.unitime.timetable.gwt.client.TimeHint::_showTimeHint(Lcom/google/gwt/core/client/JavaScriptObject;Ljava/lang/String;)(source, content);
	};
	$wnd.hideGwtTimeHint = function() {
		@org.unitime.timetable.gwt.client.TimeHint::hideHint()();
	};
	}-*/;

	
	public static class TimeHintRequest implements GwtRpcRequest<TimeHintResponse> {
		private String iParameter;
		
		public TimeHintRequest() {}
		public TimeHintRequest(String parameter) {
			iParameter = parameter;
		}
		
		public String getParameter() { return iParameter; }
		public void setParameter(String parameter) { iParameter = parameter; }
		
		@Override
		public String toString() { return getParameter(); }
	}
	
	public static class TimeHintResponse implements GwtRpcResponse {
		private String iContent = null;
		
		public TimeHintResponse() {}
		public TimeHintResponse(String content) {
			iContent = content;
		}
		
		public String getContent() { return iContent; }
		public void setContent(String content) { iContent = content; }
		
		@Override
		public String toString() { return getContent(); }
	}
}
