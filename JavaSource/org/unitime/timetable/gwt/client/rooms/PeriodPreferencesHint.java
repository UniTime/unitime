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
package org.unitime.timetable.gwt.client.rooms;

import org.unitime.timetable.gwt.client.GwtHint;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.shared.RoomInterface;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Tomas Muller
 */
public class PeriodPreferencesHint {
	private static PeriodPreferencesWidget sSharing;
	private static Long sLastLocationId = null;
	private static boolean sShowHint = false;
	private static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	
	public static PeriodPreferencesWidget content(RoomInterface.PeriodPreferenceModel model) {
		if (sSharing == null)
			sSharing = new PeriodPreferencesWidget(false);
		sSharing.setModel(model);
		return sSharing;
	}
	
	/** Never use from GWT code */
	public static void _showPeriodPreferencesHint(JavaScriptObject source, String locationId, String examTypeId) {
		showHint((Element) source.cast(), toLong(locationId), toLong(examTypeId));
	}
	
	public static void _showExamPeriodPreferencesHint(JavaScriptObject source, String examId, String periodId) {
		showHintForExam((Element) source.cast(), toLong(examId), toLong(periodId));
	}

	public static void showHint(final Element relativeObject, final Long locationId, final Long examTypeId) {
		sLastLocationId = locationId;
		sShowHint = true;
		RPC.execute(RoomInterface.PeriodPreferenceRequest.load(locationId, examTypeId), new AsyncCallback<RoomInterface.PeriodPreferenceModel>() {
			@Override
			public void onFailure(Throwable caught) {
			}
			
			@Override
			public void onSuccess(RoomInterface.PeriodPreferenceModel result) {
				if (locationId.equals(sLastLocationId) && sShowHint && result != null && !result.getPeriods().isEmpty())
					GwtHint.showHint(relativeObject, content(result));
			}
		});
	}
	
	public static void showHintForExam(final Element relativeObject, final Long examId, final Long periodId) {
		sLastLocationId = examId;
		sShowHint = true;
		RPC.execute(RoomInterface.PeriodPreferenceRequest.loadForExam(examId, null), new AsyncCallback<RoomInterface.PeriodPreferenceModel>() {
			@Override
			public void onFailure(Throwable caught) {
			}
			
			@Override
			public void onSuccess(RoomInterface.PeriodPreferenceModel result) {
				if (examId.equals(sLastLocationId) && sShowHint && result != null) {
					if (periodId != null) result.setAssignedPeriodId(periodId);
					GwtHint.showHint(relativeObject, content(result));
				}
			}
		});
	}
	
	public static void hideHint() {
		sShowHint = false;
		GwtHint.hideHint();
	}
	
	public static native void createTriggers()/*-{
	$wnd.showGwtPeriodPreferencesHint = function(source, locationId, examTypeId) {
		@org.unitime.timetable.gwt.client.rooms.PeriodPreferencesHint::_showPeriodPreferencesHint(Lcom/google/gwt/core/client/JavaScriptObject;Ljava/lang/String;Ljava/lang/String;)(source, locationId, examTypeId);
	};
	$wnd.showGwtExamPeriodPreferencesHint = function(source, examId, periodId) {
		@org.unitime.timetable.gwt.client.rooms.PeriodPreferencesHint::_showExamPeriodPreferencesHint(Lcom/google/gwt/core/client/JavaScriptObject;Ljava/lang/String;Ljava/lang/String;)(source, examId, periodId);
	};
	$wnd.hideGwtPeriodPreferencesHint = function() {
		@org.unitime.timetable.gwt.client.rooms.PeriodPreferencesHint::hideHint()();
	};
	}-*/;

	private static Long toLong(String value) {
		try {
			return Long.valueOf(value);
		} catch (Exception e) {
			return null;
		}
	}

}
