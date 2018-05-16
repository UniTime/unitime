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
import org.unitime.timetable.gwt.resources.GwtMessages;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.ScriptInjector;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.TextBox;

/**
 * @author Tomas Muller
 */
public class GoogleMap extends MapWidget {
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private String iApiKey = null;
	
	public GoogleMap(TextBox x, TextBox y, String apiKey) {
		super(x, y);
		setStyleName("unitime-GoogleMap");
		iApiKey = apiKey;
	
		iMapControl = new AbsolutePanel(); iMapControl.setStyleName("control");
		final TextBox searchBox = new TextBox();
		searchBox.setStyleName("unitime-TextBox"); searchBox.addStyleName("searchBox");
		searchBox.getElement().setId("mapSearchBox");
		searchBox.setTabIndex(-1);
		iMapControl.add(searchBox);
		Button button = new Button(MESSAGES.buttonGeocode(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				geocodeAddress();
			}
		});
		button.setTabIndex(-1);
		searchBox.addKeyPressHandler(new KeyPressHandler() {
			@Override
			public void onKeyPress(KeyPressEvent event) {
				switch (event.getNativeEvent().getKeyCode()) {
				case KeyCodes.KEY_ENTER:
            		event.preventDefault();
            		geocodeAddress();
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
		
		add(iMapControl);

		addGoogleMap(getElement(), iMapControl.getElement());
	}

	protected native void addGoogleMap(Element canvas, Element control) /*-{
		$wnd.geoceodeMarker = function geoceodeMarker() {
			var searchBox = $doc.getElementById('mapSearchBox');
			if (!searchBox) return;
			$wnd.geocoder.geocode({'location': $wnd.marker.getPosition()}, function(results, status) {
				if (status == $wnd.google.maps.GeocoderStatus.OK) {
					if (results[0]) {
						$wnd.marker.setTitle(results[0].formatted_address);
						searchBox.value = results[0].formatted_address;
					} else {
						$wnd.marker.setTitle(null);
						searchBox.value = "";
					}
				} else {
					$wnd.marker.setTitle(null);
					searchBox.value = "";
				}
			});
		}
		$wnd.that = this
	
		$wnd.setupGoogleMap = function setupGoogleMap() {
			var latlng = new $wnd.google.maps.LatLng(50, -58);
			var myOptions = {
				zoom: 2,
				center: latlng,
				mapTypeId: $wnd.google.maps.MapTypeId.ROADMAP
			};
	
			$wnd.geocoder = new $wnd.google.maps.Geocoder();
			$wnd.map = new $wnd.google.maps.Map(canvas, myOptions);
			$wnd.marker = new $wnd.google.maps.Marker({
				position: latlng,
				map: $wnd.map,
				draggable: true,
				visible: false
			});
	
			$wnd.map.controls[$wnd.google.maps.ControlPosition.BOTTOM_LEFT].push(control);		
	
			var t = null;
		
			$wnd.google.maps.event.addListener($wnd.marker, 'position_changed', function() {
				$doc.getElementById("coordX").value = '' + $wnd.marker.getPosition().lat().toFixed(6);
				$doc.getElementById("coordY").value = '' + $wnd.marker.getPosition().lng().toFixed(6);
				if (t != null) clearTimeout(t);
				t = setTimeout($wnd.geoceodeMarker, 500);
			});
		
			$wnd.google.maps.event.addListener($wnd.map, 'rightclick', function(event) {
				if ($wnd.marker.getDraggable()) {
					$wnd.marker.setPosition(event.latLng);
					$wnd.marker.setVisible(true);
				}
			});
		
			$wnd.that.@org.unitime.timetable.gwt.client.rooms.GoogleMap::setMarker()();
		};
	}-*/;

	@Override
	public native void setMarker() /*-{
		try {
			var x = $doc.getElementById("coordX").value;
			var y = $doc.getElementById("coordY").value;
			if (x && y) {
				var pos = new $wnd.google.maps.LatLng(x, y);
				$wnd.marker.setPosition(pos);
				$wnd.marker.setVisible(true);
				if ($wnd.marker.getMap().getZoom() <= 10) $wnd.marker.getMap().setZoom(16);
				$wnd.marker.getMap().panTo(pos);
			} else {
				$wnd.marker.setVisible(false);
			}
			$wnd.marker.setDraggable(this.@org.unitime.timetable.gwt.client.rooms.MapWidget::isEnabled()());
		} catch (error) {}
	}-*/;

	protected native void geocodeAddress() /*-{
		var address = $doc.getElementById("mapSearchBox").value;
		$wnd.geocoder.geocode({ 'address': address }, function(results, status) {
			if (status == $wnd.google.	maps.GeocoderStatus.OK) {
				if (results[0]) {
					$wnd.marker.setPosition(results[0].geometry.location);
					$wnd.marker.setTitle(results[0].formatted_address);
					$wnd.marker.setVisible(true);
					if ($wnd.map.getZoom() <= 10) $wnd.map.setZoom(16);
						$wnd.map.panTo(results[0].geometry.location);
				} else {
					$wnd.marker.setVisible(false);
				}
			} else {
				$wnd.marker.setVisible(false);
			}
		});
	}-*/;

	@Override
	public void setup() {
		ScriptInjector.fromUrl("https://maps.googleapis.com/maps/api/js?" + (iApiKey != null && !iApiKey.isEmpty() ? "key=" + iApiKey + "&" : "") +
				"sensor=false&callback=setupGoogleMap").setWindow(ScriptInjector.TOP_WINDOW).setCallback(
				new Callback<Void, Exception>() {
					@Override
					public void onSuccess(Void result) {
					}
					@Override
					public void onFailure(Exception e) {
						UniTimeNotifications.error(e.getMessage(), e);
						setVisible(false);
						iMapControl = null;
					}
				}).inject();
	}
}
