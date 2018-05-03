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

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.RoomInterface.GeocodeRequest;
import org.unitime.timetable.gwt.shared.RoomInterface.GeocodeResponse;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.ScriptInjector;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.LinkElement;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.TextBox;

/**
 * @author Tomas Muller
 */
public class LeafletMap extends MapWidget {
	private static final GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private String iTileUrl, iTileAttribution;
	private AbsolutePanel iMap;
	private TextBox iMapSearchBox;
	
	public LeafletMap(TextBox x, TextBox y, final String tileUrl, final String tileAttribution) {
		super(x, y);
		setStyleName("unitime-LeafletMap");
		iTileUrl = tileUrl; iTileAttribution = tileAttribution;
		
		iMap = new AbsolutePanel();
		iMap.getElement().setId("map");
		add(iMap);
		
		iMapControl = new AbsolutePanel(); iMapControl.setStyleName("control");
		iMapControl.getElement().getStyle().setPosition(Position.ABSOLUTE);
		iMapSearchBox = new TextBox();
		iMapSearchBox.setStyleName("unitime-TextBox"); iMapSearchBox.addStyleName("searchBox");
		iMapSearchBox.setTabIndex(-1);
		iMapControl.add(iMapSearchBox);
		add(iMapControl);
		Button button = new Button(MESSAGES.buttonGeocode(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				leafletGeocodeAddress();
			}
		});
		button.setTabIndex(-1);
		iMapSearchBox.addKeyPressHandler(new KeyPressHandler() {
			@Override
			public void onKeyPress(KeyPressEvent event) {
				switch (event.getNativeEvent().getKeyCode()) {
				case KeyCodes.KEY_ENTER:
            		event.preventDefault();
            		leafletGeocodeAddress();
            		return;
				}
			}
		});
		button.addStyleName("geocode");
		ToolBox.setWhiteSpace(button.getElement().getStyle(), "nowrap");
		Character accessKey = UniTimeHeaderPanel.guessAccessKey(MESSAGES.buttonGeocode());
		if (accessKey != null)
			button.setAccessKey(accessKey);
		iMapControl.add(button);
	}
	
	public void setup() {
		loadCss("https://unpkg.com/leaflet@1.3.1/dist/leaflet.css");
		ScriptInjector.fromUrl("https://unpkg.com/leaflet@1.3.1/dist/leaflet.js").setWindow(ScriptInjector.TOP_WINDOW).setCallback(
				new Callback<Void, Exception>() {
					@Override
					public void onSuccess(Void result) {
						setupLeafletMap(iTileUrl, iTileAttribution);
						setLeafletMarker();
						leafletReverseGeocode();
					}
					@Override
					public void onFailure(Exception e) {
						UniTimeNotifications.error(e.getMessage(), e);
						setVisible(false);
						iMapControl = null;
					}
				}).inject();
	}
	
	protected native void setupLeafletMap(String tileUrl, String tileAttribution) /*-{
		$wnd.map = $wnd.L.map('map').setView([50, -58], 2);
		$wnd.L.tileLayer(tileUrl, { attribution: tileAttribution }).addTo($wnd.map);
		$wnd.marker = $wnd.L.marker($wnd.L.latLng(50, -58), { draggable: true });
		$wnd.marker.setOpacity(0);
		$wnd.that = this;
		$wnd.mapIsEditable = true;
		$wnd.marker.on('dragend', function(e) {
			if ($wnd.mapIsEditable) {
				var pos = $wnd.marker.getLatLng();
				$doc.getElementById("coordX").value = '' + pos.lat;
				$doc.getElementById("coordY").value = '' + pos.lng;
				$wnd.that.@org.unitime.timetable.gwt.client.rooms.LeafletMap::leafletReverseGeocode()();
			}			
		});
		$wnd.map.on('click', function(e) {
			if ($wnd.mapIsEditable) {
				var pos = e.latlng;
				$doc.getElementById("coordX").value = '' + pos.lat;
				$doc.getElementById("coordY").value = '' + pos.lng;
				$wnd.marker.setLatLng(pos);
				$wnd.marker.setOpacity(1);
				if ($wnd.map.getZoom() <= 10) $wnd.map.setZoom(16);
				$wnd.map.panTo(pos);
				$wnd.that.@org.unitime.timetable.gwt.client.rooms.LeafletMap::leafletReverseGeocode()();
			}
		});
		$wnd.marker.addTo($wnd.map);
	}-*/;

	protected native void setLeafletMarker() /*-{
		try {
			var x = $doc.getElementById("coordX").value;
			var y = $doc.getElementById("coordY").value;
			if (x && y) {
				var pos = $wnd.L.latLng(x, y);
				$wnd.marker.setLatLng(pos);
				$wnd.marker.setOpacity(1);
				if ($wnd.map.getZoom() <= 10) $wnd.map.setZoom(16);
				$wnd.map.panTo(pos);
			} else {
				$wnd.marker.setOpacity(0);
			}
			if (this.@org.unitime.timetable.gwt.client.rooms.MapWidget::isEnabled()()) {
				$wnd.mapIsEditable = true;
				$wnd.marker.dragging.enable();
			} else {
				$wnd.mapIsEditable = false;
				$wnd.marker.dragging.disable();
			}
		} catch (error) {}
	}-*/;

	protected native String getLeafletViewBox() /*-{
		try {
			return $wnd.map.getBounds().toBBoxString();
		} catch (error) {
			return null;
		}
	}-*/;

	public void leafletGeocodeAddress() {
		RPC.execute(new GeocodeRequest(iMapSearchBox.getText(), getLeafletViewBox()), new AsyncCallback<GeocodeResponse>() {
			@Override
			public void onFailure(Throwable caught) {
				iX.setText("");
				iY.setText("");
				setLeafletMarker();
			}

			@Override
			public void onSuccess(GeocodeResponse result) {
				iX.setValue(String.valueOf(result.getLat()));
				iY.setValue(String.valueOf(result.getLon()));
				setLeafletMarker();
			}
		});
	}
	
	public void leafletReverseGeocode() {
		if (!iX.getValue().isEmpty() && !iY.getValue().isEmpty()) {
			try {
				RPC.execute(new GeocodeRequest(Double.valueOf(iX.getValue()), Double.valueOf(iY.getValue())), new AsyncCallback<GeocodeResponse>() {
					@Override
					public void onFailure(Throwable caught) {
						iMapSearchBox.setText("");
					}

					@Override
					public void onSuccess(GeocodeResponse result) {
						iMapSearchBox.setText(result.hasQuery() ? result.getQuery() : "");
					}
				});
			} catch (NumberFormatException e) {
				iMapSearchBox.setText("");
			}
		} else {
			iMapSearchBox.setText("");
		}
	}
	
	public static void loadCss(String url){
	    LinkElement link = Document.get().createLinkElement();
	    link.setRel("stylesheet");
	    link.setHref(url);
	    nativeAttachToHead(link);
	}

	protected static native void nativeAttachToHead(JavaScriptObject scriptElement) /*-{
		$doc.getElementsByTagName("head")[0].appendChild(scriptElement);
	}-*/;

	@Override
	public void setMarker() {
		setLeafletMarker();
		leafletReverseGeocode();	
	}	
}
