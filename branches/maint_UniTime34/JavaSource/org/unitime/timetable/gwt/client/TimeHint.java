/*
 * UniTime 3.4 (University Timetabling Application)
 * Copyright (C) 2012, UniTime LLC, and individual contributors
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
package org.unitime.timetable.gwt.client;

import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponse;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class TimeHint {
	private static String sLastParameter = null;
	private static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	private static boolean sShowHint = false;
	
	public static Widget content(TimeHintResponse response) {
		SimplePanel panel = null;
		if (response.getContent().startsWith("$")) {
			HTML content = new HTML(ToolBox.eval(response.getContent()), false);
			panel = new SimplePanel(content);
		} else {
			panel = new SimplePanel();
			panel.getElement().setInnerHTML(response.getContent());
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
