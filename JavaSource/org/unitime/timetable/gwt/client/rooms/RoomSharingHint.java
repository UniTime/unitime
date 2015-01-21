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
public class RoomSharingHint {
	private static RoomSharingWidget sSharing;
	private static long sLastLocationId = -1;
	private static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	
	public static RoomSharingWidget content(RoomInterface.RoomSharingModel model) {
		if (sSharing == null)
			sSharing = new RoomSharingWidget(false);
		sSharing.setModel(model);
		return sSharing;
	}
	
	/** Never use from GWT code */
	public static void _showRoomSharingHint(JavaScriptObject source, String locationId) {
		showHint((Element) source.cast(), Long.valueOf(locationId), false);
	}
	
	/** Never use from GWT code */
	public static void _showEventAvailabilityHint(JavaScriptObject source, String locationId) {
		showHint((Element) source.cast(), Long.valueOf(locationId), true);
	}

	public static void showHint(final Element relativeObject, final long locationId, boolean eventAvailability) {
		sLastLocationId = locationId;
		RPC.execute(RoomInterface.RoomSharingRequest.load(locationId, eventAvailability), new AsyncCallback<RoomInterface.RoomSharingModel>() {
			@Override
			public void onFailure(Throwable caught) {
			}
			
			@Override
			public void onSuccess(RoomInterface.RoomSharingModel result) {
				if (locationId == sLastLocationId && result != null)
					GwtHint.showHint(relativeObject, content(result));
			}
		});
	}
	
	public static void hideHint() {
		GwtHint.hideHint();
	}
	
	public static native void createTriggers()/*-{
	$wnd.showGwtRoomAvailabilityHint = function(source, content) {
		@org.unitime.timetable.gwt.client.rooms.RoomSharingHint::_showRoomSharingHint(Lcom/google/gwt/core/client/JavaScriptObject;Ljava/lang/String;)(source, content);
	};
	$wnd.showGwtRoomEventAvailabilityHint = function(source, content) {
		@org.unitime.timetable.gwt.client.rooms.RoomSharingHint::_showEventAvailabilityHint(Lcom/google/gwt/core/client/JavaScriptObject;Ljava/lang/String;)(source, content);
	};
	$wnd.hideGwtRoomAvailabilityHint = function() {
		@org.unitime.timetable.gwt.client.rooms.RoomSharingHint::hideHint()();
	};
	$wnd.hideGwtRoomEventAvailabilityHint = function() {
		@org.unitime.timetable.gwt.client.rooms.RoomSharingHint::hideHint()();
	};
}-*/;


}
