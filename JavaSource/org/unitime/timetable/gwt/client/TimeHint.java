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

import org.unitime.timetable.gwt.client.offerings.TimePreferenceWidget;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.TimePatternModel;
import org.unitime.timetable.gwt.client.rooms.PeriodPreferencesWidget;
import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponse;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.shared.RoomInterface.PeriodPreferenceModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomas Muller
 */
public class TimeHint {
	private static String sLastParameter = null;
	private static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	private static boolean sShowHint = false;
	
	public static Widget content(TimeHintResponse response) {
		if (response.getTimePrefs() != null) {
			TimePreferenceWidget w = new TimePreferenceWidget(false,
					response.getTimePrefs().getPrefLevels(),
					response.getTimePrefs().isHorizontal());
			w.setShowLegend(true);
			w.setModel(response.getTimePrefs());
			return w;
		}
		if (response.getPeriodPrefs() != null) {
			PeriodPreferencesWidget w = new PeriodPreferencesWidget(false);
			w.setModel(response.getPeriodPrefs());
			w.setShowLegend(true);
			return w;
		}
		return null;
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
				if (result != null && sLastParameter.equals(parameter) && sShowHint) {
					Widget w = content(result);
					if (w != null)
						GwtHint.showHint(relativeObject, w);
				}
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
		private PeriodPreferenceModel iPeriodPrefs;
		private TimePatternModel iTimePrefs;
		
		
		public TimeHintResponse() {}
		public TimeHintResponse(TimePatternModel model) {
			iTimePrefs = model;
		}
		public TimeHintResponse(PeriodPreferenceModel model) {
			iPeriodPrefs = model;
		}
		
		public TimePatternModel getTimePrefs() { return iTimePrefs; }
		public void setTimePrefs(TimePatternModel model) { iTimePrefs = model; }

		public PeriodPreferenceModel getPeriodPrefs() { return iPeriodPrefs; }
		public void setPeriodPrefs(PeriodPreferenceModel model) { iPeriodPrefs = model; }
	}
}
