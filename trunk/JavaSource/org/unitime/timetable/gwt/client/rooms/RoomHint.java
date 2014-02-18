/*
 * UniTime 3.4 - 3.5 (University Timetabling Application)
 * Copyright (C) 2012 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.gwt.client.rooms;

import org.unitime.timetable.gwt.client.GwtHint;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.RoomInterface;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.Element;
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
	private static boolean sShowHint = false;
	
	public static Widget content(RoomInterface.RoomHintResponse room, String prefix, String distance) {
		SimpleForm form = new SimpleForm();
		form.removeStyleName("unitime-NotPrintableBottomLine");
		form.addRow(new Label((prefix == null || prefix.isEmpty() ? "" : prefix + " ") + (room.hasDisplayName() || room.hasRoomTypeLabel() ? MESSAGES.label(room.getLabel(), room.hasDisplayName() ? room.getDisplayName() : room.getRoomTypeLabel()) : room.getLabel()), false));
		if (room.hasMiniMapUrl()) {
			Image image = new Image(room.getMiniMapUrl());
			image.setStyleName("minimap");
			form.addRow(image);
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

		if (room.hasFeatures())
			for (String name: room.getFeatureNames())
				form.addRow(name + ":", new Label(room.getFeatures(name)));

		if (room.hasGroups())
			form.addRow(MESSAGES.propRoomGroups(), new Label(room.getGroups()));
		
		if (room.hasEventStatus())
			form.addRow(MESSAGES.propRoomEventStatus(), new Label(room.getEventStatus()));
		
		if (room.hasEventDepartment())
			form.addRow(MESSAGES.propRoomEventDepartment(), new Label(room.getEventDepartment()));

		if (room.hasBreakTime())
			form.addRow(MESSAGES.propRoomBreakTime(), new Label(MESSAGES.breakTime(room.getBreakTime().toString())));
				
		if (room.hasNote())
			form.addRow(new HTML(room.getNote()));
		
		if (room.isIgnoreRoomCheck())
			form.addRow(new HTML(MESSAGES.ignoreRoomCheck()));
		
		if (distance != null && !distance.isEmpty() && !"0".equals(distance))
			form.addRow(MESSAGES.propRoomDistance(), new Label(MESSAGES.roomDistance(distance), false));
		
		SimplePanel panel = new SimplePanel(form);
		panel.setStyleName("unitime-RoomHint");
		return panel;
	}
	
	/** Never use from GWT code */
	public static void _showRoomHint(JavaScriptObject source, String locationId, String prefix, String distance) {
		showHint((Element) source.cast(), Long.valueOf(locationId), prefix, distance);
	}
	
	public static void showHint(final Element relativeObject, final long locationId, final String prefix, final String distance) {
		sLastLocationId = locationId;
		sShowHint = true;
		RPC.execute(RoomInterface.RoomHintRequest.load(locationId), new AsyncCallback<RoomInterface.RoomHintResponse>() {
			@Override
			public void onFailure(Throwable caught) {
			}
			
			@Override
			public void onSuccess(RoomInterface.RoomHintResponse result) {
				if (result != null && locationId == sLastLocationId && sShowHint)
					GwtHint.showHint(relativeObject, content(result, prefix, distance));
			}
		});
	}
	
	public static void hideHint() {
		sShowHint = false;
		GwtHint.hideHint();
	}
		
	public static native void createTriggers()/*-{
	$wnd.showGwtRoomHint = function(source, content, prefix, distance) {
		@org.unitime.timetable.gwt.client.rooms.RoomHint::_showRoomHint(Lcom/google/gwt/core/client/JavaScriptObject;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)(source, content, prefix, distance);
	};
	$wnd.hideGwtRoomHint = function() {
		@org.unitime.timetable.gwt.client.rooms.RoomHint::hideHint()();
	};
	}-*/;

}
