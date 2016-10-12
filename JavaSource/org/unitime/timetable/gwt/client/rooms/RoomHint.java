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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.unitime.timetable.gwt.client.GwtHint;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.RoomInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.FeatureInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.GroupInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomPictureInterface;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomas Muller
 */
public class RoomHint {
	private static long sLastLocationId = -1;
	private static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private static final GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	private static boolean sShowHint = false;
	private static Timer sLastSwapper = null;
	
	public static Widget content(RoomInterface.RoomHintResponse room, String prefix, String distance) {
		if (sLastSwapper != null) {
			sLastSwapper.cancel(); sLastSwapper = null;
		}
		SimpleForm form = new SimpleForm();
		form.removeStyleName("unitime-NotPrintableBottomLine");
		if (prefix != null && prefix.contains("{0}")) {
			String label = prefix.replace("{0}", room.getLabel());
			if (prefix.contains("{1}"))
				label = label.replace("{1}", room.hasDisplayName() ? room.getDisplayName() : room.hasRoomTypeLabel() ? room.getRoomTypeLabel() : "");
			form.addRow(new Label(label, false));
		} else {
			form.addRow(new Label((prefix == null || prefix.isEmpty() ? "" : prefix + " ") + (room.hasDisplayName() || room.hasRoomTypeLabel() ? MESSAGES.label(room.getLabel(), room.hasDisplayName() ? room.getDisplayName() : room.getRoomTypeLabel()) : room.getLabel()), false));
		}
		List<String> urls = new ArrayList<String>();
		if (room.hasMiniMapUrl()) {
			urls.add(room.getMiniMapUrl());
		}
		if (room.hasPictures()) {
			for (RoomPictureInterface picture: room.getPictures())
				urls.add(GWT.getHostPageBaseURL() + "picture?id=" + picture.getUniqueId());
		}
		if (!urls.isEmpty()) {
			Image image = new Image(urls.get(0));
			image.setStyleName("minimap");
			form.addRow(image);
			if (urls.size() > 1) {
				sLastSwapper = new ImageSwapper(image, urls);
				sLastSwapper.scheduleRepeating(3000);
			}
		}
		if (room.hasCapacity()) {
			if (room.hasExamCapacity()) {
				if (room.hasExamType()) {
					form.addRow(MESSAGES.propRoomCapacity(), new Label(MESSAGES.capacityWithExamType(room.getCapacity().toString(), room.getExamCapacity().toString(), room.getExamType()), false));
				} else {
					form.addRow(MESSAGES.propRoomCapacity(), new Label(MESSAGES.capacityWithExam(room.getCapacity().toString(), room.getExamCapacity().toString()), false));
				}
			} else {
				form.addRow(MESSAGES.propRoomCapacity(), new Label(MESSAGES.capacity(room.getCapacity().toString()), false));
			}
		}
		
		if (room.hasArea())
			form.addRow(MESSAGES.propRoomArea(), new HTML(room.getArea(), false));
		
		if (room.hasFeatures(null)) {
			P features = new P("features");
			for (Iterator<FeatureInterface> i = room.getFeatures(null).iterator(); i.hasNext(); ) {
				FeatureInterface feature = i.next();
				P f = new P();
				if (feature.hasDescription()) {
					f.setText(MESSAGES.hintRoomFeatureWithDescription(feature.getLabel(), feature.getDescription()));
					f.addStyleName("feature-des");
				} else {
					f.setText(feature.getLabel() + (i.hasNext() ? CONSTANTS.itemSeparator() : ""));
					f.addStyleName("feature");
				}
				features.add(f);
			}
			form.addRow(MESSAGES.propFeatures(), features);
		}
		for (String type: room.getFeatureTypes()) {
			P features = new P("features");
			for (Iterator<FeatureInterface> i = room.getFeatures(type).iterator(); i.hasNext(); ) {
				FeatureInterface feature = i.next();
				P f = new P();
				if (feature.hasDescription()) {
					f.setText(MESSAGES.hintRoomFeatureWithDescription(feature.getLabel(), feature.getDescription()));
					f.addStyleName("feature-des");
				} else {
					f.setText(feature.getLabel() + (i.hasNext() ? CONSTANTS.itemSeparator() : ""));
					f.addStyleName("feature");
				}
				features.add(f);
			}
			form.addRow(type, features);
		}

		if (room.hasGroups()) {
			P groups = new P("groups");
			for (Iterator<GroupInterface> i = room.getGroups().iterator(); i.hasNext(); ) {
				GroupInterface group = i.next();
				P g = new P();
				if (group.hasDescription()) {
					g.setText(MESSAGES.hintRoomFeatureWithDescription(group.getLabel(), group.getDescription()));
					g.addStyleName("group-des");
				} else {
					g.setText(group.getLabel() + (i.hasNext() ? CONSTANTS.itemSeparator() : ""));
					g.addStyleName("group");
				}
				groups.add(g);
			}
			form.addRow(MESSAGES.propRoomGroups(), groups);
		}
		
		if (room.hasEventStatus())
			form.addRow(MESSAGES.propRoomEventStatus(), new Label(room.getEventStatus()));
		
		if (room.hasEventDepartment())
			form.addRow(MESSAGES.propRoomEventDepartment(), new Label(room.getEventDepartment()));

		if (room.hasBreakTime())
			form.addRow(MESSAGES.propRoomBreakTime(), new Label(MESSAGES.breakTime(room.getBreakTime().toString())));
				
		if (room.hasNote()) {
			HTML note = new HTML(room.getNote());
			note.addStyleName("note");
			form.addRow(note);
		}
		
		if (room.isIgnoreRoomCheck())
			form.addRow(new HTML(MESSAGES.ignoreRoomCheck()));
		
		if (distance != null && !distance.isEmpty() && !"0".equals(distance))
			form.addRow(MESSAGES.propRoomDistance(), new Label(MESSAGES.roomDistance(distance), false));
		
		SimplePanel panel = new SimplePanel(form);
		panel.setStyleName("unitime-RoomHint");
		return panel;
	}
	
	/** Never use from GWT code */
	public static void _showRoomHint(JavaScriptObject source, String locationId, String prefix, String distance, String note) {
		showHint((Element) source.cast(), Long.valueOf(locationId), prefix, distance, note, true);
	}

	public static void showHint(final Element relativeObject, final long locationId, final String prefix, final String distance, final boolean showRelativeToTheObject) {
		showHint(relativeObject, locationId, prefix, distance, null, showRelativeToTheObject);
	}

	public static void showHint(final Element relativeObject, final long locationId, final String prefix, final String distance, final String note, final boolean showRelativeToTheObject) {
		sLastLocationId = locationId;
		sShowHint = true;
		RPC.execute(RoomInterface.RoomHintRequest.load(locationId), new AsyncCallback<RoomInterface.RoomHintResponse>() {
			@Override
			public void onFailure(Throwable caught) {
			}
			
			@Override
			public void onSuccess(RoomInterface.RoomHintResponse result) {
				if (result != null && locationId == sLastLocationId && sShowHint) {
					if (note != null) result.setNote(note);
					GwtHint.showHint(relativeObject, content(result, prefix, distance), showRelativeToTheObject);
				}
			}
		});
	}
	
	public static void hideHint() {
		sShowHint = false;
		if (sLastSwapper != null) {
			sLastSwapper.cancel(); sLastSwapper = null;
		}
		GwtHint.hideHint();
	}
		
	public static native void createTriggers()/*-{
	$wnd.showGwtRoomHint = function(source, content, prefix, distance, note) {
		@org.unitime.timetable.gwt.client.rooms.RoomHint::_showRoomHint(Lcom/google/gwt/core/client/JavaScriptObject;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)(source, content, prefix, distance, note);
	};
	$wnd.hideGwtRoomHint = function() {
		@org.unitime.timetable.gwt.client.rooms.RoomHint::hideHint()();
	};
	}-*/;
	
	private static class ImageSwapper extends Timer {
		Image iImage;
		List<String> iUrls;
		int iIndex;
		
		ImageSwapper(Image image, List<String> urls) {
			iImage = image; iUrls = urls; iIndex = 0;
		}
		
		@Override
		public void run() {
			iIndex ++;
			iImage.setUrl(iUrls.get(iIndex % iUrls.size()));
			if (!iImage.isAttached()) cancel();
		}
	}

}
