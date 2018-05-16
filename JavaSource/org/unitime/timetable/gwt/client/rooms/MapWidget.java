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

import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.shared.RoomInterface.MapPropertiesInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.MapPropertiesRequest;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;

/**
 * @author Tomas Muller
 */
public abstract class MapWidget extends AbsolutePanel implements HasEnabled {
	protected static final GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	protected TextBox iX, iY;
	private boolean iInitialized = false;
	protected AbsolutePanel iMapControl;
	
	protected MapWidget(TextBox x, TextBox y) {
		iX = x; iX.getElement().setId("coordX");
		iY = y; iY.getElement().setId("coordY");
		iX.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				setMarker();
			}
		});
		iY.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				setMarker();
			}
		});
	}
	
	protected static MapWidget createMap(TextBox x, TextBox y, MapPropertiesInterface result) {
		if (result.isGoogleMap())
			return new GoogleMap(x, y, result.getGoogleMapApiKey());
		if (result.isLeafletMap())
			return new LeafletMap(x, y, result.getLeafletMapTiles(), result.getLeafletMapAttribution());
		return null;
	}
	
	@Override
	public boolean isEnabled() {
		return iMapControl != null && iMapControl.isVisible();
	}

	@Override
	public void setEnabled(boolean enabled) {
		if (iMapControl != null)
			iMapControl.setVisible(enabled);
	}
	
	public void onShow() {
		if (!isVisible()) return;
		if (!iInitialized) {
			iInitialized = true;
			setup();
		} else if (iMapControl != null) {
			setMarker();
		}
	}
	
	protected abstract void setup();
	public abstract void setMarker();
	
	public static void insert(final RootPanel panel) {
		RPC.execute(new MapPropertiesRequest(), new AsyncCallback<MapPropertiesInterface>() {
			@Override
			public void onFailure(Throwable caught) {
			}

			@Override
			public void onSuccess(MapPropertiesInterface result) {
				MapWidget map = MapWidget.createMap(TextBox.wrap(Document.get().getElementById("coordX")), TextBox.wrap(Document.get().getElementById("coordY")), result);
				if (map != null) {
					panel.add(map);
					panel.setVisible(true);
					map.onShow();
				}
			}
		});
	}
}
